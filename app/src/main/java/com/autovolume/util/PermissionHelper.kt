package com.autovolume.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * 权限管理工具类
 *
 * 集中管理所有运行时权限的检查和申请逻辑。
 * 支持 Android 13+ 的新权限模型。
 */
object PermissionHelper {

    /** 需要申请的所有权限列表 */
    val REQUIRED_PERMISSIONS: Array<String> = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    /**
     * 检查是否所有必要权限都已授予
     */
    fun hasAllPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 检查是否缺少必要权限
     */
    fun getMissingPermissions(context: Context): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 检查是否在电池优化白名单中
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * 请求加入电池优化白名单
     *
     * 引导用户关闭电池优化，确保后台服务不被杀死。
     * 这对国产手机尤为重要。
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                // 完全不支持，忽略
            }
        }
    }

    /**
     * 获取厂商后台保护提示信息
     *
     * 不同厂商有不同的后台管理策略，需要引导用户手动设置。
     */
    fun getManufacturerWarning(): String? {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") ->
                "检测到小米/红米设备。请前往：设置 → 应用 → 后台管理 → AutoVolume → 允许后台运行"

            manufacturer.contains("huawei") || manufacturer.contains("honor") ->
                "检测到华为/荣耀设备。请前往：设置 → 电池 → 启动管理 → AutoVolume → 手动管理（全部允许）"

            manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") ->
                "检测到 OPPO/realme/一加设备。请前往：设置 → 电池 → 更多设置 → 自启动管理 → AutoVolume → 允许自启动"

            manufacturer.contains("vivo") ->
                "检测到 vivo 设备。请前往：设置 → 电池 → 后台耗电管理 → AutoVolume → 允许后台运行"

            manufacturer.contains("samsung") ->
                "检测到三星设备。请前往：设置 → 电池 → 后台使用限制 → AutoVolume → 添加到永不休眠应用"

            manufacturer.contains("meizu") ->
                "检测到魅族设备。请前往：手机管家 → 权限管理 → 后台管理 → AutoVolume → 允许后台运行"

            else -> null
        }
    }
}
