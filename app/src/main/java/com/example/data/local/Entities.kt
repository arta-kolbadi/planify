package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val displayName: String = "",
    val xp: Int = 0,
    val level: Int = 1,
    val coins: Int = 50,
    val streakDays: Int = 0,
    val lastActiveEpochDay: Long = 0L,
    val dailyFocusGoalMinutes: Int = 90,
    val equippedOutfit: String = "classic_blue",
    val equippedAccessory: String = "none",
    val equippedRoom: String = "minimal_desk",
    val unlockedItemsCsv: String = "classic_blue,none,minimal_desk"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameEn: String,
    val nameFa: String,
    val colorHex: Long
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateEpochDay: Long,
    val startTimeMinutes: Int = 540, // 09:00
    val endTimeMinutes: Int = 600,   // 10:00
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val categoryName: String = "Study",
    val status: String = "TODO",     // TODO, IN_PROGRESS, DONE
    val estimatedMinutes: Int = 45,
    val actualMinutes: Int = 0,
    val recurrenceRule: String = "NONE", // NONE, DAILY, WEEKLY
    val reminderEnabled: Boolean = false,
    val studyGoal: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subtasks")
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val isDone: Boolean = false
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String, // POMODORO, CUSTOM, STOPWATCH, COUNT_UP, DEEP_FOCUS, STUDY_SESSION, SHORT_BREAK, LONG_BREAK
    val taskId: Long? = null,
    val taskTitle: String = "",
    val categoryName: String = "Study",
    val plannedSeconds: Int,
    val actualSeconds: Int,
    val completed: Boolean,
    val ambientSound: String = "NONE",
    val dateEpochDay: Long,
    val startedAt: Long,
    val endedAt: Long
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "Study",
    val tagsCsv: String = "",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isJournal: Boolean = false,
    val journalDateEpochDay: Long = 0L,
    val attachmentUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assistantType: String, // GENERAL, STUDY, PLANNER, FOCUS, NOTE
    val title: String,
    val modelId: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val role: String, // user, assistant
    val content: String,
    val modelUsed: String = "",
    val attachmentSummary: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "academy_progress")
data class AcademyProgressEntity(
    @PrimaryKey val lessonId: String,
    val subjectId: String,
    val isCompleted: Boolean = false,
    val isBookmarked: Boolean = false,
    val weakScoreCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val lessonId: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val frontEn: String,
    val backEn: String,
    val frontFa: String,
    val backFa: String,
    val masteryLevel: Int = 0,
    val nextReviewEpochDay: Long = 0L
)

@Entity(tableName = "custom_lessons")
data class CustomLessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val title: String,
    val body: String,
    val attachmentUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_rooms")
data class StudyRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isGroup: Boolean = false,
    val sharedGoal: String = "",
    val sessionMinutes: Int = 45,
    val participantsCsv: String = "",
    val completedMinutes: Int = 0,
    val completedSessions: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_room_tasks")
data class StudyRoomTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val title: String,
    val assignee: String = "",
    val isDone: Boolean = false
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val titleEn: String,
    val titleFa: String,
    val descEn: String,
    val descFa: String,
    val xpReward: Int,
    val coinReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val titleEn: String,
    val titleFa: String,
    val isWeekly: Boolean,
    val targetCount: Int,
    val currentCount: Int = 0,
    val xpReward: Int,
    val coinReward: Int,
    val isClaimed: Boolean = false
)

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val isBlocked: Boolean = true
)
