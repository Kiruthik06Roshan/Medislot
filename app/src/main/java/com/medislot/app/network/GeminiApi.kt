package com.medislot.app.network

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.medislot.app.BuildConfig

object GeminiApi {
    private const val MODEL_NAME = "gemini-2.5-flash"

    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
    )

    private val generationConfig = generationConfig {
        temperature = 0.1f // low temperature to ensure output format compliance
        topK = 40
        topP = 0.95f
    }

    fun createModel(modelName: String = MODEL_NAME): GenerativeModel {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        
        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            safetySettings = safetySettings,
            generationConfig = generationConfig
        )
    }
}
