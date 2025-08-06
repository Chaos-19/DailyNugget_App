package com.chaosdev.devbuddy.ui.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import com.chaosdev.devbuddy.data.model.User
import com.chaosdev.devbuddy.data.model.UserResponse
import com.chaosdev.devbuddy.data.model.updatePreferencesRespons
import com.chaosdev.devbuddy.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences,
    private val apiService: ApiService
) : ViewModel() {

    private val _selectedTopics = MutableStateFlow<List<String>>(emptyList())
    val selectedTopics: StateFlow<List<String>> = _selectedTopics.asStateFlow()

    private val _selectedTime = MutableStateFlow(10)
    val selectedTime: StateFlow<Int> = _selectedTime.asStateFlow()

    private val _notificationEnabled = MutableStateFlow(false)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    fun updateSelectedTopics(topics: List<String>) {
        _selectedTopics.value = topics
    }

    fun updateSelectedTime(time: Int) {
        _selectedTime.value = time
    }

    fun toggleNotification(enabled: Boolean) {
        _notificationEnabled.value = enabled
    }

    fun saveOnboardingData() {
        viewModelScope.launch {
            val apiResult = updatePreferencesWithApi(_selectedTopics.value)
            if (apiResult.isSuccess) {
                onboardingPreferences.setHasSeenOnboarding(true)
                onboardingPreferences.setSelectedTopics(_selectedTopics.value)
                onboardingPreferences.setDailyCommitment(_selectedTime.value)
                onboardingPreferences.setNotificationEnabled(_notificationEnabled.value)
            } else {
                Log.e("OnboardingViewModel", "Failed to update preferences with API: ${apiResult.exceptionOrNull()?.message}")
            }
        }
    }

    private suspend fun updatePreferencesWithApi(selectedTopics: List<String>): Result<updatePreferencesRespons> {
        return try {
            val response = apiService.updatePreferencesWithApi(selectedTopics)
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error registering user with API: ${e.message}", e)
            Result.failure(e)
        }
    }
}