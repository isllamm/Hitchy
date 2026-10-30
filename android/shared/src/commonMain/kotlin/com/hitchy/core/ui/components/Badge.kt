package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import hitchy.shared.generated.resources.badge_pending
import hitchy.shared.generated.resources.badge_verified
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class BadgeVariant {
    Verified,
    Pending,
}

@Composable
fun Badge(
    variant: BadgeVariant,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val pillShape = RoundedCornerShape(50)

    val (bg, textColor, icon) = when (variant) {
        BadgeVariant.Verified -> Triple(
            colors.successBg,
            colors.successText,
            HitchyIcons.IdCardVerified,
        )

        BadgeVariant.Pending -> Triple(
            colors.warningBg,
            colors.warningText,
            HitchyIcons.Clock,
        )
    }

    val text = label ?: when (variant) {
        BadgeVariant.Verified -> stringResource(Res.string.badge_verified)
        BadgeVariant.Pending -> stringResource(Res.string.badge_pending)
    }

    Row(
        modifier = modifier
            .clip(pillShape)
            .background(bg)
            .padding(horizontal = spacing.space8, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = text,
            style = typography.captionStrong,
            color = textColor,
        )
    }
}

@Preview
@Composable
fun BadgeLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Row(
                modifier = Modifier.padding(spacing.space16),
                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                Badge(variant = BadgeVariant.Verified)
                Badge(variant = BadgeVariant.Pending)
            }
        }
    }
}

@Preview
@Composable
fun BadgeRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BadgeLtrPreview()
    }
}
