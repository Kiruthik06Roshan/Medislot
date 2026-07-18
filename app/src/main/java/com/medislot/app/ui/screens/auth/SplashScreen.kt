package com.medislot.app.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Run animations in parallel
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 1000)
        )
        alpha.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 800)
        )
        delay(800) // Hold state to make up the 2 second delay
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220)), // Premium Midnight Dark background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Icon(
                imageVector = Icons.Default.MonitorHeart,
                contentDescription = "MediSlot Logo",
                tint = Color(0xFF3B82F6), // Blue healthcare accent
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "MediSlot",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Smart Health Scheduler",
                color = Color(0xFF94A3B8), // Lighter slate
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
