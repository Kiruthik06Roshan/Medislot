package com.medislot.app.ui.navigation

sealed class Screen(val route: String) {
    // Auth Routes
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object RoleSelection : Screen("role_selection")

    // Patient Routes
    object PatientHome : Screen("patient_home")
    object PatientSymptomChecker : Screen("patient_symptom_checker")
    object PatientDoctorSearch : Screen("patient_doctor_search")
    object PatientDoctorDetails : Screen("patient_doctor_details/{doctorId}") {
        fun createRoute(doctorId: String) = "patient_doctor_details/$doctorId"
    }
    object PatientAppointmentBooking : Screen("patient_appointment_booking/{doctorId}") {
        fun createRoute(doctorId: String) = "patient_appointment_booking/$doctorId"
    }
    object PatientQueueWaiting : Screen("patient_queue_waiting/{appointmentId}") {
        fun createRoute(appointmentId: String) = "patient_queue_waiting/$appointmentId"
    }
    object PatientHistory : Screen("patient_history")
    object PatientRecords : Screen("patient_records")
    object PatientEmergency : Screen("patient_emergency")
    object PatientProfile : Screen("patient_profile")
    object PatientSettings : Screen("patient_settings")
    object PatientNotifications : Screen("patient_notifications")
    object PatientHospitalMap : Screen("patient_hospital_map")

    // Doctor Routes
    object DoctorHome : Screen("doctor_home")
    object DoctorAppointments : Screen("doctor_appointments")
    object DoctorPatientDetails : Screen("doctor_patient_details/{patientId}") {
        fun createRoute(patientId: String) = "doctor_patient_details/$patientId"
    }
    object DoctorPrescriptionUpload : Screen("doctor_prescription_upload/{appointmentId}") {
        fun createRoute(appointmentId: String) = "doctor_prescription_upload/$appointmentId"
    }
    object DoctorSlots : Screen("doctor_slots")
    object DoctorProfile : Screen("doctor_profile")

    // Hospital Routes
    object HospitalHome : Screen("hospital_home")
    object HospitalDoctors : Screen("hospital_doctors")
    object HospitalResources : Screen("hospital_resources")
    object HospitalAlerts : Screen("hospital_alerts")
    object HospitalAnalytics : Screen("hospital_analytics")
    object HospitalProfile : Screen("hospital_profile")
}
