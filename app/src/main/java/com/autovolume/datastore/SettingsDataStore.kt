package com.autovolume.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.autovolume.model.AppSettings
import com.autovolume.model.RunMode
import com.autovolume.model.ThemeMode
import com.autovolume.model.VolumeProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore Preferences 扩展属性
 *
 * 通过 Context 扩展属性创建 DataStore 实例。
 * 使用 preferencesDataStore 委托，确保全局只有一个实例。
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auto_volume_settings",
    produceMigrations = { context -> DataStoreMigration.getMigrations() }
)

/**
 * 设置数据仓库
 *
 * 负责所有设置的持久化读写。
 * 使用 DataStore 替代 SharedPreferences，支持 Kotlin Flow 响应式读取。
 *
 * 特点：
 * - 类型安全的键定义
 * - 基于 Flow 的自动通知
 * - 线程安全
 * - 支持事务性操作
 */
class SettingsDataStore(private val context: Context) {

    // ==================== 键定义 ====================
    // 每个设置项对应一个唯一键，用于 DataStore 读写

    private object Keys {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val RUN_MODE = stringPreferencesKey("run_mode")

        // 音量映射
        val NOISE_MAPPING_LOW = intPreferencesKey("noise_mapping_low")
        val VOLUME_MAPPING_LOW = intPreferencesKey("volume_mapping_low")
        val NOISE_MAPPING_MID = intPreferencesKey("noise_mapping_mid")
        val VOLUME_MAPPING_MID = intPreferencesKey("volume_mapping_mid")
        val NOISE_MAPPING_HIGH = intPreferencesKey("noise_mapping_high")
        val VOLUME_MAPPING_HIGH = intPreferencesKey("volume_mapping_high")
        val NOISE_MAPPING_MAX = intPreferencesKey("noise_mapping_max")
        val VOLUME_MAPPING_MAX = intPreferencesKey("volume_mapping_max")

        // 音量限制
        val MIN_VOLUME_PERCENT = intPreferencesKey("min_volume_percent")
        val MAX_VOLUME_PERCENT = intPreferencesKey("max_volume_percent")

        // 检测参数
        val SAMPLE_RATE = intPreferencesKey("sample_rate")
        val DETECTION_INTERVAL_MS = longPreferencesKey("detection_interval_ms")
        val SMOOTHING_FACTOR = floatPreferencesKey("smoothing_factor")
        val NOISE_THRESHOLD = intPreferencesKey("noise_threshold")

        // 调节速度
        val MAX_VOLUME_STEP = intPreferencesKey("max_volume_step")
        val COOLDOWN_MS = longPreferencesKey("cooldown_ms")
        val MAX_ADJUSTMENTS_PER_SECOND = intPreferencesKey("max_adjustments_per_second")

        // 耳机
        val HEADSET_ONLY = booleanPreferencesKey("headset_only")

        // 安全
        val NIGHT_MODE_ENABLED = booleanPreferencesKey("night_mode_enabled")
        val NIGHT_MAX_VOLUME = intPreferencesKey("night_max_volume")

        // 节能
        val SMART_POWER_SAVING = booleanPreferencesKey("smart_power_saving")
        val SCREEN_OFF_MULTIPLIER = intPreferencesKey("screen_off_multiplier")
        val ADAPTIVE_SAMPLING = booleanPreferencesKey("adaptive_sampling")

        // 调试
        val SHOW_DEBUG_INFO = booleanPreferencesKey("show_debug_info")

        // 运行状态（非设置，用于跨进程状态恢复）
        val SERVICE_WAS_RUNNING = booleanPreferencesKey("service_was_running")

        // 后台运行提示已展示
        val BACKGROUND_TIP_SHOWN = booleanPreferencesKey("background_tip_shown")

        // 主题模式
        val THEME_MODE = stringPreferencesKey("theme_mode")

        // 配置管理
        val PROFILE_NAMES = stringPreferencesKey("profile_names")
        val CURRENT_PROFILE = stringPreferencesKey("current_profile")
    }

    /**
     * 响应式观察设置变化
     *
     * 返回一个 Flow，每当设置发生变化时自动发射新的 AppSettings 对象。
     * ViewModel 可以 collect 这个 Flow 来实时更新 UI。
     */
    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            isEnabled = prefs[Keys.IS_ENABLED] ?: false,
            runMode = RunMode.fromString(prefs[Keys.RUN_MODE] ?: RunMode.BALANCED.name),

            noiseMappingLow = prefs[Keys.NOISE_MAPPING_LOW] ?: 30,
            volumeMappingLow = prefs[Keys.VOLUME_MAPPING_LOW] ?: 20,
            noiseMappingMid = prefs[Keys.NOISE_MAPPING_MID] ?: 50,
            volumeMappingMid = prefs[Keys.VOLUME_MAPPING_MID] ?: 40,
            noiseMappingHigh = prefs[Keys.NOISE_MAPPING_HIGH] ?: 70,
            volumeMappingHigh = prefs[Keys.VOLUME_MAPPING_HIGH] ?: 70,
            noiseMappingMax = prefs[Keys.NOISE_MAPPING_MAX] ?: 90,
            volumeMappingMax = prefs[Keys.VOLUME_MAPPING_MAX] ?: 100,

            minVolumePercent = prefs[Keys.MIN_VOLUME_PERCENT] ?: 10,
            maxVolumePercent = prefs[Keys.MAX_VOLUME_PERCENT] ?: 90,

            sampleRate = prefs[Keys.SAMPLE_RATE] ?: 44100,
            detectionIntervalMs = prefs[Keys.DETECTION_INTERVAL_MS] ?: 1000L,
            smoothingFactor = prefs[Keys.SMOOTHING_FACTOR] ?: 0.3f,
            noiseThreshold = prefs[Keys.NOISE_THRESHOLD] ?: 5,

            maxVolumeStep = prefs[Keys.MAX_VOLUME_STEP] ?: 5,
            cooldownMs = prefs[Keys.COOLDOWN_MS] ?: 2000L,
            maxAdjustmentsPerSecond = prefs[Keys.MAX_ADJUSTMENTS_PER_SECOND] ?: 3,

            headsetOnly = prefs[Keys.HEADSET_ONLY] ?: true,

            nightModeEnabled = prefs[Keys.NIGHT_MODE_ENABLED] ?: false,
            nightMaxVolumePercent = prefs[Keys.NIGHT_MAX_VOLUME] ?: 60,

            smartPowerSaving = prefs[Keys.SMART_POWER_SAVING] ?: true,
            screenOffIntervalMultiplier = prefs[Keys.SCREEN_OFF_MULTIPLIER] ?: 5,
            adaptiveSampling = prefs[Keys.ADAPTIVE_SAMPLING] ?: true,

            showDebugInfo = prefs[Keys.SHOW_DEBUG_INFO] ?: false,

            themeMode = ThemeMode.fromString(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
        )
    }

    /** 获取服务是否曾经运行过（用于开机自启判断） */
    val serviceWasRunningFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SERVICE_WAS_RUNNING] ?: false
    }

    /**
     * 更新单个设置项
     *
     * 使用 edit 事务确保原子性写入。
     * 写入后，settingsFlow 会自动发射新值。
     */
    suspend fun updateIsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_ENABLED] = enabled }
    }

    suspend fun updateRunMode(mode: RunMode) {
        context.dataStore.edit { prefs ->
            // 如果切换到预设模式，同时应用该模式的预设参数
            if (mode != RunMode.CUSTOM) {
                val preset = AppSettings.getPresetForMode(mode)
                prefs[Keys.RUN_MODE] = mode.name
                prefs[Keys.SAMPLE_RATE] = preset.sampleRate
                prefs[Keys.DETECTION_INTERVAL_MS] = preset.detectionIntervalMs
                prefs[Keys.SMOOTHING_FACTOR] = preset.smoothingFactor
                prefs[Keys.MAX_VOLUME_STEP] = preset.maxVolumeStep
                prefs[Keys.COOLDOWN_MS] = preset.cooldownMs
                prefs[Keys.MAX_ADJUSTMENTS_PER_SECOND] = preset.maxAdjustmentsPerSecond
                prefs[Keys.ADAPTIVE_SAMPLING] = preset.adaptiveSampling
            } else {
                prefs[Keys.RUN_MODE] = mode.name
            }
        }
    }

    suspend fun updateMinVolume(percent: Int) {
        context.dataStore.edit { it[Keys.MIN_VOLUME_PERCENT] = percent.coerceIn(0, 100) }
    }

    suspend fun updateMaxVolume(percent: Int) {
        context.dataStore.edit { it[Keys.MAX_VOLUME_PERCENT] = percent.coerceIn(0, 100) }
    }

    suspend fun updateDetectionInterval(ms: Long) {
        context.dataStore.edit { it[Keys.DETECTION_INTERVAL_MS] = ms.coerceIn(100, 10000) }
    }

    suspend fun updateSmoothingFactor(factor: Float) {
        context.dataStore.edit { it[Keys.SMOOTHING_FACTOR] = factor.coerceIn(0.01f, 0.99f) }
    }

    suspend fun updateNoiseThreshold(threshold: Int) {
        context.dataStore.edit { it[Keys.NOISE_THRESHOLD] = threshold.coerceIn(1, 30) }
    }

    suspend fun updateMaxVolumeStep(step: Int) {
        context.dataStore.edit { it[Keys.MAX_VOLUME_STEP] = step.coerceIn(1, 20) }
    }

    suspend fun updateCooldownMs(ms: Long) {
        context.dataStore.edit { it[Keys.COOLDOWN_MS] = ms.coerceIn(500, 30000) }
    }

    suspend fun updateMaxAdjustmentsPerSecond(count: Int) {
        context.dataStore.edit { it[Keys.MAX_ADJUSTMENTS_PER_SECOND] = count.coerceIn(1, 10) }
    }

    suspend fun updateHeadsetOnly(value: Boolean) {
        context.dataStore.edit { it[Keys.HEADSET_ONLY] = value }
    }

    suspend fun updateNightModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NIGHT_MODE_ENABLED] = enabled }
    }

    suspend fun updateNightMaxVolume(percent: Int) {
        context.dataStore.edit { it[Keys.NIGHT_MAX_VOLUME] = percent.coerceIn(10, 100) }
    }

    suspend fun updateSmartPowerSaving(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SMART_POWER_SAVING] = enabled }
    }

    suspend fun updateScreenOffMultiplier(multiplier: Int) {
        context.dataStore.edit { it[Keys.SCREEN_OFF_MULTIPLIER] = multiplier.coerceIn(2, 20) }
    }

    suspend fun updateAdaptiveSampling(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ADAPTIVE_SAMPLING] = enabled }
    }

    suspend fun updateShowDebugInfo(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_DEBUG_INFO] = show }
    }

    suspend fun updateSampleRate(rate: Int) {
        context.dataStore.edit { it[Keys.SAMPLE_RATE] = rate }
    }

    suspend fun updateNoiseMapping(low: Int, lowVol: Int, mid: Int, midVol: Int,
                                     high: Int, highVol: Int, max: Int, maxVol: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOISE_MAPPING_LOW] = low
            prefs[Keys.VOLUME_MAPPING_LOW] = lowVol
            prefs[Keys.NOISE_MAPPING_MID] = mid
            prefs[Keys.VOLUME_MAPPING_MID] = midVol
            prefs[Keys.NOISE_MAPPING_HIGH] = high
            prefs[Keys.VOLUME_MAPPING_HIGH] = highVol
            prefs[Keys.NOISE_MAPPING_MAX] = max
            prefs[Keys.VOLUME_MAPPING_MAX] = maxVol
        }
    }

    /** 记录服务运行状态（用于开机自启） */
    suspend fun updateServiceWasRunning(wasRunning: Boolean) {
        context.dataStore.edit { it[Keys.SERVICE_WAS_RUNNING] = wasRunning }
    }

    /** 重置所有设置为默认值 */
    suspend fun resetToDefaults() {
        context.dataStore.edit { it.clear() }
    }

    // ==================== 后台运行提示 ====================

    val backgroundTipShownFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.BACKGROUND_TIP_SHOWN] ?: false
    }

    suspend fun updateBackgroundTipShown(shown: Boolean) {
        context.dataStore.edit { it[Keys.BACKGROUND_TIP_SHOWN] = shown }
    }

    // ==================== 主题模式 ====================

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.fromString(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    // ==================== 配置管理 ====================

    /** 获取所有配置名称列表 */
    val profileNamesFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val names = prefs[Keys.PROFILE_NAMES] ?: ""
        if (names.isBlank()) emptyList() else names.split(",").filter { it.isNotBlank() }
    }

    /** 获取当前配置名称 */
    val currentProfileFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.CURRENT_PROFILE] ?: "默认"
    }

    /** 根据名称获取配置 */
    suspend fun getProfileByName(name: String): VolumeProfile? {
        if (name == "默认") {
            val prefs = context.dataStore.data.first()
            return VolumeProfile(
                name = "默认",
                minVolumePercent = prefs[Keys.MIN_VOLUME_PERCENT] ?: 10,
                maxVolumePercent = prefs[Keys.MAX_VOLUME_PERCENT] ?: 90,
                noiseMappingLow = prefs[Keys.NOISE_MAPPING_LOW] ?: 30,
                volumeMappingLow = prefs[Keys.VOLUME_MAPPING_LOW] ?: 20,
                noiseMappingMid = prefs[Keys.NOISE_MAPPING_MID] ?: 50,
                volumeMappingMid = prefs[Keys.VOLUME_MAPPING_MID] ?: 40,
                noiseMappingHigh = prefs[Keys.NOISE_MAPPING_HIGH] ?: 70,
                volumeMappingHigh = prefs[Keys.VOLUME_MAPPING_HIGH] ?: 70,
                noiseMappingMax = prefs[Keys.NOISE_MAPPING_MAX] ?: 90,
                volumeMappingMax = prefs[Keys.VOLUME_MAPPING_MAX] ?: 100,
                smoothingFactor = prefs[Keys.SMOOTHING_FACTOR] ?: 0.3f,
                noiseThreshold = prefs[Keys.NOISE_THRESHOLD] ?: 5,
                maxVolumeStep = prefs[Keys.MAX_VOLUME_STEP] ?: 5,
                cooldownMs = prefs[Keys.COOLDOWN_MS] ?: 2000L,
                maxAdjustmentsPerSecond = prefs[Keys.MAX_ADJUSTMENTS_PER_SECOND] ?: 3
            )
        }
        val key = stringPreferencesKey("profile_${name}")
        val prefs = context.dataStore.data.first()
        val encoded = prefs[key] ?: return null
        return VolumeProfile.decodeFromString(encoded)
    }

    /** 创建新配置（保存当前设置为新配置） */
    suspend fun createProfile(name: String) {
        context.dataStore.edit { prefs ->
            val profile = VolumeProfile(
                name = name,
                minVolumePercent = prefs[Keys.MIN_VOLUME_PERCENT] ?: 10,
                maxVolumePercent = prefs[Keys.MAX_VOLUME_PERCENT] ?: 90,
                noiseMappingLow = prefs[Keys.NOISE_MAPPING_LOW] ?: 30,
                volumeMappingLow = prefs[Keys.VOLUME_MAPPING_LOW] ?: 20,
                noiseMappingMid = prefs[Keys.NOISE_MAPPING_MID] ?: 50,
                volumeMappingMid = prefs[Keys.VOLUME_MAPPING_MID] ?: 40,
                noiseMappingHigh = prefs[Keys.NOISE_MAPPING_HIGH] ?: 70,
                volumeMappingHigh = prefs[Keys.VOLUME_MAPPING_HIGH] ?: 70,
                noiseMappingMax = prefs[Keys.NOISE_MAPPING_MAX] ?: 90,
                volumeMappingMax = prefs[Keys.VOLUME_MAPPING_MAX] ?: 100,
                smoothingFactor = prefs[Keys.SMOOTHING_FACTOR] ?: 0.3f,
                noiseThreshold = prefs[Keys.NOISE_THRESHOLD] ?: 5,
                maxVolumeStep = prefs[Keys.MAX_VOLUME_STEP] ?: 5,
                cooldownMs = prefs[Keys.COOLDOWN_MS] ?: 2000L,
                maxAdjustmentsPerSecond = prefs[Keys.MAX_ADJUSTMENTS_PER_SECOND] ?: 3
            )
            val key = stringPreferencesKey("profile_${name}")
            prefs[key] = profile.encodeToString()

            val existing = prefs[Keys.PROFILE_NAMES] ?: ""
            val names = if (existing.isBlank()) name else "${existing},${name}"
            prefs[Keys.PROFILE_NAMES] = names
        }
    }

    /** 删除配置 */
    suspend fun deleteProfile(name: String) {
        if (name == "默认") return
        context.dataStore.edit { prefs ->
            val key = stringPreferencesKey("profile_${name}")
            prefs.remove(key)

            val existing = prefs[Keys.PROFILE_NAMES] ?: ""
            val names = existing.split(",").filter { it.isNotBlank() && it != name }.joinToString(",")
            prefs[Keys.PROFILE_NAMES] = names

            if (prefs[Keys.CURRENT_PROFILE] == name) {
                prefs[Keys.CURRENT_PROFILE] = "默认"
            }
        }
    }

    /** 重命名配置 */
    suspend fun renameProfile(oldName: String, newName: String) {
        if (oldName == "默认") return
        context.dataStore.edit { prefs ->
            val oldKey = stringPreferencesKey("profile_${oldName}")
            val encoded = prefs[oldKey] ?: return@edit
            prefs.remove(oldKey)

            val newKey = stringPreferencesKey("profile_${newName}")
            val profile = VolumeProfile.decodeFromString(encoded)
            if (profile != null) {
                prefs[newKey] = profile.copy(name = newName).encodeToString()
            }

            val existing = prefs[Keys.PROFILE_NAMES] ?: ""
            val names = existing.split(",").map { if (it == oldName) newName else it }.joinToString(",")
            prefs[Keys.PROFILE_NAMES] = names

            if (prefs[Keys.CURRENT_PROFILE] == oldName) {
                prefs[Keys.CURRENT_PROFILE] = newName
            }
        }
    }

    /** 切换到指定配置（将配置参数应用到主设置） */
    suspend fun switchProfile(name: String) {
        val profile = getProfileByName(name) ?: return
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENT_PROFILE] = name
            prefs[Keys.MIN_VOLUME_PERCENT] = profile.minVolumePercent
            prefs[Keys.MAX_VOLUME_PERCENT] = profile.maxVolumePercent
            prefs[Keys.NOISE_MAPPING_LOW] = profile.noiseMappingLow
            prefs[Keys.VOLUME_MAPPING_LOW] = profile.volumeMappingLow
            prefs[Keys.NOISE_MAPPING_MID] = profile.noiseMappingMid
            prefs[Keys.VOLUME_MAPPING_MID] = profile.volumeMappingMid
            prefs[Keys.NOISE_MAPPING_HIGH] = profile.noiseMappingHigh
            prefs[Keys.VOLUME_MAPPING_HIGH] = profile.volumeMappingHigh
            prefs[Keys.NOISE_MAPPING_MAX] = profile.noiseMappingMax
            prefs[Keys.VOLUME_MAPPING_MAX] = profile.volumeMappingMax
            prefs[Keys.SMOOTHING_FACTOR] = profile.smoothingFactor
            prefs[Keys.NOISE_THRESHOLD] = profile.noiseThreshold
            prefs[Keys.MAX_VOLUME_STEP] = profile.maxVolumeStep
            prefs[Keys.COOLDOWN_MS] = profile.cooldownMs
            prefs[Keys.MAX_ADJUSTMENTS_PER_SECOND] = profile.maxAdjustmentsPerSecond
        }
    }
}
