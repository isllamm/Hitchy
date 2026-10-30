package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class BannerVariant {
    Success,
    Error,
    Warning,
    Info,
}

@Composable
fun HitchyBanner(
    text: String,
    modifier: Modifier = Modifier,
    variant: BannerVariant = BannerVariant.Info,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val bannerShape = RoundedCornerShape(12.dp)

    val (bg, contentColor, icon) = when (variant) {
        BannerVariant.Success -> Triple(colors.successBg, colors.successText, HitchyIcons.Success)
        BannerVariant.Error -> Triple(colors.errorBg, colors.errorText, HitchyIcons.Error)
        BannerVariant.Warning -> Triple(colors.warningBg, colors.warningText, HitchyIcons.Warning)
        BannerVariant.Info -> Triple(colors.infoBg, colors.infoText, HitchyIcons.Info)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(bannerShape)
            .background(bg)
            .padding(horizontal = spacing.space16, vertical = spacing.space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space12),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )

        Text(
            text = text,
            style = typography.body,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )

        if (onDismiss != null) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Close),
                    contentDescription = "Dismiss",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun HitchyBannerLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                HitchyBanner(
                    text = "Your booking request was sent to the owner.",
                    variant = BannerVariant.Success,
                    onDismiss = {},
                )
                HitchyBanner(
                    text = "You have an outstanding fee of EGP 20.",
                    variant = BannerVariant.Warning,
                )
                HitchyBanner(
                    text = "Ride cancelled by owner.",
                    variant = BannerVariant.Error,
                )
                HitchyBanner(
                    text = "Owners accept requests within 2 hours.",
                    variant = BannerVariant.Info,
                )
            }
        }
    }
}

@Preview
@Composable
fun HitchyBannerRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchyBannerLtrPreview()
    }
}
