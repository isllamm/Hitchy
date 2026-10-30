package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RouteTimeline(
    pickupTime: String,
    pickupLocation: String,
    dropOffTime: String,
    dropOffLocation: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        // Pickup row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pickupTime,
                style = typography.captionStrong,
                color = colors.textSecondary,
                modifier = Modifier.width(60.dp),
            )
            Box(
                modifier = Modifier
                    .size(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.purple),
                )
            }
            Text(
                text = pickupLocation,
                style = typography.bodyStrong,
                color = colors.navy,
                modifier = Modifier.padding(start = spacing.space12),
                maxLines = 1,
            )
        }

        // Connecting line
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.width(60.dp))
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(colors.border),
                )
            }
        }

        // Drop-off row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dropOffTime,
                style = typography.captionStrong,
                color = colors.textSecondary,
                modifier = Modifier.width(60.dp),
            )
            Box(
                modifier = Modifier.size(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Pin),
                    contentDescription = null,
                    tint = colors.pin,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = dropOffLocation,
                style = typography.bodyStrong,
                color = colors.navy,
                modifier = Modifier.padding(start = spacing.space12),
                maxLines = 1,
            )
        }
    }
}

@Preview
@Composable
fun RouteTimelineLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Box(modifier = Modifier.padding(spacing.space16)) {
                RouteTimeline(
                    pickupTime = "07:30 AM",
                    pickupLocation = "Nasr City (Rabaa)",
                    dropOffTime = "08:15 AM",
                    dropOffLocation = "Smart Village (Gate 1)",
                )
            }
        }
    }
}

@Preview
@Composable
fun RouteTimelineRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        RouteTimelineLtrPreview()
    }
}
