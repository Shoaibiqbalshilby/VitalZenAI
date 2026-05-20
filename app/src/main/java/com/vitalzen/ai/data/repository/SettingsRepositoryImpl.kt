package com.vitalzen.ai.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vitalzen.ai.domain.model.AppSettings
import com.vitalzen.ai.domain.model.ThemeMode
import com.vitalzen.ai.domain.model.UnitSystem
import com.vitalzen.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            AppSettings(
                themeMode = preferences[THEME_MODE_KEY]?.toThemeMode() ?: ThemeMode.SYSTEM,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED_KEY] ?: true,
                wellnessRemindersEnabled = preferences[WELLNESS_REMINDERS_ENABLED_KEY] ?: true,
                measurementRemindersEnabled = preferences[MEASUREMENT_REMINDERS_ENABLED_KEY] ?: true,
                notificationSoundEnabled = preferences[NOTIFICATION_SOUND_ENABLED_KEY] ?: true,
                measurementDurationSeconds = preferences[MEASUREMENT_DURATION_SECONDS_KEY] ?: 60,
                dataSyncEnabled = preferences[DATA_SYNC_ENABLED_KEY] ?: true,
                languageCode = preferences[LANGUAGE_CODE_KEY] ?: "en",
                unitSystem = preferences[UNIT_SYSTEM_KEY]?.toUnitSystem() ?: UnitSystem.METRIC
            )
        }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    override suspend fun updateWellnessRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[WELLNESS_REMINDERS_ENABLED_KEY] = enabled
        }
    }

    override suspend fun updateMeasurementRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[MEASUREMENT_REMINDERS_ENABLED_KEY] = enabled
        }
    }

    override suspend fun updateNotificationSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_SOUND_ENABLED_KEY] = enabled
        }
    }

    override suspend fun updateMeasurementDurationSeconds(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[MEASUREMENT_DURATION_SECONDS_KEY] = seconds
        }
    }

    override suspend fun updateDataSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DATA_SYNC_ENABLED_KEY] = enabled
        }
    }

    override suspend fun updateLanguageCode(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE_KEY] = languageCode
        }
    }

    override suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        dataStore.edit { preferences ->
            preferences[UNIT_SYSTEM_KEY] = unitSystem.name
        }
    }

    private fun String.toThemeMode(): ThemeMode = runCatching {
        ThemeMode.valueOf(this)
    }.getOrDefault(ThemeMode.SYSTEM)

    private fun String.toUnitSystem(): UnitSystem = runCatching {
        UnitSystem.valueOf(this)
    }.getOrDefault(UnitSystem.METRIC)

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val WELLNESS_REMINDERS_ENABLED_KEY = booleanPreferencesKey("wellness_reminders_enabled")
        val MEASUREMENT_REMINDERS_ENABLED_KEY = booleanPreferencesKey("measurement_reminders_enabled")
        val NOTIFICATION_SOUND_ENABLED_KEY = booleanPreferencesKey("notification_sound_enabled")
        val MEASUREMENT_DURATION_SECONDS_KEY = intPreferencesKey("measurement_duration_seconds")
        val DATA_SYNC_ENABLED_KEY = booleanPreferencesKey("data_sync_enabled")
        val LANGUAGE_CODE_KEY = stringPreferencesKey("language_code")
        val UNIT_SYSTEM_KEY = stringPreferencesKey("unit_system")
    }
}