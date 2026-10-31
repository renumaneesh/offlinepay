package com.offlinepay.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.offlinepay.domain.model.PaymentTarget
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
                    var resolvedTarget by remember { mutableStateOf<PaymentTarget?>(null) }

                    ScanScreen(
                        onPaymentTargetResolved = { target ->
                            resolvedTarget = target
                            val msg = when (target) {
                                is PaymentTarget.UpiId -> "Scanned UPI: ${target.vpa} (Amount: ${target.amount ?: "None"})"
                                is PaymentTarget.MobileNumber -> "Selected Mobile: ${target.number}"
                            }
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                        },
                        onNavigateToHistory = {
                            Toast.makeText(this@MainActivity, "History (Available in Phase 3)", Toast.LENGTH_SHORT).show()
                        },
                        onNavigateToSettings = {
                            Toast.makeText(this@MainActivity, "Settings (Available in Phase 6)", Toast.LENGTH_SHORT).show()
                        },
                        onPayToContactClicked = {
                            Toast.makeText(this@MainActivity, "Pay to Contact (Available in Phase 2b)", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}
