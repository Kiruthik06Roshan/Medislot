package com.medislot.app.data.ai

import com.medislot.app.network.GeminiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiRepositoryImplTest {

    // Simple fake GeminiService for testing
    private class FakeGeminiService : GeminiService {
        val attempts = mutableListOf<String>()
        var responses = mutableMapOf<String, Result<String>>()

        override fun isOnline(): Boolean = true

        override suspend fun generateContent(prompt: String, timeoutMs: Long): Result<String> {
            return generateContent("gemini-2.5-flash", prompt, timeoutMs)
        }

        override suspend fun generateContent(modelName: String, prompt: String, timeoutMs: Long): Result<String> {
            attempts.add(modelName)
            return responses[modelName] ?: Result.failure(Exception("Model $modelName not configured in fake"))
        }
    }

    @Test
    fun testPrimaryModelSucceeds() = runBlocking {
        val fakeService = FakeGeminiService().apply {
            responses["gemini-2.5-flash"] = Result.success("""
                {
                    "possibleConditions": ["Condition A"],
                    "severity": "Low",
                    "recommendedAction": "Action",
                    "homeCare": "Care",
                    "doctorVisitRecommendation": "Visit",
                    "emergencyWarning": "Warning",
                    "disclaimer": "Disclaimer"
                }
            """.trimIndent())
        }
        val repository = GeminiRepositoryImpl(fakeService)

        val result = repository.checkSymptoms("fever")
        assertTrue(result.isSuccess)
        assertEquals("Condition A", result.getOrThrow().possibleConditions.first())
        assertEquals(listOf("gemini-2.5-flash"), fakeService.attempts)
    }

    @Test
    fun testFallbackToSecondModel() = runBlocking {
        val fakeService = FakeGeminiService().apply {
            responses["gemini-2.5-flash"] = Result.failure(Exception("Quota Exceeded"))
            responses["gemini-3.5-flash-lite"] = Result.success("""
                {
                    "possibleConditions": ["Condition B"],
                    "severity": "Low",
                    "recommendedAction": "Action",
                    "homeCare": "Care",
                    "doctorVisitRecommendation": "Visit",
                    "emergencyWarning": "Warning",
                    "disclaimer": "Disclaimer"
                }
            """.trimIndent())
        }
        val repository = GeminiRepositoryImpl(fakeService)

        val result = repository.checkSymptoms("fever")
        assertTrue(result.isSuccess)
        assertEquals("Condition B", result.getOrThrow().possibleConditions.first())
        assertEquals(listOf("gemini-2.5-flash", "gemini-3.5-flash-lite"), fakeService.attempts)
    }

    @Test
    fun testFallbackToThirdModel() = runBlocking {
        val fakeService = FakeGeminiService().apply {
            responses["gemini-2.5-flash"] = Result.failure(Exception("Quota Exceeded"))
            responses["gemini-3.5-flash-lite"] = Result.failure(Exception("Overloaded"))
            responses["gemini-3"] = Result.success("""
                {
                    "possibleConditions": ["Condition C"],
                    "severity": "Low",
                    "recommendedAction": "Action",
                    "homeCare": "Care",
                    "doctorVisitRecommendation": "Visit",
                    "emergencyWarning": "Warning",
                    "disclaimer": "Disclaimer"
                }
            """.trimIndent())
        }
        val repository = GeminiRepositoryImpl(fakeService)

        val result = repository.checkSymptoms("fever")
        assertTrue(result.isSuccess)
        assertEquals("Condition C", result.getOrThrow().possibleConditions.first())
        assertEquals(listOf("gemini-2.5-flash", "gemini-3.5-flash-lite", "gemini-3"), fakeService.attempts)
    }

    @Test
    fun testAllModelsFailReturnsMockFallbackException() = runBlocking {
        val fakeService = FakeGeminiService().apply {
            responses["gemini-2.5-flash"] = Result.failure(Exception("Quota Exceeded"))
            responses["gemini-3.5-flash-lite"] = Result.failure(Exception("Overloaded"))
            responses["gemini-3"] = Result.failure(Exception("Unsupported"))
        }
        val repository = GeminiRepositoryImpl(fakeService)

        val result = repository.checkSymptoms("fever")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MockFallbackException)
        val mockFallback = result.exceptionOrNull() as MockFallbackException
        val symptomCheckResponse = mockFallback.mockData as SymptomCheckResponse
        assertEquals(listOf("Mild Viral Infection", "Seasonal Allergies", "Acute Rhino-sinusitis"), symptomCheckResponse.possibleConditions)
        assertEquals(listOf("gemini-2.5-flash", "gemini-3.5-flash-lite", "gemini-3"), fakeService.attempts)
    }

    @Test
    fun testInvalidJsonResponseFallback() = runBlocking {
        val fakeService = FakeGeminiService().apply {
            // Succeeded HTTP response but invalid JSON (invalid response)
            responses["gemini-2.5-flash"] = Result.success("This is not JSON")
            responses["gemini-3.5-flash-lite"] = Result.success("""
                {
                    "possibleConditions": ["Condition D"],
                    "severity": "Low",
                    "recommendedAction": "Action",
                    "homeCare": "Care",
                    "doctorVisitRecommendation": "Visit",
                    "emergencyWarning": "Warning",
                    "disclaimer": "Disclaimer"
                }
            """.trimIndent())
        }
        val repository = GeminiRepositoryImpl(fakeService)

        val result = repository.checkSymptoms("fever")
        assertTrue(result.isSuccess)
        assertEquals("Condition D", result.getOrThrow().possibleConditions.first())
        assertEquals(listOf("gemini-2.5-flash", "gemini-3.5-flash-lite"), fakeService.attempts)
    }
}
