package com.offlinepay.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offlinepay.R
import com.offlinepay.ui.theme.AccentCyan
import com.offlinepay.ui.theme.ScrimColor
import com.offlinepay.ui.theme.TextPrimary
import kotlin.math.min

@Composable
fun ViewfinderOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_animation")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_sweep"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Calculate viewfinder box dimensions (square, 70% of min dimension, capped at 320dp)
            val boxSize = min(width * 0.75f, 320.dp.toPx())
            val left = (width - boxSize) / 2f
            val top = (height - boxSize) / 2.4f // Slightly elevated above vertical center
            val right = left + boxSize
            val bottom = top + boxSize
            val cornerRadius = 16.dp.toPx()

            val boxRect = Rect(left, top, right, bottom)
            val boxPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = boxRect,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }

            // Draw darkened scrim outside the box
            clipPath(boxPath, clipOp = ClipOp.Difference) {
                drawRect(color = ScrimColor)
            }

            // Draw subtle frame border
            drawRoundRect(
                color = AccentCyan.copy(alpha = 0.35f),
                topLeft = Offset(left, top),
                size = Size(boxSize, boxSize),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Draw corner accent brackets
            val bracketLength = 36.dp.toPx()
            val bracketStroke = 4.5.dp.toPx()
            val bracketColor = AccentCyan

            // Top-Left corner
            drawLine(
                color = bracketColor,
                start = Offset(left, top + bracketLength),
                end = Offset(left, top + cornerRadius),
                strokeWidth = bracketStroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(left + cornerRadius, top),
                end = Offset(left + bracketLength, top),
                strokeWidth = bracketStroke,
                cap = StrokeCap.Round
            )

            // Top-Right corner
            drawLine(
                color = bracketColor,
                start = Offset(right - bracketLength, top),
                end = Offset(right - cornerRadius, top),
                strokeWidth = bracketStroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(right, top + cornerRadius),
                end = Offset(right, top + bracketLength),
                strokeWidth = bracketStroke,
                cap = StrokeCap.Round
            )

            // Bottom-Left corner
            drawLine(
                color = bracketColor,
                start = Offset(left, bottom - bracketLength),
                end = Offset(left, bottom - cornerRadius),
                strokeWidth = bracketStroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(left + cornerRadius, bottom),
                end = Offset(left + bracketLength, bottom),
                strokeWidth = bracketStroke,
                cap = StrokeCap.Round
            )

            // Bottom-Right corner
            drawLine(
                color = bracketColor,
                start = Offset(right - bracketLength, bottom),
                end = Offset(right - cornerRadius, bottom),
                strokeWidth = bracketStroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(right, bottom - bracketLength),
                end = Offset(right, bottom - cornerRadius),
                strokeWidth = bracketStroke,
                cap = StrokeCap.Round
            )

            // Draw animated laser scanning line inside the box
            val laserY = top + (boxSize * laserPosition)
            val laserBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    AccentCyan.copy(alpha = 0.85f),
                    AccentCyan,
                    AccentCyan.copy(alpha = 0.85f),
                    Color.Transparent
                ),
                startX = left + 8.dp.toPx(),
                endX = right - 8.dp.toPx()
            )

            drawLine(
                brush = laserBrush,
                start = Offset(left + 8.dp.toPx(), laserY),
                end = Offset(right - 8.dp.toPx(), laserY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Guiding instruction banner under the viewfinder
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 260.dp)
                .background(
                    color = Color(0xCC141A29),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.align_qr_hint),
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
