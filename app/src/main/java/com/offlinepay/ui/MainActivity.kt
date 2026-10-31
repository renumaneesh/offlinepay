package com.offlinepay.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.offlinepay.domain.model.PaymentTarget
import com.offlinepay.ui.confirm.ConfirmationScreen
import com.offlinepay.ui.scan.ScanScreen
import com.offlinepay.ui.theme.OfflinePayTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfflinePayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentTarget by remember { mutableStateOf<PaymentTarget?>(null) }

                    AnimatedContent(
                        targetState = currentTarget,
                        transitionSpec = {
                            if (targetState != null) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }
                        },
                        label = "screen_transition"
                    ) { target ->
                        if (target == null) {
                            ScanScreen(
                                onPaymentTargetResolved = { scanned ->
                                    currentTarget = scanned
                                },
                                onNavigateToHistory = {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "History (Available in Phase 3)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onNavigateToSettings = {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Settings (Available in Phase 6)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onPayToContactClicked = {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Pay to Contact (Available in Phase 2b)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        } else {
                            ConfirmationScreen(
                                target = target,
                                onProceedToPay = { confirmedTarget, isTrusted ->
                                    val payee = when (confirmedTarget) {
                                        is PaymentTarget.UpiId -> confirmedTarget.vpa
                                        is PaymentTarget.MobileNumber -> confirmedTarget.number
                                    }
                                    val msg = "Proceeding with ₹${confirmedTarget.amount} to $payee (Trusted: $isTrusted)"
                                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                                    currentTarget = null
                                },
                                onCancel = {
                                    currentTarget = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
