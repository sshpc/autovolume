package com.autovolume.model

/**
 * 主题模式枚举
 */
enum class ThemeMode(val displayName: String) {
    LIGHT("浅色模式"),
    DARK("深色模式"),
    SYSTEM("跟随系统");

    companion object {
        fun fromString(value: String): ThemeMode =
            entries.find { it.name == value } ?: SYSTEM
    }
}

/**
 * 音量配置方案
 *
 * 每个配置独立保存灵敏度、音量范围、映射曲线、响应速度等参数。
 */
data class VolumeProfile(
    val name: String = "",
    val minVolumePercent: Int = 10,
    val maxVolumePercent: Int = 90,
    val noiseMappingLow: Int = 30,
    val volumeMappingLow: Int = 20,
    val noiseMappingMid: Int = 50,
    val volumeMappingMid: Int = 40,
    val noiseMappingHigh: Int = 70,
    val volumeMappingHigh: Int = 70,
    val noiseMappingMax: Int = 90,
    val volumeMappingMax: Int = 100,
    val smoothingFactor: Float = 0.3f,
    val noiseThreshold: Int = 5,
    val maxVolumeStep: Int = 5,
    val cooldownMs: Long = 2000L,
    val maxAdjustmentsPerSecond: Int = 3
) {
    fun encodeToString(): String {
        return listOf(
            name,
            minVolumePercent.toString(),
            maxVolumePercent.toString(),
            noiseMappingLow.toString(),
            volumeMappingLow.toString(),
            noiseMappingMid.toString(),
            volumeMappingMid.toString(),
            noiseMappingHigh.toString(),
            volumeMappingHigh.toString(),
            noiseMappingMax.toString(),
            volumeMappingMax.toString(),
            smoothingFactor.toString(),
            noiseThreshold.toString(),
            maxVolumeStep.toString(),
            cooldownMs.toString(),
            maxAdjustmentsPerSecond.toString()
        ).joinToString("|")
    }

    companion object {
        fun decodeFromString(encoded: String): VolumeProfile? {
            return try {
                val parts = encoded.split("|")
                if (parts.size < 16) return null
                VolumeProfile(
                    name = parts[0],
                    minVolumePercent = parts[1].toInt(),
                    maxVolumePercent = parts[2].toInt(),
                    noiseMappingLow = parts[3].toInt(),
                    volumeMappingLow = parts[4].toInt(),
                    noiseMappingMid = parts[5].toInt(),
                    volumeMappingMid = parts[6].toInt(),
                    noiseMappingHigh = parts[7].toInt(),
                    volumeMappingHigh = parts[8].toInt(),
                    noiseMappingMax = parts[9].toInt(),
                    volumeMappingMax = parts[10].toInt(),
                    smoothingFactor = parts[11].toFloat(),
                    noiseThreshold = parts[12].toInt(),
                    maxVolumeStep = parts[13].toInt(),
                    cooldownMs = parts[14].toLong(),
                    maxAdjustmentsPerSecond = parts[15].toInt()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * 运行模式枚举
 *
 * 定义了四种运行模式，每种模式对应不同的功耗和响应速度：
 * - POWER_SAVE: 省电模式，低采样率、长间隔，适合待机
 * - BALANCED: 平衡模式，默认推荐，平衡响应与功耗
 * - HIGH_RESPONSE: 高响应模式，高采样率、短间隔，适合通勤
 * - CUSTOM: 自定义模式，所有参数由用户手动配置
 */
enum class RunMode(val displayName: String) {
    POWER_SAVE("省电模式"),
    BALANCED("平衡模式"),
    HIGH_RESPONSE("高响应模式"),
    CUSTOM("自定义模式");

    companion object {
        /**
         * 从字符串安全解析，默认返回 BALANCED
         */
        fun fromString(value: String): RunMode =
            entries.find { it.name == value } ?: BALANCED
    }
}

/**
 * 应用设置数据类
 *
 * 包含所有可配置参数。每个参数都有默认值，确保应用首次运行时有合理的初始状态。
 * 通过 DataStore 持久化存储。
 */
data class AppSettings(
    // ==================== 基础开关 ====================
    /** 是否启用自动音量调节 */
    val isEnabled: Boolean = false,

    /** 运行模式 */
    val runMode: RunMode = RunMode.BALANCED,

    // ==================== 音量映射参数 ====================
    /**
     * 噪音-音量映射关系定义
     * 格式：Map<环境dB, 目标音量百分比>
     *
     * 例如：30dB → 20%，50dB → 40%，70dB → 70%，90dB → 100%
     * 中间值通过线性插值计算
     */
    val noiseMappingLow: Int = 30,       // 低噪音参考点 (dB)
    val volumeMappingLow: Int = 20,      // 低噪音对应音量 (%)
    val noiseMappingMid: Int = 50,       // 中噪音参考点 (dB)
    val volumeMappingMid: Int = 40,      // 中噪音对应音量 (%)
    val noiseMappingHigh: Int = 70,      // 高噪音参考点 (dB)
    val volumeMappingHigh: Int = 70,     // 高噪音对应音量 (%)
    val noiseMappingMax: Int = 90,       // 最大噪音参考点 (dB)
    val volumeMappingMax: Int = 100,     // 最大噪音对应音量 (%)

    // ==================== 音量限制 ====================
    /** 最小音量百分比 (0-100) */
    val minVolumePercent: Int = 10,

    /** 最大音量百分比 (0-100) */
    val maxVolumePercent: Int = 90,

    // ==================== 检测参数 ====================
    /** 麦克风采样率 (Hz)，影响音频捕获质量 */
    val sampleRate: Int = 44100,

    /** 检测间隔 (毫秒)，控制每多久检测一次环境噪音 */
    val detectionIntervalMs: Long = 1000L,

    /**
     * dB 平滑系数 (0.0 - 1.0)
     * 值越大越平滑（响应慢），值越小越灵敏（波动大）
     * 使用指数移动平均：smoothed = alpha * newValue + (1-alpha) * smoothed
     */
    val smoothingFactor: Float = 0.3f,

    /** 噪音灵敏度阈值 (dB)，低于此值的噪音变化会被忽略 */
    val noiseThreshold: Int = 5,

    // ==================== 音量调节速度 ====================
    /**
     * 每次音量变化最大步进 (百分比)
     * 防止音量突然大幅变化
     */
    val maxVolumeStep: Int = 5,

    /**
     * 音量变化冷却时间 (毫秒)
     * 在此时间内不会再次调节音量
     */
    val cooldownMs: Long = 2000L,

    /** 每秒最大音量调整次数 */
    val maxAdjustmentsPerSecond: Int = 3,

    // ==================== 耳机检测 ====================
    /** 是否只在耳机连接时启用（推荐开启） */
    val headsetOnly: Boolean = true,

    // ==================== 安全设置 ====================
    /** 夜间模式：22:00-07:00 限制最大音量 */
    val nightModeEnabled: Boolean = false,

    /** 夜间最大音量百分比 */
    val nightMaxVolumePercent: Int = 60,

    // ==================== 智能节能 ====================
    /** 屏幕关闭时降低检测频率 */
    val smartPowerSaving: Boolean = true,

    /** 屏幕关闭时的检测间隔倍数（例如 5 表示间隔变为5倍） */
    val screenOffIntervalMultiplier: Int = 5,

    // ==================== 动态频率 ====================
    /** 启用动态自适应采样频率 */
    val adaptiveSampling: Boolean = true,

    // ==================== 调试 ====================
    /** 是否显示实时调试信息 */
    val showDebugInfo: Boolean = false,

    // ==================== 主题 ====================
    /** 主题模式 */
    val themeMode: ThemeMode = ThemeMode.SYSTEM
) {
    companion object {
        /**
         * 根据运行模式获取预设配置
         *
         * @param mode 运行模式
         * @return 该模式对应的推荐参数（仅返回模式相关参数，其他保持默认）
         */
        fun getPresetForMode(mode: RunMode): AppSettings = when (mode) {
            RunMode.POWER_SAVE -> AppSettings(
                runMode = mode,
                sampleRate = 16000,          // 低采样率，节省CPU
                detectionIntervalMs = 3000L,  // 3秒检测一次
                smoothingFactor = 0.5f,       // 高平滑，减少波动
                maxVolumeStep = 3,            // 小步进
                cooldownMs = 5000L,           // 5秒冷却
                maxAdjustmentsPerSecond = 1,
                adaptiveSampling = false       // 关闭动态采样以进一步省电
            )
            RunMode.BALANCED -> AppSettings(
                runMode = mode,
                sampleRate = 22050,          // 中等采样率
                detectionIntervalMs = 1000L,  // 1秒检测一次
                smoothingFactor = 0.3f,       // 适中平滑
                maxVolumeStep = 5,
                cooldownMs = 2000L,
                maxAdjustmentsPerSecond = 3
            )
            RunMode.HIGH_RESPONSE -> AppSettings(
                runMode = mode,
                sampleRate = 44100,          // 高采样率
                detectionIntervalMs = 500L,   // 0.5秒检测一次
                smoothingFactor = 0.15f,      // 低平滑，快速响应
                maxVolumeStep = 8,            // 大步进
                cooldownMs = 1000L,           // 1秒冷却
                maxAdjustmentsPerSecond = 5
            )
            RunMode.CUSTOM -> AppSettings(runMode = mode) // 自定义使用全部默认值
        }
    }
}
