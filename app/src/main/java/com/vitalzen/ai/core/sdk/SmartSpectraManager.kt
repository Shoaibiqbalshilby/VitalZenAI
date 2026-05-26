package com.vitalzen.ai.core.sdk

import android.content.Context
import android.content.Intent
import androidx.camera.core.CameraSelector
import androidx.lifecycle.Observer
import com.presagetech.smartspectra.SmartSpectraMode
import com.presagetech.smartspectra.SmartSpectraSdk
import com.presagetech.smartspectra.ui.SmartSpectraActivity
import com.presage.physiology.proto.MetricsProto
import com.vitalzen.ai.features.scan.WellnessMetrics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class SmartSpectraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        const val MAX_WAVEFORM_SAMPLES = 120
    }

    private val sdk = SmartSpectraSdk.Companion.run {
        initialize(context)
        getInstance()
    }

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    private val _liveMetrics = MutableStateFlow(WellnessMetrics())
    private val liveMetrics = _liveMetrics.asStateFlow()

    private val pulseWaveformBuffer = ArrayDeque<Float>()
    private val breathingWaveformBuffer = ArrayDeque<Float>()

    private var apiKey: String? = null
    private var latestErrorMessage: String? = null

    private val metricsObserver = Observer<MetricsProto.MetricsBuffer?> { metrics ->
        metrics?.let(::updateMetrics)
    }

    private val errorObserver = Observer<String?> { message ->
        latestErrorMessage = message?.takeIf { it.isNotBlank() }
    }

    init {
        sdk.metricsBuffer.observeForever(metricsObserver)
        SmartSpectraSdkCompat.getErrorMessage(sdk).observeForever(errorObserver)
        sdk.setCameraPosition(CameraSelector.LENS_FACING_FRONT)
        sdk.setMeasurementDuration(30.0)
        sdk.setSmartSpectraMode(SmartSpectraMode.SPOT)
    }

    fun initialize(key: String) {
        require(key.isNotBlank()) { "SmartSpectra API key is missing" }
        apiKey = key
        latestErrorMessage = null
        sdk.setApiKey(key)
        _isInitialized.value = true
    }

    fun prepareForMeasurement() {
        pulseWaveformBuffer.clear()
        breathingWaveformBuffer.clear()
        latestErrorMessage = null
        _liveMetrics.value = WellnessMetrics()
        SmartSpectraSdkCompat.clearMetricsBuffer(sdk)
    }

    fun createMeasurementIntent(): Intent {
        check(_isInitialized.value && !apiKey.isNullOrBlank()) { "SDK not initialized" }
        return Intent(context, SmartSpectraActivity::class.java)
    }

    fun getLiveMetrics(): Flow<WellnessMetrics> = liveMetrics

    fun stopMeasurement() {
        latestErrorMessage = null
    }

    private fun updateMetrics(metrics: MetricsProto.MetricsBuffer) {
        if (metrics.hasPulse()) {
            appendWaveform(pulseWaveformBuffer, metrics.pulse.traceList.map { it.value })
        }
        if (metrics.hasBreathing()) {
            appendWaveform(breathingWaveformBuffer, metrics.breathing.upperTraceList.map { it.value })
        }

        val pulseSample = metrics.takeIf { it.hasPulse() }
            ?.pulse
            ?.rateList
            ?.lastOrNull()
        val breathingSample = metrics.takeIf { it.hasBreathing() }
            ?.breathing
            ?.rateList
            ?.lastOrNull()

        val faceSignals = buildMap {
            val face = metrics.takeIf { it.hasFace() }?.face ?: return@buildMap
            face.blinkingList.lastOrNull()?.let { put("Blinking", if (it.detected) 1f else 0f) }
            face.talkingList.lastOrNull()?.let { put("Talking", if (it.detected) 1f else 0f) }
        }

        _liveMetrics.value = WellnessMetrics(
            heartRate = pulseSample?.value?.roundToInt() ?: _liveMetrics.value.heartRate,
            breathingRate = breathingSample?.value?.roundToInt() ?: _liveMetrics.value.breathingRate,
            hrvRmssd = 0.0,
            emotions = faceSignals.ifEmpty { _liveMetrics.value.emotions },
            confidenceScores = buildMap {
                pulseSample?.let { put("HR", it.confidence) }
                breathingSample?.let { put("BR", it.confidence) }
            }.ifEmpty { _liveMetrics.value.confidenceScores },
            pulseWaveform = normalizeWaveform(pulseWaveformBuffer),
            breathingWaveform = normalizeWaveform(breathingWaveformBuffer)
        )
    }

    private fun appendWaveform(target: ArrayDeque<Float>, samples: List<Float>) {
        samples.takeLast(MAX_WAVEFORM_SAMPLES).forEach { sample ->
            target.addLast(sample)
            while (target.size > MAX_WAVEFORM_SAMPLES) {
                target.removeFirst()
            }
        }
    }

    private fun normalizeWaveform(samples: ArrayDeque<Float>): List<Float> {
        if (samples.isEmpty()) return emptyList()

        val minValue = samples.minOrNull() ?: return emptyList()
        val maxValue = samples.maxOrNull() ?: return emptyList()
        val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f

        return samples.map { ((it - minValue) / range).coerceIn(0f, 1f) }
    }
}
