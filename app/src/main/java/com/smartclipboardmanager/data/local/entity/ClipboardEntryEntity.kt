package com.smartclipboardmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_entries")
data class ClipboardEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val sourceApp: String?,
    val createdAtMillis: Long,
    val isPinned: Boolean,
    val contentType: String,
    val isSensitive: Boolean
)
