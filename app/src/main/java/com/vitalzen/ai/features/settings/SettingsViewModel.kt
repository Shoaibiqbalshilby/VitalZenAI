package com.vitalzen.ai.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalzen.ai.domain.model.AppSettings
import com.vitalzen.ai.domain.model.ThemeMode
import com.vitalzen.ai.domain.model.UnitSystem
import com.vitalzen.ai.domain.repository.AuthRepository
import com.vitalzen.ai.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun setThemeMode(themeMode: ThemeMode) = viewModelScope.launch {
        settingsRepository.updateThemeMode(themeMode)
    }

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.updateNotificationsEnabled(enabled)
    }

    fun setWellnessRemindersEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.updateWellnessRemindersEnabled(enabled)
    }

    fun setMeasurementRemindersEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.updateMeasurementRemindersEnabled(enabled)
    }

    fun setNotificationSoundEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.updateNotificationSoundEnabled(enabled)
    }

    fun setMeasurementDurationSeconds(seconds: Int) = viewModelScope.launch {
        settingsRepository.updateMeasurementDurationSeconds(seconds)
    }

    fun setDataSyncEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.updateDataSyncEnabled(enabled)
    }

    fun setLanguageCode(languageCode: String) = viewModelScope.launch {
        settingsRepository.updateLanguageCode(languageCode)
    }

    fun setUnitSystem(unitSystem: UnitSystem) = viewModelScope.launch {
        settingsRepository.updateUnitSystem(unitSystem)
    }

    suspend fun signOut() {
        authRepository.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> {
        return authRepository.deleteAccount()
    }
}