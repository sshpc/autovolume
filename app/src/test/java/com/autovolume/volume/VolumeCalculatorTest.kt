package com.autovolume.volume

import com.autovolume.model.AppSettings
import com.autovolume.model.RunMode
import org.junit.Assert.*
import org.junit.Test

/**
 * VolumeCalculator 单元测试
 *
 * 测试音量计算核心算法：
 * - 分段线性插值映射
 * - 音量步进计算
 * - 夜间模式判断
 * - 自适应采样间隔计算
 */
class VolumeCalculatorTest {

    // 默认映射参数：30dB→20%, 50dB→40%, 70dB→70%, 90dB→100%
    private val defaultSettings = AppSettings()

    // ==================== 映射算法测试 ====================

    @Test
    fun `mapNoiseToVolume below lowest returns lowest volume`() {
        // 低于 30dB → 20%
        assertEquals(20, VolumeCalculator.mapNoiseToVolume(20f, defaultSettings))
        assertEquals(20, VolumeCalculator.mapNoiseToVolume(10f, defaultSettings))
        assertEquals(20, VolumeCalculator.mapNoiseToVolume(0f, defaultSettings))
    }

    @Test
    fun `mapNoiseToVolume at exact control points`() {
        assertEquals(20, VolumeCalculator.mapNoiseToVolume(30f, defaultSettings))
        assertEquals(40, VolumeCalculator.mapNoiseToVolume(50f, defaultSettings))
        assertEquals(70, VolumeCalculator.mapNoiseToVolume(70f, defaultSettings))
        assertEquals(100, VolumeCalculator.mapNoiseToVolume(90f, defaultSettings))
    }

    @Test
    fun `mapNoiseToVolume above highest returns highest volume`() {
        assertEquals(100, VolumeCalculator.mapNoiseToVolume(100f, defaultSettings))
        assertEquals(100, VolumeCalculator.mapNoiseToVolume(120f, defaultSettings))
    }

    @Test
    fun `mapNoiseToVolume linear interpolation in low range`() {
        // 30dB→20%, 50dB→40%, 40dB 中间点 → 30%
        assertEquals(30, VolumeCalculator.mapNoiseToVolume(40f, defaultSettings))
    }

    @Test
    fun `mapNoiseToVolume linear interpolation in mid range`() {
        // 50dB→40%, 70dB→70%, 60dB 中间点 → 55%
        assertEquals(55, VolumeCalculator.mapNoiseToVolume(60f, defaultSettings))
    }

    @Test
    fun `mapNoiseToVolume linear interpolation in high range`() {
        // 70dB→70%, 90dB→100%, 80dB 中间点 → 85%
        assertEquals(85, VolumeCalculator.mapNoiseToVolume(80f, defaultSettings))
    }

    @Test
    fun `mapNoiseToVolume with custom settings`() {
        val custom = AppSettings(
            noiseMappingLow = 40, volumeMappingLow = 10,
            noiseMappingMid = 60, volumeMappingMid = 50,
            noiseMappingHigh = 80, volumeMappingHigh = 80,
            noiseMappingMax = 100, volumeMappingMax = 100
        )
        assertEquals(10, VolumeCalculator.mapNoiseToVolume(40f, custom))
        assertEquals(50, VolumeCalculator.mapNoiseToVolume(60f, custom))
        assertEquals(30, VolumeCalculator.mapNoiseToVolume(50f, custom)) // 中间点
        assertEquals(80, VolumeCalculator.mapNoiseToVolume(80f, custom))
        assertEquals(100, VolumeCalculator.mapNoiseToVolume(100f, custom))
    }

    // ==================== 线性插值测试 ====================

    @Test
    fun `interpolate basic calculation`() {
        // 50 在 30-70 之间，比例 0.5，v1=20, v2=70 → 20 + 0.5*50 = 45
        assertEquals(45, VolumeCalculator.interpolate(50f, 30f, 70f, 20, 70))
    }

    @Test
    fun `interpolate at start`() {
        assertEquals(20, VolumeCalculator.interpolate(30f, 30f, 70f, 20, 70))
    }

    @Test
    fun `interpolate at end`() {
        assertEquals(70, VolumeCalculator.interpolate(70f, 30f, 70f, 20, 70))
    }

    @Test
    fun `interpolate with same n1 n2 returns v1`() {
        assertEquals(50, VolumeCalculator.interpolate(50f, 50f, 50f, 50, 100))
    }

    // ==================== 步进计算测试 ====================

    @Test
    fun `calculateStep returns 0 for small difference`() {
        // 差距 < 2，不调节
        assertEquals(0, VolumeCalculator.calculateStep(51, 50, 5))
        assertEquals(0, VolumeCalculator.calculateStep(49, 50, 5))
    }

    @Test
    fun `calculateStep applies max step limit`() {
        // 差距 20，maxStep 5 → 返回 5
        assertEquals(5, VolumeCalculator.calculateStep(70, 50, 5))
        assertEquals(-5, VolumeCalculator.calculateStep(30, 50, 5))
    }

    @Test
    fun `calculateStep returns actual diff when within limit`() {
        // 差距 3，maxStep 5 → 返回 3
        assertEquals(3, VolumeCalculator.calculateStep(53, 50, 5))
        assertEquals(-3, VolumeCalculator.calculateStep(47, 50, 5))
    }

    // ==================== 夜间模式测试 ====================

    @Test
    fun `isNightTime at midnight`() {
        assertTrue(VolumeCalculator.isNightTime(0))
    }

    @Test
    fun `isNightTime at 3am`() {
        assertTrue(VolumeCalculator.isNightTime(3))
    }

    @Test
    fun `isNightTime at 6_59am`() {
        assertTrue(VolumeCalculator.isNightTime(6))
    }

    @Test
    fun `isNotNightTime at 7am`() {
        assertFalse(VolumeCalculator.isNightTime(7))
    }

    @Test
    fun `isNotNightTime at noon`() {
        assertFalse(VolumeCalculator.isNightTime(12))
    }

    @Test
    fun `isNotNightTime at 21_59`() {
        assertFalse(VolumeCalculator.isNightTime(21))
    }

    @Test
    fun `isNightTime at 22`() {
        assertTrue(VolumeCalculator.isNightTime(22))
    }

    @Test
    fun `isNightTime at 23`() {
        assertTrue(VolumeCalculator.isNightTime(23))
    }

    @Test
    fun `applyNightLimit during night`() {
        // 夜间，音量 80%，限制 60% → 60
        assertEquals(60, VolumeCalculator.applyNightLimit(80, 60, 2))
    }

    @Test
    fun `applyNightLimit during day returns original`() {
        // 白天，音量 80%，限制 60% → 80（不限制）
        assertEquals(80, VolumeCalculator.applyNightLimit(80, 60, 12))
    }

    @Test
    fun `applyNightLimit during night within limit`() {
        // 夜间，音量 40%，限制 60% → 40（未超限）
        assertEquals(40, VolumeCalculator.applyNightLimit(40, 60, 2))
    }

    // ==================== 自适应采样间隔测试 ====================

    @Test
    fun `adaptive interval with stable count below threshold`() {
        // stableCount=2 < threshold=3 → multiplier=1
        val interval = VolumeCalculator.calculateAdaptiveInterval(
            baseInterval = 1000, stableCount = 2, stableThreshold = 3,
            maxMultiplier = 5, isPowerSaving = false, screenOffMultiplier = 5
        )
        assertEquals(1000L, interval)
    }

    @Test
    fun `adaptive interval with stable count at threshold`() {
        // stableCount=3, threshold=3 → multiplier=1+(3-3)/3=1
        val interval = VolumeCalculator.calculateAdaptiveInterval(
            baseInterval = 1000, stableCount = 3, stableThreshold = 3,
            maxMultiplier = 5, isPowerSaving = false, screenOffMultiplier = 5
        )
        assertEquals(1000L, interval)
    }

    @Test
    fun `adaptive interval increases with stable count`() {
        // stableCount=6, threshold=3 → multiplier=1+(6-3)/3=2
        val interval = VolumeCalculator.calculateAdaptiveInterval(
            baseInterval = 1000, stableCount = 6, stableThreshold = 3,
            maxMultiplier = 5, isPowerSaving = false, screenOffMultiplier = 5
        )
        assertEquals(2000L, interval)
    }

    @Test
    fun `adaptive interval capped at max multiplier`() {
        // stableCount=100, threshold=3 → multiplier capped at 5
        val interval = VolumeCalculator.calculateAdaptiveInterval(
            baseInterval = 1000, stableCount = 100, stableThreshold = 3,
            maxMultiplier = 5, isPowerSaving = false, screenOffMultiplier = 5
        )
        assertEquals(5000L, interval)
    }

    @Test
    fun `adaptive interval with power saving`() {
        // stableCount=0, power saving on, screenOffMultiplier=5
        val interval = VolumeCalculator.calculateAdaptiveInterval(
            baseInterval = 1000, stableCount = 0, stableThreshold = 3,
            maxMultiplier = 5, isPowerSaving = true, screenOffMultiplier = 5
        )
        assertEquals(5000L, interval)
    }

    @Test
    fun `adaptive interval with both adaptive and power saving`() {
        // stableCount=6 → adaptive=2, power saving=5 → total=10
        val interval = VolumeCalculator.calculateAdaptiveInterval(
            baseInterval = 1000, stableCount = 6, stableThreshold = 3,
            maxMultiplier = 5, isPowerSaving = true, screenOffMultiplier = 5
        )
        assertEquals(10000L, interval)
    }

    @Test
    fun `adaptive interval capped at 30 seconds`() {
        // baseInterval=10000, stableCount=100 → 50000, capped at 30000
        val interval = VolumeCalculator.calculateAdaptiveInterval(
            baseInterval = 10000, stableCount = 100, stableThreshold = 3,
            maxMultiplier = 5, isPowerSaving = false, screenOffMultiplier = 5
        )
        assertEquals(30000L, interval)
    }

    // ==================== RunMode 预设测试 ====================

    @Test
    fun `power save preset has low sample rate`() {
        val preset = AppSettings.getPresetForMode(RunMode.POWER_SAVE)
        assertEquals(16000, preset.sampleRate)
        assertEquals(3000L, preset.detectionIntervalMs)
    }

    @Test
    fun `balanced preset has medium settings`() {
        val preset = AppSettings.getPresetForMode(RunMode.BALANCED)
        assertEquals(22050, preset.sampleRate)
        assertEquals(1000L, preset.detectionIntervalMs)
    }

    @Test
    fun `high response preset has high sample rate`() {
        val preset = AppSettings.getPresetForMode(RunMode.HIGH_RESPONSE)
        assertEquals(44100, preset.sampleRate)
        assertEquals(500L, preset.detectionIntervalMs)
    }

    @Test
    fun `custom preset uses defaults`() {
        val preset = AppSettings.getPresetForMode(RunMode.CUSTOM)
        assertEquals(RunMode.CUSTOM, preset.runMode)
        assertEquals(44100, preset.sampleRate) // 默认值
    }
}
