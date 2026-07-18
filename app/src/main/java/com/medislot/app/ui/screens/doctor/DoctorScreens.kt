package com.medislot.app.ui.screens.doctor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.data.model.MockData
import com.medislot.app.ui.components.MediSlotButton
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.components.MediSlotSecondaryButton
import com.medislot.app.ui.components.MediSlotTextField
import com.medislot.app.ui.components.MediSlotTopBar
import com.medislot.app.ui.components.SectionHeader
import com.medislot.app.ui.components.StatusChip
import com.medislot.app.ui.theme.LocalDimens

@Composable
fun DoctorDashboardScreen(
    onNavigateToAppointments: () -> Unit,
    onNavigateToSlots: () -> Unit,
    onLogout: () -> Unit
) {
    val doctor = MockData.doctors[0]

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Doctor Workspace",
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
        }
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
                text = "Welcome,",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = doctor.name,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${doctor.department} • ${doctor.hospital}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // AI Scheduling Insights Banner
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "AI Scheduling Insights",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Clinic queue flow is optimal today. Suggest opening 2 additional consultation slots between 03:00 PM - 04:00 PM for walk-in buffer.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItemCard(label = "Scheduled", value = "8", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
                StatItemCard(label = "Completed", value = "5", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
                StatItemCard(label = "Pending", value = "2", modifier = Modifier.weight(1f), color = Color(0xFFF59E0B))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Appointments CTA
            MediSlotCard(
                onClick = onNavigateToAppointments,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Today's Consultations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Manage patients scheduled for today.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slots CTA
            MediSlotCard(
                onClick = onNavigateToSlots,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Slot Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Open work intervals & duty parameters.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Consultation Hours details
            SectionHeader(title = "Duty Profile Details")
            Spacer(modifier = Modifier.height(12.dp))
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Assigned Room", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Room 4B (Wing B)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Consultation Hours", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("09:00 AM - 05:00 PM", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Average Duration", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("15 mins per assessment", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatItemCard(label: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f)), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium, // 28 Bold
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ==========================================================
// Doctor Consultations Screen
// ==========================================================
@Composable
fun DoctorAppointmentsScreen(
    onNavigateToPatientDetails: (String) -> Unit,
    onNavigateToUploadPrescription: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val patientQueue = listOf(
        Pair("Sarah Connor", "Time: 10:30 AM • Reason: Annual Cardiovascular Checkup"),
        Pair("Arthur Dent", "Time: 11:00 AM • Reason: Mild Chest Discomfort"),
        Pair("Ellen Ripley", "Time: 11:30 AM • Reason: Post-Surgery Evaluation")
    )

    Scaffold(
        topBar = { MediSlotTopBar(title = "Consultation Schedule", onBackClick = onNavigateBack) }
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
                title = "Live Patient Queue",
                subtitle = "Verify patient health databases or issue diagnostic prescriptions."
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(patientQueue) { (name, details) ->
                    MediSlotCard {
                        Column {
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
                                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MediSlotSecondaryButton(
                                    text = "EHR Details",
                                    onClick = { onNavigateToPatientDetails("patient_456") },
                                    modifier = Modifier
                                        .weight(1f)
                                )
                                MediSlotButton(
                                    text = "Write RX",
                                    onClick = { onNavigateToUploadPrescription("appt_999") },
                                    modifier = Modifier
                                        .weight(1.1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// Patient Medical Profile Detail Screen (Doctor View)
// ==========================================================
@Composable
fun DoctorPatientDetailsScreen(
    patientId: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = { MediSlotTopBar(title = "EHR Patient Database", onBackClick = onNavigateBack) }
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

            // Profile Header Card
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(MockData.patientProfile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "Age: ${MockData.patientProfile.age} | Gender: ${MockData.patientProfile.gender} | ID: #PAT-456",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItemCard(label = "Blood Group", value = MockData.patientProfile.bloodGroup, modifier = Modifier.weight(1f), color = Color(0xFFEF4444))
                StatItemCard(label = "BMI Rating", value = MockData.patientProfile.bmi, modifier = Modifier.weight(1.2f), color = MaterialTheme.colorScheme.secondary)
            }

            // Allergies & Chronic details
            SectionHeader(title = "Clinical Summary")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Known Allergies", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Text(MockData.patientProfile.allergies.joinToString(", "), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))
                    Column {
                        Text("Chronic Conditions History", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        MockData.patientProfile.history.forEach { h ->
                            Text("• $h", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }

            // Lab Reports
            SectionHeader(title = "Recent Diagnostic Lab Reports")
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MockData.patientProfile.labReports.forEach { report ->
                    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(report.testName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Result: ${report.result}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            StatusChip(status = report.status)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================================
// Prescription Desk Screen
// ==========================================================
@Composable
fun PrescriptionUploadScreen(
    appointmentId: String,
    onNavigateBack: () -> Unit
) {
    var prescriptionText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var submitSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Prescription Portal", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (!submitSuccess) {
                SectionHeader(
                    title = "Write Diagnostic Prescription",
                    subtitle = "Input clinical remarks. Submit to encrypt and publish directly to patient EHR."
                )

                // Input fields
                MediSlotTextField(
                    value = prescriptionText,
                    onValueChange = { prescriptionText = it },
                    label = "Medications (e.g. Paracetamol 500mg 2x daily)",
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                MediSlotTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = "Clinical Remarks & Diagnostics Notes",
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                MediSlotButton(
                    text = "Publish Prescription",
                    onClick = { submitSuccess = true },
                    enabled = prescriptionText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Success screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Published Successfully",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Prescription is securely encrypted and published to Sarah Connor's Digital Health Records.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    MediSlotButton(
                        text = "Go Back to Dashboard",
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ==========================================================
// Slot Management Screen
// ==========================================================
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
