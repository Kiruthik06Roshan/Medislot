package com.medislot.app.data.local.dao

import androidx.room.*
import com.medislot.app.data.local.entity.*

@Dao
interface PatientDao {
    @Query("SELECT * FROM local_patient_profile WHERE uid = :uid LIMIT 1")
    suspend fun getProfile(uid: String): LocalPatientProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: LocalPatientProfile)
}

@Dao
interface DoctorDao {
    @Query("SELECT * FROM local_doctor_profile")
    suspend fun getAllDoctors(): List<LocalDoctorProfile>

    @Query("SELECT * FROM local_doctor_profile WHERE uid = :uid LIMIT 1")
    suspend fun getDoctorByUid(uid: String): LocalDoctorProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<LocalDoctorProfile>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: LocalDoctorProfile)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM local_appointment WHERE patientId = :patientId")
    suspend fun getPatientAppointments(patientId: String): List<LocalAppointment>

    @Query("SELECT * FROM local_appointment WHERE doctorId = :doctorId")
    suspend fun getDoctorAppointments(doctorId: String): List<LocalAppointment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<LocalAppointment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: LocalAppointment)
}

@Dao
interface StaffScheduleDao {
    @Query("SELECT * FROM local_staff_schedule")
    suspend fun getSchedules(): List<LocalStaffSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<LocalStaffSchedule>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: LocalStaffSchedule)

    @Query("DELETE FROM local_staff_schedule WHERE id = :id")
    suspend fun deleteSchedule(id: String)
}

@Dao
interface LeaveRequestDao {
    @Query("SELECT * FROM local_leave_request")
    suspend fun getLeaveRequests(): List<LocalLeaveRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequests(leaves: List<LocalLeaveRequest>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(leave: LocalLeaveRequest)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM local_inventory_item")
    suspend fun getInventory(): List<LocalInventoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(items: List<LocalInventoryItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LocalInventoryItem)
}

@Dao
interface OperationalAlertDao {
    @Query("SELECT * FROM local_operational_alert WHERE isResolved = 0")
    suspend fun getActiveAlerts(): List<LocalOperationalAlert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<LocalOperationalAlert>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: LocalOperationalAlert)
}
