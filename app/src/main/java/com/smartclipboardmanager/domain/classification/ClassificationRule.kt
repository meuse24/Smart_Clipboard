package com.smartclipboardmanager.domain.classification

import com.smartclipboardmanager.domain.model.ClipContentType

interface ClassificationRule {
    val type: ClipContentType
    val priority: Int

    fun matches(text: NormalizedClipboardText): Boolean
}
