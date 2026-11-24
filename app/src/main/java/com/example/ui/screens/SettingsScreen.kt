package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.CalendarSystem
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.ai.AssistantPersona
import com.example.data.local.AppPreferencesState
import com.example.data.local.UserProfileEntity
import com.example.ui.components.PlanifyCard
import com.example.ui.theme.PlanifyThemeType
import com.example.ui.theme.buildPalette
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    prefs: AppPreferencesState,
    profile: UserProfileEntity?,
    connectionTestStatus: String?,
    backupStatus: String?,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetCalendar: (CalendarSystem) -> Unit,
    onSetTheme: (PlanifyThemeType) -> Unit,
    onSetCustomTheme: (Long, Long, Boolean) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onSetNotifications: (Boolean) -> Unit,
    onUpdateProfile: (String, Int) -> Unit,
    onSaveAiConfig: (String, String, String, Map<String, String>, Float, Int, String) -> Unit,
    onTestConnection: (String, String) -> Unit,
    onSetAppLockPin: (String) -> Unit,
    onExportBackup: (Uri) -> Unit,
    onImportBackup: (Uri) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val context = LocalContext.current

    var profileName by remember(profile) { mutableStateOf(profile?.displayName.orEmpty()) }
    var dailyGoal by remember(profile) { mutableStateOf((profile?.dailyFocusGoalMinutes ?: 90).toString()) }

    var apiKey by remember(prefs.openRouterApiKey) { mutableStateOf(prefs.openRouterApiKey) }
    var baseUrl by remember(prefs.openRouterBaseUrl) { mutableStateOf(prefs.openRouterBaseUrl) }
    var defaultModel by remember(prefs.openRouterDefaultModel) { mutableStateOf(prefs.openRouterDefaultModel) }
    var aiTemp by remember(prefs.aiTemperature) { mutableFloatStateOf(prefs.aiTemperature) }
    var aiMaxTokens by remember(prefs.aiMaxTokens) { mutableStateOf(prefs.aiMaxTokens.toString()) }
    var aiSysPrompt by remember(prefs.aiSystemPrompt) { mutableStateOf(prefs.aiSystemPrompt) }
    val perAssistantModels = remember(prefs.assistantModels) {
        mutableStateMapOf<String, String>().apply { putAll(prefs.assistantModels) }
    }

    var pinDraft by remember(prefs.appLockPin) { mutableStateOf(prefs.appLockPin) }
    var customPrimary by remember(prefs.customPrimaryHex) { mutableLongStateOf(prefs.customPrimaryHex) }
    var customSecondary by remember(prefs.customSecondaryHex) { mutableLongStateOf(prefs.customSecondaryHex) }
    var customDark by remember(prefs.customDarkBase) { mutableStateOf(prefs.customDarkBase) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onExportBackup(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onImportBackup(uri)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Language & Calendar
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.settingsLanguage, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = prefs.language == AppLanguage.FA,
                            onClick = { onSetLanguage(AppLanguage.FA) },
                            label = { Text("فارسی (RTL)") },
                            modifier = Modifier.testTag("settings_lang_fa")
                        )
                        FilterChip(
                            selected = prefs.language == AppLanguage.EN,
                            onClick = { onSetLanguage(AppLanguage.EN) },
                            label = { Text("English (LTR)") },
                            modifier = Modifier.testTag("settings_lang_en")
                        )
                    }

                    Text(strings.settingsCalendarType, style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = prefs.calendarSystem == CalendarSystem.JALALI,
                            onClick = { onSetCalendar(CalendarSystem.JALALI) },
                            label = { Text(strings.settingsCalendarJalali) }
                        )
                        FilterChip(
                            selected = prefs.calendarSystem == CalendarSystem.GREGORIAN,
                            onClick = { onSetCalendar(CalendarSystem.GREGORIAN) },
                            label = { Text(strings.settingsCalendarGregorian) }
                        )
                    }
                }
            }
        }

        // 8 Themes + Custom Theme Builder
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.settingsTheme, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlanifyThemeType.entries.forEach { theme ->
                            val swatch = buildPalette(theme, customPrimary, customSecondary, customDark).previewSwatch
                            val selected = prefs.themeType == theme
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .clickable { onSetTheme(theme) }
                                    .testTag("settings_theme_${theme.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    swatch.forEach { c ->
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .border(0.5.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                                        )
                                    }
                                    Text(themeDisplayName(theme, strings), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    // Custom Theme Studio
                    Text(strings.settingsCustomTheme, style = MaterialTheme.typography.titleSmall)
                    val colorChoices = listOf(0xFF168BFFL, 0xFF0EA5E9L, 0xFF10B981L, 0xFFF59E0BL, 0xFFEC4899L, 0xFFEF4444L)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.settingsCustomPrimary, style = MaterialTheme.typography.labelSmall)
                        colorChoices.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(if (customPrimary == hex) 3.dp else 1.dp, Color.White, CircleShape)
                                    .clickable {
                                        customPrimary = hex
                                        onSetCustomTheme(customPrimary, customSecondary, customDark)
                                    }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.settingsCustomSecondary, style = MaterialTheme.typography.labelSmall)
                        colorChoices.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(if (customSecondary == hex) 3.dp else 1.dp, Color.White, CircleShape)
                                    .clickable {
                                        customSecondary = hex
                                        onSetCustomTheme(customPrimary, customSecondary, customDark)
                                    }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.settingsCustomDarkMode, style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = customDark,
                            onCheckedChange = {
                                customDark = it
                                onSetCustomTheme(customPrimary, customSecondary, customDark)
                            }
                        )
                    }

                    // Font Size + Notifications
                    Text(
                        text = "${strings.settingsFontSize}: ${(prefs.fontScale * 100).roundToInt()}%",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Slider(
                        value = prefs.fontScale,
                        onValueChange = onSetFontScale,
                        valueRange = 0.85f..1.25f,
                        steps = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.settingsNotifications, style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = prefs.notificationsEnabled, onCheckedChange = onSetNotifications)
                    }
                }
            }
        }

        // Local Profile
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.settingsProfile, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text(strings.onboardingNameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dailyGoal,
                        onValueChange = { dailyGoal = it.filter(Char::isDigit).take(3) },
                        label = { Text(strings.settingsDailyGoal) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = { onUpdateProfile(profileName, dailyGoal.toIntOrNull() ?: 90) }) {
                        Text(strings.save)
                    }
                }
            }
        }

        // OpenRouter AI Configuration
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.settingsAiConfig, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text(strings.settingsApiKey) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("settings_openrouter_key_input")
                    )
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text(strings.settingsBaseUrl) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = defaultModel,
                        onValueChange = { defaultModel = it },
                        label = { Text(strings.settingsDefaultModel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(strings.settingsAssistantModels, style = MaterialTheme.typography.titleSmall)
                    AssistantPersona.entries.forEach { p ->
                        OutlinedTextField(
                            value = perAssistantModels[p.id].orEmpty(),
                            onValueChange = { perAssistantModels[p.id] = it },
                            label = { Text(p.label(strings)) },
                            placeholder = { Text(defaultModel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text("${strings.settingsTemperature}: ${String.format("%.1f", aiTemp)}", style = MaterialTheme.typography.labelLarge)
                    Slider(value = aiTemp, onValueChange = { aiTemp = it }, valueRange = 0f..1.5f)

                    OutlinedTextField(
                        value = aiMaxTokens,
                        onValueChange = { aiMaxTokens = it.filter(Char::isDigit).take(4) },
                        label = { Text(strings.settingsMaxTokens) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = aiSysPrompt,
                        onValueChange = { aiSysPrompt = it },
                        label = { Text(strings.settingsSystemPrompt) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                onSaveAiConfig(
                                    apiKey,
                                    baseUrl,
                                    defaultModel,
                                    perAssistantModels.toMap(),
                                    aiTemp,
                                    aiMaxTokens.toIntOrNull() ?: 1500,
                                    aiSysPrompt
                                )
                            },
                            modifier = Modifier.testTag("settings_save_ai_btn")
                        ) {
                            Text(strings.save)
                        }
                        OutlinedButton(
                            onClick = { onTestConnection(apiKey, baseUrl) },
                            modifier = Modifier.testTag("settings_test_ai_btn")
                        ) {
                            Text(strings.settingsTestConnection)
                        }
                    }

                    if (!connectionTestStatus.isNullOrBlank()) {
                        val msg = when {
                            connectionTestStatus == "..." -> strings.loading
                            connectionTestStatus.startsWith("OK:") -> "${strings.settingsConnectionSuccess} (${connectionTestStatus.removePrefix("OK:")} models)"
                            else -> "${strings.settingsConnectionFailed}: ${connectionTestStatus.removePrefix("ERR:")}"
                        }
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (connectionTestStatus.startsWith("OK")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Privacy PIN & Data Export / Import
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.settingsPrivacyPin, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = pinDraft,
                            onValueChange = { pinDraft = it.filter(Char::isDigit).take(4) },
                            label = { Text(strings.settingsPinHint) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { onSetAppLockPin(pinDraft) }) {
                            Text(strings.save)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(strings.settingsDataMgmt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { exportLauncher.launch("planify-backup.json") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.settingsExportData)
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.settingsImportData)
                        }
                    }
                    if (!backupStatus.isNullOrBlank()) {
                        Text(
                            text = backupStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // About PLANIFY
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Color.White) {
                        Image(
                            painter = painterResource(id = R.drawable.planify_logo_banner),
                            contentDescription = strings.appName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().height(72.dp).padding(10.dp)
                        )
                    }
                    Text(strings.settingsAbout, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(strings.settingsAboutDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(strings.offlineLocalBadge, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
