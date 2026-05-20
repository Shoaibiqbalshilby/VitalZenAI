package com.vitalzen.ai.features.scan

sealed class MeasurementState {
    object Idle : MeasurementState()
    object Initializing : MeasurementState()
    data class Scanning(val progress: Int) : MeasurementState()
    data class Finished(val metrics: WellnessMetrics) : MeasurementState()
    data class Error(val message: String) : MeasurementState()
}
