package com.smartclipboardmanager.domain.privacy

import com.smartclipboardmanager.domain.model.ClipContentType

class SensitiveContentPolicy {

    fun isSensitive(type: ClipContentType): Boolean {
        return when (type) {
            ClipContentType.OTP,
            ClipContentType.IBAN,
            ClipContentType.EMAIL,
            ClipContentType.PHONE_NUMBER -> true
            else -> false
        }
    }
}
