package com.chaosdev.devbuddy.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    suspend fun hasSeenOnboarding(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[HAS_SEEN_ONBOARDING] ?: false
        }.first()
    }

    suspend fun setHasSeenOnboarding(hasSeen: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING] = hasSeen
        }
    }
}