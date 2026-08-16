package com.medislot.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.data.model.VerificationStatus
import com.medislot.app.data.model.VerificationStateStore
import com.medislot.app.ui.components.MediSlotButton
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.components.MediSlotOutlinedButton
import com.medislot.app.data.local.DatabaseProvider
import com.medislot.app.network.RetrofitClient
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationStatusScreen(
    role: String,
    hospitalName: String,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val isDemoMode = DemoConfig.isDemoModeActive

    // Determine the name key to look up in our state store (only for demo mode)
    val displayName = remember(isDemoMode) {
        if (isDemoMode) {
            if (role == "doctor") {
                // Find a mock doctor name or default to a demo doctor name
                VerificationStateStore.doctorApplications.find { it.hospitalName == hospitalName }?.name ?: "Dr. Jane Smith"
            } else {
                if (hospitalName != "None" && hospitalName.isNotBlank()) hospitalName else "Demo Hospital"
            }
        } else {
            ""
        }
    }
    
    var realStatus by remember { mutableStateOf(VerificationStatus.PENDING) }
    var realRejectionReason by remember { mutableStateOf<String?>(null) }
    var realHospitalName by remember { mutableStateOf<String?>(null) }
    var isLoadingRealStatus by remember { mutableStateOf(false) }

    // Get live status from VerificationStateStore (only for demo mode)
    val statusMap = if (isDemoMode) VerificationStateStore.userVerificationStatus else mutableMapOf<String, VerificationStatus>()
    val rejectionReasonsMap = if (isDemoMode) VerificationStateStore.userRejectionReasons else mutableMapOf<String, String>()
    
    LaunchedEffect(displayName, isDemoMode) {
        if (isDemoMode) {
            if (statusMap[displayName] == null) {
                statusMap[displayName] = VerificationStatus.PENDING
            }
        } else {
            isLoadingRealStatus = true
            try {
                val uid = DatabaseProvider.getDataStoreManager().uidFlow.first()
                if (uid != null) {
                    val res = RetrofitClient.apiService.getUserStatus(uid)
                    realStatus = when (res.status) {
                        "Approved" -> VerificationStatus.APPROVED
                        "Rejected" -> VerificationStatus.REJECTED
                        "Waiting Documents" -> VerificationStatus.WAITING_FOR_DOCUMENTS
                        else -> VerificationStatus.PENDING
                    }
                    realRejectionReason = res.rejection_reason
                    realHospitalName = res.hospital_name
                }
            } catch (e: Exception) {
                // Keep default pending status
            } finally {
                isLoadingRealStatus = false
            }
        }
    }

    val currentStatus = if (isDemoMode) (statusMap[displayName] ?: VerificationStatus.PENDING) else realStatus
    val rejectionReason = if (isDemoMode) {
        rejectionReasonsMap[displayName] ?: "Verification documents could not be validated. Please ensure clear, readable copies of certificates are uploaded."
    } else {
        realRejectionReason ?: "Verification documents could not be validated. Please ensure clear, readable copies of certificates are uploaded."
    }

    // Theme color corresponding to roles
    val themeColor = when (role) {
        "doctor" -> MaterialTheme.colorScheme.secondary
        "hospital" -> Color(0xFFF59E0B) // Amber
        else -> MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Verification", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Login")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Large visual state card with animations
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Dynamic status icon
                        val iconRes = when (currentStatus) {
                            VerificationStatus.PENDING -> Icons.Default.HourglassEmpty
                            VerificationStatus.APPROVED -> Icons.Default.CheckCircle
                            VerificationStatus.REJECTED -> Icons.Default.Error
                            VerificationStatus.WAITING_FOR_DOCUMENTS -> Icons.Default.Description
                        }
                        
                        val iconColor = when (currentStatus) {
                            VerificationStatus.PENDING -> Color(0xFFF59E0B) // Yellow/Amber
                            VerificationStatus.APPROVED -> Color(0xFF10B981) // Green
                            VerificationStatus.REJECTED -> Color(0xFFEF4444) // Red
                            VerificationStatus.WAITING_FOR_DOCUMENTS -> Color(0xFF3B82F6) // Blue
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconRes,
                                contentDescription = "Status Icon",
                                tint = iconColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Status Chip
                        val chipText = when (currentStatus) {
                            VerificationStatus.PENDING -> if (role == "doctor") "Pending Hospital Approval" else "Pending Verification"
                            VerificationStatus.APPROVED -> if (role == "doctor") "Doctor Approved" else "Hospital Verified"
                            VerificationStatus.REJECTED -> if (role == "doctor") "Application Rejected" else "Verification Rejected"
                            VerificationStatus.WAITING_FOR_DOCUMENTS -> "Waiting for Documents"
                        }

                        Box(
                            modifier = Modifier
                                .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipText,
                                color = iconColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Success Titles and Messages
                        Text(
                            text = "Application Status Update",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val explanationText = when (currentStatus) {
                            VerificationStatus.PENDING -> {
                                if (role == "doctor") {
                                    "Your application has been forwarded to the selected hospital ($hospitalName) for verification. The Hospital Administrator will verify your qualifications. You will receive approval once your documents are verified."
                                } else {
                                    "Your hospital registration is under verification. Our MediSlot Super Admin will review your submitted documents. You will be notified once your hospital has been approved."
                                }
                            }
                            VerificationStatus.APPROVED -> {
                                if (role == "doctor") {
                                    "Congratulations! Your doctor registration has been approved by $hospitalName. Your account is now fully active."
                                } else {
                                    "Congratulations! Your hospital registration has been approved by the MediSlot Super Admin. Your coordinator dashboard is now active."
                                }
                            }
                            VerificationStatus.REJECTED -> {
                                "Unfortunately, your application was not approved. Please review the reason below and resubmit updated files."
                            }
                            VerificationStatus.WAITING_FOR_DOCUMENTS -> {
                                "The hospital coordinator has requested additional/updated certificates. Please resubmit clear document uploads."
                            }
                        }

                        Text(
                            text = explanationText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        // Rejection Reason Details Box
                        if (currentStatus == VerificationStatus.REJECTED) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF2F2), RoundedCornerShape(14.dp))
                                    .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(14.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Rejection Reason:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB91C1C)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rejectionReason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }
                    }
                }

                // Interactive progress/timeline stepper
                MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Verification Timeline", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        
                        TimelineItem(
                            stepNumber = "1",
                            title = "Account Created",
                            description = "Details and credentials filled in",
                            isCompleted = true
                        )
                        
                        TimelineItem(
                            stepNumber = "2",
                            title = "Document Review",
                            description = if (role == "doctor") "Pending verification by $hospitalName Coordinator" else "Pending document check by MediSlot Platform Admin",
                            isCompleted = currentStatus == VerificationStatus.APPROVED,
                            isActive = currentStatus == VerificationStatus.PENDING || currentStatus == VerificationStatus.WAITING_FOR_DOCUMENTS,
                            isError = currentStatus == VerificationStatus.REJECTED
                        )
                        
                        TimelineItem(
                            stepNumber = "3",
                            title = "Account Activated",
                            description = "Full access to dashboard resources enabled",
                            isCompleted = currentStatus == VerificationStatus.APPROVED,
                            isActive = false
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStatus == VerificationStatus.APPROVED) {
                        MediSlotButton(
                            text = "Go to Dashboard",
                            onClick = onNavigateToDashboard,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (currentStatus == VerificationStatus.REJECTED || currentStatus == VerificationStatus.WAITING_FOR_DOCUMENTS) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        MediSlotButton(
                            text = "Resubmit Documents",
                            onClick = {
                                if (isDemoMode) {
                                    if (role == "doctor") {
                                        VerificationStateStore.resubmitDoctor(displayName)
                                    } else {
                                        VerificationStateStore.resubmitHospital(displayName)
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "Please re-register with updated documents.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MediSlotOutlinedButton(
                            text = "Back to Login",
                            onClick = onNavigateToLogin,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        MediSlotOutlinedButton(
                            text = "Log Out",
                            onClick = onNavigateToLogin,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // REVIEWER SIMULATION DRAWER/PANEL (Visible in UI to showcase verification state shifts)
                if (isDemoMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🛠️ Reviewer Demo Controls",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Toggle account approval states to verify UI transition responsiveness.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = { statusMap[displayName] = VerificationStatus.PENDING },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                ) {
                                    Text("Pending", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Button(
                                    onClick = { statusMap[displayName] = VerificationStatus.APPROVED },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                ) {
                                    Text("Approve", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Button(
                                    onClick = {
                                        statusMap[displayName] = VerificationStatus.REJECTED
                                        rejectionReasonsMap[displayName] = if (role == "doctor") {
                                            "MD/MS degree certificate is unreadable. Please upload a high-resolution PDF copy."
                                        } else {
                                            "Invalid License Number or registration records do not match registry."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                ) {
                                    Text("Reject", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Button(
                                    onClick = { statusMap[displayName] = VerificationStatus.WAITING_FOR_DOCUMENTS },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                ) {
                                    Text("Req Docs", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
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
private fun TimelineItem(
    stepNumber: String,
    title: String,
    description: String,
    isCompleted: Boolean,
    isActive: Boolean = false,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        val bubbleColor = when {
            isCompleted -> Color(0xFF10B981)
            isError -> Color(0xFFEF4444)
            isActive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        }

        val textColor = when {
            isCompleted || isActive -> MaterialTheme.colorScheme.onSurface
            isError -> Color(0xFFEF4444)
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(bubbleColor, RoundedCornerShape(100.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(text = stepNumber, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
