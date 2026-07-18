package com.medislot.app.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.ui.components.MediSlotButton
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.theme.LocalDimens

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(
                    text = "Select Workspace Role",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose your account type to align with your workspace.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
 
            // Cards Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RoleCard(
                    roleId = "patient",
                    title = "Patient Workspace",
                    description = "Book appointments, track live queues, analyze symptoms, and manage EHR records.",
                    icon = Icons.Default.Person,
                    color = MaterialTheme.colorScheme.primary,
                    isSelected = selectedRole == "patient",
                    onClick = { selectedRole = "patient" }
                )

                RoleCard(
                    roleId = "doctor",
                    title = "Doctor Workspace",
                    description = "Monitor today's schedule, consult patients, configure slots, and publish prescriptions.",
                    icon = Icons.Default.MedicalServices,
                    color = MaterialTheme.colorScheme.secondary,
                    isSelected = selectedRole == "doctor",
                    onClick = { selectedRole = "doctor" }
                )

                RoleCard(
                    roleId = "hospital",
                    title = "Hospital Coordinator",
                    description = "Real-time resource tracking, coordinator alerts desk, and operational analytics.",
                    icon = Icons.Default.LocalHospital,
                    color = Color(0xFFF59E0B),
                    isSelected = selectedRole == "hospital",
                    onClick = { selectedRole = "hospital" }
                )
            }
 
            Spacer(modifier = Modifier.height(32.dp))
 
            // Confirm/Proceed Button
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                MediSlotButton(
                    text = "Confirm & Proceed",
                    onClick = { selectedRole?.let { onRoleSelected(it) } },
                    enabled = selectedRole != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RoleCard(
    roleId: String,
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.02f else 1.0f, label = "scale")
    val borderColor by animateColorAsState(targetValue = if (isSelected) color else Color(0xFF334155).copy(alpha = 0.5f), label = "border")

    MediSlotCard(
        onClick = onClick,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
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
}
