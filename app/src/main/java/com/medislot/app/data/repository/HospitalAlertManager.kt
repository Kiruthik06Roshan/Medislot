package com.medislot.app.data.repository

import com.medislot.app.data.model.BedInventory
import com.medislot.app.data.model.ICUBeds
import com.medislot.app.data.model.MedicineInventory
import com.medislot.app.data.model.OxygenInventory
import com.medislot.app.data.model.BloodInventory
import com.medislot.app.data.model.AmbulanceInventory
import com.medislot.app.data.model.EquipmentItem
import com.medislot.app.data.model.HospitalAlert

object HospitalAlertManager {

    fun generateAlerts(
        beds: BedInventory,
        icu: ICUBeds,
        medicines: List<MedicineInventory>,
        oxygen: OxygenInventory,
        bloodBank: List<BloodInventory>,
        ambulances: AmbulanceInventory,
        equipment: List<EquipmentItem>
    ): List<HospitalAlert> {
        val alerts = mutableListOf<HospitalAlert>()
        val now = System.currentTimeMillis()
        
        // ICU Full
        if (icu.available <= 0) {
            alerts.add(
                HospitalAlert(
                    id = "alert_icu_full",
                    title = "ICU Full",
                    message = "ICU beds are at 100% capacity. No available ICU beds remaining.",
                    severity = "Critical",
                    timestamp = now,
                    department = "ICU"
                )
            )
        }
        
        // Low Oxygen
        if (oxygen.availableCylinder < oxygen.threshold) {
            alerts.add(
                HospitalAlert(
                    id = "alert_low_oxygen",
                    title = "Low Oxygen",
                    message = "Oxygen Cylinders (${oxygen.availableCylinder}) are below the threshold of ${oxygen.threshold}.",
                    severity = "Critical",
                    timestamp = now,
                    department = "Facilities"
                )
            )
        }
        
        // Low Medicine
        medicines.forEach { med ->
            if (med.quantity < med.threshold) {
                alerts.add(
                    HospitalAlert(
                        id = "alert_low_med_${med.medicineName}",
                        title = "Low Medicine",
                        message = "Stock of ${med.medicineName} is at ${med.quantity} (Threshold: ${med.threshold}).",
                        severity = "High",
                        timestamp = now,
                        department = "Pharmacy"
                    )
                )
            }
        }
        
        // Low Blood
        bloodBank.forEach { blood ->
            val bloodThreshold = 5
            if (blood.units < bloodThreshold) {
                alerts.add(
                    HospitalAlert(
                        id = "alert_low_blood_${blood.bloodGroup}",
                        title = "Low Blood",
                        message = "Blood group ${blood.bloodGroup} reserves are low: ${blood.units} units remaining.",
                        severity = "High",
                        timestamp = now,
                        department = "Lab & Blood Bank"
                    )
                )
            }
        }
        
        // No Ambulance Available
        if (ambulances.available <= 0) {
            alerts.add(
                HospitalAlert(
                    id = "alert_no_ambulance",
                    title = "No Ambulance Available",
                    message = "All transport vehicles are busy. No emergency ambulances available.",
                    severity = "High",
                    timestamp = now,
                    department = "Logistics"
                )
            )
        }
        
        // Equipment Failure / Maintenance
        equipment.forEach { eq ->
            if (eq.status == "Maintenance") {
                alerts.add(
                    HospitalAlert(
                        id = "alert_eq_maintenance_${eq.id}",
                        title = "Equipment Failure",
                        message = "Equipment '${eq.name}' is offline (status: Maintenance).",
                        severity = "Medium",
                        timestamp = now,
                        department = "Biomedical"
                    )
                )
            }
        }
        
        return alerts
    }
}
