package com.chaosdev.devbuddy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource // <--- Ensure this import is present
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged // Ensure this is imported
import kotlinx.coroutines.flow.map // Ensure this is imported

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val logoutState = _logoutState.asStateFlow()

    /*val currentUser: FirebaseUser?
        get() = authRepository.currentUser*/

    // Correctly observe the authStateChanges Flow from AuthRepository
    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateChanges
        .distinctUntilChanged() // Only emit if the user object actually changes (prevents redundant updates)
        .asStateFlow() // Convert to StateFlow. Its initial value will be the first emitted by authStateChanges (likely null, then the user).



    fun signOut() {
        _logoutState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.signOut()
            result.onSuccess { // <--- Use .onSuccess and .onFailure
                _logoutState.value = Resource.Success(Unit)
            }.onFailure { exception ->
                _logoutState.value = Resource.Error(exception.message ?: "Logout failed")
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = Resource.Idle()
    }
}
/*package com.chaosdev.devbuddy.ui.home;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
import com.chaosdev.devbuddy.ui.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val logoutState = _logoutState.asStateFlow()

    // Expose current user if needed, or handle in repository directly
    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    fun signOut() {
        _logoutState.value = Resource.Loading()
        viewModelScope.launch {
            when (val result = authRepository.signOut()) {
                is Result.Success -> {
                    _logoutState.value = Resource.Success(Unit)
                }
                is Result.Error -> {
                    _logoutState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Logout failed")
                }
                else -> { /* Handle other states */ }
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = Resource.Idle()
    }
}
*/