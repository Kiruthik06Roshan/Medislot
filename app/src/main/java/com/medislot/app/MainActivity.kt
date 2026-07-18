package com.medislot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.medislot.app.ui.navigation.MediSlotApp
import com.medislot.app.ui.theme.MediSlotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediSlotTheme {
                MediSlotApp()
            }
        }
    }
}