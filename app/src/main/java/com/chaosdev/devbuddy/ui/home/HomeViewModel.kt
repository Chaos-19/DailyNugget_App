package com.chaosdev.devbuddy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val logoutState = _logoutState.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateChanges
    .distinctUntilChanged()
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun signOut() {
        _logoutState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.signOut()
            if (result.isSuccess) {
                _logoutState.value = Resource.Success(Unit)
            } else {
                _logoutState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Logout failed")
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = Resource.Idle()
    }
}