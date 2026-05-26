package com.vitalzen.ai.features.scan

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalzen.ai.BuildConfig
import com.vitalzen.ai.core.sdk.SmartSpectraManager
import com.vitalzen.ai.domain.model.Vitals
import com.vitalzen.ai.domain.repository.VitalsRepository
import com.vitalzen.ai.domain.usecase.CalculateWellnessScoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val smartSpectraManager: SmartSpectraManager,
    private val repository: VitalsRepository,
    private val calculateWellnessScore: CalculateWellnessScoreUseCase
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

        try {
            smartSpectraManager.initialize(key = BuildConfig.SMARTSPECTRA_API_KEY)
            smartSpectraManager.prepareForMeasurement()
        } catch (e: Exception) {
            _uiState.value = MeasurementState.Error(e.message ?: "Unable to start SmartSpectra")
            return
        }

        _uiState.value = MeasurementState.Initializing

        metricsJob = viewModelScope.launch {
            smartSpectraManager.getLiveMetrics().collect { metrics ->
                _liveMetrics.value = metrics
                if (isValidMeasurement(metrics) && _uiState.value is MeasurementState.Scanning) {
                    completeScan(metrics)
                }
            }
        }

        scanJob = viewModelScope.launch {
            _uiState.value = MeasurementState.Scanning(0)
            for (progress in 0..95 step 5) {
                if (_uiState.value !is MeasurementState.Scanning) return@launch
                _uiState.value = MeasurementState.Scanning(progress)
                delay(1_000)
            }

            if (_uiState.value is MeasurementState.Scanning) {
                _uiState.value = MeasurementState.Error(
                    "No valid SmartSpectra result was received. Please try again."
                )
                metricsJob?.cancel()
            }
        }
    }

    fun createMeasurementIntent(): Intent = smartSpectraManager.createMeasurementIntent()

    fun onMeasurementActivityClosed() {
        if (_uiState.value is MeasurementState.Scanning && !isValidMeasurement(_liveMetrics.value)) {
            scanJob?.cancel()
            metricsJob?.cancel()
            _uiState.value = MeasurementState.Error(
                "Measurement was cancelled or did not produce a usable result."
            )
        }
    }

    private suspend fun completeScan(metrics: WellnessMetrics) {
        if (_uiState.value !is MeasurementState.Scanning) return

        _uiState.value = MeasurementState.Finished(metrics)
        saveVitals(metrics)
        scanJob?.cancel()
        metricsJob?.cancel()
    }

    private fun isValidMeasurement(metrics: WellnessMetrics): Boolean {
        return metrics.heartRate > 0 || metrics.breathingRate > 0
    }

    private suspend fun saveVitals(metrics: WellnessMetrics) {
        val stressLevel = "Unknown"
        val mood = if (metrics.emotions["Talking"] == 1f) "Talking" else "Neutral"
        val oxygenLevel = 0

        repository.saveVitals(
            Vitals(
                timestamp = System.currentTimeMillis(),
                heartRate = metrics.heartRate,
                oxygenLevel = oxygenLevel,
                breathRate = metrics.breathingRate,
                wellnessScore = calculateWellnessScore(
                    heartRate = metrics.heartRate,
                    oxygenLevel = oxygenLevel,
                    stressLevel = stressLevel
                ),
                mood = mood,
                stressLevel = stressLevel
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
