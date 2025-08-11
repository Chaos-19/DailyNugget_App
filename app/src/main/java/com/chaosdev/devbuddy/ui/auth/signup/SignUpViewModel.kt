package com.chaosdev.devbuddy.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import com.chaosdev.devbuddy.ui.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    sealed class NavigationState {
        object Onboarding : NavigationState()
        object Home : NavigationState()
    }

    private val _signUpState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Idle())
    val signUpState = _signUpState.asStateFlow()

    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState = _navigationState.asStateFlow()

    fun signUpWithEmail(email: String, password: String) {
        _signUpState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.createUserWithEmailAndPassword(email, password)
            if (result.isSuccess) {
                _signUpState.value = Resource.Success(result.getOrNull()!!)
                checkOnboardingStatus()
            } else {
                _signUpState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Signup failed")
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

    fun resetSignUpState() {
        _signUpState.value = Resource.Idle()
        _navigationState.value = null
    }
}