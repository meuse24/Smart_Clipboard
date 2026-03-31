package com.smartclipboardmanager.domain.repository

import com.smartclipboardmanager.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun updateDarkTheme(enabled: Boolean)
    suspend fun updateHistoryLimit(limit: Int)
    suspend fun updatePersistOtpEntries(enabled: Boolean)
    suspend fun updateHideSensitivePreview(enabled: Boolean)
    suspend fun updateRetentionDays(days: Int)
}
