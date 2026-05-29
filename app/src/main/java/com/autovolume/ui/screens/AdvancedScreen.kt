package com.autovolume.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autovolume.model.AppSettings
import com.autovolume.model.RunMode
import com.autovolume.model.VolumeAdjustmentEvent
import com.autovolume.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 高级设置与调试页面
 *
 * 展示：
 * - 运行模式详细参数
 * - 实时 dB 和音量映射
 * - 调节历史记录
 * - 导入/导出配置
 * - 重置默认
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedScreen(
    settings: AppSettings,
    currentDb: Float,
    currentVolume: Int,
    rawMappedVolume: Int,
    adjustmentHistory: List<VolumeAdjustmentEvent>,
    onBack: () -> Unit,
    onSampleRateChange: (Int) -> Unit,
    onShowDebugChange: (Boolean) -> Unit,
    onResetDefaults: () -> Unit,
    onRequestBatteryOptimization: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("高级与调试") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== 实时状态 =====
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("实时状态", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        StatusRow("环境噪音", "%.1f dB".format(currentDb))
                        StatusRow("当前媒体音量", "$currentVolume%")
                        StatusRow("映射目标（原始）", "$rawMappedVolume%")
                        StatusRow("映射目标（限制后）",
                            "${rawMappedVolume.coerceIn(settings.minVolumePercent, settings.maxVolumePercent)}%")
                        StatusRow("运行模式", settings.runMode.displayName)
                        StatusRow("夜间时段", if (isNightTime()) "是" else "否")
                    }
                }
            }

            // ===== 当前模式参数 =====
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("当前模式参数", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        StatusRow("采样率", "${settings.sampleRate} Hz")
                        StatusRow("检测间隔", "${settings.detectionIntervalMs} ms")
                        StatusRow("平滑系数", "%.2f".format(settings.smoothingFactor))
                        StatusRow("最大步进", "${settings.maxVolumeStep}%")
                        StatusRow("冷却时间", "${settings.cooldownMs} ms")
                        StatusRow("最大调整/秒", "${settings.maxAdjustmentsPerSecond}")
                        StatusRow("自适应采样", if (settings.adaptiveSampling) "开启" else "关闭")
                    }
                }
            }

            // ===== 自定义参数 =====
            if (settings.runMode == RunMode.CUSTOM) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("自定义参数", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("采样率", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(8000, 16000, 22050, 44100).forEach { rate ->
                                    FilterChip(
                                        selected = settings.sampleRate == rate,
                                        onClick = { onSampleRateChange(rate) },
                                        label = { Text("${rate/1000}k", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ===== 调试开关 =====
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("显示调试信息", fontWeight = FontWeight.Medium)
                            Text("在主页面显示详细数据", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = settings.showDebugInfo, onCheckedChange = onShowDebugChange)
                    }
                }
            }

            // ===== 后台运行优化 =====
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("后台运行优化", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "为确保 AutoVolume 在后台正常运行，建议：",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("1. 将 AutoVolume 加入电池优化白名单", fontSize = 13.sp)
                        Text("2. 在系统设置中允许自启动", fontSize = 13.sp)
                        Text("3. 关闭对 AutoVolume 的后台限制", fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onRequestBatteryOptimization,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("电池优化设置")
                        }
                        Spacer(Modifier.height(8.dp))
                        val manufacturer = android.os.Build.MANUFACTURER
                        val warning = com.autovolume.util.PermissionHelper.getManufacturerWarning()
                        if (warning != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "$manufacturer 设备提示",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        warning,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ===== 调节历史 =====
            item {
                Text("调节历史 (最近20条)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (adjustmentHistory.isEmpty()) {
                item {
                    Text("暂无调节记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(adjustmentHistory.take(20)) { event ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                .format(Date(event.timestamp)), fontSize = 12.sp)
                            Text("${event.fromVolume}% → ${event.toVolume}%",
                                fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text("%.1f dB".format(event.triggerDb), fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ===== 重置 =====
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onResetDefaults,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重置所有设置为默认值")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

private fun isNightTime(): Boolean {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return hour >= 22 || hour < 7
}
