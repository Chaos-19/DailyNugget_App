package com.chaosdev.devbuddy.data.datastore;

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore instance
val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_preferences")

@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val HAS_SEEN_ONBOARDING_KEY = booleanPreferencesKey("has_seen_onboarding")

    /**
     * Reads the onboarding status. True if onboarding has been seen, false otherwise.
     */
    suspend fun hasSeenOnboarding(): Boolean {
        return context.onboardingDataStore.data.map { preferences ->
            preferences[HAS_SEEN_ONBOARDING_KEY] ?: false
        }.first() // .first() collects the first value and then cancels the flow
    }

    /**
     * Sets the onboarding status to true.
     */
    suspend fun setOnboardingCompleted() {
        context.onboardingDataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING_KEY] = true
        }
    }
}