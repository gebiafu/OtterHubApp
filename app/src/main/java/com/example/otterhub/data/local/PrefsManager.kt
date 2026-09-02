package com.example.otterhub.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "otterhub_prefs")

class PrefsManager(private val context: Context) {

    companion object {
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
        private val KEY_PASSWORD = stringPreferencesKey("password")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_IS_SETUP = stringPreferencesKey("is_setup")
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { it[KEY_BASE_URL] ?: "" }
    val password: Flow<String> = context.dataStore.data.map { it[KEY_PASSWORD] ?: "" }
    val authToken: Flow<String> = context.dataStore.data.map { it[KEY_AUTH_TOKEN] ?: "" }
    val isSetup: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SETUP] == "true" }

    suspend fun saveSetupConfig(baseUrl: String, password: String) {
        context.dataStore.edit {
            it[KEY_BASE_URL] = baseUrl.trimEnd('/')
            it[KEY_PASSWORD] = password
            it[KEY_IS_SETUP] = "true"
        }
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { it.remove(KEY_AUTH_TOKEN) }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
