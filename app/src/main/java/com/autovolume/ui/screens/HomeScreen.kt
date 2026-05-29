package com.autovolume.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autovolume.headset.HeadsetDetector
import com.autovolume.model.AppSettings
import com.autovolume.model.RunMode
import com.autovolume.ui.components.DbMeter
import com.autovolume.ui.components.VolumeIndicator
import com.autovolume.ui.theme.*
import com.autovolume.ui.components.BugReport
import com.autovolume.ui.components.Headset
import com.autovolume.ui.components.Speaker
import com.autovolume.ui.components.Tune

/**
 * 主页面
 *
 * 展示实时噪音检测、音量状态、耳机连接、模式切换等核心信息。
 * 使用增强的可视化组件：渐变 dB 仪表盘 + 圆弧音量指示器。
 */
@Composable
fun HomeScreen(
    settings: AppSettings,
    isServiceRunning: Boolean,
    currentDb: Float,
    currentVolume: Int,
    rawMappedVolume: Int,
    headsetConnected: Boolean,
    headsetType: HeadsetDetector.HeadsetType,
    onToggleService: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onRunModeChange: (RunMode) -> Unit,
    onMinVolumeChange: (Int) -> Unit,
    onMaxVolumeChange: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdvanced: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ===== 主开关卡片 =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (settings.isEnabled)
                    MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("自动音量调节", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 运行状态指示灯
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .padding(end = 6.dp)
                        ) {
                            if (settings.isEnabled && isServiceRunning) {
                                // 绿色呼吸灯效果由 Compose 动画处理
                                Surface(
                                    modifier = Modifier.size(8.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = StatusGreen
                                ) {}
                            } else {
                                Surface(
                                    modifier = Modifier.size(8.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                ) {}
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = when {
                                settings.isEnabled && isServiceRunning -> "运行中"
                                settings.isEnabled -> "启动中..."
                                else -> "已停止"
                            },
                            fontSize = 14.sp,
                            color = if (settings.isEnabled && isServiceRunning) StatusGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = settings.isEnabled,
                    onCheckedChange = { enabled ->
                        onToggleEnabled(enabled)
                        onToggleService()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== dB 仪表盘 =====
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("环境噪音", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                DbMeter(dbLevel = currentDb)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 音量指示器 =====
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("音量状态", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                VolumeIndicator(
                    currentVolume = currentVolume,
                    targetVolume = rawMappedVolume,
                    minVolume = settings.minVolumePercent,
                    maxVolume = settings.maxVolumePercent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 耳机状态 =====
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (headsetConnected) Icons.Default.Headset
                    else Icons.Default.Speaker,
                    contentDescription = null,
                    tint = if (headsetConnected) StatusGreen
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (headsetConnected) "耳机已连接" else "未连接耳机",
                        fontWeight = FontWeight.Medium
                    )
                    Text(headsetType.displayName, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (settings.headsetOnly && !headsetConnected) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("连接耳机后自动启用", fontSize = 11.sp) },
                        icon = { Icon(Icons.Default.Info, null, Modifier.size(14.dp)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 运行模式 =====
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("运行模式", fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RunMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.runMode == mode,
                            onClick = { onRunModeChange(mode) },
                            label = { Text(mode.displayName, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 快速设置：音量范围 =====
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("音量范围", fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("最小", fontSize = 14.sp, modifier = Modifier.width(40.dp))
                    Slider(
                        value = settings.minVolumePercent.toFloat(),
                        onValueChange = { onMinVolumeChange(it.toInt()) },
                        valueRange = 0f..50f,
                        steps = 49,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${settings.minVolumePercent}%",
                        fontSize = 14.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(45.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("最大", fontSize = 14.sp, modifier = Modifier.width(40.dp))
                    Slider(
                        value = settings.maxVolumePercent.toFloat(),
                        onValueChange = { onMaxVolumeChange(it.toInt()) },
                        valueRange = 50f..100f,
                        steps = 49,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${settings.maxVolumePercent}%",
                        fontSize = 14.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(45.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 导航按钮 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Tune, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("设置")
            }
            OutlinedButton(
                onClick = onNavigateToAdvanced,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.BugReport, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("高级/调试")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
