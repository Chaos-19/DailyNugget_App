package com.chaosdev.devbuddy.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class NavigationState {
        object Login : NavigationState()
        object Home : NavigationState()
    }

    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState = _navigationState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            authRepository.authStateChanges.collect { user ->
                _navigationState.value = if (user != null) {
                    NavigationState.Home
                } else {
                    NavigationState.Login
                }
                _isLoading.value = false
            }
        }
    }
}