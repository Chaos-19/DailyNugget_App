package com.chaosdev.devbuddy.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaosdev.devbuddy.data.datastore.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingPreferences.setHasSeenOnboarding(true)
        }
    }
}