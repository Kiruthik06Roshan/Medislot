package com.medislot.app.ui.screens.auth

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

// Global audit log list to persist logs during session
private val sessionAuditLogs = mutableStateListOf<String>(
    "System initialized and database seeded successfully.",
    "FastAPI backend connected on Port 8000.",
    "Room Local SQLite Cache synced."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) }
    
    // Live reactive lists from VerificationStateStore
    val hospitalApps = VerificationStateStore.hospitalApplications
    val doctorApps = VerificationStateStore.doctorApplications

    var selectedHospitalForRejection by remember { mutableStateOf<HospitalApplication?>(null) }
    var selectedDoctorForRejection by remember { mutableStateOf<DoctorApplication?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    
    var showHospitalDocDialog by remember { mutableStateOf<HospitalApplication?>(null) }
    var showDoctorDocDialog by remember { mutableStateOf<DoctorApplication?>(null) }

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
                            VerificationStateStore.rejectHospital(app.id, rejectionReasonInput)
                            sessionAuditLogs.add(0, "Rejected hospital application for: ${app.name} (Reason: $rejectionReasonInput)")
                            Toast.makeText(context, "Hospital application rejected", Toast.LENGTH_SHORT).show()
                            selectedHospitalForRejection = null
                            rejectionReasonInput = ""
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

    // Rejection Dialog for Doctor
    if (selectedDoctorForRejection != null) {
        AlertDialog(
            onDismissRequest = { selectedDoctorForRejection = null; rejectionReasonInput = "" },
            title = { Text("Reject Doctor Recruitment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Provide a rejection reason for ${selectedDoctorForRejection?.name}:")
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        placeholder = { Text("E.g., Credentials could not be verified...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val app = selectedDoctorForRejection!!
                        if (rejectionReasonInput.isNotBlank()) {
                            VerificationStateStore.rejectDoctor(app.id, rejectionReasonInput)
                            sessionAuditLogs.add(0, "Rejected doctor verification for: ${app.name} (Reason: $rejectionReasonInput)")
                            Toast.makeText(context, "Doctor recruitment application rejected", Toast.LENGTH_SHORT).show()
                            selectedDoctorForRejection = null
                            rejectionReasonInput = ""
                        } else {
                            Toast.makeText(context, "Reason cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Submit Rejection", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDoctorForRejection = null; rejectionReasonInput = "" }) {
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
                    DocAttachmentRow("Hospital_Registration_Certificate.pdf")
                    DocAttachmentRow("State_Medical_License.pdf")
                    DocAttachmentRow("NABH_Accreditation_Proof.pdf")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHospitalDocDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    // View Documents Dialog (Doctor)
    if (showDoctorDocDialog != null) {
        AlertDialog(
            onDismissRequest = { showDoctorDocDialog = null },
            title = { Text("Verification Documents", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Uploaded attachments for ${showDoctorDocDialog?.name}:", fontWeight = FontWeight.Medium)
                    val docs = showDoctorDocDialog?.docsAttached?.split(",") ?: listOf("MBBS_Degree.pdf")
                    docs.forEach { doc ->
                        DocAttachmentRow(doc.trim())
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDoctorDocDialog = null }) {
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
                val pendingCount = hospitalApps.count { it.status == VerificationStatus.PENDING } +
                        doctorApps.count { it.status == VerificationStatus.PENDING }
                val approvedCount = hospitalApps.count { it.status == VerificationStatus.APPROVED } +
                        doctorApps.count { it.status == VerificationStatus.APPROVED }

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

            // 2. TAB SELECTION
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        val count = hospitalApps.count { it.status == VerificationStatus.PENDING }
                        Text(if (count > 0) "Hospitals ($count)" else "Hospitals", fontWeight = FontWeight.Bold)
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        val count = doctorApps.count { it.status == VerificationStatus.PENDING }
                        Text(if (count > 0) "Doctors ($count)" else "Doctors", fontWeight = FontWeight.Bold)
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Audit Trail", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. TAB CONTENT
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> {
                        if (hospitalApps.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hospital registration requests.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                            VerificationStateStore.approveHospital(app.id)
                                            sessionAuditLogs.add(0, "Approved hospital coordinator credentials for: ${app.name}")
                                            Toast.makeText(context, "✅ Hospital status approved successfully!", Toast.LENGTH_SHORT).show()
                                        },
                                        onReject = { selectedHospitalForRejection = app }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        if (doctorApps.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No doctor registration requests.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(doctorApps) { app ->
                                    DoctorRequestCard(
                                        app = app,
                                        onViewDocs = { showDoctorDocDialog = app },
                                        onApprove = {
                                            VerificationStateStore.approveDoctor(app.id)
                                            sessionAuditLogs.add(0, "Approved doctor application and clinical roster for: ${app.name}")
                                            Toast.makeText(context, "✅ Doctor status approved successfully!", Toast.LENGTH_SHORT).show()
                                        },
                                        onReject = { selectedDoctorForRejection = app }
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Text(
                                    text = "Session Activity Trails",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Chronological logging of approvals, rejections, and server status signals.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(sessionAuditLogs) { log ->
                                AuditTrailRow(log = log)
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
                    Text("Submitted: ${app.submittedDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun DoctorRequestCard(
    app: DoctorApplication,
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
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(100.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MedicalServices, null, tint = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Hospital: ${app.hospitalName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("Specialization: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(app.specialization, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Experience: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("${app.experienceYears} Years", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Reg. Number: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(app.medicalRegistrationNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Institution: ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(app.mbbsInstitution, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("View Credentials", style = MaterialTheme.typography.labelMedium)
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
private fun DocAttachmentRow(filename: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(filename, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.weight(1f))
        Text("Verified", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AuditTrailRow(log: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(log, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
