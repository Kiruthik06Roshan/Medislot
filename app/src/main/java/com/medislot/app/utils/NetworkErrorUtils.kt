package com.medislot.app.utils

import com.medislot.app.BuildConfig
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorUtils {
    fun getReadableErrorMessage(throwable: Throwable?): String {
        if (throwable == null) return "An unknown error occurred."

        val targetUrl = try { BuildConfig.BASE_URL } catch (_: Exception) { "MediSlot server" }

        return when (throwable) {
            is ConnectException -> {
                "Cannot connect to MediSlot server at $targetUrl. Please verify the server is running and accessible."
            }
            is SocketTimeoutException -> {
                "Connection to MediSlot server timed out ($targetUrl). Please check network stability."
            }
            is UnknownHostException -> {
                "MediSlot server host not found ($targetUrl). Please check your network connection."
            }
            is HttpException -> {
                val code = throwable.code()
                when (code) {
                    401 -> "Invalid credentials (HTTP 401 Unauthorized)."
                    403 -> "Access denied (HTTP 403 Forbidden)."
                    404 -> "Requested endpoint not found (HTTP 404)."
                    409 -> "Resource Conflict (HTTP 409). User or record already exists."
                    422 -> "Validation Error (HTTP 422). Please check your input fields."
                    500 -> "Internal Server Error (HTTP 500). Please try again later."
                    else -> "Server returned error (HTTP $code)."
                }
            }
            else -> {
                val msg = throwable.localizedMessage ?: throwable.message ?: ""
                if (msg.contains("failed to connect", ignoreCase = true) || msg.contains("ConnectException", ignoreCase = true)) {
                    "Cannot connect to MediSlot server at $targetUrl."
                } else if (msg.isNotBlank()) {
                    msg
                } else {
                    "An unexpected network error occurred."
                }
            }
        }
    }
}
