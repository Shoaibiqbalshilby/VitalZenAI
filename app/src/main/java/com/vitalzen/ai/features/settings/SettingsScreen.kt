package com.vitalzen.ai.features.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalzen.ai.BuildConfig
import com.vitalzen.ai.domain.model.ThemeMode
import com.vitalzen.ai.domain.model.UnitSystem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionCard(title = "Appearance") {
                ChoiceChipsRow(
                    title = "Dark mode",
                    options = listOf(
                        ThemeMode.SYSTEM to "System",
                        ThemeMode.LIGHT to "Light",
                        ThemeMode.DARK to "Dark"
                    ),
                    selected = settings.themeMode to settings.themeMode.name,
                    labelFor = { it.second },
                    onSelected = { viewModel.setThemeMode(it.first) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionCard(title = "Notifications") {
                SettingsSwitchRow(
                    title = "Push notifications",
                    subtitle = "Master notification switch",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
                SettingsSwitchRow(
                    title = "Wellness reminders",
                    subtitle = "Breathing, hydration, and recovery nudges",
                    checked = settings.wellnessRemindersEnabled,
                    onCheckedChange = viewModel::setWellnessRemindersEnabled
                )
                SettingsSwitchRow(
                    title = "Measurement reminders",
                    subtitle = "Prompt to take your next scan",
                    checked = settings.measurementRemindersEnabled,
                    onCheckedChange = viewModel::setMeasurementRemindersEnabled
                )
                SettingsSwitchRow(
                    title = "Sound alerts",
                    subtitle = "Play a sound for important notifications",
                    checked = settings.notificationSoundEnabled,
                    onCheckedChange = viewModel::setNotificationSoundEnabled
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionCard(title = "Measurement") {
                ChoiceChipsRow(
                    title = "Duration",
                    options = listOf(30, 60, 90),
                    selected = settings.measurementDurationSeconds,
                    labelFor = { "${it}s" },
                    onSelected = viewModel::setMeasurementDurationSeconds
                )
                Spacer(modifier = Modifier.height(12.dp))
                ChoiceChipsRow(
                    title = "Units",
                    options = listOf(UnitSystem.METRIC, UnitSystem.IMPERIAL),
                    selected = settings.unitSystem,
                    labelFor = { if (it == UnitSystem.METRIC) "Metric" else "Imperial" },
                    onSelected = viewModel::setUnitSystem
                )
                Spacer(modifier = Modifier.height(12.dp))
                ChoiceChipsRow(
                    title = "Language",
                    options = listOf(
                        LanguageOption("en", "English"),
                        LanguageOption("es", "Spanish"),
                        LanguageOption("fr", "French"),
                        LanguageOption("de", "German")
                    ),
                    selected = languageOptionFor(settings.languageCode),
                    labelFor = { it.label },
                    onSelected = { viewModel.setLanguageCode(it.code) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionCard(title = "Sync") {
                SettingsSwitchRow(
                    title = "Data sync",
                    subtitle = "Back up scan history and preferences",
                    checked = settings.dataSyncEnabled,
                    onCheckedChange = viewModel::setDataSyncEnabled
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionCard(title = "Legal") {
                SettingsActionRow(
                    title = "Privacy policy",
                    subtitle = "Open the privacy policy in your browser",
                    onClick = { openUrl(context, PRIVACY_POLICY_URL) }
                )
                SettingsActionRow(
                    title = "Terms of use",
                    subtitle = "Review the terms that govern the app",
                    onClick = { openUrl(context, TERMS_OF_USE_URL) }
                )
                SettingsActionRow(
                    title = "Friction log",
                    subtitle = "Open the latest product friction log",
                    onClick = { openUrl(context, FRICTION_LOG_URL) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionCard(title = "Account") {
                SettingsActionRow(
                    title = "Logout",
                    subtitle = "Sign out of this device",
                    leadingIcon = Icons.Default.ExitToApp,
                    onClick = { showLogoutDialog = true }
                )
                SettingsActionRow(
                    title = "Delete account",
                    subtitle = "Permanently remove your account",
                    leadingIcon = Icons.Default.Delete,
                    destructive = true,
                    onClick = { showDeleteDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSectionCard(title = "About") {
                ListItem(
                    headlineContent = { Text("VitalZen AI") },
                    supportingContent = { Text("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") },
                    trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) }
                )
                ListItem(
                    headlineContent = { Text("Wellness companion") },
                    supportingContent = {
                        Text("Track scans, review trends, and tune the app to your routine.")
                    }
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Sign out of your account on this device?") },
            confirmButton = {
                Button(onClick = {
                    showLogoutDialog = false
                    scope.launch {
                        viewModel.signOut()
                        snackbarHostState.showSnackbar("Logged out")
                    }
                }) { Text("Logout") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account") },
            text = { Text("This permanently removes the authenticated account from Firebase. This action cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    scope.launch {
                        val message = viewModel.deleteAccount().fold(
                            onSuccess = { "Account deleted" },
                            onFailure = { it.message ?: "Failed to delete account" }
                        )
                        snackbarHostState.showSnackbar(message)
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors()
            )
        }
    )
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector? = null,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = title,
                color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = { Text(subtitle) },
        leadingContent = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) }
    )
}

@Composable
private fun <T> ChoiceChipsRow(
    title: String,
    options: List<T>,
    selected: T,
    labelFor: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    label = { Text(labelFor(option)) }
                )
            }
        }
    }
}

private data class LanguageOption(
    val code: String,
    val label: String
)

private fun languageOptionFor(code: String): LanguageOption = when (code) {
    "es" -> LanguageOption("es", "Spanish")
    "fr" -> LanguageOption("fr", "French")
    "de" -> LanguageOption("de", "German")
    else -> LanguageOption("en", "English")
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

private const val PRIVACY_POLICY_URL = "https://vitalzen.ai/privacy"
private const val TERMS_OF_USE_URL = "https://vitalzen.ai/terms"
private const val FRICTION_LOG_URL = "https://github.com/Shoaibiqbalshilby/VitalZenAI/blob/main/FRICTION_LOG.md"
