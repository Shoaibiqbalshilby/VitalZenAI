package com.vitalzen.ai.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.vitalzen.ai.data.local.dao.VitalsDao
import com.vitalzen.ai.data.local.entity.VitalsEntity
import com.vitalzen.ai.domain.model.Vitals
import com.vitalzen.ai.domain.repository.VitalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VitalsRepositoryImpl @Inject constructor(
    private val dao: VitalsDao,
    private val firestore: FirebaseFirestore
) : VitalsRepository {

    override suspend fun saveVitals(vitals: Vitals) {
        val entity = vitals.toEntity()
        dao.insertVitals(entity)
        
        // Sync to Firestore (Simplified version)
        try {
            firestore.collection("vitals_history")
                .add(vitals)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getVitalsHistory(): Flow<List<Vitals>> {
        return dao.getAllVitals().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getVitalsById(id: Long): Vitals? {
        return dao.getVitalsById(id)?.toDomain()
    }

    private fun VitalsEntity.toDomain() = Vitals(
        id = id,
        timestamp = timestamp,
        heartRate = heartRate,
        oxygenLevel = oxygenLevel,
        breathRate = breathRate,
        wellnessScore = wellnessScore,
        mood = mood,
        stressLevel = stressLevel
    )

    private fun Vitals.toEntity() = VitalsEntity(
        id = id,
        timestamp = timestamp,
        heartRate = heartRate,
        oxygenLevel = oxygenLevel,
        breathRate = breathRate,
        wellnessScore = wellnessScore,
        mood = mood,
        stressLevel = stressLevel
    )
}
