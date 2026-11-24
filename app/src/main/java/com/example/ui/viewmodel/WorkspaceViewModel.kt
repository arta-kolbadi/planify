package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PlanifyApplication
import com.example.core.i18n.JalaliCalendar
import com.example.data.local.AcademyProgressEntity
import com.example.data.local.BlockedAppEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.CustomLessonEntity
import com.example.data.local.FlashcardEntity
import com.example.data.local.FocusSessionEntity
import com.example.data.local.NoteEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudyRoomEntity
import com.example.data.local.StudyRoomTaskEntity
import com.example.data.local.SubtaskEntity
import com.example.data.local.TaskEntity
import com.example.focus.AmbientSound
import com.example.focus.FocusMode
import com.example.focus.InstalledAppInfo
import com.example.focus.UsageAccessHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PlanifyApplication
    private val repo = app.repository
    val focusManager = app.focusTimerManager

    val categories: StateFlow<List<CategoryEntity>> = repo.categoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repo.tasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subtasks: StateFlow<List<SubtaskEntity>> = repo.subtasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionEntity>> = repo.focusSessionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repo.notesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val academyProgress: StateFlow<List<AcademyProgressEntity>> = repo.academyProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizAttempts: StateFlow<List<QuizAttemptEntity>> = repo.quizAttemptsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<FlashcardEntity>> = repo.flashcardsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customLessons: StateFlow<List<CustomLessonEntity>> = repo.customLessonsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyRooms: StateFlow<List<StudyRoomEntity>> = repo.studyRoomsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyRoomTasks: StateFlow<List<StudyRoomTaskEntity>> = repo.studyRoomTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedApps: StateFlow<List<BlockedAppEntity>> = repo.blockedAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDateEpochDay = MutableStateFlow(JalaliCalendar.todayEpochDay())
    val selectedDateEpochDay: StateFlow<Long> = _selectedDateEpochDay.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    fun selectDate(epochDay: Long) {
        _selectedDateEpochDay.value = epochDay
    }

    fun saveTask(task: TaskEntity, subtaskTitles: List<String>) {
        viewModelScope.launch {
            repo.saveTask(task, subtaskTitles)
        }
    }

    fun toggleTaskDone(task: TaskEntity) {
        viewModelScope.launch { repo.toggleTaskDone(task) }
    }

    fun toggleSubtask(subtask: SubtaskEntity) {
        viewModelScope.launch { repo.toggleSubtask(subtask) }
    }

    fun moveTaskOrder(task: TaskEntity, dayTasks: List<TaskEntity>, moveUp: Boolean) {
        viewModelScope.launch { repo.moveTaskOrder(task, dayTasks, moveUp) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repo.deleteTask(task) }
    }

    fun startFocusWithTask(task: TaskEntity) {
        focusManager.selectTask(task.id, task.title, task.categoryName)
        focusManager.selectMode(FocusMode.CUSTOM)
        focusManager.setCustomMinutes(task.estimatedMinutes.coerceIn(5, 180))
        focusManager.startSession()
    }

    fun setFocusMode(mode: FocusMode) = focusManager.selectMode(mode)
    fun setFocusMinutes(minutes: Int) = focusManager.setCustomMinutes(minutes)
    fun selectFocusTask(taskId: Long?, title: String, category: String) =
        focusManager.selectTask(taskId, title, category)
    fun setAmbientSound(sound: AmbientSound) = focusManager.setAmbientSound(sound)
    fun setAppBlockEnabled(enabled: Boolean) = focusManager.setAppBlockEnabled(enabled)
    fun startFocus() = focusManager.startSession()
    fun pauseFocus() = focusManager.pauseSession()
    fun resumeFocus() = focusManager.resumeSession()
    fun endFocus(completed: Boolean) = focusManager.finishAndSaveSession(completed)

    fun refreshInstalledApps(context: Context) {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                UsageAccessHelper.getLaunchableApps(context)
            }
            _installedApps.value = apps
        }
    }

    fun setAppBlocked(pkg: String, label: String, blocked: Boolean) {
        viewModelScope.launch { repo.setAppBlocked(pkg, label, blocked) }
    }

    fun saveNote(note: NoteEntity, onSavedId: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repo.saveNote(note)
            onSavedId?.invoke(id)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repo.deleteNote(note) }
    }

    fun markLessonCompleted(lessonId: String, subjectId: String, completed: Boolean) {
        viewModelScope.launch { repo.markLessonCompleted(lessonId, subjectId, completed) }
    }

    fun toggleLessonBookmark(lessonId: String, subjectId: String) {
        viewModelScope.launch { repo.toggleLessonBookmark(lessonId, subjectId) }
    }

    fun recordQuizAttempt(subjectId: String, lessonId: String, score: Int, total: Int) {
        viewModelScope.launch { repo.recordQuizAttempt(subjectId, lessonId, score, total) }
    }

    fun answerFlashcard(card: FlashcardEntity, known: Boolean) {
        viewModelScope.launch { repo.updateFlashcardMastery(card, known) }
    }

    fun addCustomLesson(subjectId: String, title: String, body: String, uri: String?) {
        viewModelScope.launch { repo.addCustomLesson(subjectId, title, body, uri) }
    }

    fun deleteCustomLesson(lesson: CustomLessonEntity) {
        viewModelScope.launch { repo.deleteCustomLesson(lesson) }
    }

    fun createStudyRoom(name: String, isGroup: Boolean, goal: String, minutes: Int, participants: String) {
        viewModelScope.launch {
            repo.createStudyRoom(
                StudyRoomEntity(
                    name = name.trim(),
                    isGroup = isGroup,
                    sharedGoal = goal.trim(),
                    sessionMinutes = minutes.coerceIn(5, 180),
                    participantsCsv = participants.trim()
                )
            )
        }
    }

    fun recordStudyRoomMinutes(room: StudyRoomEntity, minutes: Int) {
        viewModelScope.launch {
            repo.updateStudyRoom(
                room.copy(
                    completedMinutes = room.completedMinutes + minutes,
                    completedSessions = room.completedSessions + 1
                )
            )
            repo.awardProgress(xpDelta = minutes * 2, coinDelta = (minutes / 3).coerceAtLeast(3))
        }
    }

    fun deleteStudyRoom(room: StudyRoomEntity) {
        viewModelScope.launch { repo.deleteStudyRoom(room) }
    }

    fun addRoomTask(roomId: Long, title: String, assignee: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repo.addStudyRoomTask(StudyRoomTaskEntity(roomId = roomId, title = title.trim(), assignee = assignee.trim()))
        }
    }

    fun toggleRoomTask(task: StudyRoomTaskEntity) {
        viewModelScope.launch { repo.toggleStudyRoomTask(task) }
    }

    fun deleteRoomTask(task: StudyRoomTaskEntity) {
        viewModelScope.launch { repo.deleteRoomTask(task) }
    }
}
