package com.autovolume.audio

import org.junit.Assert.*
import org.junit.Test

/**
 * AudioCalculator 单元测试
 *
 * 测试音频计算核心算法：
 * - RMS（均方根）计算
 * - RMS → dB 转换
 * - 指数移动平均平滑
 * - 环境稳定性判断
 */
class AudioCalculatorTest {

    // ==================== RMS 计算测试 ====================

    @Test
    fun `calculateRms with silence returns zero`() {
        val samples = ShortArray(1024) { 0 }
        val rms = AudioCalculator.calculateRms(samples, 1024)
        assertEquals(0.0, rms, 0.0001)
    }

    @Test
    fun `calculateRms with max amplitude returns one`() {
        val samples = ShortArray(1024) { Short.MAX_VALUE }
        val rms = AudioCalculator.calculateRms(samples, 1024)
        assertEquals(1.0, rms, 0.0001)
    }

    @Test
    fun `calculateRms with half amplitude`() {
        // 半振幅 16383 → RMS = 16383/32767 ≈ 0.5
        val samples = ShortArray(1024) { (Short.MAX_VALUE / 2).toShort() }
        val rms = AudioCalculator.calculateRms(samples, 1024)
        assertEquals(0.5, rms, 0.01)
    }

    @Test
    fun `calculateRms with partial count`() {
        val samples = ShortArray(1024) { Short.MAX_VALUE }
        // 只使用前 512 个采样
        val rms = AudioCalculator.calculateRms(samples, 512)
        assertEquals(1.0, rms, 0.0001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculateRms with zero count throws`() {
        val samples = ShortArray(1024)
        AudioCalculator.calculateRms(samples, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculateRms with count exceeding array throws`() {
        val samples = ShortArray(100)
        AudioCalculator.calculateRms(samples, 200)
    }

    // ==================== dB 转换测试 ====================

    @Test
    fun `rmsToDecibels with silence returns floor`() {
        val db = AudioCalculator.rmsToDecibels(0.00001)
        assertEquals(AudioCalculator.DB_FLOOR, db, 0.01f)
    }

    @Test
    fun `rmsToDecibels with full scale`() {
        // RMS = 1.0 → 20*log10(1.0/1.0) + 90 = 0 + 90 = 90 dB
        val db = AudioCalculator.rmsToDecibels(1.0)
        assertEquals(90.0f, db, 0.1f)
    }

    @Test
    fun `rmsToDecibels with half scale`() {
        // RMS = 0.5 → 20*log10(0.5) + 90 = -6.02 + 90 ≈ 83.98
        val db = AudioCalculator.rmsToDecibels(0.5)
        assertEquals(83.98f, db, 0.5f)
    }

    @Test
    fun `rmsToDecibels is clamped to ceiling`() {
        // 非常大的 RMS 不应超过 DB_CEILING
        val db = AudioCalculator.rmsToDecibels(100.0)
        assertEquals(AudioCalculator.DB_CEILING, db, 0.01f)
    }

    @Test
    fun `rmsToDecibels is clamped to floor`() {
        val db = AudioCalculator.rmsToDecibels(0.00001)
        assertEquals(AudioCalculator.DB_FLOOR, db, 0.01f)
    }

    // ==================== 平滑算法测试 ====================

    @Test
    fun `smooth first value returns new value`() {
        // current == 0 时直接返回 new
        val result = AudioCalculator.smooth(0f, 50f, 0.3f)
        assertEquals(50f, result, 0.01f)
    }

    @Test
    fun `smooth with alpha 1 returns new value`() {
        // alpha = 1.0 完全使用新值
        val result = AudioCalculator.smooth(30f, 80f, 0.99f)
        assertEquals(80f, result, 1f)
    }

    @Test
    fun `smooth with alpha 0 keeps current`() {
        // alpha ≈ 0 几乎保持原值
        val result = AudioCalculator.smooth(30f, 80f, 0.01f)
        assertEquals(30f, result, 1f)
    }

    @Test
    fun `smooth with alpha 0_3`() {
        // smoothed = 0.3 * 80 + 0.7 * 30 = 24 + 21 = 45
        val result = AudioCalculator.smooth(30f, 80f, 0.3f)
        assertEquals(45f, result, 0.1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `smooth with invalid alpha throws`() {
        AudioCalculator.smooth(30f, 80f, 1.5f)
    }

    // ==================== 环境稳定性测试 ====================

    @Test
    fun `isEnvironmentStable with same value returns true`() {
        assertTrue(AudioCalculator.isEnvironmentStable(50f, 50f, 5))
    }

    @Test
    fun `isEnvironmentStable with small change returns true`() {
        assertTrue(AudioCalculator.isEnvironmentStable(52f, 50f, 5))
    }

    @Test
    fun `isEnvironmentStable with exact threshold returns false`() {
        // |55 - 50| = 5，不小于 5，所以是 false
        assertFalse(AudioCalculator.isEnvironmentStable(55f, 50f, 5))
    }

    @Test
    fun `isEnvironmentStable with large change returns false`() {
        assertFalse(AudioCalculator.isEnvironmentStable(80f, 50f, 5))
    }

    @Test
    fun `isEnvironmentStable handles negative change`() {
        assertTrue(AudioCalculator.isEnvironmentStable(48f, 50f, 5))
    }
}
