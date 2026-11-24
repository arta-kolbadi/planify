package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.CalendarSystem
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalCalendarSystem
import com.example.core.i18n.LocalStrings

@Composable
fun BilingualDatePickerDialog(
    initialEpochDay: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val calSystem = LocalCalendarSystem.current

    val initialPair = remember(initialEpochDay, calSystem) {
        if (calSystem == CalendarSystem.JALALI) {
            val j = JalaliCalendar.jalaliFromEpochDay(initialEpochDay)
            Triple(j.year, j.month, j.day)
        } else {
            JalaliCalendar.gregorianFromEpochDay(initialEpochDay)
        }
    }

    var displayYear by remember { mutableIntStateOf(initialPair.first) }
    var displayMonth by remember { mutableIntStateOf(initialPair.second) }

    val daysInMonth = if (calSystem == CalendarSystem.JALALI) {
        JalaliCalendar.jalaliMonthLength(displayYear, displayMonth)
    } else {
        JalaliCalendar.gregorianMonthLength(displayYear, displayMonth)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (displayMonth == 1) {
                        displayMonth = 12
                        displayYear -= 1
                    } else {
                        displayMonth -= 1
                    }
                }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = strings.back)
                }
                Text(
                    text = JalaliCalendar.formatMonthYear(displayYear, displayMonth, lang, calSystem),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    if (displayMonth == 12) {
                        displayMonth = 1
                        displayYear += 1
                    } else {
                        displayMonth += 1
                    }
                }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = strings.add)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val headers = if (lang == AppLanguage.FA) {
                    listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                } else {
                    listOf("S", "M", "T", "W", "T", "F", "S")
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    headers.forEach { h ->
                        Text(
                            text = h,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val rows = (1..daysInMonth).chunked(7)
                rows.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (idx in 0 until 7) {
                            val dayNum = week.getOrNull(idx)
                            if (dayNum == null) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else {
                                val epoch = if (calSystem == CalendarSystem.JALALI) {
                                    JalaliCalendar.epochDayFromJalali(displayYear, displayMonth, dayNum)
                                } else {
                                    JalaliCalendar.epochDayFromGregorian(displayYear, displayMonth, dayNum)
                                }
                                val isSelected = epoch == initialEpochDay
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            onDateSelected(epoch)
                                            onDismiss()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = JalaliCalendar.formatNumber(dayNum, lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(JalaliCalendar.todayEpochDay())
                onDismiss()
            }) {
                Text(strings.plannerTodayBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
