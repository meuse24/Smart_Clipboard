package com.smartclipboardmanager.data.repository

import com.smartclipboardmanager.data.local.datastore.SettingsDataStore
import com.smartclipboardmanager.domain.model.AppSettings
import com.smartclipboardmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = settingsDataStore.observeSettings()

    override suspend fun getSettings(): AppSettings = settingsDataStore.getSettings()

    override suspend fun updateDarkTheme(enabled: Boolean) {
        settingsDataStore.updateDarkTheme(enabled)
    }

    override suspend fun updateHistoryLimit(limit: Int) {
        settingsDataStore.updateHistoryLimit(limit)
    }

    override suspend fun updatePersistOtpEntries(enabled: Boolean) {
        settingsDataStore.updatePersistOtpEntries(enabled)
    }

    override suspend fun updateHideSensitivePreview(enabled: Boolean) {
        settingsDataStore.updateHideSensitivePreview(enabled)
    }

    override suspend fun updateRetentionDays(days: Int) {
        settingsDataStore.updateRetentionDays(days)
    }
}
