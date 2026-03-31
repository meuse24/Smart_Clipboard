package com.smartclipboardmanager.domain.classification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardTextNormalizerTest {

    private val normalizer = ClipboardTextNormalizer()

    @Test
    fun `normalizes line endings and trims`() {
        val normalized = normalizer.normalize("  one\r\ntwo\rthree  ")

        assertEquals("one\ntwo\nthree", normalized.trimmed)
        assertEquals(3, normalized.lineCount)
    }

    @Test
    fun `extracts compact digits and iban candidate`() {
        val normalized = normalizer.normalize("DE89 3704-0044 0532 0130 00")

        assertEquals("89370400440532013000", normalized.compactDigits)
        assertEquals("DE89370400440532013000", normalized.ibanCandidate)
    }

    @Test
    fun `detects empty trimmed content`() {
        val normalized = normalizer.normalize(" \n \r\n  ")

        assertTrue(normalized.trimmed.isEmpty())
        assertEquals(0, normalized.lineCount)
        assertFalse(normalized.raw.isEmpty())
    }
}
