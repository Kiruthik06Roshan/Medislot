package com.medislot.app.data.repository

import com.medislot.app.data.model.HospitalApplication
import com.medislot.app.data.model.DoctorApplication

interface SuperAdminRepository {
    suspend fun getHospitals(): Result<List<HospitalApplication>>
    suspend fun approveHospital(hospId: String): Result<Unit>
    suspend fun rejectHospital(hospId: String, reason: String): Result<Unit>

    suspend fun getDoctors(): Result<List<DoctorApplication>>
    suspend fun approveDoctor(appId: String): Result<Unit>
    suspend fun rejectDoctor(appId: String, reason: String): Result<Unit>
}
