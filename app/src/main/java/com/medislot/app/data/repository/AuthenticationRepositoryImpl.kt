package com.medislot.app.data.repository

import com.medislot.app.data.local.DatabaseProvider
import com.medislot.app.network.RetrofitClient
import com.medislot.app.network.LoginRequest
import com.medislot.app.network.RegisterRequest
import com.medislot.app.network.TokenResponse
import kotlinx.coroutines.flow.firstOrNull

class AuthenticationRepositoryImpl : AuthenticationRepository {

    private val dataStore = DatabaseProvider.getDataStoreManager()

    override suspend fun login(email: String, password: String): Result<TokenResponse> {
        return try {
            val response = RetrofitClient.apiService.login(LoginRequest(email.trim(), password))
            dataStore.saveTokens(response.access_token, response.refresh_token)
            dataStore.saveUserSession(response.uid, response.role, response.email)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, fullName: String, role: String): Result<TokenResponse> {
        return try {
            val response = RetrofitClient.apiService.register(RegisterRequest(email.trim(), password, fullName.trim(), role))
            dataStore.saveTokens(response.access_token, response.refresh_token)
            dataStore.saveUserSession(response.uid, response.role, response.email)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            dataStore.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        val token = dataStore.accessTokenFlow.firstOrNull()
        return !token.isNullOrEmpty()
    }

    override suspend fun getAccessToken(): String? {
        return dataStore.accessTokenFlow.firstOrNull()
    }

    override suspend fun getRole(): String? {
        return dataStore.roleFlow.firstOrNull() ?: "patient"
    }

    override suspend fun getUid(): String? {
        return dataStore.uidFlow.firstOrNull()
    }

    override suspend fun getEmail(): String? {
        return dataStore.emailFlow.firstOrNull()
    }
}
