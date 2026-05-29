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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.autovolume.service.AutoVolumeService
import com.autovolume.ui.MainViewModel
import com.autovolume.ui.screens.AdvancedScreen
import com.autovolume.ui.screens.HomeScreen
import com.autovolume.ui.screens.ProfileScreen
import com.autovolume.ui.screens.SettingsScreen
import com.autovolume.ui.theme.AutoVolumeTheme
import com.autovolume.ui.components.Warning
import com.autovolume.util.PermissionHelper

/**
 * 主 Activity（单 Activity 架构）
 */
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            // 权限被拒绝
        }
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
                        },
                        onOpenAppSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                            }
                            startActivity(intent)
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

/**
 * Compose 应用入口
 */
@Composable
fun AutoVolumeAppContent(
    viewModel: MainViewModel,
    onRequestPermissions: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // 从 ViewModel 获取所有状态
    val settings by viewModel.settings.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val currentDb by viewModel.currentDb.collectAsState()
    val currentVolume by viewModel.currentVolume.collectAsState()
    val rawMappedVolume by viewModel.rawMappedVolume.collectAsState()
    val adjustmentHistory by viewModel.adjustmentHistory.collectAsState()
    val headsetConnected by viewModel.headsetConnected.collectAsState()
    val headsetType by viewModel.headsetType.collectAsState()
    val manufacturerWarning by viewModel.manufacturerWarning.collectAsState()
    val backgroundTipShown by viewModel.backgroundTipShown.collectAsState()
    val profileNames by viewModel.profileNames.collectAsState()
    val currentProfileName by viewModel.currentProfileName.collectAsState()

    // 权限状态
    val hasAudioPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    // 监听权限变化
    LaunchedEffect(Unit) {
        snapshotFlow {
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        }.collect { result ->
            hasAudioPermission.value = result == PackageManager.PERMISSION_GRANTED
        }
    }

    // 厂商警告对话框
    var showManufacturerDialog by remember { mutableStateOf(false) }

    // 首次启动检查厂商警告（只在未展示过时弹出）
    LaunchedEffect(Unit) {
        if (!backgroundTipShown) {
            viewModel.checkManufacturerWarning()
        }
    }

    LaunchedEffect(manufacturerWarning) {
        if (manufacturerWarning != null && !backgroundTipShown) {
            showManufacturerDialog = true
        }
    }

    // 主导航
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // ===== 主页面 =====
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
                    if (!hasAudioPermission.value) {
                        onRequestPermissions()
                    } else {
                        viewModel.toggleService()
                    }
                },
                onToggleEnabled = { viewModel.updateIsEnabled(it) },
                onRunModeChange = { viewModel.updateRunMode(it) },
                onMinVolumeChange = { viewModel.updateMinVolume(it) },
                onMaxVolumeChange = { viewModel.updateMaxVolume(it) },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToAdvanced = { navController.navigate("advanced") },
                onProfileSwitch = { viewModel.switchProfile(it) },
                onOpenAppSettings = {
                    val missingPerms = viewModel.getMissingPermissions()
                    if (missingPerms.isNotEmpty()) {
                        viewModel.openAppSettings()
                    }
                },
                missingPermissions = viewModel.getMissingPermissions()
            )
        }

        // ===== 设置页面 =====
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
                onNavigateToAdvanced = { navController.navigate("advanced") }
            )
        }

        // ===== 配置管理页面 =====
        composable("profile") {
            ProfileScreen(
                profileNames = profileNames,
                currentProfileName = currentProfileName,
                onBack = { navController.popBackStack() },
                onCreateProfile = { viewModel.createProfile(it) },
                onDeleteProfile = { viewModel.deleteProfile(it) },
                onRenameProfile = { oldName, newName -> viewModel.renameProfile(oldName, newName) },
                onSwitchProfile = { viewModel.switchProfile(it) }
            )
        }

        // ===== 高级调试页面 =====
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
                onResetDefaults = { viewModel.resetToDefaults() }
            )
        }
    }

    // ===== 厂商后台保活警告对话框（只在首次显示） =====
    if (showManufacturerDialog && manufacturerWarning != null) {
        AlertDialog(
            onDismissRequest = {
                showManufacturerDialog = false
                viewModel.markBackgroundTipShown()
            },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("后台运行提示") },
            text = {
                Column {
                    Text(manufacturerWarning!!)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "如果 AutoVolume 在后台被系统杀死，请按上述步骤设置。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showManufacturerDialog = false
                    viewModel.markBackgroundTipShown()
                    onRequestBatteryOptimization()
                }) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showManufacturerDialog = false
                    viewModel.markBackgroundTipShown()
                }) {
                    Text("知道了")
                }
            }
        )
    }
}
