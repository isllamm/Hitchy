package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import hitchy.shared.generated.resources.badge_same_company
import hitchy.shared.generated.resources.badge_tier_bronze
import hitchy.shared.generated.resources.badge_tier_gold
import hitchy.shared.generated.resources.badge_tier_silver
import hitchy.shared.generated.resources.badge_women_only
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class CommunityBadgeType {
    WomenOnly,
    SameCompany,
    BronzeTier,
    SilverTier,
    GoldTier,
}

@Composable
fun CommunityBadge(
    type: CommunityBadgeType,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val pillShape = RoundedCornerShape(50)

    val label = when (type) {
        CommunityBadgeType.WomenOnly -> stringResource(Res.string.badge_women_only)
        CommunityBadgeType.SameCompany -> stringResource(Res.string.badge_same_company)
        CommunityBadgeType.BronzeTier -> stringResource(Res.string.badge_tier_bronze)
        CommunityBadgeType.SilverTier -> stringResource(Res.string.badge_tier_silver)
        CommunityBadgeType.GoldTier -> stringResource(Res.string.badge_tier_gold)
    }

    val icon: DrawableResource = when (type) {
        CommunityBadgeType.WomenOnly -> HitchyIcons.Person

        CommunityBadgeType.SameCompany -> HitchyIcons.ShieldCheck

        CommunityBadgeType.BronzeTier,
        CommunityBadgeType.SilverTier,
        CommunityBadgeType.GoldTier,
        -> HitchyIcons.Star
    }

    val (bg, textColor) = when (type) {
        CommunityBadgeType.WomenOnly -> Pair(colors.lavender, colors.purple)
        CommunityBadgeType.SameCompany -> Pair(colors.infoBg, colors.infoText)
        CommunityBadgeType.BronzeTier -> Pair(colors.warningBg, colors.warningText)
        CommunityBadgeType.SilverTier -> Pair(colors.disabledSurface, colors.textPrimary)
        CommunityBadgeType.GoldTier -> Pair(colors.warningBg, colors.warningText)
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
            text = label,
            style = typography.captionStrong,
            color = textColor,
        )
    }
}

@Preview
@Composable
fun CommunityBadgeLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Row(
                modifier = Modifier.padding(spacing.space16),
                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                CommunityBadge(type = CommunityBadgeType.WomenOnly)
                CommunityBadge(type = CommunityBadgeType.SameCompany)
                CommunityBadge(type = CommunityBadgeType.GoldTier)
            }
        }
    }
}

@Preview
@Composable
fun CommunityBadgeRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        CommunityBadgeLtrPreview()
    }
}
