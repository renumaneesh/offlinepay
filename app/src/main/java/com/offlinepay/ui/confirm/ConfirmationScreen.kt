package com.offlinepay.ui.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offlinepay.R
import com.offlinepay.domain.model.PaymentTarget
import com.offlinepay.ui.components.DisplayRiskLevel
import com.offlinepay.ui.components.RiskBanner
import com.offlinepay.ui.theme.AccentCyan
import com.offlinepay.ui.theme.DarkBackground
import com.offlinepay.ui.theme.DarkSurface
import com.offlinepay.ui.theme.DarkSurfaceBorder
import com.offlinepay.ui.theme.DarkSurfaceElevated
import com.offlinepay.ui.theme.PrimaryBlue
import com.offlinepay.ui.theme.RiskHigh
import com.offlinepay.ui.theme.RiskLow
import com.offlinepay.ui.theme.TextMuted
import com.offlinepay.ui.theme.TextPrimary
import com.offlinepay.ui.theme.TextSecondary
import java.math.BigDecimal
import java.util.regex.Pattern

private val AMOUNT_REGEX = Pattern.compile("^[0-9]+(\\.[0-9]{1,2})?$")

@Composable
fun ConfirmationScreen(
    target: PaymentTarget,
    onProceedToPay: (PaymentTarget, Boolean) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialAmount = target.amount
    val isAmountLocked = initialAmount != null

    var enteredAmountText by remember {
        mutableStateOf(initialAmount?.toPlainString() ?: "")
    }
    var isTrustedPayee by remember { mutableStateOf(false) }

    // Real-time amount validation
    val amountValidationResult by remember {
        derivedStateOf {
            val text = enteredAmountText.trim()
            when {
                text.isEmpty() -> AmountValidation.Invalid(R.string.amount_required)
                !AMOUNT_REGEX.matcher(text).matches() -> AmountValidation.Invalid(R.string.amount_invalid_format)
                else -> {
                    try {
                        val parsed = BigDecimal(text)
                        if (parsed <= BigDecimal.ZERO) {
                            AmountValidation.Invalid(R.string.amount_positive)
                        } else {
                            AmountValidation.Valid(parsed)
                        }
                    } catch (e: Exception) {
                        AmountValidation.Invalid(R.string.amount_invalid_format)
                    }
                }
            }
        }
    }

    val isProceedEnabled = amountValidationResult is AmountValidation.Valid
    val scrollState = rememberScrollState()

    val (payeeName, payeeId, isUpiTarget, note) = when (target) {
        is PaymentTarget.UpiId -> Quad(
            target.name ?: stringResource(id = R.string.unknown_payee),
            target.vpa,
            true,
            target.note
        )
        is PaymentTarget.MobileNumber -> Quad(
            target.contactName ?: stringResource(id = R.string.unknown_payee),
            target.number,
            false,
            null
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(44.dp)
                    .background(DarkSurface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.cancel_payment),
                    tint = TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.confirm_payment_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = stringResource(id = R.string.confirm_payment_subtitle),
                    fontSize = 12.sp,
                    color = AccentCyan
                )
            }

            Spacer(modifier = Modifier.size(44.dp))
        }

        // 1. Payee Identity Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(AccentCyan.copy(alpha = 0.12f), CircleShape)
                        .border(1.5.dp, AccentCyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUpiTarget) Icons.Default.QrCode else Icons.Default.Phone,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.paying_to),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = payeeName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = payeeId,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AccentCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 2. Amount Section (Editable or Locked)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.amount_label),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    if (isAmountLocked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Set by QR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentCyan
                            )
                        }
                    }
                }

                if (isAmountLocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = stringResource(id = R.string.currency_inr),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = initialAmount!!.toPlainString(),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = enteredAmountText,
                        onValueChange = { enteredAmountText = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.enter_amount_hint),
                                color = TextMuted,
                                fontSize = 18.sp
                            )
                        },
                        leadingIcon = {
                            Text(
                                text = stringResource(id = R.string.currency_inr),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan,
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = amountValidationResult is AmountValidation.Invalid,
                        supportingText = {
                            if (amountValidationResult is AmountValidation.Invalid) {
                                val errRes = (amountValidationResult as AmountValidation.Invalid).errorRes
                                Text(
                                    text = stringResource(id = errRes),
                                    color = RiskHigh,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            errorBorderColor = RiskHigh
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        // 3. Note / Remark Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Notes,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.note_label),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Text(
                        text = note ?: stringResource(id = R.string.no_note_provided),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (note != null) TextPrimary else TextMuted
                    )
                }
            }
        }

        // 4. On-Device AI Fraud Risk Banner (Redesigned & Responsive)
        RiskBanner(
            riskLevel = DisplayRiskLevel.LOW,
            reasons = emptyList()
        )

        // 5. Mark Payee as Trusted Toggle
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (isTrustedPayee) RiskLow.copy(alpha = 0.15f) else Color(0x33788293),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (isTrustedPayee) RiskLow else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(id = R.string.trust_payee_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(id = R.string.trust_payee_desc),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Switch(
                    checked = isTrustedPayee,
                    onCheckedChange = { isTrustedPayee = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = RiskLow,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 6. Large High-Contrast Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (amountValidationResult is AmountValidation.Valid) {
                        val validAmount = (amountValidationResult as AmountValidation.Valid).amount
                        val resolvedTarget = when (target) {
                            is PaymentTarget.UpiId -> target.copy(amount = validAmount)
                            is PaymentTarget.MobileNumber -> target.copy(amount = validAmount)
                        }
                        onProceedToPay(resolvedTarget, isTrustedPayee)
                    }
                },
                enabled = isProceedEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.35f),
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.proceed_to_pay),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Text(
                    text = stringResource(id = R.string.cancel_payment),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private sealed class AmountValidation {
    data class Valid(val amount: BigDecimal) : AmountValidation()
    data class Invalid(val errorRes: Int) : AmountValidation()
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
