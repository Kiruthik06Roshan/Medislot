package com.medislot.app.data.repository

import com.medislot.app.network.RetrofitClient
import com.medislot.app.network.AppointmentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

class AppointmentRepositoryImpl : AppointmentRepository {

    override suspend fun bookAppointment(doctorId: String, dateTime: Long): Result<String> {
        if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
            return Result.success("apt_demo_" + UUID.randomUUID().toString().take(6))
        }

        return try {
            val authRepo = AuthenticationRepositoryImpl()
            val patientUid = authRepo.getUid() ?: ""
            val docRepo = DoctorRepositoryImpl()
            val docProfile = docRepo.getProfile(doctorId).getOrNull()

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateTime))
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dateTime))

            val response = RetrofitClient.apiService.createAppointment(
                AppointmentRequest(
                    patient_id = patientUid,
                    doctor_id = doctorId,
                    doctor_name = docProfile?.name ?: "Doctor Specialist",
                    department = docProfile?.specialization ?: "General Medicine",
                    hospital = docProfile?.hospital_name ?: "City General Hospital",
                    date = dateStr,
                    time = timeStr
                )
            )
            Result.success(response.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getQueueWaitingTime(appointmentId: String): Flow<Int> = flow {
        // Mock queue wait time simulation (15 to 45 mins)
        emit(25)
    }

    override suspend fun rescheduleAppointment(appointmentId: String, date: String, time: String): Result<Unit> {
        return try {
            if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
                Result.success(Unit)
            } else {
                RetrofitClient.apiService.rescheduleAppointment(appointmentId, date, time)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
        return try {
            if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
                Result.success(Unit)
            } else {
                RetrofitClient.apiService.cancelAppointment(appointmentId)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
