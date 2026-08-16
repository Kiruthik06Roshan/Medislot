package com.medislot.app.network

import android.util.Log
import com.medislot.app.BuildConfig
import com.medislot.app.data.local.DatabaseProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val BASE_URL = BuildConfig.BASE_URL.takeIf { it.isNotBlank() } ?: "http://10.0.2.2:8000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
        try {
            val token = runBlocking {
                DatabaseProvider.getDataStoreManager().accessTokenFlow.first()
            }
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        } catch (e: Exception) {
            Log.e("MediSlotNet", "Error reading accessToken for auth interceptor: ${e.message}")
        }
        chain.proceed(builder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Log.d("MediSlotNet", "Initializing RetrofitClient with BASE_URL: $BASE_URL")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
