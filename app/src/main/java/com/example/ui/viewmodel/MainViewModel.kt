package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PlanifyApplication
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.CalendarSystem
import com.example.data.local.AppPreferencesState
import com.example.data.local.BackupManager
import com.example.data.local.ChallengeEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.PlanifyThemeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PlanifyApplication
    private val prefsRepo = app.preferencesRepository
    private val repo = app.repository
    private val backupManager = BackupManager(app.database)

    val preferences: StateFlow<AppPreferencesState> = prefsRepo.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferencesState())

    val profile: StateFlow<UserProfileEntity?> = repo.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements = repo.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val challenges = repo.challengesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isPinUnlocked = MutableStateFlow(false)
    val isPinUnlocked: StateFlow<Boolean> = _isPinUnlocked.asStateFlow()

    private val _connectionTestStatus = MutableStateFlow<String?>(null)
    val connectionTestStatus: StateFlow<String?> = _connectionTestStatus.asStateFlow()

    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    init {
        viewModelScope.launch {
            repo.ensureSeeded()
        }
    }

    fun setLanguage(language: AppLanguage) {
        app.currentLanguageCache = language
        viewModelScope.launch { prefsRepo.setLanguage(language) }
    }

    fun setCalendarSystem(system: CalendarSystem) {
        viewModelScope.launch { prefsRepo.setCalendarSystem(system) }
    }

    fun setTheme(themeType: PlanifyThemeType) {
        viewModelScope.launch { prefsRepo.setTheme(themeType) }
    }

    fun setCustomThemeColors(primaryHex: Long, secondaryHex: Long, darkBase: Boolean) {
        viewModelScope.launch { prefsRepo.setCustomThemeColors(primaryHex, secondaryHex, darkBase) }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { prefsRepo.setFontScale(scale) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setNotificationsEnabled(enabled) }
    }

    fun finishOnboarding(name: String, dailyGoalMinutes: Int) {
        viewModelScope.launch {
            repo.updateProfileNameAndGoal(name, dailyGoalMinutes)
            prefsRepo.completeOnboarding()
        }
    }

    fun updateProfile(name: String, dailyGoalMinutes: Int) {
        viewModelScope.launch {
            repo.updateProfileNameAndGoal(name, dailyGoalMinutes)
        }
    }

    fun unlockOrEquipMascotItem(itemId: String, category: String, costCoins: Int) {
        viewModelScope.launch {
            repo.unlockOrEquipMascotItem(itemId, category, costCoins)
        }
    }

    fun claimChallenge(challenge: ChallengeEntity) {
        viewModelScope.launch {
            repo.claimChallenge(challenge)
        }
    }

    fun saveAiConfig(
        apiKey: String,
        baseUrl: String,
        defaultModel: String,
        assistantModels: Map<String, String>,
        temperature: Float,
        maxTokens: Int,
        systemPrompt: String
    ) {
        viewModelScope.launch {
            prefsRepo.saveAiConfig(
                apiKey = apiKey,
                baseUrl = baseUrl,
                defaultModel = defaultModel,
                assistantModels = assistantModels,
                temperature = temperature,
                maxTokens = maxTokens,
                systemPrompt = systemPrompt
            )
        }
    }

    fun testOpenRouterConnection(apiKey: String, baseUrl: String) {
        viewModelScope.launch {
            _connectionTestStatus.value = "..."
            val result = app.openRouterClient.testConnection(apiKey, baseUrl)
            _connectionTestStatus.value = result.fold(
                onSuccess = { count -> "OK:$count" },
                onFailure = { err -> "ERR:${err.localizedMessage ?: "Unknown error"}" }
            )
        }
    }

    fun clearConnectionStatus() {
        _connectionTestStatus.value = null
    }

    fun setAppLockPin(pin: String) {
        viewModelScope.launch {
            prefsRepo.setAppLockPin(pin)
            if (pin.isBlank()) _isPinUnlocked.value = true
        }
    }

    fun verifyPin(input: String): Boolean {
        val actual = preferences.value.appLockPin
        val ok = actual.isBlank() || input == actual
        if (ok) _isPinUnlocked.value = true
        return ok
    }

    fun exportBackupToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val json = withContext(Dispatchers.IO) { backupManager.exportJson() }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    } ?: error("Cannot open output stream")
                }
            }.onSuccess {
                _backupStatus.value = "EXPORT_OK"
            }.onFailure { err ->
                _backupStatus.value = "ERR:${err.localizedMessage}"
            }
        }
    }

    fun importBackupFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val raw = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Cannot read file")
                }
                backupManager.importJson(raw).getOrThrow()
            }.onSuccess { count ->
                _backupStatus.value = "IMPORT_OK:$count"
            }.onFailure { err ->
                _backupStatus.value = "ERR:${err.localizedMessage}"
            }
        }
    }

    fun clearBackupStatus() {
        _backupStatus.value = null
    }
}
