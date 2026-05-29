package com.autovolume.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

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

// ==================== MusicNote ====================
val Icons.Filled.MusicNote: ImageVector
    get() {
        if (_musicNote != null) return _musicNote!!
        _musicNote = materialIcon(name = "Filled.MusicNote") {
            materialPath {
                moveTo(12.0f, 3.0f)
                verticalLineTo(13.56f)
                curveTo(11.34f, 13.21f, 10.6f, 13.0f, 9.82f, 13.0f)
                curveTo(7.73f, 13.0f, 6.0f, 14.79f, 6.0f, 17.0f)
                reflectiveCurveTo(7.73f, 21.0f, 9.82f, 21.0f)
                reflectiveCurveTo(13.64f, 19.21f, 13.64f, 17.0f)
                verticalLineTo(7.0f)
                horizontalLineTo(17.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(12.0f)
                close()
            }
        }
        return _musicNote!!
    }
private var _musicNote: ImageVector? = null

// ==================== Person ====================
val Icons.Filled.Person: ImageVector
    get() {
        if (_person != null) return _person!!
        _person = materialIcon(name = "Filled.Person") {
            materialPath {
                moveTo(12.0f, 12.0f)
                curveTo(14.21f, 12.0f, 16.0f, 10.21f, 16.0f, 8.0f)
                reflectiveCurveTo(14.21f, 4.0f, 12.0f, 4.0f)
                reflectiveCurveTo(8.0f, 5.79f, 8.0f, 8.0f)
                reflectiveCurveTo(9.79f, 12.0f, 12.0f, 12.0f)
                close()
                moveTo(12.0f, 14.0f)
                curveTo(9.33f, 14.0f, 4.0f, 15.34f, 4.0f, 18.0f)
                verticalLineTo(20.0f)
                horizontalLineTo(20.0f)
                verticalLineTo(18.0f)
                curveTo(20.0f, 15.34f, 14.67f, 14.0f, 12.0f, 14.0f)
                close()
            }
        }
        return _person!!
    }
private var _person: ImageVector? = null

// ==================== Code ====================
val Icons.Filled.Code: ImageVector
    get() {
        if (_code != null) return _code!!
        _code = materialIcon(name = "Filled.Code") {
            materialPath {
                moveTo(9.4f, 16.6f)
                lineTo(4.8f, 12.0f)
                lineTo(9.4f, 7.4f)
                lineTo(8.0f, 6.0f)
                lineTo(2.0f, 12.0f)
                lineTo(8.0f, 18.0f)
                lineTo(9.4f, 16.6f)
                close()
                moveTo(14.6f, 16.6f)
                lineTo(19.2f, 12.0f)
                lineTo(14.6f, 7.4f)
                lineTo(16.0f, 6.0f)
                lineTo(22.0f, 12.0f)
                lineTo(16.0f, 18.0f)
                lineTo(14.6f, 16.6f)
                close()
            }
        }
        return _code!!
    }
private var _code: ImageVector? = null

// ==================== Favorite ====================
val Icons.Filled.Favorite: ImageVector
    get() {
        if (_favorite != null) return _favorite!!
        _favorite = materialIcon(name = "Filled.Favorite") {
            materialPath {
                moveTo(12.0f, 21.35f)
                lineTo(10.55f, 20.03f)
                curveTo(5.4f, 15.36f, 2.0f, 12.28f, 2.0f, 8.5f)
                curveTo(2.0f, 5.42f, 4.42f, 3.0f, 7.5f, 3.0f)
                curveTo(9.24f, 3.0f, 10.91f, 3.81f, 12.0f, 5.09f)
                curveTo(13.09f, 3.81f, 14.76f, 3.0f, 16.5f, 3.0f)
                curveTo(19.58f, 3.0f, 22.0f, 5.42f, 22.0f, 8.5f)
                curveTo(22.0f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
                lineTo(12.0f, 21.35f)
                close()
            }
        }
        return _favorite!!
    }
private var _favorite: ImageVector? = null

// ==================== Settings ====================
val Icons.Filled.Settings: ImageVector
    get() {
        if (_settings != null) return _settings!!
        _settings = materialIcon(name = "Filled.Settings") {
            materialPath {
                moveTo(19.14f, 12.94f)
                curveTo(19.18f, 12.64f, 19.2f, 12.33f, 19.2f, 12.0f)
                reflectiveCurveTo(19.18f, 11.36f, 19.13f, 11.06f)
                lineTo(21.16f, 9.48f)
                curveTo(21.34f, 9.34f, 21.39f, 9.07f, 21.28f, 8.87f)
                lineTo(19.36f, 5.55f)
                curveTo(19.24f, 5.33f, 18.99f, 5.26f, 18.77f, 5.33f)
                lineTo(16.38f, 6.29f)
                curveTo(15.88f, 5.91f, 15.35f, 5.59f, 14.76f, 5.35f)
                lineTo(14.4f, 2.81f)
                curveTo(14.36f, 2.57f, 14.16f, 2.4f, 13.92f, 2.4f)
                horizontalLineTo(10.08f)
                curveTo(9.84f, 2.4f, 9.65f, 2.57f, 9.61f, 2.81f)
                lineTo(9.25f, 5.35f)
                curveTo(8.66f, 5.59f, 8.12f, 5.92f, 7.63f, 6.29f)
                lineTo(5.24f, 5.33f)
                curveTo(5.02f, 5.25f, 4.77f, 5.33f, 4.65f, 5.55f)
                lineTo(2.74f, 8.87f)
                curveTo(2.62f, 9.08f, 2.66f, 9.34f, 2.86f, 9.48f)
                lineTo(4.89f, 11.06f)
                curveTo(4.84f, 11.36f, 4.8f, 11.69f, 4.8f, 12.0f)
                reflectiveCurveTo(4.84f, 12.64f, 4.89f, 12.94f)
                lineTo(2.86f, 14.52f)
                curveTo(2.68f, 14.66f, 2.63f, 14.93f, 2.74f, 15.13f)
                lineTo(4.65f, 18.45f)
                curveTo(4.77f, 18.67f, 5.02f, 18.74f, 5.24f, 18.67f)
                lineTo(7.63f, 17.71f)
                curveTo(8.13f, 18.09f, 8.66f, 18.41f, 9.25f, 18.65f)
                lineTo(9.61f, 21.19f)
                curveTo(9.65f, 21.43f, 9.84f, 21.6f, 10.08f, 21.6f)
                horizontalLineTo(13.92f)
                curveTo(14.16f, 21.6f, 14.36f, 21.43f, 14.39f, 21.19f)
                lineTo(14.75f, 18.65f)
                curveTo(15.34f, 18.41f, 15.88f, 18.09f, 16.37f, 17.71f)
                lineTo(18.76f, 18.67f)
                curveTo(18.98f, 18.75f, 19.23f, 18.67f, 19.35f, 18.45f)
                lineTo(21.27f, 15.13f)
                curveTo(21.39f, 14.92f, 21.34f, 14.66f, 21.15f, 14.52f)
                lineTo(19.14f, 12.94f)
                close()
                moveTo(12.0f, 15.6f)
                curveTo(10.02f, 15.6f, 8.4f, 13.98f, 8.4f, 12.0f)
                reflectiveCurveTo(10.02f, 8.4f, 12.0f, 8.4f)
                reflectiveCurveTo(15.6f, 10.02f, 15.6f, 12.0f)
                reflectiveCurveTo(13.98f, 15.6f, 12.0f, 15.6f)
                close()
            }
        }
        return _settings!!
    }
private var _settings: ImageVector? = null

// ==================== Add ====================
val Icons.Filled.Add: ImageVector
    get() {
        if (_add != null) return _add!!
        _add = materialIcon(name = "Filled.Add") {
            materialPath {
                moveTo(19.0f, 13.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(19.0f)
                horizontalLineTo(11.0f)
                verticalLineTo(13.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(11.0f)
                horizontalLineTo(11.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(11.0f)
                horizontalLineTo(19.0f)
                verticalLineTo(13.0f)
                close()
            }
        }
        return _add!!
    }
private var _add: ImageVector? = null

// ==================== Edit ====================
val Icons.Filled.Edit: ImageVector
    get() {
        if (_edit != null) return _edit!!
        _edit = materialIcon(name = "Filled.Edit") {
            materialPath {
                moveTo(3.0f, 17.25f)
                verticalLineTo(21.0f)
                horizontalLineTo(6.75f)
                lineTo(17.81f, 9.94f)
                lineTo(14.06f, 6.19f)
                lineTo(3.0f, 17.25f)
                close()
                moveTo(20.71f, 7.04f)
                curveTo(21.1f, 6.65f, 21.1f, 6.02f, 20.71f, 5.63f)
                lineTo(18.37f, 3.29f)
                curveTo(17.98f, 2.9f, 17.35f, 2.9f, 16.96f, 3.29f)
                lineTo(15.13f, 5.12f)
                lineTo(18.88f, 8.87f)
                lineTo(20.71f, 7.04f)
                close()
            }
        }
        return _edit!!
    }
private var _edit: ImageVector? = null

// ==================== Delete ====================
val Icons.Filled.Delete: ImageVector
    get() {
        if (_delete != null) return _delete!!
        _delete = materialIcon(name = "Filled.Delete") {
            materialPath {
                moveTo(6.0f, 19.0f)
                curveTo(6.0f, 20.1f, 6.9f, 21.0f, 8.0f, 21.0f)
                horizontalLineTo(16.0f)
                curveTo(17.1f, 21.0f, 18.0f, 20.1f, 18.0f, 19.0f)
                verticalLineTo(7.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(19.0f, 4.0f)
                horizontalLineTo(15.5f)
                lineTo(14.5f, 3.0f)
                horizontalLineTo(9.5f)
                lineTo(8.5f, 4.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(6.0f)
                horizontalLineTo(19.0f)
                verticalLineTo(4.0f)
                close()
            }
        }
        return _delete!!
    }
private var _delete: ImageVector? = null

// ==================== CheckCircle ====================
val Icons.Filled.CheckCircle: ImageVector
    get() {
        if (_checkCircle != null) return _checkCircle!!
        _checkCircle = materialIcon(name = "Filled.CheckCircle") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveTo(6.48f, 22.0f, 12.0f, 22.0f)
                reflectiveCurveTo(22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(10.0f, 17.0f)
                lineTo(5.0f, 12.0f)
                lineTo(6.41f, 10.59f)
                lineTo(10.0f, 14.17f)
                lineTo(17.59f, 6.58f)
                lineTo(19.0f, 8.0f)
                lineTo(10.0f, 17.0f)
                close()
            }
        }
        return _checkCircle!!
    }
private var _checkCircle: ImageVector? = null
