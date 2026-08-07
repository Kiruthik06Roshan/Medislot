package com.medislot.app.ui.screens.hospital

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.medislot.app.data.model.LeaveRequest
import com.medislot.app.data.model.StaffMember
import com.medislot.app.data.model.StaffSchedule
import com.medislot.app.ui.components.*
import com.medislot.app.viewmodel.AiState
import com.medislot.app.viewmodel.HospitalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffSchedulingScreen(
    onNavigateBack: () -> Unit,
    viewModel: HospitalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val schedules by viewModel.staffSchedules.collectAsState()
    val leaveRequests by viewModel.leaveRequests.collectAsState()
    val staffMembers by viewModel.staffMembers.collectAsState()
    val aiStaffState by viewModel.aiStaffRecommendationState.collectAsState()

    var activeTab by remember { mutableStateOf("Schedule") } // "Schedule", "Leaves"
    var selectedDay by remember { mutableStateOf("Monday") }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    // Filter schedules by day
    val schedulesForDay = schedules.filter { it.date.equals(selectedDay, ignoreCase = true) }

    // Dialog state
    var showAssignDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<StaffSchedule?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchStaffRecommendations(forceRefresh = false)
    }

    Scaffold(
        topBar = {
            MediSlotTopBar(
                title = "Staff Scheduling",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.fetchStaffRecommendations(forceRefresh = true) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Get AI Advice", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // Navigation Tabs (Schedule vs Leave Management)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    listOf("Schedule", "Leaves").forEach { tab ->
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
                                text = if (tab == "Leaves") "Leaves (${leaveRequests.count { it.status == "Pending" }})" else "Weekly Roster",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            if (activeTab == "Schedule") {
                // Schedule Analytics Summary Row
                item {
                    val docsCount = schedulesForDay.count { it.role == "Doctor" && it.status == "On Duty" }
                    val nursesCount = schedulesForDay.count { it.role == "Nurse" && it.status == "On Duty" }
                    val leavesCount = schedulesForDay.count { it.status == "Leave" }
                    val emergencyCount = schedulesForDay.count { it.status == "Emergency Duty" || it.shiftType == "Emergency" }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsMiniCard("Doctors On Duty", "$docsCount", Icons.Default.People, Modifier.weight(1f))
                        AnalyticsMiniCard("Nurses On Duty", "$nursesCount", Icons.Default.MedicalServices, Modifier.weight(1f))
                        AnalyticsMiniCard("On Leave", "$leavesCount", Icons.Default.HourglassEmpty, Modifier.weight(1f))
                        AnalyticsMiniCard("Emergency", "$emergencyCount", Icons.Default.Warning, Modifier.weight(1f))
                    }
                }

                // AI suggestions banner
                item {
                    when (val state = aiStaffState) {
                        is AiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                            }
                        }
                        is AiState.Success -> {
                            val recommendations = state.data
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SmartToy, null, tint = Color(0xFF2563EB), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Gemini Scheduler AI Recommendation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                                        Text("${recommendations.additionalStaffing} ${recommendations.doctorRedistribution}", fontSize = 12.sp, color = Color(0xFF1E3A8A))
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                // Days of week selector
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(daysOfWeek) { day ->
                            val isSelected = selectedDay == day
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedDay = day }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = day,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Schedule Operations Row (Assign Shift, Duplicate Roster)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MediSlotButton(
                            text = "Assign Shift",
                            onClick = { showAssignDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        MediSlotOutlinedButton(
                            text = "Duplicate Previous Week",
                            onClick = {
                                viewModel.duplicatePreviousWeek()
                                Toast.makeText(context, "🟢 Previous week duplicated successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Roster list
                if (schedulesForDay.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No shifts assigned for $selectedDay.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                } else {
                    items(schedulesForDay) { schedule ->
                        StaffScheduleCard(
                            schedule = schedule,
                            onEdit = { editingSchedule = schedule },
                            onDelete = { viewModel.deleteShift(schedule.id) }
                        )
                    }
                }
            } else {
                // Leaves Management tab
                val pendingLeaves = leaveRequests.filter { it.status == "Pending" }
                val resolvedLeaves = leaveRequests.filter { it.status != "Pending" }

                item {
                    Text("Pending Requests", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                if (pendingLeaves.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No pending leave requests.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(pendingLeaves) { leave ->
                        LeaveRequestCard(
                            request = leave,
                            onApprove = { viewModel.approveLeave(leave.id) },
                            onReject = { viewModel.rejectLeave(leave.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Resolved Requests", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                if (resolvedLeaves.isEmpty()) {
                    item {
                        Text("No history of resolved leaves.", color = MaterialTheme.colorScheme.outline, fontSize = 13.sp)
                    }
                } else {
                    items(resolvedLeaves) { leave ->
                        ResolvedLeaveCard(request = leave)
                    }
                }
            }
        }
    }

    // Dialog: Assign Shift
    if (showAssignDialog) {
        ShiftAssignmentDialog(
            staffList = staffMembers,
            selectedDay = selectedDay,
            onDismiss = { showAssignDialog = false },
            onSave = { name, role, dept, day, type, time, room, status ->
                val sched = StaffSchedule(
                    id = "sch_${System.currentTimeMillis()}",
                    name = name,
                    role = role,
                    department = dept,
                    date = day,
                    shiftType = type,
                    shiftTime = time,
                    room = room,
                    status = status
                )
                viewModel.assignShift(sched)
                showAssignDialog = false
                Toast.makeText(context, "🟢 Shift assigned successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Edit Shift
    if (editingSchedule != null) {
        ShiftAssignmentDialog(
            staffList = staffMembers,
            selectedDay = editingSchedule!!.date,
            initialSchedule = editingSchedule,
            onDismiss = { editingSchedule = null },
            onSave = { name, role, dept, day, type, time, room, status ->
                val updated = editingSchedule!!.copy(
                    name = name,
                    role = role,
                    department = dept,
                    date = day,
                    shiftType = type,
                    shiftTime = time,
                    room = room,
                    status = status
                )
                viewModel.editShift(updated)
                editingSchedule = null
                Toast.makeText(context, "🟢 Shift updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AnalyticsMiniCard(title: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(count, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StaffScheduleCard(
    schedule: StaffSchedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(schedule.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (schedule.role) {
                                    "Doctor" -> Color(0xFFEFF6FF)
                                    "Nurse" -> Color(0xFFECFDF5)
                                    else -> Color(0xFFF2F4F7)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            schedule.role,
                            color = when (schedule.role) {
                                "Doctor" -> Color(0xFF2563EB)
                                "Nurse" -> Color(0xFF10B981)
                                else -> Color(0xFF475467)
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${schedule.department} • ${schedule.room}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${schedule.shiftType} shift (${schedule.shiftTime})", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }

            Column(horizontalAlignment = Alignment.End) {
                val statusColor = when (schedule.status) {
                    "On Duty" -> Color(0xFF10B981)
                    "Leave" -> Color(0xFFF59E0B)
                    "Emergency Duty" -> Color(0xFFEF4444)
                    else -> Color(0xFF6B7280)
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(schedule.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LeaveRequestCard(
    request: LeaveRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(request.staffName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text("${request.role} • ${request.department}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Pending Review", color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Period: ${request.startDate} to ${request.endDate}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Reason: \"${request.reason}\"", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve", color = Color.White)
                }
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ResolvedLeaveCard(request: LeaveRequest) {
    val isApproved = request.status == "Approved"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(request.staffName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                Text("${request.startDate} to ${request.endDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val statusColor = if (isApproved) Color(0xFF10B981) else Color(0xFFEF4444)
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(request.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftAssignmentDialog(
    staffList: List<StaffMember>,
    selectedDay: String,
    initialSchedule: StaffSchedule? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialSchedule?.name ?: "") }
    var role by remember { mutableStateOf(initialSchedule?.role ?: "Doctor") }
    var dept by remember { mutableStateOf(initialSchedule?.department ?: "Cardiology") }
    var day by remember { mutableStateOf(initialSchedule?.date ?: selectedDay) }
    var shiftType by remember { mutableStateOf(initialSchedule?.shiftType ?: "Morning") }
    var customTime by remember { mutableStateOf(initialSchedule?.shiftTime ?: "") }
    var room by remember { mutableStateOf(initialSchedule?.room ?: "Room 101") }
    var status by remember { mutableStateOf(initialSchedule?.status ?: "On Duty") }

    val rolesList = listOf("Doctor", "Nurse", "Receptionist", "Lab Technician", "Pharmacist")
    val shiftTypes = listOf("Morning", "Afternoon", "Night", "Emergency", "Custom Shift")
    val statuses = listOf("On Duty", "Off Duty", "Leave", "Emergency Duty")

    // Automatic filling from selection
    var showStaffDropdown by remember { mutableStateOf(false) }

    val calculatedTime = when (shiftType) {
        "Morning" -> "07:00 AM - 01:00 PM"
        "Afternoon" -> "01:00 PM - 07:00 PM"
        "Night" -> "07:00 PM - 07:00 AM"
        "Emergency" -> "On-Call Emergency"
        else -> customTime
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialSchedule != null) "Edit Shift Assignment" else "Assign Staff Shift", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dropdown to pick staff member
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Staff Name") },
                        trailingIcon = {
                            IconButton(onClick = { showStaffDropdown = !showStaffDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showStaffDropdown,
                        onDismissRequest = { showStaffDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        staffList.forEach { staff ->
                            DropdownMenuItem(
                                text = { Text("${staff.name} (${staff.role} - ${staff.department})") },
                                onClick = {
                                    name = staff.name
                                    role = staff.role
                                    dept = staff.department
                                    room = staff.room
                                    showStaffDropdown = false
                                }
                            )
                        }
                    }
                }

                // Display inferred Role & Dept
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role") },
                        modifier = Modifier.weight(1f),
                        enabled = false
                    )
                    OutlinedTextField(
                        value = dept,
                        onValueChange = { dept = it },
                        label = { Text("Department") },
                        modifier = Modifier.weight(1f),
                        enabled = false
                    )
                }

                // Date day selector
                OutlinedTextField(
                    value = day,
                    onValueChange = { day = it },
                    label = { Text("Day of Week") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Shift Type
                var showShiftDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = shiftType,
                        onValueChange = { shiftType = it },
                        label = { Text("Shift Type") },
                        trailingIcon = {
                            IconButton(onClick = { showShiftDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showShiftDropdown,
                        onDismissRequest = { showShiftDropdown = false }
                    ) {
                        shiftTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    shiftType = type
                                    showShiftDropdown = false
                                }
                            )
                        }
                    }
                }

                // Room field
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room / Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Shift time details

                if (shiftType == "Custom Shift") {
                    OutlinedTextField(
                        value = customTime,
                        onValueChange = { customTime = it },
                        label = { Text("Enter Shift Time (e.g. 10:00 AM - 02:00 PM)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = calculatedTime,
                        onValueChange = {},
                        label = { Text("Shift Time Duration") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }

                // Status dropdown
                var showStatusDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("Duty Status") },
                        trailingIcon = {
                            IconButton(onClick = { showStatusDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        statuses.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    status = st
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalTime = if (shiftType == "Custom Shift") customTime else calculatedTime
                        onSave(name, role, dept, day, shiftType, finalTime, room, status)
                    }
                }
            ) {
                Text("Save Assignment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
