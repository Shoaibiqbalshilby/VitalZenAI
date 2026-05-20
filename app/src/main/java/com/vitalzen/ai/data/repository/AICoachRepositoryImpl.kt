package com.vitalzen.ai.data.repository

import com.vitalzen.ai.domain.repository.AICoachRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class AICoachRepositoryImpl @Inject constructor() : AICoachRepository {
    override suspend fun getAdvice(vitalsId: Long): String {
        return "Based on your recent scan, you seem a bit stressed. Try a 5-minute breathing exercise to lower your heart rate."
    }

    override fun getChatHistory(): Flow<List<String>> {
        return flowOf(listOf("Hello! I'm your VitalZen AI coach. How can I help you today?"))
    }
}
