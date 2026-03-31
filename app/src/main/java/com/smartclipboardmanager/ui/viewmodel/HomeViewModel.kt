package com.smartclipboardmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartclipboardmanager.domain.model.ClipboardImportInput
import com.smartclipboardmanager.domain.privacy.SensitiveContentRedactor
import com.smartclipboardmanager.domain.repository.ClipboardRepository
import com.smartclipboardmanager.domain.repository.SettingsRepository
import com.smartclipboardmanager.domain.usecase.CleanupOldEntriesUseCase
import com.smartclipboardmanager.domain.usecase.ImportClipboardContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeEntryUiModel(
    val id: Long,
    val previewContent: String,
    val sourceApp: String?,
    val createdAtMillis: Long,
    val typeLabel: String,
    val isSensitive: Boolean
)

data class HomeUiState(
    val recentEntries: List<HomeEntryUiModel> = emptyList(),
    val isDarkTheme: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val clipboardRepository: ClipboardRepository,
    settingsRepository: SettingsRepository,
    private val importClipboardContentUseCase: ImportClipboardContentUseCase,
    private val cleanupOldEntriesUseCase: CleanupOldEntriesUseCase,
    private val sensitiveContentRedactor: SensitiveContentRedactor
) : ViewModel() {

    private val loadingState = MutableStateFlow(true)

    val uiState: StateFlow<HomeUiState> = combine(
        clipboardRepository.observeRecentEntries(limit = 5),
        settingsRepository.observeSettings(),
        loadingState
    ) { entries, settings, isLoading ->
        HomeUiState(
            recentEntries = entries.map {
                val previewContent = if (it.isSensitive && settings.hideSensitivePreview) {
                    sensitiveContentRedactor.redact(it.contentType, it.content)
                } else {
                    it.content
                }
                HomeEntryUiModel(
                    id = it.id,
                    previewContent = previewContent,
                    sourceApp = it.sourceApp,
                    createdAtMillis = it.createdAtMillis,
                    typeLabel = it.contentType.name,
                    isSensitive = it.isSensitive
                )
            },
            isDarkTheme = settings.isDarkTheme,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            if (clipboardRepository.countEntries() == 0) {
                importClipboardContentUseCase(
                    ClipboardImportInput(
                        content = "ssh user@server 'sudo systemctl restart app'",
                        sourceApp = "Terminal"
                    )
                )
                importClipboardContentUseCase(
                    ClipboardImportInput(
                        content = "https://developer.android.com/topic/architecture",
                        sourceApp = "Chrome"
                    )
                )
                importClipboardContentUseCase(
                    ClipboardImportInput(
                        content = "Weekly sync moved to 14:30 CET",
                        sourceApp = "Slack"
                    )
                )
            }

            cleanupOldEntriesUseCase()
            loadingState.value = false
        }
    }

    fun importClipboardContent(content: String, sourceApp: String?) {
        viewModelScope.launch {
            importClipboardContentUseCase(
                ClipboardImportInput(content = content, sourceApp = sourceApp)
            )
            cleanupOldEntriesUseCase()
        }
    }
}
