package com.vitalzen.ai.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalzen.ai.domain.model.Vitals
import com.vitalzen.ai.domain.repository.VitalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: VitalsRepository
) : ViewModel() {

    private val _timeRange = MutableStateFlow(TimeRange.WEEK)
    val timeRange = _timeRange.asStateFlow()

    val analyticsData = combine(
        repository.getVitalsHistory(),
        _timeRange
    ) { history, range ->
        processHistory(history, range)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsState())

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
    }

    private fun processHistory(history: List<Vitals>, range: TimeRange): AnalyticsState {
        if (history.isEmpty()) return AnalyticsState()

        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        
        val filtered = when (range) {
            TimeRange.DAY -> history.filter { it.timestamp > now - 24 * 60 * 60 * 1000 }
            TimeRange.WEEK -> history.filter { it.timestamp > now - 7 * 24 * 60 * 60 * 1000 }
            TimeRange.MONTH -> history.filter { it.timestamp > now - 30L * 24 * 60 * 60 * 1000 }
        }

        return AnalyticsState(
            avgHeartRate = filtered.map { it.heartRate }.average().toInt(),
            avgHrv = filtered.map { if (it.stressLevel == "Low") 80.0 else 40.0 }.average(), // Mocking HRV from stress level if not saved
            moodDistribution = filtered.groupBy { it.mood }.mapValues { it.value.size.toFloat() },
            heartRateTrend = filtered.sortedBy { it.timestamp }.map { it.timestamp.toFloat() to it.heartRate.toFloat() },
            wellnessScores = filtered.sortedBy { it.timestamp }.map { it.timestamp.toFloat() to it.wellnessScore.toFloat() }
        )
    }
}

enum class TimeRange { DAY, WEEK, MONTH }

data class AnalyticsState(
    val avgHeartRate: Int = 0,
    val avgHrv: Double = 0.0,
    val moodDistribution: Map<String, Float> = emptyMap(),
    val heartRateTrend: List<Pair<Float, Float>> = emptyList(),
    val wellnessScores: List<Pair<Float, Float>> = emptyList()
)
