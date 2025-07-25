package com.chaosdev.devbuddy.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
        private val SELECTED_TOPICS = stringSetPreferencesKey("selected_topics")
        private val DAILY_COMMITMENT = intPreferencesKey("daily_commitment")
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
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

    suspend fun setSelectedTopics(topics: List<String>) {
        dataStore.edit { preferences ->
            preferences[SELECTED_TOPICS] = topics.toSet()
        }
    }

    suspend fun getSelectedTopics(): Set<String> {
        return dataStore.data.map { preferences ->
            preferences[SELECTED_TOPICS] ?: emptySet()
        }.first()
    }

    suspend fun setDailyCommitment(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[DAILY_COMMITMENT] = minutes
        }
    }

    suspend fun getDailyCommitment(): Int {
        return dataStore.data.map { preferences ->
            preferences[DAILY_COMMITMENT] ?: 10
        }.first()
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun isNotificationEnabled(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[NOTIFICATION_ENABLED] ?: false
        }.first()
    }
}