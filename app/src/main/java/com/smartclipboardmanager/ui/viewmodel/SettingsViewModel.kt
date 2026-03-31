package com.smartclipboardmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartclipboardmanager.domain.repository.SettingsRepository
import com.smartclipboardmanager.domain.usecase.CleanupOldEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val historyLimit: Int = 100,
    val persistOtpEntries: Boolean = false,
    val hideSensitivePreview: Boolean = true,
    val retentionDays: Int = 30
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val cleanupOldEntriesUseCase: CleanupOldEntriesUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.observeSettings()
        .map {
            SettingsUiState(
                isDarkTheme = it.isDarkTheme,
                historyLimit = it.historyLimit,
                persistOtpEntries = it.persistOtpEntries,
                hideSensitivePreview = it.hideSensitivePreview,
                retentionDays = it.retentionDays
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateDarkTheme(enabled) }
    }

    fun setPersistOtpEntries(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updatePersistOtpEntries(enabled) }
    }

    fun setHideSensitivePreview(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateHideSensitivePreview(enabled) }
    }

    fun updateHistoryLimit(limit: Int) {
        viewModelScope.launch { settingsRepository.updateHistoryLimit(limit) }
    }

    fun updateRetentionDays(days: Int) {
        viewModelScope.launch {
            settingsRepository.updateRetentionDays(days)
            cleanupOldEntriesUseCase()
        }
    }
}
