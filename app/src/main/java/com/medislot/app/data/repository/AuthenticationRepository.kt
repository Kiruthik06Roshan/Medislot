package com.medislot.app.data.repository

import com.medislot.app.network.TokenResponse

interface AuthenticationRepository {
    suspend fun login(email: String, password: String): Result<TokenResponse>
    suspend fun register(email: String, password: String, fullName: String, role: String): Result<TokenResponse>
    suspend fun logout(): Result<Unit>
    suspend fun isLoggedIn(): Boolean
    suspend fun getAccessToken(): String?
    suspend fun getRole(): String?
    suspend fun getUid(): String?
    suspend fun getEmail(): String?
}
