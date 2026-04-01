package com.smartclipboardmanager.domain.usecase

import com.smartclipboardmanager.data.media.MediaStoreHelper
import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.domain.repository.ClipboardRepository
import javax.inject.Inject

class ImportMediaContentUseCase @Inject constructor(
    private val clipboardRepository: ClipboardRepository,
    private val mediaStoreHelper: MediaStoreHelper
) {
    suspend operator fun invoke(
        uriString: String,
        mimeType: String,
        sourceApp: String?
    ): Boolean {
        val localPath = mediaStoreHelper.copyToLocalStorage(uriString, mimeType) ?: return false
        val contentType = if (mimeType.startsWith("image/", ignoreCase = true)) {
            ClipContentType.IMAGE
        } else {
            ClipContentType.FILE
        }
        val displayContent = if (contentType == ClipContentType.IMAGE) {
            "[image]"
        } else {
            mediaStoreHelper.displayNameForUri(uriString)
        }
        clipboardRepository.importEntry(
            content = displayContent,
            sourceApp = sourceApp,
            capturedAtMillis = System.currentTimeMillis(),
            contentType = contentType,
            isSensitive = false,
            mediaUri = localPath
        )
        return true
    }
}
