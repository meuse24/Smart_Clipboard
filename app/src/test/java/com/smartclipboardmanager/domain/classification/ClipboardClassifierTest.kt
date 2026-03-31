package com.smartclipboardmanager.domain.classification

import com.smartclipboardmanager.domain.model.ClipContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardClassifierTest {

    private val classifier = ClipboardClassifier(ClipboardTextNormalizer())

    @Test
    fun `classifies URL`() {
        val result = classifier.classify("https://example.org/path?a=1")

        assertEquals(ClipContentType.URL, result.primaryType)
        assertTrue(result.matchedTypes.contains(ClipContentType.URL))
    }

    @Test
    fun `classifies phone number`() {
        val result = classifier.classify("+43 664 123 45 67")

        assertEquals(ClipContentType.PHONE_NUMBER, result.primaryType)
    }

    @Test
    fun `classifies email`() {
        val result = classifier.classify("alice.smith+ops@example.com")

        assertEquals(ClipContentType.EMAIL, result.primaryType)
    }

    @Test
    fun `classifies OTP from contextual message`() {
        val result = classifier.classify("Your verification code is 728194")

        assertEquals(ClipContentType.OTP, result.primaryType)
    }

    @Test
    fun `classifies JSON object`() {
        val result = classifier.classify("{\"id\":42,\"name\":\"entry\"}")

        assertEquals(ClipContentType.JSON, result.primaryType)
    }

    @Test
    fun `classifies valid IBAN`() {
        val result = classifier.classify("DE89 3704 0044 0532 0130 00")

        assertEquals(ClipContentType.IBAN, result.primaryType)
    }

    @Test
    fun `classifies multiline text`() {
        val result = classifier.classify("Zeile 1\nZeile 2\nZeile 3")

        assertEquals(ClipContentType.MULTI_LINE_TEXT, result.primaryType)
    }

    @Test
    fun `classifies possible code snippet`() {
        val input = """
            fun greet(name: String): String {
                return "Hello, ${'$'}name"
            }
        """.trimIndent()

        val result = classifier.classify(input)

        assertEquals(ClipContentType.CODE_SNIPPET, result.primaryType)
        assertTrue(result.matchedTypes.contains(ClipContentType.MULTI_LINE_TEXT))
    }

    @Test
    fun `applies priority OTP over phone number for plain digits`() {
        val result = classifier.classify("123456")

        assertEquals(ClipContentType.OTP, result.primaryType)
        assertTrue(result.matchedTypes.contains(ClipContentType.OTP))
    }

    @Test
    fun `applies priority JSON over multiline text`() {
        val input = """
            {
              "id": 1,
              "active": true
            }
        """.trimIndent()

        val result = classifier.classify(input)

        assertEquals(ClipContentType.JSON, result.primaryType)
        assertTrue(result.matchedTypes.contains(ClipContentType.MULTI_LINE_TEXT))
    }

    @Test
    fun `falls back to plain text`() {
        val result = classifier.classify("ein normaler kurzer text")

        assertEquals(ClipContentType.PLAIN_TEXT, result.primaryType)
    }
}
