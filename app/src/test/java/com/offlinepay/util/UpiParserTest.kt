package com.offlinepay.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class UpiParserTest {

    @Test
    fun parse_validFullQr_returnsSuccess() {
        val qr = "upi://pay?pa=merchant@okaxis&pn=John%20Doe&am=150.50&cu=INR&tn=Lunch%20Bill"
        val result = UpiParser.parse(qr)

        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("merchant@okaxis", payload.vpa)
        assertEquals("John Doe", payload.name)
        assertEquals(BigDecimal("150.50"), payload.amount)
        assertEquals("INR", payload.currency)
        assertEquals("Lunch Bill", payload.note)
    }

    @Test
    fun parse_missingAmount_returnsSuccessWithNullAmount() {
        val qr = "upi://pay?pa=user@okhdfcbank&pn=Ramesh"
        val result = UpiParser.parse(qr)

        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("user@okhdfcbank", payload.vpa)
        assertEquals("Ramesh", payload.name)
        assertNull(payload.amount)
        assertEquals("INR", payload.currency)
    }

    @Test
    fun parse_missingName_returnsSuccessWithNullName() {
        val qr = "upi://pay?pa=9876543210@paytm&am=500.00"
        val result = UpiParser.parse(qr)

        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("9876543210@paytm", payload.vpa)
        assertNull(payload.name)
        assertEquals(BigDecimal("500.00"), payload.amount)
    }

    @Test
    fun parse_urlEncodedName_decodesCorrectly() {
        val qr = "upi://pay?pa=shop@icici&pn=Sri%20Krishna%20Stores%20%26%20Co&am=20.00"
        val result = UpiParser.parse(qr)

        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("shop@icici", payload.vpa)
        assertEquals("Sri Krishna Stores & Co", payload.name)
        assertEquals(BigDecimal("20.00"), payload.amount)
    }

    @Test
    fun parse_wrongScheme_returnsFailure() {
        val qr = "http://pay?pa=test@upi&am=10.00"
        val result = UpiParser.parse(qr)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception!!.message!!.contains("Not a valid UPI QR code"))
    }

    @Test
    fun parse_malformedUri_returnsFailure() {
        val qr = "upi://pay?pa=test@upi&am=abc"
        val result = UpiParser.parse(qr)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception!!.message!!.contains("Invalid amount format"))
    }

    @Test
    fun parse_nonInrCurrency_returnsFailure() {
        val qr = "upi://pay?pa=test@upi&cu=USD&am=10.00"
        val result = UpiParser.parse(qr)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception!!.message!!.contains("Unsupported currency"))
    }

    @Test
    fun parse_emptyString_returnsFailure() {
        val qr = ""
        val result = UpiParser.parse(qr)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception!!.message!!.contains("empty"))
    }

    @Test
    fun parse_invalidVpa_returnsFailure() {
        val qr = "upi://pay?pa=not-an-email-vpa&am=10.00"
        val result = UpiParser.parse(qr)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception!!.message!!.contains("Invalid payee UPI ID format"))
    }

    @Test
    fun parse_negativeAmount_returnsFailure() {
        val qr = "upi://pay?pa=valid@bank&am=-50.00"
        val result = UpiParser.parse(qr)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception!!.message!!.contains("Invalid amount format"))
    }

    @Test
    fun parse_zeroAmount_returnsFailure() {
        val qr = "upi://pay?pa=valid@bank&am=0"
        val result = UpiParser.parse(qr)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception!!.message!!.contains("greater than zero"))
    }

    @Test
    fun parse_moreThanTwoDecimals_returnsFailure() {
        val qr = "upi://pay?pa=valid@bank&am=10.123"
        val result = UpiParser.parse(qr)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception!!.message!!.contains("at most 2 decimal places"))
    }

    @Test
    fun toPaymentTarget_convertsCorrectly() {
        val qr = "upi://pay?pa=shop@axis&pn=Shopkeeper&am=75.00&tn=Tea"
        val payload = UpiParser.parse(qr).getOrThrow()
        val target = payload.toPaymentTarget()

        assertEquals("shop@axis", target.vpa)
        assertEquals("Shopkeeper", target.name)
        assertEquals(BigDecimal("75.00"), target.amount)
        assertEquals("Tea", target.note)
        assertEquals("shop", target.handle)
        assertEquals("axis", target.pspDomain)
    }
}
