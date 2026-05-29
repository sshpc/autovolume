package com.autovolume.headset

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 耳机连接检测器
 *
 * 双重检测机制：
 * 1. BroadcastReceiver 监听系统广播
 * 2. AudioDeviceCallback 监听音频设备变化（更可靠）
 *
 * 任意机制触发都会重新检查所有设备，确保状态实时刷新。
 */
class HeadsetDetector(private val context: Context) {

    companion object {
        private const val TAG = "HeadsetDetector"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _isHeadsetConnected = MutableStateFlow(false)
    val isHeadsetConnected: StateFlow<Boolean> = _isHeadsetConnected.asStateFlow()

    private val _connectionType = MutableStateFlow(HeadsetType.NONE)
    val connectionType: StateFlow<HeadsetType> = _connectionType.asStateFlow()

    enum class HeadsetType(val displayName: String) {
        NONE("未连接"),
        BLUETOOTH("蓝牙耳机"),
        WIRED("有线耳机"),
        USB("USB 耳机")
    }

    // ==================== BroadcastReceiver ====================

    private val headsetPlugReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", 0)
                Log.d(TAG, "有线耳机广播: state=$state")
                checkAllDevices()
            }
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED,
                "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED",
                "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED",
                "android.bluetooth.action.ACTIVE_DEVICE_CHANGED" -> {
                    Log.d(TAG, "蓝牙广播: ${intent.action}")
                    checkAllDevices()
                }
            }
        }
    }

    // ==================== AudioDeviceCallback ====================

    /**
     * 音频设备变化回调
     * 比 BroadcastReceiver 更可靠，能捕获所有音频设备变化
     */
    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            Log.d(TAG, "音频设备添加: ${addedDevices.size} 个设备")
            checkAllDevices()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            Log.d(TAG, "音频设备移除: ${removedDevices.size} 个设备")
            checkAllDevices()
        }
    }

    // ==================== 生命周期 ====================

    fun start() {
        // 注册有线耳机广播
        val headsetFilter = IntentFilter(AudioManager.ACTION_HEADSET_PLUG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(headsetPlugReceiver, headsetFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(headsetPlugReceiver, headsetFilter)
        }

        // 注册蓝牙广播
        val btFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.action.ACTIVE_DEVICE_CHANGED")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(bluetoothReceiver, btFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(bluetoothReceiver, btFilter)
        }

        // 注册 AudioDeviceCallback（关键修复：确保蓝牙连接后实时刷新）
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))

        // 执行初始检查
        checkAllDevices()

        Log.i(TAG, "耳机检测器已启动（含 AudioDeviceCallback）")
    }

    fun stop() {
        try { context.unregisterReceiver(headsetPlugReceiver) } catch (e: Exception) {
            Log.w(TAG, "注销有线耳机广播失败", e)
        }
        try { context.unregisterReceiver(bluetoothReceiver) } catch (e: Exception) {
            Log.w(TAG, "注销蓝牙广播失败", e)
        }
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        Log.i(TAG, "耳机检测器已停止")
    }

    // ==================== 设备检测 ====================

    private fun checkAllDevices() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        var hasBluetooth = false
        var hasWired = false
        var hasUsb = false

        for (device in devices) {
            when (device.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_HEARING_AID,
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLE_SPEAKER -> hasBluetooth = true

                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> hasWired = true

                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_USB_DEVICE -> hasUsb = true
            }
        }

        val connected = hasBluetooth || hasWired || hasUsb
        val newType = when {
            hasBluetooth -> HeadsetType.BLUETOOTH
            hasWired -> HeadsetType.WIRED
            hasUsb -> HeadsetType.USB
            else -> HeadsetType.NONE
        }

        // 仅在状态变化时更新并打印日志
        if (_isHeadsetConnected.value != connected || _connectionType.value != newType) {
            _isHeadsetConnected.value = connected
            _connectionType.value = newType
            Log.i(TAG, "耳机状态变化: connected=$connected, type=$newType")
        }
    }
}
