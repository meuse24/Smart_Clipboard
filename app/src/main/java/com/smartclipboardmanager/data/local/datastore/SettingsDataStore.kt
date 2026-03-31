package com.smartclipboardmanager.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartclipboardmanager.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private val dataStore = context.settingsDataStore

    fun observeSettings(): Flow<AppSettings> {
        return dataStore.data.map { prefs ->
            AppSettings(
                isDarkTheme = prefs[KEY_DARK_THEME] ?: false,
                historyLimit = prefs[KEY_HISTORY_LIMIT] ?: 100,
                persistOtpEntries = prefs[KEY_PERSIST_OTP] ?: false,
                hideSensitivePreview = prefs[KEY_HIDE_SENSITIVE_PREVIEW] ?: true,
                retentionDays = prefs[KEY_RETENTION_DAYS] ?: 30
            )
        }
    }

    suspend fun getSettings(): AppSettings = observeSettings().first()

    suspend fun updateDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DARK_THEME] = enabled
        }
    }

    suspend fun updateHistoryLimit(limit: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_HISTORY_LIMIT] = limit.coerceIn(25, 1000)
        }
    }

    suspend fun updatePersistOtpEntries(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_PERSIST_OTP] = enabled
        }
    }

    suspend fun updateHideSensitivePreview(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_HIDE_SENSITIVE_PREVIEW] = enabled
        }
    }

    suspend fun updateRetentionDays(days: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_RETENTION_DAYS] = days.coerceIn(1, 365)
        }
    }

    private companion object {
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_HISTORY_LIMIT = intPreferencesKey("history_limit")
        val KEY_PERSIST_OTP = booleanPreferencesKey("persist_otp_entries")
        val KEY_HIDE_SENSITIVE_PREVIEW = booleanPreferencesKey("hide_sensitive_preview")
        val KEY_RETENTION_DAYS = intPreferencesKey("retention_days")
    }
}
