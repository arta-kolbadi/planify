package com.example.ui.screens

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.AppStrings
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.local.AppPreferencesState
import com.example.ui.components.LiquidGlassHeroCard
import com.example.ui.components.PlanifyAmbientBackdrop
import com.example.ui.components.PlanifyCard
import com.example.ui.theme.PlanifyThemeType
import com.example.ui.theme.buildPalette
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    prefs: AppPreferencesState,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectTheme: (PlanifyThemeType) -> Unit,
    onComplete: (name: String, dailyGoalMinutes: Int) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    var name by remember { mutableStateOf("") }
    var goalMinutes by remember { mutableFloatStateOf(90f) }

    PlanifyAmbientBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LiquidGlassHeroCard(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.planify_logo_banner),
                            contentDescription = strings.appName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(104.dp)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                Text(
                    text = strings.onboardingTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = strings.tagline,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = strings.onboardingSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Language Picker Card
                PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.onboardingSelectLanguage, style = MaterialTheme.typography.titleMedium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AppLanguage.entries.forEach { option ->
                                val selected = prefs.language == option
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectLanguage(option) },
                                    label = {
                                        Text(
                                            text = if (option == AppLanguage.FA) "فارسی (شمسی • RTL)" else "English (Gregorian • LTR)",
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("onboarding_lang_${option.name.lowercase()}")
                                )
                            }
                        }
                    }
                }

                // 8-Theme Live Selector Card
                PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.onboardingSelectTheme, style = MaterialTheme.typography.titleMedium)
                        }
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PlanifyThemeType.entries.forEach { theme ->
                                val swatch = buildPalette(
                                    theme,
                                    prefs.customPrimaryHex,
                                    prefs.customSecondaryHex,
                                    prefs.customDarkBase
                                ).previewSwatch
                                val isSelected = prefs.themeType == theme
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .clickable { onSelectTheme(theme) }
                                        .testTag("onboarding_theme_${theme.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                            swatch.forEach { c ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(c)
                                                        .border(0.5.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                                                )
                                            }
                                        }
                                        Text(
                                            text = themeDisplayName(theme, strings),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Name & Daily Goal
                PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(strings.onboardingNameLabel) },
                            placeholder = { Text(strings.onboardingNamePlaceholder) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
                        )
                        val goalInt = goalMinutes.roundToInt()
                        Text(
                            text = "${strings.onboardingDailyGoalLabel}: ${JalaliCalendar.formatNumber(goalInt, lang)} ${strings.minutesShort}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Slider(
                            value = goalMinutes,
                            onValueChange = { goalMinutes = it },
                            valueRange = 30f..240f,
                            steps = 13
                        )
                    }
                }

                Button(
                    onClick = { onComplete(name.ifBlank { if (lang == AppLanguage.FA) "کاربر پلنیفای" else "Scholar" }, goalMinutes.roundToInt()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_start_button")
                ) {
                    Text(strings.onboardingGetStarted, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
}

fun themeDisplayName(theme: PlanifyThemeType, strings: AppStrings): String = when (theme) {
    PlanifyThemeType.LIGHT -> strings.themeLight
    PlanifyThemeType.DARK -> strings.themeDark
    PlanifyThemeType.AMOLED -> strings.themeAmoled
    PlanifyThemeType.OCEAN_BLUE -> strings.themeOceanBlue
    PlanifyThemeType.COZY_GREEN -> strings.themeCozyGreen
    PlanifyThemeType.SAKURA_PINK -> strings.themeSakuraPink
    PlanifyThemeType.SUNSET_ORANGE -> strings.themeSunsetOrange
    PlanifyThemeType.CUSTOM -> strings.themeCustom
}
