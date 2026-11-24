package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.PlanifyAppRoot
import com.example.ui.theme.PlanifyTheme
import com.example.ui.viewmodel.AiViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkspaceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val workspaceViewModel: WorkspaceViewModel = viewModel()
            val aiViewModel: AiViewModel = viewModel()
            val prefs by mainViewModel.preferences.collectAsStateWithLifecycle()

            PlanifyTheme(
                themeType = prefs.themeType,
                language = prefs.language,
                calendarSystem = prefs.calendarSystem,
                fontScale = prefs.fontScale,
                customPrimaryHex = prefs.customPrimaryHex,
                customSecondaryHex = prefs.customSecondaryHex,
                customDarkBase = prefs.customDarkBase
            ) {
                PlanifyAppRoot(
                    mainViewModel = mainViewModel,
                    workspaceViewModel = workspaceViewModel,
                    aiViewModel = aiViewModel
                )
            }
        }
    }
}
