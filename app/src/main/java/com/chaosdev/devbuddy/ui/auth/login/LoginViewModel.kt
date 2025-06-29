package com.chaosdev.devbuddy.ui.auth.login

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource // <--- Ensure this import is present and correct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val oneTapClient: SignInClient,
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Idle())
    val loginState = _loginState.asStateFlow()

    private val _googleSignInFlow = MutableStateFlow<Resource<BeginSignInResult>>(Resource.Idle())
    val googleSignInFlow = _googleSignInFlow.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        _loginState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.signInWithEmailAndPassword(email, password)
            result.onSuccess { user -> // <--- Use .onSuccess and .onFailure for kotlin.Result
                _loginState.value = Resource.Success(user)
            }.onFailure { exception ->
                _loginState.value = Resource.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun beginGoogleSignIn() {
        _googleSignInFlow.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.beginGoogleSignIn()
            result.onSuccess { beginSignInResult -> // <--- Use .onSuccess and .onFailure
                _googleSignInFlow.value = Resource.Success(beginSignInResult)
            }.onFailure { exception ->
                _googleSignInFlow.value = Resource.Error(exception.message ?: "Unknown Google Sign-In error")
            }
        }
    }

    fun signInWithGoogleCredential(credential: SignInCredential) {
        _loginState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(credential)
            result.onSuccess { user -> // <--- Use .onSuccess and .onFailure
                _loginState.value = Resource.Success(user)
            }.onFailure { exception ->
                _loginState.value = Resource.Error(exception.message ?: "Google credential sign-in failed")
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = Resource.Idle()
    }

    fun resetGoogleSignInFlow() {
        _googleSignInFlow.value = Resource.Idle()
    }
}
/*package com.chaosdev.devbuddy.ui.auth.login;


import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
//import com.chaosdev.devbuddy.domain.usecase.LoginUseCase // If you use use cases
import com.chaosdev.devbuddy.ui.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val oneTapClient: SignInClient, // Injected for Google One Tap
    // private val loginUseCase: LoginUseCase // If using use cases
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Idle())
    val loginState = _loginState.asStateFlow()

    private val _googleSignInFlow = MutableStateFlow<Resource<BeginSignInResult>>(Resource.Idle())
    val googleSignInFlow = _googleSignInFlow.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        _loginState.value = Resource.Loading()
        viewModelScope.launch {
            // Using repository directly:
            when (val result = authRepository.signInWithEmailAndPassword(email, password)) {
            // Using use case:
            // when (val result = loginUseCase(email, password)) {
                is Result.Success -> {
                    _loginState.value = Resource.Success(result.getOrNull()!!)
                }
                is Result.Error -> {
                    _loginState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
                else -> {
                    // Handle other states if necessary
                }
            }
        }
    }

    fun beginGoogleSignIn() {
        _googleSignInFlow.value = Resource.Loading()
        viewModelScope.launch {
            when (val result = authRepository.beginGoogleSignIn()) {
                is Result.Success -> {
                    _googleSignInFlow.value = Resource.Success(result.getOrNull()!!)
                }
                is Result.Error -> {
                    _googleSignInFlow.value = Resource.Error(result.exceptionOrNull()?.message ?: "Unknown Google Sign-In error")
                }
                else -> { /* Handle other cases */ }
            }
        }
    }

    fun signInWithGoogleCredential(credential: SignInCredential) {
        _loginState.value = Resource.Loading()
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(credential)) {
                is Result.Success -> {
                    _loginState.value = Resource.Success(result.getOrNull()!!)
                }
                is Result.Error -> {
                    _loginState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Google credential sign-in failed")
                }
                else -> { /* Handle other cases */ }
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = Resource.Idle()
    }

    fun resetGoogleSignInFlow() {
        _googleSignInFlow.value = Resource.Idle()
    }
}
*/