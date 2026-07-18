package com.medislot.app.ui.screens.hospital

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.GasMeter
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.data.model.MockData
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.components.MediSlotSearchBar
import com.medislot.app.ui.components.MediSlotTopBar
import com.medislot.app.ui.components.SectionHeader
import com.medislot.app.ui.components.StatusChip
import com.medislot.app.ui.theme.LocalDimens
import com.medislot.app.ui.theme.SOSGradient

@Composable
fun HospitalDashboardScreen(
    onNavigateToDoctors: () -> Unit,
    onNavigateToResources: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onLogout: () -> Unit
) {
    val activeAlerts = MockData.notifications.count { it.type == "Emergency" && !it.isRead }
    val icuResource = MockData.resources.find { it.name == "ICU Beds" }
    val oxygenResource = MockData.resources.find { it.name == "Oxygen Reserves" }

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Hospital Admin Desk",
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
                text = "Welcome, Admin",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "City General Hospital",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Operational Alerts Bar
            if (activeAlerts > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(colors = SOSGradient))
                        .clickable { onNavigateToAlerts() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Operational Alerts Active", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("You have $activeAlerts unresolved critical alerts. Review now.", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Hospital Load progress card
            SectionHeader(title = "Operational Load")
            Spacer(modifier = Modifier.height(12.dp))
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hospital Intake Level", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        StatusChip(status = "Critical (82%)")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { 0.82f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.error,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stat Grid
            SectionHeader(title = "Primary Resources")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ICU bed card
                icuResource?.let {
                    MediSlotCard(
                        onClick = onNavigateToResources,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column {
                            Icon(Icons.Default.Bed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("ICU Beds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${it.available}/${it.total} free", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Oxygen reserves card
                oxygenResource?.let {
                    MediSlotCard(
                        onClick = onNavigateToResources,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column {
                            Icon(Icons.Default.GasMeter, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Oxygen Reserves", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${it.available} L", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Resource Grid CTAs
            SectionHeader(title = "Quick Actions")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MediSlotCard(
                    onClick = onNavigateToDoctors,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Icon(Icons.Default.People, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Doctors Online", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("14 active staff", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                MediSlotCard(
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Icon(Icons.Default.Analytics, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Review occupancy trends", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Admissions overview stats
            SectionHeader(title = "Daily Operational Summary")
            Spacer(modifier = Modifier.height(12.dp))
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Today's Admissions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("24 patients", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Patients in Queue", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("30 waiting", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Registered Duty Staff", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("56 online staff", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================================
// Doctor Management Screen
// ==========================================================
@Composable
fun DoctorManagementScreen(onNavigateBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("All") }
    val departments = listOf("All", "Cardiology", "Neurology", "Orthopedics", "Pediatrics")

    val filteredDoctors = MockData.doctors.filter { doc ->
        (selectedDept == "All" || doc.department == selectedDept) &&
                doc.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Doctor Allocation", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            MediSlotSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search doctor by name"
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredDoctors) { doctor ->
                    MediSlotCard {
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
                                Text(doctor.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${doctor.department} • Room ${if (doctor.department == "Cardiology") "4B" else "2A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            StatusChip(status = "On Duty")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// Resource Monitoring Screen
// ==========================================================
@Composable
fun ResourceMonitoringScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = { MediSlotTopBar(title = "Resource Monitor", onBackClick = onNavigateBack) }
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
                title = "Real-Time Resources",
                subtitle = "Operational thresholds tracking for emergency backups."
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(MockData.resources) { res ->
                    val progress = res.available.toFloat() / res.total.toFloat()
                    val (color, status) = if (progress < 0.25f) {
                        Pair(MaterialTheme.colorScheme.error, "Critical")
                    } else if (progress < 0.6f) {
                        Pair(Color(0xFFF59E0B), "Warning")
                    } else {
                        Pair(MaterialTheme.colorScheme.secondary, "Normal")
                    }

                    MediSlotCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(res.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Category: ${res.category}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${res.available} / ${res.total}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        StatusChip(status = status)
                                    }
                                    Text(res.unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// Operations Alerts Screen
// ==========================================================
@Composable
fun AlertsScreen(onNavigateBack: () -> Unit) {
    val alerts = MockData.notifications.filter { it.priority == "HIGH" }

    Scaffold(
        topBar = { MediSlotTopBar(title = "Operations Alerts", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (alerts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "System Normal. No alerts active.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    items(alerts) { alert ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(alert.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(alert.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// Analytics Screen
// ==========================================================
@Composable
fun AnalyticsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = { MediSlotTopBar(title = "Hospital Analytics", onBackClick = onNavigateBack) }
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

            SectionHeader(title = "Admissions Intake Trends")

            // Mini admissions bar chart layout
            MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Monthly Admissions Rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Average patient intake distributions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        ChartBar("Jan", 0.4f, MaterialTheme.colorScheme.primary)
                        ChartBar("Feb", 0.65f, MaterialTheme.colorScheme.primary)
                        ChartBar("Mar", 0.8f, MaterialTheme.colorScheme.primary)
                        ChartBar("Apr", 0.5f, MaterialTheme.colorScheme.primary)
                        ChartBar("May", 0.9f, MaterialTheme.colorScheme.error)
                        ChartBar("Jun", 0.72f, MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Department load stats
            SectionHeader(title = "Department Load Occupancy")
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MockData.departmentsUsage.forEach { dept ->
                    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(dept.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text("Patients in queue: ${dept.patientsWaiting}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${dept.loadPercentage}% occupancy",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dept.loadPercentage > 85) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { dept.loadPercentage.toFloat() / 100f },
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = if (dept.loadPercentage > 85) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChartBar(label: String, fillFraction: Float, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.width(36.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp * fillFraction)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
