package com.offlinepay.util

import com.offlinepay.data.local.entity.PayeeType
import com.offlinepay.domain.model.PaymentTarget
import java.util.regex.Pattern

data class NormalizedPayee(
    val payeeId: String,
    val payeeType: PayeeType,
    val displayName: String?,
    val originalIdentifier: String,
    val isNumericHandle: Boolean
)

/**
 * Normalizes payment targets to a canonical payeeId.
 *
 * CRITICAL REQUIREMENT:
 * A UPI ID with a numeric handle like "9876543210@ybl" and the mobile number "9876543210"
 * refer to the same individual. Storing "9876543210" as payeeId for both ensures:
 * - "is_new_payee" check works consistently across QR and Contact flows
 * - Transaction frequency (1h, 24h, total count) is unified
 * - Payee trust status is shared across both methods
 */
object PayeeNormalizer {

    private val NUMERIC_MOBILE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$")

    fun normalize(target: PaymentTarget): NormalizedPayee {
        return when (target) {
            is PaymentTarget.MobileNumber -> {
                val cleanNumber = ContactNormalizer.normalize(target.number).getOrDefault(target.number)
                NormalizedPayee(
                    payeeId = cleanNumber,
                    payeeType = PayeeType.MOBILE,
                    displayName = target.contactName,
                    originalIdentifier = target.number,
                    isNumericHandle = true
                )
            }
            is PaymentTarget.UpiId -> {
                val vpa = target.vpa.trim()
                val handle = vpa.substringBefore('@').trim()
                if (NUMERIC_MOBILE_PATTERN.matcher(handle).matches()) {
                    // Numeric 10-digit UPI handle -> Canonical 10-digit payeeId
                    NormalizedPayee(
                        payeeId = handle,
                        payeeType = PayeeType.UPI_ID,
                        displayName = target.name,
                        originalIdentifier = vpa,
                        isNumericHandle = true
                    )
                } else {
                    // Standard named or merchant UPI ID (e.g. shop@okaxis, maneesh@ptyes)
                    NormalizedPayee(
                        payeeId = vpa.lowercase(),
                        payeeType = PayeeType.UPI_ID,
                        displayName = target.name,
                        originalIdentifier = vpa,
                        isNumericHandle = false
                    )
                }
            }
        }
    }
}
