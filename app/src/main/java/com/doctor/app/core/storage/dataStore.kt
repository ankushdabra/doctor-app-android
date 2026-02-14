package com.doctor.app.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.doctor.app.login.api.UserDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    private val gson = Gson()

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val THEME_KEY = stringPreferencesKey("theme_mode")
        private val USER_DETAILS_KEY = stringPreferencesKey("user_details")
    }

    val token: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: "FOLLOW_SYSTEM"
        }

    val userDetails: Flow<UserDto?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_DETAILS_KEY]?.let {
                try {
                    gson.fromJson(it, UserDto::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserDetails(user: UserDto) {
        context.dataStore.edit { prefs ->
            prefs[USER_DETAILS_KEY] = gson.toJson(user)
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_DETAILS_KEY)
        }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode
        }
    }
}
