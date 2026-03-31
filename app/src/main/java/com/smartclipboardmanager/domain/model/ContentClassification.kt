package com.smartclipboardmanager.domain.model

data class ContentClassification(
    val primaryType: ClipContentType,
    val matchedTypes: Set<ClipContentType>,
    val normalizedContent: String
)
