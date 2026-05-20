package com.vitalzen.ai.domain.repository

import com.vitalzen.ai.domain.model.Vitals
import kotlinx.coroutines.flow.Flow

interface VitalsRepository {
    suspend fun saveVitals(vitals: Vitals)
    fun getVitalsHistory(): Flow<List<Vitals>>
    suspend fun getVitalsById(id: Long): Vitals?
}
