package com.autovolume

import android.app.Application
import android.util.Log

/**
 * Application 类
 *
 * 在应用进程创建时初始化。
 * 用于执行全局初始化操作。
 */
class AutoVolumeApp : Application() {

    companion object {
        private const val TAG = "AutoVolumeApp"
        lateinit var instance: AutoVolumeApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i(TAG, "应用进程创建")
    }
}
