package com.xrayvpn.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * API service for fetching configuration URLs
 */
interface ConfigApiService {
    suspend fun getConfigList(url: String): String
}

/**
 * Implementation of ConfigApiService using OkHttp and manual requests
 */
class ConfigApiServiceImpl(
    private val okHttpClient: OkHttpClient
) {
    suspend fun fetchConfigList(url: String): String {
        val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .build()
        
        return okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch config: ${response.code}")
            }
            response.body?.string() ?: throw Exception("Empty response")
        }
    }
}

/**
 * Factory for creating HTTP clients
 */
object HttpClientFactory {
    fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }
}
