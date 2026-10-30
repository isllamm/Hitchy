package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

private const val MIN_SEATS = 1
private const val MAX_SEATS = 3

@Composable
fun PassengersStepper(
    count: Int,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minCount: Int = MIN_SEATS,
    maxCount: Int = MAX_SEATS,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    val canDecrease = enabled && count > minCount
    val canIncrease = enabled && count < maxCount

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space16),
    ) {
        // Minus button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (canDecrease) colors.lavender else colors.disabledSurface)
                .clickable(enabled = canDecrease) { onCountChange(count - 1) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "−",
                style = typography.title,
                color = if (canDecrease) colors.purple else colors.textDisabled,
            )
        }

        // Count display
        Text(
            text = count.toString(),
            style = typography.h2,
            color = colors.navy,
        )

        // Plus button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (canIncrease) colors.lavender else colors.disabledSurface)
                .clickable(enabled = canIncrease) { onCountChange(count + 1) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                style = typography.title,
                color = if (canIncrease) colors.purple else colors.textDisabled,
            )
        }
    }
}

@Preview
@Composable
fun PassengersStepperLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Box(modifier = Modifier.padding(spacing.space16)) {
                PassengersStepper(count = 2, onCountChange = {})
            }
        }
    }
}

@Preview
@Composable
fun PassengersStepperRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        PassengersStepperLtrPreview()
    }
}
