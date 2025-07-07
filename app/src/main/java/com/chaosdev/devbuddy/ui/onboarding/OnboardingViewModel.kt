package com.chaosdev.devbuddy.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
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
            onboardingPreferences.setHasSeenOnboarding(true)
            onboardingPreferences.setSelectedTopics(_selectedTopics.value)
            onboardingPreferences.setDailyCommitment(_selectedTime.value)
            onboardingPreferences.setNotificationEnabled(_notificationEnabled.value)
        }
    }
}