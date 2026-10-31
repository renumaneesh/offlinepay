package com.offlinepay.ui.confirm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.regex.Pattern

class AmountValidationTest {

    private val amountRegex = Pattern.compile("^[0-9]+(\\.[0-9]{1,2})?$")

    private fun validateAmount(rawText: String?): Result<BigDecimal> {
        val text = rawText?.trim()
        if (text.isNullOrEmpty()) {
            return Result.failure(IllegalArgumentException("Amount is required"))
        }
        if (!amountRegex.matcher(text).matches()) {
            return Result.failure(IllegalArgumentException("Maximum 2 decimal places allowed"))
        }
        return try {
            val parsed = BigDecimal(text)
            if (parsed <= BigDecimal.ZERO) {
                Result.failure(IllegalArgumentException("Amount must be greater than ₹0"))
            } else {
                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Invalid format"))
        }
    }

    @Test
    fun validateAmount_validInteger_returnsSuccess() {
        val result = validateAmount("500")
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("500"), result.getOrThrow())
    }

    @Test
    fun validateAmount_validDecimal_returnsSuccess() {
        val result = validateAmount("150.75")
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("150.75"), result.getOrThrow())
    }

    @Test
    fun validateAmount_singleDecimal_returnsSuccess() {
        val result = validateAmount("20.5")
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("20.5"), result.getOrThrow())
    }

    @Test
    fun validateAmount_withWhitespace_trimsAndSucceeds() {
        val result = validateAmount("  99.99  ")
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("99.99"), result.getOrThrow())
    }

    @Test
    fun validateAmount_emptyOrNull_returnsFailure() {
        val res1 = validateAmount("")
        val res2 = validateAmount("   ")
        val res3 = validateAmount(null)

        assertTrue(res1.isFailure)
        assertTrue(res2.isFailure)
        assertTrue(res3.isFailure)
        assertEquals("Amount is required", res1.exceptionOrNull()?.message)
    }

    @Test
    fun validateAmount_zero_returnsFailure() {
        val result = validateAmount("0")
        assertTrue(result.isFailure)
        assertEquals("Amount must be greater than ₹0", result.exceptionOrNull()?.message)
    }

    @Test
    fun validateAmount_negative_returnsFailure() {
        val result = validateAmount("-50.00")
        assertTrue(result.isFailure)
        assertEquals("Maximum 2 decimal places allowed", result.exceptionOrNull()?.message)
    }

    @Test
    fun validateAmount_threeDecimals_returnsFailure() {
        val result = validateAmount("10.999")
        assertTrue(result.isFailure)
        assertEquals("Maximum 2 decimal places allowed", result.exceptionOrNull()?.message)
    }

    @Test
    fun validateAmount_lettersOrSpecialChars_returnsFailure() {
        val result = validateAmount("500abc")
        assertTrue(result.isFailure)
    }
}
