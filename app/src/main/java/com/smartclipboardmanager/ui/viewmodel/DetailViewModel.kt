package com.smartclipboardmanager.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartclipboardmanager.data.media.MediaStoreHelper
import com.smartclipboardmanager.domain.model.ClipboardEntry
import com.smartclipboardmanager.domain.repository.ClipboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val clipboardRepository: ClipboardRepository,
    private val mediaStoreHelper: MediaStoreHelper
) : ViewModel() {

    private val entryId: Long? = savedStateHandle.get<Long>("entryId")

    val uiState: StateFlow<DetailUiState> = entryId?.let { id ->
        clipboardRepository.observeEntryById(id)
            .map { entry -> DetailUiState(entry = entry, deleted = entry == null) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetailUiState()
            )
    } ?: MutableStateFlow(DetailUiState(deleted = true))

    fun togglePinned() {
        val entry = uiState.value.entry ?: return
        viewModelScope.launch {
            clipboardRepository.setPinned(entry.id, !entry.isPinned)
        }
    }

    fun deleteEntry() {
        val entry = uiState.value.entry ?: return
        viewModelScope.launch {
            entry.mediaUri?.let { mediaStoreHelper.deleteFile(it) }
            clipboardRepository.deleteEntry(entry.id)
        }
    }
}
