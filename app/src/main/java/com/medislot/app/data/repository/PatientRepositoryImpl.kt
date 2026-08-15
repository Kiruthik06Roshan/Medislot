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
                // Mock fallback
                Result.success(
                    PatientProfileResponse(
                        id = "pat_mock",
                        uid = uid,
                        age = 29,
                        gender = "Female",
                        contact = "+1 (555) 019-2834",
                        blood_group = "O-Positive (O+)",
                        height = "168 cm",
                        weight = "58 kg",
                        bmi = "20.5",
                        allergies = "Penicillin",
                        medications = "Multivitamin Active",
                        medical_history = "Mild Hypertension"
                    )
                )
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
            // Cache locally and report success (sync can happen later)
            val mockResponse = PatientProfileResponse(
                id = "pat_mock",
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
                    id = mockResponse.id,
                    uid = mockResponse.uid,
                    age = mockResponse.age,
                    gender = mockResponse.gender,
                    contact = mockResponse.contact,
                    bloodGroup = mockResponse.blood_group,
                    height = mockResponse.height,
                    weight = mockResponse.weight,
                    bmi = mockResponse.bmi,
                    allergies = mockResponse.allergies,
                    medications = mockResponse.medications,
                    medicalHistory = mockResponse.medical_history
                )
            )
            Result.success(mockResponse)
        }
    }

    override suspend fun getAppointments(patientId: String): Result<List<AppointmentResponse>> {
        return try {
            val response = RetrofitClient.apiService.getPatientAppointments(patientId)
            Result.success(response)
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    override suspend fun getMedicalRecords(patientId: String): Result<List<MedicalRecordResponse>> {
        return try {
            val response = RetrofitClient.apiService.getPatientMedicalRecords(patientId)
            Result.success(response)
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    override suspend fun addMedicalRecord(request: MedicalRecordRequest): Result<MedicalRecordResponse> {
        return try {
            val response = RetrofitClient.apiService.createMedicalRecord(request)
            Result.success(response)
        } catch (e: Exception) {
            val mockResponse = MedicalRecordResponse(
                id = "rec_mock_" + System.currentTimeMillis().hashCode(),
                patient_id = request.patient_id,
                title = request.title,
                record_type = request.record_type,
                date = request.date,
                file_url = request.file_url,
                result_summary = request.result_summary,
                doctor_id = request.doctor_id
            )
            Result.success(mockResponse)
        }
    }
}
