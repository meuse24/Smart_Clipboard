package com.smartclipboardmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.domain.model.ClipboardImportInput
import com.smartclipboardmanager.domain.privacy.SensitiveContentRedactor
import com.smartclipboardmanager.domain.repository.ClipboardRepository
import com.smartclipboardmanager.domain.repository.SettingsRepository
import com.smartclipboardmanager.domain.usecase.CleanupOldEntriesUseCase
import com.smartclipboardmanager.domain.usecase.ImportClipboardContentUseCase
import com.smartclipboardmanager.domain.usecase.ImportMediaContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeEntryUiModel(
    val id: Long,
    val previewContent: String,
    val sourceApp: String?,
    val createdAtMillis: Long,
    val typeLabel: String,
    val contentType: ClipContentType,
    val isSensitive: Boolean,
    val mediaUri: String?
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
    private val importMediaContentUseCase: ImportMediaContentUseCase,
    private val cleanupOldEntriesUseCase: CleanupOldEntriesUseCase,
    private val sensitiveContentRedactor: SensitiveContentRedactor
) : ViewModel() {

    private val loadingState = MutableStateFlow(true)

    private val _mediaImportResult = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val mediaImportResult: SharedFlow<Boolean> = _mediaImportResult.asSharedFlow()

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
                    contentType = it.contentType,
                    isSensitive = it.isSensitive,
                    mediaUri = it.mediaUri
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

    fun importMediaContent(uriString: String, mimeType: String, sourceApp: String?) {
        viewModelScope.launch {
            val success = importMediaContentUseCase(uriString, mimeType, sourceApp)
            _mediaImportResult.tryEmit(success)
            if (success) cleanupOldEntriesUseCase()
        }
    }
}
