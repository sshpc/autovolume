package com.autovolume

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.autovolume.ui.MainViewModel
import com.autovolume.ui.screens.*
import com.autovolume.ui.theme.AutoVolumeTheme
import com.autovolume.util.PermissionHelper

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRequiredPermissions()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            AutoVolumeTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AutoVolumeAppContent(
                        viewModel = viewModel,
                        onRequestPermissions = { requestRequiredPermissions() },
                        onRequestBatteryOptimization = {
                            PermissionHelper.requestIgnoreBatteryOptimizations(this)
                        }
                    )
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val missing = PermissionHelper.getMissingPermissions(this)
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}

@Composable
fun AutoVolumeAppContent(
    viewModel: MainViewModel,
    onRequestPermissions: () -> Unit,
    onRequestBatteryOptimization: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val settings by viewModel.settings.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val currentDb by viewModel.currentDb.collectAsState()
    val currentVolume by viewModel.currentVolume.collectAsState()
    val rawMappedVolume by viewModel.rawMappedVolume.collectAsState()
    val adjustmentHistory by viewModel.adjustmentHistory.collectAsState()
    val headsetConnected by viewModel.headsetConnected.collectAsState()
    val headsetType by viewModel.headsetType.collectAsState()
    val profileNames by viewModel.profileNames.collectAsState()
    val currentProfileName by viewModel.currentProfileName.collectAsState()

    // 麦克风权限状态
    val hasAudioPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        }.collect { result ->
            hasAudioPermission.value = result == PackageManager.PERMISSION_GRANTED
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                settings = settings,
                isServiceRunning = isServiceRunning,
                currentDb = currentDb,
                currentVolume = currentVolume,
                rawMappedVolume = rawMappedVolume,
                headsetConnected = headsetConnected,
                headsetType = headsetType,
                profileNames = profileNames,
                currentProfileName = currentProfileName,
                onToggleService = {
                    if (!hasAudioPermission.value) onRequestPermissions()
                    else viewModel.toggleService()
                },
                onToggleEnabled = { viewModel.updateIsEnabled(it) },
                onRunModeChange = { viewModel.updateRunMode(it) },
                onMinVolumeChange = { viewModel.updateMinVolume(it) },
                onMaxVolumeChange = { viewModel.updateMaxVolume(it) },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToProfile = { navController.navigate("profile") },
                onProfileSwitch = { viewModel.switchProfile(it) },
                onOpenAppSettings = { viewModel.openAppSettings() },
                missingPermissions = viewModel.getMissingPermissions()
            )
        }

        composable("settings") {
            SettingsScreen(
                settings = settings,
                themeMode = viewModel.themeMode.collectAsState().value,
                onBack = { navController.popBackStack() },
                onDetectionIntervalChange = { viewModel.updateDetectionInterval(it) },
                onSmoothingFactorChange = { viewModel.updateSmoothingFactor(it) },
                onNoiseThresholdChange = { viewModel.updateNoiseThreshold(it) },
                onMaxVolumeStepChange = { viewModel.updateMaxVolumeStep(it) },
                onCooldownMsChange = { viewModel.updateCooldownMs(it) },
                onMaxAdjustmentsChange = { viewModel.updateMaxAdjustmentsPerSecond(it) },
                onHeadsetOnlyChange = { viewModel.updateHeadsetOnly(it) },
                onNightModeChange = { viewModel.updateNightModeEnabled(it) },
                onNightMaxVolumeChange = { viewModel.updateNightMaxVolume(it) },
                onSmartPowerSavingChange = { viewModel.updateSmartPowerSaving(it) },
                onScreenOffMultiplierChange = { viewModel.updateScreenOffMultiplier(it) },
                onAdaptiveSamplingChange = { viewModel.updateAdaptiveSampling(it) },
                onNoiseMappingChange = { n1, v1, n2, v2, n3, v3, n4, v4 ->
                    viewModel.updateNoiseMapping(n1, v1, n2, v2, n3, v3, n4, v4)
                },
                onResetDefaults = { viewModel.resetToDefaults() },
                onThemeModeChange = { viewModel.updateThemeMode(it) },
                onNavigateToAdvanced = { navController.navigate("advanced") },
                onRequestBatteryOptimization = onRequestBatteryOptimization
            )
        }

        composable("profile") {
            ProfileScreen(
                profileNames = profileNames,
                currentProfileName = currentProfileName,
                onBack = { navController.popBackStack() },
                onCreateProfile = { viewModel.createProfile(it) },
                onDeleteProfile = { viewModel.deleteProfile(it) },
                onRenameProfile = { old, new -> viewModel.renameProfile(old, new) },
                onSwitchProfile = { viewModel.switchProfile(it) },
                onExportProfile = { name, onResult ->
                    viewModel.exportProfileAndGetJson(name, onResult)
                },
                onImportProfile = { json, onResult -> viewModel.importProfile(json, onResult) }
            )
        }

        composable("advanced") {
            AdvancedScreen(
                settings = settings,
                currentDb = currentDb,
                currentVolume = currentVolume,
                rawMappedVolume = rawMappedVolume,
                adjustmentHistory = adjustmentHistory,
                onBack = { navController.popBackStack() },
                onSampleRateChange = { viewModel.updateSampleRate(it) },
                onShowDebugChange = { viewModel.updateShowDebugInfo(it) },
                onResetDefaults = { viewModel.resetToDefaults() },
                onRequestBatteryOptimization = onRequestBatteryOptimization
            )
        }
    }
}
