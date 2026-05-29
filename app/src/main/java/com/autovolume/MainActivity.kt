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
import com.autovolume.ui.screens.SettingsScreen
import com.autovolume.ui.theme.AutoVolumeTheme
import com.autovolume.ui.components.Warning
import com.autovolume.util.PermissionHelper

/**
 * 主 Activity（单 Activity 架构）
 *
 * 使用 Jetpack Compose Navigation 管理页面导航。
 * 所有页面（Home / Settings / Advanced）通过 NavController 切换。
 *
 * 权限处理：
 * 使用 ActivityResultContracts.RequestMultiplePermissions 申请所有必要权限。
 * 包含厂商后台保活引导对话框。
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
            AutoVolumeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AutoVolumeAppContent(
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
 *
 * 设置 NavHost 和三个页面路由。
 * 包含权限状态管理和厂商警告对话框。
 *
 * 所有实时数据（dB、音量、耳机状态）来自 ViewModel，
 * ViewModel 通过 Service companion object StateFlow 获取。
 */
@Composable
fun AutoVolumeAppContent(
    onRequestPermissions: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current

    // 从 ViewModel 获取所有状态（ViewModel 从 Service companion object 同步）
    val settings by viewModel.settings.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val currentDb by viewModel.currentDb.collectAsState()
    val currentVolume by viewModel.currentVolume.collectAsState()
    val rawMappedVolume by viewModel.rawMappedVolume.collectAsState()
    val adjustmentHistory by viewModel.adjustmentHistory.collectAsState()
    val headsetConnected by viewModel.headsetConnected.collectAsState()
    val headsetType by viewModel.headsetType.collectAsState()
    val manufacturerWarning by viewModel.manufacturerWarning.collectAsState()

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

    // 首次启动检查厂商警告
    LaunchedEffect(Unit) {
        viewModel.checkManufacturerWarning()
    }

    LaunchedEffect(manufacturerWarning) {
        if (manufacturerWarning != null) {
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
                onNavigateToAdvanced = { navController.navigate("advanced") }
            )
        }

        // ===== 设置页面 =====
        composable("settings") {
            SettingsScreen(
                settings = settings,
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
                onResetDefaults = { viewModel.resetToDefaults() }
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

    // ===== 厂商后台保活警告对话框 =====
    if (showManufacturerDialog && manufacturerWarning != null) {
        AlertDialog(
            onDismissRequest = { showManufacturerDialog = false },
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
                    onRequestBatteryOptimization()
                }) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManufacturerDialog = false }) {
                    Text("知道了")
                }
            }
        )
    }
}
