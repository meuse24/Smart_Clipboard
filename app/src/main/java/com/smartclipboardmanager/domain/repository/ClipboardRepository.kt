package com.smartclipboardmanager.domain.repository

import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.domain.model.ClipboardEntry
import kotlinx.coroutines.flow.Flow

interface ClipboardRepository {
    fun observeHistory(limit: Int? = null): Flow<List<ClipboardEntry>>
    fun observeRecentEntries(limit: Int): Flow<List<ClipboardEntry>>
    fun observeEntryById(id: Long): Flow<ClipboardEntry?>
    suspend fun setPinned(id: Long, pinned: Boolean)
    suspend fun deleteEntry(id: Long)
    suspend fun countEntries(): Int
    suspend fun importEntry(
        content: String,
        sourceApp: String?,
        capturedAtMillis: Long,
        contentType: ClipContentType,
        isSensitive: Boolean
    )
    suspend fun deleteOlderThan(thresholdMillis: Long)
    suspend fun seedDemoDataIfEmpty()
}
