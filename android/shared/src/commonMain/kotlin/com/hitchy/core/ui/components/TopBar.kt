package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onOverflowClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.white,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = spacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBackClick != null) {
                BackButton(onClick = onBackClick)
            } else {
                Box(modifier = Modifier.size(48.dp))
            }

            Text(
                text = title,
                style = typography.h3,
                color = colors.navy,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.space8),
                maxLines = 1,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (actions != null) {
                    actions()
                } else if (onOverflowClick != null) {
                    IconButton(
                        onClick = onOverflowClick,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter = painterResource(HitchyIcons.More),
                            contentDescription = "More options",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun TopBarLtrPreview() {
    HitchyTheme {
        Surface(color = LocalHitchyColors.current.background) {
            TopBar(
                title = "Ride Details",
                onBackClick = {},
                onOverflowClick = {},
            )
        }
    }
}

@Preview
@Composable
fun TopBarRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchyTheme {
            Surface(color = LocalHitchyColors.current.background) {
                TopBar(
                    title = "تفاصيل الرحلة",
                    onBackClick = {},
                    onOverflowClick = {},
                )
            }
        }
    }
}
