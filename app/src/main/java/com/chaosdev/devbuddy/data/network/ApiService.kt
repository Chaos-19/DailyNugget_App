package com.chaosdev.devbuddy.data.network

import android.content.Context
import com.chaosdev.devbuddy.data.model.Challenge
import com.chaosdev.devbuddy.data.model.FeedItem
import com.chaosdev.devbuddy.data.model.Progress
import com.chaosdev.devbuddy.data.model.User
import com.chaosdev.devbuddy.data.model.UserResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.File
import javax.inject.Singleton

interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body user: User): UserResponse

    @GET("api/feed")
    suspend fun getFeed(): List<FeedItem>

    @GET("api/progress")
    suspend fun getProgress(): Progress

    @GET("api/challenges")
    suspend fun getChallenges(): List<Challenge>
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024 // 10 MB
        val cache = Cache(File(context.cacheDir, "http-cache"), cacheSize.toLong())
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(client: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.dailynuggetapp.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}