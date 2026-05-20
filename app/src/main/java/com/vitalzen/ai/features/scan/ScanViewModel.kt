package com.vitalzen.ai.features.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalzen.ai.core.sdk.SmartSpectraManager
import com.vitalzen.ai.domain.model.Vitals
import com.vitalzen.ai.domain.repository.VitalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val smartSpectraManager: SmartSpectraManager,
    private val repository: VitalsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MeasurementState>(MeasurementState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _liveMetrics = MutableStateFlow(WellnessMetrics())
    val liveMetrics = _liveMetrics.asStateFlow()

    private var scanJob: Job? = null
    private var metricsJob: Job? = null

    fun startScan() {
        scanJob?.cancel()
        metricsJob?.cancel()

        _uiState.value = MeasurementState.Initializing

        scanJob = viewModelScope.launch {
            try {
                // Initialize with a dummy API key
                smartSpectraManager.initialize(key = "VITAL-ZEN-API-KEY")
                
                _uiState.value = MeasurementState.Scanning(0)
                
                // Collect live metrics
                metricsJob = launch {
                    smartSpectraManager.getLiveMetrics().collect { metrics ->
                        _liveMetrics.value = metrics
                    }
                }

                smartSpectraManager.startMeasurement().collect { progress ->
                    _uiState.value = MeasurementState.Scanning(progress)
                }

                val finalMetrics = _liveMetrics.value
                _uiState.value = MeasurementState.Finished(finalMetrics)
                
                saveVitals(finalMetrics)
                
            } catch (e: Exception) {
                _uiState.value = MeasurementState.Error(e.message ?: "Unknown error")
            } finally {
                metricsJob?.cancel()
            }
        }
    }

    private suspend fun saveVitals(metrics: WellnessMetrics) {
        repository.saveVitals(
            Vitals(
                timestamp = System.currentTimeMillis(),
                heartRate = metrics.heartRate,
                oxygenLevel = 98,
                breathRate = metrics.breathingRate,
                wellnessScore = 85,
                mood = metrics.emotions.keys.firstOrNull() ?: "Neutral",
                stressLevel = if (metrics.hrvRmssd < 40) "High" else "Low"
            )
        )
    }

    fun stopScan() {
        smartSpectraManager.stopMeasurement()
        scanJob?.cancel()
        metricsJob?.cancel()
        _uiState.value = MeasurementState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}
