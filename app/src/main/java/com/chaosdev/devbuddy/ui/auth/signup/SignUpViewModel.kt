package com.chaosdev.devbuddy.ui.auth.signup;


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.chaosdev.devbuddy.data.repository.AuthRepository
//import com.chaosdev.devbuddy.ui.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.chaosdev.devbuddy.ui.common.Resource // Ensure this is imported

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signUpState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Idle())
    val signUpState = _signUpState.asStateFlow()

    fun signUpWithEmail(email: String, password: String) {
        _signUpState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.createUserWithEmailAndPassword(email, password)
            if (result.isSuccess) { // <--- Change
                _signUpState.value = Resource.Success(result.getOrNull()!!)
            } else { // <--- Change
                _signUpState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun resetSignUpState() {
        _signUpState.value = Resource.Idle()
    }
}