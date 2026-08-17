package com.medislot.app.data.repository

import com.medislot.app.network.*

interface PatientRepository {
    suspend fun getProfile(uid: String): Result<PatientProfileResponse>
    suspend fun updateProfile(request: PatientProfileRequest): Result<PatientProfileResponse>
    suspend fun getAppointments(patientId: String): Result<List<AppointmentResponse>>
    suspend fun getMedicalRecords(patientId: String): Result<List<MedicalRecordResponse>>
    suspend fun addMedicalRecord(request: MedicalRecordRequest): Result<MedicalRecordResponse>

    // Patient Queue System Operations
    suspend fun joinQueue(request: QueueJoinRequest): Result<QueueResponse>
    suspend fun getActiveQueue(patientId: String): Result<QueueResponse>
    suspend fun leaveQueue(queueId: String): Result<Unit>
    suspend fun getDepartmentQueue(hospitalId: String, departmentId: String, doctorId: String? = null): Result<List<PatientQueueInfo>>
    suspend fun updateQueueOrder(request: QueueUpdateList): Result<Unit>
}
