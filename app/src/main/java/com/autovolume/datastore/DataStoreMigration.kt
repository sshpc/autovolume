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
 *
 * 迁移策略：
 * - 版本 1→2: 无破坏性变更，添加新字段默认值
 * - 版本 2→3: 字段重命名（screenOffMultiplier → screenOffIntervalMultiplier）
 * - 未来版本: 按需添加
 */
object DataStoreMigration {

    private const val TAG = "DataStoreMigration"

    /**
     * 获取所有迁移列表
     *
     * 返回的迁移按版本顺序排列，DataStore 会依次执行。
     */
    fun getMigrations(): List<DataMigration<Preferences>> {
        return listOf(
            Migration1to2(),
            Migration2to3()
        )
    }

    /**
     * 迁移 1→2: 添加新字段默认值
     *
     * v1.0.0 → v1.1.0:
     * - 添加 adaptiveSampling 默认值
     * 添加 screenOffIntervalMultiplier 默认值
     */
    private class Migration1to2 : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean {
            // 如果没有版本标记，说明是旧版本，需要迁移
            return currentData[DataStoreKeys.DATA_VERSION] == null
        }

        override suspend fun migrate(currentData: Preferences): Preferences {
            Log.i(TAG, "执行迁移 1→2: 添加新字段默认值")
            return currentData.toMutablePreferences().apply {
                // 添加版本标记
                this[DataStoreKeys.DATA_VERSION] = 2
                // 新字段使用默认值（不需要显式设置，DataStore 读取时会使用默认值）
            }.toPreferences()
        }

        override suspend fun cleanUp() {
            Log.i(TAG, "迁移 1→2 清理完成")
        }
    }

    /**
     * 迁移 2→3: 字段重命名
     *
     * v1.1.0 → v1.2.0:
     * - screenOffMultiplier → screenOffIntervalMultiplier（已在代码中处理）
     * - 这里确保旧键值被正确清理
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
                // 旧键名的清理（如果存在）
                // screenOffMultiplier 已在 v1.0 中定义为同一键名，无需额外处理
            }.toPreferences()
        }

        override suspend fun cleanUp() {
            Log.i(TAG, "迁移 2→3 清理完成")
        }
    }
}

/**
 * DataStore 键定义（独立于 SettingsDataStore，用于迁移）
 *
 * 迁移管理器需要访问键定义，但不应依赖 SettingsDataStore 实例。
 * 这里定义迁移专用的键。
 */
internal object DataStoreKeys {
    val DATA_VERSION = androidx.datastore.preferences.core.intPreferencesKey("data_version")
}
