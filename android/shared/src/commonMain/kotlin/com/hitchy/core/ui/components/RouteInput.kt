package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.location_swap
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RouteInput(
    pickupValue: String,
    dropOffValue: String,
    onPickupClick: () -> Unit,
    onDropOffClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSwap: (() -> Unit)? = null,
    onPickupClear: (() -> Unit)? = null,
    onDropOffClear: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val spacing = LocalHitchySpacing.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = colors.white,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.space16, vertical = spacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LocationField(
                    value = pickupValue,
                    onClick = onPickupClick,
                    type = LocationPointType.Pickup,
                    onClear = onPickupClear,
                    enabled = enabled,
                )

                HorizontalDivider(
                    color = colors.border,
                    modifier = Modifier.padding(start = 36.dp, end = spacing.space8),
                )

                LocationField(
                    value = dropOffValue,
                    onClick = onDropOffClick,
                    type = LocationPointType.DropOff,
                    onClear = onDropOffClear,
                    enabled = enabled,
                )
            }

            if (onSwap != null && enabled) {
                IconButton(
                    onClick = onSwap,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.lavender),
                ) {
                    Icon(
                        painter = painterResource(HitchyIcons.Swap),
                        contentDescription = stringResource(Res.string.location_swap),
                        tint = colors.purple,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RouteInputLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Box(modifier = Modifier.padding(spacing.space16)) {
                RouteInput(
                    pickupValue = "Nasr City, Abbas El Akkad",
                    dropOffValue = "Smart Village, Building B12",
                    onPickupClick = {},
                    onDropOffClick = {},
                    onSwap = {},
                    onPickupClear = {},
                    onDropOffClear = {},
                )
            }
        }
    }
}

@Preview
@Composable
fun RouteInputRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        RouteInputLtrPreview()
    }
}
