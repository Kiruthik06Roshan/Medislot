package com.medislot.app.ui.ai.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.medislot.app.ui.components.MediSlotCard
import kotlinx.coroutines.delay

@Composable
fun AiLoadingCard(
    modifier: Modifier = Modifier,
    loadingText: String = "MediSlot AI is formulating advice..."
) {
    // 2.5s message rotating logic
    val messages = remember(loadingText) {
        listOf(
            loadingText,
            "Analyzing symptoms...",
            "Reviewing medical information...",
            "Generating recommendations...",
            "Consulting clinical guidelines...",
            "Evaluating treatment options...",
            "Structuring response insights..."
        )
    }
    var currentMsgIndex by remember { mutableStateOf(0) }

    LaunchedEffect(loadingText) {
        while (true) {
            delay(2500)
            currentMsgIndex = (currentMsgIndex + 1) % messages.size
        }
    }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Animated pulse container effect
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    MediSlotCard(
        modifier = modifier.fillMaxWidth().graphicsLayer(alpha = pulseAlpha)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ThinkingAnimation()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = messages[currentMsgIndex],
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skeleton lines representing generated content
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
            )
        }
    }
}
