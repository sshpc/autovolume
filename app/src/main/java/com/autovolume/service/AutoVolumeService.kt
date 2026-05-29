package com.autovolume.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.autovolume.MainActivity
import com.autovolume.R
import com.autovolume.audio.AudioAnalyzer
import com.autovolume.datastore.SettingsDataStore
import com.autovolume.headset.HeadsetDetector
import com.autovolume.model.AppSettings
import com.autovolume.volume.VolumeController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 前台服务：核心自动音量调节服务
 *
 * 职责：
 * 1. 运行前台通知保持进程存活
 * 2. 协调 AudioAnalyzer、VolumeController、HeadsetDetector
 * 3. 根据环境噪音自动调节媒体音量
 * 4. 实现智能节能（屏幕关闭时降低频率）
 * 5. 处理 Android 14/15 的前台服务类型
 * 6. 通过 companion object StateFlow 暴露实时状态给 ViewModel
 */
class AutoVolumeService : Service() {

    companion object {
        private const val TAG = "AutoVolumeService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "auto_volume_channel"
        private const val WAKELOCK_TAG = "AutoVolume:DetectionWakeLock"

        const val ACTION_START = "com.autovolume.START"
        const val ACTION_STOP = "com.autovolume.STOP"

        // ==================== 服务状态暴露给 ViewModel ====================

        /** 服务是否正在运行 */
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        /** 当前环境 dB（实时） */
        private val _currentDb = MutableStateFlow(0f)
        val currentDb: StateFlow<Float> = _currentDb.asStateFlow()

        /** 当前音量百分比（实时） */
        private val _currentVolume = MutableStateFlow(0)
        val currentVolume: StateFlow<Int> = _currentVolume.asStateFlow()

        /** 映射目标音量（未限制） */
        private val _rawMappedVolume = MutableStateFlow(0)
        val rawMappedVolume: StateFlow<Int> = _rawMappedVolume.asStateFlow()

        /** 耳机是否连接 */
        private val _headsetConnected = MutableStateFlow(false)
        val headsetConnected: StateFlow<Boolean> = _headsetConnected.asStateFlow()

        /** 耳机类型 */
        private val _headsetType = MutableStateFlow(HeadsetDetector.HeadsetType.NONE)
        val headsetType: StateFlow<HeadsetDetector.HeadsetType> = _headsetType.asStateFlow()

        /** 音量调节历史 */
        private val _adjustmentHistory = MutableStateFlow<List<com.autovolume.model.VolumeAdjustmentEvent>>(emptyList())
        val adjustmentHistory: StateFlow<List<com.autovolume.model.VolumeAdjustmentEvent>> = _adjustmentHistory.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, AutoVolumeService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, AutoVolumeService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private lateinit var audioAnalyzer: AudioAnalyzer
    private lateinit var volumeController: VolumeController
    private lateinit var headsetDetector: HeadsetDetector
    private lateinit var settingsDataStore: SettingsDataStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var controlJob: Job? = null
    private var powerSaveJob: Job? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private var screenReceiver: BroadcastReceiver? = null
    private var isScreenOn = true

    /** 上一次通知更新时间，限制频率 */
    private var lastNotificationUpdate = 0L

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "服务 onCreate")

        audioAnalyzer = AudioAnalyzer(this)
        volumeController = VolumeController(this)
        headsetDetector = HeadsetDetector(this)
        settingsDataStore = SettingsDataStore(this)

        createNotificationChannel()
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "服务 onStartCommand: action=${intent?.action}")

        when (intent?.action) {
            ACTION_START -> startDetection()
            ACTION_STOP -> stopSelf()
            else -> startDetection()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.i(TAG, "服务 onDestroy")
        stopDetection()
        serviceScope.cancel()
        releaseWakeLock()
        unregisterScreenReceiver()
        super.onDestroy()
    }

    /**
     * Android 15+ 前台服务超时回调
     *
     * 当前台服务运行超过系统限制时间时调用。
     * 我们优雅降级：保存状态，停止服务，通知用户。
     */
    override fun onTimeout(startId: Int, fgsType: Int) {
        Log.w(TAG, "前台服务超时: startId=$startId, fgsType=$fgsType")
        // 保存当前状态，以便用户手动重启
        serviceScope.launch {
            settingsDataStore.updateServiceWasRunning(true)
        }
        stopSelf()
    }

    // ==================== 核心逻辑 ====================

    private fun startDetection() {
        if (_isRunning.value) {
            Log.w(TAG, "检测已在运行")
            return
        }

        // 提升为前台服务
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    createNotification("正在检测环境噪音..."),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(NOTIFICATION_ID, createNotification("正在检测环境噪音..."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "启动前台服务失败", e)
            stopSelf()
            return
        }

        acquireWakeLock()
        headsetDetector.start()
        audioAnalyzer.start(serviceScope)
        startControlLoop()
        startHeadsetObserver()
        startPowerSaveMonitor()

        _isRunning.value = true
        serviceScope.launch {
            settingsDataStore.updateServiceWasRunning(true)
        }

        Log.i(TAG, "检测已启动")
    }

    private fun stopDetection() {
        controlJob?.cancel()
        powerSaveJob?.cancel()
        audioAnalyzer.stop()
        headsetDetector.stop()
        releaseWakeLock()

        _isRunning.value = false
        _currentDb.value = 0f
        _currentVolume.value = 0
        _rawMappedVolume.value = 0

        serviceScope.launch {
            settingsDataStore.updateServiceWasRunning(false)
        }

        Log.i(TAG, "检测已停止")
    }

    /**
     * 控制循环：协调检测结果、耳机状态、设置，执行音量调节
     *
     * 实时将检测结果、音量状态同步到 companion object StateFlow，
     * 供 ViewModel 观察。
     */
    private fun startControlLoop() {
        controlJob = serviceScope.launch {
            combine(
                settingsDataStore.settingsFlow,
                audioAnalyzer.detectionResult,
                headsetDetector.isHeadsetConnected
            ) { settings, result, headsetConnected ->
                Triple(settings, result, headsetConnected)
            }.collectLatest { (settings, result, headsetConnected) ->
                try {
                    audioAnalyzer.updateSettings(settings)

                    // 同步实时状态到 companion object
                    _currentDb.value = result.dbLevel

                    if (!settings.isEnabled) return@collectLatest
                    if (settings.headsetOnly && !headsetConnected) return@collectLatest

                    volumeController.adjustVolume(result.dbLevel, settings)

                    // 同步音量状态
                    _currentVolume.value = volumeController.getCurrentVolumePercent()
                    _rawMappedVolume.value = volumeController.rawMappedPercent.value
                    _adjustmentHistory.value = volumeController.adjustmentHistory.value

                    // 限频更新通知（每3秒最多一次）
                    maybeUpdateNotification(result.dbLevel, _currentVolume.value, settings)

                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "控制循环异常", e)
                }
            }
        }
    }

    /**
     * 耳机状态观察器：实时同步耳机连接状态到 companion object
     */
    private fun startHeadsetObserver() {
        serviceScope.launch {
            headsetDetector.isHeadsetConnected.collect { connected ->
                _headsetConnected.value = connected
            }
        }
        serviceScope.launch {
            headsetDetector.connectionType.collect { type ->
                _headsetType.value = type
            }
        }
    }

    /**
     * 智能节能监控：屏幕关闭时降低检测频率
     *
     * 根据 isScreenOn 和 settings 智能调节 AudioAnalyzer 的检测间隔。
     */
    private fun startPowerSaveMonitor() {
        powerSaveJob = serviceScope.launch {
            while (isActive) {
                delay(3000)
                // 将屏幕状态传递给 AudioAnalyzer，联动自适应采样
                audioAnalyzer.setPowerSaving(!isScreenOn)
                if (!isScreenOn) {
                    Log.d(TAG, "屏幕关闭，智能节能模式生效")
                }
            }
        }
    }

    // ==================== 通知管理 ====================

    /**
     * 限频更新通知内容
     *
     * 通知更新频率限制为每 3 秒最多一次，避免过于频繁的系统调用。
     *
     * @param db 当前环境 dB
     * @param volume 当前音量百分比
     * @param settings 当前设置
     */
    private fun maybeUpdateNotification(db: Float, volume: Int, settings: AppSettings) {
        val now = System.currentTimeMillis()
        if (now - lastNotificationUpdate < 3000) return
        lastNotificationUpdate = now

        val headsetInfo = if (_headsetConnected.value) {
            " · ${_headsetType.value.displayName}"
        } else {
            " · 扬声器"
        }

        val statusText = if (settings.headsetOnly && !_headsetConnected.value) {
            "等待耳机连接..."
        } else {
            "%.1f dB → 音量 %d%%%s".format(db, volume, headsetInfo)
        }

        updateNotification(statusText)
    }

    // ==================== 电源管理 ====================

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
            acquire(24 * 60 * 60 * 1000L) // 24小时超时保护
        }
        Log.d(TAG, "WakeLock 已获取")
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Log.d(TAG, "WakeLock 已释放")
            }
        }
        wakeLock = null
    }

    // ==================== 屏幕状态监听 ====================

    private fun registerScreenReceiver() {
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        isScreenOn = true
                        Log.d(TAG, "屏幕打开")
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        isScreenOn = false
                        Log.d(TAG, "屏幕关闭")
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun unregisterScreenReceiver() {
        screenReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
        }
        screenReceiver = null
    }

    // ==================== 通知 ====================

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "自动音量调节",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "显示自动音量调节服务的运行状态"
            setShowBadge(false)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun createNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, AutoVolumeService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoVolume")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "停止", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        try {
            val nm = getSystemService(NotificationManager::class.java)
            nm.notify(NOTIFICATION_ID, createNotification(text))
        } catch (e: Exception) {
            Log.w(TAG, "更新通知失败", e)
        }
    }
}
