package com.autovolume.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autovolume.ui.theme.*

/**
 * 分贝仪表盘组件（增强版）
 *
 * 特性：
 * - 渐变色条表示噪音等级
 * - 平滑动画过渡
 * - 高噪音时脉冲闪烁效果
 * - 实时 dB 数值 + 等级文字
 * - 刻度标签
 *
 * @param dbLevel 当前 dB 值
 * @param isStable 环境是否稳定
 * @param modifier Modifier
 */
@Composable
fun DbMeter(
    dbLevel: Float,
    isStable: Boolean = true,
    modifier: Modifier = Modifier
) {
    // dB 归一化到 0-1 范围 (20-110dB)
    val normalizedDb = ((dbLevel - 20f) / 90f).coerceIn(0f, 1f)

    // 动画平滑过渡
    val animatedProgress by animateFloatAsState(
        targetValue = normalizedDb,
        animationSpec = tween(durationMillis = 300),
        label = "db_progress"
    )

    // 高噪音脉冲动画
    val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // 根据 dB 等级选择颜色
    val dbColor = when {
        dbLevel < 40 -> DbLow
        dbLevel < 60 -> DbMedium
        dbLevel < 80 -> DbHigh
        else -> DbVeryHigh
    }

    // 高噪音时使用脉冲效果
    val displayAlpha = if (dbLevel >= 80) pulseAlpha else 1f

    // 噪音等级文字
    val levelText = when {
        dbLevel < 30 -> "极安静"
        dbLevel < 40 -> "安静"
        dbLevel < 50 -> "较安静"
        dbLevel < 60 -> "适中"
        dbLevel < 70 -> "较吵"
        dbLevel < 80 -> "吵闹"
        dbLevel < 90 -> "很吵"
        else -> "极吵"
    }

    // 稳定性指示
    val stabilityText = if (isStable) "● 稳定" else "◌ 波动"
    val stabilityColor = if (isStable) StatusGreen else StatusYellow

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // dB 数值显示（带脉冲效果）
        Text(
            text = "%.1f dB".format(dbLevel),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = dbColor.copy(alpha = displayAlpha)
        )

        // 噪音等级 + 稳定性
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = levelText,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stabilityText,
                fontSize = 12.sp,
                color = stabilityColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 渐变进度条
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .padding(horizontal = 16.dp)
        ) {
            val barWidth = size.width
            val barHeight = size.height
            val cornerRadius = barHeight / 2

            // 背景条
            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.15f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(cornerRadius)
            )

            // 渐变填充
            val fillWidth = barWidth * animatedProgress
            if (fillWidth > 0) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(DbLow, DbMedium, DbHigh, DbVeryHigh),
                        startX = 0f,
                        endX = fillWidth
                    ),
                    size = Size(fillWidth, barHeight),
                    cornerRadius = CornerRadius(cornerRadius)
                )
            }

            // 指示器小圆点
            val indicatorX = fillWidth
            drawCircle(
                color = Color.White,
                radius = barHeight * 0.6f,
                center = Offset(indicatorX, barHeight / 2)
            )
            drawCircle(
                color = dbColor,
                radius = barHeight * 0.4f,
                center = Offset(indicatorX, barHeight / 2)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 刻度标签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("20", "40", "60", "80", "100").forEach { label ->
                Text(label, fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
