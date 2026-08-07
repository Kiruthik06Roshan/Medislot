package com.medislot.app.ui.screens.hospital

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GasMeter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.medislot.app.data.model.*
import com.medislot.app.ui.components.*
import com.medislot.app.ui.theme.LocalDimens
import com.medislot.app.ui.theme.SOSGradient
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.medislot.app.viewmodel.AiState
import com.medislot.app.ui.ai.components.*
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.OutlinedTextField

@Composable
fun HospitalDashboardScreen(
    onNavigateToDoctors: () -> Unit,
    onNavigateToResources: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToStaffScheduling: () -> Unit,
    onNavigateToDoctorRecruitment: () -> Unit,
    onLogout: () -> Unit,
    viewModel: com.medislot.app.viewmodel.AiViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    hospitalViewModel: com.medislot.app.viewmodel.HospitalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val resourceState by hospitalViewModel.resourceState.collectAsState()
    val activeAlerts = resourceState.alerts.count { !it.isResolved && (it.severity == "Critical" || it.severity == "High") }
    val icuResource = resourceState.icu
    val oxygenResource = resourceState.oxygen

    // AI state collectors
    val briefingState by viewModel.dailyBriefingState.collectAsState()
    val insightsState by viewModel.operationalInsightsState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val aiStatus by viewModel.aiStatus.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadDailyBriefing("Morning/Afternoon Shift, ICU at 85% occupancy", "Staff shortage in pediatrics department, ED overload risk high")
        viewModel.loadOperationalInsights("82", "148", "4", "10")
    }

    var showBroadcastDialog by remember { mutableStateOf(false) }

    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("Trigger Emergency Broadcast?", fontWeight = FontWeight.Bold) },
            text = { Text("This will transmit an urgent SOS broadcast alert to all active duty staff, doctors on duty, and clinical rooms. Proceed with caution.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBroadcastDialog = false
                        Toast.makeText(context, "🟢 Emergency broadcast transmitted successfully!", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text("Broadcast SOS", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Hospital Admin Desk",
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Welcome, Admin",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "City General Hospital",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Secondary subtle Date/Time/Operational header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "July 22, 2026",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Text(
                    text = "04:50 PM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                com.medislot.app.ui.ai.components.AiStatusIndicator(status = aiStatus)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Daily Briefing Card
            SectionHeader(title = "AI Daily Briefing", subtitle = "Today's hospital operations run-sheet")
            Spacer(modifier = Modifier.height(10.dp))
            when (val state = briefingState) {
                is AiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        ThinkingAnimation()
                    }
                }
                is AiState.Failure -> {
                    AiErrorCard(
                        errorText = state.error,
                        onRetry = { viewModel.loadDailyBriefing("Morning/Afternoon Shift, ICU at 85% occupancy", "Staff shortage in pediatrics department, ED overload risk high") }
                    )
                }
                is AiState.Success -> {
                    val briefing = state.data
                    MediSlotCard(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (state.isFallback) {
                                val formattedTime = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(state.timestamp))
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
                            if (state.isMock) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sample Recommendation",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SmartToy, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Current Shift Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(briefing.todaySummary, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Busy Hours Pred:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(briefing.predictedBusyHours, style = MaterialTheme.typography.bodySmall)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Risk Severity:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444))
                                    Text(briefing.criticalAlerts.firstOrNull() ?: "Low Risk", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Operational Recommendations:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            briefing.recommendations.forEach { recommendation ->
                                Text("• $recommendation", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dashboard KPI row (scrollable, compact)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KPICard(
                    icon = Icons.Default.Person,
                    number = "24",
                    label = "Today's Admissions",
                    trend = "+12%",
                    trendPositive = true
                )
                KPICard(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    number = "18",
                    label = "Today's Discharges",
                    trend = "+5%",
                    trendPositive = true
                )
                KPICard(
                    icon = Icons.Default.Warning,
                    number = "3",
                    label = "Emergency Cases",
                    trend = "-2%",
                    trendPositive = false
                )
                KPICard(
                    icon = Icons.Default.MedicalServices,
                    number = "14",
                    label = "Doctors On Duty",
                    trend = "Stable",
                    trendPositive = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Operational Alerts Bar
            if (activeAlerts > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(colors = SOSGradient))
                        .clickable { onNavigateToAlerts() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Operational Alerts Active", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("You have $activeAlerts unresolved critical alerts. Review now.", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Hospital Load progress card
            SectionHeader(title = "Operational Load")
            Spacer(modifier = Modifier.height(12.dp))
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hospital Intake Level", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        StatusChip(status = "Critical (82%)")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { 0.82f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.error,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stat Grid
            SectionHeader(title = "Primary Resources")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ICU bed card
                val icuPctUsed = if (icuResource.total > 0) (icuResource.occupied * 100) / icuResource.total else 0
                MediSlotCard(
                    onClick = onNavigateToResources,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Icon(Icons.Default.Bed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("ICU Beds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${icuResource.available}/${icuResource.total} free", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$icuPctUsed% occupancy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }

                // Oxygen reserves card
                val oxygenPctUsed = if (oxygenResource.totalCylinder > 0) ((oxygenResource.totalCylinder - oxygenResource.availableCylinder) * 100) / oxygenResource.totalCylinder else 0
                MediSlotCard(
                    onClick = onNavigateToResources,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Icon(Icons.Default.GasMeter, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Oxygen Reserves", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${oxygenResource.availableCylinder} Cyl", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$oxygenPctUsed% depletion", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Grid (6 items)
            SectionHeader(title = "Quick Actions Desk")
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        icon = Icons.Default.People,
                        title = "Allocate Doctor",
                        subtitle = "Manage doctor schedules",
                        onClick = onNavigateToDoctors,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Default.Bed,
                        title = "Manage Beds",
                        subtitle = "Check bed allocations",
                        onClick = onNavigateToResources,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        icon = Icons.Default.Warning,
                        title = "Emergency Broadcast",
                        subtitle = "Trigger hospital-wide alert",
                        onClick = { showBroadcastDialog = true },
                        modifier = Modifier.weight(1f),
                        isEmergency = true
                    )
                    QuickActionCard(
                        icon = Icons.Default.Notifications,
                        title = "View Critical Alerts",
                        subtitle = "$activeAlerts unresolved issues",
                        onClick = onNavigateToAlerts,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        icon = Icons.Default.Schedule,
                        title = "Staff Scheduling",
                        subtitle = "Update nurse & doctor shifts",
                        onClick = onNavigateToStaffScheduling,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Default.LocalHospital,
                        title = "Resource Allocation",
                        subtitle = "Manage backup stocks",
                        onClick = onNavigateToResources,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        icon = Icons.Default.PersonSearch,
                        title = "Doctor Applications",
                        subtitle = "Review applicant credentials",
                        onClick = onNavigateToDoctorRecruitment,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Default.Analytics,
                        title = "Analytics Desk",
                        subtitle = "View detailed reports",
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Operational Insights Card
            SectionHeader(title = "AI Operational Insights", subtitle = "Machine-learning hospital load diagnostics")
            Spacer(modifier = Modifier.height(10.dp))
            when (val state = insightsState) {
                is AiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        ThinkingAnimation()
                    }
                }
                is AiState.Failure -> {
                    AiErrorCard(
                        errorText = state.error,
                        onRetry = { viewModel.loadOperationalInsights("82", "148", "4", "10") }
                    )
                }
                is AiState.Success -> {
                    val insights = state.data
                    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (state.isFallback) {
                                val formattedTime = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(state.timestamp))
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
                            if (state.isMock) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sample Recommendation",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "EHR Load Analytics Insights",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text("Bottlenecks Identified:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444))
                            insights.bottlenecks.forEach { item ->
                                Text("• $item", style = MaterialTheme.typography.bodySmall)
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Summary Analysis (Priority: ${insights.priorityLevel}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(insights.summary, style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Suggested Improvements:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            insights.suggestedImprovements.forEach { item ->
                                Text("• $item", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Admissions overview stats
            SectionHeader(title = "Daily Operational Summary")
            Spacer(modifier = Modifier.height(12.dp))
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Today's Admissions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("24 patients", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.15f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Patients in Queue", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("30 waiting", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.15f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Registered Duty Staff", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("56 online staff", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun KPICard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    number: String,
    label: String,
    trend: String? = null,
    trendPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = modifier.width(135.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                if (trend != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (trend.lowercase() == "stable" || trendPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (trend.lowercase() == "stable") MaterialTheme.colorScheme.primary else if (trendPositive) Color(0xFF22C55E) else Color(0xFFEF4444),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = trend,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (trend.lowercase() == "stable") MaterialTheme.colorScheme.primary else if (trendPositive) Color(0xFF22C55E) else Color(0xFFEF4444)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(number, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEmergency: Boolean = false
) {
    MediSlotCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEmergency) Color(0xFFEF4444).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEmergency) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DoctorManagementScreen(onNavigateBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("All") }
    val departments = listOf("All", "Cardiology", "Neurology", "Orthopedics", "Pediatrics")

    val filteredDoctors = MockData.doctors.filter { doc ->
        (selectedDept == "All" || doc.department == selectedDept) &&
                doc.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Doctor Allocation", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            MediSlotSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search doctor by name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(departments) { dept ->
                    val isSelected = selectedDept == dept
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedDept = dept }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = dept,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (filteredDoctors.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.PersonSearch,
                    title = "No Staff Found",
                    message = "We couldn't find any doctors matching '$searchQuery' under $selectedDept.",
                    modifier = Modifier.fillMaxSize().padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredDoctors) { doctor ->
                        MediSlotCard {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(doctor.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${doctor.department} • ${doctor.room}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    StatusChip(status = doctor.status)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = doctor.shift,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = doctor.availability,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
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

// ==========================================================
// Resource Monitoring Screen
// ==========================================================
@Composable
fun ResourceMonitoringScreen(
    onNavigateBack: () -> Unit,
    viewModel: com.medislot.app.viewmodel.AiViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    hospitalViewModel: com.medislot.app.viewmodel.HospitalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val resourceState by hospitalViewModel.resourceState.collectAsState()
    val aiRecommendationState by hospitalViewModel.aiRecommendationState.collectAsState()
    val aiStatus by viewModel.aiStatus.collectAsState()

    var selectedMedicine by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        hospitalViewModel.fetchAiRecommendations(forceRefresh = false)
    }

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Resource Monitor",
                onBackClick = onNavigateBack,
                actions = {
                    com.medislot.app.ui.ai.components.AiStatusIndicator(status = aiStatus)
                }
            )
        }
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
                title = "Real-Time Resources",
                subtitle = "Operational thresholds tracking for emergency backups."
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. AI Recommendation / Safety Optimization Plans
                item {
                    when (val state = aiRecommendationState) {
                        is AiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                                ThinkingAnimation()
                            }
                        }
                        is AiState.Failure -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { hospitalViewModel.fetchAiRecommendations(forceRefresh = true) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Failed: ${state.error}. Tap to retry AI recommendations...", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        is AiState.Success -> {
                            val data = state.data
                            MediSlotCard(
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.secondary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("AI Safety & Capacity Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Text(
                                            text = "Request Audit",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.clickable { hospitalViewModel.fetchAiRecommendations(forceRefresh = true) }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(data.summary, style = MaterialTheme.typography.bodySmall)
                                    
                                    if (data.resourceAllocation.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("SUGGESTED REALLOCATIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                        data.resourceAllocation.forEach { item ->
                                            Text("• $item", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    if (data.capacityPlanning.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("CAPACITY PREPAREDNESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                        data.capacityPlanning.forEach { item ->
                                            Text("• $item", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { hospitalViewModel.fetchAiRecommendations(forceRefresh = true) }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tap to generate AI capacity recommendations...", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // 2. Simulation Event Control Panel
                item {
                    MediSlotCard(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simulation Control Panel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "Simulate active hospital events to test real-time alerts, recomputed stats, and reactive logs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Patient flows
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MediSlotSecondaryButton(
                                    text = "Admit Patient (+Beds)",
                                    onClick = { hospitalViewModel.admitPatient() },
                                    modifier = Modifier.weight(1f)
                                )
                                MediSlotSecondaryButton(
                                    text = "Discharge (-Beds)",
                                    onClick = { hospitalViewModel.dischargePatient() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MediSlotSecondaryButton(
                                    text = "Admit ICU (+ICU)",
                                    onClick = { hospitalViewModel.admitToIcu() },
                                    modifier = Modifier.weight(1f)
                                )
                                MediSlotSecondaryButton(
                                    text = "Discharge ICU (-ICU)",
                                    onClick = { hospitalViewModel.dischargeFromIcu() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MediSlotSecondaryButton(
                                    text = "Assign Amb (+Busy)",
                                    onClick = { hospitalViewModel.assignAmbulance() },
                                    modifier = Modifier.weight(1f)
                                )
                                MediSlotSecondaryButton(
                                    text = "Release Amb (-Busy)",
                                    onClick = { hospitalViewModel.releaseAmbulance() },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Dispense Medicine Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1.5f)) {
                                    OutlinedTextField(
                                        value = selectedMedicine,
                                        onValueChange = { selectedMedicine = it },
                                        placeholder = { Text("E.g., Paracetamol 500mg", fontSize = 12.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                MediSlotButton(
                                    text = "Dispense Med",
                                    onClick = {
                                        if (selectedMedicine.isNotBlank()) {
                                            hospitalViewModel.dispenseMedicine(selectedMedicine.trim())
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Issue Blood Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1.5f)) {
                                    OutlinedTextField(
                                        value = selectedBloodGroup,
                                        onValueChange = { selectedBloodGroup = it },
                                        placeholder = { Text("E.g., O- or A+", fontSize = 12.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                MediSlotButton(
                                    text = "Issue Blood",
                                    onClick = {
                                        if (selectedBloodGroup.isNotBlank()) {
                                            hospitalViewModel.issueBlood(selectedBloodGroup.trim().uppercase())
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Oxygen reserves trigger
                            MediSlotSecondaryButton(
                                text = "Consume Oxygen Cylinder",
                                onClick = { hospitalViewModel.useOxygen() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Beds Card
                item {
                    val beds = resourceState.beds
                    val bedsProgress = if (beds.totalBeds > 0) beds.availableBeds.toFloat() / beds.totalBeds.toFloat() else 0f
                    MediSlotCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("General Ward Beds", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                StatusChip(status = if (beds.availableBeds < 10) "Low Available" else "Normal")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Occupied: ${beds.occupiedBeds} • Free: ${beds.availableBeds} • Total: ${beds.totalBeds}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { bedsProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = if (beds.availableBeds < 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                }

                // ICU Beds Card
                item {
                    val icu = resourceState.icu
                    val icuProgress = if (icu.total > 0) icu.available.toFloat() / icu.total.toFloat() else 0f
                    MediSlotCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ICU Ward Beds", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                StatusChip(status = if (icu.available == 0) "ICU Full" else if (icu.available < 3) "Critical" else "Normal")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Occupied: ${icu.occupied} • Free: ${icu.available} • Total: ${icu.total}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { icuProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = if (icu.available < 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                }

                // Oxygen Reserves Card
                item {
                    val oxy = resourceState.oxygen
                    val oxyProgress = if (oxy.totalCylinder > 0) oxy.availableCylinder.toFloat() / oxy.totalCylinder.toFloat() else 0f
                    MediSlotCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.GasMeter, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Oxygen Cylinder Reserves", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                StatusChip(status = if (oxy.availableCylinder < oxy.threshold) "Low Stock" else "Safe")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Available: ${oxy.availableCylinder} Cylinders • Total: ${oxy.totalCylinder} (Threshold: ${oxy.threshold})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { oxyProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = if (oxy.availableCylinder < oxy.threshold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                }

                // Ambulance Fleet Card
                item {
                    val amb = resourceState.ambulances
                    val ambTotal = amb.available + amb.busy
                    val ambProgress = if (ambTotal > 0) amb.available.toFloat() / ambTotal.toFloat() else 0f
                    MediSlotCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalHospital, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ambulance Fleet Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                StatusChip(status = if (amb.available == 0) "No Ambulance" else "Normal")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Available: ${amb.available} • Dispatched/Busy: ${amb.busy} • Total: $ambTotal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { ambProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = if (amb.available == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                }

                // Medicines Inventory
                item {
                    MediSlotCard {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MedicalServices, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pharmacy Medicines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            resourceState.medicines.forEach { med ->
                                val isLow = med.quantity < med.threshold
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(med.medicineName, style = MaterialTheme.typography.bodyMedium)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${med.quantity} unit",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isLow) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            StatusChip(status = "Low")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Blood Bank Reserves
                item {
                    MediSlotCard {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalHospital, null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Blood Bank Reserves", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            resourceState.bloodBank.forEach { blood ->
                                val isLow = blood.units < 5
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Blood Group ${blood.bloodGroup}", style = MaterialTheme.typography.bodyMedium)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${blood.units} units",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isLow) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            StatusChip(status = "Critical")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Medical Devices & Equipment
                item {
                    MediSlotCard {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Analytics, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Medical Devices & Equipment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            resourceState.equipment.forEach { eq ->
                                val isMaint = eq.status == "Maintenance"
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(eq.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text("Type: ${eq.type}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        StatusChip(status = eq.status)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isMaint) "Fix Equipment" else "Maintenance",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
                                                if (isMaint) {
                                                    hospitalViewModel.completeEquipmentMaintenance(eq.id)
                                                } else {
                                                    hospitalViewModel.maintainEquipment(eq.id)
                                                }
                                            }
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

// ==========================================================
// Operations Alerts Screen
// ==========================================================
@Composable
fun AlertsScreen(
    onNavigateBack: () -> Unit,
    viewModel: com.medislot.app.viewmodel.AiViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    hospitalViewModel: com.medislot.app.viewmodel.HospitalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val resourceState by hospitalViewModel.resourceState.collectAsState()
    val alertsList = resourceState.alerts
    val briefingState by viewModel.dailyBriefingState.collectAsState()
    val notificationState by viewModel.notificationState.collectAsState()
    val aiStatus by viewModel.aiStatus.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDailyBriefing("Morning/Afternoon Shift, ICU at 85% occupancy", "Staff shortage in pediatrics department, ED overload risk high")
        viewModel.generateNotificationAlert("Critical Alert: ICU occupancy at 95%. Staff shortages reported in Ward B. High triage volumes.")
    }

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Operations Alerts",
                onBackClick = onNavigateBack,
                actions = {
                    com.medislot.app.ui.ai.components.AiStatusIndicator(status = aiStatus)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (alertsList.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CheckCircle,
                    title = "All Alerts Resolved",
                    message = "No operational alerts active. The hospital systems and resources are functioning within normal parameters.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    item {
                        // AI Alert Copilot Card
                        MediSlotCard(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI Risk Alert Copilot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                when (val state = briefingState) {
                                    is AiState.Loading -> ThinkingAnimation()
                                    is AiState.Success -> {
                                        if (state.isFallback) {
                                            val formattedTime = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(state.timestamp))
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
                                        if (state.isMock) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Sample Recommendation",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                        Text("SYSTEM OVERLOAD RISK PREDICTION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                        Text("Predicted overload risk indicators based on current shift loads:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        state.data.criticalAlerts.forEach { Text("⚠ $it", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444)) }
                                    }
                                    else -> {}
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("AI NOTIFICATION GENERATOR DRAFT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                when (val state = notificationState) {
                                    is AiState.Loading -> ThinkingAnimation()
                                    is AiState.Failure -> Text("Failed to generate broadcast notification text.", color = MaterialTheme.colorScheme.error)
                                    is AiState.Success -> {
                                        val draft = state.data.notificationText
                                        if (state.isFallback) {
                                            val formattedTime = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(state.timestamp))
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
                                        if (state.isMock) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Sample Recommendation",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .padding(10.dp)
                                        ) {
                                            Text(draft, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            MediSlotSecondaryButton(
                                                text = "Copy Text",
                                                onClick = {
                                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                    val clip = android.content.ClipData.newPlainText("EHR Risk Alert Broadcast", draft)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "Broadcast text copied!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                            MediSlotButton(
                                                text = "Send Broadcast",
                                                onClick = {
                                                    Toast.makeText(context, "🟢 Broadcast successfully dispatched to all duty staff!", Toast.LENGTH_LONG).show()
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }

                    items(alertsList) { alert ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (alert.severity) {
                                    "Critical" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
                                    "High" -> Color(0xFFF59E0B).copy(alpha = 0.08f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = when (alert.severity) {
                                    "Critical" -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                    "High" -> Color(0xFFF59E0B).copy(alpha = 0.3f)
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (alert.severity) {
                                                    "Critical" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                    "High" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (alert.severity) {
                                                "Critical", "High" -> Icons.Default.Warning
                                                else -> Icons.Default.Info
                                            },
                                            contentDescription = null,
                                            tint = when (alert.severity) {
                                                "Critical" -> MaterialTheme.colorScheme.error
                                                "High" -> Color(0xFFF59E0B)
                                                else -> MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = alert.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = when (alert.severity) {
                                                    "Critical" -> MaterialTheme.colorScheme.error
                                                    else -> MaterialTheme.colorScheme.onBackground
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            val formattedTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(alert.timestamp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = formattedTime,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            StatusChip(status = alert.severity)
                                            Text(
                                                text = "Dept: ${alert.department}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = alert.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            hospitalViewModel.resolveAlert(alert.id)
                                            Toast.makeText(context, "Alert resolved successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Resolve", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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

// ==========================================================
// Analytics Screen
// ==========================================================
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: com.medislot.app.viewmodel.AiViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    hospitalViewModel: com.medislot.app.viewmodel.HospitalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val depts = MockData.departmentsUsage
    val resourceState by hospitalViewModel.resourceState.collectAsState()
    val analyticsState by hospitalViewModel.resourceAnalytics.collectAsState()
    val briefingState by viewModel.dailyBriefingState.collectAsState()
    val aiStatus by viewModel.aiStatus.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDailyBriefing("Morning/Afternoon Shift, ICU at 85% occupancy", "Staff shortage in pediatrics department, ED overload risk high")
    }

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Hospital Analytics",
                onBackClick = onNavigateBack,
                actions = {
                    com.medislot.app.ui.ai.components.AiStatusIndicator(status = aiStatus)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // AI Executive Performance Report Card
            when (val state = briefingState) {
                is AiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        ThinkingAnimation()
                    }
                }
                is AiState.Failure -> {
                    AiErrorCard(
                        errorText = state.error,
                        onRetry = { viewModel.loadDailyBriefing("Morning/Afternoon Shift, ICU at 85% occupancy", "Staff shortage in pediatrics department, ED overload risk high") }
                    )
                }
                is AiState.Success -> {
                    val report = state.data
                    MediSlotCard(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (state.isFallback) {
                                val formattedTime = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(state.timestamp))
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
                            if (state.isMock) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sample Recommendation",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SmartToy, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Executive Performance Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(report.todaySummary, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Primary Risk Vectors:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444))
                            report.criticalAlerts.forEach { alert ->
                                Text("• $alert", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "Key Operations Analytics")
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bed occupancy %
                    MediSlotCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Icon(Icons.Default.Bed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Bed Occupancy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${analyticsState.bedOccupancyPercentage.toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("General ward rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    // ICU utilization %
                    MediSlotCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Icon(Icons.Default.Bed, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("ICU Beds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${analyticsState.icuOccupancyPercentage.toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("ICU ward rate", style = MaterialTheme.typography.labelSmall, color = if (analyticsState.icuOccupancyPercentage > 85) Color(0xFFEF4444) else Color(0xFF22C55E))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Ambulance utilization
                    MediSlotCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Icon(Icons.Default.LocalHospital, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ambulance Util", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${analyticsState.ambulanceUtilizationPercentage.toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Fleet in service", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Medicines consumed
                    MediSlotCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Icon(Icons.Default.MedicalServices, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Meds Dispensed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${analyticsState.medicineConsumptionCount} units", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Dispensing cycle", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Oxygen consumed
                    MediSlotCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Icon(Icons.Default.GasMeter, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Oxygen Consumed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${analyticsState.oxygenConsumptionCount} Cyl", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Oxygen reserves pull", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Blood issued
                    MediSlotCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Icon(Icons.Default.LocalHospital, null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Blood Units Issued", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${analyticsState.bloodConsumptionCount} units", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Transfusion pool pull", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444))
                        }
                    }
                }

                // Weekly Occupancy Trend
                MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Weekly Occupancy Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Stable operational average: 82%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusChip(status = "Stable")
                    }
                }
            }

            SectionHeader(title = "Admissions Intake Trends")

            // Mini admissions bar chart layout
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Monthly Admissions Rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Average patient intake distributions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        ChartBar("Jan", 0.4f, MaterialTheme.colorScheme.primary)
                        ChartBar("Feb", 0.65f, MaterialTheme.colorScheme.primary)
                        ChartBar("Mar", 0.8f, MaterialTheme.colorScheme.primary)
                        ChartBar("Apr", 0.5f, MaterialTheme.colorScheme.primary)
                        ChartBar("May", 0.9f, MaterialTheme.colorScheme.error)
                        ChartBar("Jun", 0.72f, MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Department load stats
            SectionHeader(title = "Department Load Occupancy")
            if (depts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Analytics,
                    title = "No Analytics Data",
                    message = "There is no load occupancy statistics recorded for departments currently.",
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    depts.forEach { dept ->
                        MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(dept.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text("Patients in queue: ${dept.patientsWaiting}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${dept.loadPercentage}% occupancy",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dept.loadPercentage > 85) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { dept.loadPercentage.toFloat() / 100f },
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(6.dp)
                                            .clip(CircleShape),
                                        color = if (dept.loadPercentage > 85) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(
                title = "Live Simulator Logs",
                subtitle = "Real-time updates of hospital inventory operations."
            )
            if (resourceState.logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No simulator logs generated yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    resourceState.logs.forEach { log ->
                        val logTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                        MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(log.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(logTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChartBar(label: String, fillFraction: Float, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.width(36.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp * fillFraction)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
