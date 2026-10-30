package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PriceSummary(
    pricePerSeatEgp: Int,
    seatsCount: Int,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val totalEgp = pricePerSeatEgp * seatsCount

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.lilac)
            .padding(spacing.space16),
        verticalArrangement = Arrangement.spacedBy(spacing.space8),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "EGP $pricePerSeatEgp × $seatsCount seat${if (seatsCount > 1) "s" else ""}",
                style = typography.body,
                color = colors.textSecondary,
            )
            Text(
                text = "EGP $totalEgp",
                style = typography.bodyStrong,
                color = colors.navy,
            )
        }

        HorizontalDivider(color = colors.border, thickness = 1.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Total (Fuel share)",
                style = typography.bodyStrong,
                color = colors.navy,
            )
            Text(
                text = "EGP $totalEgp",
                style = typography.title,
                color = colors.purple,
            )
        }
    }
}

@Preview
@Composable
fun PriceSummaryLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            BoxPadding(modifier = Modifier.padding(spacing.space16)) {
                PriceSummary(pricePerSeatEgp = 45, seatsCount = 2)
            }
        }
    }
}

@Composable
private fun BoxPadding(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}

@Preview
@Composable
fun PriceSummaryRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        PriceSummaryLtrPreview()
    }
}
