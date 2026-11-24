package com.example.data.local

import androidx.room.withTransaction
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class PlanifyBackupPayload(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val profile: UserProfileBackup? = null,
    val tasks: List<TaskBackup> = emptyList(),
    val subtasks: List<SubtaskBackup> = emptyList(),
    val focusSessions: List<FocusSessionBackup> = emptyList(),
    val notes: List<NoteBackup> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UserProfileBackup(
    val displayName: String,
    val xp: Int,
    val level: Int,
    val coins: Int,
    val streakDays: Int,
    val dailyFocusGoalMinutes: Int,
    val equippedOutfit: String,
    val equippedAccessory: String,
    val equippedRoom: String,
    val unlockedItemsCsv: String
)

@JsonClass(generateAdapter = true)
data class TaskBackup(
    val id: Long,
    val title: String,
    val description: String,
    val dateEpochDay: Long,
    val startTimeMinutes: Int,
    val endTimeMinutes: Int,
    val priority: String,
    val categoryName: String,
    val status: String,
    val estimatedMinutes: Int,
    val actualMinutes: Int,
    val recurrenceRule: String,
    val reminderEnabled: Boolean,
    val studyGoal: String,
    val sortOrder: Int
)

@JsonClass(generateAdapter = true)
data class SubtaskBackup(
    val taskId: Long,
    val title: String,
    val isDone: Boolean
)

@JsonClass(generateAdapter = true)
data class FocusSessionBackup(
    val mode: String,
    val taskTitle: String,
    val categoryName: String,
    val plannedSeconds: Int,
    val actualSeconds: Int,
    val completed: Boolean,
    val ambientSound: String,
    val dateEpochDay: Long,
    val startedAt: Long,
    val endedAt: Long
)

@JsonClass(generateAdapter = true)
data class NoteBackup(
    val title: String,
    val content: String,
    val category: String,
    val tagsCsv: String,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val isJournal: Boolean,
    val journalDateEpochDay: Long,
    val createdAt: Long,
    val updatedAt: Long
)

class BackupManager(private val db: AppDatabase) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(PlanifyBackupPayload::class.java).indent("  ")

    suspend fun exportJson(): String {
        val profile = db.userProfileDao().getProfileOnce()
        val tasks = db.taskDao().getAllTasksOnce()
        val subtasks = db.taskDao().getAllSubtasksOnce()
        val sessions = db.focusSessionDao().getAllSessionsOnce()
        val notes = db.noteDao().getAllNotesOnce()

        val payload = PlanifyBackupPayload(
            profile = profile?.let {
                UserProfileBackup(
                    displayName = it.displayName,
                    xp = it.xp,
                    level = it.level,
                    coins = it.coins,
                    streakDays = it.streakDays,
                    dailyFocusGoalMinutes = it.dailyFocusGoalMinutes,
                    equippedOutfit = it.equippedOutfit,
                    equippedAccessory = it.equippedAccessory,
                    equippedRoom = it.equippedRoom,
                    unlockedItemsCsv = it.unlockedItemsCsv
                )
            },
            tasks = tasks.map {
                TaskBackup(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    dateEpochDay = it.dateEpochDay,
                    startTimeMinutes = it.startTimeMinutes,
                    endTimeMinutes = it.endTimeMinutes,
                    priority = it.priority,
                    categoryName = it.categoryName,
                    status = it.status,
                    estimatedMinutes = it.estimatedMinutes,
                    actualMinutes = it.actualMinutes,
                    recurrenceRule = it.recurrenceRule,
                    reminderEnabled = it.reminderEnabled,
                    studyGoal = it.studyGoal,
                    sortOrder = it.sortOrder
                )
            },
            subtasks = subtasks.map { SubtaskBackup(it.taskId, it.title, it.isDone) },
            focusSessions = sessions.map {
                FocusSessionBackup(
                    mode = it.mode,
                    taskTitle = it.taskTitle,
                    categoryName = it.categoryName,
                    plannedSeconds = it.plannedSeconds,
                    actualSeconds = it.actualSeconds,
                    completed = it.completed,
                    ambientSound = it.ambientSound,
                    dateEpochDay = it.dateEpochDay,
                    startedAt = it.startedAt,
                    endedAt = it.endedAt
                )
            },
            notes = notes.map {
                NoteBackup(
                    title = it.title,
                    content = it.content,
                    category = it.category,
                    tagsCsv = it.tagsCsv,
                    isPinned = it.isPinned,
                    isFavorite = it.isFavorite,
                    isJournal = it.isJournal,
                    journalDateEpochDay = it.journalDateEpochDay,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        )
        return adapter.toJson(payload)
    }

    suspend fun importJson(rawJson: String): Result<Int> = runCatching {
        val payload = adapter.fromJson(rawJson)
            ?: throw IllegalArgumentException("Invalid backup format")

        var restoredCount = 0
        db.withTransaction {
            payload.profile?.let { p ->
                val cur = db.userProfileDao().getProfileOnce() ?: UserProfileEntity()
                db.userProfileDao().upsert(
                    cur.copy(
                        displayName = p.displayName,
                        xp = p.xp,
                        level = p.level,
                        coins = p.coins,
                        streakDays = p.streakDays,
                        dailyFocusGoalMinutes = p.dailyFocusGoalMinutes,
                        equippedOutfit = p.equippedOutfit,
                        equippedAccessory = p.equippedAccessory,
                        equippedRoom = p.equippedRoom,
                        unlockedItemsCsv = p.unlockedItemsCsv
                    )
                )
            }
            db.taskDao().clearAll()
            val taskIdMap = mutableMapOf<Long, Long>()
            for (t in payload.tasks) {
                val newId = db.taskDao().insertTask(
                    TaskEntity(
                        title = t.title,
                        description = t.description,
                        dateEpochDay = t.dateEpochDay,
                        startTimeMinutes = t.startTimeMinutes,
                        endTimeMinutes = t.endTimeMinutes,
                        priority = t.priority,
                        categoryName = t.categoryName,
                        status = t.status,
                        estimatedMinutes = t.estimatedMinutes,
                        actualMinutes = t.actualMinutes,
                        recurrenceRule = t.recurrenceRule,
                        reminderEnabled = t.reminderEnabled,
                        studyGoal = t.studyGoal,
                        sortOrder = t.sortOrder
                    )
                )
                taskIdMap[t.id] = newId
                restoredCount++
            }
            for (st in payload.subtasks) {
                val mappedTaskId = taskIdMap[st.taskId] ?: continue
                db.taskDao().insertSubtask(SubtaskEntity(taskId = mappedTaskId, title = st.title, isDone = st.isDone))
            }
            db.focusSessionDao().clearAll()
            for (s in payload.focusSessions) {
                db.focusSessionDao().insertSession(
                    FocusSessionEntity(
                        mode = s.mode,
                        taskTitle = s.taskTitle,
                        categoryName = s.categoryName,
                        plannedSeconds = s.plannedSeconds,
                        actualSeconds = s.actualSeconds,
                        completed = s.completed,
                        ambientSound = s.ambientSound,
                        dateEpochDay = s.dateEpochDay,
                        startedAt = s.startedAt,
                        endedAt = s.endedAt
                    )
                )
                restoredCount++
            }
            db.noteDao().clearAll()
            for (n in payload.notes) {
                db.noteDao().insertNote(
                    NoteEntity(
                        title = n.title,
                        content = n.content,
                        category = n.category,
                        tagsCsv = n.tagsCsv,
                        isPinned = n.isPinned,
                        isFavorite = n.isFavorite,
                        isJournal = n.isJournal,
                        journalDateEpochDay = n.journalDateEpochDay,
                        createdAt = n.createdAt,
                        updatedAt = n.updatedAt
                    )
                )
                restoredCount++
            }
        }
        restoredCount
    }
}
