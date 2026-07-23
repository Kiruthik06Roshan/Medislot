package com.medislot.app.ui.screens.doctor

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.data.model.MockData
import com.medislot.app.data.model.NotificationItem
import com.medislot.app.data.model.LabReport
import com.medislot.app.ui.components.*
import com.medislot.app.ui.theme.LocalDimens
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import com.medislot.app.viewmodel.AiState
import com.medislot.app.ui.ai.components.*

// Helper chip styling (responsively sized to prevent clipping)
@Composable
fun DoctorStatusChip(status: String, modifier: Modifier = Modifier) {
    val (bgColor, txtColor) = when (status.lowercase()) {
        "emergency" -> Pair(Color(0xFFFEE2E2), Color(0xFFEF4444))
        "high" -> Pair(Color(0xFFFFEDD5), Color(0xFFEA580C))
        "waiting" -> Pair(Color(0xFFF3F4F6), Color(0xFF6B7280))
        "checked in" -> Pair(Color(0xFFECFDF5), Color(0xFF10B981))
        "in consultation" -> Pair(Color(0xFFE0F2FE), Color(0xFF0284C7))
        "completed" -> Pair(Color(0xFFECFDF5), Color(0xFF10B981))
        "cancelled" -> Pair(Color(0xFFF9FAFB), Color(0xFF9CA3AF))
        else -> Pair(Color(0xFFF3F4F6), Color(0xFF4B5563))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            color = txtColor,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun StatItemCard(label: String, value: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Visual Queue Timeline Component
@Composable
fun VisualQueueTimeline(appointments: List<PatientRecord>, modifier: Modifier = Modifier) {
    val nonCompleted = appointments.filter { it.status != "Completed" && it.status != "Cancelled" }
    
    if (nonCompleted.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No active patients in queue", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Active Workspace Queue Timeline",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            nonCompleted.forEachIndexed { index, patient ->
                val isCurrent = patient.status == "In Consultation"
                val cardBorder = if (isCurrent) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                val cardBg = if (isCurrent) Color(0xFFF0F9FF) else MaterialTheme.colorScheme.surface
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .border(cardBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Queue #${patient.queueNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (patient.priority == "Emergency") {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFEF4444))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("SOS", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = patient.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DoctorStatusChip(status = patient.status)
                                val waitText = if (patient.status == "In Consultation") "Active" 
                                              else if (patient.estimatedWaitMinutes == 999) "Offline"
                                              else "${patient.estimatedWaitMinutes}m wait"
                                Text(
                                    text = waitText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isCurrent) Color(0xFF0284C7) else Color(0xFFEA580C),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (index < nonCompleted.size - 1) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Enhanced Notification Center Dialog
// ----------------------------------------------------
@Composable
fun NotificationCenterDialog(
    onDismiss: () -> Unit,
    notifications: List<NotificationItem>
) {
    var activeFilter by remember { mutableStateOf("All") }
    
    val filtered = notifications.filter { notif ->
        when (activeFilter) {
            "All" -> true
            "Unread" -> !notif.isRead
            "Emergency" -> notif.type == "Emergency" || notif.priority == "HIGH"
            "Queue" -> notif.type == "Queue"
            else -> true
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notification Center",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Filter buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Unread", "Emergency", "Queue")
                    filters.forEach { filter ->
                        val isSelected = activeFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { activeFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notifications match this filter.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { notif ->
                            val isCritical = notif.priority == "HIGH" || notif.type == "Emergency"
                            val cardBg = if (isCritical) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            val borderCol = if (isCritical) Color(0xFFFCA5A5) else Color.Transparent
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardBg)
                                    .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (isCritical) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCritical) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.timestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    DoctorWorkspaceState.notifications.clear()
                    onDismiss()
                }
            ) {
                Text("Clear All")
            }
        }
    )
}

// ----------------------------------------------------
// Upgraded Doctor Dashboard Screen
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    onNavigateToAppointments: () -> Unit,
    onNavigateToSlots: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val doctor = DoctorWorkspaceState.doctorProfile
    var showNotifications by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Greeting based on time
    val greeting = remember {
        val calendar = java.util.Calendar.getInstance()
        when (calendar.get(java.util.Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
    
    // Break Timer ticking
    val currentStatus = DoctorWorkspaceState.dutyStatus
    LaunchedEffect(key1 = currentStatus) {
        if (currentStatus == DoctorDutyStatus.BREAK) {
            while (true) {
                delay(1000)
                DoctorWorkspaceState.breakTimerSeconds += 1
                DoctorWorkspaceState.recalculateEstimatedWaitTimes()
            }
        } else {
            DoctorWorkspaceState.breakTimerSeconds = 0L
        }
    }
    
    val unreadNotifCount = DoctorWorkspaceState.notifications.size

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Doctor Workspace",
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = { showNotifications = true }) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = Color.White
                                        ) {
                                            Text(unreadNotifCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showNotifications) {
            NotificationCenterDialog(
                onDismiss = { showNotifications = false },
                notifications = DoctorWorkspaceState.notifications
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp) // Tightened margins for improved density
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Header Row: Avatar, Greeting, Date & Room details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = doctor.name.split(" ").filter { it.isNotEmpty() && !it.contains(".") }.take(2).map { it.first() }.joinToString("").uppercase()
                    Text(text = initials, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "$greeting,", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = doctor.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        text = "${doctor.specialization} • ${doctor.hospital} • Room ${DoctorWorkspaceState.roomNumber}", // Fixed bug: "Room Room 4B" to "Room 4B"
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duty Status selector Card with Break Timer simulation
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = DoctorWorkspaceState.todayDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        if (currentStatus == DoctorDutyStatus.BREAK) {
                            val breakMins = DoctorWorkspaceState.breakTimerSeconds / 60
                            val breakSecs = DoctorWorkspaceState.breakTimerSeconds % 60
                            Text(
                                text = String.format("On Break: %02d:%02d", breakMins, breakSecs),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(text = "Room ${DoctorWorkspaceState.roomNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Box {
                        val statusColor = when (currentStatus) {
                            DoctorDutyStatus.AVAILABLE -> Color(0xFF10B981)
                            DoctorDutyStatus.BUSY -> Color(0xFFEF4444)
                            DoctorDutyStatus.BREAK -> Color(0xFFF59E0B)
                            DoctorDutyStatus.OFFLINE -> Color(0xFF9CA3AF)
                        }
                        
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, statusColor), RoundedCornerShape(20.dp))
                                .clickable { showStatusDropdown = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = currentStatus.displayName, color = statusColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false }
                        ) {
                            DoctorDutyStatus.values().forEach { stateVal ->
                                DropdownMenuItem(
                                    text = { Text(stateVal.displayName) },
                                    onClick = {
                                        DoctorWorkspaceState.dutyStatus = stateVal
                                        showStatusDropdown = false
                                        DoctorWorkspaceState.recalculateEstimatedWaitTimes()
                                        DoctorWorkspaceState.addNotification(
                                            title = "Status Changed",
                                            message = "Marked as ${stateVal.displayName}.",
                                            type = "Queue",
                                            priority = "LOW"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulated Smart AI Insights Card
            val hasEmergency = DoctorWorkspaceState.appointments.any { it.priority == "Emergency" && it.status != "Completed" && it.status != "Cancelled" }
            val currentWaitThreshold = DoctorWorkspaceState.appointments.lastOrNull { it.status == "Checked In" || it.status == "Waiting" }?.estimatedWaitMinutes ?: 0
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0F9FF))
                    .border(BorderStroke(1.dp, Color(0xFFBAE6FD)), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Smart AI Clinic Insights", color = Color(0xFF0369A1), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (hasEmergency) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Emergency Patient Detected in queue. Recommend consulting immediately.", color = Color(0xFF334155), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (currentWaitThreshold > 25) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, null, tint = Color(0xFFEA580C), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Queue Overload Detected. Waiting time exceeds threshold.", color = Color(0xFF334155), style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Recommend notifying Admin to allocate another doctor.", color = Color(0xFF334155), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Queue flow is stable. Average consultation delay is optimal today.", color = Color(0xFF334155), style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Recommend opening additional slots to buffer peak walk-ins.", color = Color(0xFF334155), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // statistics cards (increased height and typography for readability)
            SectionHeader(title = "Live Operations Statistics")
            Spacer(modifier = Modifier.height(8.dp))
            
            val totalCount = DoctorWorkspaceState.appointments.size
            val completedCount = DoctorWorkspaceState.appointments.count { it.status == "Completed" }
            val pendingCount = DoctorWorkspaceState.appointments.count { it.status == "Checked In" || it.status == "Waiting" }
            val emergencyCount = DoctorWorkspaceState.appointments.count { it.priority == "Emergency" && it.status != "Completed" && it.status != "Cancelled" }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Large Statistics cards with set height of 105.dp
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(105.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Text("Scheduled", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(totalCount.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(105.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Text("Completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(completedCount.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(105.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Text("SOS/Pending", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$emergencyCount/$pendingCount", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Queue Timeline Card Preview
            VisualQueueTimeline(appointments = DoctorWorkspaceState.appointments)

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions Panel
            SectionHeader(title = "Workspace Quick Actions")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick start consultation
                Button(
                    onClick = {
                        val next = DoctorWorkspaceState.appointments.firstOrNull { it.status == "Checked In" || it.status == "Waiting" }
                        if (next != null) {
                            if (currentStatus == DoctorDutyStatus.OFFLINE) {
                                Toast.makeText(context, "Doctor is offline", Toast.LENGTH_SHORT).show()
                            } else {
                                DoctorWorkspaceState.startConsultation(next.id)
                                Toast.makeText(context, "Consultation started.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "No patients waiting.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Next", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                
                Button(
                    onClick = onNavigateToAppointments,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.List, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Queue View", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Button(
                    onClick = onNavigateToHistory,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Icon(Icons.Default.History, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("History", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Consultations & Upcoming Appointments Preview
            SectionHeader(title = "Consultations Registry Summary")
            Spacer(modifier = Modifier.height(8.dp))
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Consultations:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val activePat = DoctorWorkspaceState.appointments.firstOrNull { it.status == "In Consultation" }
                        Text(activePat?.name ?: "None", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Upcoming Appointment Next:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val upcoming = DoctorWorkspaceState.appointments.firstOrNull { it.status == "Checked In" || it.status == "Waiting" }
                        Text(upcoming?.name ?: "None", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pending Lab Records:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("3 items pending authorization", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hospital Announcements
            SectionHeader(title = "Hospital Operations Board")
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Announcement, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Staff Announcements", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Clinical QA Audit scheduled for Wing B tomorrow at 10:00 AM. Please ensure EHR summaries are synced.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ----------------------------------------------------
// Redesigned Today's Consultations screen
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAppointmentsScreen(
    onNavigateToPatientDetails: (String) -> Unit,
    onNavigateToUploadPrescription: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    
    val appointments = DoctorWorkspaceState.appointments
    val isTimerRunning = DoctorWorkspaceState.isTimerRunning
    
    // Live ticking wait times
    LaunchedEffect(key1 = isTimerRunning, key2 = DoctorWorkspaceState.consultationTimerSeconds) {
        if (isTimerRunning) {
            DoctorWorkspaceState.recalculateEstimatedWaitTimes()
        }
    }

    val filteredAppointments = appointments.filter { patient ->
        val matchesSearch = patient.name.contains(searchQuery, ignoreCase = true) || 
                            patient.symptoms.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "All" -> true
            "Waiting" -> patient.status == "Waiting" || patient.status == "Checked In"
            "Completed" -> patient.status == "Completed"
            "Emergency" -> patient.priority == "Emergency"
            "High Priority" -> patient.priority == "High"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Live Consultation Queue", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Queue Timeline Component
            VisualQueueTimeline(appointments = appointments)
            
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search patient name, case or symptoms...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            val filters = listOf("All", "Waiting", "Completed", "Emergency", "High Priority")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredAppointments.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No clinic patient records found.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { DoctorWorkspaceState.resetState() }) {
                        Text("Refresh Simulated Database")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredAppointments) { patient ->
                        MediSlotCard {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = patient.name.take(2).uppercase(),
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(text = patient.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "Queue #${patient.queueNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "${patient.appointmentTime} • ${patient.age} yrs • ${patient.gender}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (patient.estimatedWaitMinutes > 0) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(${patient.estimatedWaitMinutes}m wait)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFEA580C),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Symptoms: ${patient.symptoms}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DoctorStatusChip(status = patient.status)
                                    DoctorStatusChip(status = patient.priority)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MediSlotSecondaryButton(
                                        text = "EHR Details",
                                        onClick = { onNavigateToPatientDetails(patient.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    val isCurrent = patient.status == "In Consultation"
                                    val isCompleted = patient.status == "Completed" || patient.status == "Cancelled"
                                    val isOffline = DoctorWorkspaceState.dutyStatus == DoctorDutyStatus.OFFLINE
                                    
                                    if (!isCompleted) {
                                        MediSlotButton(
                                            text = if (isCurrent) "Workspace" else "Start Consult",
                                            onClick = {
                                                if (!isCurrent) {
                                                    DoctorWorkspaceState.startConsultation(patient.id)
                                                }
                                                onNavigateToUploadPrescription(patient.id)
                                            },
                                            modifier = Modifier.weight(1.1f),
                                            enabled = !isOffline,
                                            gradient = if (isCurrent) listOf(Color(0xFF0284C7), Color(0xFF0EA5E9)) else listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Enhanced EHR Details Screen (with 8 Animated Tabs)
// ----------------------------------------------------
@Composable
fun DoctorPatientDetailsScreen(
    patientId: String,
    onNavigateToUploadPrescription: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val patient = DoctorWorkspaceState.appointments.firstOrNull { it.id == patientId } ?: DoctorWorkspaceState.appointments[0]
    var activeTab by remember { mutableStateOf(0) }
    var showReportsDialog by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<LabReport?>(null) }
    val context = LocalContext.current

    val tabs = listOf("Overview", "Med History", "Vitals", "Prescriptions", "Lab Reports", "Radiology", "Timeline", "Files")

    if (showReportsDialog && selectedReport != null) {
        AlertDialog(
            onDismissRequest = { showReportsDialog = false },
            title = { Text(selectedReport!!.testName, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Intake Date:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(selectedReport!!.date, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Findings:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(selectedReport!!.result, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Status Code:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        StatusChip(status = selectedReport!!.status)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showReportsDialog = false }) {
                    Text("Close Document")
                }
            }
        )
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Patient EHR Records", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Header patient card
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(patient.name.take(2).uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(patient.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            // Shortened Blood Group display to prevent wrapping issues (Fixed bug 3)
                            Text(
                                text = "Age: ${patient.age} | ${patient.gender} | Blood: ${patient.bloodGroup.substringBefore(" ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontally Scrollable EHR Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, tabName ->
                    val isSelected = activeTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { activeTab = index }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tabName,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content with transition animations
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (activeTab) {
                    0 -> { // Overview
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionHeader(title = "Health Metric Status")
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatItemCard(label = "Blood Group", value = patient.bloodGroup.substringBefore(" "), modifier = Modifier.weight(1f), color = Color(0xFFEF4444))
                                StatItemCard(label = "BMI", value = patient.bmi.substringBefore(" "), modifier = Modifier.weight(1f), color = Color(0xFF10B981))
                            }
                            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Intake Symptoms", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(patient.symptoms, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Emergency Contact", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                    Text(patient.emergencyContact, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    1 -> { // Medical History
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(title = "EHR Conditions History")
                            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Chronic Conditions", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                                    patient.history.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                                    if (patient.history.isEmpty()) Text("No chronic diseases in history.")
                                }
                            }
                            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Known Allergies & Intolerances", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall)
                                    patient.allergies.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                                    if (patient.allergies.isEmpty()) Text("No allergies recorded.")
                                }
                            }
                        }
                    }
                    2 -> { // Vitals
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(title = "Latest Vitals Summary")
                            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Blood Pressure:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(patient.vitals.bloodPressure, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Heart Rate:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${patient.vitals.heartRate} bpm", fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Temperature:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${patient.vitals.temperature} °F", fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("O2 Saturation:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${patient.vitals.oxygenSaturation} %", fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Pain Scale Index:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${patient.vitals.painScale} / 10", fontWeight = FontWeight.Bold, color = if (patient.vitals.painScale >= 5) Color.Red else Color.Green)
                                    }
                                }
                            }
                        }
                    }
                    3 -> { // Prescriptions
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(title = "Active Clinic Prescriptions")
                            patient.medications.forEach { med ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(med, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (patient.medications.isEmpty()) {
                                Text("No active medications found.")
                            }
                        }
                    }
                    4 -> { // Lab Reports
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(title = "Clinical Lab Documents")
                            patient.uploadedReports.forEach { report ->
                                MediSlotCard(
                                    onClick = {
                                        selectedReport = report
                                        showReportsDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(report.testName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Uploaded: ${report.date}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        StatusChip(status = report.status)
                                    }
                                }
                            }
                        }
                    }
                    5 -> { // Radiology
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(title = "Imaging & Radiology files")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Chest X-Ray Plate 1B (Dicom Format)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("Archived on Jun 02, 2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    6 -> { // Timeline
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionHeader(title = "Clinic Visit Logs")
                            patient.previousVisits.forEach { visit ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(visit, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    7 -> { // Files
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(title = "EHR Uploaded Documents")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Consent Form Signed.pdf", style = MaterialTheme.typography.bodyMedium)
                                }
                                Icon(Icons.Default.FileDownload, null, tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }

            // Action
            val isCurrent = patient.status == "In Consultation"
            val isCompleted = patient.status == "Completed" || patient.status == "Cancelled"
            
            if (!isCompleted) {
                Box(modifier = Modifier.padding(16.dp)) {
                    MediSlotButton(
                        text = if (isCurrent) "Open Workspace Desk" else "Start Consultation Workspace",
                        onClick = {
                            if (!isCurrent) {
                                DoctorWorkspaceState.startConsultation(patient.id)
                            }
                            onNavigateToUploadPrescription(patient.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// Redesigned Doctor Consultation Workspace Desk
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionUploadScreen(
    appointmentId: String,
    onNavigateBack: () -> Unit,
    viewModel: com.medislot.app.viewmodel.AiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val patient = DoctorWorkspaceState.appointments.firstOrNull { it.id == appointmentId } 
        ?: DoctorWorkspaceState.appointments.firstOrNull { it.status == "In Consultation" }
        ?: DoctorWorkspaceState.appointments[0]

    // AI dialog triggers and state flows
    val context = LocalContext.current
    var activeDocAiFeature by remember { mutableStateOf<String?>(null) }
    val soapState by viewModel.soapNoteState.collectAsState()
    val enhanceState by viewModel.clinicalEnhancementState.collectAsState()
    val diffState by viewModel.diffDiagnosisState.collectAsState()
    val prescDraftState by viewModel.prescriptionDraftState.collectAsState()
    val labState by viewModel.labInterpretationState.collectAsState()
    val eduState by viewModel.patientEducationState.collectAsState()
    val refState by viewModel.referralLetterState.collectAsState()
    val dischargeState by viewModel.dischargeSummaryState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val aiStatus by viewModel.aiStatus.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect {
            snackbarHostState.showSnackbar(it)
        }
    }
        
    // Buffer input states populated from state manager
    var chiefComplaint by remember { mutableStateOf(DoctorWorkspaceState.currentDiagnosis.chiefComplaint) }
    var symptomsInput by remember { mutableStateOf(DoctorWorkspaceState.currentDiagnosis.symptoms) }
    var primaryDiag by remember { mutableStateOf(DoctorWorkspaceState.currentDiagnosis.primaryDiagnosis) }
    var secondaryDiag by remember { mutableStateOf(DoctorWorkspaceState.currentDiagnosis.secondaryDiagnosis) }
    var clinicalImpression by remember { mutableStateOf(DoctorWorkspaceState.currentDiagnosis.clinicalImpression) }
    var diagNotes by remember { mutableStateOf(DoctorWorkspaceState.currentDiagnosis.doctorNotes) }
    
    // Vitals buffer states
    var bpVal by remember { mutableStateOf(DoctorWorkspaceState.currentVitals.bloodPressure) }
    var hrVal by remember { mutableStateOf(DoctorWorkspaceState.currentVitals.heartRate.toString()) }
    var tempVal by remember { mutableStateOf(DoctorWorkspaceState.currentVitals.temperature.toString()) }
    var o2Val by remember { mutableStateOf(DoctorWorkspaceState.currentVitals.oxygenSaturation.toString()) }
    var respRateVal by remember { mutableStateOf(DoctorWorkspaceState.currentVitals.respiratoryRate.toString()) }
    var heightVal by remember { mutableStateOf(DoctorWorkspaceState.currentVitals.heightCm.toString()) }
    var weightVal by remember { mutableStateOf(DoctorWorkspaceState.currentVitals.weightKg.toString()) }
    var painScaleVal by remember { mutableStateOf(DoctorWorkspaceState.currentVitals.painScale.toFloat()) }
    
    // Clinical Notes buffers
    var observationInput by remember { mutableStateOf(DoctorWorkspaceState.currentClinicalNotes.observation) }
    var clinicalNotesInput by remember { mutableStateOf(DoctorWorkspaceState.currentClinicalNotes.clinicalNotes) }
    var treatmentPlanInput by remember { mutableStateOf(DoctorWorkspaceState.currentClinicalNotes.treatmentPlan) }
    var recommendationsInput by remember { mutableStateOf(DoctorWorkspaceState.currentClinicalNotes.recommendations) }
    var lifestyleInput by remember { mutableStateOf(DoctorWorkspaceState.currentClinicalNotes.lifestyleAdvice) }
    var referralNotesInput by remember { mutableStateOf(DoctorWorkspaceState.currentClinicalNotes.referralNotes) }
    
    // Follow-up states
    var showFollowUpDropdown by remember { mutableStateOf(false) }
    var followUpDuration by remember { mutableStateOf(DoctorWorkspaceState.currentFollowUpDuration) }
    var followUpNotesInput by remember { mutableStateOf(DoctorWorkspaceState.currentFollowUpNotes) }
    
    // Prescription Input buffer states
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("") }
    var medDuration by remember { mutableStateOf("") }
    var medInstructions by remember { mutableStateOf("") }
    var morningSelected by remember { mutableStateOf(false) }
    var afternoonSelected by remember { mutableStateOf(false) }
    var nightSelected by remember { mutableStateOf(false) }
    
    var showCompleteDialog by remember { mutableStateOf(false) }
    
    // Live ticking timer
    val isTimerRunning = DoctorWorkspaceState.isTimerRunning
    LaunchedEffect(key1 = isTimerRunning) {
        if (isTimerRunning) {
            while (true) {
                delay(1000)
                DoctorWorkspaceState.consultationTimerSeconds += 1
            }
        }
    }
    
    val minutes = DoctorWorkspaceState.consultationTimerSeconds / 60
    val seconds = DoctorWorkspaceState.consultationTimerSeconds % 60
    val timerString = String.format("%02d:%02d", minutes, seconds)

    // Complete Consultation Dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Patient Consultation Summary", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Review outcomes for ${patient.name}:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Elapsed Duration:", fontWeight = FontWeight.SemiBold)
                        Text("$timerString mins", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Primary Diagnosis:", fontWeight = FontWeight.SemiBold)
                        Text(primaryDiag.ifBlank { "Unspecified" }, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Medications Prescribed:", fontWeight = FontWeight.SemiBold)
                        Text("${DoctorWorkspaceState.currentPrescriptions.size} Item(s)", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lab Orders Placed:", fontWeight = FontWeight.SemiBold)
                        Text("${DoctorWorkspaceState.currentLabOrders.size} Test(s)", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Follow-Up Plan:", fontWeight = FontWeight.SemiBold)
                        Text(followUpDuration, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompleteDialog = false
                        
                        // Save buffered data back into state singleton
                        DoctorWorkspaceState.currentVitals = PatientVitals(
                            bloodPressure = bpVal,
                            heartRate = hrVal.toIntOrNull() ?: 72,
                            temperature = tempVal.toFloatOrNull() ?: 98.6f,
                            oxygenSaturation = o2Val.toIntOrNull() ?: 98,
                            respiratoryRate = respRateVal.toIntOrNull() ?: 16,
                            heightCm = heightVal.toFloatOrNull() ?: 170f,
                            weightKg = weightVal.toFloatOrNull() ?: 70f,
                            painScale = painScaleVal.toInt()
                        ).apply { calculateBMI() }
                        
                        DoctorWorkspaceState.currentDiagnosis = PatientDiagnosis(
                            chiefComplaint = chiefComplaint,
                            symptoms = symptomsInput,
                            primaryDiagnosis = primaryDiag,
                            secondaryDiagnosis = secondaryDiag,
                            clinicalImpression = clinicalImpression,
                            doctorNotes = diagNotes
                        )
                        
                        DoctorWorkspaceState.currentClinicalNotes = PatientClinicalNotes(
                            observation = observationInput,
                            clinicalNotes = clinicalNotesInput,
                            treatmentPlan = treatmentPlanInput,
                            recommendations = recommendationsInput,
                            lifestyleAdvice = lifestyleInput,
                            referralNotes = referralNotesInput
                        )
                        
                        DoctorWorkspaceState.currentFollowUpDuration = followUpDuration
                        DoctorWorkspaceState.currentFollowUpNotes = followUpNotesInput
                        
                        val finalMins = (DoctorWorkspaceState.consultationTimerSeconds / 60).toInt() + 1
                        DoctorWorkspaceState.completeActiveConsultation(finalMins)
                        
                        Toast.makeText(context, "Consultation saved & queue progressed.", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }
                ) {
                    Text("Complete Consultation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Clinical Workspace Desk", onBackClick = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Timer & averages banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0F9FF))
                    .border(BorderStroke(1.dp, Color(0xFFBAE6FD)), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Timer: $timerString", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                        Text("Avg Duration: ${DoctorWorkspaceState.doctorProfile.averageConsultationTime} mins", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0284C7))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.medislot.app.ui.ai.components.AiStatusIndicator(status = aiStatus)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE0F2FE))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Consulting Now", color = Color(0xFF0369A1), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Patient Header Card (with Emergency and Allergy badges)
            Box(modifier = Modifier.fillMaxWidth()) {
                val hasAllergies = patient.allergies.isNotEmpty()
                val isEmergency = patient.priority == "Emergency"
                val borderStrokeCol = if (isEmergency) Color(0xFFEF4444) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, borderStrokeCol),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(patient.name.take(2).uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(patient.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("Queue #${patient.queueNumber}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Text("Age: ${patient.age} | Gender: ${patient.gender} | Blood: ${patient.bloodGroup.substringBefore(" ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isEmergency) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFEE2E2))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("EMERGENCY CASE", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.Bold)
                                }
                            }
                            if (hasAllergies) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFEDD5))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("ALLERGIES RECORDED", color = Color(0xFFD97706), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Editable Vitals Section
            SectionHeader(title = "Clinical Vitals Intake")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bpVal,
                            onValueChange = { bpVal = it },
                            label = { Text("Blood Pressure") },
                            placeholder = { Text("120/80") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = hrVal,
                            onValueChange = { hrVal = it },
                            label = { Text("Heart Rate (bpm)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tempVal,
                            onValueChange = { tempVal = it },
                            label = { Text("Temp (°F)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = o2Val,
                            onValueChange = { o2Val = it },
                            label = { Text("O2 Sat (%)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = heightVal,
                            onValueChange = { heightVal = it },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = weightVal,
                            onValueChange = { weightVal = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    
                    // Pain Scale slider (0 to 10)
                    Column {
                        val painVal = painScaleVal.toInt()
                        val painColor = if (painVal <= 3) Color(0xFF10B981) else if (painVal <= 6) Color(0xFFF59E0B) else Color(0xFFEF4444)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pain Scale Level: $painVal / 10", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = painColor)
                        }
                        Slider(
                            value = painScaleVal,
                            onValueChange = { painScaleVal = it },
                            valueRange = 0f..10f,
                            steps = 9
                        )
                    }
                }
            }

            // Diagnosis Section
            SectionHeader(title = "Clinical Diagnosis & Findings")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = chiefComplaint, onValueChange = { chiefComplaint = it }, label = { Text("Chief Complaint") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = symptomsInput, onValueChange = { symptomsInput = it }, label = { Text("Symptoms Details") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = primaryDiag, onValueChange = { primaryDiag = it }, label = { Text("Primary Diagnosis") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = secondaryDiag, onValueChange = { secondaryDiag = it }, label = { Text("Secondary Diagnosis") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = clinicalImpression, onValueChange = { clinicalImpression = it }, label = { Text("Clinical Impression") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = diagNotes, onValueChange = { diagNotes = it }, label = { Text("Diagnostic Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            }

            // Prescription Desk
            SectionHeader(title = "Prescriptions Registry (Write RX)")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = medName, onValueChange = { medName = it }, label = { Text("Medicine Name") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = medDosage, onValueChange = { medDosage = it }, label = { Text("Dosage") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = medDuration, onValueChange = { medDuration = it }, label = { Text("Duration") }, modifier = Modifier.weight(1f))
                    }
                    
                    Text("Frequency / Intervals:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = morningSelected, onCheckedChange = { morningSelected = it })
                            Text("Morning", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = afternoonSelected, onCheckedChange = { afternoonSelected = it })
                            Text("Afternoon", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = nightSelected, onCheckedChange = { nightSelected = it })
                            Text("Night", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    OutlinedTextField(value = medInstructions, onValueChange = { medInstructions = it }, label = { Text("Usage instructions (e.g. before food)") }, modifier = Modifier.fillMaxWidth())
                    
                    Button(
                        onClick = {
                            if (medName.isNotBlank() && medDosage.isNotBlank()) {
                                DoctorWorkspaceState.currentPrescriptions.add(
                                    PrescriptionItem(
                                        id = "med_gen_${System.currentTimeMillis()}",
                                        medicineName = medName,
                                        dosage = medDosage,
                                        morning = morningSelected,
                                        afternoon = afternoonSelected,
                                        night = nightSelected,
                                        duration = medDuration,
                                        instructions = medInstructions
                                    )
                                )
                                medName = ""
                                medDosage = ""
                                medDuration = ""
                                medInstructions = ""
                                morningSelected = false
                                afternoonSelected = false
                                nightSelected = false
                            } else {
                                Toast.makeText(context, "Please enter medicine name and dosage", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Rx List")
                    }
                }
            }

            // Prescribed medications items list
            if (DoctorWorkspaceState.currentPrescriptions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DoctorWorkspaceState.currentPrescriptions.forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.medicineName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    val schedules = mutableListOf<String>()
                                    if (item.morning) schedules.add("Morning")
                                    if (item.afternoon) schedules.add("Afternoon")
                                    if (item.night) schedules.add("Night")
                                    val schedText = if (schedules.isNotEmpty()) schedules.joinToString("-") else "As needed"
                                    Text(
                                        text = "${item.dosage} • $schedText • Duration: ${item.duration}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (item.instructions.isNotBlank()) {
                                        Text(text = "Note: ${item.instructions}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(onClick = { DoctorWorkspaceState.currentPrescriptions.remove(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            SectionHeader(title = "Clinical Notes & Treatment Plan")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // AI Consultation Copilot Card
                    MediSlotCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SmartToy, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Consultation Copilot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AssistChip(
                                    onClick = {
                                        activeDocAiFeature = "soap"
                                        viewModel.generateSoapNote("Patient observations: $observationInput. Vitals details: $clinicalNotesInput.")
                                    },
                                    label = { Text("Generate SOAP") }
                                )
                                AssistChip(
                                    onClick = {
                                        activeDocAiFeature = "enhance"
                                        viewModel.enhanceClinicalNotes(clinicalNotesInput)
                                    },
                                    label = { Text("Enhance Notes") }
                                )
                                AssistChip(
                                    onClick = {
                                        activeDocAiFeature = "diff"
                                        viewModel.loadDifferentialDiagnosis(observationInput, "Vitals: $clinicalNotesInput", "No relevant medical history")
                                    },
                                    label = { Text("Differential Diagnosis") }
                                )
                                AssistChip(
                                    onClick = {
                                        activeDocAiFeature = "presc_draft"
                                        viewModel.loadPrescriptionDraft(observationInput, "Vitals: $clinicalNotesInput", "None")
                                    },
                                    label = { Text("Draft Prescription") }
                                )
                                AssistChip(
                                    onClick = {
                                        activeDocAiFeature = "lab_interp"
                                        viewModel.interpretLabReport("CBC / Routine blood check", "Hb 11.2, WBC 12500, Platelets 180k, Creatinine 0.9")
                                    },
                                    label = { Text("Explain Lab Report") }
                                )
                                AssistChip(
                                    onClick = {
                                        activeDocAiFeature = "education"
                                        viewModel.generatePatientEducation("Standard viral infection", "Bed rest, high hydration, paracetamol on fever spike")
                                    },
                                    label = { Text("Patient Education") }
                                )
                                AssistChip(
                                    onClick = {
                                        activeDocAiFeature = "referral"
                                        viewModel.generateReferralLetter(patient.name, patient.age, "Acute Viral Infection", "Severe fatigue and headache. Normal chest auscultation.", "Internal Medicine")
                                    },
                                    label = { Text("Referral Letter") }
                                )
                                AssistChip(
                                    onClick = {
                                        activeDocAiFeature = "discharge"
                                        viewModel.generateDischargeSummary("3 days inpatient stay for acute fever.", "IV fluids, Antibiotics (Ceftriaxone 1g BD)", "Tab Cefixime 200mg BD for 5 days")
                                    },
                                    label = { Text("Discharge Summary") }
                                )
                            }
                        }
                    }

                    OutlinedTextField(value = observationInput, onValueChange = { observationInput = it }, label = { Text("Clinical Observations (Subjective)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = clinicalNotesInput, onValueChange = { clinicalNotesInput = it }, label = { Text("Internal Clinical Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = treatmentPlanInput, onValueChange = { treatmentPlanInput = it }, label = { Text("Treatment Plan Detail") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = recommendationsInput, onValueChange = { recommendationsInput = it }, label = { Text("Special Recommendations") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = lifestyleInput, onValueChange = { lifestyleInput = it }, label = { Text("Lifestyle & Nutrition Advice") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = referralNotesInput, onValueChange = { referralNotesInput = it }, label = { Text("Referral Notes / Department Handover") }, modifier = Modifier.fillMaxWidth())
                }
            }

            // Lab Investigations Checklist
            SectionHeader(title = "Request Lab Investigations")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val labTests = listOf("CBC", "Blood Test", "Urine Test", "ECG", "X-Ray", "MRI", "CT Scan", "Ultrasound", "Liver Function Test", "Kidney Function Test")
                    
                    labTests.forEach { test ->
                        val isChecked = DoctorWorkspaceState.currentLabOrders.any { it.testName == test }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checkState ->
                                    if (checkState) {
                                        DoctorWorkspaceState.currentLabOrders.add(LabOrderItem(id = java.util.UUID.randomUUID().toString(), testName = test))
                                    } else {
                                        DoctorWorkspaceState.currentLabOrders.removeAll { it.testName == test }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(test, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Follow-Up
            SectionHeader(title = "Schedule Follow-up Consultation")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box {
                        OutlinedTextField(
                            value = followUpDuration,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Follow-up Interval") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showFollowUpDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(expanded = showFollowUpDropdown, onDismissRequest = { showFollowUpDropdown = false }) {
                            val options = listOf("None", "3 Days", "1 Week", "2 Weeks", "1 Month", "3 Months", "Custom Date")
                            options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        followUpDuration = opt
                                        showFollowUpDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = followUpNotesInput,
                        onValueChange = { followUpNotesInput = it },
                        label = { Text("Follow-up instructions/remarks") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action completes consultation
            val isOffline = DoctorWorkspaceState.dutyStatus == DoctorDutyStatus.OFFLINE
            MediSlotButton(
                text = "Complete Consultation Summary",
                onClick = { showCompleteDialog = true },
                enabled = !isOffline,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // AI Consultation Copilot Overlay Dialogs
    if (activeDocAiFeature != null) {
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        AlertDialog(
            onDismissRequest = { activeDocAiFeature = null },
            title = {
                Text(
                    text = when (activeDocAiFeature) {
                        "soap" -> "AI SOAP Note Drafting"
                        "enhance" -> "AI Documentation Enhancement"
                        "diff" -> "AI Differential Diagnoses suggestions"
                        "presc_draft" -> "AI Prescription Draft Assistant"
                        "lab_interp" -> "AI Laboratory Findings Explanation"
                        "education" -> "AI Patient Education Summary"
                        "referral" -> "AI Referral Letter Drafting"
                        "discharge" -> "AI Discharge Summary Drafting"
                        else -> "AI Assistant"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val fallbackState = when (activeDocAiFeature) {
                        "soap" -> soapState
                        "enhance" -> enhanceState
                        "diff" -> diffState
                        "presc_draft" -> prescDraftState
                        "lab_interp" -> labState
                        "education" -> eduState
                        "referral" -> refState
                        "discharge" -> dischargeState
                        else -> null
                    }
                    if (fallbackState is AiState.Success && fallbackState.isFallback) {
                        val formattedTime = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(fallbackState.timestamp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Previous AI Recommendation", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                Text("Generated earlier on $formattedTime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    if (fallbackState is AiState.Success && fallbackState.isMock) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sample Recommendation",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                // We can also use align if we wanted, but Box does it beautifully
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    when (activeDocAiFeature) {
                        "soap" -> {
                            when (val state = soapState) {
                                is AiState.Loading -> AiLoadingCard(loadingText = "Drafting clinical SOAP notes...")
                                is AiState.Failure -> AiErrorCard(state.error, onRetry = { viewModel.generateSoapNote("Patient observations: $observationInput. Vitals details: $clinicalNotesInput.") })
                                is AiState.Success -> {
                                    val data = state.data
                                    Text("Subjective:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text(data.subjective)
                                    Text("Objective:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text(data.objective)
                                    Text("Assessment:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text(data.assessment)
                                    Text("Plan:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text(data.plan)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    MediSlotButton(
                                        text = "Apply SOAP Note to Fields",
                                        onClick = {
                                            observationInput = data.subjective
                                            clinicalNotesInput = data.objective
                                            treatmentPlanInput = data.assessment
                                            recommendationsInput = data.plan
                                            activeDocAiFeature = null
                                            Toast.makeText(context, "SOAP notes populated!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                else -> {}
                            }
                        }
                        "enhance" -> {
                            when (val state = enhanceState) {
                                is AiState.Loading -> AiLoadingCard(loadingText = "Enhancing notes structure...")
                                is AiState.Failure -> AiErrorCard(state.error, onRetry = { viewModel.enhanceClinicalNotes(clinicalNotesInput) })
                                is AiState.Success -> {
                                    val data = state.data
                                    Text(data.enhancedNotes)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    MediSlotButton(
                                        text = "Apply Enhanced Notes to SOAP",
                                        onClick = {
                                            clinicalNotesInput = data.enhancedNotes
                                            activeDocAiFeature = null
                                            Toast.makeText(context, "Notes enhanced successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                else -> {}
                            }
                        }
                        "diff" -> {
                            when (val state = diffState) {
                                is AiState.Loading -> AiLoadingCard(loadingText = "Calculating differential diagnoses...")
                                is AiState.Failure -> AiErrorCard(state.error, onRetry = { viewModel.loadDifferentialDiagnosis(observationInput, "Vitals: $clinicalNotesInput", "") })
                                is AiState.Success -> {
                                    val data = state.data
                                    Text("Possible Diagnoses:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    data.possibleDiagnoses.forEach { Text("• $it") }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Suggested Investigations:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    data.suggestedInvestigations.forEach { Text("• $it") }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Red Flag Warnings:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color(0xFFEF4444))
                                    data.redFlags.forEach { Text("• $it", color = Color(0xFFEF4444)) }
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(data.disclaimer, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                else -> {}
                            }
                        }
                        "presc_draft" -> {
                            when (val state = prescDraftState) {
                                is AiState.Loading -> AiLoadingCard(loadingText = "Formulating prescription categories...")
                                is AiState.Failure -> AiErrorCard(state.error, onRetry = { viewModel.loadPrescriptionDraft(observationInput, "Vitals: $clinicalNotesInput", "") })
                                is AiState.Success -> {
                                    val data = state.data
                                    Text("Recommended Medicine Categories:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    data.medicineCategories.forEach { Text("• $it") }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Supportive Lifestyle Adjustments:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    data.lifestyleAdvice.forEach { Text("• $it") }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Follow-up Recommendation:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text(data.followUpRecommendations)
                                }
                                else -> {}
                            }
                        }
                        "lab_interp" -> {
                            when (val state = labState) {
                                is AiState.Loading -> AiLoadingCard(loadingText = "Summarizing pathological outputs...")
                                is AiState.Failure -> AiErrorCard(state.error, onRetry = { viewModel.interpretLabReport("CBC", "Hb 11.2, WBC 12500") })
                                is AiState.Success -> {
                                    val data = state.data
                                    Text(data.summary)
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (data.abnormalValues.isNotEmpty()) {
                                        Text("Abnormal Markers:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color(0xFFEA580C))
                                        data.abnormalValues.forEach { Text("• $it", color = Color(0xFFEA580C)) }
                                    }
                                    if (data.criticalValues.isNotEmpty()) {
                                        Text("Critical Alerts:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color(0xFFEF4444))
                                        data.criticalValues.forEach { Text("• $it", color = Color(0xFFEF4444)) }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Follow-up Tests to Order:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    data.possibleFollowUpTests.forEach { Text("• $it") }
                                }
                                else -> {}
                            }
                        }
                        "education" -> {
                            when (val state = eduState) {
                                is AiState.Loading -> AiLoadingCard(loadingText = "Drafting patient home-care guide...")
                                is AiState.Failure -> AiErrorCard(state.error, onRetry = { viewModel.generatePatientEducation("Standard viral infection", "Rest & fluid") })
                                is AiState.Success -> {
                                    val data = state.data
                                    Text(data.instructions)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Warning Symptoms to seek care immediately:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color(0xFFEF4444))
                                    data.warningSigns.forEach { Text("• $it", color = Color(0xFFEF4444)) }
                                }
                                else -> {}
                            }
                        }
                        "referral" -> {
                            when (val state = refState) {
                                is AiState.Loading -> AiLoadingCard(loadingText = "Drafting referral documentation...")
                                is AiState.Failure -> AiErrorCard(state.error, onRetry = { viewModel.generateReferralLetter("John Connor", 35, "Cardiac Murmur", "Echocardiogram suggested.", "Cardiologist") })
                                is AiState.Success -> {
                                    val data = state.data
                                    Text(data.letter)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    MediSlotButton(
                                        text = "Copy Referral Letter",
                                        onClick = {
                                            val clip = android.content.ClipData.newPlainText("EHR Referral Letter", data.letter)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Referral letter copied!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                else -> {}
                            }
                        }
                        "discharge" -> {
                            when (val state = dischargeState) {
                                is AiState.Loading -> AiLoadingCard(loadingText = "Generating discharge outline...")
                                is AiState.Failure -> AiErrorCard(state.error, onRetry = { viewModel.generateDischargeSummary("Fever case", "IV Fluids", "Antibiotics") })
                                is AiState.Success -> {
                                    val data = state.data
                                    Text(data.letter)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    MediSlotButton(
                                        text = "Copy Discharge Summary",
                                        onClick = {
                                            val clip = android.content.ClipData.newPlainText("EHR Discharge Summary", data.letter)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Discharge summary copied!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDocAiFeature = null }) {
                    Text("Close")
                }
            }
        )
    }
}

// ----------------------------------------------------
// Expanded Doctor Consultation History Screen
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorHistoryScreen(
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf("All") }
    
    val history = DoctorWorkspaceState.consultationHistory

    val filtered = history.filter { item ->
        val matchesSearch = item.patientName.contains(searchQuery, ignoreCase = true) || 
                            item.diagnosis.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (activeCategory) {
            "All" -> true
            "Today" -> item.dateCategory == "Today"
            "Yesterday" -> item.dateCategory == "Yesterday"
            "Last Week" -> item.dateCategory == "Last Week"
            "Last Month" -> item.dateCategory == "Last Month"
            else -> true
        }
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Consultation Logs History", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search past patients or diagnoses...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Category scroll row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cats = listOf("All", "Today", "Yesterday", "Last Week", "Last Month")
                cats.forEach { cat ->
                    val isSelected = activeCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (filtered.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No past consultations found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filtered) { item ->
                        MediSlotCard {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.patientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(item.dateCategory, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Diagnosis: ${item.diagnosis}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Duration: ${item.durationMinutes} mins", style = MaterialTheme.typography.bodySmall)
                                    Text("Rx Count: ${item.prescriptionCount} | Lab: ${item.labOrdersCount}", style = MaterialTheme.typography.bodySmall)
                                }
                                if (item.followUp != "None") {
                                    Text("Follow-Up: ${item.followUp}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFEA580C), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Slot Management Screen
// ----------------------------------------------------
@Composable
fun SlotManagementScreen(
    onNavigateBack: () -> Unit
) {
    val timesList = listOf(
        "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM",
        "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
        "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM",
        "04:00 PM", "04:30 PM"
    )
    val activeSlots = remember { mutableStateListOf("09:00 AM", "10:30 AM", "11:00 AM", "02:30 PM", "04:00 PM") }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Manage Consulting Slots", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "Configure Duty Hours",
                subtitle = "Toggle availability intervals. Active hours appear highlighted in primary blue."
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(timesList) { time ->
                    val isActive = activeSlots.contains(time)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .border(BorderStroke(1.dp, if (isActive) Color.Transparent else Color(0xFF334155)), RoundedCornerShape(12.dp))
                            .clickable {
                                if (isActive) {
                                    activeSlots.remove(time)
                                } else {
                                    activeSlots.add(time)
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = time,
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            MediSlotButton(
                text = "Save Duty Configuration",
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )
        }
    }
}

// ----------------------------------------------------
// Expanded Doctor Profile Screen
// ----------------------------------------------------
@Composable
fun DoctorProfileScreen(
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val profile = DoctorWorkspaceState.doctorProfile
    val context = LocalContext.current
    
    var editSpecialty by remember { mutableStateOf(profile.specialization) }
    var editQualification by remember { mutableStateOf(profile.qualification) }
    var editExperience by remember { mutableStateOf(profile.experience) }
    var editLicense by remember { mutableStateOf(profile.licenseNumber) }
    var editHours by remember { mutableStateOf(profile.dutyHours) }
    var editAvgTime by remember { mutableStateOf(profile.averageConsultationTime.toString()) }
    var editLang by remember { mutableStateOf(profile.languages) }
    var editCertifications by remember { mutableStateOf(profile.certifications) }
    var editPublications by remember { mutableStateOf(profile.researchPublications) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Professional Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(value = editSpecialty, onValueChange = { editSpecialty = it }, label = { Text("Specialization Area") })
                    OutlinedTextField(value = editQualification, onValueChange = { editQualification = it }, label = { Text("Qualifications") })
                    OutlinedTextField(value = editExperience, onValueChange = { editExperience = it }, label = { Text("Experience Years") })
                    OutlinedTextField(value = editLicense, onValueChange = { editLicense = it }, label = { Text("Medical License ID") })
                    OutlinedTextField(value = editHours, onValueChange = { editHours = it }, label = { Text("Duty Shift Hours") })
                    OutlinedTextField(value = editAvgTime, onValueChange = { editAvgTime = it }, label = { Text("Avg Consultation (m)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = editLang, onValueChange = { editLang = it }, label = { Text("Languages Spoken") })
                    OutlinedTextField(value = editCertifications, onValueChange = { editCertifications = it }, label = { Text("Board Certifications") })
                    OutlinedTextField(value = editPublications, onValueChange = { editPublications = it }, label = { Text("Research & Publications") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val avgTimeInt = editAvgTime.toIntOrNull() ?: profile.averageConsultationTime
                        DoctorWorkspaceState.doctorProfile = profile.copy(
                            specialization = editSpecialty,
                            qualification = editQualification,
                            experience = editExperience,
                            licenseNumber = editLicense,
                            dutyHours = editHours,
                            averageConsultationTime = avgTimeInt,
                            languages = editLang,
                            certifications = editCertifications,
                            researchPublications = editPublications
                        )
                        DoctorWorkspaceState.recalculateEstimatedWaitTimes()
                        showEditDialog = false
                        Toast.makeText(context, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Doctor Workspace Profile",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Profile Header Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = profile.name.split(" ").filter { it.isNotEmpty() && !it.contains(".") }.take(2).map { it.first() }.joinToString("").uppercase()
                        Text(text = initials, color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = profile.specialization, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(text = profile.hospital, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Stats row (Taller statistic boxes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(95.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Text("Treated Patients", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(profile.patientsTreated.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(95.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Text("Success Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${profile.successRate}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            }

            // Professional Details
            SectionHeader(title = "Professional Information")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(label = "Department", value = profile.department)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(label = "Medical License ID", value = profile.licenseNumber)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(label = "Hospital Email", value = profile.hospitalEmail)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(label = "Contact Number", value = profile.contactNumber)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(label = "Languages Spoken", value = profile.languages)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(label = "Qualifications", value = profile.qualification)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(label = "Clinic Experience", value = profile.experience)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(label = "Board Certifications", value = profile.certifications)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(label = "Publications", value = profile.researchPublications)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    DetailRow(label = "Memberships", value = profile.professionalMemberships)
                }
            }

            // Edit Profile Button
            MediSlotButton(
                text = "Edit Workspace Credentials",
                onClick = {
                    editSpecialty = profile.specialization
                    editQualification = profile.qualification
                    editExperience = profile.experience
                    editLicense = profile.licenseNumber
                    editHours = profile.dutyHours
                    editAvgTime = profile.averageConsultationTime.toString()
                    editLang = profile.languages
                    editCertifications = profile.certifications
                    editPublications = profile.researchPublications
                    showEditDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
