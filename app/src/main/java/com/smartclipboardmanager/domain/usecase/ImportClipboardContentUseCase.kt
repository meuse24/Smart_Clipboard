package com.smartclipboardmanager.domain.usecase

import com.smartclipboardmanager.domain.classification.ClipboardClassifier
import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.domain.model.ClipboardImportInput
import com.smartclipboardmanager.domain.privacy.SensitiveContentPolicy
import com.smartclipboardmanager.domain.repository.ClipboardRepository
import com.smartclipboardmanager.domain.repository.SettingsRepository
import javax.inject.Inject

sealed class ImportResult {
    data class Imported(val type: ClipContentType, val isSensitive: Boolean) : ImportResult()
    data object SkippedEmpty : ImportResult()
    data object SkippedOtpPolicy : ImportResult()
}

class ImportClipboardContentUseCase @Inject constructor(
    private val classifier: ClipboardClassifier,
    private val sensitiveContentPolicy: SensitiveContentPolicy,
    private val clipboardRepository: ClipboardRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(input: ClipboardImportInput): ImportResult {
        val classification = classifier.classify(input.content)
        val normalizedContent = classification.normalizedContent
        if (normalizedContent.isBlank()) {
            return ImportResult.SkippedEmpty
        }

        val settings = settingsRepository.getSettings()
        if (classification.primaryType == ClipContentType.OTP && !settings.persistOtpEntries) {
            return ImportResult.SkippedOtpPolicy
        }

        val isSensitive = sensitiveContentPolicy.isSensitive(classification.primaryType)

        clipboardRepository.importEntry(
            content = normalizedContent,
            sourceApp = input.sourceApp,
            capturedAtMillis = input.capturedAtMillis,
            contentType = classification.primaryType,
            isSensitive = isSensitive,
            mediaUri = input.mediaUri
        )

        return ImportResult.Imported(
            type = classification.primaryType,
            isSensitive = isSensitive
        )
    }
}
