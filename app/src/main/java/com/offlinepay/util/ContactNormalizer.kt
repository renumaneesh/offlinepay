package com.offlinepay.util

import java.util.regex.Pattern

/**
 * Normalizes and validates Indian mobile numbers for offline USSD payments.
 * Ensures consistent 10-digit mobile number format matching ^[6-9][0-9]{9}$
 * without requiring any contact read permissions.
 */
object ContactNormalizer {

    private val MOBILE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$")

    /**
     * Normalizes a raw phone number string into a clean 10-digit Indian mobile number.
     *
     * @param rawNumber The phone number string selected from contacts or typed.
     * @return [Result.success] with the 10-digit string, or [Result.failure] with a descriptive reason.
     */
    fun normalize(rawNumber: String?): Result<String> {
        val trimmed = rawNumber?.trim()
        if (trimmed.isNullOrEmpty()) {
            return Result.failure(IllegalArgumentException("Phone number cannot be empty."))
        }

        // 1. Strip all formatting: spaces, hyphens, parentheses, dots, slashes
        var cleaned = trimmed.replace(Regex("[\\s\\-\\(\\)\\.\\/\\\\]"), "")

        // 2. Strip leading international dial code '+91' or '0091'
        if (cleaned.startsWith("+91")) {
            cleaned = cleaned.substring(3)
        } else if (cleaned.startsWith("0091")) {
            cleaned = cleaned.substring(4)
        } else if (cleaned.startsWith("91") && cleaned.length == 12) {
            cleaned = cleaned.substring(2)
        }

        // 3. Strip leading trunk prefix '0' (for 11-digit numbers like 09876543210)
        if (cleaned.startsWith("0") && cleaned.length == 11) {
            cleaned = cleaned.substring(1)
        }

        // 4. Validate exact 10 digits
        if (cleaned.length != 10) {
            return Result.failure(
                IllegalArgumentException("Invalid number length (${cleaned.length} digits). Expected a 10-digit Indian mobile number.")
            )
        }

        // 5. Validate starting digit (must start with 6, 7, 8, or 9 per Indian National Numbering Plan)
        if (!MOBILE_PATTERN.matcher(cleaned).matches()) {
            val firstChar = cleaned.first()
            return Result.failure(
                IllegalArgumentException("Invalid mobile number '$cleaned'. Indian mobile numbers must start with 6, 7, 8, or 9 (starts with '$firstChar').")
            )
        }

        return Result.success(cleaned)
    }
}
