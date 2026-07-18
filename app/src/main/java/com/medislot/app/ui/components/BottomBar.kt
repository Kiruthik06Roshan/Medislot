package com.medislot.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class UserRole {
    PATIENT, DOCTOR, HOSPITAL
}

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    // Patient Items
    object PatientHome : BottomNavItem("patient_home", Icons.Default.Home, "Home")
    object PatientHistory : BottomNavItem("patient_history", Icons.Default.History, "History")
    object PatientRecords : BottomNavItem("patient_records", Icons.Default.Description, "Records")
    object PatientProfile : BottomNavItem("patient_profile", Icons.Default.Person, "Profile")

    // Doctor Items
    object DoctorHome : BottomNavItem("doctor_home", Icons.Default.Dashboard, "Dashboard")
    object DoctorSchedule : BottomNavItem("doctor_slots", Icons.Default.CalendarMonth, "Slots")
    object DoctorProfile : BottomNavItem("doctor_profile", Icons.Default.Person, "Profile")

    // Hospital Items
    object HospitalHome : BottomNavItem("hospital_home", Icons.Default.Dashboard, "Dashboard")
    object HospitalResources : BottomNavItem("hospital_resources", Icons.Default.MonitorHeart, "Resources")
    object HospitalDoctors : BottomNavItem("hospital_doctors", Icons.Default.People, "Doctors")
    object HospitalAnalytics : BottomNavItem("hospital_analytics", Icons.Default.Analytics, "Analytics")
    object HospitalProfile : BottomNavItem("hospital_profile", Icons.Default.Person, "Profile")
}

@Composable
fun MediSlotBottomBar(
    role: UserRole,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = when (role) {
        UserRole.PATIENT -> listOf(
            BottomNavItem.PatientHome,
            BottomNavItem.PatientHistory,
            BottomNavItem.PatientRecords,
            BottomNavItem.PatientProfile
        )
        UserRole.DOCTOR -> listOf(
            BottomNavItem.DoctorHome,
            BottomNavItem.DoctorSchedule,
            BottomNavItem.DoctorProfile
        )
        UserRole.HOSPITAL -> listOf(
            BottomNavItem.HospitalHome,
            BottomNavItem.HospitalResources,
            BottomNavItem.HospitalDoctors,
            BottomNavItem.HospitalAnalytics,
            BottomNavItem.HospitalProfile
        )
    }

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
