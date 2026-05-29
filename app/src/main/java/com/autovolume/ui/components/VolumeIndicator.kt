package com.autovolume.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autovolume.ui.theme.*

/**
 * 音量指示器组件
 *
 * 圆弧式音量显示，带渐变色和动画。
 *
 * @param currentVolume 当前音量百分比
 * @param targetVolume 目标音量百分比
 * @param minVolume 最小音量限制
 * @param maxVolume 最大音量限制
 * @param modifier Modifier
 */
@Composable
fun VolumeIndicator(
    currentVolume: Int,
    targetVolume: Int,
    minVolume: Int = 0,
    maxVolume: Int = 100,
    modifier: Modifier = Modifier
) {
    val animatedCurrent by animateFloatAsState(
        targetValue = currentVolume.toFloat(),
        animationSpec = tween(durationMillis = 500, easing = EaseOutCubic),
        label = "current_volume"
    )

    val animatedTarget by animateFloatAsState(
        targetValue = targetVolume.toFloat(),
        animationSpec = tween(durationMillis = 300),
        label = "target_volume"
    )

    // 音量颜色
    val volumeColor = when {
        currentVolume < 30 -> StatusBlue
        currentVolume < 60 -> StatusGreen
        currentVolume < 80 -> StatusYellow
        else -> StatusRed
    }

    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // 背景弧
            drawArc(
                color = Color.Gray.copy(alpha = 0.15f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // 目标音量弧（虚线效果）
            val targetSweep = animatedTarget / 100f * 270f
            drawArc(
                color = StatusGreen.copy(alpha = 0.3f),
                startAngle = 135f,
                sweepAngle = targetSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth * 0.5f, cap = StrokeCap.Round)
            )

            // 当前音量弧
            val currentSweep = animatedCurrent / 100f * 270f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(StatusBlue, StatusGreen, StatusYellow, StatusRed)
                ),
                startAngle = 135f,
                sweepAngle = currentSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // 音量限制标记
            val minAngle = Math.toRadians((135.0 + minVolume / 100.0 * 270.0))
            val maxAngle = Math.toRadians((135.0 + maxVolume / 100.0 * 270.0))
            val radius = size.width / 2 - strokeWidth

            drawCircle(
                color = StatusYellow,
                radius = 4.dp.toPx(),
                center = Offset(
                    (size.width / 2 + radius * kotlin.math.cos(minAngle)).toFloat(),
                    (size.height / 2 + radius * kotlin.math.sin(minAngle)).toFloat()
                )
            )
            drawCircle(
                color = StatusRed,
                radius = 4.dp.toPx(),
                center = Offset(
                    (size.width / 2 + radius * kotlin.math.cos(maxAngle)).toFloat(),
                    (size.height / 2 + radius * kotlin.math.sin(maxAngle)).toFloat()
                )
            )
        }

        // 中心文字
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$currentVolume%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = volumeColor
            )
            Text(
                text = "→ $targetVolume%",
                fontSize = 12.sp,
                color = StatusGreen
            )
        }
    }
}
