package com.vitalzen.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vitals_history")
data class VitalsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val heartRate: Int,
    val oxygenLevel: Int,
    val breathRate: Int,
    val wellnessScore: Int,
    val mood: String,
    val stressLevel: String
)
