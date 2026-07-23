package com.medislot.app.data.ai

object PromptTemplates {

    val SYSTEM_JSON_INSTRUCTION = "You must output strictly a raw JSON string. Do not wrap the JSON in ```json or any markdown formatting. Do not include any text before or after the JSON. Ensure it is fully valid and parseable JSON matching the requested structure."

    fun getSymptomCheckerPrompt(symptoms: String): String {
        return """
            Role: Clinical Symptom Assistant
            Context: You are a safe, non-diagnostic symptom guide helping a patient understand possible departments and actions based on their symptoms.
            Tone: Empathetic, calm, non-alarming, and objective.
            Medical Disclaimer: "This tool does not provide diagnoses or replace medical advice. If experiencing severe discomfort, seek immediate professional care."
            
            Input Symptoms: $symptoms
            
            Return a raw JSON structure matching exactly this schema:
            {
              "possibleConditions": ["Condition A", "Condition B"],
              "severity": "Low | Moderate | High",
              "recommendedAction": "Action advice summary",
              "homeCare": "Home care guidelines",
              "doctorVisitRecommendation": "Guidance on when/why to see a specialist",
              "emergencyWarning": "Specific symptoms that require ER visit, or empty if none",
              "disclaimer": "Medical Disclaimer text"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getReportExplanationPrompt(reportText: String): String {
        return """
            Role: Patient Health Educator
            Context: The patient has provided the following laboratory or diagnostic report. Explain it in plain, simple, layperson language.
            Tone: Reassuring, clear, educational.
            Medical Disclaimer: "This explanation is for educational purposes. Always discuss laboratory results directly with your primary care provider."
            
            Report Data:
            $reportText
            
            Return a raw JSON structure matching exactly this schema:
            {
              "summary": "High level explanation in simple terms",
              "normalFindings": ["Normal item 1", "Normal item 2"],
              "abnormalFindings": ["Elevated/low item 1 with explanation"],
              "possibleMeaning": "What these findings could represent overall",
              "questionsToAskDoctor": ["Question 1", "Question 2"],
              "disclaimer": "Medical Disclaimer text"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getPrescriptionExplanationPrompt(prescriptionText: String): String {
        return """
            Role: Clinical Pharmacist Guide
            Context: The patient has provided a prescription or medication list. Explain each medicine, its purpose, dosage instructions, and general safety tips.
            Tone: Informative, precise, safe.
            Medical Disclaimer: "Follow your doctor's exact instructions. Do not change doses without consulting your physician."
            
            Prescription Data:
            $prescriptionText
            
            Return a raw JSON structure matching exactly this schema:
            {
              "summary": "Brief summary of medications prescribed",
              "medicines": [
                {
                  "name": "Medicine name",
                  "purpose": "What this medicine does",
                  "howToTake": "Dosage / Timing instructions",
                  "commonSideEffects": ["Side effect 1", "Side effect 2"]
                }
              ],
              "generalPrecautions": ["Precaution 1", "Precaution 2"],
              "disclaimer": "Medical Disclaimer text"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getDailyHealthTipsPrompt(age: Int, gender: String, history: String, lifestyle: String): String {
        return """
            Role: Personalized Health Coach
            Context: Generate personalized, actionable daily health tips based on basic patient metadata:
            Age: $age, Gender: $gender
            Medical History: $history
            Lifestyle: $lifestyle
            
            Return a raw JSON structure matching exactly this schema:
            {
              "hydration": "Tip about hydration tailored to profile",
              "exercise": "Tip about activity and physical training",
              "sleep": "Tip about resting guidelines",
              "nutrition": "Dietary advice based on history",
              "mentalWellness": "Stress management / focus tip"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getDietRecommendationPrompt(conditions: String, weight: String, bmi: String): String {
        return """
            Role: Specialized Medical Nutritionist
            Context: Provide custom dietary advice based on chronic conditions (e.g. Diabetes, Hypertension, Pregnancy), weight, and BMI.
            Conditions: $conditions
            Weight: $weight, BMI: $bmi
            
            Return a raw JSON structure matching exactly this schema:
            {
              "foodsToEat": ["Food 1", "Food 2"],
              "foodsToAvoid": ["Food 1", "Food 2"],
              "hydration": "Hydration requirement guidelines",
              "lifestyleAdvice": ["Lifestyle tip 1", "Lifestyle tip 2"]
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getAppointmentPrepPrompt(doctorName: String, specialty: String, lastVisitSymptoms: String): String {
        return """
            Role: Clinical Prep Assistant
            Context: Help the patient organize a practical checklist before their upcoming appointment with a doctor.
            Doctor: $doctorName, Specialty: $specialty
            Patient Symptoms/Concerns: $lastVisitSymptoms
            
            Return a raw JSON structure matching exactly this schema:
            {
              "checklist": [
                "Gather previous reports like [example related reports]",
                "List of current medications",
                "Key questions about [symptoms/specialty]",
                "Other items to bring"
              ]
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getChatAssistantPrompt(chatHistory: String, userMessage: String): String {
        return """
            Role: MediSlot AI Assistant Helper
            Context: You are a friendly health navigation assistant inside a hospital app. Help the patient with clinical navigation, appointment questions, general health education, or medication information. 
            Do not provide specific diagnoses. Keep answers clear and patient-oriented.
            
            Conversation History:
            $chatHistory
            
            New Patient Message: $userMessage
            
            Return a raw JSON structure matching exactly this schema:
            {
              "reply": "Empathetic, clear, and informative response to the user's message.",
              "suggestedQuestions": ["Suggested follow-up query 1?", "Suggested follow-up query 2?"]
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    // ==========================================
    // DOCTOR DASHBOARD PROMPTS
    // ==========================================

    fun getConsultationSummaryPrompt(rawNotes: String): String {
        return """
            Role: Clinical Scribe Specialist
            Context: Translate raw clinical shorthand consultation notes into a professional medical consultation summary.
            Tone: Highly clinical, professional, objective.
            
            Raw Consultation Notes:
            $rawNotes
            
            Return a raw JSON structure matching exactly this schema:
            {
              "chiefComplaint": "Clear clinical chief complaint summary",
              "history": "History of present illness details",
              "diagnosis": "Clinical diagnoses",
              "treatment": "Treatment / therapy prescribed",
              "prescriptionSummary": "List of drugs and dosing outline",
              "followUp": "Follow-up directions",
              "patientInstructions": "Simple instructions translated for patient benefit"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getSoapNotePrompt(rawNotes: String): String {
        return """
            Role: Medical Documentation Scribe
            Context: Formulate a standard clinical SOAP (Subjective, Objective, Assessment, Plan) note based on raw clinical documentation entry.
            Tone: Clinical, formal, concise.
            
            Raw Inputs:
            $rawNotes
            
            Return a raw JSON structure matching exactly this schema:
            {
              "subjective": "Subjective findings (patient complaints, history)",
              "objective": "Objective findings (vitals, exams, labs)",
              "assessment": "Assessment (diagnoses, differential reasoning)",
              "plan": "Plan (treatments, orders, follow-ups)"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getClinicalEnhancementPrompt(shorthandNotes: String): String {
        return """
            Role: Medical Transcription Editor
            Context: Convert raw shorthand notes into a highly professional clinical documentation narrative fit for patient EHR records.
            Example shorthand: "Patient fever 3 days. BP normal. Continue meds." -> "Patient presents with a 3-day history of low-grade fever. Blood pressure remains within normal physiological limits. Advised to continue current medication regimen."
            
            Shorthand: $shorthandNotes
            
            Return a raw JSON structure matching exactly this schema:
            {
              "enhancedNotes": "Enhanced professional clinical narrative"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getDiffDiagnosisPrompt(symptoms: String, vitals: String, history: String): String {
        return """
            Role: Diagnostic Intelligence Consultant
            Context: Provide differential diagnosis ideas and diagnostic workup suggestions based on patient presentation.
            Symptoms: $symptoms
            Vitals: $vitals
            History: $history
            
            Return a raw JSON structure matching exactly this schema:
            {
              "possibleDiagnoses": ["Diagnosis 1 (Confidence %)", "Diagnosis 2 (Confidence %)"],
              "suggestedInvestigations": ["Lab/Test 1", "Lab/Test 2"],
              "redFlags": ["Critical warning signs to monitor"],
              "confidenceLevel": "High | Medium | Low",
              "disclaimer": "For clinical decision support only; ultimate responsibility lies with the treating physician."
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getPrescriptionDraftPrompt(symptoms: String, vitals: String, history: String): String {
        return """
            Role: Clinical Pharmacology Assistant
            Context: Suggest appropriate medicine categories, supportive lifestyle adjustments, and follow-up guidance based on the clinical state.
            Symptoms: $symptoms
            Vitals: $vitals
            History: $history
            
            Return a raw JSON structure matching exactly this schema:
            {
              "medicineCategories": ["Class 1 (e.g. Beta blockers)", "Class 2 (e.g. Statins)"],
              "lifestyleAdvice": ["Lifestyle guidance 1", "Lifestyle guidance 2"],
              "followUpRecommendations": "Suggested timing and parameters to check at follow-up"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getLabReportInterpretationPrompt(testName: String, labText: String): String {
        return """
            Role: Pathology Interpretation Specialist
            Context: Interpret the laboratory values below, highlighting critical alarms, clinical meaning, and diagnostic options.
            Test: $testName
            Data: $labText
            
            Return a raw JSON structure matching exactly this schema:
            {
              "summary": "Interpretation summary",
              "abnormalValues": ["Abnormal analyte: value (clinical range)"],
              "criticalValues": ["Critical alert values if any"],
              "recommendations": ["Follow-up monitoring suggestions"],
              "possibleFollowUpTests": ["Reflex tests to order"]
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getRadiologyReportPrompt(scanType: String, reportFindings: String): String {
        return """
            Role: Diagnostic Radiologist Consultant
            Context: Summarize key radiological conclusions, abnormal structures, and clinical suggestions based on the scan findings.
            Scan Type: $scanType
            Report Findings: $reportFindings
            
            Return a raw JSON structure matching exactly this schema:
            {
              "summary": "Clear, concise diagnostic summary of findings",
              "criticalFindings": ["Abnormal structural/functional findings"],
              "recommendations": ["Next diagnostic imaging / clinical steps"]
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getPatientEducationPrompt(diagnosis: String, instructions: String): String {
        return """
            Role: Patient Communication Educator
            Context: Generate clear, patient-friendly home-care guidelines and warning signs.
            Diagnosis: $diagnosis
            Instructions: $instructions
            
            Return a raw JSON structure matching exactly this schema:
            {
              "instructions": "Patient-friendly educational instruction narrative",
              "warningSigns": ["Alert sign 1", "Alert sign 2"]
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getReferralLetterPrompt(patientName: String, age: Int, diagnosis: String, summary: String, targetSpecialist: String): String {
        return """
            Role: Clinical Communications Scribe
            Context: Draft a highly professional clinical referral letter to another specialist.
            Patient: $patientName (Age: $age)
            Clinical Summary & Diagnosis: $summary | $diagnosis
            Target Specialist: $targetSpecialist
            
            Return a raw JSON structure matching exactly this schema:
            {
              "letter": "Dear Dr. [Specialist],\n\nI am referring [Patient]... [Professional clinical text]\n\nSincerely,\n[Referring Doctor]"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getDischargeSummaryPrompt(stayDetails: String, treatments: String, meds: String): String {
        return """
            Role: Hospital Medicine Scribe
            Context: Generate a complete discharge summary for a patient leaving hospital care.
            Stay Details: $stayDetails
            Treatments: $treatments
            Discharge Meds: $meds
            
            Return a raw JSON structure matching exactly this schema:
            {
              "letter": "DISCHARGE SUMMARY OUTLINE\n\nHospital Course: [Summary]\n\nMedications: [List]\n\nFollow-up: [Plan]"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    // ==========================================
    // HOSPITAL ADMIN PROMPTS
    // ==========================================

    fun getOperationalInsightsPrompt(admissions: String, waitingTime: String, occupancy: String, utilization: String): String {
        return """
            Role: Hospital Operational Analytics Director
            Context: Analyze metrics to generate optimization insights, priority alarms, and bottlenecks.
            Admissions Today: $admissions
            Avg Wait Time: $waitingTime
            Bed Occupancy: $occupancy
            Doctor Utilization: $utilization
            
            Return a raw JSON structure matching exactly this schema:
            {
              "summary": "Operational performance summary",
              "bottlenecks": ["Resource bottleneck 1", "Capacity bottleneck 2"],
              "suggestedImprovements": ["Action plan 1", "Staff shift optimization 2"],
              "priorityLevel": "High | Medium | Low"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getResourceOptimizationPrompt(resourcesList: String): String {
        return """
            Role: Hospital Capacity Manager
            Context: Analyze resources (beds, ICU, ventilators, staff) to suggest optimization and safety plans.
            Resources status:
            $resourcesList
            
            Return a raw JSON structure matching exactly this schema:
            {
              "summary": "Analysis of resource levels",
              "resourceAllocation": ["Shift resource A to department B reasons"],
              "capacityPlanning": ["Alert recommendations to prevent shortages"]
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getStaffAllocationPrompt(departmentsLoad: String, nurseDoctorCount: String): String {
        return """
            Role: Nurse & Doctor Shift Allocator
            Context: Suggest staffing adjustments, extra resources, or redistribution of clinicians.
            Load per department: $departmentsLoad
            Clinicians available: $nurseDoctorCount
            
            Return a raw JSON structure matching exactly this schema:
            {
              "additionalStaffing": "Department staffing alerts",
              "doctorRedistribution": "Doctor redistribution recommendations",
              "shiftOptimization": "Shift adjustment tips"
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getEmergencyRiskPrompt(traumaInflow: String, bedsFree: String, waitingMins: String): String {
        return """
            Role: Hospital Risk Analyst
            Context: Predict emergency department overload and preparedness checklist.
            Trauma Inflow Trend: $traumaInflow
            Free ER Beds: $bedsFree
            ED Wait Time: $waitingMins mins
            
            Return a raw JSON structure matching exactly this schema:
            {
              "riskLevel": "Critical | Elevated | Normal",
              "reason": "Analysis of ER risk triggers",
              "recommendations": ["ER step 1", "ER step 2"],
              "preparednessChecklist": ["Verify supply A", "Alert staff shift B"]
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getPerformanceReportPrompt(flow: String, efficiency: String, waiting: String): String {
        return """
            Role: Hospital Chief Operations Officer
            Context: Generate an executive operations report for hospital performance.
            Intake Flow: $flow
            Clinician Efficiency: $efficiency
            Wait Times: $waiting
            
            Return a raw JSON structure matching exactly this schema:
            {
              "patientFlow": "Analysis of patient volume throughput",
              "doctorEfficiency": "Summary of doctor consultation paces",
              "resourceUsage": "ICU and equipment usage analysis",
              "waitingTime": "Patient waiting trends review",
              "capacity": "Overall hospital capacity status",
              "improvementAreas": ["Improvement suggestion 1", "Improvement suggestion 2"]
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getDailyBriefingPrompt(metrics: String, activeCriticalAlerts: String): String {
        return """
            Role: Chief Medical Officer Briefing Director
            Context: Generate a morning tactical briefing for hospital administration.
            Metrics status: $metrics
            Active critical alarms: $activeCriticalAlerts
            
            Return a raw JSON structure matching exactly this schema:
            {
              "todaySummary": "Tactical summary for today's shifts",
              "criticalAlerts": ["Alert 1 description", "Alert 2 description"],
              "predictedBusyHours": "Busy hours window prediction (e.g. 10:00 AM - 02:00 PM)",
              "recommendations": ["Daily directive 1", "Daily directive 2"]
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }

    fun getNotificationPrompt(operationalAlertText: String): String {
        return """
            Role: Hospital Communications Bot
            Context: Convert complex operational metrics into a short, concise, action-oriented admin push notification.
            Example metrics: "Emergency Department beds = 0, trauma inflow incoming." -> "Emergency Department approaching capacity. Recommend assigning two additional physicians."
            
            Source metrics: $operationalAlertText
            
            Return a raw JSON structure matching exactly this schema:
            {
              "notificationText": "Concise, actionable notification alert string."
            }
            
            $SYSTEM_JSON_INSTRUCTION
        """.trimIndent()
    }
}
