package com.medislot.app.data.repository

import com.medislot.app.network.RetrofitClient
import com.medislot.app.network.AppointmentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

class AppointmentRepositoryImpl : AppointmentRepository {

    override suspend fun bookAppointment(doctorId: String, dateTime: Long): Result<String> {
        return try {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateTime))
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dateTime))
            val response = RetrofitClient.apiService.createAppointment(
                AppointmentRequest(
                    patient_id = "pat_demo_id",
                    doctor_id = doctorId,
                    doctor_name = "Dr. Chosen",
                    department = "General Medicine",
                    hospital = "City General Hospital",
                    date = dateStr,
                    time = timeStr
                )
            )
            Result.success(response.id)
        } catch (e: Exception) {
            // Demo fallback: generate a mock appointment id instead of failing
            Result.success("apt_mock_" + UUID.randomUUID().toString().take(6))
        }
    }

    override suspend fun getQueueWaitingTime(appointmentId: String): Flow<Int> = flow {
        // Mock queue wait time simulation (15 to 45 mins)
        emit(25)
    }

    override suspend fun rescheduleAppointment(appointmentId: String, date: String, time: String): Result<Unit> {
        return try {
            RetrofitClient.apiService.rescheduleAppointment(appointmentId, date, time)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
        return try {
            RetrofitClient.apiService.updateAppointmentStatus(appointmentId, "Cancelled")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
