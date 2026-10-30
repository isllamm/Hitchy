package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.empty_state_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun EmptyState(
    title: String = stringResource(Res.string.empty_state_title),
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: DrawableResource = HitchyIcons.Search,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(36.dp),
            )
        }

        Text(
            text = title,
            style = typography.h3,
            color = colors.navy,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = spacing.space16),
        )

        if (!message.isNullOrBlank()) {
            Text(
                text = message,
                style = typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = spacing.space8),
            )
        }

        if (!actionLabel.isNullOrBlank() && onActionClick != null) {
            HitchyButton(
                onClick = onActionClick,
                variant = HitchyButtonVariant.Primary,
                modifier = Modifier.padding(top = spacing.space16),
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Preview
@Composable
fun EmptyStateLtrPreview() {
    HitchyTheme {
        Surface(color = LocalHitchyColors.current.background) {
            EmptyState(
                title = "No rides found",
                message = "Try searching for a different pickup point or date.",
                actionLabel = "Clear filters",
                onActionClick = {},
            )
        }
    }
}

@Preview
@Composable
fun EmptyStateRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchyTheme {
            Surface(color = LocalHitchyColors.current.background) {
                EmptyState(
                    title = "لم يتم العثور على رحلات",
                    message = "جرب البحث بنقطة تحرك مختلفة أو موعد آخر.",
                    actionLabel = "مسح الفلاتر",
                    onActionClick = {},
                )
            }
        }
    }
}
