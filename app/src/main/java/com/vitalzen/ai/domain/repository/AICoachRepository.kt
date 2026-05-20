package com.vitalzen.ai.domain.repository

import kotlinx.coroutines.flow.Flow

interface AICoachRepository {
    suspend fun getAdvice(vitalsId: Long): String
    fun getChatHistory(): Flow<List<String>>
}
