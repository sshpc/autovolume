package com.autovolume.headset

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 耳机连接检测器
 *
 * 支持检测：
 * 1. 蓝牙耳机（A2DP / HFP / LE Audio）
 * 2. 有线耳机（3.5mm / USB-C）
 *
 * 实现方式：
 * - 监听系统广播 ACTION_HEADSET_PLUG（有线耳机）
 * - 监听 BluetoothProfile 连接状态（蓝牙耳机）
 * - 直接查询 AudioDeviceInfo（初始状态）
 *
 * 对外暴露 StateFlow<Boolean>，外部 collect 即可获取连接状态。
 */
class HeadsetDetector(private val context: Context) {

    companion object {
        private const val TAG = "HeadsetDetector"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // ==================== 耳机连接状态 Flow ====================

    private val _isHeadsetConnected = MutableStateFlow(false)
    /**
     * 耳机连接状态
     * true = 有任意耳机连接
     * false = 无耳机连接（使用扬声器）
     */
    val isHeadsetConnected: StateFlow<Boolean> = _isHeadsetConnected.asStateFlow()

    private val _connectionType = MutableStateFlow(HeadsetType.NONE)
    /** 当前耳机连接类型 */
    val connectionType: StateFlow<HeadsetType> = _connectionType.asStateFlow()

    // ==================== 耳机类型枚举 ====================

    enum class HeadsetType(val displayName: String) {
        NONE("未连接"),
        BLUETOOTH("蓝牙耳机"),
        WIRED("有线耳机"),
        USB("USB 耳机")
    }

    // ==================== 广播接收器 ====================

    /**
     * 有线耳机插拔广播接收器
     *
     * 监听 ACTION_HEADSET_PLUG：
     * - state=1: 耳机插入
     * - state=0: 耳机拔出
     */
    private val headsetPlugReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", 0)
                Log.d(TAG, "有线耳机状态变化: state=$state")
                // 有线耳机变化时重新检查所有设备
                checkAllDevices()
            }
        }
    }

    /**
     * 蓝牙连接状态广播接收器
     *
     * 监听多种蓝牙相关广播以覆盖不同场景
     */
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED,
                "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED",
                "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED",
                // Android 13+ LE Audio
                "android.bluetooth.action.ACTIVE_DEVICE_CHANGED" -> {
                    Log.d(TAG, "蓝牙状态变化: ${intent.action}")
                    checkAllDevices()
                }
            }
        }
    }

    // ==================== 生命周期 ====================

    /**
     * 启动耳机检测
     *
     * 注册广播接收器并执行初始检查
     */
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

        // 执行初始检查
        checkAllDevices()

        Log.i(TAG, "耳机检测器已启动")
    }

    /**
     * 停止耳机检测
     */
    fun stop() {
        try {
            context.unregisterReceiver(headsetPlugReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "注销有线耳机广播失败", e)
        }
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "注销蓝牙广播失败", e)
        }
        Log.i(TAG, "耳机检测器已停止")
    }

    // ==================== 设备检测 ====================

    /**
     * 综合检查所有音频输出设备
     *
     * 使用 AudioDeviceInfo API 遍历所有已连接的音频设备。
     * 这种方式比单独监听广播更可靠，可以覆盖所有情况。
     */
    private fun checkAllDevices() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        var hasBluetooth = false
        var hasWired = false
        var hasUsb = false

        for (device in devices) {
            when (device.type) {
                // 蓝牙耳机类型
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_HEARING_AID,
                // Android 14+ LE Audio
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLE_SPEAKER -> {
                    hasBluetooth = true
                }

                // 有线耳机类型
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
                    hasWired = true
                }

                // USB 耳机
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_USB_DEVICE -> {
                    hasUsb = true
                }
            }
        }

        // 更新状态（优先级：蓝牙 > 有线 > USB）
        val connected = hasBluetooth || hasWired || hasUsb
        _isHeadsetConnected.value = connected
        _connectionType.value = when {
            hasBluetooth -> HeadsetType.BLUETOOTH
            hasWired -> HeadsetType.WIRED
            hasUsb -> HeadsetType.USB
            else -> HeadsetType.NONE
        }

        Log.d(TAG, "设备检查结果: connected=$connected, type=${_connectionType.value}, " +
                "bt=$hasBluetooth, wired=$hasWired, usb=$hasUsb")
    }
}
