package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllOnce(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CategoryEntity): Long
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dateEpochDay ASC, sortOrder ASC, startTimeMinutes ASC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    suspend fun getAllTasksOnce(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE dateEpochDay = :epochDay ORDER BY sortOrder ASC, startTimeMinutes ASC")
    fun observeTasksForDay(epochDay: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY id ASC")
    fun observeSubtasks(taskId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks ORDER BY id ASC")
    fun observeAllSubtasks(): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks ORDER BY id ASC")
    suspend fun getAllSubtasksOnce(): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksForTask(taskId: Long)
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun observeAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    suspend fun getAllSessionsOnce(): List<FocusSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY id ASC")
    suspend fun getAllNotesOnce(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun clearAll()
}

@Dao
interface AiChatDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC, id ASC")
    fun observeMessages(conversationId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC, id ASC")
    suspend fun getMessagesOnce(conversationId: Long): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun clearMessages(conversationId: Long)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)
}

@Dao
interface AcademyDao {
    @Query("SELECT * FROM academy_progress")
    fun observeProgress(): Flow<List<AcademyProgressEntity>>

    @Query("SELECT * FROM academy_progress")
    suspend fun getProgressOnce(): List<AcademyProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: AcademyProgressEntity)

    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun observeQuizAttempts(): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttemptEntity): Long

    @Query("SELECT * FROM flashcards ORDER BY id ASC")
    fun observeFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards ORDER BY id ASC")
    suspend fun getFlashcardsOnce(): List<FlashcardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(card: FlashcardEntity)

    @Query("SELECT * FROM custom_lessons ORDER BY createdAt DESC")
    fun observeCustomLessons(): Flow<List<CustomLessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomLesson(lesson: CustomLessonEntity): Long

    @Delete
    suspend fun deleteCustomLesson(lesson: CustomLessonEntity)
}

@Dao
interface StudyRoomDao {
    @Query("SELECT * FROM study_rooms ORDER BY createdAt DESC")
    fun observeRooms(): Flow<List<StudyRoomEntity>>

    @Query("SELECT * FROM study_room_tasks ORDER BY id ASC")
    fun observeAllRoomTasks(): Flow<List<StudyRoomTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: StudyRoomEntity): Long

    @Update
    suspend fun updateRoom(room: StudyRoomEntity)

    @Delete
    suspend fun deleteRoom(room: StudyRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoomTask(task: StudyRoomTaskEntity): Long

    @Update
    suspend fun updateRoomTask(task: StudyRoomTaskEntity)

    @Delete
    suspend fun deleteRoomTask(task: StudyRoomTaskEntity)
}

@Dao
interface GamificationDao {
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, id ASC")
    fun observeAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAchievementsOnce(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(items: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(item: AchievementEntity)

    @Query("SELECT * FROM challenges ORDER BY isWeekly ASC, id ASC")
    fun observeChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges")
    suspend fun getChallengesOnce(): List<ChallengeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(items: List<ChallengeEntity>)

    @Update
    suspend fun updateChallenge(item: ChallengeEntity)
}

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY appLabel ASC")
    fun observeBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    suspend fun getBlockedPackagesOnce(): List<BlockedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlockedApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteBlockedApp(packageName: String)
}
