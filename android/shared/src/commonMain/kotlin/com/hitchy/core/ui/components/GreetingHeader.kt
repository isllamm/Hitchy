package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import hitchy.shared.generated.resources.greeting_morning
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GreetingHeader(
    firstName: String,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    greetingText: String = stringResource(Res.string.greeting_morning),
    hasUnreadNotifications: Boolean = false,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space16, vertical = spacing.space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = greetingText,
                style = typography.caption,
                color = colors.textSecondary,
            )
            Text(
                text = firstName,
                style = typography.h2,
                color = colors.navy,
            )
        }

        Box(contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.white),
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Bell),
                    contentDescription = "Notifications",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }

            if (hasUnreadNotifications) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(x = 8.dp, y = (-8).dp)
                        .clip(CircleShape)
                        .background(colors.pin),
                )
            }
        }
    }
}

@Preview
@Composable
fun GreetingHeaderLtrPreview() {
    HitchyTheme {
        Surface(color = LocalHitchyColors.current.background) {
            GreetingHeader(
                firstName = "Mahmoud",
                onNotificationsClick = {},
                hasUnreadNotifications = true,
            )
        }
    }
}

@Preview
@Composable
fun GreetingHeaderRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchyTheme {
            Surface(color = LocalHitchyColors.current.background) {
                GreetingHeader(
                    firstName = "محمود",
                    greetingText = "صباح الخير",
                    onNotificationsClick = {},
                    hasUnreadNotifications = true,
                )
            }
        }
    }
}
