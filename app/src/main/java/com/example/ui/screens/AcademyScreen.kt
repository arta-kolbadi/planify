package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.local.AcademyProgressEntity
import com.example.data.local.CustomLessonEntity
import com.example.data.local.FlashcardEntity
import com.example.data.local.QuizAttemptEntity
import com.example.domain.academy.AcademyCurriculum
import com.example.domain.academy.AcademyLesson
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PlanifyCard
import com.example.ui.theme.LocalPlanifyColors

@Composable
fun AcademyScreen(
    progressList: List<AcademyProgressEntity>,
    quizAttempts: List<QuizAttemptEntity>,
    flashcards: List<FlashcardEntity>,
    customLessons: List<CustomLessonEntity>,
    onMarkLessonComplete: (String, String, Boolean) -> Unit,
    onToggleBookmark: (String, String) -> Unit,
    onRecordQuizAttempt: (String, String, Int, Int) -> Unit,
    onAnswerFlashcard: (FlashcardEntity, Boolean) -> Unit,
    onAddCustomLesson: (String, String, String, String?) -> Unit,
    onDeleteCustomLesson: (CustomLessonEntity) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val extra = LocalPlanifyColors.current

    var mainTab by rememberSaveable { mutableIntStateOf(0) } // 0=Hamyar.me, 1=Lessons, 2=Flashcards, 3=Weak/Bookmarks
    var hamyarUrl by rememberSaveable { mutableStateOf(HAMYAR_HOME_URL) }
    var selectedGradeId by remember { mutableStateOf<String?>(null) }
    var selectedSubjectId by remember { mutableStateOf("math") }
    var openedLesson by remember { mutableStateOf<AcademyLesson?>(null) }
    var showAddMaterialDialog by remember { mutableStateOf(false) }

    val savedHamyarBookmarks = remember(customLessons) {
        customLessons.filter { it.body.trim().startsWith("http://") || it.body.trim().startsWith("https://") }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (mainTab == 1 && openedLesson == null) {
                FloatingActionButton(
                    onClick = { showAddMaterialDialog = true },
                    modifier = Modifier.testTag("academy_add_material_fab")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = strings.academyAddMaterial)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (openedLesson != null) {
                BackHandler { openedLesson = null }
                LessonDetailView(
                    lesson = openedLesson!!,
                    progress = progressList.firstOrNull { it.lessonId == openedLesson!!.id },
                    onBack = { openedLesson = null },
                    onMarkComplete = { completed ->
                        onMarkLessonComplete(openedLesson!!.id, openedLesson!!.subjectId, completed)
                    },
                    onToggleBookmark = {
                        onToggleBookmark(openedLesson!!.id, openedLesson!!.subjectId)
                    },
                    onSubmitQuiz = { score, total ->
                        onRecordQuizAttempt(openedLesson!!.subjectId, openedLesson!!.id, score, total)
                    }
                )
            } else {
                ScrollableTabRow(
                    selectedTabIndex = mainTab,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
                ) {
                    Tab(
                        selected = mainTab == 0,
                        onClick = { mainTab = 0 },
                        text = { Text(strings.academyHamyarTab) },
                        modifier = Modifier.testTag("academy_tab_hamyar")
                    )
                    Tab(
                        selected = mainTab == 1,
                        onClick = { mainTab = 1 },
                        text = { Text(strings.academyLessons) },
                        modifier = Modifier.testTag("academy_tab_lessons")
                    )
                    Tab(
                        selected = mainTab == 2,
                        onClick = { mainTab = 2 },
                        text = { Text(strings.academyFlashcards) },
                        modifier = Modifier.testTag("academy_tab_flashcards")
                    )
                    Tab(
                        selected = mainTab == 3,
                        onClick = { mainTab = 3 },
                        text = { Text(strings.academyWeakPoints) },
                        modifier = Modifier.testTag("academy_tab_weak")
                    )
                }

                when (mainTab) {
                    0 -> {
                        HamyarPortalView(
                            currentUrl = hamyarUrl,
                            onUrlChanged = { hamyarUrl = it },
                            savedHamyarBookmarks = savedHamyarBookmarks,
                            onSaveBookmark = { title, url ->
                                if (savedHamyarBookmarks.none { it.body.trim() == url.trim() }) {
                                    onAddCustomLesson(selectedSubjectId, title, url, null)
                                }
                            },
                            onOpenOfflineLessons = { mainTab = 1 }
                        )
                    }

                    1 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Grade Filter Row
                            item {
                                Text(strings.academyGrades, style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedGradeId == null,
                                        onClick = { selectedGradeId = null },
                                        label = { Text("All") }
                                    )
                                    AcademyCurriculum.grades.forEach { g ->
                                        FilterChip(
                                            selected = selectedGradeId == g.id,
                                            onClick = { selectedGradeId = g.id },
                                            label = { Text(if (lang == AppLanguage.FA) g.nameFa else g.nameEn) }
                                        )
                                    }
                                }
                            }

                            // Subjects Row
                            item {
                                Text(strings.academySubjects, style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.height(6.dp))
                                val visibleSubjects = AcademyCurriculum.subjects.filter {
                                    selectedGradeId == null || it.gradeId == selectedGradeId
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    visibleSubjects.forEach { s ->
                                        FilterChip(
                                            selected = selectedSubjectId == s.id,
                                            onClick = { selectedSubjectId = s.id },
                                            label = { Text(if (lang == AppLanguage.FA) s.nameFa else s.nameEn) }
                                        )
                                    }
                                }
                            }

                            // Chapters & Lessons
                            val subjectChapters = AcademyCurriculum.chapters.filter { it.subjectId == selectedSubjectId }
                            subjectChapters.forEach { chapter ->
                                item(key = "ch_${chapter.id}") {
                                    Text(
                                        text = if (lang == AppLanguage.FA) chapter.titleFa else chapter.titleEn,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                val chapLessons = AcademyCurriculum.lessons.filter { it.chapterId == chapter.id }
                                items(chapLessons, key = { it.id }) { lesson ->
                                    val prog = progressList.firstOrNull { it.lessonId == lesson.id }
                                    PlanifyCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { openedLesson = lesson }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (prog?.isCompleted == true) Icons.Rounded.CheckCircle else Icons.Rounded.School,
                                                contentDescription = null,
                                                tint = if (prog?.isCompleted == true) extra.success else MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (lang == AppLanguage.FA) lesson.titleFa else lesson.titleEn,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = if (prog?.isCompleted == true) strings.academyCompleted else "${strings.academyStepByStep} • ${strings.academyExercises} • ${strings.academyQuizzes}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(onClick = { onToggleBookmark(lesson.id, lesson.subjectId) }) {
                                                Icon(
                                                    imageVector = if (prog?.isBookmarked == true) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                                    contentDescription = strings.academyBookmarks,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // User Added Study Materials & Saved Hamyar Pages
                            val myMaterials = customLessons.filter {
                                it.subjectId == selectedSubjectId ||
                                    it.body.trim().startsWith("http://") ||
                                    it.body.trim().startsWith("https://")
                            }
                            if (myMaterials.isNotEmpty()) {
                                item {
                                    Text(strings.academyAddMaterial, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                items(myMaterials, key = { "custom_${it.id}" }) { mat ->
                                    val isUrl = mat.body.trim().startsWith("http://") || mat.body.trim().startsWith("https://")
                                    PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = mat.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(onClick = { onDeleteCustomLesson(mat) }) {
                                                    Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = extra.danger, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            Text(mat.body, style = MaterialTheme.typography.bodySmall)
                                            if (isUrl) {
                                                OutlinedButton(
                                                    onClick = {
                                                        hamyarUrl = mat.body.trim()
                                                        mainTab = 0
                                                    }
                                                ) {
                                                    Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(strings.academyHamyarOpenSaved)
                                                }
                                            }
                                            if (!mat.attachmentUri.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = Uri.parse(mat.attachmentUri),
                                                    contentDescription = mat.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(140.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        FlashcardsDeckView(
                            flashcards = flashcards,
                            onAnswer = onAnswerFlashcard
                        )
                    }

                    3 -> {
                        WeakPointsAndBookmarksView(
                            progressList = progressList,
                            quizAttempts = quizAttempts,
                            onOpenLesson = { openedLesson = it }
                        )
                    }
                }
            }
        }
    }

    if (showAddMaterialDialog) {
        AddCustomMaterialDialog(
            subjectId = selectedSubjectId,
            onDismiss = { showAddMaterialDialog = false },
            onSave = { title, body, uri ->
                onAddCustomLesson(selectedSubjectId, title, body, uri)
                showAddMaterialDialog = false
            }
        )
    }
}

@Composable
private fun LessonDetailView(
    lesson: AcademyLesson,
    progress: AcademyProgressEntity?,
    onBack: () -> Unit,
    onMarkComplete: (Boolean) -> Unit,
    onToggleBookmark: () -> Unit,
    onSubmitQuiz: (Int, Int) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val extra = LocalPlanifyColors.current

    var hintCount by remember { mutableIntStateOf(0) }
    var exerciseInput by remember { mutableStateOf("") }
    var exerciseFeedback by remember { mutableStateOf<Boolean?>(null) }

    val selectedQuizAnswers = remember { mutableStateMapOf<Int, Int>() }
    var quizSubmittedScore by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text("← ${strings.back}") }
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (progress?.isBookmarked == true) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = strings.academyBookmarks
                    )
                }
            }
            Text(
                text = if (lang == AppLanguage.FA) lesson.titleFa else lesson.titleEn,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Step-by-Step Explanation
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.academyStepByStep, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    val steps = if (lang == AppLanguage.FA) lesson.stepsFa else lesson.stepsEn
                    steps.forEach { step ->
                        Text(step, style = MaterialTheme.typography.bodyMedium)
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(strings.academyExamPrep, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(if (lang == AppLanguage.FA) lesson.examTipFa else lesson.examTipEn, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Guided Exercise with Progressive Hints
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.academyExercises, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (lang == AppLanguage.FA) lesson.exercise.promptFa else lesson.exercise.promptEn,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    val hints = if (lang == AppLanguage.FA) lesson.exercise.hintsFa else lesson.exercise.hintsEn
                    hints.take(hintCount).forEachIndexed { idx, h ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = extra.warning, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${idx + 1}. $h", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = exerciseInput,
                            onValueChange = { exerciseInput = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = {
                            exerciseFeedback = exerciseInput.trim().equals(lesson.exercise.expectedAnswer, ignoreCase = true)
                        }) {
                            Text(strings.academySubmitAnswer)
                        }
                    }
                    if (hintCount < hints.size) {
                        TextButton(onClick = { hintCount++ }) {
                            Text(if (hintCount == 0) strings.academyShowHint else strings.academyNextHint)
                        }
                    }
                    exerciseFeedback?.let { ok ->
                        Text(
                            text = if (ok) strings.academyCorrect else strings.academyTryAgain,
                            color = if (ok) extra.success else extra.danger,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Interactive Quiz
        item {
            PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.academyQuizzes, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    lesson.quiz.forEachIndexed { qIdx, q ->
                        Text(
                            text = "${qIdx + 1}. ${if (lang == AppLanguage.FA) q.questionFa else q.questionEn}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val options = if (lang == AppLanguage.FA) q.optionsFa else q.optionsEn
                        options.forEachIndexed { optIdx, optText ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedQuizAnswers[qIdx] = optIdx },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedQuizAnswers[qIdx] == optIdx,
                                    onClick = { selectedQuizAnswers[qIdx] = optIdx }
                                )
                                Text(optText, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Button(
                        onClick = {
                            val correct = lesson.quiz.indices.count { selectedQuizAnswers[it] == lesson.quiz[it].correctIndex }
                            quizSubmittedScore = correct
                            onSubmitQuiz(correct, lesson.quiz.size)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.academySubmitAnswer)
                    }
                    quizSubmittedScore?.let { sc ->
                        Text(
                            text = "${strings.academyQuizScore}: ${JalaliCalendar.formatNumber(sc, lang)} / ${JalaliCalendar.formatNumber(lesson.quiz.size, lang)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            val done = progress?.isCompleted == true
            Button(
                onClick = { onMarkComplete(!done) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (done) strings.academyCompleted else strings.academyMarkComplete)
            }
        }
    }
}

@Composable
private fun FlashcardsDeckView(
    flashcards: List<FlashcardEntity>,
    onAnswer: (FlashcardEntity, Boolean) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    var currentIndex by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }

    if (flashcards.isEmpty()) {
        EmptyStateCard(icon = Icons.Rounded.Flip, title = strings.noResults, modifier = Modifier.padding(16.dp))
        return
    }
    val card = flashcards[currentIndex % flashcards.size]

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${JalaliCalendar.formatNumber((currentIndex % flashcards.size) + 1, lang)} / ${JalaliCalendar.formatNumber(flashcards.size, lang)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        PlanifyCard(
            modifier = Modifier.fillMaxWidth().height(230.dp),
            onClick = { flipped = !flipped }
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (!flipped) {
                        if (lang == AppLanguage.FA) card.frontFa else card.frontEn
                    } else {
                        if (lang == AppLanguage.FA) card.backFa else card.backEn
                    },
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = strings.academyFlipCard,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onAnswer(card, false)
                    flipped = false
                    currentIndex++
                },
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text(strings.academyReviewAgain)
            }
            Button(
                onClick = {
                    onAnswer(card, true)
                    flipped = false
                    currentIndex++
                },
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text(strings.academyKnowIt)
            }
        }
    }
}

@Composable
private fun WeakPointsAndBookmarksView(
    progressList: List<AcademyProgressEntity>,
    quizAttempts: List<QuizAttemptEntity>,
    onOpenLesson: (AcademyLesson) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val extra = LocalPlanifyColors.current

    val weakLessons = progressList.filter { it.weakScoreCount > 0 }.sortedByDescending { it.weakScoreCount }
    val bookmarked = progressList.filter { it.isBookmarked }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(strings.academyWeakPoints, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (weakLessons.isEmpty()) {
            item {
                EmptyStateCard(icon = Icons.Rounded.Warning, title = strings.noResults)
            }
        } else {
            items(weakLessons, key = { "weak_${it.lessonId}" }) { prog ->
                val lesson = AcademyCurriculum.lessons.firstOrNull { it.id == prog.lessonId }
                if (lesson != null) {
                    PlanifyCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenLesson(lesson) }) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = extra.warning)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (lang == AppLanguage.FA) lesson.titleFa else lesson.titleEn,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(strings.academyBookmarks, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (bookmarked.isEmpty()) {
            item {
                EmptyStateCard(icon = Icons.Rounded.BookmarkBorder, title = strings.noResults)
            }
        } else {
            items(bookmarked, key = { "bm_${it.lessonId}" }) { prog ->
                val lesson = AcademyCurriculum.lessons.firstOrNull { it.id == prog.lessonId }
                if (lesson != null) {
                    PlanifyCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenLesson(lesson) }) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (lang == AppLanguage.FA) lesson.titleFa else lesson.titleEn,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        if (quizAttempts.isNotEmpty()) {
            item {
                Text(strings.academyQuizzes, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(quizAttempts.take(8), key = { "qa_${it.id}" }) { attempt ->
                PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(AcademyCurriculum.subjectName(attempt.subjectId, lang))
                        Text(
                            text = "${JalaliCalendar.formatNumber(attempt.score, lang)}/${JalaliCalendar.formatNumber(attempt.totalQuestions, lang)}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCustomMaterialDialog(
    subjectId: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String?) -> Unit
) {
    val strings = LocalStrings.current
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.academyAddMaterial) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.academyMaterialTitle) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text(strings.academyMaterialBody) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(imageUri?.lastPathSegment ?: strings.academyAttachImage)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && body.isNotBlank(),
                onClick = { onSave(title, body, imageUri?.toString()) }
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
