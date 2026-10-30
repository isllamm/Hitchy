package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NotificationRow(
    title: String,
    body: String,
    timestampText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUnread: Boolean = false,
    icon: DrawableResource = HitchyIcons.Bell,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    val bgColor = if (isUnread) colors.lilac else colors.white

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space16, vertical = spacing.space12),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(spacing.space12),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = if (isUnread) typography.bodyStrong else typography.body,
                    color = colors.navy,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = timestampText,
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }

            Text(
                text = body,
                style = typography.caption,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        if (isUnread) {
            Box(
                modifier = Modifier
                    .padding(top = spacing.space4)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.purple),
            )
        }
    }
}

@Preview
@Composable
fun NotificationRowLtrPreview() {
    HitchyTheme {
        Surface(color = LocalHitchyColors.current.background) {
            Column {
                NotificationRow(
                    title = "Booking Accepted!",
                    body = "Ahmed accepted your request for tomorrow's ride.",
                    timestampText = "10m ago",
                    isUnread = true,
                    onClick = {},
                )
                NotificationRow(
                    title = "Ride completed",
                    body = "Don't forget to rate your host.",
                    timestampText = "Yesterday",
                    isUnread = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview
@Composable
fun NotificationRowRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        NotificationRowLtrPreview()
    }
}
