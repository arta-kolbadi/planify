package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        CategoryEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
        FocusSessionEntity::class,
        NoteEntity::class,
        ConversationEntity::class,
        ChatMessageEntity::class,
        AcademyProgressEntity::class,
        QuizAttemptEntity::class,
        FlashcardEntity::class,
        CustomLessonEntity::class,
        StudyRoomEntity::class,
        StudyRoomTaskEntity::class,
        AchievementEntity::class,
        ChallengeEntity::class,
        BlockedAppEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun noteDao(): NoteDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun academyDao(): AcademyDao
    abstract fun studyRoomDao(): StudyRoomDao
    abstract fun gamificationDao(): GamificationDao
    abstract fun blockedAppDao(): BlockedAppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "planify_local.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                seedInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        suspend fun seedInitialData(db: AppDatabase) {
            if (db.userProfileDao().getProfileOnce() == null) {
                db.userProfileDao().upsert(UserProfileEntity())
            }
            if (db.categoryDao().getAllOnce().isEmpty()) {
                db.categoryDao().insertAll(
                    listOf(
                        CategoryEntity(nameEn = "Study", nameFa = "مطالعه", colorHex = 0xFF168BFF),
                        CategoryEntity(nameEn = "Exam Prep", nameFa = "آمادگی آزمون", colorHex = 0xFF20D4E8),
                        CategoryEntity(nameEn = "Project", nameFa = "پروژه", colorHex = 0xFF20B879),
                        CategoryEntity(nameEn = "Reading", nameFa = "کتاب‌خوانی", colorHex = 0xFFF5A623),
                        CategoryEntity(nameEn = "Personal", nameFa = "شخصی", colorHex = 0xFFEF5350)
                    )
                )
            }
            if (db.gamificationDao().getAchievementsOnce().isEmpty()) {
                db.gamificationDao().insertAchievements(
                    listOf(
                        AchievementEntity(
                            id = "first_task",
                            titleEn = "First Step",
                            titleFa = "اولین قدم",
                            descEn = "Complete your first planned task.",
                            descFa = "اولین وظیفه برنامه‌ریزی‌شده خود را تکمیل کنید.",
                            xpReward = 50,
                            coinReward = 20
                        ),
                        AchievementEntity(
                            id = "focus_starter",
                            titleEn = "Deep Diver",
                            titleFa = "شروع تمرکز عمیق",
                            descEn = "Complete 1 focus session.",
                            descFa = "یک جلسه تمرکز را به پایان برسانید.",
                            xpReward = 60,
                            coinReward = 25
                        ),
                        AchievementEntity(
                            id = "note_taker",
                            titleEn = "Knowledge Keeper",
                            titleFa = "نگهدارنده دانش",
                            descEn = "Write 3 study notes or journal entries.",
                            descFa = "۳ یادداشت یا ژورنال روزانه ثبت کنید.",
                            xpReward = 70,
                            coinReward = 30
                        ),
                        AchievementEntity(
                            id = "academy_scholar",
                            titleEn = "Academy Scholar",
                            titleFa = "پژوهشگر آکادمی",
                            descEn = "Complete 2 Academy lessons or quizzes.",
                            descFa = "۲ درس یا آزمون آکادمی را تکمیل کنید.",
                            xpReward = 100,
                            coinReward = 45
                        ),
                        AchievementEntity(
                            id = "streak_3",
                            titleEn = "3-Day Momentum",
                            titleFa = "زنجیره ۳ روزه",
                            descEn = "Maintain a 3-day study streak.",
                            descFa = "۳ روز پیاپی فعالیت مطالعاتی داشته باشید.",
                            xpReward = 120,
                            coinReward = 60
                        ),
                        AchievementEntity(
                            id = "focus_master",
                            titleEn = "Focus Master",
                            titleFa = "استاد تمرکز",
                            descEn = "Accumulate 180 minutes of focus.",
                            descFa = "در مجموع ۱۸۰ دقیقه تمرکز ثبت کنید.",
                            xpReward = 200,
                            coinReward = 100
                        )
                    )
                )
            }
            if (db.gamificationDao().getChallengesOnce().isEmpty()) {
                db.gamificationDao().insertChallenges(
                    listOf(
                        ChallengeEntity(
                            id = "daily_tasks_2",
                            titleEn = "Complete 2 tasks today",
                            titleFa = "تکمیل ۲ وظیفه امروز",
                            isWeekly = false,
                            targetCount = 2,
                            xpReward = 40,
                            coinReward = 20
                        ),
                        ChallengeEntity(
                            id = "daily_focus_30",
                            titleEn = "Focus for 30 minutes",
                            titleFa = "۳۰ دقیقه تمرکز روزانه",
                            isWeekly = false,
                            targetCount = 30,
                            xpReward = 50,
                            coinReward = 25
                        ),
                        ChallengeEntity(
                            id = "weekly_sessions_5",
                            titleEn = "Finish 5 focus sessions this week",
                            titleFa = "اتمام ۵ جلسه تمرکز در هفته",
                            isWeekly = true,
                            targetCount = 5,
                            xpReward = 150,
                            coinReward = 75
                        ),
                        ChallengeEntity(
                            id = "weekly_quiz_2",
                            titleEn = "Pass 2 Academy quizzes",
                            titleFa = "قبولی در ۲ آزمون آکادمی",
                            isWeekly = true,
                            targetCount = 2,
                            xpReward = 120,
                            coinReward = 60
                        )
                    )
                )
            }
            if (db.academyDao().getFlashcardsOnce().isEmpty()) {
                db.academyDao().insertFlashcards(
                    listOf(
                        FlashcardEntity(
                            subjectId = "math",
                            frontEn = "Pythagorean Theorem",
                            backEn = "In a right triangle, a² + b² = c² where c is the hypotenuse.",
                            frontFa = "قضیه فیثاغورس",
                            backFa = "در مثلث قائم‌الزاویه، مربع وتر برابر با مجموع مربعات دو ضلع دیگر است: a² + b² = c²"
                        ),
                        FlashcardEntity(
                            subjectId = "math",
                            frontEn = "Derivative of xⁿ",
                            backEn = "d/dx (xⁿ) = n · xⁿ⁻¹ (Power Rule)",
                            frontFa = "مشتق xⁿ",
                            backFa = "طبق قاعده توان: d/dx (xⁿ) = n · xⁿ⁻¹"
                        ),
                        FlashcardEntity(
                            subjectId = "physics",
                            frontEn = "Newton's Second Law",
                            backEn = "F = m · a (Net force equals mass times acceleration)",
                            frontFa = "قانون دوم نیوتن",
                            backFa = "نیروی خالص وارد بر جسم برابر است با حاصل‌ضرب جرم در شتاب: F = m · a"
                        ),
                        FlashcardEntity(
                            subjectId = "physics",
                            frontEn = "Ohm's Law",
                            backEn = "V = I · R (Voltage = Current × Resistance)",
                            frontFa = "قانون اهم",
                            backFa = "اختلاف پتانسیل دو سر رسانا برابر است با شدت جریان ضربدر مقاومت: V = I · R"
                        ),
                        FlashcardEntity(
                            subjectId = "cs",
                            frontEn = "Binary Search Time Complexity",
                            backEn = "O(log n) on a sorted array by halving the search interval each step.",
                            frontFa = "پیچیدگی زمانی جستجوی دودویی",
                            backFa = "O(log n) روی آرایه مرتب‌شده با نصف‌کردن بازه در هر مرحله"
                        ),
                        FlashcardEntity(
                            subjectId = "english",
                            frontEn = "Active Recall",
                            backEn = "Testing yourself from memory instead of passively re-reading notes.",
                            frontFa = "بازیابی فعال (Active Recall)",
                            backFa = "آزمون‌گرفتن از خود از حافظه به‌جای روخوانی منفعلانه جزوه"
                        )
                    )
                )
            }
        }
    }
}
