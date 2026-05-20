package com.vitalzen.ai.features.scan

data class WellnessMetrics(
    val heartRate: Int = 0,
    val breathingRate: Int = 0,
    val hrvRmssd: Double = 0.0,
    val emotions: Map<String, Float> = emptyMap(),
    val confidenceScores: Map<String, Float> = emptyMap(),
    val pulseWaveform: List<Float> = emptyList(),
    val breathingWaveform: List<Float> = emptyList()
)
