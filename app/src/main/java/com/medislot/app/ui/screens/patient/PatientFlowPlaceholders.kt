package com.medislot.app.ui.screens.patient

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.data.model.AppointmentData
import com.medislot.app.data.model.DoctorProfileData
import com.medislot.app.data.model.MockData
import com.medislot.app.ui.components.AppointmentCard
import com.medislot.app.ui.components.MediSlotButton
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.components.MediSlotSearchBar
import com.medislot.app.ui.components.MediSlotSecondaryButton
import com.medislot.app.ui.components.MediSlotTextField
import com.medislot.app.ui.components.MediSlotTopBar
import com.medislot.app.ui.components.SectionHeader
import com.medislot.app.ui.components.StatusChip
import com.medislot.app.ui.components.UserRole
import com.medislot.app.ui.theme.SOSGradient
import kotlinx.coroutines.delay

@Composable
fun StatusBadge(status: String) {
    StatusChip(status = status)
}

@Composable
fun StatGridBox(label: String, value: String, modifier: Modifier = Modifier) {
    MediSlotCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================================
// AI Symptom Checker Screen (Redesigned - Non-diagnostic)
// ==========================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymptomCheckerScreen(
    onBookClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    
    // UI states
    var checkingInProgress by remember { mutableStateOf(false) }
    var isShimmerLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()

    // Non-diagnostic result structures
    var suggestedDept by remember { mutableStateOf("") }
    var suggestedSpecialists by remember { mutableStateOf<List<String>>(emptyList()) }
    var urgencyLevel by remember { mutableStateOf("") } 
    var actionRecommendation by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }
    
    // Expanded AI result attributes
    var confidencePct by remember { mutableStateOf(0) }
    var riskRating by remember { mutableStateOf("") }
    var recommendedWaitingTime by remember { mutableStateOf("") }
    var nearestSpecialistName by remember { mutableStateOf("") }
    var hospitalOccupancyRate by remember { mutableStateOf(0) }
    var estimatedConsultationTime by remember { mutableStateOf(0) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            isShimmerLoading = true
            delay(800)
            isShimmerLoading = false
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "AI Symptom Assistant", onBackClick = onNavigateBack) }
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
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (isShimmerLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(48.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(80.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(160.dp))
                    }
                } else {
                    Text(
                        text = "Explain your symptoms",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 26.sp),
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

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(20.dp))

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
                        text = "Analyze Symptoms Safely",
                        onClick = {
                            if (selectedSymptoms.isEmpty()) return@MediSlotButton
                            checkingInProgress = true
                        },
                        enabled = selectedSymptoms.isNotEmpty() && !checkingInProgress,
                        modifier = Modifier.fillMaxWidth()
                    )

                    LaunchedEffect(checkingInProgress) {
                        if (checkingInProgress) {
                            delay(1200)
                            checkingInProgress = false
                            
                            // Simple safe matching logic mapping to departments
                            val hasUrgent = selectedSymptoms.any { it.contains("Chest", ignoreCase = true) || it.contains("Breath", ignoreCase = true) }
                            val hasNeuro = selectedSymptoms.any { it.contains("Headache", ignoreCase = true) || it.contains("Dizzy", ignoreCase = true) || it.contains("Fatigue", ignoreCase = true) }
                            val hasOrtho = selectedSymptoms.any { it.contains("Joint", ignoreCase = true) || it.contains("Stiffness", ignoreCase = true) }
                            val hasPediatrics = selectedSymptoms.any { it.contains("Fever", ignoreCase = true) || it.contains("Cough", ignoreCase = true) }

                            confidencePct = (85..98).random()
                            hospitalOccupancyRate = (40..85).random()
                            estimatedConsultationTime = (10..45).random()

                            when {
                                hasUrgent -> {
                                    suggestedDept = "Cardiology"
                                    suggestedSpecialists = listOf("Cardiologist", "Pulmonologist", "General Physician")
                                    urgencyLevel = "High"
                                    riskRating = "High Risk"
                                    recommendedWaitingTime = "Immediate (< 2 hrs)"
                                    nearestSpecialistName = "Dr. John Doe (1.5 km)"
                                    actionRecommendation = "Based on symptoms of chest discomfort or breathing difficulties, we recommend booking a priority review with a Cardiologist today. If experiencing acute distress, trigger an Emergency SOS immediately."
                                }
                                hasNeuro -> {
                                    suggestedDept = "Neurology"
                                    suggestedSpecialists = listOf("Neurologist", "Neuro-Physiotherapist", "Internal Medicine Specialist")
                                    urgencyLevel = "Moderate"
                                    riskRating = "Medium Risk"
                                    recommendedWaitingTime = "Within 24 Hours"
                                    nearestSpecialistName = "Dr. Helen Cho (3.2 km)"
                                    actionRecommendation = "Symptoms point toward neurological guidance. Please schedule a standard consultation with a Neurologist within 24-48 hours."
                                }
                                hasOrtho -> {
                                    suggestedDept = "Orthopedics"
                                    suggestedSpecialists = listOf("Orthopedic Surgeon", "Sports Therapist", "Rheumatologist")
                                    urgencyLevel = "Low"
                                    riskRating = "Low Risk"
                                    recommendedWaitingTime = "Within 72 Hours"
                                    nearestSpecialistName = "Dr. Marcus Vance (2.1 km)"
                                    actionRecommendation = "Stiffness and joint symptoms indicate skeletal/muscle framework evaluation. Schedule a consultation with an Orthopedics specialist."
                                }
                                hasPediatrics -> {
                                    suggestedDept = "Pediatrics"
                                    suggestedSpecialists = listOf("Pediatrician", "General Physician", "Immunologist")
                                    urgencyLevel = "Moderate"
                                    riskRating = "Medium Risk"
                                    recommendedWaitingTime = "Within 12 Hours"
                                    nearestSpecialistName = "Dr. Sarah Jenkins (4.5 km)"
                                    actionRecommendation = "General medicine or childhood immunization symptoms indicated. Please schedule a review with a Pediatrician or General Physician."
                                }
                                else -> {
                                    suggestedDept = "General Medicine"
                                    suggestedSpecialists = listOf("General Physician", "Family Medicine Specialist")
                                    urgencyLevel = "Low"
                                    riskRating = "Low Risk"
                                    recommendedWaitingTime = "Self-care & monitor"
                                    nearestSpecialistName = "Clinic Room 104"
                                    actionRecommendation = "Standard viral symptoms or general complaints. Book a general medicine checkup or consult a physician online."
                                }
                            }
                            showResults = true
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // AI Loading Spinner
                    if (checkingInProgress) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Analyzing symptom mapping securely...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Results Card
                    AnimatedVisibility(visible = showResults && !checkingInProgress) {
                        Column {
                            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (urgencyLevel) {
                                                        "High" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                                        "Moderate" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                        else -> Color(0xFF22C55E).copy(alpha = 0.15f)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = when (urgencyLevel) {
                                                    "High" -> Color(0xFFEF4444)
                                                    "Moderate" -> Color(0xFFF59E0B)
                                                    else -> Color(0xFF22C55E)
                                                },
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "AI System Recommendations",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = "Urgency Level: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    text = urgencyLevel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (urgencyLevel) {
                                                        "High" -> Color(0xFFEF4444)
                                                        "Moderate" -> Color(0xFFF59E0B)
                                                        else -> Color(0xFF22C55E)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(text = "RECOMMENDED DEPARTMENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Text(text = suggestedDept, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = "SUGGESTED SPECIALISTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = suggestedSpecialists.joinToString(", "), style = MaterialTheme.typography.bodyMedium)

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Modern AI Stats Chips (Requirement 9)
                                    Text(text = "AI METRICS & STATISTICS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Confidence Chip
                                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                            Text("Confidence: $confidencePct%", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                        // Risk Level Chip
                                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (urgencyLevel == "High") Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                            Text(riskRating, color = if (urgencyLevel == "High") Color(0xFFEF4444) else Color(0xFFF59E0B), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                        // Recommended Wait time chip
                                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                            Text("Wait time: $recommendedWaitingTime", style = MaterialTheme.typography.labelSmall)
                                        }
                                        // Nearest Specialist chip
                                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                            Text("Nearest: $nearestSpecialistName", style = MaterialTheme.typography.labelSmall)
                                        }
                                        // Occupancy Chip
                                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                            Text("Occupancy: $hospitalOccupancyRate%", style = MaterialTheme.typography.labelSmall)
                                        }
                                        // Est consult time chip
                                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                            Text("Consult duration: $estimatedConsultationTime mins", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(text = "ACTION GUIDELINE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = actionRecommendation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Suggested Doctors List
                            SectionHeader(title = "Suggested Specialists Available")
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val matchingDoctors = MockData.doctors.filter { doc ->
                                doc.department.equals(suggestedDept, ignoreCase = true) || suggestedDept == "General Medicine"
                            }

                            if (matchingDoctors.isEmpty()) {
                                Text(text = "No direct specialists found, please schedule a general medicine consultation.")
                            } else {
                                matchingDoctors.forEach { doc ->
                                    MediSlotCard(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickScale { }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = doc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text(text = "${doc.department} • ${doc.hospital}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = "Fees: ${doc.fees} | Availability: Today", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                            MediSlotSecondaryButton(
                                                text = "Book",
                                                onClick = { onBookClick(doc.id) },
                                                modifier = Modifier.width(76.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    // Disclaimer Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Medical Disclaimer",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Medical Disclaimer",
                                    color = Color(0xFFEF4444),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "This tool only helps guide patients toward the appropriate healthcare professional. It does not diagnose diseases or replace medical advice.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Pull to refresh overlay indicator
            if (isRefreshing || pullDistance > 60f) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
}

// ==========================================================
// Doctor Search Screen (Redesigned - Multi-Filter & Sort)
// ==========================================================
@Composable
fun DoctorSearchScreen(
    onDoctorClick: (String) -> Unit,
    onBookClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Filters States
    var selectedDistance by remember { mutableStateOf("All") }
    var selectedRating by remember { mutableStateOf("All") }
    var selectedFee by remember { mutableStateOf("All") }
    var selectedExp by remember { mutableStateOf("All") }
    var availableToday by remember { mutableStateOf(false) }
    var selectedDept by remember { mutableStateOf("All") }

    // Sorting State
    var activeSort by remember { mutableStateOf("Highest Rated") }

    // Shimmer/Refresh states
    var isShimmerLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()

    // Filter Chips Choices
    val departments = listOf("All", "Cardiology", "Neurology", "Orthopedics", "Pediatrics")
    val distances = listOf("All", "< 2 km", "< 5 km", "< 10 km")
    val ratings = listOf("All", "4.5+ ★", "4.8+ ★")
    val fees = listOf("All", "< $100", "< $120")
    val experiences = listOf("All", "10+ Years", "15+ Years")

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            isShimmerLoading = true
            delay(800)
            isShimmerLoading = false
            isRefreshing = false
        }
    }

    // Dynamic mock doctor filtering
    val processedDoctors = remember(
        searchQuery, selectedDistance, selectedRating, selectedFee, selectedExp, availableToday, activeSort, selectedDept
    ) {
        var list = MockData.doctors.filter { doc ->
            val matchesSearch = doc.name.contains(searchQuery, ignoreCase = true) ||
                    doc.department.contains(searchQuery, ignoreCase = true) ||
                    doc.hospital.contains(searchQuery, ignoreCase = true)
            
            // Department filter
            val matchesDept = selectedDept == "All" || doc.department.equals(selectedDept, ignoreCase = true)

            // Experience matching
            val years = doc.experience.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
            val matchesExp = when (selectedExp) {
                "10+ Years" -> years >= 10
                "15+ Years" -> years >= 15
                else -> true
            }

            // Fee matching
            val feeNum = doc.fees.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
            val matchesFee = when (selectedFee) {
                "< $100" -> feeNum < 100
                "< $120" -> feeNum < 120
                else -> true
            }

            // Rating matching
            val matchesRating = when (selectedRating) {
                "4.5+ ★" -> doc.rating >= 4.5f
                "4.8+ ★" -> doc.rating >= 4.8f
                else -> true
            }

            // Distance mapping
            val dist = when (doc.id) {
                "doc_1" -> 1.5f
                "doc_2" -> 3.2f
                "doc_3" -> 2.1f
                else -> 4.5f
            }
            val matchesDist = when (selectedDistance) {
                "< 2 km" -> dist < 2.0f
                "< 5 km" -> dist < 5.0f
                "< 10 km" -> dist < 10.0f
                else -> true
            }

            // Availability matching
            val matchesAvailability = !availableToday || doc.availability.contains("Monday") || doc.availability.contains("Tuesday") || doc.availability.contains("Wednesday") || doc.availability.contains("Today")

            matchesSearch && matchesDept && matchesExp && matchesFee && matchesRating && matchesDist && matchesAvailability
        }

        // Apply Sorting
        list = when (activeSort) {
            "Nearest" -> list.sortedBy { doc ->
                when (doc.id) {
                    "doc_1" -> 1.5f
                    "doc_2" -> 3.2f
                    "doc_3" -> 2.1f
                    else -> 4.5f
                }
            }
            "Highest Rated" -> list.sortedByDescending { it.rating }
            "Earliest Available" -> list.sortedBy { it.slotTimes.firstOrNull() ?: "09:00 AM" }
            else -> list
        }

        list
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Search Doctors", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { pullDistance = 0f },
                        onDragEnd = {
                            if (pullDistance > 180f) {
                                isRefreshing = true
                            }
                            pullDistance = 0f
                        },
                        onDragCancel = { pullDistance = 0f },
                        onDrag = { change, dragAmount ->
                            pullDistance += dragAmount.y
                            change.consume()
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Search input
                MediSlotSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search doctor by name, specialty, or hospital"
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isShimmerLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(80.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(160.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(160.dp))
                    }
                } else {
                    // Expandable Filter Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FilterList, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Refine Doctor Search",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Filter Chips list
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Department
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Dept: ", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(departments) { item ->
                                    val isSelected = selectedDept == item
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { selectedDept = item }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(item, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Distance
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Distance: ", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(distances) { item ->
                                    val isSelected = selectedDistance == item
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { selectedDistance = item }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(item, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        
                        // Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rating: ", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(ratings) { item ->
                                    val isSelected = selectedRating == item
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { selectedRating = item }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(item, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Consultation Fee
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Fee: ", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(fees) { item ->
                                    val isSelected = selectedFee == item
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { selectedFee = item }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(item, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Availability & Today Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Available Today", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = availableToday,
                                    onCheckedChange = { availableToday = it }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sorting Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sort by: ", style = MaterialTheme.typography.bodySmall)
                        listOf("Nearest", "Highest Rated", "Earliest Available").forEach { sortType ->
                            val isSelected = activeSort == sortType
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { activeSort = sortType }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(sortType, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Doctors list
                    if (processedDoctors.isEmpty()) {
                        // Redeigned Empty State View (Requirement 2)
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonSearch,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "No Specialists Available",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "There are currently no doctors available for this department.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MediSlotButton(
                                    text = "Change Department",
                                    onClick = { selectedDept = "All"; searchQuery = "" },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                MediSlotSecondaryButton(
                                    text = "Change Hospital",
                                    onClick = { selectedDistance = "All"; selectedFee = "All"; selectedRating = "All"; selectedExp = "All" },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                MediSlotSecondaryButton(
                                    text = "Go Back",
                                    onClick = onNavigateBack,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(processedDoctors) { doc ->
                                val distance = when (doc.id) {
                                    "doc_1" -> "1.5 km"
                                    "doc_2" -> "3.2 km"
                                    "doc_3" -> "2.1 km"
                                    else -> "4.5 km"
                                }
                                val nextSlot = doc.slotTimes.firstOrNull() ?: "09:00 AM"

                                MediSlotCard(
                                    modifier = Modifier.clickScale { onDoctorClick(doc.id) }
                                ) {
                                    Column {
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
                                                Text(doc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "${doc.department} • ${doc.hospital}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${doc.experience} Experience • $distance away",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            
                                            Column(horizontalAlignment = Alignment.End) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = doc.rating.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                }
                                                Text(text = doc.fees, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Doctor Card Layout Fix (Requirement 1)
                                        // Full width row below time text to prevent text wrapping on Book buttons
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(text = "NEXT AVAILABLE TIME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "Today, $nextSlot", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                MediSlotSecondaryButton(
                                                    text = "Profile",
                                                    onClick = { onDoctorClick(doc.id) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                MediSlotButton(
                                                    text = "Book Appointment",
                                                    onClick = { onBookClick(doc.id) },
                                                    modifier = Modifier.weight(1.3f)
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

            // Pull to refresh overlay indicator
            if (isRefreshing || pullDistance > 60f) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
}

// ==========================================================
// Doctor Details Screen (Redesigned)
// ==========================================================
@Composable
fun DoctorDetailsScreen(
    doctorId: String,
    onBookAppointment: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val doctor = MockData.doctors.find { it.id == doctorId } ?: MockData.doctors[0]
    val context = LocalContext.current
    var selectedSlot by remember { mutableStateOf("") }

    val reviews = listOf(
        Triple("Sarah Connor", 5.0f, "Excellent care and preventative advice. Highly professional approach!"),
        Triple("John Bennett", 4.5f, "Explained my cardiovascular symptoms clearly, wait time was minimal."),
        Triple("Kyle Reese", 5.0f, "A patient-centric expert who really cares about complete recovery.")
    )

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

            // Profile header
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Experience", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(doctor.experience, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Rating", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${doctor.rating} ★", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Consultation Fee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(doctor.fees, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Biography & Education
            SectionHeader(title = "Biography & Credentials")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${doctor.bio} Highly trained clinical expert committed to evidence-based healthcare treatments.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Education: MD - Specialized Medicine, Johns Hopkins University", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(text = "Languages Spoken: English, Spanish, Medical Terminology", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Slots Availability Row/Grid
            SectionHeader(title = "Consulting Available Slots")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Consulting days: ${doctor.availability}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Grid of Slot Times
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                doctor.slotTimes.chunked(3).forEach { rowSlots ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowSlots.forEach { slot ->
                            val isSelected = selectedSlot == slot
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedSlot = slot }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(slot, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (rowSlots.size < 3) {
                            repeat(3 - rowSlots.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Indoor Map Location Guidance (mock static banner)
            val roomNum = when (doctor.name) {
                "Dr. John Doe" -> "Room 4B (Cardiology)"
                "Dr. Helen Cho" -> "Room 102 (Neurology)"
                "Dr. Marcus Vance" -> "Room 205 (Orthopedics)"
                "Dr. Sarah Jenkins" -> "Room 301 (Pediatrics)"
                else -> "Room 104"
            }
            SectionHeader(title = "Clinic Room Map Locator")
            Spacer(modifier = Modifier.height(12.dp))
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = roomNum, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = "Main Building, Wing B, 2nd Floor", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mock Visual Map Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Indoor Map Routing Outline Preview", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    MediSlotSecondaryButton(
                        text = "Simulate Room Directions",
                        onClick = {
                            Toast.makeText(context, "Routing directions simulated to $roomNum", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reviews List
            SectionHeader(title = "Patient Reviews")
            Spacer(modifier = Modifier.height(12.dp))
            reviews.forEach { (reviewer, rating, reviewText) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = reviewer, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "$rating ★", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = reviewText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Book CTA
            MediSlotButton(
                text = "Book Appointment Now",
                onClick = { onBookAppointment(doctor.id) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================================
// Guided Appointment Booking Screen (Redesigned Stepper Wizard)
// ==========================================================
@Composable
fun AppointmentBookingScreen(
    doctorId: String,
    onBookingSuccess: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Dynamic pre-fill check
    val isPreselected = doctorId != "any"
    val preselectedDoctor = if (isPreselected) MockData.doctors.find { it.id == doctorId } else null

    // Wizard step state: 1 to 7
    var currentStep by remember { mutableStateOf(if (isPreselected) 4 else 1) }

    // Selected Wizard States
    var selectedHospital by remember { mutableStateOf(preselectedDoctor?.hospital ?: "") }
    var selectedDept by remember { mutableStateOf(preselectedDoctor?.department ?: "") }
    var selectedDoctor by remember { mutableStateOf(preselectedDoctor) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("") }
    var confirmProgress by remember { mutableStateOf(false) }

    val hospitals = listOf("City General Hospital", "Metro Health Medical Center", "Children's Specialized Hospital")
    val departments = listOf("Cardiology", "Neurology", "Orthopedics", "Pediatrics", "General Medicine")
    val dates = listOf("Mon, Jul 20", "Tue, Jul 21", "Wed, Jul 22", "Thu, Jul 23", "Fri, Jul 24")
    val slots = selectedDoctor?.slotTimes ?: listOf("09:00 AM", "10:30 AM", "11:00 AM", "02:00 PM", "03:30 PM", "04:00 PM")

    Scaffold(
        topBar = { MediSlotTopBar(title = "Book Appointment Guide", onBackClick = onNavigateBack) }
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

            // Step Indicator Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step $currentStep of 7",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (currentStep) {
                        1 -> "Select Hospital"
                        2 -> "Select Department"
                        3 -> "Select Specialist"
                        4 -> "Select Date"
                        5 -> "Select Slot Time"
                        6 -> "Reason for Consultation"
                        else -> "Booking Confirmation"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { currentStep / 7f },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (currentStep) {
                1 -> {
                    SectionHeader(title = "Choose Hospital Clinic")
                    Spacer(modifier = Modifier.height(12.dp))
                    hospitals.forEach { hosp ->
                        val isSel = selectedHospital == hosp
                        MediSlotCard(
                            onClick = {
                                selectedHospital = hosp
                                currentStep = 2
                            },
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(text = hosp, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                2 -> {
                    SectionHeader(title = "Choose Clinical Department")
                    Spacer(modifier = Modifier.height(12.dp))
                    departments.forEach { dept ->
                        val isSel = selectedDept == dept
                        MediSlotCard(
                            onClick = {
                                selectedDept = dept
                                currentStep = 3
                            },
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(text = dept, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                3 -> {
                    SectionHeader(title = "Select Available Doctor")
                    Spacer(modifier = Modifier.height(12.dp))
                    val availableDocs = MockData.doctors.filter { doc ->
                        doc.hospital == selectedHospital && doc.department == selectedDept
                    }
                    if (availableDocs.isEmpty()) {
                        Text(text = "No doctors matching department $selectedDept at hospital $selectedHospital.")
                        Spacer(modifier = Modifier.height(16.dp))
                        MediSlotSecondaryButton(
                            text = "Back",
                            onClick = { currentStep = 2 },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        availableDocs.forEach { doc ->
                            MediSlotCard(
                                onClick = {
                                    selectedDoctor = doc
                                    currentStep = 4
                                },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = doc.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(text = doc.experience, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    SectionHeader(title = "Choose Consultation Date")
                    Spacer(modifier = Modifier.height(12.dp))
                    if (isPreselected && preselectedDoctor != null) {
                        MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MedicalServices, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = preselectedDoctor.name, fontWeight = FontWeight.Bold)
                                    Text(text = "${preselectedDoctor.department} • ${preselectedDoctor.hospital}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    dates.forEach { date ->
                        val isSel = selectedDate == date
                        MediSlotCard(
                            onClick = {
                                selectedDate = date
                                currentStep = 5
                            },
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(text = date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                5 -> {
                    SectionHeader(title = "Select Available Slot Time")
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        slots.chunked(3).forEach { rowSlots ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                rowSlots.forEach { slot ->
                                    val isSel = selectedSlot == slot
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                selectedSlot = slot
                                                currentStep = 6
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(slot, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                if (rowSlots.size < 3) {
                                    repeat(3 - rowSlots.size) { Spacer(modifier = Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                }
                6 -> {
                    SectionHeader(title = "Consultation Reason Details")
                    Spacer(modifier = Modifier.height(12.dp))
                    MediSlotTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = "Symptoms & Consult Reason",
                        placeholder = "e.g. Heart pressure checkup, medical certificate"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("Common Reasons", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Routine Checkup", "Medication Renewal", "Acute Discomfort", "Lab Report Review")) { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { reasonText = tag }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(tag, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    MediSlotButton(
                        text = "Continue to Confirmation",
                        onClick = { currentStep = 7 },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                7 -> {
                    SectionHeader(title = "Booking Confirmation Details")
                    Spacer(modifier = Modifier.height(12.dp))

                    val finalDocName = selectedDoctor?.name ?: "Dr. John Doe"
                    val finalHospital = selectedDoctor?.hospital ?: selectedHospital
                    val roomNum = when (finalDocName) {
                        "Dr. John Doe" -> "Room 4B"
                        "Dr. Helen Cho" -> "Room 102"
                        "Dr. Marcus Vance" -> "Room 205"
                        else -> "Room 104"
                    }

                    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("APPOINTMENT ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("MS-${System.currentTimeMillis().toString().takeLast(6)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                StatusChip(status = "Pending Confirm")
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            Column {
                                Text("CONSULTING DOCTOR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(finalDocName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            
                            Column {
                                Text("CLINIC & ROOM LOCATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$finalHospital • $roomNum", style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("DATE & TIME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$selectedDate, $selectedSlot", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ESTIMATED QUEUE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Position #14", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MediSlotSecondaryButton(
                            text = "Add to Calendar",
                            onClick = {
                                Toast.makeText(context, "Added appointment to calendar", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MediSlotSecondaryButton(
                            text = "Get Room Route",
                            onClick = {
                                Toast.makeText(context, "Room navigation path loaded", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    MediSlotButton(
                        text = "Confirm & Finish Book",
                        onClick = {
                            confirmProgress = true
                        },
                        isLoading = confirmProgress,
                        modifier = Modifier.fillMaxWidth()
                    )

                    LaunchedEffect(confirmProgress) {
                        if (confirmProgress) {
                            delay(1200)
                            val newAppt = AppointmentData(
                                id = "appt_${System.currentTimeMillis()}",
                                doctorName = finalDocName,
                                department = selectedDoctor?.department ?: selectedDept,
                                hospital = finalHospital,
                                date = selectedDate,
                                time = selectedSlot,
                                status = "Upcoming",
                                queueNumber = 14
                            )
                            MockData.appointments.add(0, newAppt)
                            onBookingSuccess(newAppt.id)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentStep > 1 && currentStep < 7) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MediSlotSecondaryButton(
                        text = "Back Step",
                        onClick = {
                            if (isPreselected && currentStep == 4) {
                                onNavigateBack()
                            } else {
                                currentStep -= 1
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================================
// Queue Tracker Screen (Redesigned - Real-time Simulator)
// ==========================================================
@Composable
fun QueueWaitingScreen(
    appointmentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHospitalMap: () -> Unit
) {
    val appointment = MockData.appointments.find { it.id == appointmentId } ?: MockData.appointments[0]
    val context = LocalContext.current

    // Live wait states
    var currentQueueNumber by remember { mutableStateOf(14) }
    var estimatedTimeMinutes by remember { mutableStateOf(42) }
    var patientsAhead by remember { mutableStateOf(3) }
    var expectedTime by remember { mutableStateOf("10:48 AM") }
    var triggerAlertClose by remember { mutableStateOf(true) }

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

    LaunchedEffect(Unit) {
        while (currentQueueNumber > 3) {
            delay(5000)
            currentQueueNumber -= 1
            patientsAhead = (currentQueueNumber - 11).coerceAtLeast(0)
            estimatedTimeMinutes = currentQueueNumber * 3
            if (patientsAhead == 1) {
                expectedTime = "10:36 AM"
            }
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Live Connection Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

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

            val roomNum = when (appointment.doctorName) {
                "Dr. John Doe" -> "Room 4B (Cardiology)"
                "Dr. Helen Cho" -> "Room 102 (Neurology)"
                "Dr. Marcus Vance" -> "Room 205 (Orthopedics)"
                else -> "Room 104"
            }

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

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Est. Wait Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "~ $estimatedTimeMinutes mins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Patients Ahead", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$patientsAhead patients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Expected Consultation Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = expectedTime, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Location Room", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = roomNum.take(8), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Queue Proximity Alert", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Notify me when 3 patients ahead", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(checked = triggerAlertClose, onCheckedChange = { triggerAlertClose = it })
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Clinic Floor Directions", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To reach $roomNum, take Lobby Elevator B to Floor 2, turn left, and walk past labs. Follow signs for clinical wing C.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            MediSlotButton(
                text = "Open Hospital Route Map",
                onClick = onNavigateToHospitalMap,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ==========================================================
// Hospital Navigation & Google Maps Screen (Requirement 6 - Polish UI)
// ==========================================================
@Composable
fun HospitalNavigationScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var selectedHospital by remember { mutableStateOf("City General Hospital") }
    var travelMode by remember { mutableStateOf("Driving") } // "Driving", "Walking"

    val hospitalAddress = when (selectedHospital) {
        "City General Hospital" -> "100 Medical Plaza, Metro City"
        else -> "250 Healthcare Blvd, Metro City"
    }
    
    val distance = when (travelMode) {
        "Driving" -> "3.2 miles"
        else -> "2.8 miles"
    }

    val travelTime = when (travelMode) {
        "Driving" -> "12 mins"
        else -> "45 mins"
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Hospital Navigation", onBackClick = onNavigateBack) }
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

            // Selector Hospital
            Text("Select Hospital Site", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("City General Hospital", "Metro Health Medical Center").forEach { hosp ->
                    val isSel = selectedHospital == hosp
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Gray, RoundedCornerShape(8.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { selectedHospital = hosp }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(hosp, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isSel) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            }

            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceVariantColor)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = surfaceVariantColor,
                        size = size
                    )
                }
                
                // Draw mockup map route lines
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Google Maps API Navigation Route",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Map view placeholder for active routes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Travel Mode Selector
            Text("Travel Mode Selection", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Driving", "Walking").forEach { mode ->
                    val isSel = travelMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { travelMode = mode }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(mode, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Travel details Card
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = selectedHospital, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = hospitalAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DISTANCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(distance, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ESTIMATED TRAVEL TIME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(travelTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }
            }

            // Action Button ready for maps integration
            MediSlotButton(
                text = "Navigate to Hospital",
                onClick = {
                    // TODO: Future Google Maps API SDK integration
                    Toast.makeText(context, "Simulating Google Maps Navigation intent routing...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================================
// Appointment History Screen (Expanded History & Status buttons)
// ==========================================================
@Composable
fun AppointmentHistoryScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Completed", "Cancelled")
    
    // Refresh states
    var isShimmerLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()

    // Dialog state variables
    var activePrescriptionToView by remember { mutableStateOf<String?>(null) }
    var appointmentList by remember { mutableStateOf(MockData.appointments.toList()) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            isShimmerLoading = true
            delay(800)
            isShimmerLoading = false
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Consultation History", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { pullDistance = 0f },
                        onDragEnd = {
                            if (pullDistance > 180f) {
                                isRefreshing = true
                            }
                            pullDistance = 0f
                        },
                        onDragCancel = { pullDistance = 0f },
                        onDrag = { change, dragAmount ->
                            pullDistance += dragAmount.y
                            change.consume()
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val filteredAppts = appointmentList.filter { appt ->
                    appt.status.lowercase() == tabs[selectedTab].lowercase()
                }

                if (isShimmerLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(140.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(140.dp))
                    }
                } else if (filteredAppts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStateView(
                            icon = Icons.Default.CalendarMonth,
                            title = "No consultations found",
                            description = "There are no records matching category ${tabs[selectedTab]}."
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredAppts) { appt ->
                            val roomNumber = when (appt.doctorName) {
                                "Dr. John Doe" -> "Room 4B"
                                "Dr. Helen Cho" -> "Room 102"
                                "Dr. Marcus Vance" -> "Room 205"
                                else -> "Room 104"
                            }
                            
                            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = appt.doctorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        StatusChip(status = appt.status)
                                    }
                                    
                                    Text(
                                        text = "${appt.department} • ${appt.hospital} • $roomNumber",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "${appt.date} at ${appt.time}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        if (appt.status == "Upcoming") {
                                            Text(text = "Position: #${appt.queueNumber}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Action buttons for history list card details (Requirement 5)
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                    
                                    when (appt.status) {
                                        "Upcoming" -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                MediSlotSecondaryButton(
                                                    text = "Cancel",
                                                    onClick = {
                                                        val index = MockData.appointments.indexOfFirst { it.id == appt.id }
                                                        if (index != -1) {
                                                            MockData.appointments[index] = MockData.appointments[index].copy(status = "Cancelled")
                                                        }
                                                        appointmentList = MockData.appointments.toList()
                                                        Toast.makeText(context, "Consultation cancelled successfully", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                MediSlotButton(
                                                    text = "Reschedule",
                                                    onClick = {
                                                        Toast.makeText(context, "Reschedule guidelines loaded", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                        "Completed" -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                MediSlotSecondaryButton(
                                                    text = "Prescription",
                                                    onClick = {
                                                        activePrescriptionToView = appt.doctorName
                                                    },
                                                    modifier = Modifier.weight(1.2f)
                                                )
                                                
                                                OutlinedButton(
                                                    onClick = {
                                                        Toast.makeText(context, "Downloading laboratory consultation report...", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f).height(36.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                                ) {
                                                    Icon(Icons.Default.Upload, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Report", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                        "Cancelled" -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Cancelled Badge", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
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

            // Pull to refresh overlay indicator
            if (isRefreshing || pullDistance > 60f) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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

    // Prescription View dialog popup
    if (activePrescriptionToView != null) {
        AlertDialog(
            onDismissRequest = { activePrescriptionToView = null },
            title = { Text("Prescription receipt") },
            text = {
                Column {
                    Text("Prescribed by: ${activePrescriptionToView}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Lisinopril 10mg (Once daily, night)")
                    Text("2. Multivitamin Active (Once daily, morning)")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Notes: Check daily vitals values and report back in 2 weeks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { activePrescriptionToView = null }) {
                    Text("Close")
                }
            }
        )
    }
}

// ==========================================================
// Medical Records / Reports Screen (Download buttons upgrade)
// ==========================================================
@Composable
fun MedicalRecordsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    
    // Shimmer/Refresh states
    var isShimmerLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            isShimmerLoading = true
            delay(800)
            isShimmerLoading = false
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Health Records Desk", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { pullDistance = 0f },
                        onDragEnd = {
                            if (pullDistance > 180f) {
                                isRefreshing = true
                            }
                            pullDistance = 0f
                        },
                        onDragCancel = { pullDistance = 0f },
                        onDrag = { change, dragAmount ->
                            pullDistance += dragAmount.y
                            change.consume()
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Lab Test Reports", "Active Prescriptions").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isShimmerLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(100.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(100.dp))
                    }
                } else if (selectedTab == 0) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(MockData.patientProfile.labReports) { report ->
                            MediSlotCard(modifier = Modifier.fillMaxWidth().clickScale { }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = report.testName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(text = "Date: ${report.date} | Result: ${report.result}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        StatusChip(status = report.status)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Redesigned Outlined Download Button (Requirement 7)
                                        OutlinedButton(
                                            onClick = {
                                                Toast.makeText(context, "Downloading PDF for ${report.testName}...", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.height(34.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Upload,
                                                contentDescription = "Download",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Download",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(MockData.patientProfile.medications) { med ->
                            MediSlotCard(modifier = Modifier.fillMaxWidth().clickScale { }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.secondary)
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(text = med, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(text = "Status: Ongoing • Prescribed by Specialist", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    
                                    // Download Outlined Button
                                    OutlinedButton(
                                        onClick = {
                                            Toast.makeText(context, "Downloading prescription receipt PDF...", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.height(34.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Upload, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PDF", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pull to refresh overlay indicator
            if (isRefreshing || pullDistance > 60f) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
}

// ==========================================================
// Emergency SOS Screen
// ==========================================================
@Composable
fun EmergencyScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
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
                    color = Color(0xFFEF4444),
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
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(pulseRadius.dp)) {
                        drawCircle(color = Color(0xFFEF4444).copy(alpha = 0.15f))
                    }
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(colors = listOf(Color(0xFFEF4444), Color(0xFF991B1B)))
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
                            onClick = {
                                Toast.makeText(context, "Initiating call to emergency coordinator...", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Call, "Call ER", tint = Color(0xFFEF4444))
                        }
                    }
                }
            } else {
                Text(
                    text = "EMERGENCY ALERTS ACTIVE",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                        .border(4.dp, Color(0xFFEF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (countdownSeconds > 0) "$countdownSeconds" else "DISPATCHED",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (countdownSeconds > 0) "Seconds to cancel" else "Ambulance on way",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Sharing Live Location with ER Desk", fontWeight = FontWeight.Bold)
                        Text(text = "Status: Ambulance route #3B dispatched. ETA: 8 minutes.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                MediSlotButton(
                    text = "Cancel SOS Request",
                    onClick = {
                        sosActive = false
                        countdownSeconds = 5
                        Toast.makeText(context, "Emergency SOS alerts cancelled.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    gradient = SOSGradient
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================================
// Patient Profile Screen (Redesigned - Tabs, Insurance card additions)
// ==========================================================
@Composable
fun PatientProfileScreen(
    role: UserRole = UserRole.PATIENT,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    
    // Shimmer/Refresh states
    var isShimmerLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            isShimmerLoading = true
            delay(800)
            isShimmerLoading = false
            isRefreshing = false
        }
    }

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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                if (isShimmerLoading) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(120.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(180.dp))
                    }
                } else {
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

                    if (role == UserRole.PATIENT) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Medical ID", "Insurance", "Emergency").forEachIndexed { idx, title ->
                                Tab(
                                    selected = selectedTab == idx,
                                    onClick = { selectedTab = idx },
                                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                )
                            }
                        }

                        when (selectedTab) {
                            0 -> {
                                // Medical ID Card widget (Replacement of QR - Requirement 4)
                                SectionHeader(title = "Health Medical ID")
                                MediSlotCard(modifier = Modifier.fillMaxWidth().clickScale { }) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("BLOOD GROUP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                Text(MockData.patientProfile.bloodGroup, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("BMI INDEX", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                Text(MockData.patientProfile.bmi, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))
                                        
                                        Column {
                                            Text("CHRONIC CONDITIONS & HISTORY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                            Text(MockData.patientProfile.history.joinToString("\n"), style = MaterialTheme.typography.bodyMedium)
                                        }
                                        
                                        HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))
                                        
                                        Column {
                                            Text("KNOWN ALLERGIES", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                            Text(MockData.patientProfile.allergies.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
                                        }
                                        
                                        HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("HEIGHT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(MockData.patientProfile.height, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("WEIGHT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(MockData.patientProfile.weight, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                // Redesigned Metrolife Insurance card (Requirement 3)
                                SectionHeader(title = "Health Insurance Card")
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.linearGradient(colors = listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)))
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.HealthAndSafety,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text("METROLIFE INSURE", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                    Text("Premium Health Plan", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            
                                            Column(horizontalAlignment = Alignment.End) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White.copy(alpha = 0.2f))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text("VIP GOLD", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("ACTIVE", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Column {
                                                Text(MockData.patientProfile.name.uppercase(), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Worldwide Premium Care", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Policy: #POL-8902123", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                                                Text("Member Since: 2020", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                                                Text("Valid: Exp 12/2028", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // Emergency contact details
                                SectionHeader(title = "Primary Emergency Contacts")
                                MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Phone, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(text = "Richard Connor (Spouse)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                    Text(text = "+1 (555) 902-1823", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            IconButton(onClick = { Toast.makeText(context, "CallingRichard...", Toast.LENGTH_SHORT).show() }) {
                                                Icon(Icons.Default.Call, null, tint = Color(0xFFEF4444))
                                            }
                                        }
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
                        }
                    }

                    // Profile Actions
                    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    Toast.makeText(context, "Edit Profile Dialog coming soon", Toast.LENGTH_SHORT).show()
                                },
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

            // Pull to refresh overlay indicator
            if (isRefreshing || pullDistance > 60f) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), CircleShape)
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
// Settings Screen (Expanded with requirement 8 details)
// ==========================================================
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    var checkDarkMode by remember { mutableStateOf(true) }
    var dataSharing by remember { mutableStateOf(false) }
    var pushNotifs by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    var showDialogType by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { MediSlotTopBar(title = "App Settings", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "General Settings")
            
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Dark Mode", style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(checked = checkDarkMode, onCheckedChange = { checkDarkMode = it })
                    }

                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))

                    // Language Selector
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showDialogType = "Language" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Language (English)", style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                    }

                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))

                    // Emergency Contacts List
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showDialogType = "Emergency" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Emergency, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Emergency Contacts", style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            SectionHeader(title = "Privacy & Security")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Data Sharing Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PrivacyTip, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Data Sharing With Hospitals", style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(checked = dataSharing, onCheckedChange = { dataSharing = it })
                    }

                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))

                    // Notification Preferences
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Queue Push Notifications", style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(checked = pushNotifs, onCheckedChange = { pushNotifs = it })
                    }
                }
            }

            SectionHeader(title = "Legal & About")
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showDialogType = "About" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("About MediSlot", style = MaterialTheme.typography.bodyMedium)
                        Text("v1.4.2", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showDialogType = "Terms" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Terms & Conditions", style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                    }

                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showDialogType = "PrivacyPolicy" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Privacy Policy Documents", style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal dialogs mapping for Settings Choices
    if (showDialogType != null) {
        AlertDialog(
            onDismissRequest = { showDialogType = null },
            title = { Text(showDialogType!!) },
            text = {
                Column {
                    when (showDialogType) {
                        "Language" -> {
                            Text("Choose Application Language:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• English (Active)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("• Spanish (Español)")
                            Text("• French (Français)")
                        }
                        "Emergency" -> {
                            Text("Emergency Medical Contacts List:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("1. Richard Connor (Spouse): +1 (555) 902-1823")
                            Text("2. Primary General ER Clinic: +1 (555) 911-3044")
                        }
                        "About" -> {
                            Text("MediSlot Health v1.4.2-Production Release.\nA national-level medical slot queue tracking assistant. Engineered for zero-waiting hospital deployments.")
                        }
                        "Terms" -> {
                            Text("By deploying MediSlot, you agree to secure medical queue status sharing policies under clinical alignment regulations. Users must comply with room and check-in timelines.")
                        }
                        "PrivacyPolicy" -> {
                            Text("MediSlot encrypts patient medical histories, vital stats logs, and consultation reasons locally. Health data sharing is strictly opt-in by the patient profile controller.")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialogType = null }) {
                    Text("OK")
                }
            }
        )
    }
}

// ==========================================================
// Notifications Screen
// ==========================================================
@Composable
fun NotificationsScreen(onNavigateBack: () -> Unit) {
    val unreadNotifs = MockData.notifications.filter { !it.isRead }
    val readNotifs = MockData.notifications.filter { it.isRead }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Notification Panel", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (MockData.notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Default.Notifications,
                        title = "No Notifications",
                        description = "You're completely caught up with health alerts."
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (unreadNotifs.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Unread Alerts")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(unreadNotifs) { notif ->
                            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .align(Alignment.CenterVertically)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = notif.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(text = notif.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = notif.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    if (readNotifs.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            SectionHeader(title = "Archived Notices")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(readNotifs) { notif ->
                            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = notif.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = notif.message, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(text = notif.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

