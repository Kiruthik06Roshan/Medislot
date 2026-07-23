package com.medislot.app.data.ai

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserFriendlyError(
    val title: String,
    val message: String,
    val retryAfterSeconds: Int? = null
)

object GeminiErrorMapper {
    fun map(error: Throwable, featureName: String): UserFriendlyError {
        val msg = error.message ?: ""
        val retryAfter = parseRetryAfter(msg)

        val friendlyError = when {
            // Quota / Rate limit (429)
            msg.contains("429") || 
            msg.contains("ResourceExhausted") || 
            msg.contains("RESOURCE_EXHAUSTED") || 
            msg.contains("Quota exceeded") || 
            msg.contains("rate limit", ignoreCase = true) || 
            msg.contains("limit exceeded", ignoreCase = true) -> {
                UserFriendlyError(
                    title = "AI Service Temporarily Busy",
                    message = "The AI assistant has reached its current usage limit.\n\nPlease try again in a few moments.\n\nYour medical information is safe and no request has been lost.",
                    retryAfterSeconds = retryAfter
                )
            }
            // Invalid API key (403, 401)
            msg.contains("403") || 
            msg.contains("401") || 
            msg.contains("PERMISSION_DENIED") || 
            msg.contains("API key", ignoreCase = true) || 
            msg.contains("API_KEY", ignoreCase = true) || 
            msg.contains("Invalid API Key") || 
            msg.contains("mock_api_key") -> {
                UserFriendlyError(
                    title = "AI Service Configuration Error",
                    message = "Please contact the administrator."
                )
            }
            // Timeout (408)
            msg.contains("408") || 
            msg.contains("Timeout") || 
            msg.contains("timeout", ignoreCase = true) || 
            error is kotlinx.coroutines.TimeoutCancellationException || 
            error is java.net.SocketTimeoutException -> {
                UserFriendlyError(
                    title = "Connection Timeout",
                    message = "The AI assistant is taking longer than expected.\n\nPlease retry."
                )
            }
            // Offline / Network Error
            msg.contains("Offline") || 
            msg.contains("offline", ignoreCase = true) || 
            msg.contains("internet", ignoreCase = true) || 
            error is java.io.IOException || 
            error is java.net.UnknownHostException || 
            error is java.net.ConnectException -> {
                UserFriendlyError(
                    title = "No Internet Connection",
                    message = "Please check your internet connection and try again."
                )
            }
            // Server Error (500, 503)
            msg.contains("500") || 
            msg.contains("503") || 
            msg.contains("Internal server error") || 
            msg.contains("Unavailable", ignoreCase = true) -> {
                UserFriendlyError(
                    title = "AI Service Temporarily Unavailable",
                    message = "Please try again later."
                )
            }
            else -> {
                UserFriendlyError(
                    title = "AI Service Temporarily Unavailable",
                    message = "Please try again later."
                )
            }
        }

        // Log technical details safely to Logcat (excluding patient sensitive details)
        val httpCode = extractHttpCode(msg) ?: "N/A"
        Log.e("MediSlotAI", """
            Gemini Error
            HTTP Code: $httpCode
            Exception: ${error.javaClass.name}
            Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}
            Prompt Type: $featureName
        """.trimIndent())

        return friendlyError
    }

    private fun extractHttpCode(message: String): String? {
        val regex = Regex("""\b(401|403|408|429|500|503)\b""")
        return regex.find(message)?.value
    }

    private fun parseRetryAfter(message: String): Int? {
        val regex = Regex("""(\d+(?:\.\d+)?)\s*(?:s|sec|second|seconds)""", RegexOption.IGNORE_CASE)
        val match = regex.find(message)
        if (match != null) {
            val value = match.groupValues[1].toDoubleOrNull()
            if (value != null) {
                return value.toInt().coerceAtLeast(1)
            }
        }
        val numberRegex = Regex("""\b(\d+)\b""")
        val numberMatches = numberRegex.findAll(message)
        for (numMatch in numberMatches) {
            val value = numMatch.groupValues[1].toIntOrNull()
            if (value != null && value > 0 && value < 3600) {
                return value
            }
        }
        return null
    }
}
