package com.vitalzen.ai.domain.model

data class Vitals(
    val id: Long = 0,
    val timestamp: Long,
    val heartRate: Int,
    val oxygenLevel: Int,
    val breathRate: Int,
    val wellnessScore: Int,
    val mood: String,
    val stressLevel: String
)
