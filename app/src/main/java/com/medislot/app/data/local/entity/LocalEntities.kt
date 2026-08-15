package com.medislot.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_patient_profile")
data class LocalPatientProfile(
    @PrimaryKey val id: String,
    val uid: String,
    val age: Int,
    val gender: String,
    val contact: String,
    val bloodGroup: String,
    val height: String,
    val weight: String,
    val bmi: String,
    val allergies: String?,
    val medications: String?,
    val medicalHistory: String?
)

@Entity(tableName = "local_doctor_profile")
data class LocalDoctorProfile(
    @PrimaryKey val id: String,
    val uid: String,
    val name: String,
    val specialization: String,
    val hospitalName: String,
    val rating: Float,
    val experienceYears: Int,
    val fees: String,
    val bio: String?,
    val availability: String,
    val slotTimes: String?,
    val contact: String,
    val status: String,
    val room: String,
    val shift: String,
    val mbbsInstitution: String?,
    val registrationNumber: String?
)

@Entity(tableName = "local_appointment")
data class LocalAppointment(
    @PrimaryKey val id: String,
    val patientId: String,
    val doctorId: String,
    val doctorName: String,
    val department: String,
    val hospital: String,
    val date: String,
    val time: String,
    val status: String,
    val queueNumber: Int
)

@Entity(tableName = "local_staff_schedule")
data class LocalStaffSchedule(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val department: String,
    val date: String,
    val shiftType: String,
    val shiftTime: String,
    val room: String,
    val status: String
)

@Entity(tableName = "local_leave_request")
data class LocalLeaveRequest(
    @PrimaryKey val id: String,
    val staffId: String,
    val staffName: String,
    val role: String,
    val department: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String
)

@Entity(tableName = "local_inventory_item")
data class LocalInventoryItem(
    @PrimaryKey val id: String,
    val name: String,
    val total: Int,
    val available: Int,
    val unit: String,
    val category: String,
    val lastUpdated: String,
    val trend: String,
    val isTrendPositive: Boolean
)

@Entity(tableName = "local_operational_alert")
data class LocalOperationalAlert(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val severity: String,
    val timestamp: String,
    val department: String,
    val isResolved: Boolean
)
