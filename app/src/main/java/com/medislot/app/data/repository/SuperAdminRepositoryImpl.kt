package com.medislot.app.data.repository

import com.medislot.app.data.model.HospitalApplication
import com.medislot.app.data.model.DoctorApplication
import com.medislot.app.data.model.VerificationStatus
import com.medislot.app.network.RetrofitClient
import com.medislot.app.network.ApiService

class SuperAdminRepositoryImpl(
    private val apiService: ApiService = RetrofitClient.apiService
) : SuperAdminRepository {

    override suspend fun getHospitals(): Result<List<HospitalApplication>> {
        return try {
            val response = apiService.getAllHospitals()
            val mapped = response.map {
                HospitalApplication(
                    id = it.id,
                    name = it.name,
                    regNumber = it.registration_number,
                    licenseNumber = it.license_number,
                    submittedDate = "2026-08-07",
                    status = when (it.status.lowercase()) {
                        "approved" -> VerificationStatus.APPROVED
                        "rejected" -> VerificationStatus.REJECTED
                        "waiting documents", "waiting_for_documents" -> VerificationStatus.WAITING_FOR_DOCUMENTS
                        else -> VerificationStatus.PENDING
                    },
                    rejectionReason = it.rejection_reason ?: "",
                    adminName = it.admin_name ?: "",
                    contact = it.contact ?: "",
                    docsAttached = it.docs_attached ?: ""
                )
            }
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveHospital(hospId: String): Result<Unit> {
        return try {
            apiService.updateHospitalStatus(hospId, "Approved")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectHospital(hospId: String, reason: String): Result<Unit> {
        return try {
            apiService.updateHospitalStatus(hospId, "Rejected", reason)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDoctors(): Result<List<DoctorApplication>> {
        return try {
            val response = apiService.getRecruitmentApplications()
            val mapped = response.map {
                DoctorApplication(
                    id = it.id,
                    name = it.name,
                    specialization = it.specialization,
                    hospitalName = it.selected_hospital,
                    experienceYears = it.experience_years,
                    docsAttached = it.docs_attached ?: "",
                    submittedDate = "2026-08-07",
                    status = when (it.status) {
                        "Approved" -> VerificationStatus.APPROVED
                        "Rejected" -> VerificationStatus.REJECTED
                        "Waiting Documents" -> VerificationStatus.WAITING_FOR_DOCUMENTS
                        else -> VerificationStatus.PENDING
                    },
                    rejectionReason = it.rejection_reason ?: "",
                    medicalRegistrationNumber = it.medical_registration_number,
                    mbbsInstitution = it.mbbs_institution,
                    resumeFile = it.resume_file ?: "resume.pdf"
                )
            }
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveDoctor(appId: String): Result<Unit> {
        return try {
            apiService.updateApplicationStatus(appId, "Approved")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectDoctor(appId: String, reason: String): Result<Unit> {
        return try {
            apiService.updateApplicationStatus(appId, "Rejected", reason)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

object SuperAdminRepositoryProvider {
    val repository: SuperAdminRepository by lazy { SuperAdminRepositoryImpl() }
}
