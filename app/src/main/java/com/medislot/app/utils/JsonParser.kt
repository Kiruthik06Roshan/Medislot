package com.medislot.app.utils

import org.json.JSONObject
import org.json.JSONArray
import com.medislot.app.data.ai.*

object JsonParser {

    private fun cleanJsonString(input: String): String {
        var cleaned = input.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substringAfter("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substringAfter("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substringBeforeLast("```")
        }
        return cleaned.trim()
    }

    private fun optStringList(obj: JSONObject, key: String): List<String> {
        val list = mutableListOf<String>()
        val arr = obj.optJSONArray(key) ?: return list
        for (i in 0 until arr.length()) {
            val v = arr.optString(i)
            if (v.isNotEmpty()) list.add(v)
        }
        return list
    }

    fun parseSymptomCheck(json: String): SymptomCheckResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            SymptomCheckResponse(
                possibleConditions = optStringList(obj, "possibleConditions"),
                severity = obj.optString("severity", "Unknown"),
                recommendedAction = obj.optString("recommendedAction", ""),
                homeCare = obj.optString("homeCare", ""),
                doctorVisitRecommendation = obj.optString("doctorVisitRecommendation", ""),
                emergencyWarning = obj.optString("emergencyWarning", ""),
                disclaimer = obj.optString("disclaimer", "")
            )
        } catch (e: Exception) {
            SymptomCheckResponse(disclaimer = "Parsing error: ${e.message}")
        }
    }

    fun parseReportExplanation(json: String): ReportExplanationResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            ReportExplanationResponse(
                summary = obj.optString("summary", ""),
                normalFindings = optStringList(obj, "normalFindings"),
                abnormalFindings = optStringList(obj, "abnormalFindings"),
                possibleMeaning = obj.optString("possibleMeaning", ""),
                questionsToAskDoctor = optStringList(obj, "questionsToAskDoctor"),
                disclaimer = obj.optString("disclaimer", "")
            )
        } catch (e: Exception) {
            ReportExplanationResponse(summary = "Error parsing report explanation.", disclaimer = e.message ?: "")
        }
    }

    fun parsePrescriptionExplanation(json: String): PrescriptionExplanationResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            val medList = mutableListOf<MedicineExplanation>()
            val medArr = obj.optJSONArray("medicines")
            if (medArr != null) {
                for (i in 0 until medArr.length()) {
                    val medObj = medArr.optJSONObject(i) ?: continue
                    val sideEffects = mutableListOf<String>()
                    val sideArr = medObj.optJSONArray("commonSideEffects")
                    if (sideArr != null) {
                        for (j in 0 until sideArr.length()) {
                            sideEffects.add(sideArr.optString(j))
                        }
                    }
                    medList.add(
                        MedicineExplanation(
                            name = medObj.optString("name", ""),
                            purpose = medObj.optString("purpose", ""),
                            howToTake = medObj.optString("howToTake", ""),
                            commonSideEffects = sideEffects
                        )
                    )
                }
            }
            PrescriptionExplanationResponse(
                summary = obj.optString("summary", ""),
                medicines = medList,
                generalPrecautions = optStringList(obj, "generalPrecautions"),
                disclaimer = obj.optString("disclaimer", "")
            )
        } catch (e: Exception) {
            PrescriptionExplanationResponse(summary = "Error parsing prescription.", disclaimer = e.message ?: "")
        }
    }

    fun parseDailyHealthTips(json: String): DailyHealthTipsResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            DailyHealthTipsResponse(
                hydration = obj.optString("hydration", "Drink clean water regularly."),
                exercise = obj.optString("exercise", "Perform light physical activity daily."),
                sleep = obj.optString("sleep", "Get 7-8 hours of quality rest."),
                nutrition = obj.optString("nutrition", "Focus on a balanced whole foods diet."),
                mentalWellness = obj.optString("mentalWellness", "Practice mindfulness and stress reduction.")
            )
        } catch (e: Exception) {
            DailyHealthTipsResponse(hydration = "Failed to parse coaching tips.")
        }
    }

    fun parseDietRecommendation(json: String): DietRecommendationResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            DietRecommendationResponse(
                foodsToEat = optStringList(obj, "foodsToEat"),
                foodsToAvoid = optStringList(obj, "foodsToAvoid"),
                hydration = obj.optString("hydration", ""),
                lifestyleAdvice = optStringList(obj, "lifestyleAdvice")
            )
        } catch (e: Exception) {
            DietRecommendationResponse(hydration = "Failed to parse nutritional advice.")
        }
    }

    fun parseAppointmentPrep(json: String): AppointmentPrepResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            AppointmentPrepResponse(
                checklist = optStringList(obj, "checklist")
            )
        } catch (e: Exception) {
            AppointmentPrepResponse(checklist = listOf("Prepare medical record files", "Write list of symptoms"))
        }
    }

    fun parseChatAssistant(json: String): ChatResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            ChatResponse(
                reply = obj.optString("reply", ""),
                suggestedQuestions = optStringList(obj, "suggestedQuestions")
            )
        } catch (e: Exception) {
            ChatResponse(reply = json) // fallback to raw message
        }
    }

    fun parseConsultationSummary(json: String): ConsultationSummaryResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            ConsultationSummaryResponse(
                chiefComplaint = obj.optString("chiefComplaint", ""),
                history = obj.optString("history", ""),
                diagnosis = obj.optString("diagnosis", ""),
                treatment = obj.optString("treatment", ""),
                prescriptionSummary = obj.optString("prescriptionSummary", ""),
                followUp = obj.optString("followUp", ""),
                patientInstructions = obj.optString("patientInstructions", "")
            )
        } catch (e: Exception) {
            ConsultationSummaryResponse(chiefComplaint = "Parse failed: ${e.message}")
        }
    }

    fun parseSoapNote(json: String): SoapNoteResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            SoapNoteResponse(
                subjective = obj.optString("subjective", ""),
                objective = obj.optString("objective", ""),
                assessment = obj.optString("assessment", ""),
                plan = obj.optString("plan", "")
            )
        } catch (e: Exception) {
            SoapNoteResponse(subjective = "Parse failed: ${e.message}")
        }
    }

    fun parseClinicalEnhancement(json: String): ClinicalEnhancementResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            ClinicalEnhancementResponse(
                enhancedNotes = obj.optString("enhancedNotes", "")
            )
        } catch (e: Exception) {
            ClinicalEnhancementResponse(enhancedNotes = json)
        }
    }

    fun parseDiffDiagnosis(json: String): DiffDiagnosisResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            DiffDiagnosisResponse(
                possibleDiagnoses = optStringList(obj, "possibleDiagnoses"),
                suggestedInvestigations = optStringList(obj, "suggestedInvestigations"),
                redFlags = optStringList(obj, "redFlags"),
                confidenceLevel = obj.optString("confidenceLevel", "Low"),
                disclaimer = obj.optString("disclaimer", "")
            )
        } catch (e: Exception) {
            DiffDiagnosisResponse(disclaimer = "Parsing error: ${e.message}")
        }
    }

    fun parsePrescriptionDraft(json: String): PrescriptionDraftResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            PrescriptionDraftResponse(
                medicineCategories = optStringList(obj, "medicineCategories"),
                lifestyleAdvice = optStringList(obj, "lifestyleAdvice"),
                followUpRecommendations = obj.optString("followUpRecommendations", "")
            )
        } catch (e: Exception) {
            PrescriptionDraftResponse(followUpRecommendations = "Parse error: ${e.message}")
        }
    }

    fun parseReportInterpretation(json: String): ReportInterpretationResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            ReportInterpretationResponse(
                summary = obj.optString("summary", ""),
                abnormalValues = optStringList(obj, "abnormalValues"),
                criticalValues = optStringList(obj, "criticalValues"),
                recommendations = optStringList(obj, "recommendations"),
                possibleFollowUpTests = optStringList(obj, "possibleFollowUpTests")
            )
        } catch (e: Exception) {
            ReportInterpretationResponse(summary = "Parse error: ${e.message}")
        }
    }

    fun parseRadiologyReport(json: String): RadiologyReportResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            RadiologyReportResponse(
                summary = obj.optString("summary", ""),
                criticalFindings = optStringList(obj, "criticalFindings"),
                recommendations = optStringList(obj, "recommendations")
            )
        } catch (e: Exception) {
            RadiologyReportResponse(summary = "Parse error: ${e.message}")
        }
    }

    fun parsePatientEducation(json: String): PatientEducationResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            PatientEducationResponse(
                instructions = obj.optString("instructions", ""),
                warningSigns = optStringList(obj, "warningSigns")
            )
        } catch (e: Exception) {
            PatientEducationResponse(instructions = "Parse error: ${e.message}")
        }
    }

    fun parseReferralLetter(json: String): ReferralLetterResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            ReferralLetterResponse(
                letter = obj.optString("letter", "")
            )
        } catch (e: Exception) {
            ReferralLetterResponse(letter = "Parse error: ${e.message}")
        }
    }

    fun parseDischargeSummary(json: String): DischargeSummaryResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            DischargeSummaryResponse(
                letter = obj.optString("letter", "")
            )
        } catch (e: Exception) {
            DischargeSummaryResponse(letter = "Parse error: ${e.message}")
        }
    }

    fun parseOperationalInsights(json: String): OperationalInsightsResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            OperationalInsightsResponse(
                summary = obj.optString("summary", ""),
                bottlenecks = optStringList(obj, "bottlenecks"),
                suggestedImprovements = optStringList(obj, "suggestedImprovements"),
                priorityLevel = obj.optString("priorityLevel", "")
            )
        } catch (e: Exception) {
            OperationalInsightsResponse(summary = "Parse error: ${e.message}")
        }
    }

    fun parseResourceOptimization(json: String): ResourceOptimizationResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            ResourceOptimizationResponse(
                summary = obj.optString("summary", ""),
                resourceAllocation = optStringList(obj, "resourceAllocation"),
                capacityPlanning = optStringList(obj, "capacityPlanning")
            )
        } catch (e: Exception) {
            ResourceOptimizationResponse(summary = "Parse error: ${e.message}")
        }
    }

    fun parseStaffAllocation(json: String): StaffAllocationResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            StaffAllocationResponse(
                additionalStaffing = obj.optString("additionalStaffing", ""),
                doctorRedistribution = obj.optString("doctorRedistribution", ""),
                shiftOptimization = obj.optString("shiftOptimization", "")
            )
        } catch (e: Exception) {
            StaffAllocationResponse()
        }
    }

    fun parseEmergencyRisk(json: String): EmergencyRiskResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            EmergencyRiskResponse(
                riskLevel = obj.optString("riskLevel", ""),
                reason = obj.optString("reason", ""),
                recommendations = optStringList(obj, "recommendations"),
                preparednessChecklist = optStringList(obj, "preparednessChecklist")
            )
        } catch (e: Exception) {
            EmergencyRiskResponse()
        }
    }

    fun parsePerformanceReport(json: String): PerformanceReportResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            PerformanceReportResponse(
                patientFlow = obj.optString("patientFlow", ""),
                doctorEfficiency = obj.optString("doctorEfficiency", ""),
                resourceUsage = obj.optString("resourceUsage", ""),
                waitingTime = obj.optString("waitingTime", ""),
                capacity = obj.optString("capacity", ""),
                improvementAreas = optStringList(obj, "improvementAreas")
            )
        } catch (e: Exception) {
            PerformanceReportResponse()
        }
    }

    fun parseDailyBriefing(json: String): DailyBriefingResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            DailyBriefingResponse(
                todaySummary = obj.optString("todaySummary", ""),
                criticalAlerts = optStringList(obj, "criticalAlerts"),
                predictedBusyHours = obj.optString("predictedBusyHours", ""),
                recommendations = optStringList(obj, "recommendations")
            )
        } catch (e: Exception) {
            DailyBriefingResponse(todaySummary = "Failed to load operational briefing.")
        }
    }

    fun parseNotification(json: String): NotificationGeneratorResponse {
        return try {
            val obj = JSONObject(cleanJsonString(json))
            NotificationGeneratorResponse(
                notificationText = obj.optString("notificationText", "")
            )
        } catch (e: Exception) {
            NotificationGeneratorResponse(notificationText = json)
        }
    }
}
