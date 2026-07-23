package com.medislot.app.ui.ai.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.medislot.app.data.ai.ApiUsageMonitor
import com.medislot.app.viewmodel.AiStatus

@Composable
fun AiStatusIndicator(
    status: AiStatus,
    modifier: Modifier = Modifier
) {
    var showStats by remember { mutableStateOf(false) }
    val isDebug = ApiUsageMonitor.isDebug()

    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when (status) {
                        AiStatus.ONLINE -> Color(0xFF22C55E)
                        AiStatus.BUSY -> Color(0xFFEAB308)
                        AiStatus.UNAVAILABLE -> Color(0xFFEF4444)
                    }.copy(alpha = 0.1f)
                )
                .clickable(enabled = isDebug) { showStats = !showStats }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val color = when (status) {
                AiStatus.ONLINE -> Color(0xFF22C55E)
                AiStatus.BUSY -> Color(0xFFEAB308)
                AiStatus.UNAVAILABLE -> Color(0xFFEF4444)
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (status) {
                    AiStatus.ONLINE -> "AI Online"
                    AiStatus.BUSY -> "Busy"
                    AiStatus.UNAVAILABLE -> "Unavailable"
                },
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (showStats && isDebug) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "AI Requests: ${ApiUsageMonitor.getRequestsToday()}\n" +
                           "Success: ${ApiUsageMonitor.getSuccessfulRequests()} | Fail: ${ApiUsageMonitor.getFailedRequests()}\n" +
                           "Cache Hits: ${String.format("%.1f", ApiUsageMonitor.getCacheHitRate())}%\n" +
                           "Avg Resp Time: ${ApiUsageMonitor.getAverageResponseTime()}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
