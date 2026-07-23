package com.medislot.app.data.ai

import java.util.Collections
import java.util.LinkedHashMap

data class CachedResult<T>(
    val data: T,
    val timestamp: Long
)

class AiCache(private val maxEntries: Int = 100) {
    private val store = Collections.synchronizedMap(
        object : LinkedHashMap<String, CachedResult<Any>>(maxEntries, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<String, CachedResult<Any>>?): Boolean {
                return size > maxEntries
            }
        }
    )

    fun put(key: String, value: Any) {
        store[key] = CachedResult(value, System.currentTimeMillis())
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): CachedResult<T>? {
        val cached = store[key] ?: return null
        return cached as? CachedResult<T>
    }

    fun clear() {
        store.clear()
    }
}
