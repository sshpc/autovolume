package com.autovolume.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autovolume.datastore.SettingsDataStore
import com.autovolume.headset.HeadsetDetector
import com.autovolume.model.AppSettings
import com.autovolume.model.RunMode
import com.autovolume.model.ThemeMode
import com.autovolume.model.VolumeAdjustmentEvent
import com.autovolume.model.VolumeProfile
import com.autovolume.service.AutoVolumeService
import com.autovolume.util.PermissionHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)

    // ==================== 设置 Flow ====================

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    // ==================== 服务状态 ====================

    val isServiceRunning: StateFlow<Boolean> = AutoVolumeService.isRunning
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ==================== 实时数据 ====================

    val currentDb: StateFlow<Float> = AutoVolumeService.currentDb
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val currentVolume: StateFlow<Int> = AutoVolumeService.currentVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val rawMappedVolume: StateFlow<Int> = AutoVolumeService.rawMappedVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val adjustmentHistory: StateFlow<List<VolumeAdjustmentEvent>> = AutoVolumeService.adjustmentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== 耳机状态 ====================

    val headsetConnected: StateFlow<Boolean> = AutoVolumeService.headsetConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val headsetType: StateFlow<HeadsetDetector.HeadsetType> = AutoVolumeService.headsetType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HeadsetDetector.HeadsetType.NONE)

    // ==================== 权限状态 ====================

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    val backgroundTipShown: StateFlow<Boolean> = settingsDataStore.backgroundTipShownFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ==================== 主题模式 ====================

    val themeMode: StateFlow<ThemeMode> = settingsDataStore.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    // ==================== 配置管理 ====================

    val profileNames: StateFlow<List<String>> = settingsDataStore.profileNamesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentProfileName: StateFlow<String> = settingsDataStore.currentProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "默认")

    // ==================== 权限检测（仅首页：麦克风 + 媒体音量控制） ====================

    fun getMissingPermissions(): List<PermissionItem> {
        val context = getApplication<Application>()
        val missing = mutableListOf<PermissionItem>()

        // 麦克风权限
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            missing.add(PermissionItem(
                name = "未开启麦克风权限",
                description = "请授权以启用自动音量调节",
                permission = android.Manifest.permission.RECORD_AUDIO
            ))
        }

        // 媒体音量控制能力
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (maxVol == 0) {
                missing.add(PermissionItem(
                    name = "无法控制媒体音量",
                    description = "设备不支持媒体音量调节",
                    permission = "audio_control"
                ))
            }
        } catch (e: Exception) {
            missing.add(PermissionItem(
                name = "媒体音量控制异常",
                description = "无法获取媒体音量状态",
                permission = "audio_control"
            ))
        }

        return missing
    }

    fun openAppSettings() {
        val context = getApplication<Application>()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openBatteryOptimization() {
        val context = getApplication<Application>()
        PermissionHelper.requestIgnoreBatteryOptimizations(context)
    }

    // ==================== 服务控制 ====================

    fun toggleService() {
        val context = getApplication<Application>()
        if (isServiceRunning.value) {
            AutoVolumeService.stop(context)
        } else {
            AutoVolumeService.start(context)
        }
    }

    // ==================== 设置更新 ====================

    fun updateIsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.updateIsEnabled(enabled) }
    }

    fun updateRunMode(mode: RunMode) {
        viewModelScope.launch { settingsDataStore.updateRunMode(mode) }
    }

    fun updateMinVolume(percent: Int) {
        viewModelScope.launch { settingsDataStore.updateMinVolume(percent) }
    }

    fun updateMaxVolume(percent: Int) {
        viewModelScope.launch { settingsDataStore.updateMaxVolume(percent) }
    }

    fun updateDetectionInterval(ms: Long) {
        viewModelScope.launch { settingsDataStore.updateDetectionInterval(ms) }
    }

    fun updateSmoothingFactor(factor: Float) {
        viewModelScope.launch { settingsDataStore.updateSmoothingFactor(factor) }
    }

    fun updateNoiseThreshold(threshold: Int) {
        viewModelScope.launch { settingsDataStore.updateNoiseThreshold(threshold) }
    }

    fun updateMaxVolumeStep(step: Int) {
        viewModelScope.launch { settingsDataStore.updateMaxVolumeStep(step) }
    }

    fun updateCooldownMs(ms: Long) {
        viewModelScope.launch { settingsDataStore.updateCooldownMs(ms) }
    }

    fun updateMaxAdjustmentsPerSecond(count: Int) {
        viewModelScope.launch { settingsDataStore.updateMaxAdjustmentsPerSecond(count) }
    }

    fun updateHeadsetOnly(value: Boolean) {
        viewModelScope.launch { settingsDataStore.updateHeadsetOnly(value) }
    }

    fun updateNightModeEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.updateNightModeEnabled(enabled) }
    }

    fun updateNightMaxVolume(percent: Int) {
        viewModelScope.launch { settingsDataStore.updateNightMaxVolume(percent) }
    }

    fun updateSmartPowerSaving(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.updateSmartPowerSaving(enabled) }
    }

    fun updateScreenOffMultiplier(multiplier: Int) {
        viewModelScope.launch { settingsDataStore.updateScreenOffMultiplier(multiplier) }
    }

    fun updateAdaptiveSampling(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.updateAdaptiveSampling(enabled) }
    }

    fun updateShowDebugInfo(show: Boolean) {
        viewModelScope.launch { settingsDataStore.updateShowDebugInfo(show) }
    }

    fun updateSampleRate(rate: Int) {
        viewModelScope.launch { settingsDataStore.updateSampleRate(rate) }
    }

    fun updateNoiseMapping(low: Int, lowVol: Int, mid: Int, midVol: Int,
                            high: Int, highVol: Int, max: Int, maxVol: Int) {
        viewModelScope.launch {
            settingsDataStore.updateNoiseMapping(low, lowVol, mid, midVol, high, highVol, max, maxVol)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch { settingsDataStore.resetToDefaults() }
    }

    // ==================== 权限 ====================

    fun showPermissionDialog(show: Boolean) {
        _showPermissionDialog.value = show
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsDataStore.updateThemeMode(mode) }
    }

    // ==================== 配置管理 ====================

    fun createProfile(name: String) {
        viewModelScope.launch { settingsDataStore.createProfile(name) }
    }

    fun deleteProfile(name: String) {
        viewModelScope.launch { settingsDataStore.deleteProfile(name) }
    }

    fun renameProfile(oldName: String, newName: String) {
        viewModelScope.launch { settingsDataStore.renameProfile(oldName, newName) }
    }

    fun switchProfile(name: String) {
        viewModelScope.launch { settingsDataStore.switchProfile(name) }
    }

    /** 获取配置的 JSON 导出字符串 */
    fun exportProfileAndGetJson(name: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val json = settingsDataStore.getProfileByName(name)?.toJson()
            onResult(json)
        }
    }

    /** 导入配置（从 JSON 字符串） */
    fun importProfile(jsonStr: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = settingsDataStore.importProfile(jsonStr)
            onResult(result)
        }
    }
}

data class PermissionItem(
    val name: String,
    val description: String,
    val permission: String
)
