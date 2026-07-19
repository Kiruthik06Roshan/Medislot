package com.medislot.app.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
                SplashScreen(
                    onTimeout = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onGetStarted = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.RoleSelection.route)
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.RoleSelection.route)
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onSubmit = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.RoleSelection.route) {
                RoleSelectionScreen(
                    onRoleSelected = { role ->
                        when (role) {
                            "patient" -> {
                                activeRole = UserRole.PATIENT
                                navController.navigate(Screen.PatientHome.route) {
                                    popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                }
                            }
                            "doctor" -> {
                                activeRole = UserRole.DOCTOR
                                navController.navigate(Screen.DoctorHome.route) {
                                    popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                }
                            }
                            "hospital" -> {
                                activeRole = UserRole.HOSPITAL
                                navController.navigate(Screen.HospitalHome.route) {
                                    popUpTo(Screen.RoleSelection.route) { inclusive = true }
                                }
                            }
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
                DoctorDashboardScreen(
                    onNavigateToAppointments = { navController.navigate(Screen.DoctorAppointments.route) },
                    onNavigateToSlots = { navController.navigate(Screen.DoctorSlots.route) },
                    onLogout = {
                        activeRole = null
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToHistory = { navController.navigate("doctor_history") }
                )
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
                HospitalDashboardScreen(
                    onNavigateToDoctors = { navController.navigate(Screen.HospitalDoctors.route) },
                    onNavigateToResources = { navController.navigate(Screen.HospitalResources.route) },
                    onNavigateToAlerts = { navController.navigate(Screen.HospitalAlerts.route) },
                    onNavigateToAnalytics = { navController.navigate(Screen.HospitalAnalytics.route) },
                    onLogout = {
                        activeRole = null
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
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
