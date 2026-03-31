package com.smartclipboardmanager.domain.usecase

import com.smartclipboardmanager.domain.repository.ClipboardRepository
import com.smartclipboardmanager.domain.repository.SettingsRepository
import javax.inject.Inject

class CleanupOldEntriesUseCase @Inject constructor(
    private val clipboardRepository: ClipboardRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()) {
        val settings = settingsRepository.getSettings()
        val retentionDays = settings.retentionDays.coerceIn(1, 365)
        val threshold = nowMillis - retentionDays * MILLIS_PER_DAY
        clipboardRepository.deleteOlderThan(threshold)
    }

    private companion object {
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}
