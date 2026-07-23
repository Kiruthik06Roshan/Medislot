package com.medislot.app.ui.screens.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.data.model.MockData
import com.medislot.app.ui.components.AppointmentCard
import com.medislot.app.ui.components.MediSlotButton
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.components.MediSlotSecondaryButton
import com.medislot.app.ui.components.MediSlotTopBar
import com.medislot.app.ui.components.MetricCard
import com.medislot.app.ui.components.QuickActionButton
import com.medislot.app.ui.components.SectionHeader
import com.medislot.app.ui.components.StatusChip
import com.medislot.app.ui.theme.LocalDimens
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import com.medislot.app.viewmodel.AiState
import com.medislot.app.ui.ai.components.*
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import android.widget.Toast
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun Modifier.clickScale(onClick: () -> Unit): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isPressed = false
                    }
                    onClick()
                }
            )
        }
}

@Composable
fun ShimmerPlaceholder(
    modifier: Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Box(
        modifier = modifier
            .background(Color(0xFF334155).copy(alpha = alpha), shape = RoundedCornerShape(12.dp))
    )
}

@Composable
fun DashboardShimmer() {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
    ) {
        // Greeting shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                ShimmerPlaceholder(modifier = Modifier.size(100.dp, 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerPlaceholder(modifier = Modifier.size(180.dp, 28.dp))
            }
            ShimmerPlaceholder(modifier = Modifier.size(48.dp).clip(CircleShape))
        }

        // Live Queue Shimmer
        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(160.dp))

        // Quick Actions Shimmer
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ShimmerPlaceholder(modifier = Modifier.weight(1f).height(100.dp))
                ShimmerPlaceholder(modifier = Modifier.weight(1f).height(100.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ShimmerPlaceholder(modifier = Modifier.weight(1f).height(100.dp))
                ShimmerPlaceholder(modifier = Modifier.weight(1f).height(100.dp))
            }
        }

        // Health Reminders Shimmer
        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(140.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PatientDashboardScreen(
    onNavigateToSymptomChecker: () -> Unit,
    onNavigateToDoctorSearch: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToHospitalMap: () -> Unit,
    onNavigateToBooking: (String) -> Unit,
    onNavigateToQueue: (String) -> Unit,
    viewModel: com.medislot.app.viewmodel.AiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val unreadNotifsCount = MockData.notifications.count { !it.isRead }
    val activeAppointment = MockData.appointments.firstOrNull() 

    // Loading & Refreshing States
    var isScreenLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()

    // Track local taken medicines for prototype responsiveness
    val takenMeds = remember { mutableStateListOf<String>() }

    // AI dialog triggers
    var isChatOpen by remember { mutableStateOf(false) }
    var isDietOpen by remember { mutableStateOf(false) }
    var isPrepOpen by remember { mutableStateOf(false) }
    var chatMessageText by remember { mutableStateOf("") }

    val chatState by viewModel.chatState.collectAsState()
    val dietState by viewModel.dietRecommendationState.collectAsState()
    val prepState by viewModel.appointmentPrepState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val aiStatus by viewModel.aiStatus.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Trigger initial load and fetch health tips
    LaunchedEffect(Unit) {
        viewModel.loadDailyHealthTips(29, "Female", "Mild Hypertension", "Healthy nutrition, moderate physical activity")
        delay(800)
        isScreenLoading = false
    }

    // Trigger pull-to-refresh reload
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            isScreenLoading = true
            viewModel.clearCache()
            viewModel.loadDailyHealthTips(29, "Female", "Mild Hypertension", "Healthy nutrition, moderate physical activity")
            delay(1000)
            isScreenLoading = false
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "MediSlot Health",
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifsCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text(text = unreadNotifsCount.toString(), color = Color.White)
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
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isChatOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI Health Assistant"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { pullDistance = 0f },
                        onDragEnd = {
                            if (pullDistance > 180f && scrollState.value == 0) {
                                isRefreshing = true
                            }
                            pullDistance = 0f
                        },
                        onDragCancel = { pullDistance = 0f },
                        onDrag = { change, dragAmount ->
                            if (scrollState.value == 0 && dragAmount.y > 0) {
                                pullDistance += dragAmount.y
                                change.consume()
                            }
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (isScreenLoading) {
                    DashboardShimmer()
                } else {
                    // Greeting & Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Hello,",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = MockData.patientProfile.name,
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.medislot.app.ui.ai.components.AiStatusIndicator(status = aiStatus)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .clickable { onNavigateToSettings() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = MockData.patientProfile.name.take(2).uppercase(),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 1. Live Queue Card Section (Scenario 1-4 Logic)
                    SectionHeader(
                        title = "My Consultation Desk",
                        subtitle = "Real-time updates for your medical scheduling"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        activeAppointment == null -> {
                            // Scenario 1: User has no appointment
                            MediSlotCard(modifier = Modifier.fillMaxWidth().clickScale { }) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Active Appointment",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "You don't have any upcoming consultations.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        MediSlotSecondaryButton(
                                            text = "Search Doctors",
                                            onClick = onNavigateToDoctorSearch,
                                            modifier = Modifier.weight(1f)
                                        )
                                        MediSlotButton(
                                            text = "Book Appointment",
                                            onClick = { onNavigateToBooking("any") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                        activeAppointment.status == "Upcoming" -> {
                            // Scenario 2: Active Appointment Booked
                            val roomNumber = when (activeAppointment.doctorName) {
                                "Dr. John Doe" -> "Room 4B (Cardiology)"
                                "Dr. Helen Cho" -> "Room 102 (Neurology)"
                                "Dr. Marcus Vance" -> "Room 205 (Orthopedics)"
                                "Dr. Sarah Jenkins" -> "Room 301 (Pediatrics)"
                                else -> "Room 104 (General Medicine)"
                            }
                            val waitTime = activeAppointment.queueNumber * 3

                            MediSlotCard(
                                modifier = Modifier.fillMaxWidth().clickScale { onNavigateToQueue(activeAppointment.id) }
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = activeAppointment.doctorName,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${activeAppointment.department} • ${activeAppointment.hospital}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        StatusChip(status = "Active")
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "QUEUE POSITION",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "#${activeAppointment.queueNumber}",
                                                style = MaterialTheme.typography.displayMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "ESTIMATED WAIT",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "~$waitTime mins",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Located at $roomNumber",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        MediSlotSecondaryButton(
                                            text = "AI Prep Checklist",
                                            onClick = {
                                                isPrepOpen = true
                                                viewModel.loadAppointmentPrep(
                                                    activeAppointment.doctorName,
                                                    activeAppointment.department,
                                                    "General checkup, vitals logs, and health tips follow-up"
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        MediSlotButton(
                                            text = "Track Live Queue",
                                            onClick = { onNavigateToQueue(activeAppointment.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                        activeAppointment.status == "Completed" -> {
                            // Scenario 3: Consultation Completed
                            val matchedDoc = MockData.doctors.find { it.name == activeAppointment.doctorName }
                            val docIdToPass = matchedDoc?.id ?: "any"

                            MediSlotCard(modifier = Modifier.fillMaxWidth().clickScale { }) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Consultation Completed",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "With ${activeAppointment.doctorName} • ${activeAppointment.department}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        StatusChip(status = "Completed")
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Your medical consultation is complete. You can download the report files or prescription documents.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        MediSlotSecondaryButton(
                                            text = "View Prescription",
                                            onClick = onNavigateToRecords,
                                            modifier = Modifier.weight(1.3f)
                                        )
                                        MediSlotButton(
                                            text = "Book Follow-up",
                                            onClick = { onNavigateToBooking(docIdToPass) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                        activeAppointment.status == "Cancelled" -> {
                            // Scenario 4: Consultation Cancelled
                            MediSlotCard(modifier = Modifier.fillMaxWidth().clickScale { }) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Appointment Cancelled",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444)
                                            )
                                            Text(
                                                text = "With ${activeAppointment.doctorName} • ${activeAppointment.department}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        StatusChip(status = "Cancelled")
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "This consultation was cancelled. You can easily schedule another checkup session below.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    MediSlotButton(
                                        text = "Book Another Appointment",
                                        onClick = { onNavigateToBooking("any") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Quick Actions Grid
                    SectionHeader(title = "Quick Actions")
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.Search,
                                label = "Search Doctors",
                                sublabel = "Find by name or specialty",
                                onClick = onNavigateToDoctorSearch,
                                iconColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f).clickScale { onNavigateToDoctorSearch() }
                            )
                            QuickActionButton(
                                icon = Icons.Default.CalendarMonth,
                                label = "Book Consultation",
                                sublabel = "Schedule new checkup",
                                onClick = { onNavigateToBooking("any") },
                                iconColor = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f).clickScale { onNavigateToBooking("any") }
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            QuickActionButton(
                                icon = Icons.Default.SmartToy,
                                label = "AI Assistant",
                                sublabel = "Check symptoms safely",
                                onClick = onNavigateToSymptomChecker,
                                iconColor = Color(0xFF10B981),
                                modifier = Modifier.weight(1f).clickScale { onNavigateToSymptomChecker() }
                            )
                            QuickActionButton(
                                icon = Icons.Default.Map,
                                label = "Hospital Map",
                                sublabel = "Google Maps Navigation",
                                onClick = onNavigateToHospitalMap,
                                iconColor = Color(0xFFEAB308),
                                modifier = Modifier.weight(1f).clickScale { onNavigateToHospitalMap() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Hero Section: Health Score Card
                    MediSlotCard(
                        modifier = Modifier.fillMaxWidth().clickScale { }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Health Score",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your health indexes are looking premium. Keep up the good work!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(72.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { 0.92f },
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    strokeWidth = 6.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Text(
                                    text = "92",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. Health Reminders (Medicine and Upcoming Tests)
                    SectionHeader(title = "Health Reminders")
                    Spacer(modifier = Modifier.height(12.dp))
                    MediSlotCard(modifier = Modifier.fillMaxWidth().clickScale { }) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Medication,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Medicine Reminder",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Lisinopril 10mg (1x daily)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Scheduled for 08:00 PM",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                val isTaken = takenMeds.contains("Lisinopril")
                                MediSlotSecondaryButton(
                                    text = if (isTaken) "Taken ✓" else "Mark Taken",
                                    onClick = {
                                        if (!isTaken) takenMeds.add("Lisinopril")
                                    },
                                    enabled = !isTaken,
                                    modifier = Modifier.width(110.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Science,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Upcoming Diagnostic Test",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Complete Blood Count (CBC)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Tomorrow at 09:00 AM • Lab C",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFEAB308),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 4. Recent Consultations
                    SectionHeader(
                        title = "Recent Consultations",
                        subtitle = "Easily book follow-up visits"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(MockData.doctors.take(2)) { doc ->
                            Box(
                                modifier = Modifier
                                    .width(280.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickScale { onNavigateToBooking(doc.id) }
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalHospital,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = doc.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = doc.department,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = doc.hospital.take(20) + "...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Book again",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 5. Daily Vitals Section
                    SectionHeader(title = "My Daily Vitals")
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MetricCard(
                                label = "Heart Rate",
                                value = "74",
                                unit = "BPM",
                                icon = Icons.Default.Favorite,
                                iconColor = Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                label = "Blood Pressure",
                                value = "118/79",
                                unit = "mmHg",
                                icon = Icons.Default.HealthAndSafety,
                                iconColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MetricCard(
                                label = "SpO2 Oxygen",
                                value = "98",
                                unit = "%",
                                icon = Icons.Default.WaterDrop,
                                iconColor = Color(0xFF3B82F6),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                label = "Body Temp",
                                value = "98.6",
                                unit = "°F",
                                icon = Icons.Default.Thermostat,
                                iconColor = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        MetricCard(
                            label = "Fasting Blood Sugar",
                            value = "95",
                            unit = "mg/dL",
                            icon = Icons.Default.Description,
                            iconColor = Color(0xFF10B981),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    MediSlotSecondaryButton(
                        text = "Generate AI Diet Plan",
                        onClick = {
                            isDietOpen = true
                            viewModel.loadDietRecommendations("Mild Hypertension", "58 kg", "20.5")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 6. Nearby Hospitals Section
                    SectionHeader(
                        title = "Nearby Hospitals",
                        subtitle = "Real-time emergency load information"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val nearbyHospitals = listOf(
                            Triple("City General Hospital", "1.2 km", "Busy"),
                            Triple("Metro Health Medical Center", "3.5 km", "Normal"),
                            Triple("Children's Specialized Hospital", "5.8 km", "Normal")
                        )
                        nearbyHospitals.forEach { (hospName, dist, status) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { onNavigateToHospitalMap() }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = hospName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "$dist away",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    StatusChip(status = status)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 7. Recent Reports Quick Access
                    SectionHeader(
                        title = "Recent Lab Reports",
                        subtitle = "Click to view full record files"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            MockData.patientProfile.labReports.take(2).forEach { report ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToRecords() }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = report.testName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = report.date,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    StatusChip(status = report.status)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToRecords() },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "View All Reports",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 8. Daily Health Tips Carousel
                    val tipsState by viewModel.dailyTipsState.collectAsState()
                    SectionHeader(title = "Daily Health Tip (AI Personalized)")
                    Spacer(modifier = Modifier.height(12.dp))
                    when (val state = tipsState) {
                        is AiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                ThinkingAnimation()
                            }
                        }
                        is AiState.Failure -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.loadDailyHealthTips(29, "Female", "Mild Hypertension", "Healthy nutrition, moderate physical activity") }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Retry loading personalized tips...", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        is AiState.Success -> {
                            val data = state.data
                            val tipCategories = listOf(
                                "Hydration" to data.hydration,
                                "Exercise" to data.exercise,
                                "Sleep" to data.sleep,
                                "Nutrition" to data.nutrition,
                                "Mental Wellness" to data.mentalWellness
                            )
                            var activeIndex by remember { mutableStateOf(0) }
                            val activeTip = tipCategories[activeIndex]
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { activeIndex = (activeIndex + 1) % tipCategories.size }
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.HealthAndSafety,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeTip.first.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = activeTip.second,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (state.isMock) {
                                            Text(
                                                text = "Sample Recommendation",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Text(
                                            text = "Tip ${activeIndex + 1} of ${tipCategories.size} (Tap for next category)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("Tip details unavailable.")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 9. Emergency Quick SOS Panel
                    MediSlotCard(
                        onClick = onNavigateToEmergency,
                        border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().clickScale { onNavigateToEmergency() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Emergency,
                                        contentDescription = "Emergency",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Emergency SOS Desk",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                    Text(
                                        text = "Instant ambulance dispatch & ER coordinator",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Pull to refresh overlay indicator
            if (isRefreshing || pullDistance > 60f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // AI dialog implementations
    val context = LocalContext.current
    if (isChatOpen) {
        AlertDialog(
            onDismissRequest = { isChatOpen = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Health Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("MediSlot AI Chat History", viewModel.exportChat())
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Chat history copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Export Chat", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { viewModel.clearChat() }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { isChatOpen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxHeight(0.7f).navigationBarsPadding()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.chatHistory) { msg ->
                            val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                            val containerColor = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val textColor = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                            
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(containerColor)
                                        .padding(12.dp)
                                ) {
                                    Text(text = msg.text, color = textColor, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        if (chatState is AiState.Loading) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.CenterStart) {
                                    ThinkingAnimation()
                                }
                            }
                        }
                        if (chatState is AiState.Success) {
                            val data = (chatState as AiState.Success<com.medislot.app.data.ai.ChatResponse>).data
                            if (data.suggestedQuestions.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        data.suggestedQuestions.forEach { question ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.sendChatMessage(question) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(text = question, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessageText,
                            onValueChange = { chatMessageText = it },
                            placeholder = { Text("Ask medical/navigation questions...") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (chatMessageText.isNotBlank()) {
                                    viewModel.sendChatMessage(chatMessageText)
                                    chatMessageText = ""
                                }
                            },
                            enabled = chatMessageText.isNotBlank() && chatState !is AiState.Loading
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (isDietOpen) {
        AlertDialog(
            onDismissRequest = { isDietOpen = false },
            title = {
                Text(
                    text = "Personalized AI Diet Planner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Dietary plans customized according to your medical conditions (Mild Hypertension) and body metrics.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    when (val state = dietState) {
                        is AiState.Loading -> {
                            AiLoadingCard(loadingText = "Drafting nutritional recommendations...")
                        }
                        is AiState.Failure -> {
                            AiErrorCard(
                                errorText = state.error,
                                onRetry = { viewModel.loadDietRecommendations("Mild Hypertension", "58 kg", "20.5") }
                            )
                        }
                        is AiState.Success -> {
                            val data = state.data
                            if (state.isMock) {
                                Text(
                                    text = "* Sample Recommendation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Text("FOODS TO PREFER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                            data.foodsToEat.forEach { Text("✔ $it", style = MaterialTheme.typography.bodyMedium) }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("FOODS TO AVOID", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            data.foodsToAvoid.forEach { Text("✖ $it", style = MaterialTheme.typography.bodyMedium) }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("HYDRATION ADVICE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(text = data.hydration, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("LIFESTYLE GUIDANCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            data.lifestyleAdvice.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isDietOpen = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (isPrepOpen && activeAppointment != null) {
        AlertDialog(
            onDismissRequest = { isPrepOpen = false },
            title = {
                Text(
                    text = "AI Appointment Prep Checklist",
                    style = MaterialTheme.typography.titleMedium,
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
                    Text("Checklist for appointment with ${activeAppointment.doctorName} (${activeAppointment.department})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    when (val state = prepState) {
                        is AiState.Loading -> {
                            AiLoadingCard(loadingText = "Formulating consult checklist...")
                        }
                        is AiState.Failure -> {
                            AiErrorCard(
                                errorText = state.error,
                                onRetry = { viewModel.loadAppointmentPrep(activeAppointment.doctorName, activeAppointment.department, "General fatigue checkup") }
                            )
                        }
                        is AiState.Success -> {
                            val data = state.data
                            if (state.isMock) {
                                Text(
                                    text = "* Sample Recommendation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            data.checklist.forEach { item ->
                                var checked by remember { mutableStateOf(false) }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { checked = !checked }.padding(vertical = 4.dp)
                                ) {
                                    androidx.compose.material3.Checkbox(checked = checked, onCheckedChange = { checked = it })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = item, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isPrepOpen = false }) {
                    Text("Close")
                }
            }
        )
    }
}
