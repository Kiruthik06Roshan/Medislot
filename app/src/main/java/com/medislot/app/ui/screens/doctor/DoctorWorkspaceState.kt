package com.medislot.app.ui.screens.doctor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.medislot.app.data.model.LabReport
import com.medislot.app.data.model.NotificationItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DoctorDutyStatus(val displayName: String) {
    AVAILABLE("Available"),
    BUSY("Busy"),
    BREAK("Break"),
    OFFLINE("Offline")
}

data class PrescriptionItem(
    val id: String,
    val medicineName: String,
    val dosage: String,
    val morning: Boolean,
    val afternoon: Boolean,
    val night: Boolean,
    val duration: String,
    val instructions: String
)

data class PatientVitals(
    var bloodPressure: String = "120/80",
    var heartRate: Int = 72,
    var temperature: Float = 98.6f,
    var oxygenSaturation: Int = 98,
    var respiratoryRate: Int = 16,
    var heightCm: Float = 170f,
    var weightKg: Float = 70f,
    var bmi: Float = 24.2f,
    var painScale: Int = 2
) {
    fun calculateBMI() {
        val heightM = heightCm / 100f
        if (heightM > 0) {
            bmi = Math.round((weightKg / (heightM * heightM)) * 10f) / 10f
        }
    }
}

data class PatientDiagnosis(
    var chiefComplaint: String = "",
    var symptoms: String = "",
    var primaryDiagnosis: String = "",
    var secondaryDiagnosis: String = "",
    var clinicalImpression: String = "",
    var doctorNotes: String = ""
)

data class PatientClinicalNotes(
    var observation: String = "",
    var clinicalNotes: String = "",
    var treatmentPlan: String = "",
    var recommendations: String = "",
    var lifestyleAdvice: String = "",
    var referralNotes: String = ""
)

data class LabOrderItem(
    val id: String,
    val testName: String,
    val priority: String = "Routine", // "Routine", "Urgent", "STAT"
    val clinicalReason: String = ""
)

data class PatientRecord(
    val id: String,
    val name: String,
    val queueNumber: Int,
    val appointmentTime: String,
    val age: Int,
    val gender: String,
    val bloodGroup: String,
    val height: String,
    val weight: String,
    val bmi: String,
    val allergies: List<String>,
    val medications: List<String>,
    val history: List<String>,
    val previousVisits: List<String>,
    val uploadedReports: List<LabReport>,
    val emergencyContact: String,
    val symptoms: String,
    val priority: String, // "Emergency", "High", "Normal"
    var status: String, // "Waiting", "Checked In", "In Consultation", "Completed", "Cancelled"
    var estimatedWaitMinutes: Int = 0,
    
    // Expanded consultation data
    var vitals: PatientVitals = PatientVitals(),
    var diagnosis: PatientDiagnosis = PatientDiagnosis(),
    var clinicalNotes: PatientClinicalNotes = PatientClinicalNotes(),
    var prescriptions: List<PrescriptionItem> = emptyList(),
    var labOrders: List<LabOrderItem> = emptyList(),
    var followUpDuration: String = "None",
    var followUpNotes: String = "",
    var actualDurationMinutes: Int = 0
)

data class DoctorProfileInfo(
    val name: String = "Dr. John Doe",
    val specialization: String = "Cardiology",
    val qualification: String = "MD, FACC, Board Certified Cardiologist",
    val experience: String = "14 Years",
    val hospital: String = "City General Hospital",
    val licenseNumber: String = "LIC-99283-CARD",
    val dutyHours: String = "09:00 AM - 05:00 PM",
    val patientsToday: Int = 8,
    val averageConsultationTime: Int = 12,
    
    // Expanded fields
    val department: String = "Department of Cardiovascular Sciences",
    val languages: String = "English, Spanish, French",
    val hospitalEmail: String = "j.doe@cityhospital.org",
    val contactNumber: String = "+1 (555) 123-4567",
    val certifications: String = "Board Certified in Interventional Cardiology, Member of AHA",
    val achievements: String = "Distinguished Clinical Cardiologist Award 2025",
    val patientsTreated: Int = 3450,
    val successRate: Int = 98,
    val researchPublications: String = "Prev. Card. Care Volume 12, Mechanics of Blood Flow in Arteries",
    val professionalMemberships: String = "Fellow of the American College of Cardiology (FACC), American Medical Association"
)

data class PastConsultation(
    val id: String,
    val patientName: String,
    val age: Int,
    val gender: String,
    val diagnosis: String,
    val durationMinutes: Int,
    val prescriptionCount: Int,
    val labOrdersCount: Int,
    val followUp: String,
    val status: String,
    val dateCategory: String // "Today", "Yesterday", "Last Week", "Last Month"
)

object DoctorWorkspaceState {
    var dutyStatus by mutableStateOf(DoctorDutyStatus.AVAILABLE)
    var roomNumber by mutableStateOf("Room 4B")
    
    val todayDate: String
        get() = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(Date())

    var doctorProfile by mutableStateOf(DoctorProfileInfo())

    // Live Patient Queue
    val appointments = mutableStateListOf<PatientRecord>()
    
    // Historical consultations
    val consultationHistory = mutableStateListOf<PastConsultation>()
    
    // Notifications list
    val notifications = mutableStateListOf<NotificationItem>()
    
    // Active Consultation states
    var activePatientId by mutableStateOf<String?>(null)
    var consultationTimerSeconds by mutableStateOf(0L)
    var isTimerRunning by mutableStateOf(false)
    
    // Break Timer ticking
    var breakTimerSeconds by mutableStateOf(0L)
    
    // Current input buffers during consultation
    var currentVitals by mutableStateOf(PatientVitals())
    var currentDiagnosis by mutableStateOf(PatientDiagnosis())
    var currentClinicalNotes by mutableStateOf(PatientClinicalNotes())
    val currentPrescriptions = mutableStateListOf<PrescriptionItem>()
    val currentLabOrders = mutableStateListOf<LabOrderItem>()
    var currentFollowUpDuration by mutableStateOf("None")
    var currentFollowUpNotes by mutableStateOf("")
    
    init {
        resetState()
    }
    
    fun resetState() {
        appointments.clear()
        appointments.addAll(listOf(
            PatientRecord(
                id = "pat_1",
                name = "Sarah Connor",
                queueNumber = 3,
                appointmentTime = "10:30 AM",
                age = 29,
                gender = "Female",
                bloodGroup = "O-Positive (O+)",
                height = "168 cm",
                weight = "58 kg",
                bmi = "20.5 (Healthy)",
                allergies = listOf("Penicillin", "Peanuts", "Dust Mites"),
                medications = listOf("Lisinopril 10mg (1x daily)", "Multivitamin Active (1x daily)"),
                history = listOf("Mild Hypertension (Diagnosed 2024)", "Appendectomy (2019)"),
                previousVisits = listOf("Routine Cardiovascular Checkup (Apr 28, 2026)", "Follow-up BP review (May 12, 2026)"),
                uploadedReports = listOf(
                    LabReport("Complete Blood Count (CBC)", "May 12, 2026", "Healthy range", "Normal"),
                    LabReport("Lipid Panel / Cholesterol", "Apr 28, 2026", "Borderline High LDL", "Normal"),
                    LabReport("Thyroid TSH Test", "Jan 15, 2026", "1.8 mIU/L (Optimal)", "Normal")
                ),
                emergencyContact = "John Connor (+1 555-019-2834)",
                symptoms = "Mild chest discomfort & fatigue during exertion",
                priority = "High",
                status = "Checked In",
                vitals = PatientVitals(
                    bloodPressure = "135/85",
                    heartRate = 82,
                    temperature = 98.9f,
                    oxygenSaturation = 97,
                    respiratoryRate = 18,
                    heightCm = 168f,
                    weightKg = 58f,
                    bmi = 20.5f,
                    painScale = 3
                ),
                diagnosis = PatientDiagnosis(
                    chiefComplaint = "Substernal chest pressure radiating to left arm on exertion.",
                    symptoms = "Mild chest discomfort & fatigue during exertion",
                    primaryDiagnosis = "Stable Angina Pectoris",
                    secondaryDiagnosis = "Essential Hypertension",
                    clinicalImpression = "Suspected Coronary Artery Disease",
                    doctorNotes = "Symptoms occur with exertion and are relieved by rest. Advise immediate lipid panel check and stress Echo."
                ),
                clinicalNotes = PatientClinicalNotes(
                    observation = "S4 heart sound present on auscultation. Lung fields clear. BP elevated.",
                    clinicalNotes = "Correlate clinical presentation with upcoming stress test results.",
                    treatmentPlan = "Prescribe Nitroglycerin sublingual as needed. Initiate Atorvastatin at bedtime.",
                    recommendations = "Refer to outpatient cardiology for treadmill test.",
                    lifestyleAdvice = "Low sodium, low fat diet. Moderate exercise as tolerated.",
                    referralNotes = "Dr. John Doe (Cardiology Wing B)"
                ),
                prescriptions = listOf(
                    PrescriptionItem("med_1", "Nitroglycerin", "0.4 mg", true, false, false, "30 days", "Sublingual, as needed for chest pain"),
                    PrescriptionItem("med_2", "Atorvastatin", "20 mg", false, false, true, "90 days", "Take at bedtime")
                ),
                labOrders = listOf(
                    LabOrderItem("lab_1", "ECG", "STAT", "Exclusion of acute coronary syndrome"),
                    LabOrderItem("lab_2", "Lipid Panel", "Urgent", "Monitor LDL/HDL cholesterol")
                )
            ),
            PatientRecord(
                id = "pat_2",
                name = "Arthur Dent",
                queueNumber = 4,
                appointmentTime = "11:00 AM",
                age = 42,
                gender = "Male",
                bloodGroup = "A-Negative (A-)",
                height = "180 cm",
                weight = "72 kg",
                bmi = "22.2 (Healthy)",
                allergies = emptyList(),
                medications = emptyList(),
                history = listOf("Mild Anxiety (Diagnosed 2023)"),
                previousVisits = listOf("Annual General Health Assessment (Jan 2026)"),
                uploadedReports = listOf(
                    LabReport("Spine X-Ray", "Jun 15, 2026", "Normal vertebrae alignment", "Normal")
                ),
                emergencyContact = "Ford Prefect (+1 555-424-2424)",
                symptoms = "Chronic mild lower back pain & stress",
                priority = "Normal",
                status = "Waiting",
                vitals = PatientVitals(
                    bloodPressure = "118/76",
                    heartRate = 68,
                    temperature = 98.4f,
                    oxygenSaturation = 99,
                    respiratoryRate = 14,
                    heightCm = 180f,
                    weightKg = 72f,
                    bmi = 22.2f,
                    painScale = 4
                ),
                diagnosis = PatientDiagnosis(
                    chiefComplaint = "Chronic mild lower back pain & stress due to posture.",
                    symptoms = "Lower back stiffness, muscle fatigue.",
                    primaryDiagnosis = "Lumbar Muscle Strain",
                    secondaryDiagnosis = "Generalized Anxiety Disorder",
                    clinicalImpression = "Posture-related musculoskeletal tension",
                    doctorNotes = "Discomfort is chronic but non-radicular. No signs of disc herniation."
                ),
                clinicalNotes = PatientClinicalNotes(
                    observation = "Mild tenderness on palpation of lumbar paraspinal muscles. Reflexes normal.",
                    clinicalNotes = "Encourage core muscle strengthening and physical therapy.",
                    treatmentPlan = "Paracetamol 650mg as needed, heat pack therapy.",
                    recommendations = "Refer to physiotherapy for postural rehabilitation.",
                    lifestyleAdvice = "Take active desk breaks every 45 mins. Dynamic stretching.",
                    referralNotes = "None"
                ),
                prescriptions = listOf(
                    PrescriptionItem("med_3", "Paracetamol", "650 mg", true, false, true, "10 days", "Take after meals for pain relief")
                ),
                labOrders = listOf(
                    LabOrderItem("lab_3", "Spine X-Ray", "Routine", "Exclude mechanical skeletal defects")
                )
            ),
            PatientRecord(
                id = "pat_3",
                name = "Ellen Ripley",
                queueNumber = 5,
                appointmentTime = "11:30 AM",
                age = 35,
                gender = "Female",
                bloodGroup = "B-Positive (B+)",
                height = "172 cm",
                weight = "63 kg",
                bmi = "21.3 (Healthy)",
                allergies = listOf("Sulfa Drugs", "Aspirin"),
                medications = listOf("Albuterol Inhaler (As needed)"),
                history = listOf("Chronic Bronchitis", "Post-Surgery Recovery"),
                previousVisits = listOf("ER Respiratory Intake (Jun 2, 2026)", "Post-Op Clearance (Jul 1, 2026)"),
                uploadedReports = listOf(
                    LabReport("Pulmonary Function Test", "Jun 10, 2026", "82% lung capacity (Borderline)", "Normal"),
                    LabReport("Chest X-Ray", "Jun 02, 2026", "Slight bronchial thickening", "Normal")
                ),
                emergencyContact = "Corporal Hicks (+1 555-882-2938)",
                symptoms = "Severe congestion, short of breath, wheezing",
                priority = "Emergency",
                status = "Waiting",
                vitals = PatientVitals(
                    bloodPressure = "128/84",
                    heartRate = 88,
                    temperature = 101.2f,
                    oxygenSaturation = 94,
                    respiratoryRate = 22,
                    heightCm = 172f,
                    weightKg = 63f,
                    bmi = 21.3f,
                    painScale = 5
                ),
                diagnosis = PatientDiagnosis(
                    chiefComplaint = "Severe congestion, short of breath & wheezing, productive cough.",
                    symptoms = "Wheezing, dyspnea, fever, chest congestion.",
                    primaryDiagnosis = "Acute Bronchitis with Bronchospasm",
                    secondaryDiagnosis = "Mild Persistent Asthma",
                    clinicalImpression = "Exacerbated lower respiratory tract infection",
                    doctorNotes = "Urgent consultation required due to oxygen saturation borderline level."
                ),
                clinicalNotes = PatientClinicalNotes(
                    observation = "Diffused wheezing bilaterally. Accessory muscle use visible. Pyrexia present.",
                    clinicalNotes = "Nebulize in clinic if distress increases. Pulse-ox monitoring.",
                    treatmentPlan = "Amoxicillin 500mg, Budesonide Inhaler, Albuterol Nebulizer.",
                    recommendations = "Monitor oxygen levels. Go to ER if O2 drops below 92%.",
                    lifestyleAdvice = "Steam inhalation twice daily. Strict avoidance of allergens and dust.",
                    referralNotes = "Pulmonology Dept (Wing A)"
                ),
                prescriptions = listOf(
                    PrescriptionItem("med_4", "Amoxicillin", "500 mg", true, true, true, "5 days", "Complete full antibiotic course"),
                    PrescriptionItem("med_5", "Budesonide Inhaler", "200 mcg", true, false, true, "30 days", "Rinse mouth after each use")
                ),
                labOrders = listOf(
                    LabOrderItem("lab_4", "Chest X-Ray", "STAT", "Rule out lobar pneumonia"),
                    LabOrderItem("lab_5", "CBC", "Urgent", "Assess leucocytosis / infection indicator")
                )
            ),
            PatientRecord(
                id = "pat_4",
                name = "Thomas Anderson",
                queueNumber = 6,
                appointmentTime = "12:00 PM",
                age = 31,
                gender = "Male",
                bloodGroup = "AB-Positive (AB+)",
                height = "185 cm",
                weight = "78 kg",
                bmi = "22.8 (Healthy)",
                allergies = emptyList(),
                medications = listOf("Melatonin 5mg (At bedtime)"),
                history = listOf("Severe Chronic Insomnia", "Ocular Fatigue"),
                previousVisits = listOf("Eye Clinic Refraction (Feb 2026)"),
                uploadedReports = listOf(
                    LabReport("Brain MRI Screen", "Apr 05, 2026", "No abnormal findings", "Normal")
                ),
                emergencyContact = "Trinity (+1 555-101-0101)",
                symptoms = "Persistent daily migraines & sleep deprivation",
                priority = "Normal",
                status = "Waiting",
                vitals = PatientVitals(
                    bloodPressure = "122/80",
                    heartRate = 76,
                    temperature = 98.6f,
                    oxygenSaturation = 98,
                    respiratoryRate = 16,
                    heightCm = 185f,
                    weightKg = 78f,
                    bmi = 22.8f,
                    painScale = 6
                ),
                diagnosis = PatientDiagnosis(
                    chiefComplaint = "Persistent daily throbbing migraines with photo-sensitivity.",
                    symptoms = "Severe throbbing headaches, sleep deprivation, eye fatigue.",
                    primaryDiagnosis = "Chronic Migraine",
                    secondaryDiagnosis = "Severe Sleep-Onset Insomnia",
                    clinicalImpression = "Tension migraine secondary to circadian rhythm disruption",
                    doctorNotes = "Migraines triggered by blue light exposure and lack of restorative sleep."
                ),
                clinicalNotes = PatientClinicalNotes(
                    observation = "Fundoscopy normal. No focal neurological deficits. Neck muscle spasm present.",
                    clinicalNotes = "Advise screen break intervals. Review sleep diary in 2 weeks.",
                    treatmentPlan = "Sumatriptan 50mg at onset of aura, Melatonin 5mg at bedtime.",
                    recommendations = "Refer to neurologist if headache frequency increases.",
                    lifestyleAdvice = "Limit screens 2 hours before bed. Keep bedroom pitch dark. Hydrate.",
                    referralNotes = "Neurology Department"
                ),
                prescriptions = listOf(
                    PrescriptionItem("med_6", "Sumatriptan", "50 mg", false, false, false, "10 days", "Take immediately at onset of migraine"),
                    PrescriptionItem("med_7", "Melatonin", "5 mg", false, false, true, "30 days", "Take 30 mins before sleep")
                ),
                labOrders = listOf(
                    LabOrderItem("lab_6", "Brain MRI Screen", "Routine", "Exclusion of vascular anomaly or lesions")
                )
            ),
            PatientRecord(
                id = "pat_5",
                name = "Bruce Wayne",
                queueNumber = 7,
                appointmentTime = "12:30 PM",
                age = 38,
                gender = "Male",
                bloodGroup = "O-Negative (O-)",
                height = "188 cm",
                weight = "95 kg",
                bmi = "26.9 (Slightly Overweight/Muscle)",
                allergies = emptyList(),
                medications = emptyList(),
                history = listOf("Multiple bone fractures", "Recurrent concussion syndromes"),
                previousVisits = listOf("Emergency Trauma Intake (Jan 10, 2026)"),
                uploadedReports = listOf(
                    LabReport("Whole Body CT Scan", "Jan 10, 2026", "Healed rib fractures, scar tissue", "Normal")
                ),
                emergencyContact = "Alfred Pennyworth (+1 555-193-9111)",
                symptoms = "Multiple physical contusions, shoulder stiffness",
                priority = "High",
                status = "Waiting",
                vitals = PatientVitals(
                    bloodPressure = "115/75",
                    heartRate = 64,
                    temperature = 98.2f,
                    oxygenSaturation = 99,
                    respiratoryRate = 12,
                    heightCm = 188f,
                    weightKg = 95f,
                    bmi = 26.9f,
                    painScale = 7
                ),
                diagnosis = PatientDiagnosis(
                    chiefComplaint = "Multiple physical contusions, right shoulder stiffness & pain.",
                    symptoms = "Bruising, restricted joint motion, localized swelling.",
                    primaryDiagnosis = "Right Shoulder Contusion & Sprain",
                    secondaryDiagnosis = "History of Multiple Traumatic Skeletal Injuries",
                    clinicalImpression = "Soft tissue trauma with minor mechanical impingement",
                    doctorNotes = "Significant bruising over anterior deltoid. Assess for rotator cuff tear."
                ),
                clinicalNotes = PatientClinicalNotes(
                    observation = "Severe tenderness over greater tuberosity. Restricted passive abduction.",
                    clinicalNotes = "Keep shoulder immobilized in sling if pain is severe during transport.",
                    treatmentPlan = "Ibuprofen 400mg twice daily, physical rehabilitation.",
                    recommendations = "Order shoulder MRI to rule out tendon ruptures.",
                    lifestyleAdvice = "Strict rest of right arm. Avoid heavy lifting or combat sports.",
                    referralNotes = "Orthopedic Sciences Dept (Wing C)"
                ),
                prescriptions = listOf(
                    PrescriptionItem("med_8", "Ibuprofen", "400 mg", true, false, true, "10 days", "Take with meals to prevent gastric upset")
                ),
                labOrders = listOf(
                    LabOrderItem("lab_7", "MRI Shoulder", "Urgent", "Assess rotator cuff and labral integrity")
                )
            )
        ))
        
        // Mock Consultation History
        consultationHistory.clear()
        consultationHistory.addAll(listOf(
            PastConsultation("hist_1", "Marcus Aurelius", 56, "Male", "Cardiac Arrhythmia Control", 15, 2, 1, "1 Week", "Completed", "Today"),
            PastConsultation("hist_2", "John Connor", 14, "Male", "General Juvenile Checkup", 8, 1, 0, "None", "Completed", "Today"),
            PastConsultation("hist_3", "Leia Organa", 31, "Female", "Post-Stress Angiography Review", 18, 3, 2, "1 Month", "Completed", "Yesterday"),
            PastConsultation("hist_4", "Luke Skywalker", 24, "Male", "Hand Prosthesis Nerve Check", 11, 0, 1, "3 Months", "Completed", "Yesterday"),
            PastConsultation("hist_5", "Clark Kent", 33, "Male", "Chest Muscular Strain Assessment", 10, 1, 1, "2 Weeks", "Completed", "Last Week"),
            PastConsultation("hist_6", "Diana Prince", 45, "Female", "Optimal Fitness Health Intake", 12, 0, 0, "None", "Completed", "Last Week"),
            PastConsultation("hist_7", "Peter Parker", 19, "Male", "Cellular Regeneration Blood Screen", 14, 2, 3, "3 Days", "Completed", "Last Month"),
            PastConsultation("hist_8", "Tony Stark", 48, "Male", "Cardiovascular Metal-Alloy Load Check", 22, 4, 4, "1 Month", "Completed", "Last Month")
        ))
        
        recalculateEstimatedWaitTimes()
        
        notifications.clear()
        notifications.addAll(listOf(
            NotificationItem("notif_d1", "New Patient Checked In", "Sarah Connor is now checked in and waiting in line.", "5 mins ago", "Queue", false, "HIGH"),
            NotificationItem("notif_d2", "Emergency Assigned", "Critical patient Ellen Ripley (Queue #5) has been assigned to you.", "15 mins ago", "Emergency", false, "HIGH"),
            NotificationItem("notif_d3", "Schedule Alert", "Review recommended buffer slots for 3:00 PM flow.", "1 hr ago", "Appointment", true, "NORMAL")
        ))
        
        activePatientId = null
        consultationTimerSeconds = 0L
        breakTimerSeconds = 0L
        isTimerRunning = false
        
        currentVitals = PatientVitals()
        currentDiagnosis = PatientDiagnosis()
        currentClinicalNotes = PatientClinicalNotes()
        currentPrescriptions.clear()
        currentLabOrders.clear()
        currentFollowUpDuration = "None"
        currentFollowUpNotes = ""
    }

    // Recalculates waiting times of remaining patients
    fun recalculateEstimatedWaitTimes() {
        val avgTime = doctorProfile.averageConsultationTime
        
        // Find active consultation indices
        val inConsultationIndex = appointments.indexOfFirst { it.status == "In Consultation" }
        val currentElapsedMinutes = (consultationTimerSeconds / 60).toInt()
        
        // Base remaining consultation minutes
        var baseRemainingMinutes = if (inConsultationIndex != -1) {
            maxOf(1, avgTime - currentElapsedMinutes)
        } else {
            0
        }
        
        // Availability modifications
        when (dutyStatus) {
            DoctorDutyStatus.BUSY -> {
                // Add a 10 minutes buffer since doctor is Busy
                baseRemainingMinutes += 10
            }
            DoctorDutyStatus.BREAK -> {
                // Consultation paused, wait time is inflated by current break time
                val breakMinutes = (breakTimerSeconds / 60).toInt()
                baseRemainingMinutes += 15 + breakMinutes
            }
            DoctorDutyStatus.OFFLINE -> {
                // Doctor offline, wait times are infinity or technically paused
                appointments.forEach {
                    if (it.status != "Completed" && it.status != "Cancelled") {
                        it.estimatedWaitMinutes = 999 // Representing unavailable
                    }
                }
                return
            }
            DoctorDutyStatus.AVAILABLE -> {}
        }
        
        var runningWaitTime = baseRemainingMinutes
        
        appointments.forEach { patient ->
            when (patient.status) {
                "Completed", "Cancelled" -> {
                    patient.estimatedWaitMinutes = 0
                }
                "In Consultation" -> {
                    patient.estimatedWaitMinutes = 0
                }
                "Checked In", "Waiting" -> {
                    patient.estimatedWaitMinutes = runningWaitTime
                    runningWaitTime += avgTime
                }
            }
        }
    }
    
    fun startConsultation(patientId: String) {
        if (dutyStatus == DoctorDutyStatus.OFFLINE) return
        
        // Save current active if any back to Checked In
        appointments.forEach {
            if (it.status == "In Consultation") {
                it.status = "Checked In"
            }
        }
        
        val index = appointments.indexOfFirst { it.id == patientId }
        if (index != -1) {
            appointments[index].status = "In Consultation"
            activePatientId = patientId
            consultationTimerSeconds = 0L
            isTimerRunning = true
            
            val patient = appointments[index]
            
            // Populate workspace text fields with active/previous metrics
            currentVitals = PatientVitals(
                bloodPressure = patient.vitals.bloodPressure,
                heartRate = patient.vitals.heartRate,
                temperature = patient.vitals.temperature,
                oxygenSaturation = patient.vitals.oxygenSaturation,
                respiratoryRate = patient.vitals.respiratoryRate,
                heightCm = patient.vitals.heightCm,
                weightKg = patient.vitals.weightKg,
                bmi = patient.vitals.bmi,
                painScale = patient.vitals.painScale
            )
            currentDiagnosis = PatientDiagnosis(
                chiefComplaint = patient.diagnosis.chiefComplaint.ifBlank { patient.symptoms },
                symptoms = patient.diagnosis.symptoms.ifBlank { patient.symptoms },
                primaryDiagnosis = patient.diagnosis.primaryDiagnosis,
                secondaryDiagnosis = patient.diagnosis.secondaryDiagnosis,
                clinicalImpression = patient.diagnosis.clinicalImpression,
                doctorNotes = patient.diagnosis.doctorNotes
            )
            currentClinicalNotes = PatientClinicalNotes(
                observation = patient.clinicalNotes.observation,
                clinicalNotes = patient.clinicalNotes.clinicalNotes,
                treatmentPlan = patient.clinicalNotes.treatmentPlan,
                recommendations = patient.clinicalNotes.recommendations,
                lifestyleAdvice = patient.clinicalNotes.lifestyleAdvice,
                referralNotes = patient.clinicalNotes.referralNotes
            )
            currentPrescriptions.clear()
            currentPrescriptions.addAll(patient.prescriptions)
            currentLabOrders.clear()
            currentLabOrders.addAll(patient.labOrders)
            currentFollowUpDuration = patient.followUpDuration
            currentFollowUpNotes = patient.followUpNotes
            
            dutyStatus = DoctorDutyStatus.BUSY
        }
        
        recalculateEstimatedWaitTimes()
        
        addNotification(
            title = "Consultation Active",
            message = "Now consulting patient ${appointments[index].name}.",
            type = "Queue",
            priority = "NORMAL"
        )
    }
    
    fun completeActiveConsultation(durationMinutes: Int) {
        val patientId = activePatientId ?: return
        val index = appointments.indexOfFirst { it.id == patientId }
        if (index != -1) {
            val patient = appointments[index]
            patient.status = "Completed"
            patient.actualDurationMinutes = durationMinutes
            
            // Commit all input buffer fields into patient file
            patient.vitals = currentVitals.copy()
            patient.diagnosis = currentDiagnosis.copy()
            patient.clinicalNotes = currentClinicalNotes.copy()
            patient.prescriptions = currentPrescriptions.toList()
            patient.labOrders = currentLabOrders.toList()
            patient.followUpDuration = currentFollowUpDuration
            patient.followUpNotes = currentFollowUpNotes
            
            // Add to consultation history
            consultationHistory.add(0, PastConsultation(
                id = "hist_gen_${System.currentTimeMillis()}",
                patientName = patient.name,
                age = patient.age,
                gender = patient.gender,
                diagnosis = currentDiagnosis.primaryDiagnosis.ifBlank { "General Assessment" },
                durationMinutes = durationMinutes,
                prescriptionCount = currentPrescriptions.size,
                labOrdersCount = currentLabOrders.size,
                followUp = currentFollowUpDuration,
                status = "Completed",
                dateCategory = "Today"
            ))
            
            // Clean up workspace state
            activePatientId = null
            isTimerRunning = false
            consultationTimerSeconds = 0L
            
            currentVitals = PatientVitals()
            currentDiagnosis = PatientDiagnosis()
            currentClinicalNotes = PatientClinicalNotes()
            currentPrescriptions.clear()
            currentLabOrders.clear()
            currentFollowUpDuration = "None"
            currentFollowUpNotes = ""
            
            // Notify about completion
            addNotification(
                title = "Consultation Completed",
                message = "${patient.name}'s records updated and Rx saved.",
                type = "Queue",
                priority = "NORMAL"
            )
            
            // Automatic queue shift! Advance the next Checked In / Waiting patient
            val nextIndex = appointments.indexOfFirst { it.status == "Checked In" || it.status == "Waiting" }
            if (nextIndex != -1) {
                val nextPatient = appointments[nextIndex]
                nextPatient.status = "In Consultation"
                activePatientId = nextPatient.id
                consultationTimerSeconds = 0L
                isTimerRunning = true
                dutyStatus = DoctorDutyStatus.BUSY
                
                addNotification(
                    title = "Queue Advanced",
                    message = "${nextPatient.name} is now in Room 4B.",
                    type = "Queue",
                    priority = "HIGH"
                )
            } else {
                dutyStatus = DoctorDutyStatus.AVAILABLE
            }
        }
        
        recalculateEstimatedWaitTimes()
    }
    
    fun addNotification(title: String, message: String, type: String, priority: String) {
        val newNotif = NotificationItem(
            id = "notif_gen_${System.currentTimeMillis()}",
            title = title,
            message = message,
            timestamp = "Just now",
            type = type,
            isRead = false,
            priority = priority
        )
        notifications.add(0, newNotif)
    }
}
