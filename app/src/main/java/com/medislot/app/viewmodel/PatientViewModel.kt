package com.medislot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medislot.app.data.repository.PatientRepository
import com.medislot.app.data.repository.PatientRepositoryImpl
import com.medislot.app.network.PatientProfileResponse
import com.medislot.app.network.AppointmentResponse
import com.medislot.app.network.MedicalRecordResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PatientUiState {
    object Loading : PatientUiState()
    data class Success(val profile: PatientProfileResponse) : PatientUiState()
    data class Error(val message: String) : PatientUiState()
}

class PatientViewModel(
    private val repository: PatientRepository = PatientRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientUiState>(PatientUiState.Loading)
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    private val _appointments = MutableStateFlow<List<AppointmentResponse>>(emptyList())
    val appointments: StateFlow<List<AppointmentResponse>> = _appointments.asStateFlow()

    private val _medicalRecords = MutableStateFlow<List<MedicalRecordResponse>>(emptyList())
    val medicalRecords: StateFlow<List<MedicalRecordResponse>> = _medicalRecords.asStateFlow()

    fun loadProfile(uid: String) {
        viewModelScope.launch {
            _uiState.value = PatientUiState.Loading
            val result = repository.getProfile(uid)
            result.fold(
                onSuccess = { _uiState.value = PatientUiState.Success(it) },
                onFailure = { _uiState.value = PatientUiState.Error(it.message ?: "Failed to load profile") }
            )
        }
    }

    fun loadAppointments(patientId: String) {
        viewModelScope.launch {
            val result = repository.getAppointments(patientId)
            result.fold(
                onSuccess = { _appointments.value = it },
                onFailure = { /* Fail silent */ }
            )
        }
    }

    fun loadMedicalRecords(patientId: String) {
        viewModelScope.launch {
            val result = repository.getMedicalRecords(patientId)
            result.fold(
                onSuccess = { _medicalRecords.value = it },
                onFailure = { /* Fail silent */ }
            )
        }
    }
}
