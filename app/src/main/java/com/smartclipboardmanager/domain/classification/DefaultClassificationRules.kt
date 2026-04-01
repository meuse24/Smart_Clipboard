package com.smartclipboardmanager.domain.classification

import com.smartclipboardmanager.domain.model.ClipContentType

class OtpRule : ClassificationRule {
    override val type: ClipContentType = ClipContentType.OTP
    override val priority: Int = 100

    private val isolatedOtpRegex = Regex("^\\d{4,8}$")
    private val contextualOtpRegex = Regex("\\b\\d{4,8}\\b")
    private val otpKeywords = listOf("otp", "code", "verification", "passcode", "2fa", "auth", "tan")

    override fun matches(text: NormalizedClipboardText): Boolean {
        val value = text.trimmed.lowercase()
        if (isolatedOtpRegex.matches(value)) {
            return true
        }

        val containsKeyword = otpKeywords.any { value.contains(it) }
        return containsKeyword && contextualOtpRegex.containsMatchIn(value)
    }
}

class IbanRule : ClassificationRule {
    override val type: ClipContentType = ClipContentType.IBAN
    override val priority: Int = 90

    private val ibanRegex = Regex("^[A-Z]{2}\\d{2}[A-Z0-9]{10,30}$")

    override fun matches(text: NormalizedClipboardText): Boolean {
        val iban = text.ibanCandidate
        if (!ibanRegex.matches(iban)) {
            return false
        }
        return isValidIbanChecksum(iban)
    }

    private fun isValidIbanChecksum(iban: String): Boolean {
        val rearranged = iban.drop(4) + iban.take(4)
        var remainder = 0L

        for (char in rearranged) {
            val transformed = if (char.isLetter()) {
                (char.code - 'A'.code + 10).toString()
            } else {
                char.toString()
            }

            for (digit in transformed) {
                remainder = (remainder * 10L + (digit - '0').toLong()) % 97L
            }
        }

        return remainder == 1L
    }
}

class EmailRule : ClassificationRule {
    override val type: ClipContentType = ClipContentType.EMAIL
    override val priority: Int = 80

    private val emailRegex = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)

    override fun matches(text: NormalizedClipboardText): Boolean {
        return emailRegex.matches(text.trimmed)
    }
}

class UrlRule : ClassificationRule {
    override val type: ClipContentType = ClipContentType.URL
    override val priority: Int = 70

    private val urlRegex = Regex(
        pattern = "^(https?://|ftp://|www\\.)[^\\s/$.?#].[^\\s]*$",
        option = RegexOption.IGNORE_CASE
    )

    override fun matches(text: NormalizedClipboardText): Boolean {
        val value = text.trimmed
        if (value.contains(' ') || value.contains('\n')) {
            return false
        }
        return urlRegex.matches(value)
    }
}

class JsonRule : ClassificationRule {
    override val type: ClipContentType = ClipContentType.JSON
    override val priority: Int = 60

    override fun matches(text: NormalizedClipboardText): Boolean {
        val value = text.trimmed
        if (value.length < 2) return false

        val looksLikeObject = value.startsWith('{') && value.endsWith('}') && value.contains(':')
        val looksLikeArray = value.startsWith('[') && value.endsWith(']')

        if (!looksLikeObject && !looksLikeArray) return false

        return hasBalancedJsonBrackets(value)
    }

    private fun hasBalancedJsonBrackets(value: String): Boolean {
        var braces = 0
        var brackets = 0
        var inString = false
        var escaped = false

        for (char in value) {
            if (escaped) {
                escaped = false
                continue
            }
            if (char == '\\') {
                escaped = true
                continue
            }
            if (char == '"') {
                inString = !inString
                continue
            }
            if (inString) continue

            when (char) {
                '{' -> braces++
                '}' -> braces--
                '[' -> brackets++
                ']' -> brackets--
            }

            if (braces < 0 || brackets < 0) {
                return false
            }
        }

        return braces == 0 && brackets == 0 && !inString
    }
}

class ColorRule : ClassificationRule {
    override val type: ClipContentType = ClipContentType.COLOR
    override val priority: Int = 65

    private val hexRegex = Regex(
        "^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$"
    )
    private val rgbRegex = Regex(
        "^rgba?\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*,\\s*\\d{1,3}(\\s*,\\s*[\\d.]+)?\\s*\\)$",
        RegexOption.IGNORE_CASE
    )
    private val hslRegex = Regex(
        "^hsla?\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}%\\s*,\\s*\\d{1,3}%(\\s*,\\s*[\\d.]+)?\\s*\\)$",
        RegexOption.IGNORE_CASE
    )

    override fun matches(text: NormalizedClipboardText): Boolean {
        val value = text.trimmed
        return hexRegex.matches(value) || rgbRegex.matches(value) || hslRegex.matches(value)
    }
}

class PhoneNumberRule : ClassificationRule {
    override val type: ClipContentType = ClipContentType.PHONE_NUMBER
    override val priority: Int = 50

    private val allowedPhoneChars = Regex("^[+()\\-./\\s\\d]{7,25}$")

    override fun matches(text: NormalizedClipboardText): Boolean {
        val value = text.trimmed
        if (!allowedPhoneChars.matches(value)) {
            return false
        }

        val digitCount = text.compactDigits.length
        return digitCount in 7..15
    }
}

class GeoLocationRule : ClassificationRule {
    override val type: ClipContentType = ClipContentType.GEO_LOCATION
    override val priority: Int = 45

    // Decimal degrees: 48.20849, 16.37208  or  48.20849,16.37208
    private val decimalCoordsRegex = Regex(
        "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)\\s*,\\s*[-+]?(180(\\.0+)?|(1[0-7]\\d|\\d{1,2})(\\.\\d+)?)$"
    )

    override fun matches(text: NormalizedClipboardText): Boolean {
        if (text.lineCount > 1) return false
        return decimalCoordsRegex.matches(text.trimmed)
    }
}

class CodeSnippetRule : ClassificationRule {
    override val type: ClipContentType = ClipContentType.CODE_SNIPPET
    override val priority: Int = 40

    private val codeMarkers = listOf(
        "{", "}", ";", "=>", "::", "==", "!=", "&&", "||", "()",
        "fun ", "class ", "interface ", "def ", "function ", "return ", "import ",
        "const ", "let ", "var ", "public ", "private ", "if (", "for (", "while ("
    )

    override fun matches(text: NormalizedClipboardText): Boolean {
        val value = text.trimmed
        if (text.lineCount < 2) return false

        val lower = value.lowercase()
        val markerHits = codeMarkers.count { marker -> lower.contains(marker.lowercase()) }

        val hasIndentedLines = value.lines().any { line -> line.startsWith("    ") || line.startsWith("\t") }
        val hasCommentSyntax = lower.contains("//") || lower.contains("/*") || lower.contains("#include")

        return markerHits >= 2 || (markerHits >= 1 && (hasIndentedLines || hasCommentSyntax))
    }
}

class MultilineTextRule : ClassificationRule {
    override val type: ClipContentType = ClipContentType.MULTI_LINE_TEXT
    override val priority: Int = 10

    override fun matches(text: NormalizedClipboardText): Boolean {
        return text.lineCount >= 2
    }
}
