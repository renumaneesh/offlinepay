package com.offlinepay.util

import com.offlinepay.domain.model.UpiPayload
import java.math.BigDecimal
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * Validates and parses raw QR code strings into [UpiPayload].
 * Ensures strict compliance with NPCI UPI specification while
 * providing user-friendly diagnostic messages for invalid codes.
 */
object UpiParser {

    private val VPA_PATTERN = Pattern.compile("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$")
    private val AMOUNT_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]{1,2})?$")

    /**
     * Parses a raw QR code string.
     *
     * @param rawQr The string scanned from the QR code or image.
     * @return [Result.success] with [UpiPayload] if valid, or [Result.failure] with an informative error message.
     */
    fun parse(rawQr: String?): Result<UpiPayload> {
        val trimmed = rawQr?.trim()
        if (trimmed.isNullOrEmpty()) {
            return Result.failure(IllegalArgumentException("QR code is empty or unscannable."))
        }

        // Fast scheme/prefix check
        val lower = trimmed.lowercase()
        if (!lower.startsWith("upi://pay")) {
            return Result.failure(
                IllegalArgumentException("Not a valid UPI QR code. Expected format starts with 'upi://pay'.")
            )
        }

        val uri: URI = try {
            URI.create(trimmed)
        } catch (e: Exception) {
            return Result.failure(
                IllegalArgumentException("Malformed QR code URL: ${e.localizedMessage ?: "Invalid URI syntax"}")
            )
        }

        val scheme = uri.scheme
        val host = uri.host
        if (!"upi".equals(scheme, ignoreCase = true) || !"pay".equals(host, ignoreCase = true)) {
            return Result.failure(
                IllegalArgumentException("Invalid scheme or host. Scheme must be 'upi' and host must be 'pay'.")
            )
        }

        val rawQuery = uri.rawQuery
        if (rawQuery.isNullOrEmpty()) {
            return Result.failure(
                IllegalArgumentException("UPI QR code contains no payment parameters.")
            )
        }

        val queryParams = parseQueryParams(rawQuery)

        // 1. Validate Payee VPA (pa)
        val vpa = queryParams["pa"]?.trim()
        if (vpa.isNullOrEmpty()) {
            return Result.failure(
                IllegalArgumentException("Missing payee UPI ID (pa parameter is required).")
            )
        }
        if (!VPA_PATTERN.matcher(vpa).matches()) {
            return Result.failure(
                IllegalArgumentException("Invalid payee UPI ID format: '$vpa'. Expected format: username@bank.")
            )
        }

        // 2. Validate Currency (cu)
        val currency = queryParams["cu"]?.trim()?.uppercase()
        if (!currency.isNullOrEmpty() && currency != "INR") {
            return Result.failure(
                IllegalArgumentException("Unsupported currency: '$currency'. Only Indian Rupees (INR) are supported.")
            )
        }

        // 3. Validate Amount (am)
        val rawAmount = queryParams["am"]?.trim()
        val amount: BigDecimal? = if (!rawAmount.isNullOrEmpty()) {
            if (!AMOUNT_PATTERN.matcher(rawAmount).matches()) {
                return Result.failure(
                    IllegalArgumentException("Invalid amount format: '$rawAmount'. Must be a positive number with at most 2 decimal places.")
                )
            }
            try {
                val parsed = BigDecimal(rawAmount)
                if (parsed <= BigDecimal.ZERO) {
                    return Result.failure(
                        IllegalArgumentException("Payment amount must be greater than zero.")
                    )
                }
                parsed
            } catch (e: Exception) {
                return Result.failure(
                    IllegalArgumentException("Unable to parse amount: '$rawAmount'.")
                )
            }
        } else {
            null
        }

        // 4. Optional metadata fields
        val name = queryParams["pn"]?.trim()?.takeIf { it.isNotEmpty() }
        val note = queryParams["tn"]?.trim()?.takeIf { it.isNotEmpty() }
        val merchantCode = queryParams["mc"]?.trim()?.takeIf { it.isNotEmpty() }
        val transactionRef = queryParams["tr"]?.trim()?.takeIf { it.isNotEmpty() }

        return Result.success(
            UpiPayload(
                vpa = vpa,
                name = name,
                amount = amount,
                currency = currency ?: "INR",
                note = note,
                merchantCode = merchantCode,
                transactionRef = transactionRef
            )
        )
    }

    /**
     * Splits and URL-decodes all query parameters from the raw query string.
     */
    private fun parseQueryParams(rawQuery: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val pairs = rawQuery.split("&")
        for (pair in pairs) {
            if (pair.isEmpty()) continue
            val idx = pair.indexOf("=")
            val key: String
            val value: String
            if (idx > 0) {
                key = pair.substring(0, idx)
                value = pair.substring(idx + 1)
            } else {
                key = pair
                value = ""
            }
            try {
                val decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8.name())
                val decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8.name())
                params[decodedKey] = decodedValue
            } catch (e: Exception) {
                // If decoding fails, store raw strings
                params[key] = value
            }
        }
        return params
    }
}
