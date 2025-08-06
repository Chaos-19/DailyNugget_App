package com.chaosdev.devbuddy.data.repository

import android.util.Log
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.chaosdev.devbuddy.data.model.User
import com.chaosdev.devbuddy.data.model.UserResponse
import com.chaosdev.devbuddy.data.network.ApiService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val oneTapClient: SignInClient, // For Google One Tap Sign-in
    private val apiService: ApiService, // Added ApiService dependency
    private val onboardingPreferences: OnboardingPreferences
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override val authStateChanges: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Register user with your API after successful Firebase registration
                val apiResult = registerUserWithApi(User(
                    username = user.displayName  ,
                    email =user.email, password = password, selectedTopics = emptyList(), readTime = 0, id = null))
                if (apiResult.isSuccess) {
                    Result.success(user)
                } else {
                    Result.failure(apiResult.exceptionOrNull() ?: Exception("API registration failed"))
                }
            } ?: Result.failure(Exception("User is null after creation"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error creating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("User is null after sign-in"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing in with email/password: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun beginGoogleSignIn(): Result<BeginSignInResult> {
        return try {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId("260909717895-b496i0o3jju5uqn18iortbddgj35899d.apps.googleusercontent.com")
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build()

            val result = oneTapClient.beginSignIn(signInRequest).await()
            Result.success(result)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error beginning Google Sign-In: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(credential: SignInCredential): Result<FirebaseUser> {
        return try {
            val googleIdToken = credential.googleIdToken
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            authResult.user?.let { user ->
                // Register user with your API after successful Google sign-in
                val apiResult = registerUserWithApi(User(username = user.displayName, email = user.email, password = user.providerId, selectedTopics = emptyList(), readTime = 0, id = null))
                if (apiResult.isSuccess) {

                    Result.success(user)
                } else {
                    Result.failure(apiResult.exceptionOrNull() ?: Exception("API registration failed"))
                }
            } ?: Result.failure(Exception("User is null after Google sign-in"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing in with Google: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            oneTapClient.signOut().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing out: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error sending password reset email: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Helper function to register user with your API
    private suspend fun registerUserWithApi(user: User): Result<UserResponse> {
        return try {
            val userData = User(
                username = user.username ?: "Unknown",
                email = user.email ?: "",
                password = "", // Password is not needed for Google sign-in
                selectedTopics = emptyList(),
                readTime = 0,
                id = null
            )
            val response = apiService.registerUser(userData)

            onboardingPreferences.setApiKey(response.apiKey.key)
            onboardingPreferences.setApiKeyId(response.apiKey.id)
            onboardingPreferences.setUserId(response.apiKey.userId)

            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error registering user with API: ${e.message}", e)
            Result.failure(e)
        }
    }
}