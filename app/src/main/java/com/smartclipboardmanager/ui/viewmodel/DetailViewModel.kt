package com.smartclipboardmanager.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartclipboardmanager.domain.model.ClipboardEntry
import com.smartclipboardmanager.domain.repository.ClipboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val entry: ClipboardEntry? = null,
    val deleted: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val clipboardRepository: ClipboardRepository
) : ViewModel() {

    private val entryId: Long = checkNotNull(savedStateHandle["entryId"])

    val uiState: StateFlow<DetailUiState> = clipboardRepository.observeEntryById(entryId)
        .map { entry -> DetailUiState(entry = entry, deleted = entry == null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailUiState()
        )

    fun togglePinned() {
        val entry = uiState.value.entry ?: return
        viewModelScope.launch {
            clipboardRepository.setPinned(entry.id, !entry.isPinned)
        }
    }

    fun deleteEntry() {
        val entry = uiState.value.entry ?: return
        viewModelScope.launch {
            clipboardRepository.deleteEntry(entry.id)
        }
    }
}
