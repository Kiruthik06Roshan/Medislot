package com.medislot.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "medislot_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val THEME_KEY = stringPreferencesKey("theme_key")
        val REMEMBER_LOGIN_KEY = booleanPreferencesKey("remember_login")
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val UID_KEY = stringPreferencesKey("user_uid")
        val ROLE_KEY = stringPreferencesKey("user_role")
        val EMAIL_KEY = stringPreferencesKey("user_email")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "System"
    }

    val rememberLoginFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REMEMBER_LOGIN_KEY] ?: false
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "en"
    }

    val uidFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[UID_KEY]
    }

    val roleFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ROLE_KEY]
    }

    val emailFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun saveRememberLogin(remember: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMEMBER_LOGIN_KEY] = remember
        }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun saveUserSession(uid: String, role: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[UID_KEY] = uid
            preferences[ROLE_KEY] = role
            preferences[EMAIL_KEY] = email
        }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(UID_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(EMAIL_KEY)
        }
    }
}
