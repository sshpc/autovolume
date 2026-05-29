package com.autovolume.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autovolume.datastore.SettingsDataStore
import com.autovolume.headset.HeadsetDetector
import com.autovolume.model.AppSettings
import com.autovolume.model.RunMode
import com.autovolume.model.VolumeAdjustmentEvent
import com.autovolume.service.AutoVolumeService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 主 ViewModel
 *
 * 管理整个应用的 UI 状态。
 *
 * 架构：
 * - settingsFlow → 设置状态（来自 DataStore）
 * - isServiceRunning → 服务运行状态（来自 Service companion object）
 * - currentDb / currentVolume / rawMappedVolume → 实时数据（来自 Service companion object）
 * - headsetConnected / headsetType → 耳机状态（来自 Service companion object）
 * - 各种 update 方法 → 修改设置
 *
 * 数据流向：Service companion object StateFlow → ViewModel → UI
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)

    // ==================== 设置 Flow ====================

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    // ==================== 服务状态（来自 Service companion object）====================

    /** 服务是否正在运行 */
    val isServiceRunning: StateFlow<Boolean> = AutoVolumeService.isRunning
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ==================== 实时数据（来自 Service companion object）====================

    /** 当前环境 dB */
    val currentDb: StateFlow<Float> = AutoVolumeService.currentDb
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    /** 当前媒体音量百分比 */
    val currentVolume: StateFlow<Int> = AutoVolumeService.currentVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** 映射后的目标音量（未限制） */
    val rawMappedVolume: StateFlow<Int> = AutoVolumeService.rawMappedVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** 音量调节历史 */
    val adjustmentHistory: StateFlow<List<VolumeAdjustmentEvent>> = AutoVolumeService.adjustmentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== 耳机状态（来自 Service companion object）====================

    /** 耳机是否连接 */
    val headsetConnected: StateFlow<Boolean> = AutoVolumeService.headsetConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 耳机类型 */
    val headsetType: StateFlow<HeadsetDetector.HeadsetType> = AutoVolumeService.headsetType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HeadsetDetector.HeadsetType.NONE)

    // ==================== 权限状态 ====================

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private val _manufacturerWarning = MutableStateFlow<String?>(null)
    val manufacturerWarning: StateFlow<String?> = _manufacturerWarning.asStateFlow()

    // ==================== 服务控制 ====================

    fun toggleService() {
        val context = getApplication<Application>()
        if (isServiceRunning.value) {
            AutoVolumeService.stop(context)
        } else {
            AutoVolumeService.start(context)
        }
    }

    // ==================== 设置更新方法 ====================

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

    fun checkManufacturerWarning() {
        _manufacturerWarning.value =
            com.autovolume.util.PermissionHelper.getManufacturerWarning()
    }
}
