package com.medislot.app.data.ai

import android.util.Log
import com.medislot.app.BuildConfig
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

object ApiUsageMonitor {
    private val totalRequests = AtomicInteger(0)
    private val successfulRequests = AtomicInteger(0)
    private val failedRequests = AtomicInteger(0)
    private val totalResponseTime = AtomicLong(0L)
    private val cacheHits = AtomicInteger(0)

    fun trackRequest() {
        totalRequests.incrementAndGet()
        logStatus()
    }

    fun trackSuccess(responseTimeMs: Long) {
        successfulRequests.incrementAndGet()
        totalResponseTime.addAndGet(responseTimeMs)
        logStatus()
    }

    fun trackFailure() {
        failedRequests.incrementAndGet()
        logStatus()
    }

    fun trackCacheHit() {
        cacheHits.incrementAndGet()
        logStatus()
    }

    fun getRequestsToday(): Int = totalRequests.get()
    fun getSuccessfulRequests(): Int = successfulRequests.get()
    fun getFailedRequests(): Int = failedRequests.get()
    
    fun getCacheHitRate(): Float {
        val total = totalRequests.get() + cacheHits.get()
        if (total == 0) return 0f
        return (cacheHits.get().toFloat() / total.toFloat()) * 100f
    }
    
    fun getAverageResponseTime(): Long {
        val success = successfulRequests.get()
        if (success == 0) return 0L
        return totalResponseTime.get() / success
    }

    fun isDebug(): Boolean {
        return try {
            BuildConfig.DEBUG
        } catch (e: Exception) {
            false
        }
    }

    private fun logStatus() {
        if (isDebug()) {
            Log.d("ApiUsageMonitor", """
                --- AI Usage Monitor ---
                Total Requests today: ${getRequestsToday()}
                Successful Requests: ${getSuccessfulRequests()}
                Failed Requests: ${getFailedRequests()}
                Average Response Time: ${getAverageResponseTime()} ms
                Cache Hit Rate: ${String.format("%.2f", getCacheHitRate())}%
                ------------------------
            """.trimIndent())
        }
    }
}
