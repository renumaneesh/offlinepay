package com.offlinepay.util

import com.offlinepay.data.local.entity.PayeeType
import com.offlinepay.domain.model.PaymentTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PayeeNormalizerTest {

    @Test
    fun normalize_mobileNumberTarget_normalizesTo10Digits() {
        val target = PaymentTarget.MobileNumber(
            number = "+91 98765 43210",
            contactName = "Ramesh Kumar",
            amount = BigDecimal("100.00")
        )

        val normalized = PayeeNormalizer.normalize(target)

        assertEquals("9876543210", normalized.payeeId)
        assertEquals(PayeeType.MOBILE, normalized.payeeType)
        assertEquals("Ramesh Kumar", normalized.displayName)
        assertTrue(normalized.isNumericHandle)
    }

    @Test
    fun normalize_upiWith10DigitNumericHandle_normalizesTo10DigitPayeeId() {
        val target = PaymentTarget.UpiId(
            vpa = "9876543210@ybl",
            name = "Ramesh Kumar",
            amount = BigDecimal("100.00"),
            note = null
        )

        val normalized = PayeeNormalizer.normalize(target)

        // Must match the mobile number payeeId so frequency & trust are unified!
        assertEquals("9876543210", normalized.payeeId)
        assertEquals(PayeeType.UPI_ID, normalized.payeeType)
        assertEquals("9876543210@ybl", normalized.originalIdentifier)
        assertTrue(normalized.isNumericHandle)
    }

    @Test
    fun normalize_upiAndMobileForSamePerson_produceIdenticalPayeeId() {
        val mobileTarget = PaymentTarget.MobileNumber("9876543210", "John Doe", null)
        val upiTarget = PaymentTarget.UpiId("9876543210@paytm", "John Doe", null, null)

        val mobileNorm = PayeeNormalizer.normalize(mobileTarget)
        val upiNorm = PayeeNormalizer.normalize(upiTarget)

        assertEquals(mobileNorm.payeeId, upiNorm.payeeId)
        assertEquals("9876543210", mobileNorm.payeeId)
    }

    @Test
    fun normalize_merchantOrNamedUpi_usesLowercaseVpa() {
        val target = PaymentTarget.UpiId(
            vpa = "Shop@OKAxis",
            name = "Grocery Store",
            amount = BigDecimal("450.00"),
            note = "Groceries"
        )

        val normalized = PayeeNormalizer.normalize(target)

        assertEquals("shop@okaxis", normalized.payeeId)
        assertEquals(PayeeType.UPI_ID, normalized.payeeType)
        assertFalse(normalized.isNumericHandle)
    }

    @Test
    fun normalize_personalNamedUpi_usesLowercaseVpa() {
        val target = PaymentTarget.UpiId(
            vpa = "maneesh@ptyes",
            name = "Maneesh",
            amount = null,
            note = null
        )

        val normalized = PayeeNormalizer.normalize(target)

        assertEquals("maneesh@ptyes", normalized.payeeId)
        assertFalse(normalized.isNumericHandle)
    }

    @Test
    fun normalize_upiWith9Digits_doesNotTreatAsMobile() {
        val target = PaymentTarget.UpiId("123456789@ybl", "Short Number", null, null)
        val normalized = PayeeNormalizer.normalize(target)

        assertEquals("123456789@ybl", normalized.payeeId)
        assertFalse(normalized.isNumericHandle)
    }

    @Test
    fun normalize_upiWith11Digits_doesNotTreatAsMobile() {
        val target = PaymentTarget.UpiId("09876543210@ybl", "Trunk Zero", null, null)
        val normalized = PayeeNormalizer.normalize(target)

        assertEquals("09876543210@ybl", normalized.payeeId)
        assertFalse(normalized.isNumericHandle)
    }

    @Test
    fun normalize_upiStartingWith5_doesNotTreatAsMobile() {
        val target = PaymentTarget.UpiId("5551234567@okhdfcbank", "Invalid Prefix", null, null)
        val normalized = PayeeNormalizer.normalize(target)

        assertEquals("5551234567@okhdfcbank", normalized.payeeId)
        assertFalse(normalized.isNumericHandle)
    }
}
