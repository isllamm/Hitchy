package com.hitchy.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.price_per_seat_large
import hitchy.shared.generated.resources.price_per_seat_small
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class PriceTagSize {
    Large,
    Small,
}

@Composable
fun PriceTag(
    amountEgp: Int,
    modifier: Modifier = Modifier,
    size: PriceTagSize = PriceTagSize.Large,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current

    if (size == PriceTagSize.Large) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(LocalHitchySpacing.current.space4),
        ) {
            Text(
                text = stringResource(Res.string.price_per_seat_large, amountEgp),
                style = typography.title,
                color = colors.navy,
            )
        }
    } else {
        Text(
            text = stringResource(Res.string.price_per_seat_small, amountEgp),
            style = typography.bodyStrong,
            color = colors.navy,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
fun PriceTagLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                PriceTag(amountEgp = 45, size = PriceTagSize.Large)
                PriceTag(amountEgp = 45, size = PriceTagSize.Small)
            }
        }
    }
}

@Preview
@Composable
fun PriceTagRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        PriceTagLtrPreview()
    }
}
