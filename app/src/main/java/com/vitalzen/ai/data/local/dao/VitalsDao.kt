package com.vitalzen.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitalzen.ai.data.local.entity.VitalsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitals(vitals: VitalsEntity)

    @Query("SELECT * FROM vitals_history ORDER BY timestamp DESC")
    fun getAllVitals(): Flow<List<VitalsEntity>>

    @Query("SELECT * FROM vitals_history WHERE id = :id")
    suspend fun getVitalsById(id: Long): VitalsEntity?
}
