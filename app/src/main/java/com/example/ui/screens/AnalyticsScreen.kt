package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.local.FocusSessionEntity
import com.example.data.local.TaskEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.CategoryDistributionBar
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LiquidGlassHeroCard
import com.example.ui.components.PlanifyCard
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.SimpleLineTrendChart
import com.example.ui.theme.LocalPlanifyColors

@Composable
fun AnalyticsScreen(
    profile: UserProfileEntity?,
    tasks: List<TaskEntity>,
    sessions: List<FocusSessionEntity>
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val extra = LocalPlanifyColors.current

    var filterIndex by remember { mutableIntStateOf(1) } // 0=Daily(1d), 1=Weekly(7d), 2=Monthly(30d)
    val daysWindow = when (filterIndex) {
        0 -> 1
        1 -> 7
        else -> 30
    }
    val today = JalaliCalendar.todayEpochDay()
    val startDay = today - (daysWindow - 1)

    val filteredSessions = remember(sessions, startDay) {
        sessions.filter { it.dateEpochDay in startDay..today }
    }
    val filteredTasks = remember(tasks, startDay) {
        tasks.filter { it.dateEpochDay in startDay..today }
    }

    val totalFocusMinutes = filteredSessions.sumOf { it.actualSeconds / 60 }
    val completedTasks = filteredTasks.count { it.status == "DONE" }
    val totalTasks = filteredTasks.size
    val goalTarget = (profile?.dailyFocusGoalMinutes ?: 90) * daysWindow
    val goalCompletionRatio = (totalFocusMinutes.toFloat() / goalTarget.coerceAtLeast(1)).coerceIn(0f, 1f)

    val barData = remember(filteredSessions, filterIndex, today, lang) {
        when (filterIndex) {
            0 -> listOf(
                JalaliCalendar.weekdayName(today, lang).take(3) to totalFocusMinutes.toFloat()
            )
            1 -> (6 downTo 0).map { offset ->
                val d = today - offset
                val mins = filteredSessions.filter { it.dateEpochDay == d }.sumOf { it.actualSeconds / 60.0 }.toFloat()
                JalaliCalendar.weekdayName(d, lang).take(3) to mins
            }
            else -> (3 downTo 0).map { weekIdx ->
                val wEnd = today - weekIdx * 7L
                val wStart = wEnd - 6L
                val mins = filteredSessions.filter { it.dateEpochDay in wStart..wEnd }.sumOf { it.actualSeconds / 60.0 }.toFloat()
                "W${4 - weekIdx}" to mins
            }
        }
    }

    val trendPoints = remember(sessions, today) {
        (6 downTo 0).map { offset ->
            val d = today - offset
            val taskScore = tasks.count { it.dateEpochDay == d && it.status == "DONE" } * 15f
            val focusScore = sessions.filter { it.dateEpochDay == d }.sumOf { it.actualSeconds / 60.0 }.toFloat()
            taskScore + focusScore
        }
    }

    val categorySlices = remember(filteredTasks, filteredSessions) {
        val palette = listOf(
            Color(0xFF168BFF),
            Color(0xFF20D4E8),
            Color(0xFF20B879),
            Color(0xFFF5A623),
            Color(0xFFEF5350)
        )
        val counts = mutableMapOf<String, Float>()
        filteredTasks.forEach { t -> counts[t.categoryName] = (counts[t.categoryName] ?: 0f) + 1f }
        filteredSessions.forEach { s -> counts[s.categoryName] = (counts[s.categoryName] ?: 0f) + (s.actualSeconds / 60f).coerceAtLeast(1f) }
        counts.entries.sortedByDescending { it.value }.mapIndexed { idx, entry ->
            Triple(entry.key, entry.value, palette[idx % palette.size])
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    0 to strings.analyticsFilterDaily,
                    1 to strings.analyticsFilterWeekly,
                    2 to strings.analyticsFilterMonthly
                ).forEach { (idx, label) ->
                    FilterChip(
                        selected = filterIndex == idx,
                        onClick = { filterIndex = idx },
                        label = { Text(label) }
                    )
                }
            }
        }

        // KPI Summary Row (Liquid Glass)
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidGlassHeroCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(strings.analyticsFocusTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${JalaliCalendar.formatNumber(totalFocusMinutes, lang)} ${strings.minutesShort}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                LiquidGlassHeroCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(strings.analyticsCompletedTasks, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${JalaliCalendar.formatNumber(completedTasks, lang)} / ${JalaliCalendar.formatNumber(totalTasks, lang)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = extra.success
                        )
                    }
                }
            }
        }

        if (filteredSessions.isEmpty() && filteredTasks.isEmpty()) {
            item {
                EmptyStateCard(icon = Icons.Rounded.BarChart, title = strings.analyticsNoData)
            }
        } else {
            item {
                PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(strings.analyticsFocusTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        SimpleBarChart(values = barData, barColor = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(strings.analyticsTrends, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        SimpleLineTrendChart(points = trendPoints, lineColor = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            if (categorySlices.isNotEmpty()) {
                item {
                    PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(strings.analyticsSubjectDist, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            CategoryDistributionBar(slices = categorySlices)
                        }
                    }
                }
            }
        }

        // Goal Completion + 14-day Consistency Grid
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(strings.analyticsGoalCompletion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${JalaliCalendar.formatNumber((goalCompletionRatio * 100).toInt(), lang)}%",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { goalCompletionRatio },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(strings.analyticsConsistency, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (13 downTo 0).forEach { offset ->
                            val d = today - offset
                            val mins = sessions.filter { it.dateEpochDay == d }.sumOf { it.actualSeconds / 60 }
                            val done = tasks.count { it.dateEpochDay == d && it.status == "DONE" }
                            val intensity = ((mins / 30f) + (done * 0.4f)).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (intensity == 0f) MaterialTheme.colorScheme.surfaceContainerHigh
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f + 0.75f * intensity)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
