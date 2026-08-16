package com.medislot.app.data.repository

import com.medislot.app.data.local.DatabaseProvider
import com.medislot.app.data.local.entity.LocalPatientProfile
import com.medislot.app.network.*

class PatientRepositoryImpl : PatientRepository {

    private val patientDao = DatabaseProvider.getDatabase().patientDao()

    override suspend fun getProfile(uid: String): Result<PatientProfileResponse> {
        return try {
            val response = RetrofitClient.apiService.getPatientProfile(uid)
            // Cache locally
            patientDao.insertProfile(
                LocalPatientProfile(
                    id = response.id,
                    uid = response.uid,
                    age = response.age,
                    gender = response.gender,
                    contact = response.contact,
                    bloodGroup = response.blood_group,
                    height = response.height,
                    weight = response.weight,
                    bmi = response.bmi,
                    allergies = response.allergies,
                    medications = response.medications,
                    medicalHistory = response.medical_history
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            // Load from Room cache
            val local = patientDao.getProfile(uid)
            if (local != null) {
                Result.success(
                    PatientProfileResponse(
                        id = local.id,
                        uid = local.uid,
                        age = local.age,
                        gender = local.gender,
                        contact = local.contact,
                        blood_group = local.bloodGroup,
                        height = local.height,
                        weight = local.weight,
                        bmi = local.bmi,
                        allergies = local.allergies,
                        medications = local.medications,
                        medical_history = local.medicalHistory
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateProfile(request: PatientProfileRequest): Result<PatientProfileResponse> {
        return try {
            val response = RetrofitClient.apiService.updatePatientProfile(request)
            patientDao.insertProfile(
                LocalPatientProfile(
                    id = response.id,
                    uid = response.uid,
                    age = response.age,
                    gender = response.gender,
                    contact = response.contact,
                    bloodGroup = response.blood_group,
                    height = response.height,
                    weight = response.weight,
                    bmi = response.bmi,
                    allergies = response.allergies,
                    medications = response.medications,
                    medicalHistory = response.medical_history
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            // Cache locally in Room DB
            val savedLocal = PatientProfileResponse(
                id = "pat_" + request.uid.take(8),
                uid = request.uid,
                age = request.age,
                gender = request.gender,
                contact = request.contact,
                blood_group = request.blood_group,
                height = request.height,
                weight = request.weight,
                bmi = request.bmi,
                allergies = request.allergies,
                medications = request.medications,
                medical_history = request.medical_history
            )
            patientDao.insertProfile(
                LocalPatientProfile(
                    id = savedLocal.id,
                    uid = savedLocal.uid,
                    age = savedLocal.age,
                    gender = savedLocal.gender,
                    contact = savedLocal.contact,
                    bloodGroup = savedLocal.blood_group,
                    height = savedLocal.height,
                    weight = savedLocal.weight,
                    bmi = savedLocal.bmi,
                    allergies = savedLocal.allergies,
                    medications = savedLocal.medications,
                    medicalHistory = savedLocal.medical_history
                )
            )
            Result.success(savedLocal)
        }
    }

    override suspend fun getAppointments(patientId: String): Result<List<AppointmentResponse>> {
        if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
            val demoApps = com.medislot.app.data.model.MockData.appointments.mapIndexed { idx, appt ->
                AppointmentResponse(
                    id = appt.id,
                    patient_id = patientId,
                    doctor_id = "doc_$idx",
                    doctor_name = appt.doctorName,
                    department = appt.department,
                    hospital = appt.hospital,
                    date = appt.date,
                    time = appt.time,
                    status = appt.status,
                    queue_number = appt.queueNumber
                )
            }
            return Result.success(demoApps)
        }

        return try {
            val response = RetrofitClient.apiService.getPatientAppointments(patientId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMedicalRecords(patientId: String): Result<List<MedicalRecordResponse>> {
        if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
            val demoRecords = com.medislot.app.data.model.MockData.patientProfile.labReports.mapIndexed { idx, report ->
                MedicalRecordResponse(
                    id = "rec_demo_$idx",
                    patient_id = patientId,
                    title = report.testName,
                    record_type = "Lab Report",
                    date = report.date,
                    file_url = "sample_report.pdf",
                    result_summary = report.result,
                    doctor_id = "doc_1"
                )
            }
            return Result.success(demoRecords)
        }

        return try {
            val response = RetrofitClient.apiService.getPatientMedicalRecords(patientId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMedicalRecord(request: MedicalRecordRequest): Result<MedicalRecordResponse> {
        if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
            val mockResponse = MedicalRecordResponse(
                id = "rec_demo_" + System.currentTimeMillis().hashCode(),
                patient_id = request.patient_id,
                title = request.title,
                record_type = request.record_type,
                date = request.date,
                file_url = request.file_url,
                result_summary = request.result_summary,
                doctor_id = request.doctor_id
            )
            return Result.success(mockResponse)
        }

        return try {
            val response = RetrofitClient.apiService.createMedicalRecord(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
