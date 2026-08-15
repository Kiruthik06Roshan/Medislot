package com.medislot.app.data.repository

import com.medislot.app.network.*

interface PatientRepository {
    suspend fun getProfile(uid: String): Result<PatientProfileResponse>
    suspend fun updateProfile(request: PatientProfileRequest): Result<PatientProfileResponse>
    suspend fun getAppointments(patientId: String): Result<List<AppointmentResponse>>
    suspend fun getMedicalRecords(patientId: String): Result<List<MedicalRecordResponse>>
    suspend fun addMedicalRecord(request: MedicalRecordRequest): Result<MedicalRecordResponse>
}
