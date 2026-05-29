package com.autovolume.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import com.autovolume.model.AppSettings
import com.autovolume.model.DetectionResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 环境噪音分析器
 *
 * 核心职责：
 * 1. 通过 AudioRecord 实时采集麦克风音频数据
 * 2. 计算 RMS（均方根）值并转换为分贝 (dB)
 * 3. 应用平滑算法（指数移动平均）防止数值跳动
 * 4. 通过 Flow 对外提供检测结果
 * 5. 动态自适应采样：环境稳定时自动降低检测频率节省功耗
 *
 * 动态自适应采样算法：
 * - 连续 N 次检测环境稳定（变化 < 阈值）→ 逐步延长检测间隔
 * - 检测到环境突变 → 立即恢复到最短检测间隔
 * - 延长范围：基础间隔 ~ 基础间隔 × maxMultiplier
 * - 屏幕关闭时配合 smartPowerSaving 进一步延长
 */
class AudioAnalyzer(private val context: Context) {

    companion object {
        private const val TAG = "AudioAnalyzer"
        private const val DB_REFERENCE = 1.0
        private const val SILENCE_THRESHOLD = 0.0001
        private const val DB_FLOOR = 20.0f
        private const val DB_CEILING = 110.0f
        private const val SPL_OFFSET = 90.0f

        /** 自适应采样：稳定计数阈值 */
        private const val ADAPTIVE_STABLE_THRESHOLD = 3

        /** 自适应采样：最大间隔倍数 */
        private const val ADAPTIVE_MAX_MULTIPLIER = 5

        /** 最大连续错误次数，超过后停止检测 */
        private const val MAX_CONSECUTIVE_ERRORS = 10
    }

    // ==================== 状态 ====================

    private var audioRecord: AudioRecord? = null
    private var analysisJob: Job? = null
    private var currentSettings = AppSettings()

    /** 平滑后的 dB 值 */
    private var smoothedDb = 0f

    /** 上一次的原始 dB 值 */
    private var lastRawDb = 0f

    /** 自适应采样：连续稳定计数 */
    private var stableCount = 0

    /** 自适应采样：当前使用的间隔倍数 */
    private var currentIntervalMultiplier = 1

    /** 是否被屏幕关闭节能模式影响 */
    private var isPowerSaving = false

    // ==================== Flow 输出 ====================

    private val _detectionResult = MutableStateFlow(DetectionResult(0f))
    val detectionResult: StateFlow<DetectionResult> = _detectionResult.asStateFlow()

    // ==================== 外部控制 ====================

    /**
     * 设置屏幕关闭节能状态
     *
     * 屏幕关闭时可配合自适应采样进一步延长间隔
     */
    fun setPowerSaving(enabled: Boolean) {
        isPowerSaving = enabled
    }

    // ==================== 配置更新 ====================

    fun updateSettings(settings: AppSettings) {
        val needRestart = currentSettings.sampleRate != settings.sampleRate ||
                currentSettings.smoothingFactor != settings.smoothingFactor ||
                currentSettings.noiseThreshold != settings.noiseThreshold

        currentSettings = settings

        if (needRestart && analysisJob?.isActive == true) {
            stop()
            start()
        }
    }

    // ==================== 启动/停止 ====================

    fun start(scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())) {
        if (analysisJob?.isActive == true) {
            Log.w(TAG, "AudioAnalyzer 已经在运行")
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "缺少 RECORD_AUDIO 权限，无法启动检测")
            return
        }

        try {
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(
                currentSettings.sampleRate, channelConfig, audioFormat
            )

            if (minBufferSize == AudioRecord.ERROR_BAD_VALUE ||
                minBufferSize == AudioRecord.ERROR) {
                Log.e(TAG, "无效的音频参数: sampleRate=${currentSettings.sampleRate}")
                return
            }

            val bufferSize = minBufferSize * 2

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                currentSettings.sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord 初始化失败")
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            Log.i(TAG, "AudioRecord 启动成功: sampleRate=${currentSettings.sampleRate}, " +
                    "bufferSize=$bufferSize")

            // 重置自适应状态
            stableCount = 0
            currentIntervalMultiplier = 1

            analysisJob = scope.launch {
                analyzeLoop()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "录音权限被拒绝", e)
        } catch (e: Exception) {
            Log.e(TAG, "启动 AudioRecord 失败", e)
            releaseAudioRecord()
        }
    }

    fun stop() {
        analysisJob?.cancel()
        analysisJob = null
        releaseAudioRecord()
        stableCount = 0
        currentIntervalMultiplier = 1
        Log.i(TAG, "AudioAnalyzer 已停止")
    }

    private fun releaseAudioRecord() {
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "停止 AudioRecord 异常", e)
        }
        try {
            audioRecord?.release()
        } catch (e: Exception) {
            Log.w(TAG, "释放 AudioRecord 异常", e)
        }
        audioRecord = null
    }

    // ==================== 核心分析循环 ====================

    /**
     * 分析循环
     *
     * 核心改进：
     * - 动态自适应采样：环境稳定时自动延长检测间隔
     * - 环境突变时立即恢复最短间隔
     * - 屏幕关闭节能模式叠加
     * - 连续错误计数 + 自动恢复
     */
    private suspend fun analyzeLoop() {
        val record = audioRecord ?: return

        val samplesPerRead = (currentSettings.sampleRate *
                currentSettings.detectionIntervalMs / 1000).toInt()
            .coerceAtLeast(1024)

        val buffer = ShortArray(samplesPerRead)
        var consecutiveErrors = 0

        while (analysisJob?.isActive == true) {
            try {
                // 检查 AudioRecord 状态
                if (audioRecord?.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                    Log.w(TAG, "AudioRecord 不在录音状态，尝试重新启动")
                    if (!restartAudioRecord()) {
                        delay(5000)
                        continue
                    }
                }

                val bytesRead = record.read(buffer, 0, buffer.size)

                when {
                    bytesRead > 0 -> {
                        consecutiveErrors = 0 // 重置错误计数
                        val rms = calculateRms(buffer, bytesRead)
                        val rawDb = rmsToDecibels(rms)
                        smoothedDb = smooth(smoothedDb, rawDb)

                        val isStable = isEnvironmentStable(rawDb)
                        updateAdaptiveState(isStable)

                        _detectionResult.value = DetectionResult(
                            dbLevel = smoothedDb.coerceIn(DB_FLOOR, DB_CEILING),
                            isStable = isStable
                        )
                    }
                    bytesRead == AudioRecord.ERROR_INVALID_OPERATION -> {
                        Log.e(TAG, "AudioRecord ERROR_INVALID_OPERATION")
                        consecutiveErrors++
                    }
                    bytesRead == AudioRecord.ERROR_BAD_VALUE -> {
                        Log.e(TAG, "AudioRecord ERROR_BAD_VALUE")
                        consecutiveErrors++
                    }
                    bytesRead == AudioRecord.ERROR_DEAD_OBJECT -> {
                        Log.e(TAG, "AudioRecord ERROR_DEAD_OBJECT，需要重新创建")
                        consecutiveErrors++
                        releaseAudioRecord()
                        delay(2000)
                        if (!restartAudioRecord()) {
                            delay(5000)
                        }
                    }
                    bytesRead < 0 -> {
                        Log.e(TAG, "AudioRecord 未知错误: $bytesRead")
                        consecutiveErrors++
                    }
                }

                // 连续错误过多，停止检测
                if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                    Log.e(TAG, "连续 $MAX_CONSECUTIVE_ERRORS 次错误，停止 AudioAnalyzer")
                    break
                }

                val actualDelay = calculateActualDelay()
                delay(actualDelay)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "音频分析异常", e)
                consecutiveErrors++
                if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                    Log.e(TAG, "连续错误过多，停止 AudioAnalyzer")
                    break
                }
                delay(1000)
            }
        }
    }

    /**
     * 重新启动 AudioRecord
     *
     * 当 AudioRecord 进入错误状态时，尝试重新创建并启动。
     * @return true 表示重新启动成功
     */
    private fun restartAudioRecord(): Boolean {
        return try {
            releaseAudioRecord()
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(
                currentSettings.sampleRate, channelConfig, audioFormat
            )
            if (minBufferSize == AudioRecord.ERROR_BAD_VALUE ||
                minBufferSize == AudioRecord.ERROR) {
                Log.e(TAG, "重新创建 AudioRecord 失败：无效参数")
                return false
            }

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                currentSettings.sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "重新创建 AudioRecord 初始化失败")
                releaseAudioRecord()
                return false
            }

            audioRecord?.startRecording()
            Log.i(TAG, "AudioRecord 重新启动成功")
            true
        } catch (e: Exception) {
            Log.e(TAG, "重新启动 AudioRecord 异常", e)
            false
        }
    }

    /**
     * 计算实际检测间隔
     *
     * 考虑三个因素：
     * 1. 基础间隔（用户设置）
     * 2. 自适应采样倍数（环境稳定时自动延长）
     * 3. 屏幕关闭节能倍数（smartPowerSaving 开启时叠加）
     *
     * 最终间隔 = 基础间隔 × 自适应倍数 × 节能倍数
     * 限制最大不超过 30 秒
     */
    private fun calculateActualDelay(): Long {
        val baseInterval = currentSettings.detectionIntervalMs
        val adaptiveMultiplier = currentIntervalMultiplier.toLong()
        val powerSaveMultiplier = if (isPowerSaving && currentSettings.smartPowerSaving) {
            currentSettings.screenOffIntervalMultiplier.toLong()
        } else {
            1L
        }

        val totalMultiplier = adaptiveMultiplier * powerSaveMultiplier
        return (baseInterval * totalMultiplier).coerceAtMost(30_000L)
    }

    /**
     * 更新自适应采样状态
     *
     * - 环境稳定：递增 stableCount，超过阈值后逐步增大间隔倍数
     * - 环境突变：重置 stableCount，恢复最短间隔
     */
    private fun updateAdaptiveState(isStable: Boolean) {
        if (!currentSettings.adaptiveSampling) {
            currentIntervalMultiplier = 1
            stableCount = 0
            return
        }

        if (isStable) {
            stableCount++
            // 超过阈值后，每再稳定 ADAPTIVE_STABLE_THRESHOLD 次，倍数 +1
            if (stableCount >= ADAPTIVE_STABLE_THRESHOLD) {
                val extraStable = stableCount - ADAPTIVE_STABLE_THRESHOLD
                currentIntervalMultiplier = (1 + extraStable / ADAPTIVE_STABLE_THRESHOLD)
                    .coerceAtMost(ADAPTIVE_MAX_MULTIPLIER)
            }
        } else {
            // 环境突变，立即恢复
            if (stableCount >= ADAPTIVE_STABLE_THRESHOLD) {
                Log.d(TAG, "环境突变，自适应采样恢复最短间隔")
            }
            stableCount = 0
            currentIntervalMultiplier = 1
        }
    }

    // ==================== 数学计算（委托给 AudioCalculator）====================

    private fun calculateRms(samples: ShortArray, count: Int): Double {
        return AudioCalculator.calculateRms(samples, count)
    }

    private fun rmsToDecibels(rms: Double): Float {
        return AudioCalculator.rmsToDecibels(rms)
    }

    private fun smooth(current: Float, new: Float): Float {
        return AudioCalculator.smooth(current, new, currentSettings.smoothingFactor)
    }

    private fun isEnvironmentStable(currentDb: Float): Boolean {
        return AudioCalculator.isEnvironmentStable(currentDb, lastRawDb, currentSettings.noiseThreshold)
    }
}
