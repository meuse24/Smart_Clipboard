package com.smartclipboardmanager.domain.privacy

import com.smartclipboardmanager.domain.model.ClipContentType
import org.junit.Assert.assertEquals
import org.junit.Test

class SensitiveContentRedactorTest {

    private val redactor = SensitiveContentRedactor()

    @Test
    fun `redacts short phone numbers fully`() {
        val result = redactor.redact(ClipContentType.PHONE_NUMBER, "+1234")

        assertEquals("[hidden phone]", result)
    }

    @Test
    fun `keeps only last two digits for longer phone numbers`() {
        val result = redactor.redact(ClipContentType.PHONE_NUMBER, "+43 664 123 45 67")

        assertEquals("***67", result)
    }
}
