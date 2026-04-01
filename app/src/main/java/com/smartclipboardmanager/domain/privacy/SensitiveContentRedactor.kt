package com.smartclipboardmanager.domain.privacy

import com.smartclipboardmanager.domain.model.ClipContentType

class SensitiveContentRedactor {

    fun redact(type: ClipContentType, content: String): String {
        return when (type) {
            ClipContentType.OTP -> "[hidden otp]"
            ClipContentType.EMAIL -> redactEmail(content)
            ClipContentType.IBAN -> redactIban(content)
            ClipContentType.PHONE_NUMBER -> redactPhone(content)
            else -> "[sensitive content hidden]"
        }
    }

    private fun redactEmail(value: String): String {
        val trimmed = value.trim()
        val atIndex = trimmed.indexOf('@')
        if (atIndex <= 0) return "[hidden email]"

        val local = trimmed.substring(0, atIndex)
        val domain = trimmed.substring(atIndex + 1)
        val localPrefix = local.first()
        return "$localPrefix***@$domain"
    }

    private fun redactIban(value: String): String {
        val compact = value.filterNot { it.isWhitespace() }
        if (compact.length < 8) return "[hidden iban]"
        return compact.take(4) + "****" + compact.takeLast(4)
    }

    private fun redactPhone(value: String): String {
        val digits = value.filter { it.isDigit() }
        if (digits.length < 7) return "[hidden phone]"
        return "***" + digits.takeLast(2)
    }
}
