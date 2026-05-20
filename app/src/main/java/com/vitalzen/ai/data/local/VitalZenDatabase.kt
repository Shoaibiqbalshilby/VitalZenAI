package com.vitalzen.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vitalzen.ai.data.local.dao.VitalsDao
import com.vitalzen.ai.data.local.entity.VitalsEntity

@Database(entities = [VitalsEntity::class], version = 1, exportSchema = false)
abstract class VitalZenDatabase : RoomDatabase() {
    abstract fun vitalsDao(): VitalsDao
}
