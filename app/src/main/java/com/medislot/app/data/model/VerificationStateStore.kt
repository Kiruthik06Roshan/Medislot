package com.medislot.app.data.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf

enum class VerificationStatus {
    PENDING, APPROVED, REJECTED, WAITING_FOR_DOCUMENTS
}

data class HospitalApplication(
    val id: String,
    val name: String,
    val regNumber: String,
    val licenseNumber: String,
    val submittedDate: String,
    var status: VerificationStatus,
    var rejectionReason: String = ""
)

data class DoctorApplication(
    val id: String,
    val name: String,
    val specialization: String,
    val hospitalName: String,
    val experienceYears: String,
    val docsAttached: String,
    val submittedDate: String,
    var status: VerificationStatus,
    var rejectionReason: String = "",
    val medicalRegistrationNumber: String = "MC-88421",
    val mbbsInstitution: String = "All India Institute of Medical Sciences",
    val resumeFile: String = "resume.pdf",
    val photoRes: String = "avatar_1"
)

object VerificationStateStore {
    val hospitalApplications = mutableStateListOf<HospitalApplication>()
    val doctorApplications = mutableStateListOf<DoctorApplication>()

    // Verification status keyed by username/name
    val userVerificationStatus = mutableStateMapOf<String, VerificationStatus>()
    val userRejectionReasons = mutableStateMapOf<String, String>()
    val doctorHospitalSelections = mutableStateMapOf<String, String>()

    init {
        // Seed mock hospital applications
        hospitalApplications.addAll(
            listOf(
                HospitalApplication("h_1", "Apollo Hospital", "H-102938", "LIC-4829", "2026-08-04", VerificationStatus.PENDING),
                HospitalApplication("h_2", "MGM Healthcare", "H-987654", "LIC-1102", "2026-08-05", VerificationStatus.REJECTED, "Invalid registration document scanned. License expiration date is cut off."),
                HospitalApplication("h_3", "MIOT", "H-883921", "LIC-9831", "2026-08-06", VerificationStatus.APPROVED)
            )
        )

        // Seed mock doctor applications
        doctorApplications.addAll(
            listOf(
                DoctorApplication("d_1", "Dr. Jane Smith", "Cardiology", "Apollo Hospital", "8", "MBBS_Degree.pdf", "2026-08-05", VerificationStatus.PENDING),
                DoctorApplication("d_2", "Dr. Robert Lee", "Pediatrics", "Apollo Hospital", "12", "MBBS_Degree.pdf, Pediatrics_Specialty.pdf", "2026-08-05", VerificationStatus.REJECTED, "Degrees/credentials could not be verified with local state council. Please re-upload your registration certificate."),
                DoctorApplication("d_3", "Dr. Emily Davis", "Neurology", "Fortis Hospital", "15", "MBBS_Degree.pdf", "2026-08-06", VerificationStatus.APPROVED)
            )
        )

        // Initialize statuses for default demo accounts so they are ready out-of-the-box
        userVerificationStatus["Apollo Hospital"] = VerificationStatus.PENDING
        userVerificationStatus["Dr. Jane Smith"] = VerificationStatus.PENDING
        doctorHospitalSelections["Dr. Jane Smith"] = "Apollo Hospital"

        userVerificationStatus["MGM Healthcare"] = VerificationStatus.REJECTED
        userRejectionReasons["MGM Healthcare"] = "Invalid registration document scanned. License expiration date is cut off."

        userVerificationStatus["Dr. Robert Lee"] = VerificationStatus.REJECTED
        userRejectionReasons["Dr. Robert Lee"] = "Degrees/credentials could not be verified with local state council. Please re-upload your registration certificate."
        doctorHospitalSelections["Dr. Robert Lee"] = "Apollo Hospital"

        userVerificationStatus["MIOT"] = VerificationStatus.APPROVED
        userVerificationStatus["Dr. Emily Davis"] = VerificationStatus.APPROVED
        doctorHospitalSelections["Dr. Emily Davis"] = "Fortis Hospital"
    }

    fun addHospitalApplication(name: String, regNum: String, license: String) {
        val app = HospitalApplication(
            id = "h_${System.currentTimeMillis()}",
            name = name,
            regNumber = regNum,
            licenseNumber = license,
            submittedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            status = VerificationStatus.PENDING
        )
        hospitalApplications.add(app)
        userVerificationStatus[name] = VerificationStatus.PENDING
        userRejectionReasons.remove(name)
    }

    fun addDoctorApplication(name: String, spec: String, hospital: String, exp: String, filename: String) {
        val app = DoctorApplication(
            id = "d_${System.currentTimeMillis()}",
            name = name,
            specialization = spec,
            hospitalName = hospital,
            experienceYears = exp,
            docsAttached = filename,
            submittedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            status = VerificationStatus.PENDING
        )
        doctorApplications.add(app)
        userVerificationStatus[name] = VerificationStatus.PENDING
        doctorHospitalSelections[name] = hospital
        userRejectionReasons.remove(name)
    }

    fun approveHospital(appId: String) {
        hospitalApplications.find { it.id == appId }?.let {
            it.status = VerificationStatus.APPROVED
            userVerificationStatus[it.name] = VerificationStatus.APPROVED
            userRejectionReasons.remove(it.name)
        }
    }

    fun rejectHospital(appId: String, reason: String) {
        hospitalApplications.find { it.id == appId }?.let {
            it.status = VerificationStatus.REJECTED
            it.rejectionReason = reason
            userVerificationStatus[it.name] = VerificationStatus.REJECTED
            userRejectionReasons[it.name] = reason
        }
    }

    fun approveDoctor(appId: String) {
        doctorApplications.find { it.id == appId }?.let {
            it.status = VerificationStatus.APPROVED
            userVerificationStatus[it.name] = VerificationStatus.APPROVED
            userRejectionReasons.remove(it.name)
        }
    }

    fun rejectDoctor(appId: String, reason: String) {
        doctorApplications.find { it.id == appId }?.let {
            it.status = VerificationStatus.REJECTED
            it.rejectionReason = reason
            userVerificationStatus[it.name] = VerificationStatus.REJECTED
            userRejectionReasons[it.name] = reason
        }
    }

    fun resubmitHospital(name: String) {
        userVerificationStatus[name] = VerificationStatus.PENDING
        userRejectionReasons.remove(name)
        hospitalApplications.find { it.name == name }?.let {
            it.status = VerificationStatus.PENDING
            it.rejectionReason = ""
        }
    }

    fun resubmitDoctor(name: String) {
        userVerificationStatus[name] = VerificationStatus.PENDING
        userRejectionReasons.remove(name)
        doctorApplications.find { it.name == name }?.let {
            it.status = VerificationStatus.PENDING
            it.rejectionReason = ""
        }
    }

    fun requestDocumentsForDoctor(appId: String) {
        doctorApplications.find { it.id == appId }?.let {
            it.status = VerificationStatus.WAITING_FOR_DOCUMENTS
            userVerificationStatus[it.name] = VerificationStatus.WAITING_FOR_DOCUMENTS
            userRejectionReasons.remove(it.name)
        }
    }
}
