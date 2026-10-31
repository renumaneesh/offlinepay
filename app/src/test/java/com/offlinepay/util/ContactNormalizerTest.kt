package com.offlinepay.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactNormalizerTest {

    @Test
    fun normalize_standard10DigitNumber_returnsSuccess() {
        val result = ContactNormalizer.normalize("9876543210")
        assertTrue(result.isSuccess)
        assertEquals("9876543210", result.getOrThrow())
    }

    @Test
    fun normalize_withPlus91AndSpaces_returnsNormalized10Digits() {
        val result = ContactNormalizer.normalize("+91 98765 43210")
        assertTrue(result.isSuccess)
        assertEquals("9876543210", result.getOrThrow())
    }

    @Test
    fun normalize_withPlus91NoSpaces_returnsNormalized10Digits() {
        val result = ContactNormalizer.normalize("+919876543210")
        assertTrue(result.isSuccess)
        assertEquals("9876543210", result.getOrThrow())
    }

    @Test
    fun normalize_withLeadingZero_returnsNormalized10Digits() {
        val result = ContactNormalizer.normalize("09876543210")
        assertTrue(result.isSuccess)
        assertEquals("9876543210", result.getOrThrow())
    }

    @Test
    fun normalize_withLeading91_returnsNormalized10Digits() {
        val result = ContactNormalizer.normalize("919876543210")
        assertTrue(result.isSuccess)
        assertEquals("9876543210", result.getOrThrow())
    }

    @Test
    fun normalize_withDashesAndParentheses_returnsNormalized10Digits() {
        val result = ContactNormalizer.normalize("(98765) 432-10")
        assertTrue(result.isSuccess)
        assertEquals("9876543210", result.getOrThrow())
    }

    @Test
    fun normalize_numberStartingWith6_returnsSuccess() {
        val result = ContactNormalizer.normalize("6301234567")
        assertTrue(result.isSuccess)
        assertEquals("6301234567", result.getOrThrow())
    }

    @Test
    fun normalize_numberStartingWith7_returnsSuccess() {
        val result = ContactNormalizer.normalize("7012345678")
        assertTrue(result.isSuccess)
        assertEquals("7012345678", result.getOrThrow())
    }

    @Test
    fun normalize_numberStartingWith8_returnsSuccess() {
        val result = ContactNormalizer.normalize("8901234567")
        assertTrue(result.isSuccess)
        assertEquals("8901234567", result.getOrThrow())
    }

    @Test
    fun normalize_invalidStartingDigit5_returnsFailure() {
        val result = ContactNormalizer.normalize("5551234567")
        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message
        assertNotNull(msg)
        assertTrue(msg!!.contains("must start with 6, 7, 8, or 9"))
    }

    @Test
    fun normalize_tooShort9Digits_returnsFailure() {
        val result = ContactNormalizer.normalize("987654321")
        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message
        assertNotNull(msg)
        assertTrue(msg!!.contains("9 digits"))
    }

    @Test
    fun normalize_tooLong12Digits_returnsFailure() {
        val result = ContactNormalizer.normalize("987654321099")
        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message
        assertNotNull(msg)
        assertTrue(msg!!.contains("12 digits"))
    }

    @Test
    fun normalize_containsAlphabetOrSpecialChars_returnsFailure() {
        val result = ContactNormalizer.normalize("98765ABCD0")
        assertTrue(result.isFailure)
    }

    @Test
    fun normalize_emptyOrNull_returnsFailure() {
        val res1 = ContactNormalizer.normalize("")
        val res2 = ContactNormalizer.normalize("   ")
        val res3 = ContactNormalizer.normalize(null)

        assertTrue(res1.isFailure)
        assertTrue(res2.isFailure)
        assertTrue(res3.isFailure)
    }
}
