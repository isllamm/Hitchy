package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fair_price_label
import hitchy.shared.generated.resources.needs_design_review
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PriceSlider(
    priceEgp: Int,
    onPriceChange: (Int) -> Unit,
    minPriceEgp: Int,
    maxPriceEgp: Int,
    fairPriceEgp: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val pillShape = RoundedCornerShape(50)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.space8),
    ) {
        // Design review badge
        Box(
            modifier = Modifier
                .clip(pillShape)
                .background(colors.warningBg)
                .padding(horizontal = spacing.space8, vertical = 2.dp),
        ) {
            Text(
                text = stringResource(Res.string.needs_design_review),
                style = typography.caption,
                color = colors.warningText,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "EGP $priceEgp",
                style = typography.h2,
                color = colors.purple,
            )

            if (priceEgp == fairPriceEgp) {
                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(colors.successBg)
                        .padding(horizontal = spacing.space8, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.fair_price_label),
                        style = typography.captionStrong,
                        color = colors.successText,
                    )
                }
            }
        }

        Slider(
            value = priceEgp.toFloat(),
            onValueChange = { onPriceChange(it.toInt()) },
            valueRange = minPriceEgp.toFloat()..maxPriceEgp.toFloat(),
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = colors.purple,
                activeTrackColor = colors.purple,
                inactiveTrackColor = colors.border,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Min: EGP $minPriceEgp",
                style = typography.caption,
                color = colors.textSecondary,
            )
            Text(
                text = "Recommended: EGP $fairPriceEgp",
                style = typography.captionStrong,
                color = colors.navy,
            )
            Text(
                text = "Max: EGP $maxPriceEgp",
                style = typography.caption,
                color = colors.textSecondary,
            )
        }
    }
}

@Preview
@Composable
fun PriceSliderLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Box(modifier = Modifier.padding(spacing.space16)) {
                PriceSlider(
                    priceEgp = 45,
                    onPriceChange = {},
                    minPriceEgp = 30,
                    maxPriceEgp = 70,
                    fairPriceEgp = 45,
                )
            }
        }
    }
}

@Preview
@Composable
fun PriceSliderRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        PriceSliderLtrPreview()
    }
}
