package com.medislot.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medislot.app.data.repository.AuthenticationRepository
import com.medislot.app.data.repository.AuthenticationRepositoryImpl
import com.medislot.app.network.TokenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: TokenResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthenticationViewModel(
    private val repository: AuthenticationRepository = AuthenticationRepositoryImpl()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { response -> _authState.value = AuthState.Success(response) },
                onFailure = { error -> _authState.value = AuthState.Error(error.message ?: "Authentication failed") }
            )
        }
    }

    fun register(email: String, password: String, fullName: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(email, password, fullName, role)
            result.fold(
                onSuccess = { response -> _authState.value = AuthState.Success(response) },
                onFailure = { error -> _authState.value = AuthState.Error(error.message ?: "Registration failed") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthState.Idle
        }
    }
}
