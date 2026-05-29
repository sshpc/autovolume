package com.autovolume.volume

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.autovolume.model.AppSettings
import com.autovolume.model.VolumeAdjustmentEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 媒体音量控制器
 *
 * 核心职责：
 * 1. 根据环境 dB 计算目标媒体音量
 * 2. 通过 AudioManager 设置 STREAM_MUSIC 音量
 * 3. 实现平滑调节（步进限制、冷却时间）
 * 4. 应用安全限制（最小/最大音量、夜间模式）
 *
 * 音量映射算法：
 * 使用分段线性插值，将环境 dB 映射到音量百分比。
 * 用户设置 4 个控制点 (dB → volume%)，中间值线性插值。
 * 插值公式：volume% = vol1 + (db - db1) / (db2 - db1) * (vol2 - vol1)
 */
class VolumeController(private val context: Context) {

    companion object {
        private const val TAG = "VolumeController"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // ==================== 状态 ====================

    /** 当前音量百分比 (0-100) */
    private var currentVolumePercent = 0

    /** 上次调节时间戳 */
    private var lastAdjustmentTime = 0L

    /** 最近的调节事件列表（用于调试显示） */
    private val _adjustmentHistory = MutableStateFlow<List<VolumeAdjustmentEvent>>(emptyList())
    val adjustmentHistory: StateFlow<List<VolumeAdjustmentEvent>> = _adjustmentHistory.asStateFlow()

    /** 当前映射结果（未应用限制的原始值，用于调试） */
    private val _rawMappedPercent = MutableStateFlow(0)
    val rawMappedPercent: StateFlow<Int> = _rawMappedPercent.asStateFlow()

    // ==================== 初始化 ====================

    init {
        // 读取当前系统媒体音量
        refreshCurrentVolume()
    }

    /**
     * 从系统读取当前媒体音量百分比
     */
    fun refreshCurrentVolume() {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        currentVolumePercent = if (maxVol > 0) (currentVol * 100 / maxVol) else 0
    }

    // ==================== 核心方法 ====================

    /**
     * 根据环境噪音调节媒体音量
     *
     * 完整流程：
     * 1. 通过映射曲线计算目标音量
     * 2. 应用安全限制（最小/最大/夜间）
     * 3. 检查冷却时间
     * 4. 应用步进限制（防止突然变化）
     * 5. 设置系统音量
     * 6. 记录调节事件
     *
     * @param dbLevel 当前环境噪音 dB
     * @param settings 当前应用设置
     */
    fun adjustVolume(dbLevel: Float, settings: AppSettings) {
        // 步骤1：通过映射曲线计算目标音量
        val mappedPercent = mapNoiseToVolume(dbLevel, settings)
        _rawMappedPercent.value = mappedPercent

        // 步骤2：应用安全限制
        var targetPercent = mappedPercent

        // 夜间模式限制
        if (settings.nightModeEnabled) {
            targetPercent = VolumeCalculator.applyNightLimit(targetPercent, settings.nightMaxVolumePercent)
            if (VolumeCalculator.isNightTime()) {
                Log.d(TAG, "夜间模式生效，最大音量限制为 ${settings.nightMaxVolumePercent}%")
            }
        }

        // 最小/最大限制
        targetPercent = targetPercent.coerceIn(settings.minVolumePercent, settings.maxVolumePercent)

        // 步骤3：检查冷却时间
        val now = System.currentTimeMillis()
        if (now - lastAdjustmentTime < settings.cooldownMs) {
            return // 还在冷却中，跳过本次调节
        }

        // 步骤4：计算实际步进
        val diff = targetPercent - currentVolumePercent
        if (kotlin.math.abs(diff) < 2) {
            return // 差距太小，不需要调节
        }

        // 步进限制：限制单次最大变化量
        val actualStep = diff.coerceIn(
            -settings.maxVolumeStep,
            settings.maxVolumeStep
        )

        // 步骤5：计算新音量并设置
        val newPercent = (currentVolumePercent + actualStep).coerceIn(0, 100)
        setSystemVolume(newPercent)

        // 步骤6：记录调节事件
        if (newPercent != currentVolumePercent) {
            recordAdjustment(currentVolumePercent, newPercent, dbLevel)
            currentVolumePercent = newPercent
            lastAdjustmentTime = now

            Log.d(TAG, "音量调节: ${currentVolumePercent - actualStep}% → $newPercent% " +
                    "(环境 ${dbLevel}dB, 目标 $mappedPercent%)")
        }
    }

    /**
     * 获取当前音量百分比
     */
    fun getCurrentVolumePercent(): Int {
        refreshCurrentVolume()
        return currentVolumePercent
    }

    /**
     * 获取最大音量级别（系统定义）
     */
    fun getMaxStreamVolume(): Int =
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    /**
     * 获取当前音量级别（0 ~ max）
     */
    fun getCurrentStreamVolume(): Int =
        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    // ==================== 映射算法（委托给 VolumeCalculator）====================

    private fun mapNoiseToVolume(db: Float, settings: AppSettings): Int {
        return VolumeCalculator.mapNoiseToVolume(db, settings)
    }

    // ==================== 系统音量设置 =====================

    /**
     * 设置系统媒体音量
     *
     * 使用 AudioManager.setStreamVolume 直接设置 STREAM_MUSIC。
     * 使用 FLAG_SHOW_UI 显示系统音量条（可选）。
     */
    private fun setSystemVolume(percent: Int) {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVol = (percent * maxVol / 100).coerceIn(0, maxVol)

        try {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                targetVol,
                0 // 不显示 UI，因为我们有自己的调试面板
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "设置音量失败：权限不足", e)
        } catch (e: Exception) {
            Log.e(TAG, "设置音量失败", e)
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 记录调节事件
     *
     * 保留最近 50 条记录，用于调试面板展示。
     */
    private fun recordAdjustment(from: Int, to: Int, db: Float) {
        val event = VolumeAdjustmentEvent(from, to, db)
        val history = _adjustmentHistory.value.toMutableList()
        history.add(0, event) // 新事件添加到头部
        if (history.size > 50) {
            history.removeAt(history.lastIndex) // 移除最旧的
        }
        _adjustmentHistory.value = history
    }
}
