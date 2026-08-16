package com.medislot.app.data.repository

import com.medislot.app.data.model.DoctorApplication
import kotlinx.coroutines.flow.StateFlow

interface DoctorRecruitmentRepository {
    val applications: StateFlow<List<DoctorApplication>>

    suspend fun getApplications(): Result<List<DoctorApplication>>
    suspend fun approveDoctor(appId: String): Result<Unit>
    suspend fun rejectDoctor(appId: String, reason: String): Result<Unit>
    suspend fun requestDocuments(appId: String): Result<Unit>
}
