package com.medislot.app.ui.ai.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.components.MediSlotSecondaryButton

@Composable
fun AiErrorCard(
    errorText: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCountingDown = errorText.contains("Please wait")

    MediSlotCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isCountingDown) "AI Service Temporarily Busy" else "AI Query Failed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            MediSlotSecondaryButton(
                text = "Retry Request",
                onClick = onRetry,
                enabled = !isCountingDown,
                modifier = Modifier.fillMaxWidth(0.6f)
            )
        }
    }
}
