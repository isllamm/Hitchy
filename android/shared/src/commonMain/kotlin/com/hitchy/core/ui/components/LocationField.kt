package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.location_clear
import hitchy.shared.generated.resources.location_dropoff_label
import hitchy.shared.generated.resources.location_pickup_label
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class LocationPointType {
    Pickup,
    DropOff,
}

@Composable
fun LocationField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: LocationPointType = LocationPointType.Pickup,
    label: String = if (type == LocationPointType.Pickup) {
        stringResource(Res.string.location_pickup_label)
    } else {
        stringResource(Res.string.location_dropoff_label)
    },
    placeholder: String = label,
    onClear: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Point indicator
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (type == LocationPointType.Pickup) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.purple),
                )
            } else {
                Icon(
                    painter = painterResource(HitchyIcons.Pin),
                    contentDescription = label,
                    tint = colors.pin,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.space12),
        ) {
            Text(
                text = label,
                style = typography.caption,
                color = colors.textSecondary,
            )
            Text(
                text = value.ifBlank { placeholder },
                style = typography.bodyStrong,
                color = if (value.isNotBlank()) colors.textPrimary else colors.textDisabled,
                maxLines = 1,
            )
        }

        if (value.isNotBlank() && onClear != null && enabled) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Close),
                    contentDescription = stringResource(Res.string.location_clear),
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun LocationFieldLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                LocationField(
                    value = "Tagamoa, 5th Settlement",
                    onClick = {},
                    type = LocationPointType.Pickup,
                    onClear = {},
                )
                LocationField(
                    value = "",
                    onClick = {},
                    type = LocationPointType.DropOff,
                )
            }
        }
    }
}

@Preview
@Composable
fun LocationFieldRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LocationFieldLtrPreview()
    }
}
