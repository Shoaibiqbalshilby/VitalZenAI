package com.vitalzen.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalzen.ai.domain.model.ThemeMode
import com.vitalzen.ai.features.settings.SettingsViewModel
import com.vitalzen.ai.navigation.NavGraph
import com.vitalzen.ai.ui.theme.VitalZenAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitalZenAppRoot()
        }
    }
}

@Composable
private fun VitalZenAppRoot() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    VitalZenAITheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        NavGraph(navController = navController)
    }
}
