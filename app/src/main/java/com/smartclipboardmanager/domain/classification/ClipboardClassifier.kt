package com.smartclipboardmanager.domain.classification

import com.smartclipboardmanager.domain.model.ClipContentType
import com.smartclipboardmanager.domain.model.ContentClassification
import javax.inject.Inject

class ClipboardClassifier @Inject constructor(
    private val normalizer: ClipboardTextNormalizer
) {

    private val sortedRules: List<ClassificationRule> = defaultRules()
        .sortedByDescending { it.priority }

    fun classify(input: String): ContentClassification {
        val normalized = normalizer.normalize(input)

        if (normalized.trimmed.isEmpty()) {
            return ContentClassification(
                primaryType = ClipContentType.PLAIN_TEXT,
                matchedTypes = setOf(ClipContentType.PLAIN_TEXT),
                normalizedContent = normalized.trimmed
            )
        }

        val matches = sortedRules
            .asSequence()
            .filter { it.matches(normalized) }
            .map { it.type }
            .toCollection(linkedSetOf())

        val finalMatches = if (matches.isEmpty()) {
            setOf(ClipContentType.PLAIN_TEXT)
        } else {
            matches
        }

        val primaryType = finalMatches.first()

        return ContentClassification(
            primaryType = primaryType,
            matchedTypes = finalMatches,
            normalizedContent = normalized.trimmed
        )
    }

    companion object {
        fun defaultRules(): List<ClassificationRule> = listOf(
            OtpRule(),
            IbanRule(),
            EmailRule(),
            UrlRule(),
            JsonRule(),
            ColorRule(),
            PhoneNumberRule(),
            GeoLocationRule(),
            CodeSnippetRule(),
            MultilineTextRule()
        )
    }
}
