package com.chaosdev.devbuddy.data.repository;

import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.identity.BeginSignInResult
import kotlinx.coroutines.flow.Flow 
/**
 * Defines the contract for authentication operations.
 */
interface AuthRepository {
    val currentUser: FirebaseUser?
    val authStateChanges: Flow<FirebaseUser?> 

    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithGoogle(credential: SignInCredential): Result<FirebaseUser>
    suspend fun signOut(): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun beginGoogleSignIn(): Result<BeginSignInResult>
}