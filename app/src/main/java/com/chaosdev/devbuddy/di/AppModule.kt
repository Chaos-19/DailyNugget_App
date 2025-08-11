package com.chaosdev.devbuddy.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import com.chaosdev.devbuddy.data.network.ApiService
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.data.repository.AuthRepositoryImpl
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideGoogleSignInClient(app: Application): SignInClient =
        Identity.getSignInClient(app.applicationContext)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        oneTapClient: SignInClient,
        apiService: ApiService,
        onboardingPreferences: OnboardingPreferences
    ): AuthRepository =  AuthRepositoryImpl(
            auth,
            oneTapClient,
            apiService,
            onboardingPreferences)


    @Provides
    @Singleton
    fun provideOnboardingPreferences(@ApplicationContext context: Context): OnboardingPreferences {
        return OnboardingPreferences(context.dataStore)
    }

//    @Provides
//    @Singleton
//    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
//        val cacheSize = 10 * 1024 * 1024 // 10 MB
//        val cache = Cache(File(context.cacheDir, "http-cache"), cacheSize.toLong())
//        val logging = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
//        return OkHttpClient.Builder()
//            .cache(cache)
//            .addInterceptor(logging)
//            .build()
//    }
}