package com.medislot.app.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.medislot.app.data.model.VerificationStatus
import com.medislot.app.data.model.VerificationStateStore
import com.medislot.app.ui.components.MediSlotCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Live reactive list from VerificationStateStore
    val hospitalApps = VerificationStateStore.hospitalApplications

    var selectedAppForRejection by remember { mutableStateOf<HospitalApplication?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var showDocDialog by remember { mutableStateOf<HospitalApplication?>(null) }

    // Rejection Dialog
    if (selectedAppForRejection != null) {
        AlertDialog(
            onDismissRequest = { selectedAppForRejection = null; rejectionReasonInput = "" },
            title = { Text("Reject Hospital Application", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Provide a rejection reason for ${selectedAppForRejection?.name}:")
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
                        val app = selectedAppForRejection!!
                        if (rejectionReasonInput.isNotBlank()) {
                            // TODO: Connect with backend platform admin API
                            // Example backend operation:
                            // database.collection("hospitals").document(app.id).update("status", "REJECTED", "rejectionReason", reason)
                            
                            VerificationStateStore.rejectHospital(app.id, rejectionReasonInput)
                            Toast.makeText(context, "Hospital application rejected", Toast.LENGTH_SHORT).show()
                            selectedAppForRejection = null
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
                TextButton(onClick = { selectedAppForRejection = null; rejectionReasonInput = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    // View Documents Dialog
    if (showDocDialog != null) {
        AlertDialog(
            onDismissRequest = { showDocDialog = null },
            title = { Text("Verification Documents", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Uploaded attachments for ${showDocDialog?.name}:", fontWeight = FontWeight.Medium)
                    
                    DocAttachmentRow("Hospital_Registration_Certificate.pdf")
                    DocAttachmentRow("State_Medical_License.pdf")
                    DocAttachmentRow("NABH_Accreditation_Proof.pdf")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDocDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hospital Verifications Desk", fontWeight = FontWeight.Bold) },
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
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header Info Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("MediSlot Super Admin Dashboard", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Hospitals are locked in verification status and require manual approval before credentials unlock dashboard access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (hospitalApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hospital registration requests pending.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            onViewDocs = { showDocDialog = app },
                            onApprove = {
                                // TODO: Connect with backend platform admin API
                                // Example backend operations:
                                // database.collection("hospitals").document(app.id).update("status", "APPROVED")
                                // notifyUserViaEmail(app.name, "APPROVED")
                                
                                VerificationStateStore.approveHospital(app.id)
                                Toast.makeText(context, "✅ Hospital status approved successfully!", Toast.LENGTH_SHORT).show()
                            },
                            onReject = { selectedAppForRejection = app }
                        )
                    }
                }
            }
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
                    Text("Submitted Date: ${app.submittedDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                // M3 Chips
                val chipColor = when (app.status) {
                    VerificationStatus.PENDING -> Color(0xFFF59E0B)
                    VerificationStatus.APPROVED -> Color(0xFF10B981)
                    VerificationStatus.REJECTED -> Color(0xFFEF4444)
                    VerificationStatus.WAITING_FOR_DOCUMENTS -> Color(0xFF3B82F6)
                }
                val chipLabel = when (app.status) {
                    VerificationStatus.PENDING -> "Pending Verification"
                    VerificationStatus.APPROVED -> "Hospital Verified"
                    VerificationStatus.REJECTED -> "Verification Rejected"
                    VerificationStatus.WAITING_FOR_DOCUMENTS -> "Waiting for Documents"
                }

                Box(
                    modifier = Modifier
                        .background(chipColor.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(chipLabel, color = chipColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))

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
        Text("Verified (Mock)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
    }
}
