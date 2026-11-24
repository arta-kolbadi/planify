package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalCalendarSystem
import com.example.core.i18n.LocalStrings
import com.example.data.local.FocusSessionEntity
import com.example.data.local.TaskEntity
import com.example.data.local.UserProfileEntity
import com.example.focus.FocusTimerUiState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LiquidGlassHeroCard
import com.example.ui.components.MascotCanvas
import com.example.ui.components.MascotMood
import com.example.ui.components.PlanifyCard
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.liquidGlassBorder
import com.example.ui.components.liquidGlassContainer
import com.example.ui.navigation.PlanifyRoute
import com.example.ui.theme.LocalPlanifyColors
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    profile: UserProfileEntity?,
    tasks: List<TaskEntity>,
    sessions: List<FocusSessionEntity>,
    focusState: FocusTimerUiState,
    onToggleTask: (TaskEntity) -> Unit,
    onNavigate: (PlanifyRoute) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val calSystem = LocalCalendarSystem.current
    val extra = LocalPlanifyColors.current

    var currentMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentMillis = System.currentTimeMillis()
            delay(30_000L)
        }
    }

    val cal = remember(currentMillis) { Calendar.getInstance().apply { timeInMillis = currentMillis } }
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val greeting = when {
        hour < 12 -> strings.dashWelcomeMorning
        hour < 18 -> strings.dashWelcomeAfternoon
        else -> strings.dashWelcomeEvening
    }
    val todayEpoch = JalaliCalendar.todayEpochDay()
    val todayTasks = remember(tasks, todayEpoch) { tasks.filter { it.dateEpochDay == todayEpoch } }
    val todayDoneCount = todayTasks.count { it.status == "DONE" }
    val todayFocusMin = remember(sessions, todayEpoch) {
        sessions.filter { it.dateEpochDay == todayEpoch }.sumOf { it.actualSeconds / 60 }
    }
    val completedSessionsCount = remember(sessions) { sessions.count { it.completed } }
    val dailyGoalMin = profile?.dailyFocusGoalMinutes ?: 90
    val goalProgress = (todayFocusMin.toFloat() / dailyGoalMin.coerceAtLeast(1)).coerceIn(0f, 1f)

    // Interactive Mascot state
    var manualMoodOffset by remember { mutableIntStateOf(-1) }
    val computedMood = when {
        manualMoodOffset >= 0 -> MascotMood.entries[manualMoodOffset % MascotMood.entries.size]
        focusState.isRunning -> MascotMood.STUDYING
        todayDoneCount > 0 || todayFocusMin >= 25 -> MascotMood.CELEBRATING
        hour >= 22 || hour < 6 -> MascotMood.SLEEPING
        todayTasks.isNotEmpty() -> MascotMood.HAPPY
        else -> MascotMood.IDLE
    }

    val weeklyData = remember(sessions, todayEpoch, lang) {
        (6 downTo 0).map { offset ->
            val day = todayEpoch - offset
            val mins = sessions.filter { it.dateEpochDay == day }.sumOf { it.actualSeconds / 60.0 }.toFloat()
            val label = JalaliCalendar.weekdayName(day, lang).take(3)
            label to mins
        }
    }

    val xp = profile?.xp ?: 0
    val level = profile?.level ?: 1
    val xpIntoLevel = xp % 150
    val xpProgress = (xpIntoLevel / 150f).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Brand Header + Greeting + Live Clock & Date
        item {
            LiquidGlassHeroCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.planify_mark),
                                contentDescription = strings.appName,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$greeting${if (!profile?.displayName.isNullOrBlank()) ", ${profile?.displayName}" else ""}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${JalaliCalendar.weekdayName(todayEpoch, lang)} • ${JalaliCalendar.formatDate(todayEpoch, lang, calSystem)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = JalaliCalendar.formatTime(hour * 60 + minute, lang),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        AssistChip(
                            onClick = { onNavigate(PlanifyRoute.Achievements) },
                            label = {
                                Text("${strings.streakLabel}: ${JalaliCalendar.formatNumber(profile?.streakDays ?: 0, lang)} ${strings.daysLabel}")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = extra.warning,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        Text(
                            text = "${strings.levelLabel} ${JalaliCalendar.formatNumber(level, lang)} • ${JalaliCalendar.formatNumber(profile?.coins ?: 0, lang)} ${strings.coinsLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2D Mascot Interactive Card + XP Bar
        item {
            LiquidGlassHeroCard(
                modifier = Modifier.fillMaxWidth().testTag("dashboard_mascot_card"),
                shape = RoundedCornerShape(24.dp),
                onClick = { manualMoodOffset = (manualMoodOffset + 1) % MascotMood.entries.size }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MascotCanvas(
                        mood = computedMood,
                        equippedOutfit = profile?.equippedOutfit ?: "classic_blue",
                        equippedAccessory = profile?.equippedAccessory ?: "none",
                        equippedRoom = profile?.equippedRoom ?: "minimal_desk",
                        size = 104.dp,
                        contentDescriptionText = strings.dashMascotTitle
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(strings.dashMascotTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = computedMood.label(strings),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        LinearProgressIndicator(
                            progress = { xpProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${JalaliCalendar.formatNumber(xp, lang)} ${strings.xpLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { onNavigate(PlanifyRoute.Achievements) }) {
                                Text(strings.mascotWardrobe)
                            }
                        }
                    }
                }
            }
        }

        // Key Metrics Row (Focus Today, Completed Sessions, Daily Goal)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PlanifyCard(modifier = Modifier.weight(1f), onClick = { onNavigate(PlanifyRoute.Focus) }) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "${JalaliCalendar.formatNumber(todayFocusMin, lang)} ${strings.minutesShort}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(strings.dashFocusToday, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                PlanifyCard(modifier = Modifier.weight(1f), onClick = { onNavigate(PlanifyRoute.Analytics) }) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = extra.success)
                        Text(
                            text = JalaliCalendar.formatNumber(completedSessionsCount, lang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(strings.dashSessionsCompleted, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                PlanifyCard(modifier = Modifier.weight(1f), onClick = { onNavigate(PlanifyRoute.Planner) }) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "${JalaliCalendar.formatNumber(todayDoneCount, lang)}/${JalaliCalendar.formatNumber(todayTasks.size, lang)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(strings.dashTodayTasks, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Daily Goals & Quick Action Buttons
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(strings.dashDailyGoals, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${JalaliCalendar.formatNumber(todayFocusMin, lang)} / ${JalaliCalendar.formatNumber(dailyGoalMin, lang)} ${strings.minutesShort}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(PlanifyRoute.Focus) },
                            modifier = Modifier.weight(1f).testTag("dash_start_focus_btn")
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.dashStartFocus)
                        }
                        FilledTonalButton(
                            onClick = { onNavigate(PlanifyRoute.Planner) },
                            modifier = Modifier.weight(1f).testTag("dash_add_task_btn")
                        ) {
                            Icon(Icons.Rounded.AddTask, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.dashAddTask)
                        }
                    }
                }
            }
        }

        // Today's Tasks & Study Schedule
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.dashStudySchedule, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { onNavigate(PlanifyRoute.Planner) }) {
                    Text(strings.navPlanner)
                }
            }
            if (todayTasks.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Rounded.AddTask,
                    title = strings.dashNoTasksToday
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayTasks.take(5).forEach { task ->
                        val isDone = task.status == "DONE"
                        PlanifyCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onToggleTask(task) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = task.title,
                                    tint = if (isDone) extra.success else MaterialTheme.colorScheme.primary
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = "${JalaliCalendar.formatTime(task.startTimeMinutes, lang)} - ${JalaliCalendar.formatTime(task.endTimeMinutes, lang)} • ${task.categoryName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Weekly Progress Chart
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.dashWeeklyProgress, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    SimpleBarChart(
                        values = weeklyData,
                        barColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Quick Module Shortcuts (reaches all 10 modules seamlessly)
        item {
            Text(strings.dashQuickExplore, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val shortcuts = listOf(
                Triple(PlanifyRoute.Academy, strings.navAcademy, Icons.Rounded.School),
                Triple(PlanifyRoute.Notes, strings.navNotes, Icons.Rounded.EditNote),
                Triple(PlanifyRoute.StudyRoom, strings.navStudyRoom, Icons.Rounded.Groups),
                Triple(PlanifyRoute.Analytics, strings.navAnalytics, Icons.Rounded.BarChart),
                Triple(PlanifyRoute.Achievements, strings.navAchievements, Icons.Rounded.EmojiEvents),
                Triple(PlanifyRoute.AiAssistant, strings.navAi, Icons.Rounded.AutoAwesome),
                Triple(PlanifyRoute.Settings, strings.navSettings, Icons.Rounded.Settings)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                shortcuts.forEach { (route, label, icon) ->
                    QuickModuleTile(
                        label = label,
                        icon = icon,
                        onClick = { onNavigate(route) }
                    )
                }
            }
        }

        // Recent Activity (Real Focus Sessions)
        item {
            Text(strings.dashRecentActivity, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            if (sessions.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Rounded.Timer,
                    title = strings.dashNoActivityYet
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sessions.take(4).forEach { s ->
                        PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.taskTitle.ifBlank { s.mode },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = JalaliCalendar.formatDate(s.dateEpochDay, lang, calSystem),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${JalaliCalendar.formatNumber((s.actualSeconds / 60).coerceAtLeast(1), lang)} ${strings.minutesShort}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickModuleTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val tileShape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier
            .liquidGlassContainer(shape = tileShape, elevation = 4.dp, heroAccent = false)
            .clickable { onClick() },
        shape = tileShape,
        color = Color.Transparent,
        border = liquidGlassBorder(heroAccent = false)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                modifier = Modifier.size(30.dp)
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                }
            }
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
