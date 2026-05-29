package com.autovolume.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 自定义图标定义
 *
 * 为了减小 APK 体积，不使用 material-icons-extended（~15MB），
 * 而是只定义实际使用的图标。
 *
 * 图标来源：Google Material Symbols (Apache 2.0 License)
 */

// ==================== Headset ====================
val Icons.Filled.Headset: ImageVector
    get() {
        if (_headset != null) return _headset!!
        _headset = materialIcon(name = "Filled.Headset") {
            materialPath {
                moveTo(12.0f, 1.0f)
                curveTo(7.0f, 1.0f, 3.0f, 5.0f, 3.0f, 10.0f)
                verticalLineTo(17.0f)
                curveTo(3.0f, 18.66f, 4.34f, 20.0f, 6.0f, 20.0f)
                horizontalLineTo(8.0f)
                curveTo(9.1f, 20.0f, 10.0f, 19.1f, 10.0f, 18.0f)
                verticalLineTo(14.0f)
                curveTo(10.0f, 12.9f, 9.1f, 12.0f, 8.0f, 12.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(10.0f)
                curveTo(6.0f, 6.69f, 8.69f, 4.0f, 12.0f, 4.0f)
                reflectiveCurveTo(18.0f, 6.69f, 18.0f, 10.0f)
                verticalLineTo(12.0f)
                horizontalLineTo(16.0f)
                curveTo(14.9f, 12.0f, 14.0f, 12.9f, 14.0f, 14.0f)
                verticalLineTo(18.0f)
                curveTo(14.0f, 19.1f, 14.9f, 20.0f, 16.0f, 20.0f)
                horizontalLineTo(18.0f)
                curveTo(19.66f, 20.0f, 21.0f, 18.66f, 21.0f, 17.0f)
                verticalLineTo(10.0f)
                curveTo(21.0f, 5.0f, 17.0f, 1.0f, 12.0f, 1.0f)
                close()
            }
        }
        return _headset!!
    }
private var _headset: ImageVector? = null

// ==================== Speaker ====================
val Icons.Filled.Speaker: ImageVector
    get() {
        if (_speaker != null) return _speaker!!
        _speaker = materialIcon(name = "Filled.Speaker") {
            materialPath {
                moveTo(3.0f, 9.0f)
                verticalLineTo(6.0f)
                horizontalLineTo(7.0f)
                lineTo(12.0f, 1.0f)
                verticalLineTo(23.0f)
                lineTo(7.0f, 18.0f)
                horizontalLineTo(3.0f)
                verticalLineTo(15.0f)
                horizontalLineTo(5.0f)
                lineTo(10.0f, 20.0f)
                verticalLineTo(4.0f)
                lineTo(5.0f, 9.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(16.5f, 12.0f)
                curveTo(16.5f, 10.23f, 15.48f, 8.71f, 14.0f, 7.97f)
                verticalLineTo(16.02f)
                curveTo(15.48f, 15.29f, 16.5f, 13.77f, 16.5f, 12.0f)
                close()
                moveTo(14.0f, 3.23f)
                verticalLineTo(5.29f)
                curveTo(16.89f, 6.15f, 19.0f, 8.83f, 19.0f, 12.0f)
                curveTo(19.0f, 15.17f, 16.89f, 17.84f, 14.0f, 18.7f)
                verticalLineTo(20.76f)
                curveTo(18.01f, 19.86f, 21.0f, 16.28f, 21.0f, 12.0f)
                curveTo(21.0f, 7.72f, 18.01f, 4.14f, 14.0f, 3.23f)
                close()
            }
        }
        return _speaker!!
    }
private var _speaker: ImageVector? = null

// ==================== BugReport ====================
val Icons.Filled.BugReport: ImageVector
    get() {
        if (_bugReport != null) return _bugReport!!
        _bugReport = materialIcon(name = "Filled.BugReport") {
            materialPath {
                moveTo(20.0f, 8.0f)
                horizontalLineTo(17.0f)
                curveTo(17.0f, 5.79f, 15.21f, 4.0f, 13.0f, 4.0f)
                horizontalLineTo(11.0f)
                curveTo(8.79f, 4.0f, 7.0f, 5.79f, 7.0f, 8.0f)
                horizontalLineTo(4.0f)
                curveTo(3.45f, 8.0f, 3.0f, 8.45f, 3.0f, 9.0f)
                verticalLineTo(10.0f)
                curveTo(3.0f, 10.55f, 3.45f, 11.0f, 4.0f, 11.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(15.0f)
                horizontalLineTo(4.0f)
                curveTo(3.45f, 15.0f, 3.0f, 15.45f, 3.0f, 16.0f)
                verticalLineTo(17.0f)
                curveTo(3.0f, 17.55f, 3.45f, 18.0f, 4.0f, 18.0f)
                horizontalLineTo(7.0f)
                curveTo(7.0f, 20.21f, 8.79f, 22.0f, 11.0f, 22.0f)
                horizontalLineTo(13.0f)
                curveTo(15.21f, 22.0f, 17.0f, 20.21f, 17.0f, 18.0f)
                horizontalLineTo(20.0f)
                curveTo(20.55f, 18.0f, 21.0f, 17.55f, 21.0f, 17.0f)
                verticalLineTo(16.0f)
                curveTo(21.0f, 15.45f, 20.55f, 15.0f, 20.0f, 15.0f)
                horizontalLineTo(19.0f)
                verticalLineTo(11.0f)
                horizontalLineTo(20.0f)
                curveTo(20.55f, 11.0f, 21.0f, 10.55f, 21.0f, 10.0f)
                verticalLineTo(9.0f)
                curveTo(21.0f, 8.45f, 20.55f, 8.0f, 20.0f, 8.0f)
                close()
                moveTo(15.0f, 18.0f)
                curveTo(15.0f, 19.1f, 14.1f, 20.0f, 13.0f, 20.0f)
                horizontalLineTo(11.0f)
                curveTo(9.9f, 20.0f, 9.0f, 19.1f, 9.0f, 18.0f)
                verticalLineTo(8.0f)
                curveTo(9.0f, 6.9f, 9.9f, 6.0f, 11.0f, 6.0f)
                horizontalLineTo(13.0f)
                curveTo(14.1f, 6.0f, 15.0f, 6.9f, 15.0f, 8.0f)
                verticalLineTo(18.0f)
                close()
            }
        }
        return _bugReport!!
    }
private var _bugReport: ImageVector? = null

// ==================== Tune ====================
val Icons.Filled.Tune: ImageVector
    get() {
        if (_tune != null) return _tune!!
        _tune = materialIcon(name = "Filled.Tune") {
            materialPath {
                moveTo(3.0f, 17.0f)
                verticalLineTo(2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(17.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(7.0f, 17.0f)
                verticalLineTo(7.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(17.0f)
                horizontalLineTo(7.0f)
                close()
                moveTo(11.0f, 17.0f)
                verticalLineTo(4.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(17.0f)
                horizontalLineTo(11.0f)
                close()
                moveTo(15.0f, 17.0f)
                verticalLineTo(12.0f)
                horizontalLineTo(17.0f)
                verticalLineTo(17.0f)
                horizontalLineTo(15.0f)
                close()
                moveTo(19.0f, 17.0f)
                verticalLineTo(9.0f)
                horizontalLineTo(21.0f)
                verticalLineTo(17.0f)
                horizontalLineTo(19.0f)
                close()
            }
        }
        return _tune!!
    }
private var _tune: ImageVector? = null

// ==================== Warning ====================
val Icons.Filled.Warning: ImageVector
    get() {
        if (_warning != null) return _warning!!
        _warning = materialIcon(name = "Filled.Warning") {
            materialPath {
                moveTo(1.0f, 21.0f)
                horizontalLineTo(23.0f)
                lineTo(12.0f, 2.0f)
                lineTo(1.0f, 21.0f)
                close()
                moveTo(13.0f, 18.0f)
                horizontalLineTo(11.0f)
                verticalLineTo(16.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(13.0f, 14.0f)
                horizontalLineTo(11.0f)
                verticalLineTo(10.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(14.0f)
                close()
            }
        }
        return _warning!!
    }
private var _warning: ImageVector? = null
