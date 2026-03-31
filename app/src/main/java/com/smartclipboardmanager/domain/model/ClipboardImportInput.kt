package com.smartclipboardmanager.domain.model

data class ClipboardImportInput(
    val content: String,
    val sourceApp: String?,
    val capturedAtMillis: Long = System.currentTimeMillis()
)
