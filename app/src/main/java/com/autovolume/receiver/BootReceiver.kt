package com.autovolume.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.autovolume.datastore.SettingsDataStore
import com.autovolume.service.AutoVolumeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 开机自启动广播接收器
 *
 * 工作流程：
 * 1. 接收 BOOT_COMPLETED 广播
 * 2. 检查用户上次是否启用了自动调节
 * 3. 如果是，自动恢复后台服务
 *
 * 兼容性：
 * - 标准 Android：监听 BOOT_COMPLETED
 * - 部分厂商（如 HTC）：监听 QUICKBOOT_POWERON
 *
 * 注意：用户必须在系统设置中允许自启动权限（部分厂商需要手动开启）
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON" &&
            intent.action != "com.htc.intent.action.QUICKBOOT_POWERON") {
            return
        }

        Log.i(TAG, "收到开机广播: ${intent.action}")

        // 使用 goAsync() 延长广播处理时间，以便完成异步数据读取
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val settingsDataStore = SettingsDataStore(context)

                // 检查上次服务是否在运行
                val wasRunning = settingsDataStore.serviceWasRunningFlow.first()

                if (wasRunning) {
                    Log.i(TAG, "上次服务在运行，自动恢复服务")
                    AutoVolumeService.start(context)
                } else {
                    Log.i(TAG, "上次服务未运行，跳过自动启动")
                }
            } catch (e: Exception) {
                Log.e(TAG, "开机自启处理失败", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
