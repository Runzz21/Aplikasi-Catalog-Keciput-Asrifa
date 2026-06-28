package com.keciput.asrifa.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class UserData(
    val name: String = "",
    val email: String = "",
    val viewedCount: Int = 0,
    val notificationEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "Bahasa Indonesia",
    val searchHistory: List<String> = emptyList()
)

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_NAME = stringPreferencesKey("user_name")
        val KEY_EMAIL = stringPreferencesKey("user_email")
        val KEY_VIEWED = intPreferencesKey("viewed_count")
        val KEY_NOTIF = booleanPreferencesKey("notif_enabled")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_SEARCH_HISTORY = stringSetPreferencesKey("search_history")
    }

    val userDataFlow: Flow<UserData> = dataStore.data.map { prefs ->
        UserData(
            name = prefs[KEY_NAME] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            viewedCount = prefs[KEY_VIEWED] ?: 0,
            notificationEnabled = prefs[KEY_NOTIF] ?: true,
            darkModeEnabled = prefs[KEY_DARK_MODE] ?: false,
            language = prefs[KEY_LANGUAGE] ?: "Bahasa Indonesia",
            searchHistory = prefs[KEY_SEARCH_HISTORY]?.toList() ?: emptyList()
        )
    }

    suspend fun updateProfile(name: String, email: String) {
        dataStore.edit { prefs ->
            prefs[KEY_NAME] = name
            prefs[KEY_EMAIL] = email
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIF] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun incrementViewedCount() {
        dataStore.edit { it[KEY_VIEWED] = (it[KEY_VIEWED] ?: 0) + 1 }
    }

    suspend fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        dataStore.edit { prefs ->
            val currentHistory = prefs[KEY_SEARCH_HISTORY] ?: emptySet()
            // Add new query to the beginning (by converting to list and back, though Set doesn't guarantee order, 
            // but we can handle order manually if needed. StringSet is a simple way)
            val updatedHistory = (setOf(query) + currentHistory).take(10).toSet()
            prefs[KEY_SEARCH_HISTORY] = updatedHistory
        }
    }

    suspend fun removeSearchHistory(query: String) {
        dataStore.edit { prefs ->
            val currentHistory = prefs[KEY_SEARCH_HISTORY] ?: emptySet()
            prefs[KEY_SEARCH_HISTORY] = currentHistory.filter { it != query }.toSet()
        }
    }

    suspend fun clearSearchHistory() {
        dataStore.edit { it.remove(KEY_SEARCH_HISTORY) }
    }

    suspend fun clearUserData() {
        dataStore.edit { it.clear() }
    }
}
