package com.medislot.app.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object DemoConfig {
    const val ENABLE_DEMO_MODE = true
    const val ENABLE_DEMO_SHORTCUT = ENABLE_DEMO_MODE
    var isDemoModeActive by mutableStateOf(false)
}
