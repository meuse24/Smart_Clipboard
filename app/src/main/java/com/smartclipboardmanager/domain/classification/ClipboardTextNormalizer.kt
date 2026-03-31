package com.smartclipboardmanager.domain.classification

import javax.inject.Inject

data class NormalizedClipboardText(
    val raw: String,
    val trimmed: String,
    val canonicalNewLines: String,
    val lineCount: Int,
    val compactDigits: String,
    val ibanCandidate: String
)

class ClipboardTextNormalizer @Inject constructor() {

    fun normalize(input: String): NormalizedClipboardText {
        val canonical = input.replace("\r\n", "\n").replace("\r", "\n")
        val trimmed = canonical.trim()
        val lineCount = if (trimmed.isEmpty()) 0 else trimmed.split('\n').size
        val compactDigits = trimmed.filter { it.isDigit() }
        val ibanCandidate = trimmed
            .uppercase()
            .filterNot { it.isWhitespace() || it == '-' }

        return NormalizedClipboardText(
            raw = input,
            trimmed = trimmed,
            canonicalNewLines = canonical,
            lineCount = lineCount,
            compactDigits = compactDigits,
            ibanCandidate = ibanCandidate
        )
    }
}
