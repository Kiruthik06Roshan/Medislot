package com.medislot.app.data.model

data class StaffMember(
    val id: String,
    val name: String,
    val role: String, // "Doctor", "Nurse", "Receptionist", "Lab Technician", "Pharmacist"
    val department: String,
    val room: String,
    val status: String // "On Duty", "Off Duty", "Leave", "Emergency Duty"
)

data class StaffSchedule(
    val id: String,
    val name: String,
    val role: String, // "Doctor", "Nurse", "Receptionist", "Lab Technician", "Pharmacist"
    val department: String,
    val date: String, // "Monday", "Tuesday", etc.
    val shiftType: String, // "Morning", "Afternoon", "Night", "Emergency", "Custom"
    val shiftTime: String, // e.g. "07:00 AM - 01:00 PM"
    val room: String,
    val status: String // "On Duty", "Off Duty", "Leave", "Emergency Duty"
)

data class LeaveRequest(
    val id: String,
    val staffId: String,
    val staffName: String,
    val role: String,
    val department: String,
    val startDate: String, // e.g. "2026-08-10"
    val endDate: String,
    val reason: String,
    var status: String // "Pending", "Approved", "Rejected"
)
