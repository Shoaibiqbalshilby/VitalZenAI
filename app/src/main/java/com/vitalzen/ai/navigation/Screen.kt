package com.vitalzen.ai.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Scan : Screen("scan")
    object History : Screen("history")
    object Profile : Screen("profile")
    object AIWellnessCoach : Screen("ai_coach")
    object Settings : Screen("settings")
    object Subscription : Screen("subscription")
}
