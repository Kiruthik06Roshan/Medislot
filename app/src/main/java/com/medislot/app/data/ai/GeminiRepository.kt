package com.medislot.app.data.ai

interface GeminiRepository {
    // Patient
    suspend fun checkSymptoms(symptoms: String, forceRefresh: Boolean = false): Result<SymptomCheckResponse>
    suspend fun explainReport(reportText: String, forceRefresh: Boolean = false): Result<ReportExplanationResponse>
    suspend fun explainPrescription(prescriptionText: String, forceRefresh: Boolean = false): Result<PrescriptionExplanationResponse>
    suspend fun getDailyHealthTips(age: Int, gender: String, history: String, lifestyle: String, forceRefresh: Boolean = false): Result<DailyHealthTipsResponse>
    suspend fun getDietRecommendation(conditions: String, weight: String, bmi: String, forceRefresh: Boolean = false): Result<DietRecommendationResponse>
    suspend fun getAppointmentPrep(doctorName: String, specialty: String, lastVisitSymptoms: String, forceRefresh: Boolean = false): Result<AppointmentPrepResponse>
    suspend fun chatWithAssistant(chatHistory: String, userMessage: String): Result<ChatResponse>

    // Doctor
    suspend fun generateConsultationSummary(rawNotes: String, forceRefresh: Boolean = false): Result<ConsultationSummaryResponse>
    suspend fun generateSoapNote(rawNotes: String, forceRefresh: Boolean = false): Result<SoapNoteResponse>
    suspend fun enhanceClinicalNotes(shorthandNotes: String, forceRefresh: Boolean = false): Result<ClinicalEnhancementResponse>
    suspend fun getDifferentialDiagnosis(symptoms: String, vitals: String, history: String, forceRefresh: Boolean = false): Result<DiffDiagnosisResponse>
    suspend fun draftPrescription(symptoms: String, vitals: String, history: String, forceRefresh: Boolean = false): Result<PrescriptionDraftResponse>
    suspend fun interpretLabReport(testName: String, labText: String, forceRefresh: Boolean = false): Result<ReportInterpretationResponse>
    suspend fun interpretRadiologyReport(scanType: String, reportFindings: String, forceRefresh: Boolean = false): Result<RadiologyReportResponse>
    suspend fun generatePatientEducation(diagnosis: String, instructions: String, forceRefresh: Boolean = false): Result<PatientEducationResponse>
    suspend fun generateReferralLetter(patientName: String, age: Int, diagnosis: String, summary: String, targetSpecialist: String, forceRefresh: Boolean = false): Result<ReferralLetterResponse>
    suspend fun generateDischargeSummary(stayDetails: String, treatments: String, meds: String, forceRefresh: Boolean = false): Result<DischargeSummaryResponse>

    // Admin
    suspend fun getOperationalInsights(admissions: String, waitingTime: String, occupancy: String, utilization: String, forceRefresh: Boolean = false): Result<OperationalInsightsResponse>
    suspend fun optimizeResources(resourcesList: String, forceRefresh: Boolean = false): Result<ResourceOptimizationResponse>
    suspend fun suggestStaffAllocation(departmentsLoad: String, nurseDoctorCount: String, forceRefresh: Boolean = false): Result<StaffAllocationResponse>
    suspend fun predictEmergencyRisk(traumaInflow: String, bedsFree: String, waitingMins: String, forceRefresh: Boolean = false): Result<EmergencyRiskResponse>
    suspend fun generatePerformanceReport(flow: String, efficiency: String, waiting: String, forceRefresh: Boolean = false): Result<PerformanceReportResponse>
    suspend fun getDailyBriefing(metrics: String, activeCriticalAlerts: String, forceRefresh: Boolean = false): Result<DailyBriefingResponse>
    suspend fun generateNotification(operationalAlertText: String, forceRefresh: Boolean = false): Result<NotificationGeneratorResponse>
    
    // Cache clearing
    fun clearCache()
}
