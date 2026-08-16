package com.medislot.app.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medislot.app.ui.components.MediSlotBottomBar
import com.medislot.app.ui.components.UserRole
import com.medislot.app.ui.screens.auth.ForgotPasswordScreen
import com.medislot.app.ui.screens.auth.LoginScreen
import com.medislot.app.ui.screens.auth.OnboardingScreen
import com.medislot.app.ui.screens.auth.RegisterScreen
import com.medislot.app.ui.screens.auth.RoleSelectionScreen
import com.medislot.app.ui.screens.auth.SplashScreen
import com.medislot.app.ui.screens.auth.VerificationStatusScreen
import com.medislot.app.ui.screens.auth.SuperAdminDashboardScreen
import com.medislot.app.ui.screens.doctor.DoctorAppointmentsScreen
import com.medislot.app.ui.screens.doctor.DoctorDashboardScreen
import com.medislot.app.ui.screens.doctor.DoctorPatientDetailsScreen
import com.medislot.app.ui.screens.doctor.PrescriptionUploadScreen
import com.medislot.app.ui.screens.doctor.SlotManagementScreen
import com.medislot.app.ui.screens.doctor.DoctorProfileScreen
import com.medislot.app.ui.screens.doctor.DoctorHistoryScreen
import com.medislot.app.ui.screens.hospital.AlertsScreen
import com.medislot.app.ui.screens.hospital.AnalyticsScreen
import com.medislot.app.ui.screens.hospital.DoctorManagementScreen
import com.medislot.app.ui.screens.hospital.HospitalDashboardScreen
import com.medislot.app.ui.screens.hospital.ResourceMonitoringScreen
import com.medislot.app.ui.screens.hospital.StaffSchedulingScreen
import com.medislot.app.ui.screens.hospital.DoctorRecruitmentScreen
import com.medislot.app.ui.screens.patient.AppointmentBookingScreen
import com.medislot.app.ui.screens.patient.AppointmentHistoryScreen
import com.medislot.app.ui.screens.patient.DoctorDetailsScreen
import com.medislot.app.ui.screens.patient.DoctorSearchScreen
import com.medislot.app.ui.screens.patient.EmergencyScreen
import com.medislot.app.ui.screens.patient.MedicalRecordsScreen
import com.medislot.app.ui.screens.patient.NotificationsScreen
import com.medislot.app.ui.screens.patient.PatientDashboardScreen
import com.medislot.app.ui.screens.patient.PatientProfileScreen
import com.medislot.app.ui.screens.patient.QueueWaitingScreen
import com.medislot.app.ui.screens.patient.SettingsScreen
import com.medislot.app.ui.screens.patient.SymptomCheckerScreen
import com.medislot.app.ui.screens.patient.HospitalNavigationScreen

@Composable
fun MediSlotApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Track active user role dynamically
    var activeRole by remember { mutableStateOf<UserRole?>(null) }

    // Helper to determine if we should show the bottom bar based on current screen
    val showBottomBar = currentRoute in listOf(
        Screen.PatientHome.route,
        Screen.PatientHistory.route,
        Screen.PatientRecords.route,
        Screen.PatientProfile.route,
        Screen.DoctorHome.route,
        Screen.DoctorSlots.route,
        Screen.DoctorProfile.route,
        Screen.HospitalHome.route,
        Screen.HospitalResources.route,
        Screen.HospitalDoctors.route,
        Screen.HospitalAnalytics.route,
        Screen.HospitalProfile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar && activeRole != null) {
                MediSlotBottomBar(
                    role = activeRole!!,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            enterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }) + androidx.compose.animation.fadeIn() },
            exitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it }) + androidx.compose.animation.fadeOut() },
            popEnterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }) + androidx.compose.animation.fadeIn() },
            popExitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }) + androidx.compose.animation.fadeOut() }
        ) {
            // ==========================================
            // AUTH FLOW
            // ==========================================
            composable(Screen.Splash.route) {
                val coroutineScope = rememberCoroutineScope()
                SplashScreen(
                    onTimeout = {
                        coroutineScope.launch {
                            val authRepo = com.medislot.app.data.repository.AuthenticationRepositoryImpl()
                            val isLoggedIn = authRepo.isLoggedIn()
                            val savedRole = authRepo.getRole()
                            if (isLoggedIn && !savedRole.isNullOrEmpty()) {
                                com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive = false
                                activeRole = when (savedRole) {
                                    "patient" -> UserRole.PATIENT
                                    "doctor" -> UserRole.DOCTOR
                                    "hospital", "hospital_coordinator" -> UserRole.HOSPITAL
                                    else -> null
                                }
                                val targetRoute = when (savedRole) {
                                    "patient" -> Screen.PatientHome.route
                                    "doctor" -> Screen.DoctorHome.route
                                    "hospital", "hospital_coordinator" -> Screen.HospitalHome.route
                                    "super_admin" -> Screen.SuperAdminDashboard.route
                                    else -> Screen.Onboarding.route
                                }
                                navController.navigate(targetRoute) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Onboarding.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onGetStarted = {
                        navController.navigate(Screen.RoleSelection.route)
                    }
                )
            }

            composable(
                route = Screen.Login.route,
                arguments = listOf(navArgument("role") { type = NavType.StringType })
            ) { backStackEntry ->
                val role = backStackEntry.arguments?.getString("role") ?: "patient"
                LoginScreen(
                    role = role,
                    onLoginSuccess = { username ->
                        val isDemoMode = com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive
                        if (isDemoMode) {
                            val currentStatus = com.medislot.app.data.model.VerificationStateStore.userVerificationStatus[username] ?: com.medislot.app.data.model.VerificationStatus.PENDING
                            val isApproved = currentStatus == com.medislot.app.data.model.VerificationStatus.APPROVED

                            if (role == "super_admin") {
                                activeRole = null
                                navController.navigate(Screen.SuperAdminDashboard.route) {
                                    popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                }
                            } else if (role == "patient" || isApproved) {
                                activeRole = when (role) {
                                    "patient" -> UserRole.PATIENT
                                    "doctor" -> UserRole.DOCTOR
                                    "hospital" -> UserRole.HOSPITAL
                                    else -> UserRole.PATIENT
                                }
                                val homeRoute = when (role) {
                                    "patient" -> Screen.PatientHome.route
                                    "doctor" -> Screen.DoctorHome.route
                                    "hospital" -> Screen.HospitalHome.route
                                    else -> Screen.PatientHome.route
                                }
                                navController.navigate(homeRoute) {
                                    popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                }
                            } else {
                                // If doctor or hospital admin is not approved yet, redirect to status checking screen
                                val hospitalSelection = if (role == "doctor") {
                                    com.medislot.app.data.model.VerificationStateStore.doctorHospitalSelections[username] ?: "Apollo Hospital"
                                } else "None"
                                navController.navigate(Screen.VerificationStatus.createRoute(role, hospitalSelection))
                            }
                        } else {
                            if (role == "super_admin") {
                                activeRole = null
                                navController.navigate(Screen.SuperAdminDashboard.route) {
                                    popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                }
                            } else if (role == "patient") {
                                activeRole = UserRole.PATIENT
                                navController.navigate(Screen.PatientHome.route) {
                                    popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                }
                            } else {
                                coroutineScope.launch {
                                    try {
                                        val uid = com.medislot.app.data.local.DatabaseProvider.getDataStoreManager().uidFlow.first()
                                        if (uid != null) {
                                            val res = com.medislot.app.network.RetrofitClient.apiService.getUserStatus(uid)
                                            if (res.status == "Approved") {
                                                activeRole = when (role) {
                                                    "doctor" -> UserRole.DOCTOR
                                                    "hospital" -> UserRole.HOSPITAL
                                                    else -> UserRole.PATIENT
                                                }
                                                val homeRoute = when (role) {
                                                    "doctor" -> Screen.DoctorHome.route
                                                    "hospital" -> Screen.HospitalHome.route
                                                    else -> Screen.PatientHome.route
                                                }
                                                navController.navigate(homeRoute) {
                                                    popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate(Screen.VerificationStatus.createRoute(role, res.hospital_name ?: "None"))
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Failed to verify account status: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.createRoute(role))
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    onNavigateToSuperAdmin = {
                        com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive = false
                        navController.navigate(Screen.SuperAdminDashboard.route)
                    }
                )
            }

            composable(
                route = Screen.Register.route,
                arguments = listOf(navArgument("role") { type = NavType.StringType })
            ) { backStackEntry ->
                val role = backStackEntry.arguments?.getString("role") ?: "patient"
                RegisterScreen(
                    role = role,
                    onRegisterSuccess = { regRole, hospitalName ->
                        // Show verification success screen for Hospital/Doctor, proceed directly for Patient
                        if (regRole == "patient") {
                            navController.navigate(Screen.Login.createRoute(regRole)) {
                                popUpTo(Screen.Register.createRoute(regRole)) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.VerificationStatus.createRoute(regRole, hospitalName)) {
                                popUpTo(Screen.Register.createRoute(regRole)) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.createRoute(role)) {
                            popUpTo(Screen.Login.createRoute(role)) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.VerificationStatus.route,
                arguments = listOf(
                    navArgument("role") { type = NavType.StringType },
                    navArgument("hospitalName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val statusRole = backStackEntry.arguments?.getString("role") ?: "patient"
                val hospitalName = backStackEntry.arguments?.getString("hospitalName") ?: "None"
                VerificationStatusScreen(
                    role = statusRole,
                    hospitalName = hospitalName,
                    onNavigateToLogin = {
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        activeRole = when (statusRole) {
                            "doctor" -> UserRole.DOCTOR
                            "hospital" -> UserRole.HOSPITAL
                            else -> UserRole.PATIENT
                        }
                        val homeRoute = when (statusRole) {
                            "doctor" -> Screen.DoctorHome.route
                            "hospital" -> Screen.HospitalHome.route
                            else -> Screen.PatientHome.route
                        }
                        navController.navigate(homeRoute) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.SuperAdminDashboard.route) {
                SuperAdminDashboardScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onLogout = {
                        coroutineScope.launch {
                            val authRepo = com.medislot.app.data.repository.AuthenticationRepositoryImpl()
                            authRepo.logout()
                            com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive = false
                            com.medislot.app.data.model.VerificationStateStore.reset()
                            navController.navigate(Screen.RoleSelection.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onSubmit = {
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.RoleSelection.route) {
                RoleSelectionScreen(
                    onRoleSelected = { role ->
                        com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive = false
                        navController.navigate(Screen.Login.createRoute(role))
                    },
                    onNavigateToPatientDemo = {
                        com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive = true
                        activeRole = UserRole.PATIENT
                        navController.navigate(Screen.PatientHome.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    },
                    onNavigateToDoctorDemo = {
                        com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive = true
                        activeRole = UserRole.DOCTOR
                        navController.navigate(Screen.DoctorHome.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    },
                    onNavigateToHospitalDemo = {
                        com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive = true
                        activeRole = UserRole.HOSPITAL
                        navController.navigate(Screen.HospitalHome.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    },
                    onNavigateToSuperAdminDemo = {
                        com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive = true
                        activeRole = null
                        navController.navigate(Screen.SuperAdminDashboard.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    }
                )
            }

            // ==========================================
            // PATIENT FLOW
            // ==========================================
            composable(Screen.PatientHome.route) {
                PatientDashboardScreen(
                    onNavigateToSymptomChecker = { navController.navigate(Screen.PatientSymptomChecker.route) },
                    onNavigateToDoctorSearch = { navController.navigate(Screen.PatientDoctorSearch.route) },
                    onNavigateToHistory = { navController.navigate(Screen.PatientHistory.route) },
                    onNavigateToRecords = { navController.navigate(Screen.PatientRecords.route) },
                    onNavigateToEmergency = { navController.navigate(Screen.PatientEmergency.route) },
                    onNavigateToSettings = { navController.navigate(Screen.PatientSettings.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.PatientNotifications.route) },
                    onNavigateToHospitalMap = { navController.navigate(Screen.PatientHospitalMap.route) },
                    onNavigateToBooking = { doctorId -> navController.navigate(Screen.PatientAppointmentBooking.createRoute(doctorId)) },
                    onNavigateToQueue = { apptId -> navController.navigate(Screen.PatientQueueWaiting.createRoute(apptId)) }
                )
            }

            composable(Screen.PatientSymptomChecker.route) {
                SymptomCheckerScreen(
                    onBookClick = { docId ->
                        navController.navigate(Screen.PatientAppointmentBooking.createRoute(docId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PatientDoctorSearch.route) {
                DoctorSearchScreen(
                    onDoctorClick = { docId ->
                        navController.navigate(Screen.PatientDoctorDetails.createRoute(docId))
                    },
                    onBookClick = { docId ->
                        navController.navigate(Screen.PatientAppointmentBooking.createRoute(docId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PatientDoctorDetails.route,
                arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                DoctorDetailsScreen(
                    doctorId = doctorId,
                    onBookAppointment = { docId ->
                        navController.navigate(Screen.PatientAppointmentBooking.createRoute(docId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PatientAppointmentBooking.route,
                arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                AppointmentBookingScreen(
                    doctorId = doctorId,
                    onBookingSuccess = { apptId ->
                        navController.navigate(Screen.PatientQueueWaiting.createRoute(apptId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PatientQueueWaiting.route,
                arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                QueueWaitingScreen(
                    appointmentId = appointmentId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHospitalMap = { navController.navigate(Screen.PatientHospitalMap.route) }
                )
            }

            composable(Screen.PatientHistory.route) {
                AppointmentHistoryScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.PatientRecords.route) {
                MedicalRecordsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.PatientEmergency.route) {
                EmergencyScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.PatientProfile.route) {
                PatientProfileScreen(
                    role = UserRole.PATIENT,
                    onLogout = {
                        activeRole = null
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PatientSettings.route) {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.PatientNotifications.route) {
                NotificationsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.PatientHospitalMap.route) {
                HospitalNavigationScreen(onNavigateBack = { navController.popBackStack() })
            }

            // ==========================================
            // DOCTOR FLOW
            // ==========================================
            composable(Screen.DoctorHome.route) {
                val coroutineScope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    if (!com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
                        coroutineScope.launch {
                            try {
                                val authRepo = com.medislot.app.data.repository.AuthenticationRepositoryImpl()
                                val docRepo = com.medislot.app.data.repository.DoctorRepositoryImpl()
                                val uid = authRepo.getUid()
                                if (uid != null) {
                                    val result = docRepo.getProfile(uid)
                                    result.fold(
                                        onSuccess = { response ->
                                            com.medislot.app.ui.screens.doctor.DoctorWorkspaceState.doctorProfile =
                                                com.medislot.app.ui.screens.doctor.DoctorProfileInfo(
                                                    name = response.name,
                                                    specialization = response.specialization,
                                                    hospital = response.hospital_name,
                                                    experience = "${response.experience_years} Years",
                                                    contactNumber = response.contact,
                                                    averageConsultationTime = 12
                                                )
                                            com.medislot.app.ui.screens.doctor.DoctorWorkspaceState.activeSlots.clear()
                                            com.medislot.app.ui.screens.doctor.DoctorWorkspaceState.activeSlots.addAll(
                                                response.slot_times?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                                            )
                                            
                                            // Fetch real queue/appointments
                                            val apptResult = docRepo.getAppointments(response.id)
                                            apptResult.fold(
                                                onSuccess = { appts ->
                                                    com.medislot.app.ui.screens.doctor.DoctorWorkspaceState.appointments.clear()
                                                    val mapped = appts.map { apt ->
                                                        com.medislot.app.ui.screens.doctor.PatientRecord(
                                                            id = apt.id,
                                                            name = "Patient " + apt.patient_id.takeLast(4),
                                                            queueNumber = apt.queue_number,
                                                            appointmentTime = apt.time,
                                                            age = 30,
                                                            gender = "Male",
                                                            bloodGroup = "O+",
                                                            height = "170 cm",
                                                            weight = "70 kg",
                                                            bmi = "24.2",
                                                            allergies = emptyList(),
                                                            medications = emptyList(),
                                                            history = emptyList(),
                                                            previousVisits = emptyList(),
                                                            uploadedReports = emptyList(),
                                                            emergencyContact = "",
                                                            symptoms = "Consultation",
                                                            priority = "Normal",
                                                            status = when (apt.status) {
                                                                "Upcoming" -> "Waiting"
                                                                "CheckedIn" -> "Checked In"
                                                                "InConsultation" -> "In Consultation"
                                                                else -> apt.status
                                                            }
                                                        )
                                                    }
                                                    com.medislot.app.ui.screens.doctor.DoctorWorkspaceState.appointments.addAll(mapped)
                                                },
                                                onFailure = {}
                                            )
                                        },
                                        onFailure = {}
                                    )
                                }
                            } catch (e: Exception) {}
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
                        DemoBanner()
                    }
                    DoctorDashboardScreen(
                        onNavigateToAppointments = { navController.navigate(Screen.DoctorAppointments.route) },
                        onNavigateToSlots = { navController.navigate(Screen.DoctorSlots.route) },
                        onLogout = {
                            com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive = false
                            activeRole = null
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToHistory = { navController.navigate("doctor_history") }
                    )
                }
            }

            composable(Screen.DoctorAppointments.route) {
                DoctorAppointmentsScreen(
                    onNavigateToPatientDetails = { patId ->
                        navController.navigate(Screen.DoctorPatientDetails.createRoute(patId))
                    },
                    onNavigateToUploadPrescription = { apptId ->
                        navController.navigate(Screen.DoctorPrescriptionUpload.createRoute(apptId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.DoctorPatientDetails.route,
                arguments = listOf(navArgument("patientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
                DoctorPatientDetailsScreen(
                    patientId = patientId,
                    onNavigateToUploadPrescription = { apptId ->
                        navController.navigate(Screen.DoctorPrescriptionUpload.createRoute(apptId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.DoctorPrescriptionUpload.route,
                arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                PrescriptionUploadScreen(
                    appointmentId = appointmentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DoctorSlots.route) {
                SlotManagementScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("doctor_history") {
                DoctorHistoryScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.DoctorProfile.route) {
                DoctorProfileScreen(
                    onLogout = {
                        activeRole = null
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ==========================================
            // HOSPITAL FLOW
            // ==========================================
            composable(Screen.HospitalHome.route) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
                        DemoBanner()
                    }
                    HospitalDashboardScreen(
                        onNavigateToDoctors = { navController.navigate(Screen.HospitalDoctors.route) },
                        onNavigateToResources = { navController.navigate(Screen.HospitalResources.route) },
                        onNavigateToAlerts = { navController.navigate(Screen.HospitalAlerts.route) },
                        onNavigateToAnalytics = { navController.navigate(Screen.HospitalAnalytics.route) },
                        onNavigateToStaffScheduling = { navController.navigate(Screen.HospitalStaffScheduling.route) },
                        onNavigateToDoctorRecruitment = { navController.navigate(Screen.HospitalDoctorRecruitment.route) },
                        onLogout = {
                            com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive = false
                            activeRole = null
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(Screen.HospitalDoctors.route) {
                DoctorManagementScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.HospitalResources.route) {
                ResourceMonitoringScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.HospitalAlerts.route) {
                AlertsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.HospitalAnalytics.route) {
                AnalyticsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.HospitalStaffScheduling.route) {
                StaffSchedulingScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.HospitalDoctorRecruitment.route) {
                DoctorRecruitmentScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.HospitalProfile.route) {
                PatientProfileScreen(
                    role = UserRole.HOSPITAL,
                    onLogout = {
                        activeRole = null
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun DemoBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0F2FE))
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Demo Mode - Authentication Bypassed",
                color = Color(0xFF0369A1),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
