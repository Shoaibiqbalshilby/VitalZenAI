package com.vitalzen.ai.features.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated != null) {
            if (isAuthenticated == true) {
                onNavigateToHome()
            } else {
                onNavigateToAuth()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "VitalZen AI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
