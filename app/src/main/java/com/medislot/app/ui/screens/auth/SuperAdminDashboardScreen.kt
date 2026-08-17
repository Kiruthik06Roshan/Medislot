package com.medislot.app.ui.screens.auth

import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.data.model.HospitalApplication
import com.medislot.app.data.model.DoctorApplication
import com.medislot.app.data.model.VerificationStatus
import com.medislot.app.data.model.VerificationStateStore
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.viewmodel.SuperAdminViewModel
import androidx.compose.runtime.collectAsState

// Global audit log list to persist logs during session
private val sessionAuditLogs = mutableStateListOf<String>(
    "System initialized and database seeded successfully.",
    "FastAPI backend connected on Port 8000.",
    "Room Local SQLite Cache synced."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: SuperAdminViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloadingPdf by remember { mutableStateOf(false) }
    
    val isDemoMode = DemoConfig.isDemoModeActive
    val realHospitalApps by viewModel.hospitals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val hospitalApps = if (isDemoMode) VerificationStateStore.hospitalApplications else realHospitalApps

    LaunchedEffect(isDemoMode) {
        if (!isDemoMode) {
            viewModel.loadHospitals()
        }
    }

    var selectedHospitalForRejection by remember { mutableStateOf<HospitalApplication?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    
    var showHospitalDocDialog by remember { mutableStateOf<HospitalApplication?>(null) }

    // Rejection Dialog for Hospital
    if (selectedHospitalForRejection != null) {
        AlertDialog(
            onDismissRequest = { selectedHospitalForRejection = null; rejectionReasonInput = "" },
            title = { Text("Reject Hospital Application", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Provide a rejection reason for ${selectedHospitalForRejection?.name}:")
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        placeholder = { Text("E.g., Invalid document scanned copy...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val app = selectedHospitalForRejection!!
                        if (rejectionReasonInput.isNotBlank()) {
                            if (isDemoMode) {
                                VerificationStateStore.rejectHospital(app.id, rejectionReasonInput)
                                sessionAuditLogs.add(0, "Rejected hospital application for: ${app.name} (Reason: $rejectionReasonInput)")
                                Toast.makeText(context, "Hospital application rejected", Toast.LENGTH_SHORT).show()
                                selectedHospitalForRejection = null
                                rejectionReasonInput = ""
                            } else {
                                viewModel.rejectHospital(
                                    hospId = app.id,
                                    reason = rejectionReasonInput,
                                    onSuccess = {
                                        sessionAuditLogs.add(0, "Rejected hospital application for: ${app.name} (Reason: $rejectionReasonInput)")
                                        Toast.makeText(context, "Hospital application rejected", Toast.LENGTH_SHORT).show()
                                        selectedHospitalForRejection = null
                                        rejectionReasonInput = ""
                                    },
                                    onFailure = { error ->
                                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        } else {
                            Toast.makeText(context, "Reason cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Submit Rejection", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedHospitalForRejection = null; rejectionReasonInput = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    // View Documents Dialog (Hospital)
    if (showHospitalDocDialog != null) {
        AlertDialog(
            onDismissRequest = { showHospitalDocDialog = null },
            title = { Text("Verification Documents", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Uploaded attachments for ${showHospitalDocDialog?.name}:", fontWeight = FontWeight.Medium)
                    val docsStr = showHospitalDocDialog?.docsAttached ?: ""
                    val docsList = docsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    if (docsList.isEmpty()) {
                        Text(
                            text = "No verification documents uploaded.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        docsList.forEach { doc ->
                            DocAttachmentRow(
                                filename = doc,
                                onViewPdf = { filename ->
                                    if (!isDownloadingPdf) {
                                        isDownloadingPdf = true
                                        coroutineScope.launch {
                                            try {
                                                val responseBody = com.medislot.app.network.RetrofitClient.apiService.downloadDocument(filename)
                                                val bytes = responseBody.bytes()
                                                val cleanName = filename.substringAfter("_").replace("[^a-zA-Z0-9.]".toRegex(), "_")
                                                val cacheFile = java.io.File(context.cacheDir, cleanName)
                                                val fos = java.io.FileOutputStream(cacheFile)
                                                fos.write(bytes)
                                                fos.close()
                                                com.medislot.app.ui.screens.patient.openDownloadedPdf(context, cacheFile)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error downloading PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isDownloadingPdf = false
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHospitalDocDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Super Admin Console", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            Spacer(modifier = Modifier.height(10.dp))

            // 1. STATS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val pendingCount = hospitalApps.count { it.status == VerificationStatus.PENDING }
                val approvedCount = hospitalApps.count { it.status == VerificationStatus.APPROVED }

                AdminStatCard(
                    title = "Pending Apps",
                    value = pendingCount.toString(),
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Verified Accounts",
                    value = approvedCount.toString(),
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "System Health",
                    value = "Healthy",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. CONTENT
            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null && !isDemoMode) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { viewModel.loadHospitals() }) {
                            Text("Retry")
                        }
                    }
                } else {
                    if (hospitalApps.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pending verification requests", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(hospitalApps) { app ->
                                HospitalRequestCard(
                                    app = app,
                                    onViewDocs = { showHospitalDocDialog = app },
                                    onApprove = {
                                        if (isDemoMode) {
                                            VerificationStateStore.approveHospital(app.id)
                                            sessionAuditLogs.add(0, "Approved hospital coordinator credentials for: ${app.name}")
                                            Toast.makeText(context, "✅ Hospital status approved successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.approveHospital(
                                                hospId = app.id,
                                                onSuccess = {
                                                    sessionAuditLogs.add(0, "Approved hospital coordinator credentials for: ${app.name}")
                                                    Toast.makeText(context, "✅ Hospital status approved successfully!", Toast.LENGTH_SHORT).show()
                                                },
                                                onFailure = { error ->
                                                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    },
                                    onReject = { selectedHospitalForRejection = app }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.2f)))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun HospitalRequestCard(
    app: HospitalApplication,
    onViewDocs: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(100.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Admin: ${app.adminName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                // Verification chip status
                val (chipColor, chipLabel) = when (app.status) {
                    VerificationStatus.PENDING -> Color(0xFFF59E0B) to "Pending"
                    VerificationStatus.APPROVED -> Color(0xFF10B981) to "Verified"
                    VerificationStatus.REJECTED -> Color(0xFFEF4444) to "Rejected"
                    VerificationStatus.WAITING_FOR_DOCUMENTS -> Color(0xFF3B82F6) to "Waiting Docs"
                }

                Box(
                    modifier = Modifier
                        .background(chipColor.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(chipLabel, color = chipColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Reg. Number: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(app.regNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("License Number: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(app.licenseNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (app.adminName.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Coordinator: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(app.adminName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (app.contact.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Contact: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(app.contact, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                if (app.status == VerificationStatus.REJECTED && app.rejectionReason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rejection Reason: ${app.rejectionReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewDocs,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Documents", style = MaterialTheme.typography.labelMedium)
                }

                if (app.status == VerificationStatus.PENDING) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onReject,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                        
                        IconButton(
                            onClick = onApprove,
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(100.dp))
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocAttachmentRow(
    filename: String,
    onViewPdf: (String) -> Unit
) {
    val hasUuidPrefix = filename.contains("_") && filename.substringBefore("_").length >= 32
    val cleanName = if (hasUuidPrefix) filename.substringAfter("_") else filename

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = cleanName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            if (!hasUuidPrefix) {
                Text(
                    text = "Document file unavailable",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        if (hasUuidPrefix) {
            TextButton(
                onClick = { onViewPdf(filename) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "View PDF",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
