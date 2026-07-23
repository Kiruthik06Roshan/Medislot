package com.medislot.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medislot.app.data.ai.*
import com.medislot.app.network.GeminiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AiState<out T> {
    object Idle : AiState<Nothing>
    object Loading : AiState<Nothing>
    data class Success<out T>(val data: T, val isFallback: Boolean = false, val timestamp: Long = 0L, val isMock: Boolean = false) : AiState<T>
    data class Failure(val error: String) : AiState<Nothing>
}

enum class AiStatus {
    ONLINE, BUSY, UNAVAILABLE
}

data class ChatMessage(val text: String, val isUser: Boolean)

class AiViewModel(application: Application) : AndroidViewModel(application) {

    private val service = GeminiService(application)
    private val repository: GeminiRepository = GeminiRepositoryImpl(service)

    // State flows for each feature
    private val _symptomCheckState = MutableStateFlow<AiState<SymptomCheckResponse>>(AiState.Idle)
    val symptomCheckState: StateFlow<AiState<SymptomCheckResponse>> = _symptomCheckState.asStateFlow()

    private val _reportExplanationState = MutableStateFlow<AiState<ReportExplanationResponse>>(AiState.Idle)
    val reportExplanationState: StateFlow<AiState<ReportExplanationResponse>> = _reportExplanationState.asStateFlow()

    private val _prescriptionExplanationState = MutableStateFlow<AiState<PrescriptionExplanationResponse>>(AiState.Idle)
    val prescriptionExplanationState: StateFlow<AiState<PrescriptionExplanationResponse>> = _prescriptionExplanationState.asStateFlow()

    private val _dailyTipsState = MutableStateFlow<AiState<DailyHealthTipsResponse>>(AiState.Idle)
    val dailyTipsState: StateFlow<AiState<DailyHealthTipsResponse>> = _dailyTipsState.asStateFlow()

    private val _dietRecommendationState = MutableStateFlow<AiState<DietRecommendationResponse>>(AiState.Idle)
    val dietRecommendationState: StateFlow<AiState<DietRecommendationResponse>> = _dietRecommendationState.asStateFlow()

    private val _appointmentPrepState = MutableStateFlow<AiState<AppointmentPrepResponse>>(AiState.Idle)
    val appointmentPrepState: StateFlow<AiState<AppointmentPrepResponse>> = _appointmentPrepState.asStateFlow()

    private val _chatState = MutableStateFlow<AiState<ChatResponse>>(AiState.Idle)
    val chatState: StateFlow<AiState<ChatResponse>> = _chatState.asStateFlow()

    // Doctor dashboard states
    private val _consultationSummaryState = MutableStateFlow<AiState<ConsultationSummaryResponse>>(AiState.Idle)
    val consultationSummaryState: StateFlow<AiState<ConsultationSummaryResponse>> = _consultationSummaryState.asStateFlow()

    private val _soapNoteState = MutableStateFlow<AiState<SoapNoteResponse>>(AiState.Idle)
    val soapNoteState: StateFlow<AiState<SoapNoteResponse>> = _soapNoteState.asStateFlow()

    private val _clinicalEnhancementState = MutableStateFlow<AiState<ClinicalEnhancementResponse>>(AiState.Idle)
    val clinicalEnhancementState: StateFlow<AiState<ClinicalEnhancementResponse>> = _clinicalEnhancementState.asStateFlow()

    private val _diffDiagnosisState = MutableStateFlow<AiState<DiffDiagnosisResponse>>(AiState.Idle)
    val diffDiagnosisState: StateFlow<AiState<DiffDiagnosisResponse>> = _diffDiagnosisState.asStateFlow()

    private val _prescriptionDraftState = MutableStateFlow<AiState<PrescriptionDraftResponse>>(AiState.Idle)
    val prescriptionDraftState: StateFlow<AiState<PrescriptionDraftResponse>> = _prescriptionDraftState.asStateFlow()

    private val _labInterpretationState = MutableStateFlow<AiState<ReportInterpretationResponse>>(AiState.Idle)
    val labInterpretationState: StateFlow<AiState<ReportInterpretationResponse>> = _labInterpretationState.asStateFlow()

    private val _radiologyInterpretationState = MutableStateFlow<AiState<RadiologyReportResponse>>(AiState.Idle)
    val radiologyInterpretationState: StateFlow<AiState<RadiologyReportResponse>> = _radiologyInterpretationState.asStateFlow()

    private val _patientEducationState = MutableStateFlow<AiState<PatientEducationResponse>>(AiState.Idle)
    val patientEducationState: StateFlow<AiState<PatientEducationResponse>> = _patientEducationState.asStateFlow()

    private val _referralLetterState = MutableStateFlow<AiState<ReferralLetterResponse>>(AiState.Idle)
    val referralLetterState: StateFlow<AiState<ReferralLetterResponse>> = _referralLetterState.asStateFlow()

    private val _dischargeSummaryState = MutableStateFlow<AiState<DischargeSummaryResponse>>(AiState.Idle)
    val dischargeSummaryState: StateFlow<AiState<DischargeSummaryResponse>> = _dischargeSummaryState.asStateFlow()

    // Admin dashboard states
    private val _operationalInsightsState = MutableStateFlow<AiState<OperationalInsightsResponse>>(AiState.Idle)
    val operationalInsightsState: StateFlow<AiState<OperationalInsightsResponse>> = _operationalInsightsState.asStateFlow()

    private val _resourceOptimizationState = MutableStateFlow<AiState<ResourceOptimizationResponse>>(AiState.Idle)
    val resourceOptimizationState: StateFlow<AiState<ResourceOptimizationResponse>> = _resourceOptimizationState.asStateFlow()

    private val _staffAllocationState = MutableStateFlow<AiState<StaffAllocationResponse>>(AiState.Idle)
    val staffAllocationState: StateFlow<AiState<StaffAllocationResponse>> = _staffAllocationState.asStateFlow()

    private val _emergencyRiskState = MutableStateFlow<AiState<EmergencyRiskResponse>>(AiState.Idle)
    val emergencyRiskState: StateFlow<AiState<EmergencyRiskResponse>> = _emergencyRiskState.asStateFlow()

    private val _performanceReportState = MutableStateFlow<AiState<PerformanceReportResponse>>(AiState.Idle)
    val performanceReportState: StateFlow<AiState<PerformanceReportResponse>> = _performanceReportState.asStateFlow()

    private val _dailyBriefingState = MutableStateFlow<AiState<DailyBriefingResponse>>(AiState.Idle)
    val dailyBriefingState: StateFlow<AiState<DailyBriefingResponse>> = _dailyBriefingState.asStateFlow()

    private val _notificationState = MutableStateFlow<AiState<NotificationGeneratorResponse>>(AiState.Idle)
    val notificationState: StateFlow<AiState<NotificationGeneratorResponse>> = _notificationState.asStateFlow()

    // Chat Conversation Memory
    val chatHistory = mutableStateListOf<ChatMessage>()

    // Request Jobs for cancellation and duplicate protection
    private var symptomCheckJob: Job? = null
    private var reportExplanationJob: Job? = null
    private var prescriptionExplanationJob: Job? = null
    private var dailyTipsJob: Job? = null
    private var dietJob: Job? = null
    private var prepJob: Job? = null
    private var chatJob: Job? = null
    private var docSummaryJob: Job? = null
    private var soapNoteJob: Job? = null
    private var docEnhanceJob: Job? = null
    private var diffDiagnosisJob: Job? = null
    private var draftPrescJob: Job? = null
    private var labInterpJob: Job? = null
    private var radInterpJob: Job? = null
    private var patEduJob: Job? = null
    private var refLetterJob: Job? = null
    private var dischargeSummaryJob: Job? = null
    private var opInsightsJob: Job? = null
    private var resourceOptJob: Job? = null
    private var staffAllocJob: Job? = null
    private var erRiskJob: Job? = null
    private var perfReportJob: Job? = null
    private var briefingJob: Job? = null
    private var notificationJob: Job? = null

    // Support snackbar message stream
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    // Status tracking states
    private val _activeRequestsCount = MutableStateFlow(0)
    private val _isOnlineFlow = MutableStateFlow(true)
    private var isConfigValid = true

    val aiStatus: StateFlow<AiStatus> = combine(
        _activeRequestsCount,
        _isOnlineFlow
    ) { activeCount, isOnline ->
        when {
            !isOnline || !isConfigValid -> AiStatus.UNAVAILABLE
            activeCount > 0 -> AiStatus.BUSY
            else -> AiStatus.ONLINE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiStatus.ONLINE
    )

    private val lastRequestTimes = mutableMapOf<String, Long>()
    private val retryCounts = mutableMapOf<String, Int>()
    private var wasOffline = false

    init {
        wasOffline = !service.isOnline()
        _isOnlineFlow.value = service.isOnline()
        monitorNetwork()
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    private fun monitorNetwork() {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm != null) {
            val request = android.net.NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            try {
                cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        _isOnlineFlow.value = true
                        if (wasOffline) {
                            showSnackbar("Internet connection restored.")
                            wasOffline = false
                        }
                    }
                    override fun onLost(network: android.net.Network) {
                        _isOnlineFlow.value = false
                        wasOffline = true
                    }
                })
            } catch (e: Exception) {
                // fallback if callback registration fails
            }
        }
    }

    private fun <T> executeAiTask(
        featureName: String,
        stateFlow: MutableStateFlow<AiState<T>>,
        jobGetter: () -> Job?,
        jobSetter: (Job?) -> Unit,
        forceRefresh: Boolean = false,
        task: suspend (Boolean) -> Result<T>
    ) {
        // Duplicate request protection: check if job is active
        val currentJob = jobGetter()
        if (currentJob?.isActive == true) {
            return
        }

        // Throttling: Max 1 request every 2 seconds per feature
        val now = System.currentTimeMillis()
        val lastTime = lastRequestTimes[featureName] ?: 0L
        if (now - lastTime < 2000L && !forceRefresh) {
            showSnackbar("Waiting before retrying AI request.")
            return
        }
        lastRequestTimes[featureName] = now

        // Reset retry counters on user interaction
        if (!forceRefresh) {
            retryCounts[featureName] = 0
        }

        stateFlow.value = AiState.Loading
        _activeRequestsCount.value = _activeRequestsCount.value + 1

        val job = viewModelScope.launch {
            try {
                val result = task(forceRefresh)
                if (result.isSuccess) {
                    stateFlow.value = AiState.Success(result.getOrThrow())
                    showSnackbar("AI response generated successfully.")
                } else {
                    val error = result.exceptionOrNull() ?: Exception("Unknown error")
                    handleFailure(error, featureName, stateFlow, task)
                }
            } finally {
                _activeRequestsCount.value = (_activeRequestsCount.value - 1).coerceAtLeast(0)
            }
        }
        jobSetter(job)
    }

    private fun <T> handleFailure(
        error: Throwable,
        featureName: String,
        stateFlow: MutableStateFlow<AiState<T>>,
        task: suspend (Boolean) -> Result<T>
    ) {
        if (error is FallbackCacheException) {
            @Suppress("UNCHECKED_CAST")
            stateFlow.value = AiState.Success(error.cachedData as T, isFallback = true, timestamp = error.timestamp)
            showSnackbar("Using cached recommendation.")
            return
        }

        if (error is MockFallbackException) {
            @Suppress("UNCHECKED_CAST")
            stateFlow.value = AiState.Success(error.mockData as T, isMock = true)
            showSnackbar("Displaying sample recommendation.")
            return
        }

        val friendly = GeminiErrorMapper.map(error, featureName)
        
        if (friendly.title == "AI Service Configuration Error") {
            isConfigValid = false
        }

        val retryAfter = friendly.retryAfterSeconds
        val retries = retryCounts[featureName] ?: 0

        if (retryAfter != null && retries == 0) {
            retryCounts[featureName] = 1
            viewModelScope.launch {
                showSnackbar("Waiting before retrying AI request.")
                for (sec in retryAfter downTo 1) {
                    stateFlow.value = AiState.Failure("Please wait\n\n$sec seconds\n\nbefore trying again.")
                    delay(1000L)
                }
                stateFlow.value = AiState.Loading
                
                val retryResult = task(true)
                if (retryResult.isSuccess) {
                    stateFlow.value = AiState.Success(retryResult.getOrThrow())
                    showSnackbar("AI response generated successfully.")
                } else {
                    val retryErr = retryResult.exceptionOrNull() ?: Exception("Retry failed")
                    if (retryErr is MockFallbackException) {
                        @Suppress("UNCHECKED_CAST")
                        stateFlow.value = AiState.Success(retryErr.mockData as T, isMock = true)
                        showSnackbar("Displaying sample recommendation.")
                    } else {
                        val retryFriendly = GeminiErrorMapper.map(retryErr, featureName)
                        stateFlow.value = AiState.Failure(retryFriendly.title + "\n\n" + retryFriendly.message)
                    }
                }
            }
        } else {
            stateFlow.value = AiState.Failure(friendly.title + "\n\n" + friendly.message)
        }
    }

    // Patient Actions
    fun checkSymptoms(symptoms: String, forceRefresh: Boolean = false) {
        executeAiTask("checkSymptoms", _symptomCheckState, { symptomCheckJob }, { symptomCheckJob = it }, forceRefresh) {
            repository.checkSymptoms(symptoms, it)
        }
    }

    fun explainReport(reportText: String, forceRefresh: Boolean = false) {
        executeAiTask("explainReport", _reportExplanationState, { reportExplanationJob }, { reportExplanationJob = it }, forceRefresh) {
            repository.explainReport(reportText, it)
        }
    }

    fun explainPrescription(prescriptionText: String, forceRefresh: Boolean = false) {
        executeAiTask("explainPrescription", _prescriptionExplanationState, { prescriptionExplanationJob }, { prescriptionExplanationJob = it }, forceRefresh) {
            repository.explainPrescription(prescriptionText, it)
        }
    }

    fun loadDailyHealthTips(age: Int, gender: String, history: String, lifestyle: String, forceRefresh: Boolean = false) {
        executeAiTask("loadDailyHealthTips", _dailyTipsState, { dailyTipsJob }, { dailyTipsJob = it }, forceRefresh) {
            repository.getDailyHealthTips(age, gender, history, lifestyle, it)
        }
    }

    fun loadDietRecommendations(conditions: String, weight: String, bmi: String, forceRefresh: Boolean = false) {
        executeAiTask("loadDietRecommendations", _dietRecommendationState, { dietJob }, { dietJob = it }, forceRefresh) {
            repository.getDietRecommendation(conditions, weight, bmi, it)
        }
    }

    fun loadAppointmentPrep(doctorName: String, specialty: String, lastVisitSymptoms: String, forceRefresh: Boolean = false) {
        executeAiTask("loadAppointmentPrep", _appointmentPrepState, { prepJob }, { prepJob = it }, forceRefresh) {
            repository.getAppointmentPrep(doctorName, specialty, lastVisitSymptoms, it)
        }
    }

    fun sendChatMessage(message: String) {
        if (message.isBlank()) return
        if (chatJob?.isActive == true) return

        chatHistory.add(ChatMessage(message, true))
        _chatState.value = AiState.Loading
        _activeRequestsCount.value = _activeRequestsCount.value + 1

        val historyStr = chatHistory.joinToString("\n") {
            if (it.isUser) "Patient: ${it.text}" else "Assistant: ${it.text}"
        }

        chatJob = viewModelScope.launch {
            try {
                val result = repository.chatWithAssistant(historyStr, message)
                if (result.isSuccess) {
                    val data = result.getOrThrow()
                    chatHistory.add(ChatMessage(data.reply, false))
                    _chatState.value = AiState.Success(data)
                } else {
                    val error = result.exceptionOrNull() ?: Exception("Chat failure")
                    if (error is MockFallbackException) {
                        val data = error.mockData as ChatResponse
                        chatHistory.add(ChatMessage(data.reply, false))
                        _chatState.value = AiState.Success(data, isMock = true)
                        showSnackbar("Displaying sample recommendation.")
                    } else {
                        val friendly = GeminiErrorMapper.map(error, "chatWithAssistant")
                        _chatState.value = AiState.Failure(friendly.title + "\n\n" + friendly.message)
                    }
                }
            } finally {
                _activeRequestsCount.value = (_activeRequestsCount.value - 1).coerceAtLeast(0)
            }
        }
    }

    fun clearChat() {
        chatHistory.clear()
        _chatState.value = AiState.Idle
    }

    fun exportChat(): String {
        return chatHistory.joinToString("\n\n") {
            val role = if (it.isUser) "Patient" else "AI Chat Assistant"
            "[$role]\n${it.text}"
        }
    }

    // Doctor Actions
    fun generateConsultationSummary(rawNotes: String, forceRefresh: Boolean = false) {
        executeAiTask("generateConsultationSummary", _consultationSummaryState, { docSummaryJob }, { docSummaryJob = it }, forceRefresh) {
            repository.generateConsultationSummary(rawNotes, it)
        }
    }

    fun generateSoapNote(rawNotes: String, forceRefresh: Boolean = false) {
        executeAiTask("generateSoapNote", _soapNoteState, { soapNoteJob }, { soapNoteJob = it }, forceRefresh) {
            repository.generateSoapNote(rawNotes, it)
        }
    }

    fun enhanceClinicalNotes(shorthandNotes: String, forceRefresh: Boolean = false) {
        executeAiTask("enhanceClinicalNotes", _clinicalEnhancementState, { docEnhanceJob }, { docEnhanceJob = it }, forceRefresh) {
            repository.enhanceClinicalNotes(shorthandNotes, it)
        }
    }

    fun loadDifferentialDiagnosis(symptoms: String, vitals: String, history: String, forceRefresh: Boolean = false) {
        executeAiTask("loadDifferentialDiagnosis", _diffDiagnosisState, { diffDiagnosisJob }, { diffDiagnosisJob = it }, forceRefresh) {
            repository.getDifferentialDiagnosis(symptoms, vitals, history, it)
        }
    }

    fun loadPrescriptionDraft(symptoms: String, vitals: String, history: String, forceRefresh: Boolean = false) {
        executeAiTask("loadPrescriptionDraft", _prescriptionDraftState, { draftPrescJob }, { draftPrescJob = it }, forceRefresh) {
            repository.draftPrescription(symptoms, vitals, history, it)
        }
    }

    fun interpretLabReport(testName: String, labText: String, forceRefresh: Boolean = false) {
        executeAiTask("interpretLabReport", _labInterpretationState, { labInterpJob }, { labInterpJob = it }, forceRefresh) {
            repository.interpretLabReport(testName, labText, it)
        }
    }

    fun interpretRadiologyReport(scanType: String, reportFindings: String, forceRefresh: Boolean = false) {
        executeAiTask("interpretRadiologyReport", _radiologyInterpretationState, { radInterpJob }, { radInterpJob = it }, forceRefresh) {
            repository.interpretRadiologyReport(scanType, reportFindings, it)
        }
    }

    fun generatePatientEducation(diagnosis: String, instructions: String, forceRefresh: Boolean = false) {
        executeAiTask("generatePatientEducation", _patientEducationState, { patEduJob }, { patEduJob = it }, forceRefresh) {
            repository.generatePatientEducation(diagnosis, instructions, it)
        }
    }

    fun generateReferralLetter(patientName: String, age: Int, diagnosis: String, summary: String, targetSpecialist: String, forceRefresh: Boolean = false) {
        executeAiTask("generateReferralLetter", _referralLetterState, { refLetterJob }, { refLetterJob = it }, forceRefresh) {
            repository.generateReferralLetter(patientName, age, diagnosis, summary, targetSpecialist, it)
        }
    }

    fun generateDischargeSummary(stayDetails: String, treatments: String, meds: String, forceRefresh: Boolean = false) {
        executeAiTask("generateDischargeSummary", _dischargeSummaryState, { dischargeSummaryJob }, { dischargeSummaryJob = it }, forceRefresh) {
            repository.generateDischargeSummary(stayDetails, treatments, meds, it)
        }
    }

    // Admin Actions
    fun loadOperationalInsights(admissions: String, waitingTime: String, occupancy: String, utilization: String, forceRefresh: Boolean = false) {
        executeAiTask("loadOperationalInsights", _operationalInsightsState, { opInsightsJob }, { opInsightsJob = it }, forceRefresh) {
            repository.getOperationalInsights(admissions, waitingTime, occupancy, utilization, it)
        }
    }

    fun loadResourceOptimization(resourcesList: String, forceRefresh: Boolean = false) {
        executeAiTask("loadResourceOptimization", _resourceOptimizationState, { resourceOptJob }, { resourceOptJob = it }, forceRefresh) {
            repository.optimizeResources(resourcesList, it)
        }
    }

    fun loadStaffAllocation(departmentsLoad: String, nurseDoctorCount: String, forceRefresh: Boolean = false) {
        executeAiTask("loadStaffAllocation", _staffAllocationState, { staffAllocJob }, { staffAllocJob = it }, forceRefresh) {
            repository.suggestStaffAllocation(departmentsLoad, nurseDoctorCount, it)
        }
    }

    fun predictEmergencyRisk(traumaInflow: String, bedsFree: String, waitingMins: String, forceRefresh: Boolean = false) {
        executeAiTask("predictEmergencyRisk", _emergencyRiskState, { erRiskJob }, { erRiskJob = it }, forceRefresh) {
            repository.predictEmergencyRisk(traumaInflow, bedsFree, waitingMins, it)
        }
    }

    fun loadPerformanceReport(flow: String, efficiency: String, waiting: String, forceRefresh: Boolean = false) {
        executeAiTask("loadPerformanceReport", _performanceReportState, { perfReportJob }, { perfReportJob = it }, forceRefresh) {
            repository.generatePerformanceReport(flow, efficiency, waiting, it)
        }
    }

    fun loadDailyBriefing(metrics: String, activeCriticalAlerts: String, forceRefresh: Boolean = false) {
        executeAiTask("loadDailyBriefing", _dailyBriefingState, { briefingJob }, { briefingJob = it }, forceRefresh) {
            repository.getDailyBriefing(metrics, activeCriticalAlerts, it)
        }
    }

    fun generateNotificationAlert(operationalAlertText: String, forceRefresh: Boolean = false) {
        executeAiTask("generateNotificationAlert", _notificationState, { notificationJob }, { notificationJob = it }, forceRefresh) {
            repository.generateNotification(operationalAlertText, it)
        }
    }

    fun clearCache() {
        repository.clearCache()
        showSnackbar("AI Cache cleared.")
    }
}
