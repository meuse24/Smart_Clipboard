package com.smartclipboardmanager.data.repository

import com.smartclipboardmanager.data.local.dao.ClipboardDao
import com.smartclipboardmanager.data.local.entity.ClipboardEntryEntity
import com.smartclipboardmanager.data.mapper.toDomain
import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.domain.model.ClipboardEntry
import com.smartclipboardmanager.domain.repository.ClipboardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ClipboardRepositoryImpl @Inject constructor(
    private val dao: ClipboardDao,
    private val ioDispatcher: CoroutineDispatcher
) : ClipboardRepository {

    override fun observeHistory(limit: Int?): Flow<List<ClipboardEntry>> {
        val source = if (limit != null) dao.observeRecent(limit) else dao.observeAll()
        return source.map { items -> items.map { it.toDomain() } }
    }

    override fun observeRecentEntries(limit: Int): Flow<List<ClipboardEntry>> {
        return dao.observeRecent(limit).map { items -> items.map { it.toDomain() } }
    }

    override fun observeEntryById(id: Long): Flow<ClipboardEntry?> {
        return dao.observeById(id).map { entity -> entity?.toDomain() }
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) {
        withContext(ioDispatcher) {
            dao.setPinned(id, pinned)
        }
    }

    override suspend fun deleteEntry(id: Long) {
        withContext(ioDispatcher) {
            dao.deleteById(id)
        }
    }

    override suspend fun countEntries(): Int {
        return withContext(ioDispatcher) {
            dao.countAll()
        }
    }

    override suspend fun importEntry(
        content: String,
        sourceApp: String?,
        capturedAtMillis: Long,
        contentType: ClipContentType,
        isSensitive: Boolean
    ) {
        withContext(ioDispatcher) {
            dao.insert(
                ClipboardEntryEntity(
                    content = content,
                    sourceApp = sourceApp,
                    createdAtMillis = capturedAtMillis,
                    isPinned = false,
                    contentType = contentType.name,
                    isSensitive = isSensitive
                )
            )
        }
    }

    override suspend fun deleteOlderThan(thresholdMillis: Long) {
        withContext(ioDispatcher) {
            dao.deleteOlderThan(thresholdMillis)
        }
    }

    override suspend fun seedDemoDataIfEmpty() {
        withContext(ioDispatcher) {
            if (dao.countAll() > 0) return@withContext

            val now = System.currentTimeMillis()
            dao.insertAll(
                listOf(
                    ClipboardEntryEntity(
                        content = "ssh user@server 'sudo systemctl restart app'",
                        sourceApp = "Terminal",
                        createdAtMillis = now - 600_000,
                        isPinned = true,
                        contentType = ClipContentType.CODE_SNIPPET.name,
                        isSensitive = false
                    ),
                    ClipboardEntryEntity(
                        content = "https://developer.android.com/topic/architecture",
                        sourceApp = "Chrome",
                        createdAtMillis = now - 1_200_000,
                        isPinned = false,
                        contentType = ClipContentType.URL.name,
                        isSensitive = false
                    ),
                    ClipboardEntryEntity(
                        content = "Weekly sync moved to 14:30 CET",
                        sourceApp = "Slack",
                        createdAtMillis = now - 3_600_000,
                        isPinned = false,
                        contentType = ClipContentType.PLAIN_TEXT.name,
                        isSensitive = false
                    )
                )
            )
        }
    }
}
