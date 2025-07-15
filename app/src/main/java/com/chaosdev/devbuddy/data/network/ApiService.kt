package com.chaosdev.devbuddy.data.network

import com.chaosdev.devbuddy.data.model.Challenge
import com.chaosdev.devbuddy.data.model.FeedItem
import com.chaosdev.devbuddy.data.model.Progress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

class ApiService @Inject constructor(private val client: OkHttpClient) {
    private val gson = Gson()
    private val baseUrl = "https://api.dailynuggetapp.com"

    suspend fun getFeed(): List<FeedItem> {
        val request = Request.Builder()
            .url("$baseUrl/api/feed")
            .build()
        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val json = response.body?.string() ?: throw IOException("Empty response")
            gson.fromJson(json, object : TypeToken<List<FeedItem>>() {}.type)
        } catch (e: Exception) {
            throw IOException("Failed to fetch feed: ${e.message}")
        }
    }

    suspend fun getProgress(): Progress {
        val request = Request.Builder()
            .url("$baseUrl/api/progress")
            .build()
        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val json = response.body?.string() ?: throw IOException("Empty response")
            gson.fromJson(json, Progress::class.java)
        } catch (e: Exception) {
            throw IOException("Failed to fetch progress: ${e.message}")
        }
    }

    suspend fun getChallenges(): List<Challenge> {
        val request = Request.Builder()
            .url("$baseUrl/api/challenges")
            .build()
        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val json = response.body?.string() ?: throw IOException("Empty response")
            gson.fromJson(json, object : TypeToken<List<Challenge>>() {}.type)
        } catch (e: Exception) {
            throw IOException("Failed to fetch challenges: ${e.message}")
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(client: OkHttpClient): ApiService {
        return ApiService(client)
    }
}