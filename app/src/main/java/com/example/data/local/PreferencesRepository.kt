package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.CalendarSystem
import com.example.ui.theme.PlanifyThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "planify_prefs")

data class AppPreferencesState(
    val onboardingCompleted: Boolean = false,
    val language: AppLanguage = AppLanguage.EN,
    val themeType: PlanifyThemeType = PlanifyThemeType.LIGHT,
    val customPrimaryHex: Long = 0xFF168BFF,
    val customSecondaryHex: Long = 0xFF20D4E8,
    val customDarkBase: Boolean = false,
    val calendarSystem: CalendarSystem = CalendarSystem.GREGORIAN,
    val fontScale: Float = 1.0f,
    val notificationsEnabled: Boolean = true,
    val openRouterBaseUrl: String = "https://openrouter.ai/api/v1/",
    val openRouterApiKey: String = "",
    val openRouterDefaultModel: String = "openai/gpt-4o-mini",
    val assistantModels: Map<String, String> = emptyMap(),
    val aiTemperature: Float = 0.7f,
    val aiMaxTokens: Int = 1500,
    val aiSystemPrompt: String = "",
    val appLockPin: String = ""
)

data class PersistedFocusState(
    val isActive: Boolean = false,
    val isRunning: Boolean = false,
    val mode: String = "POMODORO",
    val taskId: Long? = null,
    val taskTitle: String = "",
    val categoryName: String = "Study",
    val plannedSeconds: Int = 1500,
    val remainingSeconds: Int = 1500,
    val elapsedSeconds: Int = 0,
    val startedAt: Long = 0L,
    val lastTickAt: Long = 0L,
    val ambientSound: String = "NONE",
    val appBlockEnabled: Boolean = false
)

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_TYPE = stringPreferencesKey("theme_type")
        val CUSTOM_PRIMARY = longPreferencesKey("custom_primary")
        val CUSTOM_SECONDARY = longPreferencesKey("custom_secondary")
        val CUSTOM_DARK_BASE = booleanPreferencesKey("custom_dark_base")
        val CALENDAR_SYSTEM = stringPreferencesKey("calendar_system")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val OR_BASE_URL = stringPreferencesKey("openrouter_base_url")
        val OR_API_KEY_ENC = stringPreferencesKey("openrouter_api_key_enc")
        val OR_DEFAULT_MODEL = stringPreferencesKey("openrouter_default_model")
        val OR_MODEL_GENERAL = stringPreferencesKey("or_model_general")
        val OR_MODEL_STUDY = stringPreferencesKey("or_model_study")
        val OR_MODEL_PLANNER = stringPreferencesKey("or_model_planner")
        val OR_MODEL_FOCUS = stringPreferencesKey("or_model_focus")
        val OR_MODEL_NOTE = stringPreferencesKey("or_model_note")
        val AI_TEMP = floatPreferencesKey("ai_temperature")
        val AI_MAX_TOKENS = intPreferencesKey("ai_max_tokens")
        val AI_SYSTEM_PROMPT = stringPreferencesKey("ai_system_prompt")
        val APP_LOCK_PIN_ENC = stringPreferencesKey("app_lock_pin_enc")

        // Active Focus Recovery Keys
        val FOCUS_ACTIVE = booleanPreferencesKey("focus_active")
        val FOCUS_RUNNING = booleanPreferencesKey("focus_running")
        val FOCUS_MODE = stringPreferencesKey("focus_mode")
        val FOCUS_TASK_ID = longPreferencesKey("focus_task_id")
        val FOCUS_TASK_TITLE = stringPreferencesKey("focus_task_title")
        val FOCUS_CATEGORY = stringPreferencesKey("focus_category")
        val FOCUS_PLANNED_SEC = intPreferencesKey("focus_planned_sec")
        val FOCUS_REMAINING_SEC = intPreferencesKey("focus_remaining_sec")
        val FOCUS_ELAPSED_SEC = intPreferencesKey("focus_elapsed_sec")
        val FOCUS_STARTED_AT = longPreferencesKey("focus_started_at")
        val FOCUS_LAST_TICK = longPreferencesKey("focus_last_tick")
        val FOCUS_AMBIENT = stringPreferencesKey("focus_ambient")
        val FOCUS_BLOCK_ENABLED = booleanPreferencesKey("focus_block_enabled")
    }

    val preferencesFlow: Flow<AppPreferencesState> = context.dataStore.data.map { prefs ->
        val lang = AppLanguage.fromCode(prefs[Keys.LANGUAGE])
        val defaultCal = if (lang == AppLanguage.FA) CalendarSystem.JALALI else CalendarSystem.GREGORIAN
        val cal = prefs[Keys.CALENDAR_SYSTEM]?.let {
            runCatching { CalendarSystem.valueOf(it) }.getOrNull()
        } ?: defaultCal

        val models = buildMap {
            prefs[Keys.OR_MODEL_GENERAL]?.takeIf { it.isNotBlank() }?.let { put("GENERAL", it) }
            prefs[Keys.OR_MODEL_STUDY]?.takeIf { it.isNotBlank() }?.let { put("STUDY", it) }
            prefs[Keys.OR_MODEL_PLANNER]?.takeIf { it.isNotBlank() }?.let { put("PLANNER", it) }
            prefs[Keys.OR_MODEL_FOCUS]?.takeIf { it.isNotBlank() }?.let { put("FOCUS", it) }
            prefs[Keys.OR_MODEL_NOTE]?.takeIf { it.isNotBlank() }?.let { put("NOTE", it) }
        }

        AppPreferencesState(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            language = lang,
            themeType = PlanifyThemeType.fromId(prefs[Keys.THEME_TYPE]),
            customPrimaryHex = prefs[Keys.CUSTOM_PRIMARY] ?: 0xFF168BFF,
            customSecondaryHex = prefs[Keys.CUSTOM_SECONDARY] ?: 0xFF20D4E8,
            customDarkBase = prefs[Keys.CUSTOM_DARK_BASE] ?: false,
            calendarSystem = cal,
            fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            openRouterBaseUrl = prefs[Keys.OR_BASE_URL] ?: "https://openrouter.ai/api/v1/",
            openRouterApiKey = SecureKeyStore.decrypt(prefs[Keys.OR_API_KEY_ENC].orEmpty()),
            openRouterDefaultModel = prefs[Keys.OR_DEFAULT_MODEL] ?: "openai/gpt-4o-mini",
            assistantModels = models,
            aiTemperature = prefs[Keys.AI_TEMP] ?: 0.7f,
            aiMaxTokens = prefs[Keys.AI_MAX_TOKENS] ?: 1500,
            aiSystemPrompt = prefs[Keys.AI_SYSTEM_PROMPT].orEmpty(),
            appLockPin = SecureKeyStore.decrypt(prefs[Keys.APP_LOCK_PIN_ENC].orEmpty())
        )
    }

    suspend fun getPreferencesOnce(): AppPreferencesState = preferencesFlow.first()

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
            prefs[Keys.CALENDAR_SYSTEM] = if (language == AppLanguage.FA) {
                CalendarSystem.JALALI.name
            } else {
                CalendarSystem.GREGORIAN.name
            }
        }
    }

    suspend fun setCalendarSystem(system: CalendarSystem) {
        context.dataStore.edit { it[Keys.CALENDAR_SYSTEM] = system.name }
    }

    suspend fun setTheme(themeType: PlanifyThemeType) {
        context.dataStore.edit { it[Keys.THEME_TYPE] = themeType.id }
    }

    suspend fun setCustomThemeColors(primaryHex: Long, secondaryHex: Long, darkBase: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_TYPE] = PlanifyThemeType.CUSTOM.id
            prefs[Keys.CUSTOM_PRIMARY] = primaryHex
            prefs[Keys.CUSTOM_SECONDARY] = secondaryHex
            prefs[Keys.CUSTOM_DARK_BASE] = darkBase
        }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[Keys.FONT_SCALE] = scale.coerceIn(0.85f, 1.25f) }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = true }
    }

    suspend fun saveAiConfig(
        apiKey: String,
        baseUrl: String,
        defaultModel: String,
        assistantModels: Map<String, String>,
        temperature: Float,
        maxTokens: Int,
        systemPrompt: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OR_API_KEY_ENC] = SecureKeyStore.encrypt(apiKey.trim())
            val normalizedUrl = baseUrl.trim().let { if (it.endsWith("/")) it else "$it/" }
            prefs[Keys.OR_BASE_URL] = normalizedUrl.ifBlank { "https://openrouter.ai/api/v1/" }
            prefs[Keys.OR_DEFAULT_MODEL] = defaultModel.trim().ifBlank { "openai/gpt-4o-mini" }
            prefs[Keys.OR_MODEL_GENERAL] = assistantModels["GENERAL"].orEmpty()
            prefs[Keys.OR_MODEL_STUDY] = assistantModels["STUDY"].orEmpty()
            prefs[Keys.OR_MODEL_PLANNER] = assistantModels["PLANNER"].orEmpty()
            prefs[Keys.OR_MODEL_FOCUS] = assistantModels["FOCUS"].orEmpty()
            prefs[Keys.OR_MODEL_NOTE] = assistantModels["NOTE"].orEmpty()
            prefs[Keys.AI_TEMP] = temperature.coerceIn(0f, 2f)
            prefs[Keys.AI_MAX_TOKENS] = maxTokens.coerceIn(128, 8192)
            prefs[Keys.AI_SYSTEM_PROMPT] = systemPrompt
        }
    }

    suspend fun setAppLockPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_LOCK_PIN_ENC] = if (pin.isBlank()) "" else SecureKeyStore.encrypt(pin.trim())
        }
    }

    suspend fun saveFocusState(state: PersistedFocusState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FOCUS_ACTIVE] = state.isActive
            prefs[Keys.FOCUS_RUNNING] = state.isRunning
            prefs[Keys.FOCUS_MODE] = state.mode
            prefs[Keys.FOCUS_TASK_ID] = state.taskId ?: -1L
            prefs[Keys.FOCUS_TASK_TITLE] = state.taskTitle
            prefs[Keys.FOCUS_CATEGORY] = state.categoryName
            prefs[Keys.FOCUS_PLANNED_SEC] = state.plannedSeconds
            prefs[Keys.FOCUS_REMAINING_SEC] = state.remainingSeconds
            prefs[Keys.FOCUS_ELAPSED_SEC] = state.elapsedSeconds
            prefs[Keys.FOCUS_STARTED_AT] = state.startedAt
            prefs[Keys.FOCUS_LAST_TICK] = state.lastTickAt
            prefs[Keys.FOCUS_AMBIENT] = state.ambientSound
            prefs[Keys.FOCUS_BLOCK_ENABLED] = state.appBlockEnabled
        }
    }

    suspend fun getPersistedFocusState(): PersistedFocusState {
        val prefs = context.dataStore.data.first()
        val tid = prefs[Keys.FOCUS_TASK_ID] ?: -1L
        return PersistedFocusState(
            isActive = prefs[Keys.FOCUS_ACTIVE] ?: false,
            isRunning = prefs[Keys.FOCUS_RUNNING] ?: false,
            mode = prefs[Keys.FOCUS_MODE] ?: "POMODORO",
            taskId = if (tid > 0L) tid else null,
            taskTitle = prefs[Keys.FOCUS_TASK_TITLE] ?: "",
            categoryName = prefs[Keys.FOCUS_CATEGORY] ?: "Study",
            plannedSeconds = prefs[Keys.FOCUS_PLANNED_SEC] ?: 1500,
            remainingSeconds = prefs[Keys.FOCUS_REMAINING_SEC] ?: 1500,
            elapsedSeconds = prefs[Keys.FOCUS_ELAPSED_SEC] ?: 0,
            startedAt = prefs[Keys.FOCUS_STARTED_AT] ?: 0L,
            lastTickAt = prefs[Keys.FOCUS_LAST_TICK] ?: 0L,
            ambientSound = prefs[Keys.FOCUS_AMBIENT] ?: "NONE",
            appBlockEnabled = prefs[Keys.FOCUS_BLOCK_ENABLED] ?: false
        )
    }
}
