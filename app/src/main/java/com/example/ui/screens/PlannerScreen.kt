package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.CalendarSystem
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalCalendarSystem
import com.example.core.i18n.LocalStrings
import com.example.data.local.CategoryEntity
import com.example.data.local.SubtaskEntity
import com.example.data.local.TaskEntity
import com.example.ui.components.BilingualDatePickerDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PlanifyCard
import com.example.ui.theme.LocalPlanifyColors

@Composable
fun PlannerScreen(
    selectedDateEpochDay: Long,
    tasks: List<TaskEntity>,
    subtasks: List<SubtaskEntity>,
    categories: List<CategoryEntity>,
    onSelectDate: (Long) -> Unit,
    onSaveTask: (TaskEntity, List<String>) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onToggleSubtask: (SubtaskEntity) -> Unit,
    onMoveTaskOrder: (TaskEntity, List<TaskEntity>, Boolean) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onStartFocusForTask: (TaskEntity) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val calSystem = LocalCalendarSystem.current
    val extra = LocalPlanifyColors.current

    var viewTab by remember { mutableIntStateOf(0) } // 0=Daily, 1=Weekly, 2=Monthly
    var showDatePicker by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }

    val dayTasks = remember(tasks, selectedDateEpochDay) {
        tasks.filter { it.dateEpochDay == selectedDateEpochDay }
            .sortedWith(compareBy({ it.sortOrder }, { it.startTimeMinutes }))
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    showTaskDialog = true
                },
                modifier = Modifier.testTag("planner_add_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.plannerNewTask)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = viewTab,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
            ) {
                Tab(selected = viewTab == 0, onClick = { viewTab = 0 }, text = { Text(strings.plannerDaily) })
                Tab(selected = viewTab == 1, onClick = { viewTab = 1 }, text = { Text(strings.plannerWeekly) })
                Tab(selected = viewTab == 2, onClick = { viewTab = 2 }, text = { Text(strings.plannerMonthly) })
            }

            // Date Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onSelectDate(selectedDateEpochDay - 1) }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = strings.back)
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = strings.plannerCalendar, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${JalaliCalendar.weekdayName(selectedDateEpochDay, lang)} • ${JalaliCalendar.formatDate(selectedDateEpochDay, lang, calSystem)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { onSelectDate(selectedDateEpochDay + 1) }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = strings.add)
                }
            }

            if (viewTab == 1) {
                // Weekly 7-day strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (-3L..3L).forEach { delta ->
                        val d = selectedDateEpochDay + delta
                        val count = tasks.count { it.dateEpochDay == d }
                        val selected = d == selectedDateEpochDay
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.clickable { onSelectDate(d) }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = JalaliCalendar.weekdayName(d, lang).take(3),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = JalaliCalendar.formatShortDate(d, lang, calSystem).takeLast(2),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${JalaliCalendar.formatNumber(count, lang)}•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else if (viewTab == 2) {
                // Monthly Jalali / Gregorian Grid
                MonthlyCalendarCard(
                    selectedDateEpochDay = selectedDateEpochDay,
                    tasks = tasks,
                    onSelectDate = {
                        onSelectDate(it)
                        viewTab = 0
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (dayTasks.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.CalendarMonth,
                            title = strings.plannerEmptyDay
                        )
                    }
                } else {
                    items(dayTasks, key = { it.id }) { task ->
                        val taskSubs = subtasks.filter { it.taskId == task.id }
                        val isDone = task.status == "DONE"
                        val priorityColor = when (task.priority) {
                            "HIGH" -> extra.danger
                            "LOW" -> extra.success
                            else -> extra.warning
                        }

                        PlanifyCard(modifier = Modifier.fillMaxWidth().testTag("task_card_${task.id}")) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { onToggleTask(task) }) {
                                        Icon(
                                            imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                            contentDescription = task.title,
                                            tint = if (isDone) extra.success else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                        if (task.description.isNotBlank()) {
                                            Text(
                                                text = task.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(priorityColor))
                                            Text(
                                                text = "${JalaliCalendar.formatTime(task.startTimeMinutes, lang)}–${JalaliCalendar.formatTime(task.endTimeMinutes, lang)} • ${task.categoryName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (task.recurrenceRule != "NONE") {
                                                Icon(Icons.Rounded.EventRepeat, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                    IconButton(onClick = { onStartFocusForTask(task) }) {
                                        Icon(Icons.Rounded.PlayArrow, contentDescription = strings.plannerStartFocusForTask, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                if (task.studyGoal.isNotBlank()) {
                                    Text(
                                        text = "${strings.plannerStudyGoal}: ${task.studyGoal}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (taskSubs.isNotEmpty()) {
                                    Column(modifier = Modifier.padding(start = 12.dp)) {
                                        taskSubs.forEach { sub ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { onToggleSubtask(sub) }
                                            ) {
                                                Checkbox(checked = sub.isDone, onCheckedChange = { onToggleSubtask(sub) })
                                                Text(
                                                    text = sub.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    textDecoration = if (sub.isDone) TextDecoration.LineThrough else TextDecoration.None
                                                )
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
                                        text = "${strings.plannerEstDuration}: ${JalaliCalendar.formatNumber(task.estimatedMinutes, lang)}m • ${strings.plannerActualDuration}: ${JalaliCalendar.formatNumber(task.actualMinutes, lang)}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row {
                                        IconButton(onClick = { onMoveTaskOrder(task, dayTasks, true) }) {
                                            Icon(Icons.Rounded.ArrowUpward, contentDescription = strings.plannerMoveUp, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { onMoveTaskOrder(task, dayTasks, false) }) {
                                            Icon(Icons.Rounded.ArrowDownward, contentDescription = strings.plannerMoveDown, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = {
                                            editingTask = task
                                            showTaskDialog = true
                                        }) {
                                            Icon(Icons.Rounded.Edit, contentDescription = strings.edit, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { onDeleteTask(task) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = extra.danger, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        BilingualDatePickerDialog(
            initialEpochDay = selectedDateEpochDay,
            onDismiss = { showDatePicker = false },
            onDateSelected = onSelectDate
        )
    }

    if (showTaskDialog) {
        TaskEditorDialog(
            existing = editingTask,
            existingSubtasks = editingTask?.let { t -> subtasks.filter { it.taskId == t.id }.map { it.title } } ?: emptyList(),
            defaultEpochDay = selectedDateEpochDay,
            categories = categories,
            onDismiss = { showTaskDialog = false },
            onSave = { task, subs ->
                onSaveTask(task, subs)
                showTaskDialog = false
            }
        )
    }
}

@Composable
private fun MonthlyCalendarCard(
    selectedDateEpochDay: Long,
    tasks: List<TaskEntity>,
    onSelectDate: (Long) -> Unit
) {
    val lang = LocalAppLanguage.current
    val calSystem = LocalCalendarSystem.current
    val (y, m, _) = if (calSystem == CalendarSystem.JALALI) {
        val j = JalaliCalendar.jalaliFromEpochDay(selectedDateEpochDay)
        Triple(j.year, j.month, j.day)
    } else {
        JalaliCalendar.gregorianFromEpochDay(selectedDateEpochDay)
    }
    val daysCount = if (calSystem == CalendarSystem.JALALI) {
        JalaliCalendar.jalaliMonthLength(y, m)
    } else {
        JalaliCalendar.gregorianMonthLength(y, m)
    }

    PlanifyCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = JalaliCalendar.formatMonthYear(y, m, lang, calSystem),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            (1..daysCount).chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (i in 0 until 7) {
                        val dNum = week.getOrNull(i)
                        if (dNum == null) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1.1f))
                        } else {
                            val epoch = if (calSystem == CalendarSystem.JALALI) {
                                JalaliCalendar.epochDayFromJalali(y, m, dNum)
                            } else {
                                JalaliCalendar.epochDayFromGregorian(y, m, dNum)
                            }
                            val hasTasks = tasks.any { it.dateEpochDay == epoch }
                            val isSel = epoch == selectedDateEpochDay
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { onSelectDate(epoch) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = JalaliCalendar.formatNumber(dNum, lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                if (hasTasks) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
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
private fun TaskEditorDialog(
    existing: TaskEntity?,
    existingSubtasks: List<String>,
    defaultEpochDay: Long,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (TaskEntity, List<String>) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val calSystem = LocalCalendarSystem.current

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var desc by remember { mutableStateOf(existing?.description ?: "") }
    var dateEpoch by remember { mutableStateOf(existing?.dateEpochDay ?: defaultEpochDay) }
    var priority by remember { mutableStateOf(existing?.priority ?: "MEDIUM") }
    var category by remember { mutableStateOf(existing?.categoryName ?: "Study") }
    var estMinutes by remember { mutableStateOf((existing?.estimatedMinutes ?: 45).toString()) }
    var startHour by remember { mutableStateOf(((existing?.startTimeMinutes ?: 540) / 60).toString()) }
    var endHour by remember { mutableStateOf(((existing?.endTimeMinutes ?: 600) / 60).toString()) }
    var recurrence by remember { mutableStateOf(existing?.recurrenceRule ?: "NONE") }
    var reminder by remember { mutableStateOf(existing?.reminderEnabled ?: false) }
    var studyGoal by remember { mutableStateOf(existing?.studyGoal ?: "") }
    var subtaskDraft by remember { mutableStateOf("") }
    val subtaskList = remember { mutableStateListOf<String>().apply { addAll(existingSubtasks) } }
    var showInnerDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) strings.plannerNewTask else strings.plannerEditTask) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.plannerTaskTitle) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(strings.plannerTaskDesc) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { showInnerDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(JalaliCalendar.formatDate(dateEpoch, lang, calSystem))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it.filter(Char::isDigit).take(2) },
                        label = { Text(strings.plannerStartTime) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { endHour = it.filter(Char::isDigit).take(2) },
                        label = { Text(strings.plannerEndTime) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = estMinutes,
                        onValueChange = { estMinutes = it.filter(Char::isDigit).take(3) },
                        label = { Text(strings.plannerEstDuration) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(strings.plannerPriority, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "LOW" to strings.plannerPriorityLow,
                        "MEDIUM" to strings.plannerPriorityMedium,
                        "HIGH" to strings.plannerPriorityHigh
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = priority == code,
                            onClick = { priority = code },
                            label = { Text(label) }
                        )
                    }
                }

                Text(strings.plannerCategory, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val catList = if (categories.isEmpty()) listOf("Study", "Exam Prep", "Project") else categories.map {
                        if (lang == AppLanguage.FA) it.nameFa else it.nameEn
                    }
                    catList.forEach { catName ->
                        FilterChip(
                            selected = category == catName,
                            onClick = { category = catName },
                            label = { Text(catName) }
                        )
                    }
                }

                Text(strings.plannerRecurring, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "NONE" to strings.plannerRecurrenceNone,
                        "DAILY" to strings.plannerRecurrenceDaily,
                        "WEEKLY" to strings.plannerRecurrenceWeekly
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = recurrence == code,
                            onClick = { recurrence = code },
                            label = { Text(label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = studyGoal,
                    onValueChange = { studyGoal = it },
                    label = { Text(strings.plannerStudyGoal) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.plannerReminder, style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = reminder, onCheckedChange = { reminder = it })
                }

                Text(strings.plannerSubtasks, style = MaterialTheme.typography.labelLarge)
                subtaskList.forEachIndexed { idx, s ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• $s", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        IconButton(onClick = { subtaskList.removeAt(idx) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = strings.delete, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = subtaskDraft,
                        onValueChange = { subtaskDraft = it },
                        placeholder = { Text(strings.plannerAddSubtask) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (subtaskDraft.isNotBlank()) {
                            subtaskList.add(subtaskDraft.trim())
                            subtaskDraft = ""
                        }
                    }) {
                        Icon(Icons.Rounded.Add, contentDescription = strings.add)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    val sH = (startHour.toIntOrNull() ?: 9).coerceIn(0, 23)
                    val eH = (endHour.toIntOrNull() ?: (sH + 1)).coerceIn(sH, 23)
                    val est = (estMinutes.toIntOrNull() ?: 45).coerceIn(5, 480)
                    onSave(
                        (existing ?: TaskEntity(title = title.trim(), dateEpochDay = dateEpoch)).copy(
                            title = title.trim(),
                            description = desc.trim(),
                            dateEpochDay = dateEpoch,
                            startTimeMinutes = sH * 60,
                            endTimeMinutes = eH * 60,
                            priority = priority,
                            categoryName = category,
                            estimatedMinutes = est,
                            recurrenceRule = recurrence,
                            reminderEnabled = reminder,
                            studyGoal = studyGoal.trim()
                        ),
                        subtaskList.toList()
                    )
                },
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )

    if (showInnerDatePicker) {
        BilingualDatePickerDialog(
            initialEpochDay = dateEpoch,
            onDismiss = { showInnerDatePicker = false },
            onDateSelected = { dateEpoch = it }
        )
    }
}
