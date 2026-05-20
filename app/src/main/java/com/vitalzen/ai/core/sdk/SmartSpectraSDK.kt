package com.vitalzen.ai.core.sdk

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

data class ScanResult(
    val heartRate: Int,
    val oxygenLevel: Int,
    val breathRate: Int,
    val wellnessScore: Int,
    val mood: String,
    val stressLevel: String
)

interface SmartSpectraSDK {
    fun startScan(): Flow<Int> // Progress 0-100
    suspend fun getResult(): ScanResult
}

@Singleton
class SmartSpectraSDKMock @Inject constructor() : SmartSpectraSDK {
    override fun startScan(): Flow<Int> = flow {
        for (i in 0..100 step 5) {
            emit(i)
            delay(200)
        }
    }

    override suspend fun getResult(): ScanResult {
        return ScanResult(
            heartRate = (60..100).random(),
            oxygenLevel = (95..100).random(),
            breathRate = (12..20).random(),
            wellnessScore = (70..95).random(),
            mood = listOf("Calm", "Happy", "Stressed", "Energetic").random(),
            stressLevel = listOf("Low", "Moderate", "High").random()
        )
    }
}
