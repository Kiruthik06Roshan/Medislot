package com.medislot.app.data.repository

import com.medislot.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.medislot.app.network.RetrofitClient
import com.medislot.app.network.StaffScheduleRequest
import com.medislot.app.network.HospitalResponse
import java.util.UUID

import kotlinx.coroutines.flow.first

class HospitalRepositoryImpl : HospitalRepository {

    private var medicineConsumption = 0
    private var bloodConsumption = 0
    private var oxygenConsumption = 0

    private val resolvedAlertIds = mutableSetOf<String>()

    private val _hospitalProfile = MutableStateFlow<HospitalResponse?>(null)
    override val hospitalProfile: StateFlow<HospitalResponse?> = _hospitalProfile.asStateFlow()

    private val _resourceState: MutableStateFlow<HospitalResourceState>
    private val _resourceAnalytics: MutableStateFlow<HospitalResourceAnalytics>

    private val _staffSchedules = MutableStateFlow<List<StaffSchedule>>(emptyList())
    override val staffSchedules: StateFlow<List<StaffSchedule>> = _staffSchedules.asStateFlow()

    private val _leaveRequests = MutableStateFlow<List<LeaveRequest>>(emptyList())
    override val leaveRequests: StateFlow<List<LeaveRequest>> = _leaveRequests.asStateFlow()

    private val _staffMembers = MutableStateFlow<List<StaffMember>>(emptyList())
    override val staffMembers: StateFlow<List<StaffMember>> = _staffMembers.asStateFlow()

    override val resourceState: StateFlow<HospitalResourceState>
        get() = _resourceState.asStateFlow()

    override val resourceAnalytics: StateFlow<HospitalResourceAnalytics>
        get() = _resourceAnalytics.asStateFlow()

    init {
        val state = if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
            val initialBeds = BedInventory(totalBeds = 150, occupiedBeds = 98, availableBeds = 52)
            val initialIcu = ICUBeds(total = 20, occupied = 16, available = 4)
            val initialOxygen = OxygenInventory(totalCylinder = 100, availableCylinder = 22, threshold = 25)
            val initialMedicines = listOf(
                MedicineInventory("Paracetamol 500mg", 150, 50),
                MedicineInventory("Amoxicillin 250mg", 80, 30),
                MedicineInventory("Ibuprofen 400mg", 24, 20),
                MedicineInventory("Ciprofloxacin 500mg", 12, 15)
            )
            val initialBlood = listOf(
                BloodInventory("A+", 25),
                BloodInventory("B+", 18),
                BloodInventory("O+", 30),
                BloodInventory("O-", 4)
            )
            val initialAmbulances = AmbulanceInventory(available = 5, busy = 3)
            val initialEquipment = listOf(
                EquipmentItem("eq_1", "Ventilator UNIT-01", "ventilators", "Available"),
                EquipmentItem("eq_2", "Ventilator UNIT-02", "ventilators", "Maintenance"),
                EquipmentItem("eq_3", "ECG Machine A", "ecg", "Available"),
                EquipmentItem("eq_4", "CT Scanner Suite", "ct", "Available"),
                EquipmentItem("eq_5", "MRI Scanner Suite", "mri", "Available"),
                EquipmentItem("eq_6", "Ultrasound Unit B", "ultrasound", "Available"),
                EquipmentItem("eq_7", "X-Ray Machine Room 3", "xray", "Available")
            )
            val initialAlerts = HospitalAlertManager.generateAlerts(
                initialBeds,
                initialIcu,
                initialMedicines,
                initialOxygen,
                initialBlood,
                initialAmbulances,
                initialEquipment
            ).filter { it.id !in resolvedAlertIds }
            val initialLogs = listOf(
                HospitalActivityLog("log_init", "System initialized with baseline inventory.", System.currentTimeMillis())
            )
            HospitalResourceState(
                beds = initialBeds,
                icu = initialIcu,
                medicines = initialMedicines,
                oxygen = initialOxygen,
                bloodBank = initialBlood,
                ambulances = initialAmbulances,
                equipment = initialEquipment,
                alerts = initialAlerts,
                logs = initialLogs
            )
        } else {
            HospitalResourceState(
                beds = BedInventory(0, 0, 0),
                icu = ICUBeds(0, 0, 0),
                medicines = emptyList(),
                oxygen = OxygenInventory(0, 0, 0),
                bloodBank = emptyList(),
                ambulances = AmbulanceInventory(0, 0),
                equipment = emptyList(),
                alerts = emptyList(),
                logs = emptyList()
            )
        }

        _resourceState = MutableStateFlow(state)
        _resourceAnalytics = MutableStateFlow(calculateAnalytics(state))

        if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
            val initialStaff = listOf(
                StaffMember("staff_1", "Dr. John Doe", "Doctor", "Cardiology", "Room 4B", "On Duty"),
                StaffMember("staff_2", "Dr. Helen Cho", "Doctor", "Neurology", "Room 2A", "On Duty"),
                StaffMember("staff_3", "Dr. Marcus Vance", "Doctor", "Orthopedics", "Room 105", "On Duty"),
                StaffMember("staff_4", "Dr. Sarah Jenkins", "Doctor", "Pediatrics", "Room 3C", "On Duty"),
                StaffMember("staff_5", "Dr. Robert Carter", "Doctor", "Cardiology", "Room 4A", "Off Duty"),
                StaffMember("staff_6", "Nurse Chloe Bennett", "Nurse", "Emergency", "ER Wing A", "On Duty"),
                StaffMember("staff_7", "Nurse Sarah Connor", "Nurse", "ICU", "ICU Desk", "On Duty"),
                StaffMember("staff_8", "Nurse Peter Parker", "Nurse", "OPD", "OPD Desk 1", "Leave"),
                StaffMember("staff_9", "Receptionist Alice Johnson", "Receptionist", "Front Desk", "Lobby", "On Duty"),
                StaffMember("staff_10", "Technician Charlie Brown", "Lab Technician", "Pathology Lab", "Lab 1", "On Duty"),
                StaffMember("staff_11", "Pharmacist Eva Green", "Pharmacist", "Inpatient Pharmacy", "Pharmacy Desk", "On Duty")
            )
            _staffMembers.value = initialStaff

            val initialSchedules = listOf(
                StaffSchedule("sch_1", "Dr. John Doe", "Doctor", "Cardiology", "Monday", "Morning", "07:00 AM - 01:00 PM", "Room 4B", "On Duty"),
                StaffSchedule("sch_2", "Dr. Helen Cho", "Doctor", "Neurology", "Monday", "Afternoon", "01:00 PM - 07:00 PM", "Room 2A", "On Duty"),
                StaffSchedule("sch_3", "Nurse Chloe Bennett", "Nurse", "Emergency", "Monday", "Morning", "07:00 AM - 01:00 PM", "ER Wing A", "On Duty"),
                StaffSchedule("sch_4", "Nurse Sarah Connor", "Nurse", "ICU", "Monday", "Night", "07:00 PM - 07:00 AM", "ICU Desk", "On Duty"),
                StaffSchedule("sch_5", "Technician Charlie Brown", "Lab Technician", "Pathology Lab", "Tuesday", "Morning", "07:00 AM - 01:00 PM", "Lab 1", "On Duty"),
                StaffSchedule("sch_6", "Pharmacist Eva Green", "Pharmacist", "Inpatient Pharmacy", "Tuesday", "Morning", "07:00 AM - 01:00 PM", "Pharmacy Desk", "On Duty")
            )
            _staffSchedules.value = initialSchedules

            val initialLeaves = listOf(
                LeaveRequest("lv_1", "staff_8", "Nurse Peter Parker", "Nurse", "OPD", "2026-08-10", "2026-08-12", "Family emergency", "Pending"),
                LeaveRequest("lv_2", "staff_10", "Technician Charlie Brown", "Lab Technician", "Pathology Lab", "2026-08-15", "2026-08-16", "Moving to new apartment", "Pending")
            )
            _leaveRequests.value = initialLeaves
        } else {
            _staffMembers.value = emptyList()
            _staffSchedules.value = emptyList()
            _leaveRequests.value = emptyList()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = com.medislot.app.data.local.DatabaseProvider.getDataStoreManager().uidFlow.first()
                if (uid != null) {
                    val profile = RetrofitClient.apiService.getHospitalProfile(uid)
                    _hospitalProfile.value = profile
                }
            } catch (e: Exception) {
                // ignore profile fetch errors
            }
            try {
                syncWithBackend()
            } catch (e: Exception) {
                // Ignore API sync errors, keep baselines
            }
        }
    }

    private suspend fun syncWithBackend() {
        val isDemo = com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive
        try {
            val invResponse = RetrofitClient.apiService.getHospitalInventory()
            val currentState = _resourceState.value
            val updatedBeds = BedInventory(
                totalBeds = invResponse.find { it.category == "Beds" }?.total ?: (if (isDemo) currentState.beds.totalBeds else 0),
                occupiedBeds = if (isDemo) {
                    (invResponse.find { it.category == "Beds" }?.total ?: 150) - (invResponse.find { it.category == "Beds" }?.available ?: 52)
                } else {
                    val total = invResponse.find { it.category == "Beds" }?.total ?: 0
                    val available = invResponse.find { it.category == "Beds" }?.available ?: 0
                    (total - available).coerceAtLeast(0)
                },
                availableBeds = invResponse.find { it.category == "Beds" }?.available ?: (if (isDemo) currentState.beds.availableBeds else 0)
            )
            val updatedIcu = ICUBeds(
                total = invResponse.find { it.category == "ICU" }?.total ?: (if (isDemo) currentState.icu.total else 0),
                occupied = if (isDemo) {
                    (invResponse.find { it.category == "ICU" }?.total ?: 20) - (invResponse.find { it.category == "ICU" }?.available ?: 4)
                } else {
                    val total = invResponse.find { it.category == "ICU" }?.total ?: 0
                    val available = invResponse.find { it.category == "ICU" }?.available ?: 0
                    (total - available).coerceAtLeast(0)
                },
                available = invResponse.find { it.category == "ICU" }?.available ?: (if (isDemo) currentState.icu.available else 0)
            )
            val updatedOxygen = OxygenInventory(
                totalCylinder = invResponse.find { it.category == "Gas" }?.total ?: (if (isDemo) 100 else 0),
                availableCylinder = invResponse.find { it.category == "Gas" }?.available ?: (if (isDemo) 22 else 0),
                threshold = if (isDemo) 25 else 0
            )

            // Sync medicines, bloodBank, equipment, ambulances
            val updatedMedicines = if (isDemo) currentState.medicines else {
                invResponse.filter { it.category == "Medicine" || it.category == "Medicines" }.map {
                    MedicineInventory(it.name, it.total, it.available)
                }
            }
            
            val updatedBlood = if (isDemo) currentState.bloodBank else {
                invResponse.filter { it.category == "Blood" || it.category == "Blood bank" }.map {
                    BloodInventory(it.name, it.available)
                }
            }

            val ambItem = invResponse.find { it.category == "Ambulance" || it.category == "Ambulances" }
            val updatedAmbulances = if (isDemo) currentState.ambulances else {
                if (ambItem != null) {
                    AmbulanceInventory(available = ambItem.available, busy = ambItem.total - ambItem.available)
                } else {
                    AmbulanceInventory(0, 0)
                }
            }

            val updatedEquipment = if (isDemo) currentState.equipment else {
                invResponse.filter { it.category == "Equipment" }.mapIndexed { index, item ->
                    EquipmentItem(
                        id = item.id.ifEmpty { "eq_$index" },
                        name = item.name,
                        type = item.name.lowercase(),
                        status = if (item.available > 0) "Available" else "Maintenance"
                    )
                }
            }

            val updatedState = currentState.copy(
                beds = updatedBeds,
                icu = updatedIcu,
                oxygen = updatedOxygen,
                medicines = updatedMedicines,
                bloodBank = updatedBlood,
                ambulances = updatedAmbulances,
                equipment = updatedEquipment
            )
            _resourceState.value = updatedState
            _resourceAnalytics.value = calculateAnalytics(updatedState)
        } catch (e: Exception) {
            // Offline fallback
        }

        try {
            val schedResponse = RetrofitClient.apiService.getStaffScheduling()
            if (isDemo) {
                if (schedResponse.isNotEmpty()) {
                    _staffSchedules.value = schedResponse.map {
                        StaffSchedule(
                            id = it.id,
                            name = it.name,
                            role = it.role,
                            department = it.department,
                            date = it.date,
                            shiftType = it.shift_type,
                            shiftTime = it.shift_time,
                            room = it.room,
                            status = it.status
                        )
                    }
                }
            } else {
                _staffSchedules.value = schedResponse.map {
                    StaffSchedule(
                        id = it.id,
                        name = it.name,
                        role = it.role,
                        department = it.department,
                        date = it.date,
                        shiftType = it.shift_type,
                        shiftTime = it.shift_time,
                        room = it.room,
                        status = it.status
                    )
                }
            }
        } catch (e: Exception) {
            // Offline fallback
        }

        try {
            val leavesResponse = RetrofitClient.apiService.getLeaveRequests()
            if (isDemo) {
                if (leavesResponse.isNotEmpty()) {
                    _leaveRequests.value = leavesResponse.map {
                        LeaveRequest(
                            id = it.id,
                            staffId = it.staff_id,
                            staffName = it.staff_name,
                            role = it.role,
                            department = it.department,
                            startDate = it.start_date,
                            endDate = it.end_date,
                            reason = it.reason,
                            status = it.status
                        )
                    }
                }
            } else {
                _leaveRequests.value = leavesResponse.map {
                    LeaveRequest(
                        id = it.id,
                        staffId = it.staff_id,
                        staffName = it.staff_name,
                        role = it.role,
                        department = it.department,
                        startDate = it.start_date,
                        endDate = it.end_date,
                        reason = it.reason,
                        status = it.status
                    )
                }
            }
        } catch (e: Exception) {
            // Offline fallback
        }

        try {
            val staffResponse = RetrofitClient.apiService.getStaffMembers()
            if (isDemo) {
                if (staffResponse.isNotEmpty()) {
                    _staffMembers.value = staffResponse.map {
                        StaffMember(
                            id = it.id,
                            name = it.name,
                            role = it.role,
                            department = it.department,
                            room = it.room,
                            status = it.status
                        )
                    }
                }
            } else {
                _staffMembers.value = staffResponse.map {
                    StaffMember(
                        id = it.id,
                        name = it.name,
                        role = it.role,
                        department = it.department,
                        room = it.room,
                        status = it.status
                    )
                }
            }
        } catch (e: Exception) {
            // Offline fallback
        }

        // Sync alerts
        try {
            val alertsResponse = RetrofitClient.apiService.getHospitalAlerts()
            if (!isDemo) {
                val currentState = _resourceState.value
                val updatedAlerts = alertsResponse.map {
                    HospitalAlert(
                        id = it.id,
                        title = it.title,
                        message = it.message,
                        severity = it.severity,
                        timestamp = System.currentTimeMillis(),
                        department = it.department,
                        isResolved = it.is_resolved
                    )
                }
                _resourceState.value = currentState.copy(alerts = updatedAlerts)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun updateStateAndAnalytics(updater: (HospitalResourceState) -> HospitalResourceState) {
        synchronized(this) {
            val currentState = _resourceState.value
            val newState = updater(currentState)
            val updatedAlerts = HospitalAlertManager.generateAlerts(
                newState.beds,
                newState.icu,
                newState.medicines,
                newState.oxygen,
                newState.bloodBank,
                newState.ambulances,
                newState.equipment
            ).filter { it.id !in resolvedAlertIds }
            val finalState = newState.copy(alerts = updatedAlerts)
            _resourceState.value = finalState
            _resourceAnalytics.value = calculateAnalytics(finalState)
        }
    }

    private fun calculateAnalytics(state: HospitalResourceState): HospitalResourceAnalytics {
        val beds = state.beds
        val icu = state.icu
        val ambulances = state.ambulances

        val bedOccupancy = if (beds.totalBeds > 0) (beds.occupiedBeds.toFloat() / beds.totalBeds) * 100f else 0f
        val icuOccupancy = if (icu.total > 0) (icu.occupied.toFloat() / icu.total) * 100f else 0f
        
        val totalAmbulances = ambulances.available + ambulances.busy
        val ambulanceUtilization = if (totalAmbulances > 0) (ambulances.busy.toFloat() / totalAmbulances) * 100f else 0f

        return HospitalResourceAnalytics(
            bedOccupancyPercentage = bedOccupancy,
            icuOccupancyPercentage = icuOccupancy,
            medicineConsumptionCount = medicineConsumption,
            bloodConsumptionCount = bloodConsumption,
            oxygenConsumptionCount = oxygenConsumption,
            ambulanceUtilizationPercentage = ambulanceUtilization
        )
    }

    private fun createLog(description: String, currentLogs: List<HospitalActivityLog>): List<HospitalActivityLog> {
        val newLog = HospitalActivityLog(
            id = "log_${System.currentTimeMillis()}_${UUID.randomUUID()}",
            description = description,
            timestamp = System.currentTimeMillis()
        )
        return (listOf(newLog) + currentLogs).take(100)
    }

    override suspend fun admitPatient(): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            if (state.beds.availableBeds > 0) {
                val newBeds = state.beds.copy(
                    occupiedBeds = state.beds.occupiedBeds + 1,
                    availableBeds = state.beds.availableBeds - 1
                )
                state.copy(
                    beds = newBeds,
                    logs = createLog("Patient admitted: General ward occupancy increased.", state.logs)
                )
            } else {
                state.copy(
                    logs = createLog("Admission failed: General ward is at full capacity.", state.logs)
                )
            }
        }
        return Result.success(Unit)
    }

    override suspend fun dischargePatient(): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            if (state.beds.occupiedBeds > 0) {
                val newBeds = state.beds.copy(
                    occupiedBeds = state.beds.occupiedBeds - 1,
                    availableBeds = state.beds.availableBeds + 1
                )
                state.copy(
                    beds = newBeds,
                    logs = createLog("Patient discharged: General ward occupancy decreased.", state.logs)
                )
            } else {
                state
            }
        }
        return Result.success(Unit)
    }

    override suspend fun admitToIcu(): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            if (state.icu.available > 0) {
                val newIcu = state.icu.copy(
                    occupied = state.icu.occupied + 1,
                    available = state.icu.available - 1
                )
                state.copy(
                    icu = newIcu,
                    logs = createLog("ICU Admission: Patient admitted to ICU ward.", state.logs)
                )
            } else {
                state.copy(
                    logs = createLog("ICU Admission failed: ICU ward is at full capacity.", state.logs)
                )
            }
        }
        return Result.success(Unit)
    }

    override suspend fun dischargeFromIcu(): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            if (state.icu.occupied > 0) {
                val newIcu = state.icu.copy(
                    occupied = state.icu.occupied - 1,
                    available = state.icu.available + 1
                )
                state.copy(
                    icu = newIcu,
                    logs = createLog("ICU Discharge: Patient discharged from ICU ward.", state.logs)
                )
            } else {
                state
            }
        }
        return Result.success(Unit)
    }

    override suspend fun dispenseMedicine(medicineName: String): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            var dispensed = false
            val newMedicines = state.medicines.map { med ->
                if (med.medicineName.equals(medicineName, ignoreCase = true) && med.quantity > 0) {
                    dispensed = true
                    medicineConsumption++
                    med.copy(quantity = med.quantity - 1)
                } else {
                    med
                }
            }
            if (dispensed) {
                state.copy(
                    medicines = newMedicines,
                    logs = createLog("Medicine dispensed: 1 unit of $medicineName.", state.logs)
                )
            } else {
                state.copy(
                    logs = createLog("Medicine dispensing failed: $medicineName is out of stock.", state.logs)
                )
            }
        }
        return Result.success(Unit)
    }

    override suspend fun useOxygen(): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            if (state.oxygen.availableCylinder > 0) {
                oxygenConsumption++
                val newOxygen = state.oxygen.copy(
                    availableCylinder = state.oxygen.availableCylinder - 1
                )
                state.copy(
                    oxygen = newOxygen,
                    logs = createLog("Oxygen consumed: 1 oxygen cylinder dispatched.", state.logs)
                )
            } else {
                state.copy(
                    logs = createLog("Oxygen consumption failed: Main reserve depleted.", state.logs)
                )
            }
        }
        return Result.success(Unit)
    }

    override suspend fun issueBlood(bloodGroup: String): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            var issued = false
            val newBloodBank = state.bloodBank.map { blood ->
                if (blood.bloodGroup.equals(bloodGroup, ignoreCase = true) && blood.units > 0) {
                    issued = true
                    bloodConsumption++
                    blood.copy(units = blood.units - 1)
                } else {
                    blood
                }
            }
            if (issued) {
                state.copy(
                    bloodBank = newBloodBank,
                    logs = createLog("Blood issued: 1 unit of $bloodGroup.", state.logs)
                )
            } else {
                state.copy(
                    logs = createLog("Blood issue failed: $bloodGroup is out of stock.", state.logs)
                )
            }
        }
        return Result.success(Unit)
    }

    override suspend fun assignAmbulance(): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            if (state.ambulances.available > 0) {
                val newAmbulances = state.ambulances.copy(
                    available = state.ambulances.available - 1,
                    busy = state.ambulances.busy + 1
                )
                state.copy(
                    ambulances = newAmbulances,
                    logs = createLog("Ambulance assigned: Dispatch team on-route.", state.logs)
                )
            } else {
                state.copy(
                    logs = createLog("Ambulance assignment failed: All ambulances busy.", state.logs)
                )
            }
        }
        return Result.success(Unit)
    }

    override suspend fun releaseAmbulance(): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            if (state.ambulances.busy > 0) {
                val newAmbulances = state.ambulances.copy(
                    available = state.ambulances.available + 1,
                    busy = state.ambulances.busy - 1
                )
                state.copy(
                    ambulances = newAmbulances,
                    logs = createLog("Ambulance released: Vehicle returned to base.", state.logs)
                )
            } else {
                state
            }
        }
        return Result.success(Unit)
    }

    override suspend fun maintainEquipment(equipmentId: String): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            var updatedName = ""
            val newEquipment = state.equipment.map { eq ->
                if (eq.id == equipmentId) {
                    updatedName = eq.name
                    eq.copy(status = "Maintenance")
                } else {
                    eq
                }
            }
            if (updatedName.isNotEmpty()) {
                state.copy(
                    equipment = newEquipment,
                    logs = createLog("Equipment maintenance: $updatedName sent to bio-med team.", state.logs)
                )
            } else {
                state
            }
        }
        return Result.success(Unit)
    }

    override suspend fun completeEquipmentMaintenance(equipmentId: String): Result<Unit> {
        /**
         * TODO:
         * Replace local inventory update
         * with backend REST API.
         */
        updateStateAndAnalytics { state ->
            var updatedName = ""
            val newEquipment = state.equipment.map { eq ->
                if (eq.id == equipmentId) {
                    updatedName = eq.name
                    eq.copy(status = "Available")
                } else {
                    eq
                }
            }
            if (updatedName.isNotEmpty()) {
                state.copy(
                    equipment = newEquipment,
                    logs = createLog("Maintenance completed: $updatedName cleared for operation.", state.logs)
                )
            } else {
                state
            }
        }
        return Result.success(Unit)
    }

    override suspend fun resolveAlert(alertId: String): Result<Unit> {
        try {
            RetrofitClient.apiService.resolveAlert(alertId)
        } catch (e: Exception) {
            // Fail silent fallback
        }
        synchronized(this) {
            resolvedAlertIds.add(alertId)
            val currentState = _resourceState.value
            val updatedAlerts = currentState.alerts.filter { it.id != alertId }
            _resourceState.value = currentState.copy(alerts = updatedAlerts)
        }
        return Result.success(Unit)
    }

    override suspend fun assignShift(schedule: StaffSchedule): Result<Unit> {
        val isDemo = com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive
        if (!isDemo) {
            try {
                val response = RetrofitClient.apiService.assignStaffShift(
                    StaffScheduleRequest(
                        name = schedule.name,
                        role = schedule.role,
                        department = schedule.department,
                        date = schedule.date,
                        shift_type = schedule.shiftType,
                        shift_time = schedule.shiftTime,
                        room = schedule.room,
                        status = schedule.status
                    )
                )
                val newSchedule = StaffSchedule(
                    id = response.id,
                    name = response.name,
                    role = response.role,
                    department = response.department,
                    date = response.date,
                    shiftType = response.shift_type,
                    shiftTime = response.shift_time,
                    room = response.room,
                    status = response.status
                )
                val current = _staffSchedules.value.toMutableList()
                current.add(newSchedule)
                _staffSchedules.value = current
                return Result.success(Unit)
            } catch (e: Exception) {
                return Result.failure(e)
            }
        } else {
            val current = _staffSchedules.value.toMutableList()
            current.add(schedule)
            _staffSchedules.value = current
            return Result.success(Unit)
        }
    }

    override suspend fun editShift(schedule: StaffSchedule): Result<Unit> {
        val isDemo = com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive
        if (!isDemo) {
            try {
                RetrofitClient.apiService.editStaffShift(
                    schedule.id,
                    StaffScheduleRequest(
                        name = schedule.name,
                        role = schedule.role,
                        department = schedule.department,
                        date = schedule.date,
                        shift_type = schedule.shiftType,
                        shift_time = schedule.shiftTime,
                        room = schedule.room,
                        status = schedule.status
                    )
                )
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
        val current = _staffSchedules.value.map {
            if (it.id == schedule.id) schedule else it
        }
        _staffSchedules.value = current
        return Result.success(Unit)
    }

    override suspend fun deleteShift(scheduleId: String): Result<Unit> {
        val isDemo = com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive
        if (!isDemo) {
            try {
                RetrofitClient.apiService.deleteStaffShift(scheduleId)
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
        val current = _staffSchedules.value.filter { it.id != scheduleId }
        _staffSchedules.value = current
        return Result.success(Unit)
    }

    override suspend fun duplicatePreviousWeek(): Result<Unit> {
        val isDemo = com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive
        if (!isDemo) {
            try {
                RetrofitClient.apiService.duplicateScheduling()
                syncWithBackend()
                return Result.success(Unit)
            } catch (e: Exception) {
                return Result.failure(e)
            }
        } else {
            val duplicated = _staffSchedules.value.map {
                it.copy(id = "sch_dup_${System.currentTimeMillis()}_${UUID.randomUUID()}")
            }
            val current = _staffSchedules.value.toMutableList()
            current.addAll(duplicated)
            _staffSchedules.value = current
            return Result.success(Unit)
        }
    }

    override suspend fun approveLeave(leaveId: String): Result<Unit> {
        try {
            RetrofitClient.apiService.updateLeaveStatus(leaveId, "Approved")
        } catch (e: Exception) {
            // Fail silent fallback
        }
        val leaves = _leaveRequests.value.map {
            if (it.id == leaveId) {
                val updated = it.copy(status = "Approved")
                // Update staff member status to "Leave"
                val staffList = _staffMembers.value.map { sm ->
                    if (sm.id == it.staffId) sm.copy(status = "Leave") else sm
                }
                _staffMembers.value = staffList
                
                // Set staff schedules status for approved leave
                val schedList = _staffSchedules.value.map { ss ->
                    if (ss.name == it.staffName) ss.copy(status = "Leave") else ss
                }
                _staffSchedules.value = schedList
                updated
            } else {
                it
            }
        }
        _leaveRequests.value = leaves
        return Result.success(Unit)
    }

    override suspend fun rejectLeave(leaveId: String): Result<Unit> {
        try {
            RetrofitClient.apiService.updateLeaveStatus(leaveId, "Rejected")
        } catch (e: Exception) {
            // Fail silent fallback
        }
        val leaves = _leaveRequests.value.map {
            if (it.id == leaveId) {
                it.copy(status = "Rejected")
            } else {
                it
            }
        }
        _leaveRequests.value = leaves
        return Result.success(Unit)
    }

    override suspend fun addStaffMember(staff: StaffMember): Result<Unit> {
        val current = _staffMembers.value.toMutableList()
        if (!current.any { it.id == staff.id || it.name == staff.name }) {
            current.add(staff)
            _staffMembers.value = current
        }
        return Result.success(Unit)
    }

    override suspend fun refreshData(): Result<Unit> {
        return try {
            syncWithBackend()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

object HospitalRepositoryProvider {
    val repository: HospitalRepository by lazy { HospitalRepositoryImpl() }
}
