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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autovolume.headset.HeadsetDetector
import com.autovolume.model.AppSettings
import com.autovolume.model.RunMode
import com.autovolume.ui.PermissionItem
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
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    settings: AppSettings,
    isServiceRunning: Boolean,
    currentDb: Float,
    currentVolume: Int,
    rawMappedVolume: Int,
    headsetConnected: Boolean,
    headsetType: HeadsetDetector.HeadsetType,
    profileNames: List<String>,
    currentProfileName: String,
    onToggleService: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onRunModeChange: (RunMode) -> Unit,
    onMinVolumeChange: (Int) -> Unit,
    onMaxVolumeChange: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onProfileSwitch: (String) -> Unit,
    onOpenAppSettings: () -> Unit,
    missingPermissions: List<PermissionItem>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ===== 权限缺失提示卡片 =====
        if (missingPermissions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "权限缺失",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    missingPermissions.forEach { perm ->
                        Text(
                            "• ${perm.name}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                        )
                        Text(
                            perm.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onOpenAppSettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("前往系统设置授权")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

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
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .padding(end = 6.dp)
                        ) {
                            if (settings.isEnabled && isServiceRunning) {
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

        // ===== 配置切换 =====
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("当前配置", fontWeight = FontWeight.Medium)
                    TextButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Settings, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("管理", fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (profileNames.isEmpty()) {
                    Text(
                        "暂无自定义配置，使用默认设置",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 始终显示默认配置
                        FilterChip(
                            selected = currentProfileName == "默认",
                            onClick = { onProfileSwitch("默认") },
                            label = { Text("默认") }
                        )
                        // 显示用户自定义配置
                        profileNames.filter { it != "默认" }.forEach { name ->
                            FilterChip(
                                selected = currentProfileName == name,
                                onClick = { onProfileSwitch(name) },
                                label = {
                                    Text(
                                        name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }
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

        // ===== 音量指示器（居中） =====
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("音量状态", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    VolumeIndicator(
                        currentVolume = currentVolume,
                        targetVolume = rawMappedVolume,
                        minVolume = settings.minVolumePercent,
                        maxVolume = settings.maxVolumePercent
                    )
                }
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

        // ===== 运行模式（FlowRow 自动换行） =====
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("运行模式", fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RunMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.runMode == mode,
                            onClick = { onRunModeChange(mode) },
                            label = {
                                Text(
                                    mode.displayName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
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
                onClick = onNavigateToProfile,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Favorite, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("配置管理")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
