package com.offlinepay.ui.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.offlinepay.R
import com.offlinepay.domain.model.PaymentTarget
import com.offlinepay.ui.components.ViewfinderOverlay
import com.offlinepay.ui.theme.AccentCyan
import com.offlinepay.ui.theme.DarkBackground
import com.offlinepay.ui.theme.DarkSurface
import com.offlinepay.ui.theme.DarkSurfaceBorder
import com.offlinepay.ui.theme.PrimaryBlue
import com.offlinepay.ui.theme.TextPrimary
import com.offlinepay.ui.theme.TextSecondary
import com.offlinepay.util.UpiParser

@Composable
fun ScanScreen(
    onPaymentTargetResolved: (PaymentTarget) -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onPayToContactClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var isTorchEnabled by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessingGalleryImage by remember { mutableStateOf(false) }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingGalleryImage = true
            decodeQrFromGalleryUri(context, uri) { result ->
                isProcessingGalleryImage = false
                result.fold(
                    onSuccess = { target ->
                        onPaymentTargetResolved(target)
                    },
                    onFailure = { err ->
                        errorMessage = err.localizedMessage ?: context.getString(R.string.gallery_no_qr_found)
                    }
                )
            }
        }
    }

    // Function to handle detected barcode from camera or gallery
    val handleScannedBarcode = { rawValue: String ->
        val parseResult = UpiParser.parse(rawValue)
        parseResult.fold(
            onSuccess = { payload ->
                onPaymentTargetResolved(payload.toPaymentTarget())
            },
            onFailure = { error ->
                errorMessage = error.localizedMessage ?: "Invalid UPI QR Code."
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        if (hasCameraPermission) {
            CameraPreview(
                isTorchEnabled = isTorchEnabled,
                onBarcodeDetected = handleScannedBarcode,
                modifier = Modifier.fillMaxSize()
            )
            ViewfinderOverlay(modifier = Modifier.fillMaxSize())
        } else {
            // High-contrast permission rationale UI
            CameraPermissionRationale(
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.app_name),
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Offline Assistant",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasCameraPermission) {
                    IconButton(
                        onClick = { isTorchEnabled = !isTorchEnabled },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (isTorchEnabled) AccentCyan else Color(0x99141A29),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = stringResource(
                                id = if (isTorchEnabled) R.string.torch_off else R.string.torch_on
                            ),
                            tint = if (isTorchEnabled) DarkBackground else TextPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onNavigateToHistory,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x99141A29), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = TextPrimary
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x99141A29), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary
                    )
                }
            }
        }

        // Bottom Action Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 36.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Scan from Gallery Button
                FilledTonalButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AccentCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.scan_from_gallery),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Pay to Contact Button (Prepared for Phase 2b)
                FilledTonalButton(
                    onClick = onPayToContactClicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AccentCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.pay_to_contact),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Error Dialog
        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = {
                    Text(
                        text = stringResource(id = R.string.invalid_qr_title),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Text(
                        text = errorMessage ?: "",
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { errorMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(text = stringResource(id = R.string.error_ok))
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

/**
 * Decodes a QR code from a saved gallery image URI using ML Kit's offline InputImage.
 */
private fun decodeQrFromGalleryUri(
    context: Context,
    imageUri: Uri,
    onComplete: (Result<PaymentTarget>) -> Unit
) {
    try {
        val inputImage = InputImage.fromFilePath(context, imageUri)
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val firstValid = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                if (firstValid != null) {
                    val parseResult = UpiParser.parse(firstValid.rawValue)
                    parseResult.fold(
                        onSuccess = { payload ->
                            onComplete(Result.success(payload.toPaymentTarget()))
                        },
                        onFailure = { err ->
                            onComplete(Result.failure(err))
                        }
                    )
                } else {
                    onComplete(
                        Result.failure(
                            IllegalArgumentException("No QR code detected in the selected image.")
                        )
                    )
                }
            }
            .addOnFailureListener { error ->
                onComplete(
                    Result.failure(
                        IllegalArgumentException("Failed to decode image: ${error.localizedMessage ?: "Unknown error"}")
                    )
                )
            }
    } catch (e: Exception) {
        onComplete(
            Result.failure(
                IllegalArgumentException("Unable to read image file: ${e.localizedMessage ?: "File read error"}")
            )
        )
    }
}

@Composable
private fun CameraPermissionRationale(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(AccentCyan.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = stringResource(id = R.string.camera_permission_required),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(id = R.string.camera_permission_rationale),
                    fontSize = 15.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = stringResource(id = R.string.grant_permission),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
