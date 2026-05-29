package com.autovolume.audio

import kotlin.math.log10
import kotlin.math.sqrt

/**
 * 音频计算工具类
 *
 * 从 AudioAnalyzer 中提取的纯数学计算函数，
 * 不依赖 Android API，可直接进行单元测试。
 */
object AudioCalculator {

    /** dB 范围下限 */
    const val DB_FLOOR = 20.0f

    /** dB 范围上限 */
    const val DB_CEILING = 110.0f

    /** 静音阈值 (RMS) */
    private const val SILENCE_THRESHOLD = 0.0001

    /** dB 计算参考值 */
    private const val DB_REFERENCE = 1.0

    /** 噪声参考声压级偏移 */
    private const val SPL_OFFSET = 90.0f

    /**
     * 计算 RMS（均方根值）
     *
     * 公式：RMS = √(Σ(sample_i)² / N)
     *
     * @param samples PCM 16-bit 采样数据
     * @param count 有效采样数量
     * @return RMS 值，范围 0.0 - 1.0（归一化）
     */
    fun calculateRms(samples: ShortArray, count: Int): Double {
        require(count > 0) { "采样数量必须大于 0" }
        require(count <= samples.size) { "采样数量不能超过数组大小" }

        var sum = 0.0
        for (i in 0 until count) {
            val normalized = samples[i].toDouble() / Short.MAX_VALUE
            sum += normalized * normalized
        }
        return sqrt(sum / count)
    }

    /**
     * 将 RMS 值转换为分贝
     *
     * 公式：dB = 20 * log10(RMS / REFERENCE) + SPL_OFFSET
     *
     * @param rms 归一化的 RMS 值
     * @return 近似的 dB SPL 值，范围 [DB_FLOOR, DB_CEILING]
     */
    fun rmsToDecibels(rms: Double): Float {
        if (rms < SILENCE_THRESHOLD) return DB_FLOOR
        val db = (20.0 * log10(rms / DB_REFERENCE) + SPL_OFFSET).toFloat()
        return db.coerceIn(DB_FLOOR, DB_CEILING)
    }

    /**
     * 指数移动平均平滑
     *
     * 公式：smoothed = α × newValue + (1 - α) × smoothed
     *
     * @param current 当前平滑值
     * @param new 新的原始值
     * @param alpha 平滑系数 (0.0 - 1.0)
     * @return 平滑后的值
     */
    fun smooth(current: Float, new: Float, alpha: Float): Float {
        require(alpha in 0.01f..0.99f) { "平滑系数必须在 0.01-0.99 之间" }
        return if (current == 0f) new
        else alpha * new + (1 - alpha) * current
    }

    /**
     * 判断环境是否稳定
     *
     * @param currentDb 当前 dB 值
     * @param lastDb 上一次 dB 值
     * @param threshold 变化阈值 (dB)
     * @return true 表示稳定（变化幅度 < 阈值）
     */
    fun isEnvironmentStable(currentDb: Float, lastDb: Float, threshold: Int): Boolean {
        return kotlin.math.abs(currentDb - lastDb) < threshold
    }
}
