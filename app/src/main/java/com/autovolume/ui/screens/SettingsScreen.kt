package com.autovolume.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autovolume.model.AppSettings
import com.autovolume.model.ThemeMode
import com.autovolume.ui.components.BugReport
import com.autovolume.ui.components.Code
import com.autovolume.ui.components.MusicNote

/**
 * 设置页面 - 所有可调节参数
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    themeMode: ThemeMode,
    onBack: () -> Unit,
    onDetectionIntervalChange: (Long) -> Unit,
    onSmoothingFactorChange: (Float) -> Unit,
    onNoiseThresholdChange: (Int) -> Unit,
    onMaxVolumeStepChange: (Int) -> Unit,
    onCooldownMsChange: (Long) -> Unit,
    onMaxAdjustmentsChange: (Int) -> Unit,
    onHeadsetOnlyChange: (Boolean) -> Unit,
    onNightModeChange: (Boolean) -> Unit,
    onNightMaxVolumeChange: (Int) -> Unit,
    onSmartPowerSavingChange: (Boolean) -> Unit,
    onScreenOffMultiplierChange: (Int) -> Unit,
    onAdaptiveSamplingChange: (Boolean) -> Unit,
    onNoiseMappingChange: (Int, Int, Int, Int, Int, Int, Int, Int) -> Unit,
    onResetDefaults: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onNavigateToAdvanced: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ===== 外观设置 =====
            SectionTitle("外观设置")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("主题模式", fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { onThemeModeChange(mode) },
                                label = { Text(mode.displayName, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ===== 检测参数 =====
            SectionTitle("检测参数")
            SettingSlider("检测间隔", settings.detectionIntervalMs.toFloat(),
                { onDetectionIntervalChange(it.toLong()) }, 200f..5000f, "${settings.detectionIntervalMs} ms")
            SettingSlider("平滑系数", settings.smoothingFactor,
                { onSmoothingFactorChange(it) }, 0.05f..0.95f, "%.2f".format(settings.smoothingFactor))
            SettingSlider("噪音阈值", settings.noiseThreshold.toFloat(),
                { onNoiseThresholdChange(it.toInt()) }, 1f..30f, "${settings.noiseThreshold} dB")

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ===== 音量映射 =====
            SectionTitle("音量映射曲线 (dB → %)")
            MappingRow("低", settings.noiseMappingLow, settings.volumeMappingLow, { n, v ->
                onNoiseMappingChange(n, v, settings.noiseMappingMid, settings.volumeMappingMid,
                    settings.noiseMappingHigh, settings.volumeMappingHigh, settings.noiseMappingMax, settings.volumeMappingMax)
            })
            MappingRow("中", settings.noiseMappingMid, settings.volumeMappingMid, { n, v ->
                onNoiseMappingChange(settings.noiseMappingLow, settings.volumeMappingLow, n, v,
                    settings.noiseMappingHigh, settings.volumeMappingHigh, settings.noiseMappingMax, settings.volumeMappingMax)
            })
            MappingRow("高", settings.noiseMappingHigh, settings.volumeMappingHigh, { n, v ->
                onNoiseMappingChange(settings.noiseMappingLow, settings.volumeMappingLow,
                    settings.noiseMappingMid, settings.volumeMappingMid, n, v,
                    settings.noiseMappingMax, settings.volumeMappingMax)
            })
            MappingRow("极", settings.noiseMappingMax, settings.volumeMappingMax, { n, v ->
                onNoiseMappingChange(settings.noiseMappingLow, settings.volumeMappingLow,
                    settings.noiseMappingMid, settings.volumeMappingMid,
                    settings.noiseMappingHigh, settings.volumeMappingHigh, n, v)
            })

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ===== 调节速度 =====
            SectionTitle("调节速度")
            SettingSlider("最大步进", settings.maxVolumeStep.toFloat(),
                { onMaxVolumeStepChange(it.toInt()) }, 1f..20f, "${settings.maxVolumeStep}%")
            SettingSlider("冷却时间", settings.cooldownMs.toFloat(),
                { onCooldownMsChange(it.toLong()) }, 500f..10000f, "${settings.cooldownMs} ms")
            SettingSlider("每秒最大调整", settings.maxAdjustmentsPerSecond.toFloat(),
                { onMaxAdjustmentsChange(it.toInt()) }, 1f..10f, "${settings.maxAdjustmentsPerSecond} 次")

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ===== 耳机检测 =====
            SectionTitle("耳机检测")
            SwitchSetting("仅耳机模式", "只在耳机连接时自动调节", settings.headsetOnly, onHeadsetOnlyChange)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ===== 安全设置 =====
            SectionTitle("安全设置")
            SwitchSetting("夜间模式", "22:00-07:00 限制最大音量", settings.nightModeEnabled, onNightModeChange)
            if (settings.nightModeEnabled) {
                SettingSlider("夜间最大音量", settings.nightMaxVolumePercent.toFloat(),
                    { onNightMaxVolumeChange(it.toInt()) }, 10f..100f, "${settings.nightMaxVolumePercent}%")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ===== 智能节能 =====
            SectionTitle("智能节能")
            SwitchSetting("智能节能", "屏幕关闭时降低检测频率", settings.smartPowerSaving, onSmartPowerSavingChange)
            if (settings.smartPowerSaving) {
                SettingSlider("屏幕关闭倍数", settings.screenOffIntervalMultiplier.toFloat(),
                    { onScreenOffMultiplierChange(it.toInt()) }, 2f..20f, "×${settings.screenOffIntervalMultiplier}")
            }
            SwitchSetting("动态自适应采样", "环境稳定时自动降低频率", settings.adaptiveSampling, onAdaptiveSamplingChange)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ===== 高级与调试 =====
            SectionTitle("高级与调试")
            OutlinedButton(
                onClick = onNavigateToAdvanced,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.BugReport, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("高级与调试")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 重置 =====
            OutlinedButton(
                onClick = onResetDefaults,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("重置为默认设置")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 关于软件 =====
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            AboutCard(context)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==================== 关于软件卡片 ====================

@Composable
private fun AboutCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // APP 图标
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            // APP 名称
            Text(
                "AutoVolume",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            // 版本
            Text(
                "v1.0.0",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            // 作者
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "作者：sshpc",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))

            // GitHub 链接
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Code,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sshpc/autovolume"))
                        context.startActivity(intent)
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "https://github.com/sshpc/autovolume",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}

// ==================== 通用组件 ====================

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun SettingSlider(
    label: String, value: Float, onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>, displayValue: String
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, modifier = Modifier.width(100.dp))
        Slider(value = value, onValueChange = onValueChange, valueRange = range,
            modifier = Modifier.weight(1f))
        Text(displayValue, fontSize = 13.sp, modifier = Modifier.width(80.dp),
            fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SwitchSetting(label: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Medium)
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MappingRow(label: String, db: Int, vol: Int, onChange: (Int, Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, modifier = Modifier.width(24.dp))
        Text("${db}dB", fontSize = 13.sp, modifier = Modifier.width(50.dp))
        Slider(value = db.toFloat(), onValueChange = { onChange(it.toInt(), vol) },
            valueRange = 20f..110f, modifier = Modifier.weight(1f))
        Text("${vol}%", fontSize = 13.sp, modifier = Modifier.width(40.dp))
        Slider(value = vol.toFloat(), onValueChange = { onChange(db, it.toInt()) },
            valueRange = 0f..100f, modifier = Modifier.weight(1f))
    }
}
