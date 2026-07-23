package com.medislot.app.data.model

data class PatientProfileData(
    val name: String,
    val age: Int,
    val gender: String,
    val email: String,
    val contact: String,
    val bloodGroup: String,
    val height: String,
    val weight: String,
    val bmi: String,
    val allergies: List<String>,
    val medications: List<String>,
    val history: List<String>,
    val labReports: List<LabReport>
)

data class LabReport(
    val testName: String,
    val date: String,
    val result: String,
    val status: String // "Normal", "Critical", "Pending"
)

data class DoctorProfileData(
    val id: String,
    val name: String,
    val department: String,
    val hospital: String,
    val rating: Float,
    val experience: String,
    val fees: String,
    val bio: String,
    val availability: String,
    val slotTimes: List<String>,
    val email: String,
    val contact: String,
    val status: String = "On Duty", // On Duty, With Patient, In Surgery, Off Duty, On Break
    val room: String = "Room 2A",
    val shift: String = "Day Shift (08:00 AM - 04:00 PM)"
)

data class HospitalResource(
    val id: String,
    val name: String,
    val total: Int,
    val available: Int,
    val unit: String,
    val category: String, // "ICU", "Staff", "Equipment", "Gas"
    val lastUpdated: String = "Updated 2 mins ago",
    val trend: String = "+1.5%",
    val isTrendPositive: Boolean = true
)

data class OperationalAlert(
    val id: String,
    val title: String,
    val message: String,
    val severity: String, // Critical, High, Medium, Low
    val timestamp: String,
    val department: String,
    var isResolved: Boolean = false
)

data class AppointmentData(
    val id: String,
    val doctorName: String,
    val department: String,
    val hospital: String,
    val date: String,
    val time: String,
    val status: String, // "Upcoming", "Completed", "Cancelled"
    val queueNumber: Int = 0
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: String, // "Appointment", "Medicine", "Queue", "Emergency"
    val isRead: Boolean = false,
    val priority: String = "NORMAL" // "HIGH", "NORMAL", "LOW"
)

data class HealthMetric(
    val type: String,
    val value: String,
    val unit: String,
    val time: String,
    val status: String // "Normal", "Elevated", "Critical"
)

data class DepartmentUsage(
    val name: String,
    val loadPercentage: Int,
    val patientsWaiting: Int
)

object MockData {
    val patientProfile = PatientProfileData(
        name = "Sarah Connor",
        age = 29,
        gender = "Female",
        email = "sarah.connor@health.com",
        contact = "+1 (555) 019-2834",
        bloodGroup = "O-Positive (O+)",
        height = "168 cm",
        weight = "58 kg",
        bmi = "20.5 (Healthy)",
        allergies = listOf("Penicillin", "Peanuts", "Dust Mites"),
        medications = listOf(
            "Lisinopril 10mg (1x daily)",
            "Multivitamin Active (1x daily)"
        ),
        history = listOf(
            "Mild Hypertension (Diagnosed 2024)",
            "Appendectomy (2019)"
        ),
        labReports = listOf(
            LabReport("Complete Blood Count (CBC)", "May 12, 2026", "Healthy range", "Normal"),
            LabReport("Lipid Panel / Cholesterol", "Apr 28, 2026", "Borderline High LDL", "Normal"),
            LabReport("Thyroid TSH Test", "Jan 15, 2026", "1.8 mIU/L (Optimal)", "Normal")
        )
    )

    val doctors = listOf(
        DoctorProfileData(
            id = "doc_1",
            name = "Dr. John Doe",
            department = "Cardiology",
            hospital = "City General Hospital",
            rating = 4.9f,
            experience = "14 years",
            fees = "$100",
            bio = "Dr. John Doe is a leading specialist in interventional cardiology and preventative heart healthcare.",
            availability = "Monday - Friday",
            slotTimes = listOf("09:00 AM", "10:30 AM", "11:00 AM", "02:30 PM", "04:00 PM"),
            email = "j.doe@cityhospital.org",
            contact = "+1 (555) 123-4567",
            status = "In Surgery",
            room = "Room 4B",
            shift = "Day Shift (08:00 AM - 04:00 PM)"
        ),
        DoctorProfileData(
            id = "doc_2",
            name = "Dr. Helen Cho",
            department = "Neurology",
            hospital = "Metro Health Medical Center",
            rating = 4.8f,
            experience = "10 years",
            fees = "$120",
            bio = "Dr. Cho specializes in neuropathic disorders, migraines, and cognitive therapies.",
            availability = "Tuesday & Thursday",
            slotTimes = listOf("09:30 AM", "10:00 AM", "01:30 PM", "03:00 PM"),
            email = "h.cho@metrohealth.org",
            contact = "+1 (555) 234-5678",
            status = "With Patient",
            room = "Room 2A",
            shift = "Evening Shift (04:00 PM - 12:00 AM)"
        ),
        DoctorProfileData(
            id = "doc_3",
            name = "Dr. Marcus Vance",
            department = "Orthopedics",
            hospital = "City General Hospital",
            rating = 4.7f,
            experience = "12 years",
            fees = "$90",
            bio = "Expert in sports medicine, joint replacements, and post-traumatic rehabilitation.",
            availability = "Monday, Wednesday, Friday",
            slotTimes = listOf("10:00 AM", "11:30 AM", "03:30 PM", "04:30 PM"),
            email = "m.vance@cityhospital.org",
            contact = "+1 (555) 345-6789",
            status = "On Duty",
            room = "Room 105",
            shift = "Day Shift (08:00 AM - 04:00 PM)"
        ),
        DoctorProfileData(
            id = "doc_4",
            name = "Dr. Sarah Jenkins",
            department = "Pediatrics",
            hospital = "Children's Specialized Hospital",
            rating = 5.0f,
            experience = "18 years",
            fees = "$80",
            bio = "Dedicated to comprehensive pediatric care, childhood development milestones, and immunology.",
            availability = "Monday - Thursday",
            slotTimes = listOf("08:30 AM", "09:00 AM", "11:00 AM", "01:00 PM", "02:00 PM"),
            email = "s.jenkins@childrenshealth.org",
            contact = "+1 (555) 456-7890",
            status = "On Break",
            room = "Room 3C",
            shift = "Day Shift (08:00 AM - 04:00 PM)"
        ),
        DoctorProfileData(
            id = "doc_5",
            name = "Dr. Robert Carter",
            department = "Cardiology",
            hospital = "City General Hospital",
            rating = 4.6f,
            experience = "8 years",
            fees = "$95",
            bio = "Dr. Carter is specialized in cardiovascular therapies.",
            availability = "Monday & Wednesday",
            slotTimes = listOf("09:00 AM", "11:00 AM"),
            email = "r.carter@cityhospital.org",
            contact = "+1 (555) 789-0123",
            status = "Off Duty",
            room = "Room 4A",
            shift = "Night Shift (12:00 AM - 08:00 AM)"
        )
    )

    val resources = listOf(
        HospitalResource("res_1", "ICU Beds", 20, 4, "beds", "ICU", "Updated 2 mins ago", "-5%", false),
        HospitalResource("res_2", "Emergency Beds", 40, 12, "beds", "ICU", "Updated 5 mins ago", "+8%", true),
        HospitalResource("res_3", "Oxygen Reserves", 1000, 780, "Liters", "Gas", "Updated 1 min ago", "-12%", false),
        HospitalResource("res_4", "Ventilators", 15, 6, "units", "Equipment", "Updated 10 mins ago", "Stable", true),
        HospitalResource("res_5", "On-Call Nurses", 50, 42, "staff", "Staff", "Updated 15 mins ago", "+4%", true),
        HospitalResource("res_6", "Duty Doctors", 18, 14, "staff", "Staff", "Updated 12 mins ago", "+2%", true)
    )

    val operationalAlerts = listOf(
        OperationalAlert("alert_1", "ICU Occupancy Above Threshold", "ICU occupancy has reached 95% capacity. Immediate discharge or transfer review required.", "Critical", "10 mins ago", "ICU"),
        OperationalAlert("alert_2", "Oxygen Reserve Below Safe Level", "Main oxygen reservoir level dropped below 20% safe reserve.", "Critical", "25 mins ago", "Facilities"),
        OperationalAlert("alert_3", "Ventilator Maintenance Required", "Ventilator UNIT-08 has scheduled routine maintenance due today.", "Medium", "1 hr ago", "Biomedical"),
        OperationalAlert("alert_4", "Emergency Department Overload", "ED waiting room time exceeds 45 minutes due to high trauma inflow.", "High", "2 hrs ago", "Emergency"),
        OperationalAlert("alert_5", "Blood Bank Inventory Low", "O-Negative blood units are below minimum stock level (2 units remaining).", "High", "4 hrs ago", "Lab & Blood Bank"),
        OperationalAlert("alert_6", "Ambulance Arrival Notification", "Trauma ambulance AMB-04 arriving in 5 minutes with 3 critical patients.", "High", "5 hrs ago", "Emergency"),
        OperationalAlert("alert_7", "Staff Shortage Current Shift", "Nurse understaffing reported in Wing B floor 3.", "Medium", "6 hrs ago", "Nursing"),
        OperationalAlert("alert_8", "Network Server Issue", "Electronic Health Record (EHR) sync latency detected in server node 4.", "Low", "8 hrs ago", "IT Department"),
        OperationalAlert("alert_9", "Fire Safety Equipment Due", "Routine inspection of pressure valves for fire suppression in Wing A.", "Low", "12 hrs ago", "Safety"),
        OperationalAlert("alert_10", "Pharmacy Stock Running Low", "Inpatient pharmacy running low on critical antibiotics (Ciprofloxacin).", "Medium", "14 hrs ago", "Pharmacy")
    )

    val appointments = mutableListOf(
        AppointmentData("appt_1", "Dr. John Doe", "Cardiology", "City General Hospital", "Today", "10:30 AM", "Upcoming", 3),
        AppointmentData("appt_2", "Dr. Helen Cho", "Neurology", "Metro Health Medical Center", "July 12, 2026", "02:00 PM", "Completed"),
        AppointmentData("appt_3", "Dr. Marcus Vance", "Orthopedics", "City General Hospital", "June 24, 2026", "11:00 AM", "Cancelled")
    )

    val notifications = listOf(
        NotificationItem("notif_1", "Appointment Reminder", "Your consultation with Dr. John Doe is scheduled for today at 10:30 AM.", "10 mins ago", "Appointment", false, "HIGH"),
        NotificationItem("notif_2", "Medicine Alert", "Time to take Lisinopril 10mg.", "30 mins ago", "Medicine", false, "NORMAL"),
        NotificationItem("notif_3", "Queue Progress Update", "You are now next in queue for Room 4B.", "1 hr ago", "Queue", true, "HIGH"),
        NotificationItem("notif_4", "Oxygen Depletion Risk", "Oxygen cylinder storage level below 25% threshold in Wing C.", "2 hrs ago", "Emergency", false, "HIGH"),
        NotificationItem("notif_5", "Staff Registered", "Registered nurse Chloe Bennett is online.", "4 hrs ago", "Queue", true, "LOW")
    )

    val healthMetrics = listOf(
        HealthMetric("Heart Rate", "74", "BPM", "10 mins ago", "Normal"),
        HealthMetric("Blood Pressure", "118/79", "mmHg", "1 hr ago", "Normal"),
        HealthMetric("Oxygen Level", "98", "%", "2 hrs ago", "Normal"),
        HealthMetric("Body Temp", "98.6", "°F", "4 hrs ago", "Normal"),
        HealthMetric("Blood Sugar", "95", "mg/dL", "Fasting", "Normal")
    )

    val departmentsUsage = listOf(
        DepartmentUsage("Emergency", 92, 14),
        DepartmentUsage("Cardiology", 78, 6),
        DepartmentUsage("ICU", 85, 2),
        DepartmentUsage("Neurology", 60, 4),
        DepartmentUsage("Orthopedics", 50, 3),
        DepartmentUsage("Pediatrics", 45, 1)
    )

    val dailyTips = listOf(
        "Stay Hydrated: Drinking 8 glasses of water daily helps maintain cellular health and circulation.",
        "Heart Health: 30 minutes of moderate cardiovascular exercise reduces heart disease risk by up to 35%.",
        "Deep Sleep: Quality sleep of 7-8 hours assists cellular repair, memory consolidations, and stress recovery.",
        "Salt Intake: Reducing processed foods cuts sodium intake, lowering chronic blood pressure triggers."
    )

    val symptomCheckerList = listOf(
        "Chest Pain", "Shortness of Breath", "Headache", "Fever", "Cough",
        "Dizziness", "Fatigue", "Nausea", "Joint Stiffness", "Sore Throat"
    )

    val symptomDiagnoses = mapOf(
        "Chest Pain" to Pair("Potential Angina or Muscle strain", "Cardiology"),
        "Shortness of Breath" to Pair("Asthma or Mild Bronchitis", "Cardiology"),
        "Headache" to Pair("Migraine or Tension Headache", "Neurology"),
        "Fever" to Pair("Common Viral Infection", "Pediatrics"),
        "Cough" to Pair("Upper Respiratory Tract infection", "Pediatrics"),
        "Dizziness" to Pair("Dehydration or Inner Ear Inflammation", "Neurology"),
        "Joint Stiffness" to Pair("Mild Osteoarthritic symptoms", "Orthopedics")
    )
}
