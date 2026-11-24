package com.example.data.local

import com.example.core.i18n.JalaliCalendar
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

class PlanifyRepository(private val db: AppDatabase) {
    val profileFlow: Flow<UserProfileEntity?> = db.userProfileDao().observeProfile()
    val categoriesFlow: Flow<List<CategoryEntity>> = db.categoryDao().observeAll()
    val tasksFlow: Flow<List<TaskEntity>> = db.taskDao().observeAllTasks()
    val subtasksFlow: Flow<List<SubtaskEntity>> = db.taskDao().observeAllSubtasks()
    val focusSessionsFlow: Flow<List<FocusSessionEntity>> = db.focusSessionDao().observeAllSessions()
    val notesFlow: Flow<List<NoteEntity>> = db.noteDao().observeAllNotes()
    val conversationsFlow: Flow<List<ConversationEntity>> = db.aiChatDao().observeConversations()
    val academyProgressFlow: Flow<List<AcademyProgressEntity>> = db.academyDao().observeProgress()
    val quizAttemptsFlow: Flow<List<QuizAttemptEntity>> = db.academyDao().observeQuizAttempts()
    val flashcardsFlow: Flow<List<FlashcardEntity>> = db.academyDao().observeFlashcards()
    val customLessonsFlow: Flow<List<CustomLessonEntity>> = db.academyDao().observeCustomLessons()
    val studyRoomsFlow: Flow<List<StudyRoomEntity>> = db.studyRoomDao().observeRooms()
    val studyRoomTasksFlow: Flow<List<StudyRoomTaskEntity>> = db.studyRoomDao().observeAllRoomTasks()
    val achievementsFlow: Flow<List<AchievementEntity>> = db.gamificationDao().observeAchievements()
    val challengesFlow: Flow<List<ChallengeEntity>> = db.gamificationDao().observeChallenges()
    val blockedAppsFlow: Flow<List<BlockedAppEntity>> = db.blockedAppDao().observeBlockedApps()

    fun observeMessages(conversationId: Long): Flow<List<ChatMessageEntity>> =
        db.aiChatDao().observeMessages(conversationId)

    suspend fun ensureSeeded() = AppDatabase.seedInitialData(db)

    suspend fun updateProfileNameAndGoal(name: String, dailyGoalMinutes: Int) {
        val current = db.userProfileDao().getProfileOnce() ?: UserProfileEntity()
        db.userProfileDao().upsert(
            current.copy(
                displayName = name.trim(),
                dailyFocusGoalMinutes = dailyGoalMinutes.coerceIn(15, 600)
            )
        )
    }

    suspend fun awardProgress(xpDelta: Int, coinDelta: Int) {
        val today = JalaliCalendar.todayEpochDay()
        val current = db.userProfileDao().getProfileOnce() ?: UserProfileEntity()
        val newXp = max(0, current.xp + xpDelta)
        val newLevel = 1 + (newXp / 150)
        val newCoins = max(0, current.coins + coinDelta)
        val newStreak = when {
            current.lastActiveEpochDay == today -> max(1, current.streakDays)
            current.lastActiveEpochDay == today - 1 -> current.streakDays + 1
            else -> 1
        }
        db.userProfileDao().upsert(
            current.copy(
                xp = newXp,
                level = newLevel,
                coins = newCoins,
                streakDays = newStreak,
                lastActiveEpochDay = today
            )
        )
        evaluateBadgesAndStreak(newStreak)
    }

    suspend fun unlockOrEquipMascotItem(itemId: String, category: String, costCoins: Int): Boolean {
        val current = db.userProfileDao().getProfileOnce() ?: UserProfileEntity()
        val unlocked = current.unlockedItemsCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (!unlocked.contains(itemId)) {
            if (current.coins < costCoins) return false
            unlocked.add(itemId)
        }
        val updated = when (category) {
            "OUTFIT" -> current.copy(
                coins = if (current.unlockedItemsCsv.contains(itemId)) current.coins else current.coins - costCoins,
                equippedOutfit = itemId,
                unlockedItemsCsv = unlocked.joinToString(",")
            )
            "ACCESSORY" -> current.copy(
                coins = if (current.unlockedItemsCsv.contains(itemId)) current.coins else current.coins - costCoins,
                equippedAccessory = itemId,
                unlockedItemsCsv = unlocked.joinToString(",")
            )
            else -> current.copy(
                coins = if (current.unlockedItemsCsv.contains(itemId)) current.coins else current.coins - costCoins,
                equippedRoom = itemId,
                unlockedItemsCsv = unlocked.joinToString(",")
            )
        }
        db.userProfileDao().upsert(updated)
        return true
    }

    suspend fun saveTask(task: TaskEntity, subtasks: List<String>? = null): Long {
        val id = db.taskDao().insertTask(task)
        if (subtasks != null) {
            db.taskDao().deleteSubtasksForTask(id)
            subtasks.filter { it.isNotBlank() }.forEach { title ->
                db.taskDao().insertSubtask(SubtaskEntity(taskId = id, title = title.trim()))
            }
        }
        return id
    }

    suspend fun toggleTaskDone(task: TaskEntity) {
        val nowDone = task.status != "DONE"
        db.taskDao().updateTask(task.copy(status = if (nowDone) "DONE" else "TODO"))
        if (nowDone) {
            awardProgress(xpDelta = 25, coinDelta = 10)
            incrementChallenge("daily_tasks_2", 1)
            unlockAchievement("first_task")
            // Spawn next recurring task if configured
            if (task.recurrenceRule == "DAILY" || task.recurrenceRule == "WEEKLY") {
                val offset = if (task.recurrenceRule == "DAILY") 1L else 7L
                db.taskDao().insertTask(
                    task.copy(
                        id = 0,
                        dateEpochDay = task.dateEpochDay + offset,
                        status = "TODO",
                        actualMinutes = 0,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun toggleSubtask(subtask: SubtaskEntity) {
        db.taskDao().updateSubtask(subtask.copy(isDone = !subtask.isDone))
    }

    suspend fun moveTaskOrder(task: TaskEntity, allDayTasks: List<TaskEntity>, moveUp: Boolean) {
        val sorted = allDayTasks.sortedWith(compareBy({ it.sortOrder }, { it.startTimeMinutes }))
        val idx = sorted.indexOfFirst { it.id == task.id }
        val targetIdx = if (moveUp) idx - 1 else idx + 1
        if (idx in sorted.indices && targetIdx in sorted.indices) {
            val reordered = sorted.toMutableList()
            val item = reordered.removeAt(idx)
            reordered.add(targetIdx, item)
            reordered.forEachIndexed { index, t ->
                db.taskDao().updateTask(t.copy(sortOrder = index))
            }
        }
    }

    suspend fun deleteTask(task: TaskEntity) {
        db.taskDao().deleteSubtasksForTask(task.id)
        db.taskDao().deleteTask(task)
    }

    suspend fun recordFocusSession(session: FocusSessionEntity) {
        db.focusSessionDao().insertSession(session)
        val minutes = max(1, session.actualSeconds / 60)
        if (session.taskId != null) {
            val linked = db.taskDao().getAllTasksOnce().firstOrNull { it.id == session.taskId }
            if (linked != null) {
                db.taskDao().updateTask(linked.copy(actualMinutes = linked.actualMinutes + minutes))
            }
        }
        val xp = minutes * 2
        val coins = max(5, minutes / 2)
        awardProgress(xpDelta = xp, coinDelta = coins)
        incrementChallenge("daily_focus_30", minutes)
        incrementChallenge("weekly_sessions_5", 1)
        unlockAchievement("focus_starter")
        val totalMinutes = db.focusSessionDao().getAllSessionsOnce().sumOf { it.actualSeconds / 60 }
        if (totalMinutes >= 180) unlockAchievement("focus_master")
    }

    suspend fun saveNote(note: NoteEntity): Long {
        val id = db.noteDao().insertNote(note.copy(updatedAt = System.currentTimeMillis()))
        val count = db.noteDao().getAllNotesOnce().size
        if (count >= 3) unlockAchievement("note_taker")
        return id
    }

    suspend fun deleteNote(note: NoteEntity) = db.noteDao().deleteNote(note)

    suspend fun markLessonCompleted(lessonId: String, subjectId: String, completed: Boolean) {
        val existing = db.academyDao().getProgressOnce().firstOrNull { it.lessonId == lessonId }
        db.academyDao().upsertProgress(
            (existing ?: AcademyProgressEntity(lessonId = lessonId, subjectId = subjectId))
                .copy(isCompleted = completed, updatedAt = System.currentTimeMillis())
        )
        if (completed) {
            awardProgress(xpDelta = 20, coinDelta = 10)
            checkAcademyBadge()
        }
    }

    suspend fun toggleLessonBookmark(lessonId: String, subjectId: String) {
        val existing = db.academyDao().getProgressOnce().firstOrNull { it.lessonId == lessonId }
        val current = existing ?: AcademyProgressEntity(lessonId = lessonId, subjectId = subjectId)
        db.academyDao().upsertProgress(
            current.copy(isBookmarked = !current.isBookmarked, updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun recordQuizAttempt(subjectId: String, lessonId: String, score: Int, total: Int) {
        db.academyDao().insertQuizAttempt(
            QuizAttemptEntity(subjectId = subjectId, lessonId = lessonId, score = score, totalQuestions = total)
        )
        val passed = total > 0 && score * 2 >= total
        if (!passed) {
            val existing = db.academyDao().getProgressOnce().firstOrNull { it.lessonId == lessonId }
            val current = existing ?: AcademyProgressEntity(lessonId = lessonId, subjectId = subjectId)
            db.academyDao().upsertProgress(current.copy(weakScoreCount = current.weakScoreCount + 1))
        } else {
            incrementChallenge("weekly_quiz_2", 1)
            awardProgress(xpDelta = 30, coinDelta = 15)
            checkAcademyBadge()
        }
    }

    suspend fun updateFlashcardMastery(card: FlashcardEntity, known: Boolean) {
        val today = JalaliCalendar.todayEpochDay()
        val nextLevel = if (known) (card.masteryLevel + 1).coerceAtMost(5) else 0
        val intervalDays = when (nextLevel) {
            0 -> 0L
            1 -> 1L
            2 -> 3L
            3 -> 7L
            4 -> 14L
            else -> 30L
        }
        db.academyDao().updateFlashcard(
            card.copy(masteryLevel = nextLevel, nextReviewEpochDay = today + intervalDays)
        )
        if (known) awardProgress(xpDelta = 5, coinDelta = 2)
    }

    suspend fun addCustomLesson(subjectId: String, title: String, body: String, attachmentUri: String?) {
        db.academyDao().insertCustomLesson(
            CustomLessonEntity(subjectId = subjectId, title = title.trim(), body = body.trim(), attachmentUri = attachmentUri)
        )
    }

    suspend fun deleteCustomLesson(lesson: CustomLessonEntity) =
        db.academyDao().deleteCustomLesson(lesson)

    suspend fun createStudyRoom(room: StudyRoomEntity): Long = db.studyRoomDao().insertRoom(room)
    suspend fun updateStudyRoom(room: StudyRoomEntity) = db.studyRoomDao().updateRoom(room)
    suspend fun deleteStudyRoom(room: StudyRoomEntity) = db.studyRoomDao().deleteRoom(room)
    suspend fun addStudyRoomTask(task: StudyRoomTaskEntity) = db.studyRoomDao().insertRoomTask(task)
    suspend fun toggleStudyRoomTask(task: StudyRoomTaskEntity) =
        db.studyRoomDao().updateRoomTask(task.copy(isDone = !task.isDone))
    suspend fun deleteRoomTask(task: StudyRoomTaskEntity) = db.studyRoomDao().deleteRoomTask(task)
    suspend fun deleteStudyRoomTask(task: StudyRoomTaskEntity) = db.studyRoomDao().deleteRoomTask(task)

    suspend fun setAppBlocked(packageName: String, appLabel: String, blocked: Boolean) {
        if (blocked) {
            db.blockedAppDao().upsertBlockedApp(BlockedAppEntity(packageName, appLabel, true))
        } else {
            db.blockedAppDao().deleteBlockedApp(packageName)
        }
    }

    suspend fun createConversation(assistantType: String, title: String, modelId: String): Long =
        db.aiChatDao().insertConversation(
            ConversationEntity(assistantType = assistantType, title = title, modelId = modelId)
        )

    suspend fun addChatMessage(
        conversationId: Long,
        role: String,
        content: String,
        modelUsed: String,
        attachmentSummary: String? = null
    ): Long {
        val id = db.aiChatDao().insertMessage(
            ChatMessageEntity(
                conversationId = conversationId,
                role = role,
                content = content,
                modelUsed = modelUsed,
                attachmentSummary = attachmentSummary
            )
        )
        val convs = db.aiChatDao().observeConversations()
        // Update timestamp via direct query isn't needed if we update entity; handled in ViewModel if title changes
        return id
    }

    suspend fun updateConversation(conv: ConversationEntity) = db.aiChatDao().updateConversation(conv)
    suspend fun deleteConversation(id: Long) = db.aiChatDao().deleteConversation(id)
    suspend fun clearConversationMessages(id: Long) = db.aiChatDao().clearMessages(id)
    suspend fun deleteChatMessage(id: Long) = db.aiChatDao().deleteMessageById(id)
    suspend fun getMessagesOnce(id: Long) = db.aiChatDao().getMessagesOnce(id)

    suspend fun claimChallenge(challenge: ChallengeEntity) {
        if (!challenge.isClaimed && challenge.currentCount >= challenge.targetCount) {
            db.gamificationDao().updateChallenge(challenge.copy(isClaimed = true))
            awardProgress(challenge.xpReward, challenge.coinReward)
        }
    }

    private suspend fun incrementChallenge(id: String, delta: Int) {
        val item = db.gamificationDao().getChallengesOnce().firstOrNull { it.id == id } ?: return
        db.gamificationDao().updateChallenge(
            item.copy(currentCount = (item.currentCount + delta).coerceAtMost(item.targetCount))
        )
    }

    private suspend fun unlockAchievement(id: String) {
        val item = db.gamificationDao().getAchievementsOnce().firstOrNull { it.id == id } ?: return
        if (!item.isUnlocked) {
            db.gamificationDao().updateAchievement(
                item.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis())
            )
            val profile = db.userProfileDao().getProfileOnce() ?: UserProfileEntity()
            val newXp = profile.xp + item.xpReward
            db.userProfileDao().upsert(
                profile.copy(
                    xp = newXp,
                    level = 1 + (newXp / 150),
                    coins = profile.coins + item.coinReward
                )
            )
        }
    }

    private suspend fun checkAcademyBadge() {
        val completed = db.academyDao().getProgressOnce().count { it.isCompleted }
        if (completed >= 2) unlockAchievement("academy_scholar")
    }

    private suspend fun evaluateBadgesAndStreak(streakDays: Int) {
        if (streakDays >= 3) unlockAchievement("streak_3")
    }
}
