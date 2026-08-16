package com.medislot.app.data.repository

import com.medislot.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.medislot.app.network.RetrofitClient
import java.util.UUID

class DoctorRecruitmentRepositoryImpl(
    private val hospitalRepository: HospitalRepository = HospitalRepositoryProvider.repository
) : DoctorRecruitmentRepository {

    private val _applications = MutableStateFlow<List<DoctorApplication>>(emptyList())
    override val applications: StateFlow<List<DoctorApplication>> = _applications.asStateFlow()

    init {
        // Sync with initial applications from VerificationStateStore only in Demo mode
        if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
            _applications.value = VerificationStateStore.doctorApplications.toList()
        } else {
            _applications.value = emptyList()
        }
    }

    override suspend fun getApplications(): Result<List<DoctorApplication>> {
        val isDemoMode = com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive
        if (isDemoMode) {
            return Result.success(_applications.value)
        }
        return try {
            syncWithBackend()
            Result.success(_applications.value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncWithBackend() {
        val appResponse = RetrofitClient.apiService.getRecruitmentApplications()
        val mapped = appResponse.map {
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
                rejectionReason = it.rejection_reason ?: ""
            )
        }
        _applications.value = mapped
    }

    override suspend fun approveDoctor(appId: String): Result<Unit> {
        val isDemoMode = com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive
        if (!isDemoMode) {
            return try {
                RetrofitClient.apiService.updateApplicationStatus(appId, "Approved")
                _applications.value = _applications.value.map {
                    if (it.id == appId) it.copy(status = VerificationStatus.APPROVED) else it
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        VerificationStateStore.approveDoctor(appId)
        val app = VerificationStateStore.doctorApplications.find { it.id == appId }
            ?: return Result.failure(Exception("Application not found"))

        // Add doctor to active MockData list
        val newDoctor = DoctorProfileData(
            id = app.id,
            name = app.name,
            department = app.specialization,
            hospital = app.hospitalName,
            rating = 4.8f,
            experience = "${app.experienceYears} years",
            fees = "$100",
            bio = "Dr. ${app.name} is a specialist in ${app.specialization}.",
            availability = "Monday - Friday",
            slotTimes = listOf("09:00 AM", "10:30 AM", "02:00 PM"),
            email = "${app.name.lowercase().replace(" ", "").replace(".", "")}@cityhospital.org",
            contact = "+1 (555) 999-8888",
            status = "On Duty",
            room = "Room ${(100..500).random()}",
            shift = "Day Shift (08:00 AM - 04:00 PM)"
        )

        synchronized(MockData.doctors) {
            if (!MockData.doctors.any { it.id == newDoctor.id || it.name == newDoctor.name }) {
                MockData.doctors.add(newDoctor)
            }
        }

        // Add doctor to staff scheduling list in HospitalRepository
        val newStaff = StaffMember(
            id = app.id,
            name = app.name,
            role = "Doctor",
            department = app.specialization,
            room = newDoctor.room,
            status = "On Duty"
        )
        hospitalRepository.addStaffMember(newStaff)

        // Increment Resource Planning (Duty Doctors count in MockData.resources)
        synchronized(MockData.resources) {
            val docResourceIndex = MockData.resources.indexOfFirst { it.id == "res_6" }
            if (docResourceIndex != -1) {
                val currentRes = MockData.resources[docResourceIndex]
                MockData.resources[docResourceIndex] = currentRes.copy(
                    total = currentRes.total + 1,
                    available = currentRes.available + 1
                )
            }
        }

        // Update local list
        _applications.value = VerificationStateStore.doctorApplications.toList()
        return Result.success(Unit)
    }

    override suspend fun rejectDoctor(appId: String, reason: String): Result<Unit> {
        val isDemoMode = com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive
        if (!isDemoMode) {
            return try {
                RetrofitClient.apiService.updateApplicationStatus(appId, "Rejected", reason)
                _applications.value = _applications.value.map {
                    if (it.id == appId) it.copy(status = VerificationStatus.REJECTED, rejectionReason = reason) else it
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        VerificationStateStore.rejectDoctor(appId, reason)
        _applications.value = VerificationStateStore.doctorApplications.toList()
        return Result.success(Unit)
    }

    override suspend fun requestDocuments(appId: String): Result<Unit> {
        val isDemoMode = com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive
        if (!isDemoMode) {
            return try {
                RetrofitClient.apiService.updateApplicationStatus(appId, "Waiting Documents")
                _applications.value = _applications.value.map {
                    if (it.id == appId) it.copy(status = VerificationStatus.WAITING_FOR_DOCUMENTS) else it
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        VerificationStateStore.requestDocumentsForDoctor(appId)
        _applications.value = VerificationStateStore.doctorApplications.toList()
        return Result.success(Unit)
    }
}

object DoctorRecruitmentRepositoryProvider {
    val repository: DoctorRecruitmentRepository by lazy { DoctorRecruitmentRepositoryImpl() }
}
