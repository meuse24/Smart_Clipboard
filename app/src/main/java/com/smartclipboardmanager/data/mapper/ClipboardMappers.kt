package com.smartclipboardmanager.data.mapper

import com.smartclipboardmanager.data.local.entity.ClipboardEntryEntity
import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.domain.model.ClipboardEntry

fun ClipboardEntryEntity.toDomain(): ClipboardEntry = ClipboardEntry(
    id = id,
    content = content,
    sourceApp = sourceApp,
    createdAtMillis = createdAtMillis,
    isPinned = isPinned,
    contentType = contentType.toClipContentType(),
    isSensitive = isSensitive
)

fun ClipboardEntry.toEntity(): ClipboardEntryEntity = ClipboardEntryEntity(
    id = id,
    content = content,
    sourceApp = sourceApp,
    createdAtMillis = createdAtMillis,
    isPinned = isPinned,
    contentType = contentType.name,
    isSensitive = isSensitive
)

private fun String.toClipContentType(): ClipContentType {
    return runCatching { ClipContentType.valueOf(this) }
        .getOrDefault(ClipContentType.PLAIN_TEXT)
}
