package com.chaosdev.devbuddy.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences // <--- Import OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val onboardingPreferences: OnboardingPreferences // <--- Inject OnboardingPreferences
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow<String?>(null) // To hold the determined start route
    val startDestination = _startDestination.asStateFlow() // Expose the start destination

    init {
        viewModelScope.launch {
            // Optional: Minimum delay to ensure splash screen is visible for a bit
            delay(500L) // Adjust as needed, e.g., 500ms

            val hasSeenOnboarding = onboardingPreferences.hasSeenOnboarding()
            val currentUser = authRepository.currentUser // Check Firebase auth status
            
            /*
            val determinedRoute = when {
                !hasSeenOnboarding -> com.chaosdev.devbuddy.ui.navigation.Screen.Onboarding.route
                currentUser == null -> com.chaosdev.devbuddy.ui.navigation.Screen.Login.route
                else -> com.chaosdev.devbuddy.ui.navigation.Screen.Home.route
            }*/
            val determinedRoute = if (currentUser == null) {
                com.chaosdev.devbuddy.ui.navigation.Screen.Login.route
            } else {
                com.chaosdev.devbuddy.ui.navigation.Screen.Home.route
            }
            _startDestination.value = determinedRoute
            _isLoading.value = false // Signal that loading is complete and route is determined
        }
    }
}

/*
package com.chaosdev.devbuddy.ui.splash;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.delay


class SplashViewModel : ViewModel() {
    private val mutableStateFlow = MutableStateFlow(true)
    val isLoading = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            mutableStateFlow.value = false
        }
    }
}*/