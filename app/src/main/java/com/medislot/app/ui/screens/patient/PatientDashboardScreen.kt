package com.medislot.app.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.data.model.MockData
import com.medislot.app.ui.components.AppointmentCard
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.components.MediSlotStatCard
import com.medislot.app.ui.components.MediSlotTopBar
import com.medislot.app.ui.components.MetricCard
import com.medislot.app.ui.components.QuickActionButton
import com.medislot.app.ui.components.SectionHeader
import com.medislot.app.ui.theme.LocalDimens

@Composable
fun PatientDashboardScreen(
    onNavigateToSymptomChecker: () -> Unit,
    onNavigateToDoctorSearch: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val unreadNotifsCount = MockData.notifications.count { !it.isRead }

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
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
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
                .padding(horizontal = 24.dp) // Exact 24px page padding as requested
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
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
                        style = MaterialTheme.typography.displayLarge, // 32 Bold
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
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

            Spacer(modifier = Modifier.height(24.dp)) // 24dp section spacing

            // Hero Section: Health Score Card
            MediSlotCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Health Score",
                            style = MaterialTheme.typography.titleLarge, // 18 Semibold
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

            // AI Suggestion Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "AI Symptom Checker",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Analyze your symptoms instantly with advanced diagnostics.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onNavigateToSymptomChecker,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp)
                                .height(38.dp)
                        ) {
                            Text(
                                text = "Analyze Now",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Appointment
            val todayAppointment = MockData.appointments.firstOrNull { it.date == "Today" }
            if (todayAppointment != null) {
                SectionHeader(title = "Today's Appointment")
                Spacer(modifier = Modifier.height(12.dp))
                AppointmentCard(
                    doctorName = todayAppointment.doctorName,
                    department = todayAppointment.department,
                    time = todayAppointment.time,
                    date = todayAppointment.date,
                    queueNumber = todayAppointment.queueNumber.toString(),
                    status = todayAppointment.status,
                    onClick = onNavigateToHistory,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Quick Actions Section
            SectionHeader(title = "Quick Actions")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Description,
                    label = "Health Records",
                    sublabel = "Access test results",
                    onClick = onNavigateToRecords,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Emergency,
                    label = "Emergency SOS",
                    sublabel = "Locate ER instantly",
                    onClick = onNavigateToEmergency,
                    iconColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Statistics / Vital Metrics
            SectionHeader(title = "Daily Vitals")
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
            
            Spacer(modifier = Modifier.height(16.dp))

            // General Queue time card
            MediSlotStatCard(
                title = "General Clinic Queue Wait Time",
                value = "12",
                unit = "mins",
                icon = Icons.Default.Search,
                iconColor = MaterialTheme.colorScheme.secondary,
                onClick = onNavigateToDoctorSearch,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Health Tips Section
            SectionHeader(title = "Daily Health Tip")
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MockData.dailyTips.forEach { tip ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Proper footer safe area padding
        }
    }
}
