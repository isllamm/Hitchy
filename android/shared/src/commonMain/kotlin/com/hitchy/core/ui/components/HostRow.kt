package com.hitchy.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HostRow(
    name: String,
    initials: String,
    rating: Double,
    modifier: Modifier = Modifier,
    isVerified: Boolean = true,
    photoPath: String? = null,
    rideCount: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    val rowModifier = modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            },
        )
        .padding(vertical = spacing.space4)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space12),
    ) {
        Avatar(
            initials = initials,
            size = AvatarSize.MD,
            photoPath = photoPath,
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                Text(
                    text = name,
                    style = typography.bodyStrong,
                    color = colors.navy,
                )
                if (isVerified) {
                    Badge(variant = BadgeVariant.Verified)
                }
            }

            RatingDisplay(
                rating = rating,
                rideCount = rideCount,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Preview
@Composable
fun HostRowLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                HostRow(
                    name = "Tarek Nour",
                    initials = "TN",
                    rating = 4.9,
                    rideCount = 42,
                    isVerified = true,
                    onClick = {},
                )
            }
        }
    }
}

@Preview
@Composable
fun HostRowRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HostRowLtrPreview()
    }
}
