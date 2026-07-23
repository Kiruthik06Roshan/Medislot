package com.medislot.app.data.ai

data class SymptomCheckResponse(
    val possibleConditions: List<String> = emptyList(),
    val severity: String = "",
    val recommendedAction: String = "",
    val homeCare: String = "",
    val doctorVisitRecommendation: String = "",
    val emergencyWarning: String = "",
    val disclaimer: String = ""
)

data class ReportExplanationResponse(
    val summary: String = "",
    val normalFindings: List<String> = emptyList(),
    val abnormalFindings: List<String> = emptyList(),
    val possibleMeaning: String = "",
    val questionsToAskDoctor: List<String> = emptyList(),
    val disclaimer: String = ""
)

data class PrescriptionExplanationResponse(
    val summary: String = "",
    val medicines: List<MedicineExplanation> = emptyList(),
    val generalPrecautions: List<String> = emptyList(),
    val disclaimer: String = ""
)

data class MedicineExplanation(
    val name: String = "",
    val purpose: String = "",
    val howToTake: String = "",
    val commonSideEffects: List<String> = emptyList()
)

data class DailyHealthTipsResponse(
    val hydration: String = "",
    val exercise: String = "",
    val sleep: String = "",
    val nutrition: String = "",
    val mentalWellness: String = ""
)

data class DietRecommendationResponse(
    val foodsToEat: List<String> = emptyList(),
    val foodsToAvoid: List<String> = emptyList(),
    val hydration: String = "",
    val lifestyleAdvice: List<String> = emptyList()
)

data class AppointmentPrepResponse(
    val checklist: List<String> = emptyList()
)

data class ConsultationSummaryResponse(
    val chiefComplaint: String = "",
    val history: String = "",
    val diagnosis: String = "",
    val treatment: String = "",
    val prescriptionSummary: String = "",
    val followUp: String = "",
    val patientInstructions: String = ""
)

data class SoapNoteResponse(
    val subjective: String = "",
    val objective: String = "",
    val assessment: String = "",
    val plan: String = ""
)

data class ClinicalEnhancementResponse(
    val enhancedNotes: String = ""
)

data class DiffDiagnosisResponse(
    val possibleDiagnoses: List<String> = emptyList(),
    val suggestedInvestigations: List<String> = emptyList(),
    val redFlags: List<String> = emptyList(),
    val confidenceLevel: String = "",
    val disclaimer: String = ""
)

data class PrescriptionDraftResponse(
    val medicineCategories: List<String> = emptyList(),
    val lifestyleAdvice: List<String> = emptyList(),
    val followUpRecommendations: String = ""
)

data class ReportInterpretationResponse(
    val summary: String = "",
    val abnormalValues: List<String> = emptyList(),
    val criticalValues: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val possibleFollowUpTests: List<String> = emptyList()
)

data class RadiologyReportResponse(
    val summary: String = "",
    val criticalFindings: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)

data class PatientEducationResponse(
    val instructions: String = "",
    val warningSigns: List<String> = emptyList()
)

data class ReferralLetterResponse(
    val letter: String = ""
)

data class DischargeSummaryResponse(
    val letter: String = ""
)

data class OperationalInsightsResponse(
    val summary: String = "",
    val bottlenecks: List<String> = emptyList(),
    val suggestedImprovements: List<String> = emptyList(),
    val priorityLevel: String = ""
)

data class ResourceOptimizationResponse(
    val summary: String = "",
    val resourceAllocation: List<String> = emptyList(),
    val capacityPlanning: List<String> = emptyList()
)

data class StaffAllocationResponse(
    val additionalStaffing: String = "",
    val doctorRedistribution: String = "",
    val shiftOptimization: String = ""
)

data class EmergencyRiskResponse(
    val riskLevel: String = "",
    val reason: String = "",
    val recommendations: List<String> = emptyList(),
    val preparednessChecklist: List<String> = emptyList()
)

data class PerformanceReportResponse(
    val patientFlow: String = "",
    val doctorEfficiency: String = "",
    val resourceUsage: String = "",
    val waitingTime: String = "",
    val capacity: String = "",
    val improvementAreas: List<String> = emptyList()
)

data class DailyBriefingResponse(
    val todaySummary: String = "",
    val criticalAlerts: List<String> = emptyList(),
    val predictedBusyHours: String = "",
    val recommendations: List<String> = emptyList()
)

data class NotificationGeneratorResponse(
    val notificationText: String = ""
)

data class ChatResponse(
    val reply: String = "",
    val suggestedQuestions: List<String> = emptyList()
)
