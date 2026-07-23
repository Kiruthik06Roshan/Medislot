package com.medislot.app.data.ai

import com.medislot.app.network.GeminiService
import com.medislot.app.utils.JsonParser

class FallbackCacheException(
    val cachedData: Any,
    val timestamp: Long,
    val originalError: Throwable
) : Exception("Previous AI Recommendation\n\nGenerated earlier", originalError)

class MockFallbackException(
    val mockData: Any,
    val originalError: Throwable
) : Exception("Sample recommendation", originalError)

class GeminiRepositoryImpl(private val service: GeminiService) : GeminiRepository {

    private val aiCache = AiCache(100)
    private val oldCache = mutableMapOf<String, Any>() // keep old for backward safety

    private fun cleanJson(input: String): String {
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

    private suspend inline fun <reified T : Any> getOrFetch(
        cacheKey: String,
        forceRefresh: Boolean = false,
        crossinline fetcher: suspend (String) -> Result<String>,
        crossinline mockResolver: () -> T,
        crossinline parser: (String) -> T
    ): Result<T> {
        val cached = aiCache.get<T>(cacheKey)
        if (cached != null && !forceRefresh) {
            ApiUsageMonitor.trackCacheHit()
            return Result.success(cached.data)
        }

        val models = listOf("gemini-2.5-flash", "gemini-3.5-flash-lite", "gemini-3")
        var lastError: Throwable = Exception("No models attempted")
        val startTime = System.currentTimeMillis()

        for (modelName in models) {
            android.util.Log.d("MediSlotAI", "Attempting model: $modelName")
            ApiUsageMonitor.trackRequest()
            val result = fetcher(modelName)
            if (result.isSuccess) {
                val text = result.getOrThrow()
                try {
                    if (T::class != ChatResponse::class &&
                        T::class != ClinicalEnhancementResponse::class &&
                        T::class != NotificationGeneratorResponse::class) {
                        org.json.JSONObject(cleanJson(text))
                    }
                    val parsed = parser(text)
                    aiCache.put(cacheKey, parsed)
                    oldCache[cacheKey] = parsed
                    ApiUsageMonitor.trackSuccess(System.currentTimeMillis() - startTime)
                    android.util.Log.d("MediSlotAI", "Model succeeded: $modelName")
                    return Result.success(parsed)
                } catch (e: Exception) {
                    ApiUsageMonitor.trackFailure()
                    android.util.Log.e("MediSlotAI", "Model failed (invalid response): $modelName. Parser error: $e", e)
                    lastError = e
                }
            } else {
                ApiUsageMonitor.trackFailure()
                val error = result.exceptionOrNull() ?: Exception("Content fetch failed.")
                android.util.Log.e("MediSlotAI", "Model failed: $modelName. Error: $error", error)
                lastError = error
            }
        }

        android.util.Log.w("MediSlotAI", "Final fallback to mock data")
        return if (cached != null) {
            Result.failure(FallbackCacheException(cached.data, cached.timestamp, lastError))
        } else {
            try {
                Result.failure(MockFallbackException(mockResolver(), lastError))
            } catch (mockError: Throwable) {
                Result.failure(lastError)
            }
        }
    }

    override suspend fun checkSymptoms(symptoms: String, forceRefresh: Boolean): Result<SymptomCheckResponse> {
        val key = "symptoms_$symptoms"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getSymptomCheckerPrompt(symptoms)) },
            { MockAiProvider.checkSymptoms(symptoms) }
        ) {
            JsonParser.parseSymptomCheck(it)
        }
    }

    override suspend fun explainReport(reportText: String, forceRefresh: Boolean): Result<ReportExplanationResponse> {
        val key = "report_${reportText.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getReportExplanationPrompt(reportText)) },
            { MockAiProvider.explainReport(reportText) }
        ) {
            JsonParser.parseReportExplanation(it)
        }
    }

    override suspend fun explainPrescription(prescriptionText: String, forceRefresh: Boolean): Result<PrescriptionExplanationResponse> {
        val key = "prescription_${prescriptionText.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getPrescriptionExplanationPrompt(prescriptionText)) },
            { MockAiProvider.explainPrescription(prescriptionText) }
        ) {
            JsonParser.parsePrescriptionExplanation(it)
        }
    }

    override suspend fun getDailyHealthTips(
        age: Int,
        gender: String,
        history: String,
        lifestyle: String,
        forceRefresh: Boolean
    ): Result<DailyHealthTipsResponse> {
        val key = "tips_${age}_${gender}_${history.hashCode()}_${lifestyle.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getDailyHealthTipsPrompt(age, gender, history, lifestyle)) },
            { MockAiProvider.getDailyHealthTips() }
        ) {
            JsonParser.parseDailyHealthTips(it)
        }
    }

    override suspend fun getDietRecommendation(
        conditions: String,
        weight: String,
        bmi: String,
        forceRefresh: Boolean
    ): Result<DietRecommendationResponse> {
        val key = "diet_${conditions.hashCode()}_${weight}_${bmi}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getDietRecommendationPrompt(conditions, weight, bmi)) },
            { MockAiProvider.getDietRecommendation(conditions, weight, bmi) }
        ) {
            JsonParser.parseDietRecommendation(it)
        }
    }

    override suspend fun getAppointmentPrep(
        doctorName: String,
        specialty: String,
        lastVisitSymptoms: String,
        forceRefresh: Boolean
    ): Result<AppointmentPrepResponse> {
        val key = "prep_${doctorName.hashCode()}_${specialty.hashCode()}_${lastVisitSymptoms.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getAppointmentPrepPrompt(doctorName, specialty, lastVisitSymptoms)) },
            { MockAiProvider.getAppointmentPrep(doctorName, specialty) }
        ) {
            JsonParser.parseAppointmentPrep(it)
        }
    }

    override suspend fun chatWithAssistant(
        chatHistory: String,
        userMessage: String
    ): Result<ChatResponse> {
        val models = listOf("gemini-2.5-flash", "gemini-3.5-flash-lite", "gemini-3")
        var lastError: Throwable = Exception("No models attempted")
        val startTime = System.currentTimeMillis()

        for (modelName in models) {
            android.util.Log.d("MediSlotAI", "Attempting model: $modelName")
            ApiUsageMonitor.trackRequest()
            val result = service.generateContent(modelName, PromptTemplates.getChatAssistantPrompt(chatHistory, userMessage))
            if (result.isSuccess) {
                val text = result.getOrThrow()
                try {
                    val parsed = JsonParser.parseChatAssistant(text)
                    ApiUsageMonitor.trackSuccess(System.currentTimeMillis() - startTime)
                    android.util.Log.d("MediSlotAI", "Model succeeded: $modelName")
                    return Result.success(parsed)
                } catch (e: Exception) {
                    ApiUsageMonitor.trackFailure()
                    android.util.Log.e("MediSlotAI", "Model failed (invalid response): $modelName. Parser error: $e", e)
                    lastError = e
                }
            } else {
                ApiUsageMonitor.trackFailure()
                val error = result.exceptionOrNull() ?: Exception("Chat failed.")
                android.util.Log.e("MediSlotAI", "Model failed: $modelName. Error: $error", error)
                lastError = error
            }
        }

        android.util.Log.w("MediSlotAI", "Final fallback to mock data")
        return try {
            Result.failure(MockFallbackException(MockAiProvider.chatWithAssistant(userMessage), lastError))
        } catch (mockError: Throwable) {
            Result.failure(lastError)
        }
    }

    override suspend fun generateConsultationSummary(rawNotes: String, forceRefresh: Boolean): Result<ConsultationSummaryResponse> {
        val key = "cons_sum_${rawNotes.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getConsultationSummaryPrompt(rawNotes)) },
            { MockAiProvider.generateConsultationSummary(rawNotes) }
        ) {
            JsonParser.parseConsultationSummary(it)
        }
    }

    override suspend fun generateSoapNote(rawNotes: String, forceRefresh: Boolean): Result<SoapNoteResponse> {
        val key = "soap_${rawNotes.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getSoapNotePrompt(rawNotes)) },
            { MockAiProvider.generateSoapNote(rawNotes) }
        ) {
            JsonParser.parseSoapNote(it)
        }
    }

    override suspend fun enhanceClinicalNotes(shorthandNotes: String, forceRefresh: Boolean): Result<ClinicalEnhancementResponse> {
        val key = "enhance_${shorthandNotes.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getClinicalEnhancementPrompt(shorthandNotes)) },
            { MockAiProvider.enhanceClinicalNotes(shorthandNotes) }
        ) {
            JsonParser.parseClinicalEnhancement(it)
        }
    }

    override suspend fun getDifferentialDiagnosis(
        symptoms: String,
        vitals: String,
        history: String,
        forceRefresh: Boolean
    ): Result<DiffDiagnosisResponse> {
        val key = "diff_${symptoms.hashCode()}_${vitals.hashCode()}_${history.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getDiffDiagnosisPrompt(symptoms, vitals, history)) },
            { MockAiProvider.getDifferentialDiagnosis(symptoms) }
        ) {
            JsonParser.parseDiffDiagnosis(it)
        }
    }

    override suspend fun draftPrescription(
        symptoms: String,
        vitals: String,
        history: String,
        forceRefresh: Boolean
    ): Result<PrescriptionDraftResponse> {
        val key = "presc_draft_${symptoms.hashCode()}_${vitals.hashCode()}_${history.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getPrescriptionDraftPrompt(symptoms, vitals, history)) },
            { MockAiProvider.draftPrescription(symptoms) }
        ) {
            JsonParser.parsePrescriptionDraft(it)
        }
    }

    override suspend fun interpretLabReport(testName: String, labText: String, forceRefresh: Boolean): Result<ReportInterpretationResponse> {
        val key = "lab_interp_${testName.hashCode()}_${labText.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getLabReportInterpretationPrompt(testName, labText)) },
            { MockAiProvider.interpretLabReport(testName) }
        ) {
            JsonParser.parseReportInterpretation(it)
        }
    }

    override suspend fun interpretRadiologyReport(
        scanType: String,
        reportFindings: String,
        forceRefresh: Boolean
    ): Result<RadiologyReportResponse> {
        val key = "rad_interp_${scanType.hashCode()}_${reportFindings.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getRadiologyReportPrompt(scanType, reportFindings)) },
            { MockAiProvider.interpretRadiologyReport(scanType) }
        ) {
            JsonParser.parseRadiologyReport(it)
        }
    }

    override suspend fun generatePatientEducation(
        diagnosis: String,
        instructions: String,
        forceRefresh: Boolean
    ): Result<PatientEducationResponse> {
        val key = "education_${diagnosis.hashCode()}_${instructions.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getPatientEducationPrompt(diagnosis, instructions)) },
            { MockAiProvider.generatePatientEducation(diagnosis) }
        ) {
            JsonParser.parsePatientEducation(it)
        }
    }

    override suspend fun generateReferralLetter(
        patientName: String,
        age: Int,
        diagnosis: String,
        summary: String,
        targetSpecialist: String,
        forceRefresh: Boolean
    ): Result<ReferralLetterResponse> {
        val key = "referral_${patientName.hashCode()}_${age}_${diagnosis.hashCode()}_${summary.hashCode()}_${targetSpecialist.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getReferralLetterPrompt(patientName, age, diagnosis, summary, targetSpecialist)) },
            { MockAiProvider.generateReferralLetter(patientName, age, diagnosis) }
        ) {
            JsonParser.parseReferralLetter(it)
        }
    }

    override suspend fun generateDischargeSummary(
        stayDetails: String,
        treatments: String,
        meds: String,
        forceRefresh: Boolean
    ): Result<DischargeSummaryResponse> {
        val key = "discharge_${stayDetails.hashCode()}_${treatments.hashCode()}_${meds.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getDischargeSummaryPrompt(stayDetails, treatments, meds)) },
            { MockAiProvider.generateDischargeSummary(stayDetails) }
        ) {
            JsonParser.parseDischargeSummary(it)
        }
    }

    override suspend fun getOperationalInsights(
        admissions: String,
        waitingTime: String,
        occupancy: String,
        utilization: String,
        forceRefresh: Boolean
    ): Result<OperationalInsightsResponse> {
        val key = "ops_${admissions}_${waitingTime}_${occupancy}_${utilization}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getOperationalInsightsPrompt(admissions, waitingTime, occupancy, utilization)) },
            { MockAiProvider.getOperationalInsights() }
        ) {
            JsonParser.parseOperationalInsights(it)
        }
    }

    override suspend fun optimizeResources(resourcesList: String, forceRefresh: Boolean): Result<ResourceOptimizationResponse> {
        val key = "resources_${resourcesList.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getResourceOptimizationPrompt(resourcesList)) },
            { MockAiProvider.optimizeResources() }
        ) {
            JsonParser.parseResourceOptimization(it)
        }
    }

    override suspend fun suggestStaffAllocation(
        departmentsLoad: String,
        nurseDoctorCount: String,
        forceRefresh: Boolean
    ): Result<StaffAllocationResponse> {
        val key = "staff_${departmentsLoad.hashCode()}_${nurseDoctorCount.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getStaffAllocationPrompt(departmentsLoad, nurseDoctorCount)) },
            { MockAiProvider.suggestStaffAllocation() }
        ) {
            JsonParser.parseStaffAllocation(it)
        }
    }

    override suspend fun predictEmergencyRisk(
        traumaInflow: String,
        bedsFree: String,
        waitingMins: String,
        forceRefresh: Boolean
    ): Result<EmergencyRiskResponse> {
        val key = "er_risk_${traumaInflow.hashCode()}_${bedsFree}_${waitingMins}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getEmergencyRiskPrompt(traumaInflow, bedsFree, waitingMins)) },
            { MockAiProvider.predictEmergencyRisk() }
        ) {
            JsonParser.parseEmergencyRisk(it)
        }
    }

    override suspend fun generatePerformanceReport(
        flow: String,
        efficiency: String,
        waiting: String,
        forceRefresh: Boolean
    ): Result<PerformanceReportResponse> {
        val key = "performance_${flow.hashCode()}_${efficiency.hashCode()}_${waiting.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getPerformanceReportPrompt(flow, efficiency, waiting)) },
            { MockAiProvider.generatePerformanceReport() }
        ) {
            JsonParser.parsePerformanceReport(it)
        }
    }

    override suspend fun getDailyBriefing(metrics: String, activeCriticalAlerts: String, forceRefresh: Boolean): Result<DailyBriefingResponse> {
        val key = "briefing_${metrics.hashCode()}_${activeCriticalAlerts.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getDailyBriefingPrompt(metrics, activeCriticalAlerts)) },
            { MockAiProvider.getDailyBriefing() }
        ) {
            JsonParser.parseDailyBriefing(it)
        }
    }

    override suspend fun generateNotification(operationalAlertText: String, forceRefresh: Boolean): Result<NotificationGeneratorResponse> {
        val key = "notify_${operationalAlertText.hashCode()}"
        return getOrFetch(
            key,
            forceRefresh,
            { model -> service.generateContent(model, PromptTemplates.getNotificationPrompt(operationalAlertText)) },
            { MockAiProvider.generateNotification(operationalAlertText) }
        ) {
            JsonParser.parseNotification(it)
        }
    }

    override fun clearCache() {
        aiCache.clear()
        oldCache.clear()
    }
}
