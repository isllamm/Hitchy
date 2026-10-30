package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import hitchy.shared.generated.resources.seat_full
import hitchy.shared.generated.resources.seat_left_1
import hitchy.shared.generated.resources.seat_left_2
import hitchy.shared.generated.resources.seat_left_3
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SeatIndicator(
    availableSeats: Int,
    modifier: Modifier = Modifier,
    totalSeats: Int = 3,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val safeTotal = totalSeats.coerceIn(1, 3)
    val safeAvailable = availableSeats.coerceIn(0, safeTotal)

    val label = when (safeAvailable) {
        0 -> stringResource(Res.string.seat_full)
        1 -> stringResource(Res.string.seat_left_1)
        2 -> stringResource(Res.string.seat_left_2)
        else -> stringResource(Res.string.seat_left_3)
    }

    val textColor = if (safeAvailable == 0) colors.textDisabled else colors.purple

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space8),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0 until safeTotal) {
                val isAvailable = i < safeAvailable
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isAvailable) colors.purple else colors.white)
                        .border(1.dp, if (isAvailable) colors.purple else colors.border, CircleShape),
                )
            }
        }

        Text(
            text = label,
            style = typography.captionStrong,
            color = textColor,
        )
    }
}

@Preview
@Composable
fun SeatIndicatorLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                SeatIndicator(availableSeats = 3)
                SeatIndicator(availableSeats = 2)
                SeatIndicator(availableSeats = 1)
                SeatIndicator(availableSeats = 0)
            }
        }
    }
}

@Preview
@Composable
fun SeatIndicatorRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        SeatIndicatorLtrPreview()
    }
}
