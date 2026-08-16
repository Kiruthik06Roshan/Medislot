package com.medislot.app.data.repository

import com.medislot.app.data.model.*
import com.medislot.app.network.HospitalResponse
import kotlinx.coroutines.flow.StateFlow

interface HospitalRepository {
    val hospitalProfile: StateFlow<HospitalResponse?>
    val resourceState: StateFlow<HospitalResourceState>
    val resourceAnalytics: StateFlow<HospitalResourceAnalytics>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun admitPatient(): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun dischargePatient(): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun admitToIcu(): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun dischargeFromIcu(): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun dispenseMedicine(medicineName: String): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun useOxygen(): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun issueBlood(bloodGroup: String): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun assignAmbulance(): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun releaseAmbulance(): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun maintainEquipment(equipmentId: String): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun completeEquipmentMaintenance(equipmentId: String): Result<Unit>

    /**
     * TODO:
     * Replace local inventory update
     * with backend REST API.
     */
    suspend fun resolveAlert(alertId: String): Result<Unit>

    // Staff Scheduling Module
    val staffSchedules: StateFlow<List<StaffSchedule>>
    val leaveRequests: StateFlow<List<LeaveRequest>>
    val staffMembers: StateFlow<List<StaffMember>>

    suspend fun assignShift(schedule: StaffSchedule): Result<Unit>
    suspend fun editShift(schedule: StaffSchedule): Result<Unit>
    suspend fun deleteShift(scheduleId: String): Result<Unit>
    suspend fun duplicatePreviousWeek(): Result<Unit>
    suspend fun approveLeave(leaveId: String): Result<Unit>
    suspend fun rejectLeave(leaveId: String): Result<Unit>
    suspend fun addStaffMember(staff: StaffMember): Result<Unit>
    suspend fun refreshData(): Result<Unit>
}
