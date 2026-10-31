package com.offlinepay.domain.model

import java.math.BigDecimal

/**
 * Parsed representation of a UPI QR URI (upi://pay?...)
 */
data class UpiPayload(
    val vpa: String,
    val name: String?,
    val amount: BigDecimal?,
    val currency: String?,
    val note: String?,
    val merchantCode: String? = null,
    val transactionRef: String? = null
) {
    /**
     * Converts this validated UPI payload into the unified domain PaymentTarget.
     */
    fun toPaymentTarget(): PaymentTarget.UpiId {
        return PaymentTarget.UpiId(
            vpa = vpa,
            name = name,
            amount = amount,
            note = note
        )
    }
}
