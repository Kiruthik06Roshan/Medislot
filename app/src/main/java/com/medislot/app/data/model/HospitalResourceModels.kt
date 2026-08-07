package com.medislot.app.data.model

data class BedInventory(
    val totalBeds: Int = 150,
    val occupiedBeds: Int = 98,
    val availableBeds: Int = 52
)

data class ICUBeds(
    val total: Int = 20,
    val occupied: Int = 16,
    val available: Int = 4
)

data class MedicineInventory(
    val medicineName: String,
    val quantity: Int,
    val threshold: Int
)

data class OxygenInventory(
    val totalCylinder: Int = 100,
    val availableCylinder: Int = 22,
    val threshold: Int = 25
)

data class BloodInventory(
    val bloodGroup: String,
    val units: Int
)

data class AmbulanceInventory(
    val available: Int = 5,
    val busy: Int = 3
)

data class EquipmentItem(
    val id: String,
    val name: String,
    val type: String, // "ventilators", "ecg", "ct", "mri", "ultrasound", "xray"
    val status: String // "Available", "Maintenance"
)

data class HospitalAlert(
    val id: String,
    val title: String,
    val message: String,
    val severity: String, // "Critical", "High", "Medium", "Low"
    val timestamp: Long,
    val department: String = "Operations",
    val isResolved: Boolean = false
)

data class HospitalActivityLog(
    val id: String,
    val description: String,
    val timestamp: Long
)

data class HospitalResourceState(
    val beds: BedInventory = BedInventory(),
    val icu: ICUBeds = ICUBeds(),
    val medicines: List<MedicineInventory> = emptyList(),
    val oxygen: OxygenInventory = OxygenInventory(),
    val bloodBank: List<BloodInventory> = emptyList(),
    val ambulances: AmbulanceInventory = AmbulanceInventory(),
    val equipment: List<EquipmentItem> = emptyList(),
    val alerts: List<HospitalAlert> = emptyList(),
    val logs: List<HospitalActivityLog> = emptyList()
)

data class HospitalResourceAnalytics(
    val bedOccupancyPercentage: Float = 0f,
    val icuOccupancyPercentage: Float = 0f,
    val medicineConsumptionCount: Int = 0,
    val bloodConsumptionCount: Int = 0,
    val oxygenConsumptionCount: Int = 0,
    val ambulanceUtilizationPercentage: Float = 0f
)
