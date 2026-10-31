package com.offlinepay.domain.model

import java.math.BigDecimal

/**
 * Core domain representation of a payment destination.
 * Both QR code scans (UPI ID) and Contact selections (Mobile Number)
 * produce a PaymentTarget and pass through the same confirmation screen.
 */
sealed class PaymentTarget {
    abstract val amount: BigDecimal?

    data class UpiId(
        val vpa: String,
        val name: String?,
        override val amount: BigDecimal?,
        val note: String?
    ) : PaymentTarget() {
        /**
         * Returns the handle portion of the VPA before the '@'.
         */
        val handle: String
            get() = vpa.substringBefore('@')

        /**
         * Returns the PSP domain portion of the VPA after the '@'.
         */
        val pspDomain: String
            get() = vpa.substringAfter('@', "")
    }

    data class MobileNumber(
        val number: String,
        val contactName: String?,
        override val amount: BigDecimal?
    ) : PaymentTarget()
}
