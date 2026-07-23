package com.medislot.app.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.withTimeout
import java.io.IOException

interface GeminiService {
    fun isOnline(): Boolean
    suspend fun generateContent(prompt: String, timeoutMs: Long = 20000L): Result<String>
    suspend fun generateContent(modelName: String, prompt: String, timeoutMs: Long = 20000L): Result<String>

    companion object {
        operator fun invoke(context: Context): GeminiService = RealGeminiService(context)
    }
}

class RealGeminiService(private val context: Context) : GeminiService {
    private val modelsMap = mutableMapOf<String, GenerativeModel>()

    private fun getModel(modelName: String): GenerativeModel {
        return modelsMap.getOrPut(modelName) {
            GeminiApi.createModel(modelName)
        }
    }

    private val model: GenerativeModel get() = getModel("gemini-2.5-flash")

    override fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override suspend fun generateContent(prompt: String, timeoutMs: Long): Result<String> {
        return generateContent("gemini-2.5-flash", prompt, timeoutMs)
    }

    override suspend fun generateContent(modelName: String, prompt: String, timeoutMs: Long): Result<String> {
        if (!isOnline()) {
            return Result.failure(IOException("Offline: No active internet connection detected."))
        }
        
        return try {
            withTimeout(timeoutMs) {
                val generativeModel = getModel(modelName)
                val apiKey = generativeModel.apiKey
                if (apiKey.isBlank() || apiKey == "mock_api_key_placeholder" || apiKey.contains("xxxx")) {
                    return@withTimeout Result.failure(Exception("Invalid API Key: Please configure GEMINI_API_KEY inside your local.properties file."))
                }
                val response = generativeModel.generateContent(prompt)
                val text = response.text
                if (text != null) {
                    Result.success(text)
                } else {
                    Result.failure(Exception("AI returned an empty response. Please try again."))
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
