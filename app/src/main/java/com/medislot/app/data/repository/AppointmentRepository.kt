package com.medislot.app.data.repository

import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    suspend fun bookAppointment(doctorId: String, dateTime: Long): Result<String>
    suspend fun getQueueWaitingTime(appointmentId: String): Flow<Int>
    suspend fun rescheduleAppointment(appointmentId: String, date: String, time: String): Result<Unit>
    suspend fun cancelAppointment(appointmentId: String): Result<Unit>
}
