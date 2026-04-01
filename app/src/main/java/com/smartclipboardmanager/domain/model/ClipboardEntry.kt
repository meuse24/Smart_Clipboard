package com.smartclipboardmanager.domain.model

data class ClipboardEntry(
    val id: Long,
    val content: String,
    val sourceApp: String?,
    val createdAtMillis: Long,
    val isPinned: Boolean,
    val contentType: ClipContentType,
    val isSensitive: Boolean,
    val mediaUri: String? = null
)
