package com.autovolume.model

/**
 * 单次环境噪音检测结果
 *
 * @param dbLevel 检测到的分贝值 (dB SPL 近似值)
 * @param timestamp 检测时间戳 (System.currentTimeMillis)
 * @param isStable 当前环境是否稳定（变化幅度低于阈值）
 */
data class DetectionResult(
    val dbLevel: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val isStable: Boolean = true
)

/**
 * 音量调节事件记录
 *
 * 用于调试页面展示调节历史
 *
 * @param fromVolume 调节前音量
 * @param toVolume 调节后音量
 * @param triggerDb 触发调节的环境 dB
 * @param timestamp 时间戳
 */
data class VolumeAdjustmentEvent(
    val fromVolume: Int,
    val toVolume: Int,
    val triggerDb: Float,
    val timestamp: Long = System.currentTimeMillis()
)
