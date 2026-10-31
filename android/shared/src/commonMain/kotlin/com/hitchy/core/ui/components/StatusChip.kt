package com.hitchy.core.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyColors
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.status_cancelled
import hitchy.shared.generated.resources.status_completed
import hitchy.shared.generated.resources.status_confirmed
import hitchy.shared.generated.resources.status_declined
import hitchy.shared.generated.resources.status_expired
import hitchy.shared.generated.resources.status_in_progress
import hitchy.shared.generated.resources.status_no_show
import hitchy.shared.generated.resources.status_requested
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class RideStatusVariant {
    Requested,
    Confirmed,
    InProgress,
    Completed,
    Cancelled,
    Declined,
    Expired,
    NoShow,
}

private data class StatusVisuals(val bg: Color, val textColor: Color, val dotColor: Color)

private fun resolveStatusColors(status: RideStatusVariant, colors: HitchyColors): StatusVisuals = when (status) {
    RideStatusVariant.Requested -> StatusVisuals(colors.lavender, colors.purple, colors.purple)

    RideStatusVariant.Confirmed -> StatusVisuals(colors.infoBg, colors.infoText, colors.info)

    RideStatusVariant.InProgress -> StatusVisuals(colors.warningBg, colors.warningText, colors.warning)

    RideStatusVariant.Completed -> StatusVisuals(colors.successBg, colors.successText, colors.success)

    RideStatusVariant.Cancelled,
    RideStatusVariant.Declined,
    RideStatusVariant.Expired,
    RideStatusVariant.NoShow,
    -> StatusVisuals(colors.errorBg, colors.errorText, colors.error)
}

@Composable
private fun resolveStatusLabel(status: RideStatusVariant): String = when (status) {
    RideStatusVariant.Requested -> stringResource(Res.string.status_requested)
    RideStatusVariant.Confirmed -> stringResource(Res.string.status_confirmed)
    RideStatusVariant.InProgress -> stringResource(Res.string.status_in_progress)
    RideStatusVariant.Completed -> stringResource(Res.string.status_completed)
    RideStatusVariant.Cancelled -> stringResource(Res.string.status_cancelled)
    RideStatusVariant.Declined -> stringResource(Res.string.status_declined)
    RideStatusVariant.Expired -> stringResource(Res.string.status_expired)
    RideStatusVariant.NoShow -> stringResource(Res.string.status_no_show)
}

@Composable
fun StatusChip(
    status: RideStatusVariant,
    modifier: Modifier = Modifier,
    customLabel: String? = null,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val pillShape = RoundedCornerShape(50)

    val visuals = resolveStatusColors(status, colors)
    val label = customLabel ?: resolveStatusLabel(status)

    Row(
        modifier = modifier
            .clip(pillShape)
            .background(visuals.bg)
            .padding(horizontal = spacing.space8, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(visuals.dotColor),
        )
        Text(
            text = label,
            style = typography.captionStrong,
            color = visuals.textColor,
        )
    }
}

/** Figma's ride status pill (cards and the trip panel): 34dp, pad 6/10, fully rounded, Bold 14/22, no dot. */
@Composable
fun StatusPill(
    status: RideStatusVariant,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = LocalHitchyColors.current
    val visuals = resolveStatusColors(status, colors)
    Text(
        text = label ?: resolveStatusLabel(status),
        style = LocalHitchyTypography.current.bodyBold,
        color = visuals.textColor,
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(visuals.bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Preview
@Composable
fun StatusChipLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Row(
                modifier = Modifier.padding(spacing.space16),
                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                StatusChip(status = RideStatusVariant.Requested)
                StatusChip(status = RideStatusVariant.Confirmed)
                StatusChip(status = RideStatusVariant.InProgress)
                StatusChip(status = RideStatusVariant.Completed)
                StatusChip(status = RideStatusVariant.Cancelled)
            }
        }
    }
}

@Preview
@Composable
fun StatusChipRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        StatusChipLtrPreview()
    }
}
