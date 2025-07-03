package com.chaosdev.devbuddy.data.repository;

import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.chaosdev.devbuddy.R
import com.google.android.gms.auth.api.identity.BeginSignInResult
import kotlinx.coroutines.flow.Flow 
import kotlinx.coroutines.channels.awaitClose 

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val oneTapClient: SignInClient // For Google One Tap Sign-in
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser
        
    // NEW: Implement authStateChanges as a Flow
    override val authStateChanges: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser) // Send the current user whenever state changes
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener) // Clean up listener when flow is cancelled
        }
    }
        

    // --- Email/Password Authentication ---

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("User is null after creation"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error creating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("User is null after sign-in"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing in with email/password: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- Google Authentication ---

    override suspend fun beginGoogleSignIn(): Result<BeginSignInResult> {
        return try {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // This is your web client ID from Firebase project settings -> Project settings -> General
                        // It's NOT the Android client ID
                        .setServerClientId("260909717895-b496i0o3jju5uqn18iortbddgj35899d.apps.googleusercontent.com") // <--- IMPORTANT!
                        .setFilterByAuthorizedAccounts(false) // Set to true to show only authorized accounts
                        .build()
                )
                .setAutoSelectEnabled(true) // Automatically select the default account if available
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
            authResult.user?.let { Result.success(it) }
                ?: Result.failure(Exception("User is null after Google sign-in"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing in with Google: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- General Auth Operations ---

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            oneTapClient.signOut().await() // Sign out from Google One Tap as well
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
}