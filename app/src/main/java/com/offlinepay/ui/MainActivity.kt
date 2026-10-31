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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.offlinepay.OfflinePayApplication
import com.offlinepay.data.local.entity.RiskLevel
import com.offlinepay.domain.model.PaymentTarget
import com.offlinepay.ui.confirm.ConfirmationScreen
import com.offlinepay.ui.history.HistoryScreen
import com.offlinepay.ui.scan.ScanScreen
import com.offlinepay.ui.theme.OfflinePayTheme
import kotlinx.coroutines.launch

sealed class Screen {
    object Scan : Screen()
    data class Confirm(val target: PaymentTarget) : Screen()
    object History : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as OfflinePayApplication).container.paymentRepository

        setContent {
            OfflinePayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Scan) }
                    val coroutineScope = rememberCoroutineScope()

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState is Screen.Confirm || targetState is Screen.History) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }
                        },
                        label = "screen_navigation"
                    ) { screen ->
                        when (screen) {
                            is Screen.Scan -> {
                                ScanScreen(
                                    onPaymentTargetResolved = { scanned ->
                                        currentScreen = Screen.Confirm(scanned)
                                    },
                                    onNavigateToHistory = {
                                        currentScreen = Screen.History
                                    },
                                    onNavigateToSettings = {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Settings (Available in Phase 6)",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                            is Screen.Confirm -> {
                                ConfirmationScreen(
                                    target = screen.target,
                                    onProceedToPay = { confirmedTarget, isTrusted ->
                                        val payee = when (confirmedTarget) {
                                            is PaymentTarget.UpiId -> confirmedTarget.vpa
                                            is PaymentTarget.MobileNumber -> "${confirmedTarget.contactName ?: "Contact"} (${confirmedTarget.number})"
                                        }
                                        val amt = confirmedTarget.amount?.toDouble() ?: 0.0

                                        coroutineScope.launch {
                                            repository.recordTransaction(
                                                target = confirmedTarget,
                                                amount = amt,
                                                riskScore = 0.1f,
                                                riskLevel = RiskLevel.LOW,
                                                riskReasons = listOf("Payee format verified offline"),
                                                handoffMode = "Dialer USSD (*99#)",
                                                userConfirmed = true,
                                                isTrusted = isTrusted
                                            )
                                        }

                                        val msg = "Proceeding with ₹$amt to $payee (Recorded in History)"
                                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                                        currentScreen = Screen.Scan
                                    },
                                    onCancel = {
                                        currentScreen = Screen.Scan
                                    }
                                )
                            }
                            is Screen.History -> {
                                HistoryScreen(
                                    repository = repository,
                                    onNavigateBack = {
                                        currentScreen = Screen.Scan
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
