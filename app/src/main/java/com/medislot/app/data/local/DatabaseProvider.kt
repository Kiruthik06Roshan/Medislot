package com.medislot.app.data.local

import android.content.Context

object DatabaseProvider {
    private var database: AppDatabase? = null
    private var dataStoreManager: DataStoreManager? = null

    fun initialize(context: Context) {
        if (database == null) {
            database = AppDatabase.getDatabase(context)
        }
        if (dataStoreManager == null) {
            dataStoreManager = DataStoreManager(context)
        }
    }

    fun getDatabase(): AppDatabase {
        return database ?: throw IllegalStateException("DatabaseProvider not initialized. Call initialize(context) first.")
    }

    fun getDataStoreManager(): DataStoreManager {
        return dataStoreManager ?: throw IllegalStateException("DatabaseProvider not initialized. Call initialize(context) first.")
    }
}
