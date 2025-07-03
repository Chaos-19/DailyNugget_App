package com.chaosdev.devbuddy.di

import android.app.Application
import android.content.Context // <--- Add this import
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.data.repository.AuthRepositoryImpl
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences // <--- Add this import
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
        oneTapClient: SignInClient
    ): AuthRepository = AuthRepositoryImpl(auth, oneTapClient)

    @Provides
    @Singleton
    fun provideOnboardingPreferences(@ApplicationContext context: Context): OnboardingPreferences {
        return OnboardingPreferences(context)
    }
}
/*package com.chaosdev.devbuddy.di;

import android.app.Application
import android.content.Context 
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.data.repository.AuthRepositoryImpl
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
        oneTapClient: SignInClient
    ): AuthRepository = AuthRepositoryImpl(auth, oneTapClient)

    // If you add Use Cases, you'd provide them here as well
    // @Provides
    // @Singleton
    // fun provideLoginUseCase(repository: AuthRepository): LoginUseCase = LoginUseCase(repository)
    
    @Provides
    @Singleton
    fun provideOnboardingPreferences(@ApplicationContext context: Context): OnboardingPreferences {
        return OnboardingPreferences(context)
    }
}
*/