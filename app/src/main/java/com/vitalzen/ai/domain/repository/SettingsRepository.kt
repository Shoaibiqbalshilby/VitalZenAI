package com.vitalzen.ai.domain.repository

import com.vitalzen.ai.domain.model.AppSettings
import com.vitalzen.ai.domain.model.ThemeMode
import com.vitalzen.ai.domain.model.UnitSystem
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateWellnessRemindersEnabled(enabled: Boolean)
    suspend fun updateMeasurementRemindersEnabled(enabled: Boolean)
    suspend fun updateNotificationSoundEnabled(enabled: Boolean)
    suspend fun updateMeasurementDurationSeconds(seconds: Int)
    suspend fun updateDataSyncEnabled(enabled: Boolean)
    suspend fun updateLanguageCode(languageCode: String)
    suspend fun updateUnitSystem(unitSystem: UnitSystem)
}