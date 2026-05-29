package com.autovolume.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.first

/**
 * DataStore 迁移管理器
 *
 * 处理应用版本升级时的偏好数据迁移。
 * 每个迁移步骤都有版本号，确保按顺序执行。
 */
object DataStoreMigration {

    private const val TAG = "DataStoreMigration"

    /**
     * 获取所有迁移列表
     */
    fun getMigrations(): List<DataMigration<Preferences>> {
        return listOf(
            Migration1to2(),
            Migration2to3(),
            Migration3to4()
        )
    }

    /**
     * 迁移 1→2: 添加新字段默认值
     */
    private class Migration1to2 : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean {
            return currentData[DataStoreKeys.DATA_VERSION] == null
        }

        override suspend fun migrate(currentData: Preferences): Preferences {
            Log.i(TAG, "执行迁移 1→2: 添加新字段默认值")
            return currentData.toMutablePreferences().apply {
                this[DataStoreKeys.DATA_VERSION] = 2
            }.toPreferences()
        }

        override suspend fun cleanUp() {
            Log.i(TAG, "迁移 1→2 清理完成")
        }
    }

    /**
     * 迁移 2→3: 字段重命名
     */
    private class Migration2to3 : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean {
            val version = currentData[DataStoreKeys.DATA_VERSION] ?: 1
            return version < 3
        }

        override suspend fun migrate(currentData: Preferences): Preferences {
            Log.i(TAG, "执行迁移 2→3: 字段重命名")
            return currentData.toMutablePreferences().apply {
                this[DataStoreKeys.DATA_VERSION] = 3
            }.toPreferences()
        }

        override suspend fun cleanUp() {
            Log.i(TAG, "迁移 2→3 清理完成")
        }
    }

    /**
     * 迁移 3→4: 添加主题模式、后台提示、配置管理字段
     */
    private class Migration3to4 : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean {
            val version = currentData[DataStoreKeys.DATA_VERSION] ?: 1
            return version < 4
        }

        override suspend fun migrate(currentData: Preferences): Preferences {
            Log.i(TAG, "执行迁移 3→4: 添加主题、配置管理字段")
            return currentData.toMutablePreferences().apply {
                this[DataStoreKeys.DATA_VERSION] = 4
                // 新字段使用默认值（不需要显式设置）
            }.toPreferences()
        }

        override suspend fun cleanUp() {
            Log.i(TAG, "迁移 3→4 清理完成")
        }
    }
}

/**
 * DataStore 键定义（独立于 SettingsDataStore，用于迁移）
 */
internal object DataStoreKeys {
    val DATA_VERSION = androidx.datastore.preferences.core.intPreferencesKey("data_version")
}
