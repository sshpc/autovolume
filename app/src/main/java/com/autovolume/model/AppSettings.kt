package com.autovolume.model

import org.json.JSONObject

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
 * 每个配置独立保存所有相关参数。
 */
data class VolumeProfile(
    val name: String = "",
    val runMode: RunMode = RunMode.BALANCED,
    val detectionIntervalMs: Long = 1000L,
    val sampleRate: Int = 44100,
    val smoothingFactor: Float = 0.3f,
    val noiseThreshold: Int = 5,
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
    val maxVolumeStep: Int = 5,
    val cooldownMs: Long = 2000L,
    val maxAdjustmentsPerSecond: Int = 3,
    val headsetOnly: Boolean = false,
    val nightModeEnabled: Boolean = false,
    val nightMaxVolumePercent: Int = 60,
    val smartPowerSaving: Boolean = false,
    val screenOffIntervalMultiplier: Int = 5,
    val adaptiveSampling: Boolean = true
) {
    /**
     * 导出为 JSON 字符串
     */
    fun toJson(): String {
        val json = JSONObject()
        json.put("name", name)
        json.put("runMode", runMode.name)
        json.put("detectionIntervalMs", detectionIntervalMs)
        json.put("sampleRate", sampleRate)
        json.put("smoothingFactor", smoothingFactor.toDouble())
        json.put("noiseThreshold", noiseThreshold)
        json.put("minVolumePercent", minVolumePercent)
        json.put("maxVolumePercent", maxVolumePercent)
        json.put("noiseMappingLow", noiseMappingLow)
        json.put("volumeMappingLow", volumeMappingLow)
        json.put("noiseMappingMid", noiseMappingMid)
        json.put("volumeMappingMid", volumeMappingMid)
        json.put("noiseMappingHigh", noiseMappingHigh)
        json.put("volumeMappingHigh", volumeMappingHigh)
        json.put("noiseMappingMax", noiseMappingMax)
        json.put("volumeMappingMax", volumeMappingMax)
        json.put("maxVolumeStep", maxVolumeStep)
        json.put("cooldownMs", cooldownMs)
        json.put("maxAdjustmentsPerSecond", maxAdjustmentsPerSecond)
        json.put("headsetOnly", headsetOnly)
        json.put("nightModeEnabled", nightModeEnabled)
        json.put("nightMaxVolumePercent", nightMaxVolumePercent)
        json.put("smartPowerSaving", smartPowerSaving)
        json.put("screenOffIntervalMultiplier", screenOffIntervalMultiplier)
        json.put("adaptiveSampling", adaptiveSampling)
        return json.toString(2)
    }

    /**
     * 编码为管道分隔字符串（用于 DataStore 内部存储）
     */
    fun encodeToString(): String {
        return listOf(
            name,
            runMode.name,
            detectionIntervalMs.toString(),
            sampleRate.toString(),
            smoothingFactor.toString(),
            noiseThreshold.toString(),
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
            maxVolumeStep.toString(),
            cooldownMs.toString(),
            maxAdjustmentsPerSecond.toString(),
            headsetOnly.toString(),
            nightModeEnabled.toString(),
            nightMaxVolumePercent.toString(),
            smartPowerSaving.toString(),
            screenOffIntervalMultiplier.toString(),
            adaptiveSampling.toString()
        ).joinToString("|")
    }

    companion object {
        /**
         * 从 JSON 字符串解析（导入配置）
         */
        fun fromJson(jsonStr: String): VolumeProfile? {
            return try {
                val json = JSONObject(jsonStr)
                VolumeProfile(
                    name = json.optString("name", "导入配置"),
                    runMode = RunMode.fromString(json.optString("runMode", "BALANCED")),
                    detectionIntervalMs = json.optLong("detectionIntervalMs", 1000L),
                    sampleRate = json.optInt("sampleRate", 44100),
                    smoothingFactor = json.optDouble("smoothingFactor", 0.3).toFloat(),
                    noiseThreshold = json.optInt("noiseThreshold", 5),
                    minVolumePercent = json.optInt("minVolumePercent", 10),
                    maxVolumePercent = json.optInt("maxVolumePercent", 90),
                    noiseMappingLow = json.optInt("noiseMappingLow", 30),
                    volumeMappingLow = json.optInt("volumeMappingLow", 20),
                    noiseMappingMid = json.optInt("noiseMappingMid", 50),
                    volumeMappingMid = json.optInt("volumeMappingMid", 40),
                    noiseMappingHigh = json.optInt("noiseMappingHigh", 70),
                    volumeMappingHigh = json.optInt("volumeMappingHigh", 70),
                    noiseMappingMax = json.optInt("noiseMappingMax", 90),
                    volumeMappingMax = json.optInt("volumeMappingMax", 100),
                    maxVolumeStep = json.optInt("maxVolumeStep", 5),
                    cooldownMs = json.optLong("cooldownMs", 2000L),
                    maxAdjustmentsPerSecond = json.optInt("maxAdjustmentsPerSecond", 3),
                    headsetOnly = json.optBoolean("headsetOnly", false),
                    nightModeEnabled = json.optBoolean("nightModeEnabled", false),
                    nightMaxVolumePercent = json.optInt("nightMaxVolumePercent", 60),
                    smartPowerSaving = json.optBoolean("smartPowerSaving", false),
                    screenOffIntervalMultiplier = json.optInt("screenOffIntervalMultiplier", 5),
                    adaptiveSampling = json.optBoolean("adaptiveSampling", true)
                )
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 从管道分隔字符串解析（DataStore 内部存储）
         */
        fun decodeFromString(encoded: String): VolumeProfile? {
            return try {
                val parts = encoded.split("|")
                if (parts.size < 25) return null
                VolumeProfile(
                    name = parts[0],
                    runMode = RunMode.fromString(parts[1]),
                    detectionIntervalMs = parts[2].toLong(),
                    sampleRate = parts[3].toInt(),
                    smoothingFactor = parts[4].toFloat(),
                    noiseThreshold = parts[5].toInt(),
                    minVolumePercent = parts[6].toInt(),
                    maxVolumePercent = parts[7].toInt(),
                    noiseMappingLow = parts[8].toInt(),
                    volumeMappingLow = parts[9].toInt(),
                    noiseMappingMid = parts[10].toInt(),
                    volumeMappingMid = parts[11].toInt(),
                    noiseMappingHigh = parts[12].toInt(),
                    volumeMappingHigh = parts[13].toInt(),
                    noiseMappingMax = parts[14].toInt(),
                    volumeMappingMax = parts[15].toInt(),
                    maxVolumeStep = parts[16].toInt(),
                    cooldownMs = parts[17].toLong(),
                    maxAdjustmentsPerSecond = parts[18].toInt(),
                    headsetOnly = parts[19].toBoolean(),
                    nightModeEnabled = parts[20].toBoolean(),
                    nightMaxVolumePercent = parts[21].toInt(),
                    smartPowerSaving = parts[22].toBoolean(),
                    screenOffIntervalMultiplier = parts[23].toInt(),
                    adaptiveSampling = parts[24].toBoolean()
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
 * 运行模式仅控制检测间隔：
 * - POWER_SAVE: 省电模式，2000ms
 * - BALANCED: 平衡模式，1000ms
 * - HIGH_RESPONSE: 高响应模式，300ms
 * - CUSTOM: 自定义模式，用户手动设置
 */
enum class RunMode(val displayName: String, val intervalMs: Long) {
    POWER_SAVE("省电模式", 2000L),
    BALANCED("平衡模式", 1000L),
    HIGH_RESPONSE("高响应模式", 300L),
    CUSTOM("自定义模式", 1000L);

    companion object {
        fun fromString(value: String): RunMode =
            entries.find { it.name == value } ?: BALANCED

        /**
         * 根据检测间隔推断运行模式
         */
        fun fromInterval(ms: Long): RunMode = when (ms) {
            2000L -> POWER_SAVE
            1000L -> BALANCED
            300L -> HIGH_RESPONSE
            else -> CUSTOM
        }
    }
}

/**
 * 应用设置数据类
 */
data class AppSettings(
    val isEnabled: Boolean = false,
    val runMode: RunMode = RunMode.BALANCED,
    val noiseMappingLow: Int = 30,
    val volumeMappingLow: Int = 20,
    val noiseMappingMid: Int = 50,
    val volumeMappingMid: Int = 40,
    val noiseMappingHigh: Int = 70,
    val volumeMappingHigh: Int = 70,
    val noiseMappingMax: Int = 90,
    val volumeMappingMax: Int = 100,
    val minVolumePercent: Int = 10,
    val maxVolumePercent: Int = 90,
    val sampleRate: Int = 44100,
    val detectionIntervalMs: Long = 1000L,
    val smoothingFactor: Float = 0.3f,
    val noiseThreshold: Int = 5,
    val maxVolumeStep: Int = 5,
    val cooldownMs: Long = 2000L,
    val maxAdjustmentsPerSecond: Int = 3,
    val headsetOnly: Boolean = false,
    val nightModeEnabled: Boolean = false,
    val nightMaxVolumePercent: Int = 60,
    val smartPowerSaving: Boolean = false,
    val screenOffIntervalMultiplier: Int = 5,
    val adaptiveSampling: Boolean = true,
    val showDebugInfo: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
) {
    companion object {
        /**
         * 根据运行模式获取检测间隔
         */
        fun getIntervalForMode(mode: RunMode): Long = mode.intervalMs
    }
}
