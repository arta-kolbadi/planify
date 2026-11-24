package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.local.StudyRoomEntity
import com.example.data.local.StudyRoomTaskEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PlanifyCard
import com.example.ui.theme.LocalPlanifyColors
import kotlinx.coroutines.delay

@Composable
fun StudyRoomScreen(
    rooms: List<StudyRoomEntity>,
    roomTasks: List<StudyRoomTaskEntity>,
    onCreateRoom: (name: String, isGroup: Boolean, goal: String, minutes: Int, participants: String) -> Unit,
    onRecordRoomSession: (StudyRoomEntity, Int) -> Unit,
    onDeleteRoom: (StudyRoomEntity) -> Unit,
    onAddRoomTask: (Long, String, String) -> Unit,
    onToggleRoomTask: (StudyRoomTaskEntity) -> Unit,
    onDeleteRoomTask: (StudyRoomTaskEntity) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val extra = LocalPlanifyColors.current

    var tabIndex by remember { mutableIntStateOf(0) } // 0=Solo, 1=Group
    var showCreateDialog by remember { mutableStateOf(false) }
    var activeRoomId by remember { mutableStateOf<Long?>(null) }

    val filteredRooms = remember(rooms, tabIndex) {
        rooms.filter { it.isGroup == (tabIndex == 1) }
    }
    val activeRoom = rooms.firstOrNull { it.id == activeRoomId }

    if (activeRoom != null) {
        BackHandler { activeRoomId = null }
        ActiveStudyRoomView(
            room = activeRoom,
            tasks = roomTasks.filter { it.roomId == activeRoom.id },
            onBack = { activeRoomId = null },
            onCompleteSession = { mins -> onRecordRoomSession(activeRoom, mins) },
            onAddTask = { title, assignee -> onAddRoomTask(activeRoom.id, title, assignee) },
            onToggleTask = onToggleRoomTask,
            onDeleteTask = onDeleteRoomTask
        )
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.testTag("study_room_create_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.roomCreate)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
            ) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text(strings.roomSoloTab) })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text(strings.roomGroupTab) })
            }

            if (tabIndex == 1) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = extra.warning.copy(alpha = 0.14f),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = strings.roomLocalNotice,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (filteredRooms.isEmpty()) {
                EmptyStateCard(
                    icon = if (tabIndex == 0) Icons.Rounded.Person else Icons.Rounded.Groups,
                    title = strings.roomCreate,
                    subtitle = strings.roomGoalHint,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRooms, key = { it.id }) { room ->
                        PlanifyCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { activeRoomId = room.id }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (room.isGroup) Icons.Rounded.Groups else Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(room.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (room.sharedGoal.isNotBlank()) {
                                        Text(room.sharedGoal, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = "${JalaliCalendar.formatNumber(room.sessionMinutes, lang)} ${strings.minutesShort} • ${JalaliCalendar.formatNumber(room.completedSessions, lang)} ${strings.dashSessionsCompleted}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { onDeleteRoom(room) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = extra.danger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var goal by remember { mutableStateOf("") }
        var minutes by remember { mutableStateOf("45") }
        var participants by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(strings.roomCreate) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(strings.roomNameHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text(strings.roomGoalHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { minutes = it.filter(Char::isDigit).take(3) },
                        label = { Text(strings.roomDurationHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (tabIndex == 1) {
                        OutlinedTextField(
                            value = participants,
                            onValueChange = { participants = it },
                            label = { Text(strings.roomParticipants) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        onCreateRoom(name, tabIndex == 1, goal, minutes.toIntOrNull() ?: 45, participants)
                        showCreateDialog = false
                    }
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}

@Composable
private fun ActiveStudyRoomView(
    room: StudyRoomEntity,
    tasks: List<StudyRoomTaskEntity>,
    onBack: () -> Unit,
    onCompleteSession: (Int) -> Unit,
    onAddTask: (String, String) -> Unit,
    onToggleTask: (StudyRoomTaskEntity) -> Unit,
    onDeleteTask: (StudyRoomTaskEntity) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val extra = LocalPlanifyColors.current

    var remainingSeconds by remember(room.id) { mutableIntStateOf(room.sessionMinutes * 60) }
    var running by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    LaunchedEffect(running) {
        while (running && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
            if (remainingSeconds == 0) {
                running = false
                onCompleteSession(room.sessionMinutes)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack) { Text("← ${strings.back}") }
            Text(room.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (room.sharedGoal.isNotBlank()) {
                Text("${strings.roomSharedGoals}: ${room.sharedGoal}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = JalaliCalendar.formatCountdown(remainingSeconds, lang),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { running = !running }) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (running) strings.roomPauseTimer else strings.roomStartTimer)
                        }
                        OutlinedButton(onClick = {
                            running = false
                            remainingSeconds = room.sessionMinutes * 60
                        }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.roomResetTimer)
                        }
                    }
                    Text(
                        text = "${strings.roomStats}: ${JalaliCalendar.formatNumber(room.completedMinutes, lang)} ${strings.minutesShort} • ${JalaliCalendar.formatNumber(room.completedSessions, lang)} ${strings.dashSessionsCompleted}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (room.isGroup && room.participantsCsv.isNotBlank()) {
            item {
                PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(strings.roomParticipants, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(room.participantsCsv, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Text(strings.roomSharedTasks, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = { Text(strings.plannerTaskTitle) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        onAddTask(newTaskTitle, "")
                        newTaskTitle = ""
                    }
                }) {
                    Text(strings.add)
                }
            }
        }

        items(tasks, key = { it.id }) { t ->
            PlanifyCard(modifier = Modifier.fillMaxWidth(), onClick = { onToggleTask(t) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (t.isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = t.title,
                        tint = if (t.isDone) extra.success else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = t.title,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (t.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                    IconButton(onClick = { onDeleteTask(t) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = strings.delete, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
