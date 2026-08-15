package com.medislot.app.data.repository

import com.medislot.app.network.*

interface DoctorRepository {
    suspend fun getProfile(uid: String): Result<DoctorProfileResponse>
    suspend fun updateProfile(request: DoctorProfileRequest): Result<DoctorProfileResponse>
    suspend fun getAppointments(doctorId: String): Result<List<AppointmentResponse>>
    suspend fun getAllDoctors(): Result<List<DoctorProfileResponse>>
}
