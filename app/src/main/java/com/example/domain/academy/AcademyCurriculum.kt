package com.example.domain.academy

import com.example.core.i18n.AppLanguage

data class AcademyGrade(val id: String, val nameEn: String, val nameFa: String)

data class AcademySubject(
    val id: String,
    val gradeId: String,
    val nameEn: String,
    val nameFa: String,
    val accentHex: Long
)

data class AcademyChapter(
    val id: String,
    val subjectId: String,
    val titleEn: String,
    val titleFa: String
)

data class AcademyExercise(
    val promptEn: String,
    val promptFa: String,
    val hintsEn: List<String>,
    val hintsFa: List<String>,
    val expectedAnswer: String
)

data class AcademyQuizQuestion(
    val questionEn: String,
    val questionFa: String,
    val optionsEn: List<String>,
    val optionsFa: List<String>,
    val correctIndex: Int
)

data class AcademyLesson(
    val id: String,
    val chapterId: String,
    val subjectId: String,
    val titleEn: String,
    val titleFa: String,
    val stepsEn: List<String>,
    val stepsFa: List<String>,
    val examTipEn: String,
    val examTipFa: String,
    val exercise: AcademyExercise,
    val quiz: List<AcademyQuizQuestion>
)

object AcademyCurriculum {
    val grades = listOf(
        AcademyGrade("g10", "Grade 10 • Foundations", "پایه دهم • مبانی"),
        AcademyGrade("g11", "Grade 11 • Core STEM", "پایه یازدهم • تخصصی"),
        AcademyGrade("g12", "Grade 12 • Exam Prep", "پایه دوازدهم • کنکور و نهایی")
    )

    val subjects = listOf(
        AcademySubject("math", "g10", "Mathematics", "ریاضیات", 0xFF168BFF),
        AcademySubject("physics", "g11", "Physics", "فیزیک", 0xFF20D4E8),
        AcademySubject("cs", "g11", "Computer Science", "علوم کامپیوتر", 0xFF20B879),
        AcademySubject("english", "g12", "Study & Exam Strategy", "مهارت‌های مطالعه و آزمون", 0xFFF5A623)
    )

    val chapters = listOf(
        AcademyChapter("ch_alg", "math", "Quadratic Equations & Functions", "معادلات درجه دوم و توابع"),
        AcademyChapter("ch_calc", "math", "Derivatives & Rates of Change", "مشتق و نرخ تغییر"),
        AcademyChapter("ch_mech", "physics", "Laws of Motion & Dynamics", "قوانین حرکت و دینامیک"),
        AcademyChapter("ch_algo", "cs", "Algorithms & Complexity", "الگوریتم‌ها و پیچیدگی زمانی"),
        AcademyChapter("ch_prep", "english", "Memory & Exam Mastery", "تکنیک‌های حافظه و مدیریت آزمون")
    )

    val lessons = listOf(
        AcademyLesson(
            id = "lesson_quad",
            chapterId = "ch_alg",
            subjectId = "math",
            titleEn = "Solving Quadratic Equations with the Discriminant",
            titleFa = "حل معادله درجه دوم با دلتا",
            stepsEn = listOf(
                "1. Write the equation in standard form: ax² + bx + c = 0 (with a ≠ 0).",
                "2. Compute the discriminant: Δ = b² - 4ac.",
                "3. If Δ > 0, there are two distinct real roots: x = (-b ± √Δ) / (2a).",
                "4. If Δ = 0, there is one repeated root: x = -b / (2a). If Δ < 0, no real roots exist."
            ),
            stepsFa = listOf(
                "۱. ابتدا معادله را به فرم استاندارد ax² + bx + c = 0 مرتب کنید.",
                "۲. مقدار مبین (دلتا) را محاسبه کنید: Δ = b² - 4ac.",
                "۳. اگر Δ > 0 باشد دو ریشه حقیقی متمایز داریم: x = (-b ± √Δ) / (2a).",
                "۴. اگر Δ = 0 باشد یک ریشه مضاعف داریم و اگر Δ < 0 باشد ریشه حقیقی ندارد."
            ),
            examTipEn = "Exam Tip: Before computing Δ, check if a + b + c = 0 (then x₁ = 1, x₂ = c/a) to save 30 seconds!",
            examTipFa = "نکته امتحانی: قبل از محاسبه دلتا بررسی کنید آیا a + b + c = 0 است؛ در این صورت یکی از ریشه‌ها ۱ و دیگری c/a است.",
            exercise = AcademyExercise(
                promptEn = "Find the positive root of x² - 5x + 6 = 0.",
                promptFa = "ریشه بزرگ‌تر معادله x² - 5x + 6 = 0 کدام عدد است؟",
                hintsEn = listOf(
                    "Identify a = 1, b = -5, c = 6.",
                    "Compute Δ = (-5)² - 4(1)(6) = 25 - 24 = 1.",
                    "The roots are (5 + 1)/2 and (5 - 1)/2."
                ),
                hintsFa = listOf(
                    "ضرایب برابرند با: a = 1 ، b = -5 و c = 6.",
                    "دلتا برابر است با: Δ = 25 - 24 = 1.",
                    "ریشه‌ها (5+1)/2 و (5-1)/2 هستند."
                ),
                expectedAnswer = "3"
            ),
            quiz = listOf(
                AcademyQuizQuestion(
                    questionEn = "If Δ = 0 in ax² + bx + c = 0, how many real roots exist?",
                    questionFa = "اگر دلتا در معادله درجه دوم صفر شود، چند ریشه حقیقی وجود دارد؟",
                    optionsEn = listOf("Zero", "One repeated root", "Two distinct roots", "Infinite"),
                    optionsFa = listOf("صفر", "یک ریشه مضاعف", "دو ریشه متمایز", "بی‌نهایت"),
                    correctIndex = 1
                ),
                AcademyQuizQuestion(
                    questionEn = "What is Δ for 2x² + 4x + 2 = 0?",
                    questionFa = "مقدار دلتا برای معادله 2x² + 4x + 2 = 0 چقدر است؟",
                    optionsEn = listOf("0", "8", "16", "-8"),
                    optionsFa = listOf("۰", "۸", "۱۶", "-۸"),
                    correctIndex = 0
                )
            )
        ),
        AcademyLesson(
            id = "lesson_deriv",
            chapterId = "ch_calc",
            subjectId = "math",
            titleEn = "Power Rule & Tangent Slope",
            titleFa = "قاعده توان و شیب خط مماس",
            stepsEn = listOf(
                "1. The derivative f'(x) measures the instantaneous rate of change (slope of tangent).",
                "2. Power Rule: For f(x) = c · xⁿ, f'(x) = c · n · xⁿ⁻¹.",
                "3. The derivative of any constant is 0."
            ),
            stepsFa = listOf(
                "۱. مشتق f'(x) نشان‌دهنده نرخ تغییر لحظه‌ای یا شیب خط مماس در هر نقطه است.",
                "۲. قاعده توان: مشتق c · xⁿ برابر است با c · n · xⁿ⁻¹.",
                "۳. مشتق هر عدد ثابت برابر با صفر است."
            ),
            examTipEn = "Always rewrite roots like √x as x^(1/2) before applying the Power Rule.",
            examTipFa = "در آزمون‌ها ابتدا رادیکال‌ها مثل √x را به صورت توان کسری x^(1/2) بنویسید و سپس مشتق بگیرید.",
            exercise = AcademyExercise(
                promptEn = "For f(x) = 3x² + 2x, what is f'(2)?",
                promptFa = "اگر f(x) = 3x² + 2x باشد، مقدار f'(2) چقدر است؟",
                hintsEn = listOf(
                    "Apply the power rule to 3x² -> 6x, and 2x -> 2.",
                    "Substitute x = 2 into f'(x) = 6x + 2."
                ),
                hintsFa = listOf(
                    "مشتق تابع برابر است با f'(x) = 6x + 2.",
                    "کافی است x = 2 را در عبارت مشتق قرار دهید."
                ),
                expectedAnswer = "14"
            ),
            quiz = listOf(
                AcademyQuizQuestion(
                    questionEn = "What is the derivative of f(x) = 5x³?",
                    questionFa = "مشتق تابع f(x) = 5x³ کدام است؟",
                    optionsEn = listOf("15x²", "5x²", "15x³", "3x²"),
                    optionsFa = listOf("15x²", "5x²", "15x³", "3x²"),
                    correctIndex = 0
                )
            )
        ),
        AcademyLesson(
            id = "lesson_newton",
            chapterId = "ch_mech",
            subjectId = "physics",
            titleEn = "Newton's Second Law in Action",
            titleFa = "کاربرد قانون دوم نیوتن",
            stepsEn = listOf(
                "1. Draw a free-body diagram showing all forces acting on the object.",
                "2. Find the net force ΣF along the axis of motion.",
                "3. Apply ΣF = m · a (Force in Newtons, mass in kg, acceleration in m/s²)."
            ),
            stepsFa = listOf(
                "۱. ابتدا نمودار آزاد جسم و تمام نیروهای وارد بر آن را رسم کنید.",
                "۲. برآیند نیروها (ΣF) را در راستای حرکت به‌دست آورید.",
                "۳. رابطه ΣF = m · a را بنویسید (نیرو برحسب نیوتن و جرم برحسب کیلوگرم)."
            ),
            examTipEn = "Remember to convert grams to kilograms (divide by 1000) before multiplying!",
            examTipFa = "در مسائل امتحانی حتماً پیش از جایگذاری، گرم را به کیلوگرم تبدیل کنید.",
            exercise = AcademyExercise(
                promptEn = "A 4 kg box experiences a net force of 20 N. What is its acceleration (m/s²)?",
                promptFa = "به جعبه‌ای به جرم ۴ کیلوگرم نیروی خالص ۲۰ نیوتن وارد می‌شود. شتاب آن چند متر بر مجذور ثانیه است؟",
                hintsEn = listOf(
                    "Rearrange F = m · a to a = F / m.",
                    "Divide 20 N by 4 kg."
                ),
                hintsFa = listOf(
                    "از رابطه a = F / m استفاده کنید.",
                    "۲۰ نیوتن را بر ۴ کیلوگرم تقسیم کنید."
                ),
                expectedAnswer = "5"
            ),
            quiz = listOf(
                AcademyQuizQuestion(
                    questionEn = "What is the SI unit of Force?",
                    questionFa = "یکای نیرو در سیستم بین‌المللی (SI) چیست؟",
                    optionsEn = listOf("Joule", "Watt", "Newton", "Pascal"),
                    optionsFa = listOf("ژول", "وات", "نیوتن", "پاسکال"),
                    correctIndex = 2
                )
            )
        ),
        AcademyLesson(
            id = "lesson_algo",
            chapterId = "ch_algo",
            subjectId = "cs",
            titleEn = "Big-O Notation & Binary Search",
            titleFa = "نماد Big-O و جستجوی دودویی",
            stepsEn = listOf(
                "1. Big-O describes how runtime grows as input size n increases.",
                "2. Linear scan is O(n); Binary Search on sorted data is O(log n).",
                "3. Binary Search checks the midpoint and discards half of the remaining items each step."
            ),
            stepsFa = listOf(
                "۱. نماد Big-O نشان می‌دهد با بزرگ‌شدن ورودی n، زمان اجرا چطور رشد می‌کند.",
                "۲. پیمایش خطی O(n) و جستجوی دودویی روی آرایه مرتب O(log n) است.",
                "۳. در جستجوی دودویی هر بار عنصر میانی بررسی و نیمی از بازه حذف می‌شود."
            ),
            examTipEn = "Binary Search only works reliably when the collection is already sorted!",
            examTipFa = "شرط اصلی استفاده از جستجوی دودویی، مرتب‌بودن داده‌ها پیش از جستجو است.",
            exercise = AcademyExercise(
                promptEn = "At most how many comparisons does Binary Search need on 16 sorted items? (log₂ 16 + 1 = 5, or log₂ 16 = ?)",
                promptFa = "مقدار log₂(16) برابر با چند است؟",
                hintsEn = listOf("Ask: 2 raised to what power equals 16?"),
                hintsFa = listOf("۲ به توان چه عددی برابر ۱۶ می‌شود؟"),
                expectedAnswer = "4"
            ),
            quiz = listOf(
                AcademyQuizQuestion(
                    questionEn = "Which complexity grows slowest for large n?",
                    questionFa = "کدام پیچیدگی زمانی برای داده‌های بسیار بزرگ سریع‌تر است؟",
                    optionsEn = listOf("O(n²)", "O(n)", "O(log n)", "O(2ⁿ)"),
                    optionsFa = listOf("O(n²)", "O(n)", "O(log n)", "O(2ⁿ)"),
                    correctIndex = 2
                )
            )
        ),
        AcademyLesson(
            id = "lesson_recall",
            chapterId = "ch_prep",
            subjectId = "english",
            titleEn = "Active Recall & Spaced Repetition",
            titleFa = "بازیابی فعال و مرور فاصله‌دار",
            stepsEn = listOf(
                "1. Close your textbook after reading a page and write down everything you remember.",
                "2. Check what you missed and focus only on the gaps.",
                "3. Review flashcards at expanding intervals: Day 1, Day 3, Day 7, Day 14."
            ),
            stepsFa = listOf(
                "۱. پس از مطالعه هر بخش کتاب را ببندید و هرچه به یاد دارید بنویسید.",
                "۲. بخش‌های فراموش‌شده را علامت بزنید و دوباره مرور کنید.",
                "۳. فلش‌کارت‌ها را در بازه‌های ۱، ۳، ۷ و ۱۴ روزه مرور کنید تا در حافظه بلندمدت بمانند."
            ),
            examTipEn = "Pair 25-minute Pomodoro sprints with 5-minute active recall quizzes.",
            examTipFa = "بهترین ترکیب: ۲۵ دقیقه مطالعه متمرکز و سپس ۵ دقیقه تست‌زنی یا بازیابی فعال.",
            exercise = AcademyExercise(
                promptEn = "After Day 1 and Day 3, what is the next standard review interval day in the 1-3-7-14 pattern?",
                promptFa = "در الگوی مرور ۱-۳-۷-۱۴، بعد از روز اول و سوم، نوبت مرور چندم (روز چندم) است؟",
                hintsEn = listOf("Follow the sequence: 1, 3, ?, 14."),
                hintsFa = listOf("الگوی اعداد را ادامه دهید: ۱، ۳، ؟، ۱۴."),
                expectedAnswer = "7"
            ),
            quiz = listOf(
                AcademyQuizQuestion(
                    questionEn = "Which study method builds stronger long-term memory?",
                    questionFa = "کدام روش مطالعه ماندگاری بیشتری در حافظه بلندمدت ایجاد می‌کند؟",
                    optionsEn = listOf("Re-reading silently 5 times", "Active Recall + Spaced Repetition", "Highlighting every sentence", "Cramming at 2 AM"),
                    optionsFa = listOf("روخوانی تکراری بدون آزمون", "بازیابی فعال و مرور فاصله‌دار", "هایلایت کردن تمام متن", "شب‌امتحانی خواندن"),
                    correctIndex = 1
                )
            )
        )
    )

    fun subjectName(subjectId: String, language: AppLanguage): String {
        val subj = subjects.firstOrNull { it.id == subjectId } ?: return subjectId
        return if (language == AppLanguage.FA) subj.nameFa else subj.nameEn
    }
}
