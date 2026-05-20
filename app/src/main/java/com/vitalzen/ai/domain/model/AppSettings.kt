package com.vitalzen.ai.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class UnitSystem {
    METRIC,
    IMPERIAL
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val wellnessRemindersEnabled: Boolean = true,
    val measurementRemindersEnabled: Boolean = true,
    val notificationSoundEnabled: Boolean = true,
    val measurementDurationSeconds: Int = 60,
    val dataSyncEnabled: Boolean = true,
    val languageCode: String = "en",
    val unitSystem: UnitSystem = UnitSystem.METRIC
)