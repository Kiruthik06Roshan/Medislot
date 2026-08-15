package com.medislot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medislot.app.data.repository.DoctorRepository
import com.medislot.app.data.repository.DoctorRepositoryImpl
import com.medislot.app.network.DoctorProfileResponse
import com.medislot.app.network.AppointmentResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DoctorUiState {
    object Loading : DoctorUiState()
    data class Success(val profile: DoctorProfileResponse) : DoctorUiState()
    data class Error(val message: String) : DoctorUiState()
}

class DoctorViewModel(
    private val repository: DoctorRepository = DoctorRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DoctorUiState>(DoctorUiState.Loading)
    val uiState: StateFlow<DoctorUiState> = _uiState.asStateFlow()

    private val _queue = MutableStateFlow<List<AppointmentResponse>>(emptyList())
    val queue: StateFlow<List<AppointmentResponse>> = _queue.asStateFlow()

    private val _doctors = MutableStateFlow<List<DoctorProfileResponse>>(emptyList())
    val doctors: StateFlow<List<DoctorProfileResponse>> = _doctors.asStateFlow()

    fun loadProfile(uid: String) {
        viewModelScope.launch {
            _uiState.value = DoctorUiState.Loading
            val result = repository.getProfile(uid)
            result.fold(
                onSuccess = { _uiState.value = DoctorUiState.Success(it) },
                onFailure = { _uiState.value = DoctorUiState.Error(it.message ?: "Failed to load doctor profile") }
            )
        }
    }

    fun loadQueue(doctorId: String) {
        viewModelScope.launch {
            val result = repository.getAppointments(doctorId)
            result.fold(
                onSuccess = { _queue.value = it },
                onFailure = { /* Fail silent */ }
            )
        }
    }

    fun loadAllDoctors() {
        viewModelScope.launch {
            val result = repository.getAllDoctors()
            result.fold(
                onSuccess = { _doctors.value = it },
                onFailure = { /* Fail silent */ }
            )
        }
    }
}
