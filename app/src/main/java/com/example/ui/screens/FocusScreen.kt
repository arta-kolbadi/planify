package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalCalendarSystem
import com.example.core.i18n.LocalStrings
import com.example.data.local.BlockedAppEntity
import com.example.data.local.FocusSessionEntity
import com.example.data.local.TaskEntity
import com.example.data.local.UserProfileEntity
import com.example.focus.AmbientSound
import com.example.focus.FocusMode
import com.example.focus.FocusTimerUiState
import com.example.focus.InstalledAppInfo
import com.example.focus.UsageAccessHelper
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LiquidGlassHeroCard
import com.example.ui.components.PlanifyCard
import com.example.ui.theme.LocalPlanifyColors
import kotlin.math.roundToInt

@Composable
fun FocusScreen(
    focusState: FocusTimerUiState,
    profile: UserProfileEntity?,
    tasks: List<TaskEntity>,
    sessions: List<FocusSessionEntity>,
    blockedApps: List<BlockedAppEntity>,
    installedApps: List<InstalledAppInfo>,
    onSelectMode: (FocusMode) -> Unit,
    onSetMinutes: (Int) -> Unit,
    onSelectTask: (Long?, String, String) -> Unit,
    onSetAmbientSound: (AmbientSound) -> Unit,
    onSetAppBlockEnabled: (Boolean) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEndSession: (Boolean) -> Unit,
    onRefreshInstalledApps: () -> Unit,
    onToggleBlockedApp: (String, String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val calSystem = LocalCalendarSystem.current
    val extra = LocalPlanifyColors.current

    var hasUsageAccess by remember { mutableStateOf(UsageAccessHelper.hasUsageStatsPermission(context)) }
    var showUsageExplainer by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    var showTaskPicker by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        onStart()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageAccess = UsageAccessHelper.hasUsageStatsPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val todayEpoch = JalaliCalendar.todayEpochDay()
    val todayMinutes = remember(sessions, todayEpoch) {
        sessions.filter { it.dateEpochDay == todayEpoch }.sumOf { it.actualSeconds / 60 }
    }
    val dailyGoal = profile?.dailyFocusGoalMinutes ?: 90
    val goalProgress = (todayMinutes.toFloat() / dailyGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val displayTime = if (focusState.mode.isCountUp) {
        JalaliCalendar.formatCountdown(focusState.elapsedSeconds, lang)
    } else {
        JalaliCalendar.formatCountdown(focusState.remainingSeconds, lang)
    }

    val ringProgress = if (focusState.mode.isCountUp) {
        ((focusState.elapsedSeconds % 3600) / 3600f).coerceIn(0.05f, 1f)
    } else if (focusState.plannedSeconds > 0) {
        (focusState.remainingSeconds.toFloat() / focusState.plannedSeconds).coerceIn(0f, 1f)
    } else 1f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Focus Modes Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FocusMode.entries.forEach { mode ->
                    FilterChip(
                        selected = focusState.mode == mode,
                        enabled = !focusState.isActive,
                        onClick = { onSelectMode(mode) },
                        label = { Text(mode.label(strings)) },
                        modifier = Modifier.testTag("focus_mode_${mode.id}")
                    )
                }
            }
        }

        // Main Timer Ring Card (Liquid Glass Showcase)
        item {
            LiquidGlassHeroCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(214.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 14.dp.toPx()
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(primaryColor, secondaryColor, primaryColor)
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * ringProgress,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = displayTime,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = focusState.taskTitle.ifBlank { strings.focusNoTaskSelected },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (!focusState.isActive && !focusState.mode.isCountUp) {
                        val currentMins = (focusState.plannedSeconds / 60).coerceIn(5, 180)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${strings.focusDurationMin}: ${JalaliCalendar.formatNumber(currentMins, lang)} ${strings.minutesShort}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Slider(
                                value = currentMins.toFloat(),
                                onValueChange = { onSetMinutes(it.roundToInt()) },
                                valueRange = 5f..180f,
                                steps = 34
                            )
                        }
                    }

                    // Task Picker & Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showTaskPicker = true },
                            enabled = !focusState.isActive,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = focusState.taskTitle.ifBlank { strings.focusSelectTask },
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!focusState.isActive) {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onStart()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(50.dp).testTag("focus_start_button")
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.focusStart)
                            }
                        } else {
                            if (focusState.isRunning) {
                                OutlinedButton(
                                    onClick = onPause,
                                    modifier = Modifier.weight(1f).height(50.dp).testTag("focus_pause_button")
                                ) {
                                    Icon(Icons.Rounded.Pause, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.focusPause)
                                }
                            } else {
                                Button(
                                    onClick = onResume,
                                    modifier = Modifier.weight(1f).height(50.dp).testTag("focus_resume_button")
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.focusResume)
                                }
                            }
                            Button(
                                onClick = { onEndSession(true) },
                                modifier = Modifier.weight(1f).height(50.dp).testTag("focus_end_button")
                            ) {
                                Icon(Icons.Rounded.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.focusEndSession)
                            }
                        }
                    }
                }
            }
        }

        // Ambient Sound Selector
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Rounded.Headphones, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.focusAmbientSound, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AmbientSound.NONE to strings.focusSoundNone,
                            AmbientSound.RAIN to strings.focusSoundRain,
                            AmbientSound.BROWN_NOISE to strings.focusSoundBrownNoise,
                            AmbientSound.FOREST to strings.focusSoundForest
                        ).forEach { (sound, label) ->
                            FilterChip(
                                selected = focusState.ambientSound == sound,
                                onClick = { onSetAmbientSound(sound) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }

        // Android App Blocking (UsageStatsManager + Focus Shield)
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            androidx.compose.material3.Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(strings.focusAppBlockerTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(strings.focusAppBlockerDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = focusState.appBlockEnabled,
                            onCheckedChange = { wantEnable ->
                                if (wantEnable && !hasUsageAccess) {
                                    showUsageExplainer = true
                                } else {
                                    onSetAppBlockEnabled(wantEnable)
                                }
                            },
                            modifier = Modifier.testTag("focus_app_blocker_switch")
                        )
                    }

                    if (!hasUsageAccess) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = extra.warning.copy(alpha = 0.12f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(strings.focusUsagePermissionMissing, style = MaterialTheme.typography.bodySmall)
                                OutlinedButton(onClick = { showUsageExplainer = true }) {
                                    androidx.compose.material3.Icon(Icons.Rounded.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.focusGrantUsageAccess)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${JalaliCalendar.formatNumber(blockedApps.size, lang)} ${strings.focusBlockedCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedButton(
                            onClick = {
                                onRefreshInstalledApps()
                                showAppPicker = true
                            }
                        ) {
                            androidx.compose.material3.Icon(Icons.Rounded.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.focusSelectBlockedApps)
                        }
                    }

                    Text(
                        text = strings.focusShieldDisclaimer,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Daily Goal + History
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(strings.focusDailyGoal, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${JalaliCalendar.formatNumber(todayMinutes, lang)} / ${JalaliCalendar.formatNumber(dailyGoal, lang)} ${strings.minutesShort}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        item {
            Text(strings.focusHistory, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (sessions.isEmpty()) {
            item {
                EmptyStateCard(icon = Icons.Rounded.Timer, title = strings.analyticsNoData)
            }
        } else {
            items(sessions.take(15), key = { it.id }) { s ->
                PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.taskTitle.ifBlank { FocusMode.fromId(s.mode).label(strings) },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = JalaliCalendar.formatDate(s.dateEpochDay, lang, calSystem),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${JalaliCalendar.formatNumber((s.actualSeconds / 60).coerceAtLeast(1), lang)} ${strings.minutesShort}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showUsageExplainer) {
        AlertDialog(
            onDismissRequest = { showUsageExplainer = false },
            title = { Text(strings.focusAppBlockerTitle) },
            text = { Text(strings.focusUsagePermissionExplain) },
            confirmButton = {
                TextButton(onClick = {
                    showUsageExplainer = false
                    UsageAccessHelper.openUsageAccessSettings(context)
                }) {
                    Text(strings.focusGrantUsageAccess)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUsageExplainer = false }) { Text(strings.cancel) }
            }
        )
    }

    if (showTaskPicker) {
        AlertDialog(
            onDismissRequest = { showTaskPicker = false },
            title = { Text(strings.focusSelectTask) },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    item {
                        TextButton(
                            onClick = {
                                onSelectTask(null, "", "Study")
                                showTaskPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.focusNoTaskSelected)
                        }
                    }
                    items(tasks.filter { it.status != "DONE" }, key = { it.id }) { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTask(t.id, t.title, t.categoryName)
                                    showTaskPicker = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(t.title, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTaskPicker = false }) { Text(strings.close) }
            }
        )
    }

    if (showAppPicker) {
        val blockedSet = blockedApps.map { it.packageName }.toSet()
        AlertDialog(
            onDismissRequest = { showAppPicker = false },
            title = { Text(strings.focusSelectBlockedApps) },
            text = {
                if (installedApps.isEmpty()) {
                    Text(strings.loading)
                } else {
                    LazyColumn(modifier = Modifier.height(340.dp)) {
                        items(installedApps, key = { it.packageName }) { appInfo ->
                            val isChecked = blockedSet.contains(appInfo.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onToggleBlockedApp(appInfo.packageName, appInfo.label, !isChecked)
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        onToggleBlockedApp(appInfo.packageName, appInfo.label, checked)
                                    }
                                )
                                Column {
                                    Text(appInfo.label, style = MaterialTheme.typography.bodyMedium)
                                    Text(appInfo.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppPicker = false }) { Text(strings.confirm) }
            }
        )
    }
}
