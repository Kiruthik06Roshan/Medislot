package com.medislot.app.services.firebase

interface FirebaseService {
    suspend fun getUserId(): String?
    suspend fun isUserLoggedIn(): Boolean
    suspend fun logout(): Result<Unit>
}
