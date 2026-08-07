package com.medislot.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medislot.app.data.ai.*
import com.medislot.app.data.model.*
import com.medislot.app.data.repository.HospitalRepository
import com.medislot.app.data.repository.HospitalRepositoryProvider
import com.medislot.app.network.GeminiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HospitalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HospitalRepository = HospitalRepositoryProvider.repository
    private val geminiService = GeminiService(application)
    private val geminiRepository: GeminiRepository = GeminiRepositoryImpl(geminiService)

    val resourceState: StateFlow<HospitalResourceState> = repository.resourceState
    val resourceAnalytics: StateFlow<HospitalResourceAnalytics> = repository.resourceAnalytics

    val staffSchedules: StateFlow<List<StaffSchedule>> = repository.staffSchedules
    val leaveRequests: StateFlow<List<LeaveRequest>> = repository.leaveRequests
    val staffMembers: StateFlow<List<StaffMember>> = repository.staffMembers

    private val _aiRecommendationState = MutableStateFlow<AiState<ResourceOptimizationResponse>>(AiState.Idle)
    val aiRecommendationState: StateFlow<AiState<ResourceOptimizationResponse>> = _aiRecommendationState.asStateFlow()

    private val _aiStaffRecommendationState = MutableStateFlow<AiState<StaffAllocationResponse>>(AiState.Idle)
    val aiStaffRecommendationState: StateFlow<AiState<StaffAllocationResponse>> = _aiStaffRecommendationState.asStateFlow()

    private var aiStaffJob: Job? = null

    private var aiJob: Job? = null

    fun admitPatient() {
        viewModelScope.launch {
            repository.admitPatient()
        }
    }

    fun dischargePatient() {
        viewModelScope.launch {
            repository.dischargePatient()
        }
    }

    fun admitToIcu() {
        viewModelScope.launch {
            repository.admitToIcu()
        }
    }

    fun dischargeFromIcu() {
        viewModelScope.launch {
            repository.dischargeFromIcu()
        }
    }

    fun dispenseMedicine(medicineName: String) {
        viewModelScope.launch {
            repository.dispenseMedicine(medicineName)
        }
    }

    fun useOxygen() {
        viewModelScope.launch {
            repository.useOxygen()
        }
    }

    fun issueBlood(bloodGroup: String) {
        viewModelScope.launch {
            repository.issueBlood(bloodGroup)
        }
    }

    fun assignAmbulance() {
        viewModelScope.launch {
            repository.assignAmbulance()
        }
    }

    fun releaseAmbulance() {
        viewModelScope.launch {
            repository.releaseAmbulance()
        }
    }

    fun maintainEquipment(equipmentId: String) {
        viewModelScope.launch {
            repository.maintainEquipment(equipmentId)
        }
    }

    fun completeEquipmentMaintenance(equipmentId: String) {
        viewModelScope.launch {
            repository.completeEquipmentMaintenance(equipmentId)
        }
    }

    fun resolveAlert(alertId: String) {
        viewModelScope.launch {
            repository.resolveAlert(alertId)
        }
    }

    fun fetchAiRecommendations(forceRefresh: Boolean = false) {
        if (aiJob?.isActive == true) return
        
        _aiRecommendationState.value = AiState.Loading

        aiJob = viewModelScope.launch {
            val state = repository.resourceState.value
            val lowMedsString = state.medicines
                .filter { it.quantity < it.threshold }
                .joinToString { "${it.medicineName}(qty:${it.quantity},threshold:${it.threshold})" }
                .ifEmpty { "None" }

            val lowBloodString = state.bloodBank
                .filter { it.units < 5 }
                .joinToString { "${it.bloodGroup}(qty:${it.units})" }
                .ifEmpty { "None" }

            val maintenanceEqString = state.equipment
                .filter { it.status == "Maintenance" }
                .joinToString { it.name }
                .ifEmpty { "None" }

            val stats = """
                ICU Beds: total=${state.icu.total}, occupied=${state.icu.occupied}, available=${state.icu.available}
                General Beds: total=${state.beds.totalBeds}, occupied=${state.beds.occupiedBeds}, available=${state.beds.availableBeds}
                Oxygen Cylinders: total=${state.oxygen.totalCylinder}, available=${state.oxygen.availableCylinder}, threshold=${state.oxygen.threshold}
                Ambulances: available=${state.ambulances.available}, busy=${state.ambulances.busy}
                Medicines Low: $lowMedsString
                Blood Low: $lowBloodString
                Equipment Offline: $maintenanceEqString
            """.trimIndent()

            val result = geminiRepository.optimizeResources(stats, forceRefresh)
            if (result.isSuccess) {
                _aiRecommendationState.value = AiState.Success(result.getOrThrow())
            } else {
                val error = result.exceptionOrNull() ?: Exception("Unknown error")
                if (error is FallbackCacheException) {
                    @Suppress("UNCHECKED_CAST")
                    _aiRecommendationState.value = AiState.Success(
                        data = error.cachedData as ResourceOptimizationResponse,
                        isFallback = true,
                        timestamp = error.timestamp
                    )
                } else if (error is MockFallbackException) {
                    @Suppress("UNCHECKED_CAST")
                    _aiRecommendationState.value = AiState.Success(
                        data = error.mockData as ResourceOptimizationResponse,
                        isMock = true
                    )
                } else {
                    _aiRecommendationState.value = AiState.Failure(error.message ?: "Failed to generate AI suggestions")
                }
            }
        }
    }

    fun assignShift(schedule: StaffSchedule) {
        viewModelScope.launch {
            repository.assignShift(schedule)
        }
    }

    fun editShift(schedule: StaffSchedule) {
        viewModelScope.launch {
            repository.editShift(schedule)
        }
    }

    fun deleteShift(scheduleId: String) {
        viewModelScope.launch {
            repository.deleteShift(scheduleId)
        }
    }

    fun duplicatePreviousWeek() {
        viewModelScope.launch {
            repository.duplicatePreviousWeek()
        }
    }

    fun approveLeave(leaveId: String) {
        viewModelScope.launch {
            repository.approveLeave(leaveId)
        }
    }

    fun rejectLeave(leaveId: String) {
        viewModelScope.launch {
            repository.rejectLeave(leaveId)
        }
    }

    fun fetchStaffRecommendations(forceRefresh: Boolean = false) {
        if (aiStaffJob?.isActive == true) return
        
        _aiStaffRecommendationState.value = AiState.Loading

        aiStaffJob = viewModelScope.launch {
            val schedules = repository.staffSchedules.value
            val docSchedules = schedules.filter { it.role == "Doctor" }.joinToString { "${it.name}(${it.department},${it.date},${it.shiftType})" }
            val nurseSchedules = schedules.filter { it.role == "Nurse" }.joinToString { "${it.name}(${it.department},${it.date},${it.shiftType})" }
            val inputScheds = "Doctors: $docSchedules; Nurses: $nurseSchedules"

            val result = geminiRepository.suggestStaffAllocation("High Load Inpatient/ICU, Low OPD", inputScheds, forceRefresh)
            if (result.isSuccess) {
                _aiStaffRecommendationState.value = AiState.Success(result.getOrThrow())
            } else {
                val error = result.exceptionOrNull() ?: Exception("Unknown error")
                if (error is FallbackCacheException) {
                    @Suppress("UNCHECKED_CAST")
                    _aiStaffRecommendationState.value = AiState.Success(
                        data = error.cachedData as StaffAllocationResponse,
                        isFallback = true,
                        timestamp = error.timestamp
                    )
                } else if (error is MockFallbackException) {
                    @Suppress("UNCHECKED_CAST")
                    _aiStaffRecommendationState.value = AiState.Success(
                        data = error.mockData as StaffAllocationResponse,
                        isMock = true
                    )
                } else {
                    _aiStaffRecommendationState.value = AiState.Failure(error.message ?: "Failed to generate AI staff scheduling suggestions")
                }
            }
        }
    }
}
