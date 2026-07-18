package com.medislot.app.ui.screens.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.data.model.AppointmentData
import com.medislot.app.data.model.DoctorProfileData
import com.medislot.app.data.model.LabReport
import com.medislot.app.data.model.MockData
import com.medislot.app.data.model.NotificationItem
import com.medislot.app.ui.components.AppointmentCard
import com.medislot.app.ui.components.MediSlotButton
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.components.MediSlotSearchBar
import com.medislot.app.ui.components.MediSlotSecondaryButton
import com.medislot.app.ui.components.MediSlotTextField
import com.medislot.app.ui.components.MediSlotTopBar
import com.medislot.app.ui.components.MetricCard
import com.medislot.app.ui.components.QuickActionButton
import com.medislot.app.ui.components.SectionHeader
import com.medislot.app.ui.components.StatusChip
import com.medislot.app.ui.components.UserRole
import com.medislot.app.ui.theme.LocalDimens
import com.medislot.app.ui.theme.PrimaryGradient
import com.medislot.app.ui.theme.SOSGradient
import com.medislot.app.ui.theme.SecondaryGradient
import kotlinx.coroutines.delay

// ==========================================================
// REUSABLE COMPONENTS
// ==========================================================

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            MediSlotButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier.width(180.dp)
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    StatusChip(status = status)
}

// ==========================================================
// Symptom Checker Screen
// ==========================================================
@Composable
fun SymptomCheckerScreen(onNavigateBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var diagnosisResult by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        topBar = { MediSlotTopBar(title = "AI Symptom Checker", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Explain your symptoms",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select matching symptoms below or search to trigger an offline AI matched assessment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Search input
            MediSlotSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search symptoms (e.g. Chest Pain, Fever)"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Common symptoms section
            SectionHeader(title = "Common Symptoms")
            Spacer(modifier = Modifier.height(8.dp))

            val filteredSymptoms = MockData.symptomCheckerList.filter {
                it.contains(searchQuery, ignoreCase = true)
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filteredSymptoms) { symptom ->
                    val isSelected = selectedSymptoms.contains(symptom)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                if (isSelected) selectedSymptoms.remove(symptom)
                                else selectedSymptoms.add(symptom)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = symptom,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selected Symptoms Tags
            if (selectedSymptoms.isNotEmpty()) {
                Text(
                    text = "Selected Symptoms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedSymptoms.forEach { symptom ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(symptom, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { selectedSymptoms.remove(symptom) }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Submit Button
            MediSlotButton(
                text = "Analyze Symptoms",
                onClick = {
                    if (selectedSymptoms.isEmpty()) return@MediSlotButton
                    // Simple mock diagnostics logic
                    val containsHeart = selectedSymptoms.any { it.lowercase().contains("chest") || it.lowercase().contains("shortness") }
                    val containsFlu = selectedSymptoms.any { it.lowercase().contains("fever") || it.lowercase().contains("cough") || it.lowercase().contains("headache") }
                    diagnosisResult = when {
                        containsHeart -> Pair("Cardiovascular Assessment", "High probability of angina or exertion stress. We strongly advise booking an urgent consultation with our Cardiology department.")
                        containsFlu -> Pair("Viral Syndrome / Influenza", "Indicates standard viral respiratory symptoms. Rest, stay hydrated, and monitor temperature. Consult a physician if symptoms persist beyond 5 days.")
                        else -> Pair("General Wellness Notice", "Symptoms don't match any acute risk flags. Maintain good rest and hydration. Book a checkup if symptoms worsen.")
                    }
                },
                enabled = selectedSymptoms.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Diagnostic assessment card
            AnimatedVisibility(visible = diagnosisResult != null) {
                diagnosisResult?.let { result ->
                    MediSlotCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = result.first,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = result.second,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Note: This is an automated offline assessment and does not replace medical advice.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ==========================================================
// Doctor Search Screen
// ==========================================================
@Composable
fun DoctorSearchScreen(onDoctorClick: (String) -> Unit, onNavigateBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("All") }
    val departments = listOf("All", "Cardiology", "Neurology", "Orthopedics", "Pediatrics")

    val filteredDoctors = MockData.doctors.filter { doc ->
        (selectedDept == "All" || doc.department == selectedDept) &&
                (doc.name.contains(searchQuery, ignoreCase = true) || doc.department.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Find Doctors", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Premium Search Bar
            MediSlotSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search doctor by name or specialty",
                onFilterClick = { /* Click filter */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Department filter chips
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

            // Doctors list
            if (filteredDoctors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Default.Person,
                        title = "No Doctors found",
                        description = "Try refining your query or department filter."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredDoctors) { doc ->
                        MediSlotCard(
                            onClick = { onDoctorClick(doc.id) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(doc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${doc.department} • ${doc.hospital}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = doc.rating.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = doc.availability, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// Doctor Details Screen
// ==========================================================
@Composable
fun DoctorDetailsScreen(
    doctorId: String,
    onBookAppointment: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val doctor = MockData.doctors.find { it.id == doctorId } ?: MockData.doctors[0]

    Scaffold(
        topBar = { MediSlotTopBar(title = "Doctor Details", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile info card
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = doctor.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = doctor.department,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = doctor.hospital,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Structured Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailBadge(label = "Experience", valStr = "8+ Years")
                    DetailBadge(label = "Rating", valStr = "${doctor.rating} ★")
                    DetailBadge(label = "Consultation", valStr = "$45")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Biography
            SectionHeader(title = "Biography")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dr. ${doctor.name.replace("Dr. ", "")} is a leading expert in ${doctor.department} with specialized training in clinical care. Committed to offering patient-centered solutions, utilizing state-of-the-art diagnostic services and therapies.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Work Schedule
            SectionHeader(title = "Availability")
            Spacer(modifier = Modifier.height(8.dp))
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Active Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = doctor.availability, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Online Slots", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Book Button
            MediSlotButton(
                text = "Book Appointment",
                onClick = { onBookAppointment(doctor.id) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailBadge(label: String, valStr: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = valStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ==========================================================
// Appointment Booking Screen
// ==========================================================
@Composable
fun AppointmentBookingScreen(
    doctorId: String,
    onBookingSuccess: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val doctor = MockData.doctors.find { it.id == doctorId } ?: MockData.doctors[0]
    val dates = listOf("Mon, Jul 20", "Tue, Jul 21", "Wed, Jul 22", "Thu, Jul 23", "Fri, Jul 24")
    val slots = listOf("09:00 AM", "10:30 AM", "11:00 AM", "02:00 PM", "03:30 PM", "04:00 PM")

    var selectedDate by remember { mutableStateOf(dates[0]) }
    var selectedSlot by remember { mutableStateOf(slots[0]) }
    var reason by remember { mutableStateOf("") }
    var bookingInProgress by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Schedule Appointment", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Doctor banner
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(doctor.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(doctor.department, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Select Date
            SectionHeader(title = "Select Consultation Date")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(dates) { date ->
                    val isSelected = selectedDate == date
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedDate = date }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = date,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Select Slots
            SectionHeader(title = "Select Available Slots")
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val chunks = slots.chunked(3)
                chunks.forEach { rowSlots ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowSlots.forEach { slot ->
                            val isSelected = selectedSlot == slot
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedSlot = slot }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = slot,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (rowSlots.size < 3) {
                            repeat(3 - rowSlots.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Note/Reason
            SectionHeader(title = "Consultation Reason")
            Spacer(modifier = Modifier.height(12.dp))
            MediSlotTextField(
                value = reason,
                onValueChange = { reason = it },
                label = "Describe your symptoms (optional)",
                placeholder = "e.g. Regular heart checkup, blood pressure concerns"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Confirm Button
            MediSlotButton(
                text = "Confirm Booking",
                onClick = {
                    bookingInProgress = true
                },
                isLoading = bookingInProgress,
                modifier = Modifier.fillMaxWidth()
            )

            LaunchedEffect(bookingInProgress) {
                if (bookingInProgress) {
                    delay(1500)
                    // create new mock appointment
                    val newAppt = AppointmentData(
                        id = "appt_${System.currentTimeMillis()}",
                        doctorName = doctor.name,
                        department = doctor.department,
                        hospital = doctor.hospital,
                        date = selectedDate,
                        time = selectedSlot,
                        status = "Upcoming",
                        queueNumber = 14
                    )
                    MockData.appointments.add(0, newAppt)
                    onBookingSuccess(newAppt.id)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================================
// Queue Waiting Screen
// ==========================================================
@Composable
fun QueueWaitingScreen(appointmentId: String, onNavigateBack: () -> Unit) {
    val appointment = MockData.appointments.find { it.id == appointmentId } ?: MockData.appointments[0]
    var currentQueueNumber by remember { mutableStateOf(14) }
    var estimatedTimeMinutes by remember { mutableStateOf(42) }

    // Pulse animation logic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Simulate queue countdown
    LaunchedEffect(Unit) {
        while (currentQueueNumber > 3) {
            delay(6000)
            currentQueueNumber -= 1
            estimatedTimeMinutes = currentQueueNumber * 3
        }
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Live Queue Tracker", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Live Consultation Status",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Large pulsing circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFF3B82F6).copy(alpha = alphaAnim * 0.15f),
                        radius = size.minDimension / 2
                    )
                    drawCircle(
                        color = Color(0xFF3B82F6),
                        radius = size.minDimension / 2.3f,
                        style = Stroke(width = 6.dp.toPx())
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "#$currentQueueNumber",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your Position",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status Info Card
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Doctor", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = appointment.doctorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        StatusChip(status = "In Progress")
                    }

                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Est. Wait Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "~ $estimatedTimeMinutes mins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Scheduled Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = appointment.time, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Info Notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "A notification will trigger once you are 3 positions away. Please make sure you are near clinic room 104.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ==========================================================
// Appointment History Screen
// ==========================================================
@Composable
fun AppointmentHistoryScreen(onNavigateBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Completed", "Cancelled")

    val filteredAppointments = MockData.appointments.filter { appt ->
        when (selectedTab) {
            0 -> appt.status == "Upcoming"
            1 -> appt.status == "Completed"
            else -> appt.status == "Cancelled"
        }
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Appointments History", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredAppointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Default.CalendarMonth,
                        title = "No Appointments Found",
                        description = "You don't have any appointments listed under the '${tabs[selectedTab]}' tab."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp)
                ) {
                    items(filteredAppointments) { appt ->
                        AppointmentCard(
                            doctorName = appt.doctorName,
                            department = appt.department,
                            time = appt.time,
                            date = appt.date,
                            queueNumber = appt.queueNumber.toString(),
                            status = appt.status,
                            onClick = { /* Detail */ }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================================
// Medical Records Screen
// ==========================================================
@Composable
fun MedicalRecordsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = { MediSlotTopBar(title = "Health Records (EHR)", onBackClick = onNavigateBack) }
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

            // General Profile Summary Card
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(MockData.patientProfile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Age: ${MockData.patientProfile.age} | Gender: ${MockData.patientProfile.gender}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Compact Metric Grid: BMI, Weight, Height, Blood Group (Rule 12 records)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        label = "Blood Group",
                        value = MockData.patientProfile.bloodGroup,
                        unit = "RH+",
                        icon = Icons.Default.LocalHospital,
                        iconColor = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "BMI Rating",
                        value = MockData.patientProfile.bmi,
                        unit = "Index",
                        icon = Icons.Default.Assignment,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        label = "Height",
                        value = MockData.patientProfile.height,
                        unit = "cm",
                        icon = Icons.Default.Bed,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Weight",
                        value = MockData.patientProfile.weight,
                        unit = "kg",
                        icon = Icons.Default.Person,
                        iconColor = Color(0xFF06B6D4),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Medical History
            SectionHeader(title = "Medical History")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MockData.patientProfile.history.forEach { historyItem ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = historyItem, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Allergies
            SectionHeader(title = "Allergies & Reactions")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MockData.patientProfile.allergies.forEach { allergy ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = allergy, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Medications
            SectionHeader(title = "Active Medications")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MockData.patientProfile.medications.forEach { med ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MedicalServices, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = med, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Lab Reports
            SectionHeader(title = "Lab Diagnostic Reports")
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
                                Text(report.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Result: ${report.result}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
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

@Composable
private fun StatGridBox(label: String, value: String, modifier: Modifier = Modifier) {
    // Replaced entirely with standardized MetricCard in MedicalRecordsScreen
}

// ==========================================================
// Emergency Navigation Screen (SOS)
// ==========================================================
@Composable
fun EmergencyScreen(onNavigateBack: () -> Unit) {
    var sosActive by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf(5) }

    LaunchedEffect(sosActive) {
        if (sosActive && countdownSeconds > 0) {
            while (countdownSeconds > 0) {
                delay(1000)
                countdownSeconds -= 1
            }
        }
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Emergency Desk", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (!sosActive) {
                Text(
                    text = "EMERGENCY SOS DESK",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Press the button below to alert local emergency coordinators and request ambulance dispatcher routing immediately.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Beautiful Pulsing SOS Trigger
                val pulseTransition = rememberInfiniteTransition(label = "sosPulse")
                val pulseRadius by pulseTransition.animateFloat(
                    initialValue = 100.dp.value,
                    targetValue = 120.dp.value,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Box(
                    modifier = Modifier
                        .size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Outer Rings
                    Canvas(modifier = Modifier.size(pulseRadius.dp)) {
                        drawCircle(color = Color(0xFFEF4444).copy(alpha = 0.15f))
                    }
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(colors = SOSGradient)
                            )
                            .clickable { sosActive = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "TRIGGER",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                SectionHeader(title = "Primary Emergency Contacts")
                Spacer(modifier = Modifier.height(12.dp))

                MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "City Hospital ER Coordinator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "+1 (555) 911-3044", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(
                            onClick = { /* call */ },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else {
                Text(
                    text = "SOS SIGNAL INITIATING",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Broadcasting your GPS coordinates to ambulance coordinator dispatchers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(60.dp))

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        .border(4.dp, MaterialTheme.colorScheme.error, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (countdownSeconds > 0) countdownSeconds.toString() else "SENT",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (countdownSeconds > 0) "seconds" else "Alert Broadcasted",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))

                MediSlotButton(
                    text = "Cancel Alert",
                    onClick = {
                        sosActive = false
                        countdownSeconds = 5
                    },
                    modifier = Modifier.fillMaxWidth(),
                    gradient = listOf(Color(0xFF475569), Color(0xFF1E293B))
                )
            }
        }
    }
}

// ==========================================================
// Patient Profile Screen (Includes QR code & digital Insurance Card)
// ==========================================================
@Composable
fun PatientProfileScreen(
    role: UserRole = UserRole.PATIENT,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = when (role) {
                    UserRole.PATIENT -> "Patient Profile"
                    UserRole.DOCTOR -> "Doctor Profile"
                    UserRole.HOSPITAL -> "Hospital Profile"
                },
                onBackClick = onNavigateBack
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

            // Avatar Header Card
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (role) {
                                UserRole.PATIENT -> Icons.Default.Person
                                UserRole.DOCTOR -> Icons.Default.MedicalServices
                                UserRole.HOSPITAL -> Icons.Default.LocalHospital
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (role) {
                            UserRole.PATIENT -> MockData.patientProfile.name
                            UserRole.DOCTOR -> MockData.doctors[0].name
                            UserRole.HOSPITAL -> "City General Hospital"
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val memberId = when (role) {
                        UserRole.PATIENT -> "#PAT-8812"
                        UserRole.DOCTOR -> "#DOC-1123"
                        UserRole.HOSPITAL -> "#HOS-0044"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = memberId,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // QR Code Placeholder Card
            if (role == UserRole.PATIENT) {
                SectionHeader(title = "Digital Check-In QR")
                MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(68.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(text = "Scan at Reception", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Use this QR code for rapid check-in and automated queue alignment upon hospital arrival.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Digital Insurance Card overlay (Rule 8 Profile page)
            if (role == UserRole.PATIENT) {
                SectionHeader(title = "Health Insurance Card")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6),
                                    Color(0xFF06B6D4)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "METROLIFE INSURE",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Premium Health Plan",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(Icons.Default.LocalHospital, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        
                        Column {
                            Text(
                                text = MockData.patientProfile.name.uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Policy ID: #POL-8902123",
                                    color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Exp: 12/2028",
                                    color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Contact Information
            SectionHeader(title = "Contact Details")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email Address",
                        value = when (role) {
                            UserRole.PATIENT -> MockData.patientProfile.email
                            UserRole.DOCTOR -> MockData.doctors[0].email
                            UserRole.HOSPITAL -> "admin@cityhospital.org"
                        }
                    )
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))
                    ProfileInfoRow(
                        icon = Icons.Default.Phone,
                        label = "Contact Number",
                        value = when (role) {
                            UserRole.PATIENT -> MockData.patientProfile.contact
                            UserRole.DOCTOR -> MockData.doctors[0].contact
                            UserRole.HOSPITAL -> "+1 (555) 902-1823"
                        }
                    )
                    if (role == UserRole.DOCTOR) {
                        HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))
                        ProfileInfoRow(
                            icon = Icons.Default.CalendarMonth,
                            label = "Consulting Days",
                            value = MockData.doctors[0].availability
                        )
                    }
                }
            }

            // Profile Actions
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { /* mock */ },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Profile Details", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { /* mock */ },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Account Security", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout CTA
            MediSlotButton(
                text = "Sign Out",
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                gradient = SOSGradient
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ==========================================================
// Settings Screen
// ==========================================================
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    var darkModeEnabled by remember { mutableStateOf(true) }
    var notificationEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Settings Parameters", onBackClick = onNavigateBack) }
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

            SectionHeader(title = "App Preferences")

            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Force Premium Dark Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(checked = darkModeEnabled, onCheckedChange = { darkModeEnabled = it })
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Push Notifications", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(checked = notificationEnabled, onCheckedChange = { notificationEnabled = it })
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Biometric Authentication", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(checked = biometricEnabled, onCheckedChange = { biometricEnabled = it })
                    }
                }
            }

            SectionHeader(title = "Data Integrations")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { /* mock */ },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Translate, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Connect Google Fit / Apple Health", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                }
            }

            SectionHeader(title = "Legal & Support")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { /* mock */ },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Privacy Policy", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.PrivacyTip, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { /* mock */ },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contact Technical Support", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ContactPhone, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================================
// Notifications Screen
// ==========================================================
@Composable
fun NotificationsScreen(onNavigateBack: () -> Unit) {
    var notificationsList = remember { mutableStateListOf<NotificationItem>().apply { addAll(MockData.notifications) } }

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Notifications Hub",
                onBackClick = onNavigateBack,
                actions = {
                    Text(
                        text = "Clear All",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { notificationsList.clear() }
                            .padding(16.dp)
                    )
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
            if (notificationsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Default.Notifications,
                        title = "No Notifications",
                        description = "You're all caught up! No recent alerts are pending."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)
                ) {
                    items(notificationsList) { notif ->
                        MediSlotCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (notif.type.lowercase()) {
                                                "emergency" -> Color(0xFFEF4444).copy(alpha = 0.12f)
                                                "medicine" -> Color(0xFF22C55E).copy(alpha = 0.12f)
                                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (notif.type.lowercase()) {
                                            "emergency" -> Icons.Default.Emergency
                                            "medicine" -> Icons.Default.MedicalServices
                                            "appointment" -> Icons.Default.CalendarMonth
                                            else -> Icons.Default.Notifications
                                        },
                                        contentDescription = null,
                                        tint = when (notif.type.lowercase()) {
                                            "emergency" -> Color(0xFFEF4444)
                                            "medicine" -> Color(0xFF22C55E)
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(notif.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(notif.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
