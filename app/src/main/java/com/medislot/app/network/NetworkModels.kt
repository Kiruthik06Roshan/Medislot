package com.medislot.app.network

data class TokenResponse(
    val access_token: String = "",
    val refresh_token: String = "",
    val token_type: String = "",
    val uid: String = "",
    val role: String = "",
    val email: String = ""
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val full_name: String,
    val role: String
)

data class PatientProfileRequest(
    val uid: String,
    val age: Int,
    val gender: String,
    val contact: String,
    val blood_group: String,
    val height: String,
    val weight: String,
    val bmi: String,
    val allergies: String? = null,
    val medications: String? = null,
    val medical_history: String? = null,
    val insurance_provider: String? = null,
    val insurance_plan: String? = null,
    val insurance_policy_number: String? = null,
    val insurance_expiry: String? = null,
    val emergency_contact_name: String? = null,
    val emergency_contact_phone: String? = null,
    val emergency_contact_relation: String? = null,
    val vitals_heart_rate: Int? = null,
    val vitals_bp: String? = null,
    val vitals_spo2: Int? = null,
    val vitals_temperature: Float? = null,
    val vitals_blood_sugar: Int? = null
)

data class PatientProfileResponse(
    val id: String,
    val uid: String,
    val age: Int,
    val gender: String,
    val contact: String,
    val blood_group: String,
    val height: String,
    val weight: String,
    val bmi: String,
    val allergies: String? = null,
    val medications: String? = null,
    val medical_history: String? = null,
    val insurance_provider: String? = null,
    val insurance_plan: String? = null,
    val insurance_policy_number: String? = null,
    val insurance_expiry: String? = null,
    val emergency_contact_name: String? = null,
    val emergency_contact_phone: String? = null,
    val emergency_contact_relation: String? = null,
    val vitals_heart_rate: Int? = null,
    val vitals_bp: String? = null,
    val vitals_spo2: Int? = null,
    val vitals_temperature: Float? = null,
    val vitals_blood_sugar: Int? = null
)

data class DoctorProfileRequest(
    val uid: String,
    val specialization: String,
    val hospital_name: String,
    val experience_years: Int,
    val contact: String,
    val mbbs_institution: String,
    val registration_number: String,
    val slot_times: String? = null
)

data class DoctorProfileResponse(
    val id: String,
    val uid: String,
    val name: String,
    val specialization: String,
    val hospital_name: String,
    val rating: Float,
    val experience_years: Int,
    val fees: String,
    val bio: String?,
    val availability: String,
    val slot_times: String?,
    val contact: String,
    val status: String,
    val room: String,
    val shift: String,
    val mbbs_institution: String?,
    val registration_number: String?
)

fun DoctorProfileResponse.toDoctorProfileData(): com.medislot.app.data.model.DoctorProfileData {
    return com.medislot.app.data.model.DoctorProfileData(
        id = id,
        name = name,
        department = specialization,
        hospital = hospital_name,
        rating = rating,
        experience = if (experience_years > 0) "$experience_years Years" else "5 Years",
        fees = if (fees.isNotBlank()) fees else "$100",
        bio = bio ?: "Specialist at $hospital_name",
        availability = if (availability.isNotBlank()) availability else "Monday - Friday",
        slotTimes = slot_times?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?: listOf("09:00 AM", "10:30 AM", "02:00 PM"),
        email = contact,
        contact = contact,
        status = if (status.isNotBlank()) status else "On Duty",
        room = if (room.isNotBlank()) room else "Room 2A",
        shift = if (shift.isNotBlank()) shift else "Day Shift"
    )
}

data class AppointmentRequest(
    val patient_id: String,
    val doctor_id: String,
    val doctor_name: String,
    val department: String,
    val hospital: String,
    val date: String,
    val time: String
)

data class AppointmentResponse(
    val id: String,
    val patient_id: String,
    val doctor_id: String,
    val doctor_name: String,
    val department: String,
    val hospital: String,
    val date: String,
    val time: String,
    val status: String,
    val queue_number: Int,
    val patient_name: String? = null
)

fun AppointmentResponse.toAppointmentData(): com.medislot.app.data.model.AppointmentData {
    return com.medislot.app.data.model.AppointmentData(
        id = id,
        doctorName = doctor_name,
        department = department,
        hospital = hospital,
        date = date,
        time = time,
        status = status,
        queueNumber = queue_number
    )
}

data class MedicalRecordRequest(
    val patient_id: String,
    val title: String,
    val record_type: String,
    val date: String,
    val file_url: String?,
    val result_summary: String?,
    val doctor_id: String?
)

data class MedicalRecordResponse(
    val id: String,
    val patient_id: String,
    val title: String,
    val record_type: String,
    val date: String,
    val file_url: String?,
    val result_summary: String?,
    val doctor_id: String?
)

data class InventoryItemResponse(
    val id: String,
    val name: String,
    val total: Int,
    val available: Int,
    val unit: String,
    val category: String,
    val last_updated: String,
    val trend: String,
    val is_trend_positive: Boolean
)

data class OperationalAlertResponse(
    val id: String,
    val title: String,
    val message: String,
    val severity: String,
    val timestamp: String,
    val department: String,
    val is_resolved: Boolean
)

data class DoctorApplicationRequest(
    val uid: String? = null,
    val name: String,
    val specialization: String,
    val experience_years: String,
    val medical_registration_number: String,
    val mbbs_institution: String,
    val docs_attached: String?,
    val resume_file: String?,
    val selected_hospital: String
)

data class DoctorApplicationResponse(
    val id: String,
    val uid: String?,
    val name: String,
    val specialization: String,
    val experience_years: String,
    val medical_registration_number: String,
    val mbbs_institution: String,
    val docs_attached: String?,
    val resume_file: String?,
    val selected_hospital: String,
    val status: String,
    val rejection_reason: String?
)

data class StaffScheduleRequest(
    val name: String,
    val role: String,
    val department: String,
    val date: String,
    val shift_type: String,
    val shift_time: String,
    val room: String,
    val status: String
)

data class StaffScheduleResponse(
    val id: String,
    val name: String,
    val role: String,
    val department: String,
    val date: String,
    val shift_type: String,
    val shift_time: String,
    val room: String,
    val status: String
)

data class LeaveRequestResponse(
    val id: String,
    val staff_id: String,
    val staff_name: String,
    val role: String,
    val department: String,
    val start_date: String,
    val end_date: String,
    val reason: String,
    val status: String
)

data class AiLogRequest(
    val prompt_type: String,
    val prompt: String,
    val response: String,
    val latency_ms: Int,
    val model_used: String,
    val was_cached: Boolean
)

data class HospitalRegisterRequest(
    val name: String,
    val uid: String,
    val license_number: String,
    val registration_number: String,
    val address: String,
    val hospital_type: String,
    val departments: String,
    val contact: String,
    val admin_name: String,
    val docs_attached: String?
)

data class HospitalResponse(
    val id: String,
    val name: String,
    val uid: String?,
    val license_number: String,
    val registration_number: String,
    val address: String,
    val hospital_type: String,
    val departments: String,
    val contact: String,
    val admin_name: String,
    val status: String,
    val rejection_reason: String?,
    val docs_attached: String?
)

data class UserStatusResponse(
    val status: String,
    val rejection_reason: String?,
    val hospital_name: String?
)

data class StaffMemberResponse(
    val id: String,
    val name: String,
    val role: String,
    val department: String,
    val room: String,
    val status: String
)
