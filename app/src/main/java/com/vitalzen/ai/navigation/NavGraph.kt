package com.vitalzen.ai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vitalzen.ai.features.ai.AiCoachScreen
import com.vitalzen.ai.features.auth.SplashScreen
import com.vitalzen.ai.features.home.HomeScreen
import com.vitalzen.ai.features.scan.ScanScreen
import com.vitalzen.ai.features.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAuth = {
                    // For now, navigate to Home even if not auth, to allow testing
                    // In real app, navigate to Auth
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onScanClick = {
                    navController.navigate(Screen.Scan.route)
                },
                onCoachClick = {
                    navController.navigate(Screen.AIWellnessCoach.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Scan.route) {
            ScanScreen(
                onScanFinished = {
                    navController.popBackStack()
                },
                onClose = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.AIWellnessCoach.route) {
            AiCoachScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
