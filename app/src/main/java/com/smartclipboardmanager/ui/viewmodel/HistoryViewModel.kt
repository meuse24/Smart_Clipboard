package com.smartclipboardmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartclipboardmanager.domain.privacy.SensitiveContentRedactor
import com.smartclipboardmanager.domain.repository.ClipboardRepository
import com.smartclipboardmanager.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class HistoryItemUiModel(
    val id: Long,
    val rawContent: String,
    val displayContent: String,
    val sourceApp: String?,
    val createdAtMillis: Long,
    val isPinned: Boolean,
    val typeLabel: String,
    val type: com.smartclipboardmanager.domain.model.ClipContentType,
    val isSensitive: Boolean,
    val mediaUri: String?
)

data class HistoryUiState(
    val entries: List<HistoryItemUiModel> = emptyList(),
    val hideSensitivePreview: Boolean = true,
    val query: String = ""
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    clipboardRepository: ClipboardRepository,
    settingsRepository: SettingsRepository,
    private val sensitiveContentRedactor: SensitiveContentRedactor
) : ViewModel() {

    private val queryState = MutableStateFlow("")

    val uiState: StateFlow<HistoryUiState> = combine(
        clipboardRepository.observeHistory(),
        settingsRepository.observeSettings(),
        queryState
    ) { entries, settings, query ->
        val normalizedQuery = query.trim().lowercase()

        val filtered = if (normalizedQuery.isBlank()) {
            entries
        } else {
            entries.filter { entry ->
                entry.content.lowercase().contains(normalizedQuery) ||
                    (entry.sourceApp?.lowercase()?.contains(normalizedQuery) == true) ||
                    entry.contentType.name.lowercase().contains(normalizedQuery)
            }
        }

        HistoryUiState(
            entries = filtered.map { entry ->
                val displayContent = if (entry.isSensitive && settings.hideSensitivePreview) {
                    sensitiveContentRedactor.redact(entry.contentType, entry.content)
                } else {
                    entry.content
                }
                HistoryItemUiModel(
                    id = entry.id,
                    rawContent = entry.content,
                    displayContent = displayContent,
                    sourceApp = entry.sourceApp,
                    createdAtMillis = entry.createdAtMillis,
                    isPinned = entry.isPinned,
                    typeLabel = entry.contentType.name,
                    type = entry.contentType,
                    isSensitive = entry.isSensitive,
                    mediaUri = entry.mediaUri
                )
            },
            hideSensitivePreview = settings.hideSensitivePreview,
            query = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )

    fun updateQuery(query: String) {
        queryState.update { query }
    }
}
