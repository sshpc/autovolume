package com.autovolume.volume

import com.autovolume.model.AppSettings
import java.util.Calendar

/**
 * 音量计算工具类
 *
 * 从 VolumeController 中提取的纯数学计算函数，
 * 不依赖 Android API，可直接进行单元测试。
 */
object VolumeCalculator {

    /**
     * 噪音-音量映射算法
     *
     * 使用分段线性插值将环境 dB 映射到音量百分比。
     * 用户定义了 4 个控制点，形成 3 个区间：
     *
     * 区间1: [noiseLow, noiseMid]  → [volLow, volMid]
     * 区间2: [noiseMid, noiseHigh] → [volMid, volHigh]
     * 区间3: [noiseHigh, noiseMax] → [volHigh, volMax]
     *
     * @param db 环境噪音 dB
     * @param settings 设置（包含映射参数）
     * @return 目标音量百分比 (0-100)
     */
    fun mapNoiseToVolume(db: Float, settings: AppSettings): Int {
        val n1 = settings.noiseMappingLow.toFloat()
        val v1 = settings.volumeMappingLow
        val n2 = settings.noiseMappingMid.toFloat()
        val v2 = settings.volumeMappingMid
        val n3 = settings.noiseMappingHigh.toFloat()
        val v3 = settings.volumeMappingHigh
        val n4 = settings.noiseMappingMax.toFloat()
        val v4 = settings.volumeMappingMax

        return when {
            db <= n1 -> v1
            db <= n2 -> interpolate(db, n1, n2, v1, v2)
            db <= n3 -> interpolate(db, n2, n3, v2, v3)
            db <= n4 -> interpolate(db, n3, n4, v3, v4)
            else -> v4
        }
    }

    /**
     * 线性插值计算
     *
     * 公式：result = v1 + (value - n1) / (n2 - n1) * (v2 - v1)
     *
     * @param value 当前输入值
     * @param n1 区间起点
     * @param n2 区间终点
     * @param v1 起点对应输出
     * @param v2 终点对应输出
     * @return 插值结果
     */
    fun interpolate(value: Float, n1: Float, n2: Float, v1: Int, v2: Int): Int {
        if (n2 == n1) return v1
        val ratio = (value - n1) / (n2 - n1)
        return (v1 + ratio * (v2 - v1)).toInt()
    }

    /**
     * 计算音量步进
     *
     * @param targetPercent 目标音量百分比
     * @param currentPercent 当前音量百分比
     * @param maxStep 最大步进
     * @return 实际变化量（带符号，正数=增大，负数=减小）
     */
    fun calculateStep(targetPercent: Int, currentPercent: Int, maxStep: Int): Int {
        val diff = targetPercent - currentPercent
        if (kotlin.math.abs(diff) < 2) return 0
        return diff.coerceIn(-maxStep, maxStep)
    }

    /**
     * 判断当前是否为夜间时段 (22:00 - 07:00)
     *
     * @param hour 当前小时 (0-23)，默认从系统获取
     * @return true 表示夜间
     */
    fun isNightTime(hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): Boolean {
        return hour >= 22 || hour < 7
    }

    /**
     * 应用夜间模式音量限制
     *
     * @param volume 原始音量百分比
     * @param nightMaxVolume 夜间最大音量百分比
     * @param hour 当前小时
     * @return 限制后的音量百分比
     */
    fun applyNightLimit(volume: Int, nightMaxVolume: Int, hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): Int {
        return if (isNightTime(hour)) {
            volume.coerceAtMost(nightMaxVolume)
        } else {
            volume
        }
    }

    /**
     * 计算自适应采样间隔
     *
     * @param baseInterval 基础检测间隔 (ms)
     * @param stableCount 连续稳定次数
     * @param stableThreshold 稳定计数阈值
     * @param maxMultiplier 最大倍数
     * @param isPowerSaving 是否处于节能模式
     * @param screenOffMultiplier 屏幕关闭倍数
     * @return 实际间隔 (ms)，最大 30000
     */
    fun calculateAdaptiveInterval(
        baseInterval: Long,
        stableCount: Int,
        stableThreshold: Int,
        maxMultiplier: Int,
        isPowerSaving: Boolean,
        screenOffMultiplier: Int
    ): Long {
        val adaptiveMultiplier = if (stableCount >= stableThreshold) {
            (1 + (stableCount - stableThreshold) / stableThreshold).coerceAtMost(maxMultiplier)
        } else {
            1
        }

        val powerSaveMultiplier = if (isPowerSaving) screenOffMultiplier.toLong() else 1L
        return (baseInterval * adaptiveMultiplier * powerSaveMultiplier).coerceAtMost(30_000L)
    }
}
