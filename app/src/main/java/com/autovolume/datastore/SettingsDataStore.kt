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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auto_volume_settings",
    produceMigrations = { context -> DataStoreMigration.getMigrations() }
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val RUN_MODE = stringPreferencesKey("run_mode")
        val NOISE_MAPPING_LOW = intPreferencesKey("noise_mapping_low")
        val VOLUME_MAPPING_LOW = intPreferencesKey("volume_mapping_low")
        val NOISE_MAPPING_MID = intPreferencesKey("noise_mapping_mid")
        val VOLUME_MAPPING_MID = intPreferencesKey("volume_mapping_mid")
        val NOISE_MAPPING_HIGH = intPreferencesKey("noise_mapping_high")
        val VOLUME_MAPPING_HIGH = intPreferencesKey("volume_mapping_high")
        val NOISE_MAPPING_MAX = intPreferencesKey("noise_mapping_max")
        val VOLUME_MAPPING_MAX = intPreferencesKey("volume_mapping_max")
        val MIN_VOLUME_PERCENT = intPreferencesKey("min_volume_percent")
        val MAX_VOLUME_PERCENT = intPreferencesKey("max_volume_percent")
        val SAMPLE_RATE = intPreferencesKey("sample_rate")
        val DETECTION_INTERVAL_MS = longPreferencesKey("detection_interval_ms")
        val SMOOTHING_FACTOR = floatPreferencesKey("smoothing_factor")
        val NOISE_THRESHOLD = intPreferencesKey("noise_threshold")
        val MAX_VOLUME_STEP = intPreferencesKey("max_volume_step")
        val COOLDOWN_MS = longPreferencesKey("cooldown_ms")
        val MAX_ADJUSTMENTS_PER_SECOND = intPreferencesKey("max_adjustments_per_second")
        val HEADSET_ONLY = booleanPreferencesKey("headset_only")
        val NIGHT_MODE_ENABLED = booleanPreferencesKey("night_mode_enabled")
        val NIGHT_MAX_VOLUME = intPreferencesKey("night_max_volume")
        val SMART_POWER_SAVING = booleanPreferencesKey("smart_power_saving")
        val SCREEN_OFF_MULTIPLIER = intPreferencesKey("screen_off_multiplier")
        val ADAPTIVE_SAMPLING = booleanPreferencesKey("adaptive_sampling")
        val SHOW_DEBUG_INFO = booleanPreferencesKey("show_debug_info")
        val SERVICE_WAS_RUNNING = booleanPreferencesKey("service_was_running")
        val BACKGROUND_TIP_SHOWN = booleanPreferencesKey("background_tip_shown")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PROFILE_NAMES = stringPreferencesKey("profile_names")
        val CURRENT_PROFILE = stringPreferencesKey("current_profile")
    }

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
            headsetOnly = prefs[Keys.HEADSET_ONLY] ?: false,
            nightModeEnabled = prefs[Keys.NIGHT_MODE_ENABLED] ?: false,
            nightMaxVolumePercent = prefs[Keys.NIGHT_MAX_VOLUME] ?: 60,
            smartPowerSaving = prefs[Keys.SMART_POWER_SAVING] ?: false,
            screenOffIntervalMultiplier = prefs[Keys.SCREEN_OFF_MULTIPLIER] ?: 5,
            adaptiveSampling = prefs[Keys.ADAPTIVE_SAMPLING] ?: true,
            showDebugInfo = prefs[Keys.SHOW_DEBUG_INFO] ?: false,
            themeMode = ThemeMode.fromString(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
        )
    }

    val serviceWasRunningFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SERVICE_WAS_RUNNING] ?: false
    }

    // ==================== 设置更新 ====================

    suspend fun updateIsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_ENABLED] = enabled }
    }

    /**
     * 切换运行模式
     * 运行模式仅控制检测间隔
     */
    suspend fun updateRunMode(mode: RunMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.RUN_MODE] = mode.name
            // 模式仅控制检测间隔
            prefs[Keys.DETECTION_INTERVAL_MS] = mode.intervalMs
            // 省电模式自动开启智能节能
            if (mode == RunMode.POWER_SAVE) {
                prefs[Keys.SMART_POWER_SAVING] = true
            } else if (mode != RunMode.CUSTOM) {
                prefs[Keys.SMART_POWER_SAVING] = false
            }
        }
    }

    suspend fun updateMinVolume(percent: Int) {
        context.dataStore.edit { it[Keys.MIN_VOLUME_PERCENT] = percent.coerceIn(0, 100) }
    }

    suspend fun updateMaxVolume(percent: Int) {
        context.dataStore.edit { it[Keys.MAX_VOLUME_PERCENT] = percent.coerceIn(0, 100) }
    }

    /**
     * 更新检测间隔
     * 手动修改时自动切换为自定义模式
     */
    suspend fun updateDetectionInterval(ms: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DETECTION_INTERVAL_MS] = ms.coerceIn(100, 10000)
            // 手动修改检测间隔 → 自动切换为自定义模式
            prefs[Keys.RUN_MODE] = RunMode.CUSTOM.name
        }
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
        context.dataStore.edit { prefs ->
            prefs[Keys.SMART_POWER_SAVING] = enabled
            // 手动修改智能节能 → 切换为自定义模式
            prefs[Keys.RUN_MODE] = RunMode.CUSTOM.name
        }
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

    suspend fun updateServiceWasRunning(wasRunning: Boolean) {
        context.dataStore.edit { it[Keys.SERVICE_WAS_RUNNING] = wasRunning }
    }

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

    val profileNamesFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val names = prefs[Keys.PROFILE_NAMES] ?: ""
        if (names.isBlank()) emptyList() else names.split(",").filter { it.isNotBlank() }
    }

    val currentProfileFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.CURRENT_PROFILE] ?: "默认"
    }

    /**
     * 从 Preferences 对象读取当前设置构建 VolumeProfile
     * 不会触发 DataStore 读锁，可在 edit 块内安全调用
     */
    private fun readCurrentProfileFromPrefs(prefs: Preferences, profileName: String): VolumeProfile {
        return VolumeProfile(
            name = profileName,
            runMode = RunMode.fromString(prefs[Keys.RUN_MODE] ?: RunMode.BALANCED.name),
            detectionIntervalMs = prefs[Keys.DETECTION_INTERVAL_MS] ?: 1000L,
            sampleRate = prefs[Keys.SAMPLE_RATE] ?: 44100,
            smoothingFactor = prefs[Keys.SMOOTHING_FACTOR] ?: 0.3f,
            noiseThreshold = prefs[Keys.NOISE_THRESHOLD] ?: 5,
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
            maxVolumeStep = prefs[Keys.MAX_VOLUME_STEP] ?: 5,
            cooldownMs = prefs[Keys.COOLDOWN_MS] ?: 2000L,
            maxAdjustmentsPerSecond = prefs[Keys.MAX_ADJUSTMENTS_PER_SECOND] ?: 3,
            headsetOnly = prefs[Keys.HEADSET_ONLY] ?: false,
            nightModeEnabled = prefs[Keys.NIGHT_MODE_ENABLED] ?: false,
            nightMaxVolumePercent = prefs[Keys.NIGHT_MAX_VOLUME] ?: 60,
            smartPowerSaving = prefs[Keys.SMART_POWER_SAVING] ?: false,
            screenOffIntervalMultiplier = prefs[Keys.SCREEN_OFF_MULTIPLIER] ?: 5,
            adaptiveSampling = prefs[Keys.ADAPTIVE_SAMPLING] ?: true
        )
    }

    /**
     * 将 VolumeProfile 的参数应用到 Preferences
     * 在 edit 块内安全调用
     */
    private fun applyProfileToPrefs(prefs: MutablePreferences, profile: VolumeProfile) {
        prefs[Keys.CURRENT_PROFILE] = profile.name
        prefs[Keys.RUN_MODE] = profile.runMode.name
        prefs[Keys.DETECTION_INTERVAL_MS] = profile.detectionIntervalMs
        prefs[Keys.SAMPLE_RATE] = profile.sampleRate
        prefs[Keys.SMOOTHING_FACTOR] = profile.smoothingFactor
        prefs[Keys.NOISE_THRESHOLD] = profile.noiseThreshold
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
        prefs[Keys.MAX_VOLUME_STEP] = profile.maxVolumeStep
        prefs[Keys.COOLDOWN_MS] = profile.cooldownMs
        prefs[Keys.MAX_ADJUSTMENTS_PER_SECOND] = profile.maxAdjustmentsPerSecond
        prefs[Keys.HEADSET_ONLY] = profile.headsetOnly
        prefs[Keys.NIGHT_MODE_ENABLED] = profile.nightModeEnabled
        prefs[Keys.NIGHT_MAX_VOLUME] = profile.nightMaxVolumePercent
        prefs[Keys.SMART_POWER_SAVING] = profile.smartPowerSaving
        prefs[Keys.SCREEN_OFF_MULTIPLIER] = profile.screenOffIntervalMultiplier
        prefs[Keys.ADAPTIVE_SAMPLING] = profile.adaptiveSampling
    }

    /** 根据名称获取配置（不触发死锁） */
    suspend fun getProfileByName(name: String): VolumeProfile? {
        if (name == "默认") {
            val prefs = context.dataStore.data.first()
            return readCurrentProfileFromPrefs(prefs, "默认")
        }
        val key = stringPreferencesKey("profile_${name}")
        val prefs = context.dataStore.data.first()
        val encoded = prefs[key] ?: return null
        return VolumeProfile.decodeFromString(encoded)
    }

    /** 创建新配置 */
    suspend fun createProfile(name: String) {
        context.dataStore.edit { prefs ->
            val profile = readCurrentProfileFromPrefs(prefs, name)
            val key = stringPreferencesKey("profile_${name}")
            prefs[key] = profile.encodeToString()
            val existing = prefs[Keys.PROFILE_NAMES] ?: ""
            prefs[Keys.PROFILE_NAMES] = if (existing.isBlank()) name else "${existing},${name}"
        }
    }

    /** 删除配置 */
    suspend fun deleteProfile(name: String) {
        if (name == "默认") return
        context.dataStore.edit { prefs ->
            val key = stringPreferencesKey("profile_${name}")
            prefs.remove(key)
            val existing = prefs[Keys.PROFILE_NAMES] ?: ""
            prefs[Keys.PROFILE_NAMES] = existing.split(",").filter { it.isNotBlank() && it != name }.joinToString(",")
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
            prefs[Keys.PROFILE_NAMES] = existing.split(",").map { if (it == oldName) newName else it }.joinToString(",")
            if (prefs[Keys.CURRENT_PROFILE] == oldName) {
                prefs[Keys.CURRENT_PROFILE] = newName
            }
        }
    }

    /**
     * 切换到指定配置（修复死锁问题）
     * 直接在 edit 块内读取配置数据，避免嵌套锁
     */
    suspend fun switchProfile(name: String) {
        context.dataStore.edit { prefs ->
            if (name == "默认") {
                // 默认配置：读取当前所有值作为默认配置
                val profile = readCurrentProfileFromPrefs(prefs, "默认")
                applyProfileToPrefs(prefs, profile)
            } else {
                val key = stringPreferencesKey("profile_${name}")
                val encoded = prefs[key] ?: return@edit
                val profile = VolumeProfile.decodeFromString(encoded) ?: return@edit
                applyProfileToPrefs(prefs, profile)
            }
        }
    }

    /** 导入配置（从 JSON） */
    suspend fun importProfile(jsonStr: String): Boolean {
        val profile = VolumeProfile.fromJson(jsonStr) ?: return false
        createProfile(profile.name)
        // 用解析的 profile 覆盖刚创建的
        val key = stringPreferencesKey("profile_${profile.name}")
        context.dataStore.edit { prefs ->
            prefs[key] = profile.encodeToString()
        }
        return true
    }
}
