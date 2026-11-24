package com.example.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.components.BottomNavItemSpec
import com.example.ui.components.LiquidGlassBottomBar
import com.example.ui.components.LiquidGlassHeroCard
import com.example.ui.components.LiquidGlassMoreNavIcon
import com.example.ui.components.LiquidGlassTopBar
import com.example.ui.components.PlanifyAmbientBackdrop
import com.example.ui.components.liquidGlassBorder
import com.example.ui.components.liquidGlassContainer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.AppStrings
import com.example.core.i18n.LocalStrings
import com.example.ui.screens.AcademyScreen
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudyRoomScreen
import com.example.ui.viewmodel.AiViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkspaceViewModel

sealed class PlanifyRoute(val route: String, val icon: ImageVector) {
    data object Dashboard : PlanifyRoute("dashboard", Icons.Rounded.Dashboard)
    data object Planner : PlanifyRoute("planner", Icons.Rounded.CalendarMonth)
    data object Focus : PlanifyRoute("focus", Icons.Rounded.Timer)
    data object AiAssistant : PlanifyRoute("ai_assistant", Icons.Rounded.AutoAwesome)
    data object Academy : PlanifyRoute("academy", Icons.Rounded.School)
    data object Notes : PlanifyRoute("notes", Icons.Rounded.EditNote)
    data object StudyRoom : PlanifyRoute("study_room", Icons.Rounded.Groups)
    data object Analytics : PlanifyRoute("analytics", Icons.Rounded.BarChart)
    data object Achievements : PlanifyRoute("achievements", Icons.Rounded.EmojiEvents)
    data object Settings : PlanifyRoute("settings", Icons.Rounded.Settings)

    fun title(strings: AppStrings): String = when (this) {
        Dashboard -> strings.navDashboard
        Planner -> strings.navPlanner
        Focus -> strings.navFocus
        AiAssistant -> strings.navAi
        Academy -> strings.navAcademy
        Notes -> strings.navNotes
        StudyRoom -> strings.navStudyRoom
        Analytics -> strings.navAnalytics
        Achievements -> strings.navAchievements
        Settings -> strings.navSettings
    }

    companion object {
        val allRoutes = listOf(
            Dashboard, Planner, Focus, AiAssistant, Academy,
            Notes, StudyRoom, Analytics, Achievements, Settings
        )
        val primaryCompactRoutes = listOf(Dashboard, Planner, Focus, AiAssistant)
        val secondaryRoutes = listOf(Academy, Notes, StudyRoom, Analytics, Achievements, Settings)

        fun fromRoute(route: String?): PlanifyRoute =
            allRoutes.firstOrNull { it.route == route } ?: Dashboard
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanifyAppRoot(
    mainViewModel: MainViewModel,
    workspaceViewModel: WorkspaceViewModel,
    aiViewModel: AiViewModel
) {
    val prefs by mainViewModel.preferences.collectAsStateWithLifecycle()
    val profile by mainViewModel.profile.collectAsStateWithLifecycle()
    val isPinUnlocked by mainViewModel.isPinUnlocked.collectAsStateWithLifecycle()

    if (!prefs.onboardingCompleted) {
        OnboardingScreen(
            prefs = prefs,
            onSelectLanguage = mainViewModel::setLanguage,
            onSelectTheme = mainViewModel::setTheme,
            onComplete = mainViewModel::finishOnboarding
        )
        return
    }

    if (prefs.appLockPin.isNotBlank() && !isPinUnlocked) {
        PinUnlockScreen(onVerify = mainViewModel::verifyPin)
        return
    }

    val context = LocalContext.current
    val strings = LocalStrings.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = PlanifyRoute.fromRoute(navBackStackEntry?.destination?.route)

    val tasks by workspaceViewModel.tasks.collectAsStateWithLifecycle()
    val subtasks by workspaceViewModel.subtasks.collectAsStateWithLifecycle()
    val categories by workspaceViewModel.categories.collectAsStateWithLifecycle()
    val sessions by workspaceViewModel.focusSessions.collectAsStateWithLifecycle()
    val focusState by workspaceViewModel.focusManager.state.collectAsStateWithLifecycle()
    val blockedApps by workspaceViewModel.blockedApps.collectAsStateWithLifecycle()
    val installedApps by workspaceViewModel.installedApps.collectAsStateWithLifecycle()
    val selectedDateEpochDay by workspaceViewModel.selectedDateEpochDay.collectAsStateWithLifecycle()
    val notes by workspaceViewModel.notes.collectAsStateWithLifecycle()
    val academyProgress by workspaceViewModel.academyProgress.collectAsStateWithLifecycle()
    val quizAttempts by workspaceViewModel.quizAttempts.collectAsStateWithLifecycle()
    val flashcards by workspaceViewModel.flashcards.collectAsStateWithLifecycle()
    val customLessons by workspaceViewModel.customLessons.collectAsStateWithLifecycle()
    val studyRooms by workspaceViewModel.studyRooms.collectAsStateWithLifecycle()
    val studyRoomTasks by workspaceViewModel.studyRoomTasks.collectAsStateWithLifecycle()
    val achievements by mainViewModel.achievements.collectAsStateWithLifecycle()
    val challenges by mainViewModel.challenges.collectAsStateWithLifecycle()
    val connStatus by mainViewModel.connectionTestStatus.collectAsStateWithLifecycle()
    val backupStatus by mainViewModel.backupStatus.collectAsStateWithLifecycle()

    val aiPersona by aiViewModel.selectedPersona.collectAsStateWithLifecycle()
    val conversations by aiViewModel.conversations.collectAsStateWithLifecycle()
    val activeConvId by aiViewModel.activeConversationId.collectAsStateWithLifecycle()
    val aiMessages by aiViewModel.messages.collectAsStateWithLifecycle()
    val isAiStreaming by aiViewModel.isStreaming.collectAsStateWithLifecycle()
    val aiStreamPreview by aiViewModel.streamingPreview.collectAsStateWithLifecycle()
    val aiError by aiViewModel.errorMessage.collectAsStateWithLifecycle()
    val aiPendingImage by aiViewModel.pendingImageUri.collectAsStateWithLifecycle()
    val dismissedProposals by aiViewModel.dismissedProposalKeys.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showMoreSheet by remember { mutableStateOf(false) }

    LaunchedEffect(focusState.lastCompletedMessageFlag) {
        if (focusState.lastCompletedMessageFlag > 0L) {
            snackbarHostState.showSnackbar(strings.focusSessionCompletedToast)
        }
    }

    val navigateTo: (PlanifyRoute) -> Unit = { target ->
        navController.navigate(target.route) {
            popUpTo(PlanifyRoute.Dashboard.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    PlanifyAmbientBackdrop {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isCompact = maxWidth < 600.dp

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    LiquidGlassTopBar(
                        title = currentRoute.title(strings),
                        language = prefs.language,
                        strings = strings,
                        onToggleLanguage = {
                            val next = if (prefs.language == AppLanguage.FA) AppLanguage.EN else AppLanguage.FA
                            mainViewModel.setLanguage(next)
                        },
                        onOpenSettings = { navigateTo(PlanifyRoute.Settings) }
                    )
                },
                bottomBar = {
                    if (isCompact) {
                        val bottomItems = PlanifyRoute.primaryCompactRoutes.map { dest ->
                            BottomNavItemSpec(
                                routeId = dest.route,
                                title = dest.title(strings),
                                icon = dest.icon,
                                testTag = "bottom_nav_${dest.route}",
                                isSelected = currentRoute == dest,
                                onClick = { navigateTo(dest) }
                            )
                        } + BottomNavItemSpec(
                            routeId = "more",
                            title = strings.navMoreHub,
                            icon = LiquidGlassMoreNavIcon(),
                            testTag = "bottom_nav_more",
                            isSelected = PlanifyRoute.secondaryRoutes.contains(currentRoute),
                            onClick = { showMoreSheet = true }
                        )
                        LiquidGlassBottomBar(items = bottomItems)
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (!isCompact) {
                        val railShape = RoundedCornerShape(24.dp)
                        Surface(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 10.dp, top = 8.dp, bottom = 12.dp)
                                .liquidGlassContainer(shape = railShape, elevation = 6.dp, heroAccent = false),
                            shape = railShape,
                            color = Color.Transparent,
                            border = liquidGlassBorder(heroAccent = false)
                        ) {
                            NavigationRail(
                                containerColor = Color.Transparent,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                PlanifyRoute.allRoutes.forEach { dest ->
                                    NavigationRailItem(
                                        selected = currentRoute == dest,
                                        onClick = { navigateTo(dest) },
                                        icon = { Icon(dest.icon, contentDescription = dest.title(strings)) },
                                        label = { Text(dest.title(strings)) }
                                    )
                                }
                            }
                        }
                    }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = PlanifyRoute.Dashboard.route
                    ) {
                        composable(PlanifyRoute.Dashboard.route) {
                            DashboardScreen(
                                profile = profile,
                                tasks = tasks,
                                sessions = sessions,
                                focusState = focusState,
                                onToggleTask = workspaceViewModel::toggleTaskDone,
                                onNavigate = navigateTo
                            )
                        }
                        composable(PlanifyRoute.Planner.route) {
                            PlannerScreen(
                                selectedDateEpochDay = selectedDateEpochDay,
                                tasks = tasks,
                                subtasks = subtasks,
                                categories = categories,
                                onSelectDate = workspaceViewModel::selectDate,
                                onSaveTask = workspaceViewModel::saveTask,
                                onToggleTask = workspaceViewModel::toggleTaskDone,
                                onToggleSubtask = workspaceViewModel::toggleSubtask,
                                onMoveTaskOrder = workspaceViewModel::moveTaskOrder,
                                onDeleteTask = workspaceViewModel::deleteTask,
                                onStartFocusForTask = { t ->
                                    workspaceViewModel.startFocusWithTask(t)
                                    navigateTo(PlanifyRoute.Focus)
                                }
                            )
                        }
                        composable(PlanifyRoute.Focus.route) {
                            FocusScreen(
                                focusState = focusState,
                                profile = profile,
                                tasks = tasks,
                                sessions = sessions,
                                blockedApps = blockedApps,
                                installedApps = installedApps,
                                onSelectMode = workspaceViewModel::setFocusMode,
                                onSetMinutes = workspaceViewModel::setFocusMinutes,
                                onSelectTask = workspaceViewModel::selectFocusTask,
                                onSetAmbientSound = workspaceViewModel::setAmbientSound,
                                onSetAppBlockEnabled = workspaceViewModel::setAppBlockEnabled,
                                onStart = workspaceViewModel::startFocus,
                                onPause = workspaceViewModel::pauseFocus,
                                onResume = workspaceViewModel::resumeFocus,
                                onEndSession = workspaceViewModel::endFocus,
                                onRefreshInstalledApps = { workspaceViewModel.refreshInstalledApps(context) },
                                onToggleBlockedApp = workspaceViewModel::setAppBlocked
                            )
                        }
                        composable(PlanifyRoute.AiAssistant.route) {
                            AiAssistantScreen(
                                prefs = prefs,
                                selectedPersona = aiPersona,
                                conversations = conversations,
                                activeConversationId = activeConvId,
                                messages = aiMessages,
                                isStreaming = isAiStreaming,
                                streamingPreview = aiStreamPreview,
                                errorMessage = aiError,
                                pendingImageUri = aiPendingImage,
                                dismissedProposalKeys = dismissedProposals,
                                onSelectPersona = aiViewModel::selectPersona,
                                onSelectConversation = aiViewModel::selectConversation,
                                onNewChat = aiViewModel::startNewConversation,
                                onDeleteConversation = aiViewModel::deleteConversation,
                                onClearChat = aiViewModel::clearCurrentConversation,
                                onSetPendingImage = aiViewModel::setPendingImage,
                                onSendMessage = { prompt, model -> aiViewModel.sendMessage(context, prompt, model) },
                                onStopGeneration = aiViewModel::stopGeneration,
                                onRegenerate = aiViewModel::regenerateLast,
                                onApproveTask = aiViewModel::approveProposedTask,
                                onDismissTask = aiViewModel::dismissProposedTask,
                                onOpenAiSettings = { navigateTo(PlanifyRoute.Settings) }
                            )
                        }
                        composable(PlanifyRoute.Academy.route) {
                            AcademyScreen(
                                progressList = academyProgress,
                                quizAttempts = quizAttempts,
                                flashcards = flashcards,
                                customLessons = customLessons,
                                onMarkLessonComplete = workspaceViewModel::markLessonCompleted,
                                onToggleBookmark = workspaceViewModel::toggleLessonBookmark,
                                onRecordQuizAttempt = workspaceViewModel::recordQuizAttempt,
                                onAnswerFlashcard = workspaceViewModel::answerFlashcard,
                                onAddCustomLesson = workspaceViewModel::addCustomLesson,
                                onDeleteCustomLesson = workspaceViewModel::deleteCustomLesson
                            )
                        }
                        composable(PlanifyRoute.Notes.route) {
                            NotesScreen(
                                notes = notes,
                                onSaveNote = workspaceViewModel::saveNote,
                                onDeleteNote = workspaceViewModel::deleteNote
                            )
                        }
                        composable(PlanifyRoute.StudyRoom.route) {
                            StudyRoomScreen(
                                rooms = studyRooms,
                                roomTasks = studyRoomTasks,
                                onCreateRoom = workspaceViewModel::createStudyRoom,
                                onRecordRoomSession = workspaceViewModel::recordStudyRoomMinutes,
                                onDeleteRoom = workspaceViewModel::deleteStudyRoom,
                                onAddRoomTask = workspaceViewModel::addRoomTask,
                                onToggleRoomTask = workspaceViewModel::toggleRoomTask,
                                onDeleteRoomTask = workspaceViewModel::deleteRoomTask
                            )
                        }
                        composable(PlanifyRoute.Analytics.route) {
                            AnalyticsScreen(
                                profile = profile,
                                tasks = tasks,
                                sessions = sessions
                            )
                        }
                        composable(PlanifyRoute.Achievements.route) {
                            AchievementsScreen(
                                profile = profile,
                                achievements = achievements,
                                challenges = challenges,
                                onClaimChallenge = mainViewModel::claimChallenge,
                                onUnlockOrEquipMascot = mainViewModel::unlockOrEquipMascotItem
                            )
                        }
                        composable(PlanifyRoute.Settings.route) {
                            SettingsScreen(
                                prefs = prefs,
                                profile = profile,
                                connectionTestStatus = connStatus,
                                backupStatus = backupStatus,
                                onSetLanguage = mainViewModel::setLanguage,
                                onSetCalendar = mainViewModel::setCalendarSystem,
                                onSetTheme = mainViewModel::setTheme,
                                onSetCustomTheme = mainViewModel::setCustomThemeColors,
                                onSetFontScale = mainViewModel::setFontScale,
                                onSetNotifications = mainViewModel::setNotificationsEnabled,
                                onUpdateProfile = mainViewModel::updateProfile,
                                onSaveAiConfig = mainViewModel::saveAiConfig,
                                onTestConnection = mainViewModel::testOpenRouterConnection,
                                onSetAppLockPin = mainViewModel::setAppLockPin,
                                onExportBackup = { uri -> mainViewModel.exportBackupToUri(context, uri) },
                                onImportBackup = { uri -> mainViewModel.importBackupFromUri(context, uri) }
                            )
                        }
                    }
                }
            }
        }
        }
    }

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(strings.dashQuickExplore, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                PlanifyRoute.secondaryRoutes.forEach { route ->
                    val isActive = currentRoute == route
                    val tileShape = RoundedCornerShape(18.dp)
                    if (isActive) {
                        LiquidGlassHeroCard(
                            shape = tileShape,
                            onClick = {
                                showMoreSheet = false
                                navigateTo(route)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("more_sheet_${route.route}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(route.icon, contentDescription = route.title(strings), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(route.title(strings), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            shape = tileShape,
                            color = Color.Transparent,
                            border = liquidGlassBorder(heroAccent = false),
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlassContainer(shape = tileShape, elevation = 2.dp, heroAccent = false)
                                .clickable {
                                    showMoreSheet = false
                                    navigateTo(route)
                                }
                                .testTag("more_sheet_${route.route}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(route.icon, contentDescription = route.title(strings), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(route.title(strings), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PinUnlockScreen(onVerify: (String) -> Boolean) {
    val strings = LocalStrings.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(strings.settingsPrivacyPin, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = {
                    pin = it.filter(Char::isDigit).take(4)
                    error = false
                },
                label = { Text(strings.settingsPinHint) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (!onVerify(pin)) {
                        error = true
                    }
                }
            ) {
                Text(strings.confirm)
            }
        }
    }
}
