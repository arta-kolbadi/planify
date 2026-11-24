package com.example.focus

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.core.i18n.AppStrings
import com.example.core.i18n.JalaliCalendar
import com.example.data.local.FocusSessionEntity
import com.example.data.local.PersistedFocusState
import com.example.data.local.PlanifyRepository
import com.example.data.local.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class FocusMode(val id: String, val defaultMinutes: Int, val isCountUp: Boolean = false) {
    POMODORO("POMODORO", 25),
    CUSTOM("CUSTOM", 45),
    STOPWATCH("STOPWATCH", 0, isCountUp = true),
    COUNT_UP("COUNT_UP", 0, isCountUp = true),
    DEEP_FOCUS("DEEP_FOCUS", 60),
    STUDY_SESSION("STUDY_SESSION", 50),
    SHORT_BREAK("SHORT_BREAK", 5),
    LONG_BREAK("LONG_BREAK", 15);

    fun label(strings: AppStrings): String = when (this) {
        POMODORO -> strings.focusModePomodoro
        CUSTOM -> strings.focusModeCustom
        STOPWATCH -> strings.focusModeStopwatch
        COUNT_UP -> strings.focusModeCountUp
        DEEP_FOCUS -> strings.focusModeDeepFocus
        STUDY_SESSION -> strings.focusModeStudySession
        SHORT_BREAK -> strings.focusModeShortBreak
        LONG_BREAK -> strings.focusModeLongBreak
    }

    companion object {
        fun fromId(id: String?): FocusMode = entries.firstOrNull { it.id == id } ?: POMODORO
    }
}

data class FocusTimerUiState(
    val isActive: Boolean = false,
    val isRunning: Boolean = false,
    val mode: FocusMode = FocusMode.POMODORO,
    val taskId: Long? = null,
    val taskTitle: String = "",
    val categoryName: String = "Study",
    val plannedSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val elapsedSeconds: Int = 0,
    val startedAt: Long = 0L,
    val ambientSound: AmbientSound = AmbientSound.NONE,
    val appBlockEnabled: Boolean = false,
    val lastCompletedMessageFlag: Long = 0L
)

class FocusTimerManager(
    private val context: Context,
    private val repository: PlanifyRepository,
    private val preferences: PreferencesRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ambientPlayer = AmbientSoundPlayer()
    private var tickerJob: Job? = null

    private val _state = MutableStateFlow(FocusTimerUiState())
    val state: StateFlow<FocusTimerUiState> = _state.asStateFlow()

    init {
        restorePersistedSession()
    }

    private fun restorePersistedSession() {
        scope.launch {
            val saved = preferences.getPersistedFocusState()
            if (!saved.isActive) return@launch
            val mode = FocusMode.fromId(saved.mode)
            val now = System.currentTimeMillis()
            val deltaSeconds = if (saved.isRunning && saved.lastTickAt > 0L) {
                ((now - saved.lastTickAt) / 1000L).toInt().coerceAtLeast(0)
            } else {
                0
            }
            val newElapsed = saved.elapsedSeconds + deltaSeconds
            val newRemaining = if (mode.isCountUp) 0 else (saved.remainingSeconds - deltaSeconds).coerceAtLeast(0)
            _state.value = FocusTimerUiState(
                isActive = true,
                isRunning = saved.isRunning,
                mode = mode,
                taskId = saved.taskId,
                taskTitle = saved.taskTitle,
                categoryName = saved.categoryName,
                plannedSeconds = saved.plannedSeconds,
                remainingSeconds = newRemaining,
                elapsedSeconds = newElapsed,
                startedAt = saved.startedAt,
                ambientSound = AmbientSound.fromId(saved.ambientSound),
                appBlockEnabled = saved.appBlockEnabled
            )
            if (!mode.isCountUp && saved.isRunning && newRemaining <= 0) {
                finishAndSaveSession(completed = true)
            } else if (saved.isRunning) {
                startTicker()
                startMonitorServiceIfNeeded()
            }
        }
    }

    fun selectMode(mode: FocusMode) {
        if (_state.value.isActive) return
        val secs = mode.defaultMinutes * 60
        _state.update {
            it.copy(
                mode = mode,
                plannedSeconds = secs,
                remainingSeconds = secs,
                elapsedSeconds = 0
            )
        }
    }

    fun setCustomMinutes(minutes: Int) {
        if (_state.value.isActive) return
        val secs = minutes.coerceIn(1, 240) * 60
        _state.update {
            it.copy(
                plannedSeconds = secs,
                remainingSeconds = secs,
                elapsedSeconds = 0
            )
        }
    }

    fun selectTask(taskId: Long?, taskTitle: String, categoryName: String) {
        _state.update {
            it.copy(
                taskId = taskId,
                taskTitle = taskTitle,
                categoryName = categoryName.ifBlank { "Study" }
            )
        }
        persistCurrent()
    }

    fun setAmbientSound(sound: AmbientSound) {
        _state.update { it.copy(ambientSound = sound) }
        if (_state.value.isRunning) {
            ambientPlayer.start(scope, sound)
        }
        persistCurrent()
    }

    fun setAppBlockEnabled(enabled: Boolean) {
        _state.update { it.copy(appBlockEnabled = enabled) }
        persistCurrent()
    }

    fun startSession() {
        val cur = _state.value
        val now = System.currentTimeMillis()
        val planned = if (cur.mode.isCountUp) 0 else cur.plannedSeconds.coerceAtLeast(60)
        _state.update {
            it.copy(
                isActive = true,
                isRunning = true,
                plannedSeconds = planned,
                remainingSeconds = if (it.isActive) it.remainingSeconds else planned,
                elapsedSeconds = if (it.isActive) it.elapsedSeconds else 0,
                startedAt = if (it.isActive && it.startedAt > 0L) it.startedAt else now
            )
        }
        ambientPlayer.start(scope, _state.value.ambientSound)
        startTicker()
        startMonitorServiceIfNeeded()
        persistCurrent()
    }

    fun pauseSession() {
        _state.update { it.copy(isRunning = false) }
        tickerJob?.cancel()
        ambientPlayer.stop()
        persistCurrent()
    }

    fun resumeSession() {
        if (!_state.value.isActive) return
        _state.update { it.copy(isRunning = true) }
        ambientPlayer.start(scope, _state.value.ambientSound)
        startTicker()
        startMonitorServiceIfNeeded()
        persistCurrent()
    }

    fun finishAndSaveSession(completed: Boolean) {
        tickerJob?.cancel()
        ambientPlayer.stop()
        stopMonitorService()

        val cur = _state.value
        val actual = cur.elapsedSeconds
        val mode = cur.mode
        val resetSecs = mode.defaultMinutes * 60
        _state.value = FocusTimerUiState(
            isActive = false,
            isRunning = false,
            mode = mode,
            taskId = cur.taskId,
            taskTitle = cur.taskTitle,
            categoryName = cur.categoryName,
            plannedSeconds = resetSecs,
            remainingSeconds = resetSecs,
            elapsedSeconds = 0,
            ambientSound = cur.ambientSound,
            appBlockEnabled = cur.appBlockEnabled,
            lastCompletedMessageFlag = if (actual >= 10) System.currentTimeMillis() else cur.lastCompletedMessageFlag
        )

        scope.launch {
            preferences.saveFocusState(PersistedFocusState(isActive = false))
            if (actual >= 10) {
                repository.recordFocusSession(
                    FocusSessionEntity(
                        mode = mode.id,
                        taskId = cur.taskId,
                        taskTitle = cur.taskTitle,
                        categoryName = cur.categoryName,
                        plannedSeconds = cur.plannedSeconds,
                        actualSeconds = actual,
                        completed = completed,
                        ambientSound = cur.ambientSound.id,
                        dateEpochDay = JalaliCalendar.todayEpochDay(),
                        startedAt = if (cur.startedAt > 0L) cur.startedAt else System.currentTimeMillis() - actual * 1000L,
                        endedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && _state.value.isRunning) {
                delay(1000L)
                val cur = _state.value
                if (!cur.isRunning) break
                val nextElapsed = cur.elapsedSeconds + 1
                if (cur.mode.isCountUp) {
                    _state.update { it.copy(elapsedSeconds = nextElapsed) }
                } else {
                    val nextRem = (cur.remainingSeconds - 1).coerceAtLeast(0)
                    _state.update {
                        it.copy(
                            elapsedSeconds = nextElapsed,
                            remainingSeconds = nextRem
                        )
                    }
                    if (nextRem <= 0) {
                        finishAndSaveSession(completed = true)
                        break
                    }
                }
                if (nextElapsed % 5 == 0) {
                    persistCurrent()
                }
            }
        }
    }

    private fun persistCurrent() {
        val cur = _state.value
        scope.launch {
            preferences.saveFocusState(
                PersistedFocusState(
                    isActive = cur.isActive,
                    isRunning = cur.isRunning,
                    mode = cur.mode.id,
                    taskId = cur.taskId,
                    taskTitle = cur.taskTitle,
                    categoryName = cur.categoryName,
                    plannedSeconds = cur.plannedSeconds,
                    remainingSeconds = cur.remainingSeconds,
                    elapsedSeconds = cur.elapsedSeconds,
                    startedAt = cur.startedAt,
                    lastTickAt = System.currentTimeMillis(),
                    ambientSound = cur.ambientSound.id,
                    appBlockEnabled = cur.appBlockEnabled
                )
            )
        }
    }

    private fun startMonitorServiceIfNeeded() {
        val intent = Intent(context, FocusMonitorService::class.java)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private fun stopMonitorService() {
        runCatching {
            context.stopService(Intent(context, FocusMonitorService::class.java))
        }
    }
}
