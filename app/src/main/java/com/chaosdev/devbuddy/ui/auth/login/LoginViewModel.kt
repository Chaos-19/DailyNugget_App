package com.chaosdev.devbuddy.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class NavigationState {
        object Onboarding : NavigationState()
        object Home : NavigationState()
        data class GoogleSignIn(val beginSignInResult: BeginSignInResult) : NavigationState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _googleSignInFlow = MutableStateFlow<Resource<BeginSignInResult>>(Resource.Idle())
    val googleSignInFlow = _googleSignInFlow.asStateFlow()

    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState = _navigationState.asStateFlow()

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.signInWithEmailAndPassword(email, password)
            if (result.isSuccess) {
                checkOnboardingStatus()
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.createUserWithEmailAndPassword(email, password)
            if (result.isSuccess) {
                _navigationState.value = NavigationState.Onboarding
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    fun beginGoogleSignIn() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _googleSignInFlow.value = Resource.Loading()
            val result = authRepository.beginGoogleSignIn()
            if (result.isSuccess) {
                _googleSignInFlow.value = Resource.Success(result.getOrNull()!!)
                _navigationState.value = NavigationState.GoogleSignIn(result.getOrNull()!!)
            } else {
                _uiState.value =
                    UiState.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
                _googleSignInFlow.value =
                    Resource.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
            }
        }
    }

    fun completeGoogleSignIn(credential: SignInCredential) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.signInWithGoogle(credential)
            if (result.isSuccess) {
                checkOnboardingStatus()
            } else {
                _uiState.value =
                    UiState.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
                _googleSignInFlow.value =
                    Resource.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
            }
        }
    }

    private suspend fun checkOnboardingStatus() {
        val hasSeenOnboarding = onboardingPreferences.hasSeenOnboarding()
        _navigationState.value = if (hasSeenOnboarding) {
            NavigationState.Home
        } else {
            NavigationState.Onboarding
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    fun resetGoogleSignInFlow() {
        _googleSignInFlow.value = Resource.Idle()
    }
}