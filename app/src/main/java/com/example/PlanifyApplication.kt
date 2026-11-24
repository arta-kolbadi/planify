package com.example

import android.app.Application
import com.example.core.i18n.AppLanguage
import com.example.data.ai.OpenRouterClient
import com.example.data.local.AppDatabase
import com.example.data.local.PlanifyRepository
import com.example.data.local.PreferencesRepository
import com.example.focus.FocusTimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlanifyApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var preferencesRepository: PreferencesRepository
        private set
    lateinit var repository: PlanifyRepository
        private set
    lateinit var openRouterClient: OpenRouterClient
        private set
    lateinit var focusTimerManager: FocusTimerManager
        private set

    @Volatile
    var currentLanguageCache: AppLanguage = AppLanguage.EN

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        preferencesRepository = PreferencesRepository(this)
        repository = PlanifyRepository(database)
        openRouterClient = OpenRouterClient()
        focusTimerManager = FocusTimerManager(this, repository, preferencesRepository)

        appScope.launch {
            repository.ensureSeeded()
            currentLanguageCache = preferencesRepository.preferencesFlow.first().language
        }
    }
}
