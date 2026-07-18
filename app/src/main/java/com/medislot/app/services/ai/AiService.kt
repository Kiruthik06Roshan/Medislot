package com.medislot.app.services.ai

interface AiService {
    suspend fun analyzeSymptoms(symptomsPrompt: String): Result<String>
    suspend fun summarizeReviews(reviews: List<String>): Result<String>
}
