package com.autovolume.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.autovolume.model.ThemeMode

// ==================== 颜色定义 ====================

// 主色调：科技蓝
private val PrimaryLight = Color(0xFF1976D2)
private val PrimaryDark = Color(0xFF90CAF9)
private val SecondaryLight = Color(0xFF26A69A)
private val SecondaryDark = Color(0xFF80CBC4)
private val TertiaryLight = Color(0xFFFF7043)
private val TertiaryDark = Color(0xFFFFAB91)

// 背景与表面
private val BackgroundLight = Color(0xFFF5F5F5)
private val SurfaceLight = Color(0xFFFFFFFF)
private val BackgroundDark = Color(0xFF121212)
private val SurfaceDark = Color(0xFF1E1E1E)

// 状态颜色
val StatusGreen = Color(0xFF4CAF50)
val StatusRed = Color(0xFFF44336)
val StatusYellow = Color(0xFFFFC107)
val StatusBlue = Color(0xFF2196F3)

// dB 等级颜色
val DbLow = Color(0xFF4CAF50)      // 安静 - 绿色
val DbMedium = Color(0xFFFFC107)   // 中等 - 黄色
val DbHigh = Color(0xFFFF9800)     // 较吵 - 橙色
val DbVeryHigh = Color(0xFFF44336) // 很吵 - 红色

// ==================== 浅色主题 ====================

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = SecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF004D40),
    tertiary = TertiaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

// ==================== 深色主题 ====================

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = SecondaryDark,
    onSecondary = Color(0xFF004D40),
    secondaryContainer = Color(0xFF00695C),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = TertiaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

/**
 * AutoVolume 主题
 *
 * @param themeMode 主题模式（浅色/深色/跟随系统）
 * @param content 内容
 */
@Composable
fun AutoVolumeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val dynamicColor = true // Android 12+ 动态取色

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
