package com.offlinepay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offlinepay.R
import com.offlinepay.ui.theme.DarkSurface
import com.offlinepay.ui.theme.RiskHigh
import com.offlinepay.ui.theme.RiskHighContainer
import com.offlinepay.ui.theme.RiskLow
import com.offlinepay.ui.theme.RiskLowContainer
import com.offlinepay.ui.theme.RiskMedium
import com.offlinepay.ui.theme.RiskMediumContainer
import com.offlinepay.ui.theme.TextPrimary
import com.offlinepay.ui.theme.TextSecondary

enum class DisplayRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

@Composable
fun RiskBanner(
    riskLevel: DisplayRiskLevel = DisplayRiskLevel.LOW,
    reasons: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val (statusColor, containerColor, labelRes, icon) = when (riskLevel) {
        DisplayRiskLevel.LOW -> Quad(
            RiskLow,
            RiskLowContainer,
            R.string.risk_level_low,
            Icons.Default.Shield
        )
        DisplayRiskLevel.MEDIUM -> Quad(
            RiskMedium,
            RiskMediumContainer,
            R.string.risk_level_medium,
            Icons.Default.Warning
        )
        DisplayRiskLevel.HIGH -> Quad(
            RiskHigh,
            RiskHighContainer,
            R.string.risk_level_high,
            Icons.Default.Warning
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, statusColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(containerColor, CircleShape)
                            .border(1.dp, statusColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.ai_safety_check_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Risk Badge Chip
                Box(
                    modifier = Modifier
                        .background(
                            color = containerColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, statusColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(id = labelRes),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (reasons.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (reason in reasons) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(text = "• ", color = statusColor, fontSize = 14.sp)
                            Text(
                                text = reason,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(id = R.string.risk_banner_safe_desc),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
