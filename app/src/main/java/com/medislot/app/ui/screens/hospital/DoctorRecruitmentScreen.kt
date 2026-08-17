package com.medislot.app.ui.screens.hospital

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.medislot.app.data.model.DoctorApplication
import com.medislot.app.data.model.VerificationStatus
import com.medislot.app.ui.components.*
import com.medislot.app.viewmodel.AiState
import com.medislot.app.viewmodel.DoctorRecruitmentViewModel
import com.medislot.app.viewmodel.SortOption
import com.medislot.app.ui.screens.auth.DemoConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorRecruitmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: DoctorRecruitmentViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val applications by viewModel.filteredApplications.collectAsState()
    val stats by viewModel.recruitmentStats.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDept by viewModel.selectedDept.collectAsState()
    val selectedExperience by viewModel.selectedExperience.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var activeTab by remember { mutableStateOf("Pending") } // Pending, Approved, Rejected, All
    var selectedAppForDocumentViewer by remember { mutableStateOf<DoctorApplication?>(null) }
    var selectedAppForRejection by remember { mutableStateOf<DoctorApplication?>(null) }

    LaunchedEffect(activeTab) {
        val filterStatus = when (activeTab) {
            "Pending" -> "Pending"
            "Approved" -> "Approved"
            "Rejected" -> "Rejected"
            else -> "All"
        }
        viewModel.updateStatusFilter(filterStatus)
    }

    LaunchedEffect(uiState) {
        if (uiState is AiState.Success) {
            Toast.makeText(context, "🟢 Operation completed successfully!", Toast.LENGTH_SHORT).show()
        } else if (uiState is AiState.Failure) {
            Toast.makeText(context, "❌ Error: ${(uiState as AiState.Failure).error}", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Doctor Applications",
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
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Recruitment Statistics Rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnalyticsMiniCard("Pending Applications", "${stats.pendingCount}", Icons.Default.HourglassEmpty, Modifier.weight(1f))
                AnalyticsMiniCard("Approved Doctors", "${stats.approvedCount}", Icons.Default.CheckCircle, Modifier.weight(1f))
                AnalyticsMiniCard("Rejected Applications", "${stats.rejectedCount}", Icons.Default.Error, Modifier.weight(1f))
                AnalyticsMiniCard("Today's Submissions", "${stats.todayApplicationsCount}", Icons.Default.CalendarToday, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            MediSlotSearchBar(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = "Search by doctor name or registration number..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                listOf("Pending", "Approved", "Rejected", "All").forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { activeTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sort & Filters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Department filter drop down
                var showDeptDropdown by remember { mutableStateOf(false) }
                val depts = listOf("All", "Cardiology", "Neurology", "Orthopedics", "Pediatrics")
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showDeptDropdown = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Dept: $selectedDept", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(expanded = showDeptDropdown, onDismissRequest = { showDeptDropdown = false }) {
                        depts.forEach { d ->
                            DropdownMenuItem(text = { Text(d) }, onClick = {
                                viewModel.updateDeptFilter(d)
                                showDeptDropdown = false
                            })
                        }
                    }
                }

                // Experience filter
                var showExpDropdown by remember { mutableStateOf(false) }
                val exps = listOf("All", "< 5 Years", "5 - 10 Years", "> 10 Years")
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showExpDropdown = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Exp: $selectedExperience", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(expanded = showExpDropdown, onDismissRequest = { showExpDropdown = false }) {
                        exps.forEach { e ->
                            DropdownMenuItem(text = { Text(e) }, onClick = {
                                viewModel.updateExperienceFilter(e)
                                showExpDropdown = false
                            })
                        }
                    }
                }

                // Sorting filter
                var showSortDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showSortDropdown = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        val sortLabel = when (sortBy) {
                            SortOption.NEWEST -> "Newest"
                            SortOption.OLDEST -> "Oldest"
                            SortOption.EXPERIENCE -> "Experience"
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Sort: $sortLabel", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(expanded = showSortDropdown, onDismissRequest = { showSortDropdown = false }) {
                        SortOption.values().forEach { option ->
                            DropdownMenuItem(text = { Text(option.name) }, onClick = {
                                viewModel.updateSortOption(option)
                                showSortDropdown = false
                            })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main lazy applications list
            val isDemoMode = DemoConfig.isDemoModeActive
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null && !isDemoMode) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { viewModel.loadApplications() }) {
                        Text("Retry")
                    }
                }
            } else if (applications.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonSearch, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No applicants matched your query.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(applications) { app ->
                        DoctorApplicantCard(
                            application = app,
                            onViewDocuments = { selectedAppForDocumentViewer = app },
                            onApprove = { viewModel.approveDoctor(app.id) },
                            onReject = { selectedAppForRejection = app },
                            onRequestDocuments = { viewModel.requestDocuments(app.id) }
                        )
                    }
                }
            }
        }
    }

    // Document Viewer modal dialog
    if (selectedAppForDocumentViewer != null) {
        DocumentViewerDialog(
            application = selectedAppForDocumentViewer!!,
            onDismiss = { selectedAppForDocumentViewer = null }
        )
    }

    // Rejection Reason dialog
    if (selectedAppForRejection != null) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { selectedAppForRejection = null },
            title = { Text("Reject Application", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Please input the reason for rejecting Dr. ${selectedAppForRejection!!.name}'s registration request:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text("Rejection reason details...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.isNotBlank()) {
                            viewModel.rejectDoctor(selectedAppForRejection!!.id, reason)
                            selectedAppForRejection = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAppForRejection = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DoctorApplicantCard(
    application: DoctorApplication,
    onViewDocuments: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRequestDocuments: () -> Unit
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row (Name + Experience)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = application.name.firstOrNull { it != 'D' && it != 'r' && it != '.' }?.toString() ?: "D",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = application.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = application.specialization,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Status chip
                val statusColor = when (application.status) {
                    VerificationStatus.PENDING -> Color(0xFFF59E0B) // Amber
                    VerificationStatus.APPROVED -> Color(0xFF10B981) // Green
                    VerificationStatus.REJECTED -> Color(0xFFEF4444) // Red
                    VerificationStatus.WAITING_FOR_DOCUMENTS -> Color(0xFF3B82F6) // Blue
                }
                val statusText = when (application.status) {
                    VerificationStatus.PENDING -> "Pending Review"
                    VerificationStatus.APPROVED -> "Approved"
                    VerificationStatus.REJECTED -> "Rejected"
                    VerificationStatus.WAITING_FOR_DOCUMENTS -> "Waiting for Docs"
                }

                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Body Details
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Medical Council Registration:", application.medicalRegistrationNumber)
                DetailRow("MBBS Institution:", application.mbbsInstitution)
                DetailRow("Experience Years:", "${application.experienceYears} Years")
                DetailRow("Documents Attached:", application.docsAttached)
                DetailRow("Application Date:", application.submittedDate)

                if (application.status == VerificationStatus.REJECTED && application.rejectionReason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("Rejection Reason: ${application.rejectionReason}", color = Color(0xFFB91C1C), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Document viewer action
                Button(
                    onClick = onViewDocuments,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RemoveRedEye, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Files", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }

                if (application.status == VerificationStatus.PENDING || application.status == VerificationStatus.WAITING_FOR_DOCUMENTS) {
                    // Request docs
                    Button(
                        onClick = onRequestDocuments,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("Request Docs", color = Color(0xFF2563EB), fontSize = 11.sp)
                    }

                    // Reject
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
                        modifier = Modifier.weight(0.8f),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("Reject", color = Color(0xFFEF4444), fontSize = 11.sp)
                    }

                    // Approve
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("Approve", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
    }
}

@Composable
fun DocumentViewerDialog(
    application: DoctorApplication,
    onDismiss: () -> Unit
) {
    val docsList = application.docsAttached.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val documents = remember(application) {
        val list = mutableListOf<Pair<String, String>>()
        docsList.forEach { doc ->
            val displayName = when {
                doc.contains("MBBS", ignoreCase = true) -> "MBBS Degree Certificate"
                doc.contains("MD", ignoreCase = true) -> "MD/MS Degree Certificate"
                doc.contains("Council", ignoreCase = true) -> "Medical Council Registration"
                doc.contains("ID", ignoreCase = true) || doc.contains("Govt", ignoreCase = true) -> "Government ID Proof"
                else -> "Verification Certificate"
            }
            list.add(Pair(displayName, doc))
        }
        if (application.resumeFile.isNotBlank() && application.resumeFile != "resume.pdf") {
            list.add(Pair("Resume/Curriculum Vitae", application.resumeFile))
        }
        list
    }

    var activeDocIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Verification Document Hub", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Reviewing files for ${application.name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (documents.isEmpty()) {
                    Text(
                        text = "No verification documents uploaded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Horizontal Tabs to pick doc
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(documents.size) { index ->
                            val isSelected = activeDocIndex == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { activeDocIndex = index }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = documents[index].first.split(" ")[0],
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Document Frame View
                    val activeDoc = documents.getOrNull(activeDocIndex) ?: Pair("Document", "")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (activeDoc.first.contains("PDF")) Icons.Default.Description else Icons.Default.Image,
                                    contentDescription = null,
                                    tint = if (activeDoc.first.contains("PDF")) Color(0xFFEF4444) else Color(0xFF3B82F6),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(activeDoc.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                Text("Filename: ${activeDoc.second}", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("🔒 VERIFIED ENCRYPTED MOCK DOCUMENT", color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done Reviewing")
            }
        }
    )
}
