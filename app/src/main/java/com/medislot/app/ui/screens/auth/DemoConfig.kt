package com.medislot.app.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object DemoConfig {
    const val ENABLE_DEMO_SHORTCUT = true
    var isDemoModeActive by mutableStateOf(false)
}
