package com.smartclipboardmanager.domain.usecase

import com.smartclipboardmanager.domain.classification.ClipboardClassifier
import com.smartclipboardmanager.domain.classification.ClipboardTextNormalizer
import com.smartclipboardmanager.domain.model.AppSettings
import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.domain.model.ClipboardEntry
import com.smartclipboardmanager.domain.model.ClipboardImportInput
import com.smartclipboardmanager.domain.privacy.SensitiveContentPolicy
import com.smartclipboardmanager.domain.media.MediaDeleter
import com.smartclipboardmanager.domain.repository.ClipboardRepository
import com.smartclipboardmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportClipboardContentUseCaseTest {

    @Test
    fun `marks email as sensitive and stores entry`() = runBlocking {
        val clipboardRepository = InMemoryClipboardRepository()
        val settingsRepository = InMemorySettingsRepository()
        val useCase = ImportClipboardContentUseCase(
            classifier = ClipboardClassifier(ClipboardTextNormalizer()),
            sensitiveContentPolicy = SensitiveContentPolicy(),
            clipboardRepository = clipboardRepository,
            settingsRepository = settingsRepository
        )

        val result = useCase(
            ClipboardImportInput(
                content = "alice@example.org",
                sourceApp = "Gmail"
            )
        )

        assertTrue(result is ImportResult.Imported)
        val imported = result as ImportResult.Imported
        assertEquals(ClipContentType.EMAIL, imported.type)
        assertTrue(imported.isSensitive)

        val first = clipboardRepository.entries.first()
        assertTrue(first.isSensitive)
        assertEquals(ClipContentType.EMAIL, first.contentType)
    }

    @Test
    fun `does not persist otp by default`() = runBlocking {
        val clipboardRepository = InMemoryClipboardRepository()
        val settingsRepository = InMemorySettingsRepository(
            AppSettings(persistOtpEntries = false)
        )
        val useCase = ImportClipboardContentUseCase(
            classifier = ClipboardClassifier(ClipboardTextNormalizer()),
            sensitiveContentPolicy = SensitiveContentPolicy(),
            clipboardRepository = clipboardRepository,
            settingsRepository = settingsRepository
        )

        val result = useCase(ClipboardImportInput(content = "123456", sourceApp = "SMS"))

        assertTrue(result is ImportResult.SkippedOtpPolicy)
        assertTrue(clipboardRepository.entries.isEmpty())
    }

    @Test
    fun `persists otp when setting enabled`() = runBlocking {
        val clipboardRepository = InMemoryClipboardRepository()
        val settingsRepository = InMemorySettingsRepository(
            AppSettings(persistOtpEntries = true)
        )
        val useCase = ImportClipboardContentUseCase(
            classifier = ClipboardClassifier(ClipboardTextNormalizer()),
            sensitiveContentPolicy = SensitiveContentPolicy(),
            clipboardRepository = clipboardRepository,
            settingsRepository = settingsRepository
        )

        val result = useCase(ClipboardImportInput(content = "123456", sourceApp = "SMS"))

        assertTrue(result is ImportResult.Imported)
        assertEquals(1, clipboardRepository.entries.size)
        assertEquals(ClipContentType.OTP, clipboardRepository.entries.first().contentType)
    }

    @Test
    fun `cleanup removes entries older than retention window`() = runBlocking {
        val now = 1_000_000_000L
        val oldTimestamp = now - 40L * 24L * 60L * 60L * 1000L
        val newTimestamp = now - 5L * 24L * 60L * 60L * 1000L

        val clipboardRepository = InMemoryClipboardRepository(
            mutableListOf(
                ClipboardEntry(
                    id = 1,
                    content = "old",
                    sourceApp = "App",
                    createdAtMillis = oldTimestamp,
                    isPinned = false,
                    contentType = ClipContentType.PLAIN_TEXT,
                    isSensitive = false
                ),
                ClipboardEntry(
                    id = 2,
                    content = "new",
                    sourceApp = "App",
                    createdAtMillis = newTimestamp,
                    isPinned = false,
                    contentType = ClipContentType.PLAIN_TEXT,
                    isSensitive = false
                )
            )
        )

        val settingsRepository = InMemorySettingsRepository(
            AppSettings(retentionDays = 30)
        )

        val cleanupUseCase = CleanupOldEntriesUseCase(
            clipboardRepository = clipboardRepository,
            settingsRepository = settingsRepository,
            mediaDeleter = object : MediaDeleter { override fun deleteFile(path: String) {} }
        )

        cleanupUseCase(nowMillis = now)

        assertEquals(1, clipboardRepository.entries.size)
        assertEquals("new", clipboardRepository.entries.first().content)
    }
}

private class InMemorySettingsRepository(
    initial: AppSettings = AppSettings()
) : SettingsRepository {

    private val settingsFlow = MutableStateFlow(initial)

    override fun observeSettings(): Flow<AppSettings> = settingsFlow

    override suspend fun getSettings(): AppSettings = settingsFlow.value

    override suspend fun updateDarkTheme(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(isDarkTheme = enabled)
    }

    override suspend fun updateHistoryLimit(limit: Int) {
        settingsFlow.value = settingsFlow.value.copy(historyLimit = limit)
    }

    override suspend fun updatePersistOtpEntries(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(persistOtpEntries = enabled)
    }

    override suspend fun updateHideSensitivePreview(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(hideSensitivePreview = enabled)
    }

    override suspend fun updateRetentionDays(days: Int) {
        settingsFlow.value = settingsFlow.value.copy(retentionDays = days)
    }
}

private class InMemoryClipboardRepository(
    val entries: MutableList<ClipboardEntry> = mutableListOf()
) : ClipboardRepository {

    private val flow = MutableStateFlow(entries.toList())
    private var nextId = (entries.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun observeHistory(limit: Int?): Flow<List<ClipboardEntry>> {
        return if (limit == null) flow else flow.map { it.take(limit) }
    }

    override fun observeRecentEntries(limit: Int): Flow<List<ClipboardEntry>> {
        return flow.map { it.sortedByDescending { entry -> entry.createdAtMillis }.take(limit) }
    }

    override fun observeEntryById(id: Long): Flow<ClipboardEntry?> {
        return flow.map { list -> list.firstOrNull { it.id == id } }
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) {
        val index = entries.indexOfFirst { it.id == id }
        if (index >= 0) {
            entries[index] = entries[index].copy(isPinned = pinned)
            flow.value = entries.toList()
        }
    }

    override suspend fun deleteEntry(id: Long) {
        entries.removeAll { it.id == id }
        flow.value = entries.sortedByDescending { it.createdAtMillis }
    }

    override suspend fun countEntries(): Int = entries.size

    override suspend fun importEntry(
        content: String,
        sourceApp: String?,
        capturedAtMillis: Long,
        contentType: ClipContentType,
        isSensitive: Boolean,
        mediaUri: String?
    ) {
        entries.add(
            ClipboardEntry(
                id = nextId++,
                content = content,
                sourceApp = sourceApp,
                createdAtMillis = capturedAtMillis,
                isPinned = false,
                contentType = contentType,
                isSensitive = isSensitive,
                mediaUri = mediaUri
            )
        )
        flow.value = entries.sortedByDescending { it.createdAtMillis }
    }

    override suspend fun deleteOlderThan(thresholdMillis: Long): List<String> {
        val removed = entries.filter { it.createdAtMillis < thresholdMillis }
        entries.removeAll { it.createdAtMillis < thresholdMillis }
        flow.value = entries.sortedByDescending { it.createdAtMillis }
        return removed.mapNotNull { it.mediaUri }
    }

    override suspend fun seedDemoDataIfEmpty() {
        if (entries.isEmpty()) {
            importEntry(
                content = "demo",
                sourceApp = "demo",
                capturedAtMillis = System.currentTimeMillis(),
                contentType = ClipContentType.PLAIN_TEXT,
                isSensitive = false,
                mediaUri = null
            )
        }
    }
}
