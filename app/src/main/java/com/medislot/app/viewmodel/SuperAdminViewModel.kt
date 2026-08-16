package com.medislot.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medislot.app.data.model.HospitalApplication
import com.medislot.app.data.model.DoctorApplication
import com.medislot.app.data.repository.SuperAdminRepository
import com.medislot.app.data.repository.SuperAdminRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SuperAdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SuperAdminRepository = SuperAdminRepositoryProvider.repository

    private val _hospitals = MutableStateFlow<List<HospitalApplication>>(emptyList())
    val hospitals: StateFlow<List<HospitalApplication>> = _hospitals.asStateFlow()

    private val _doctors = MutableStateFlow<List<DoctorApplication>>(emptyList())
    val doctors: StateFlow<List<DoctorApplication>> = _doctors.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadHospitals()
        loadDoctors()
    }

    fun loadHospitals() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.getHospitals()
                .onSuccess {
                    _hospitals.value = it
                }
                .onFailure {
                    _hospitals.value = emptyList()
                    _errorMessage.value = it.localizedMessage ?: "Failed to load hospitals"
                }
            _isLoading.value = false
        }
    }

    fun loadDoctors() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.getDoctors()
                .onSuccess {
                    _doctors.value = it
                }
                .onFailure {
                    _doctors.value = emptyList()
                    _errorMessage.value = it.localizedMessage ?: "Failed to load doctors"
                }
            _isLoading.value = false
        }
    }

    fun approveHospital(hospId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.approveHospital(hospId)
                .onSuccess {
                    loadHospitals()
                    onSuccess()
                }
                .onFailure {
                    onFailure(it.localizedMessage ?: "Failed to approve hospital")
                }
            _isLoading.value = false
        }
    }

    fun rejectHospital(hospId: String, reason: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.rejectHospital(hospId, reason)
                .onSuccess {
                    loadHospitals()
                    onSuccess()
                }
                .onFailure {
                    onFailure(it.localizedMessage ?: "Failed to reject hospital")
                }
            _isLoading.value = false
        }
    }

    fun approveDoctor(appId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.approveDoctor(appId)
                .onSuccess {
                    loadDoctors()
                    onSuccess()
                }
                .onFailure {
                    onFailure(it.localizedMessage ?: "Failed to approve doctor")
                }
            _isLoading.value = false
        }
    }

    fun rejectDoctor(appId: String, reason: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.rejectDoctor(appId, reason)
                .onSuccess {
                    loadDoctors()
                    onSuccess()
                }
                .onFailure {
                    onFailure(it.localizedMessage ?: "Failed to reject doctor")
                }
            _isLoading.value = false
        }
    }
}
