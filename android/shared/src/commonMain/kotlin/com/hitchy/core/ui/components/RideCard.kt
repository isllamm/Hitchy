package com.hitchy.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RideCard(
    departureText: String,
    pickupTime: String,
    pickupLocation: String,
    dropOffTime: String,
    dropOffLocation: String,
    hostName: String,
    hostInitials: String,
    hostPhotoPath: String? = null,
    carModelColor: String,
    priceEgp: Int,
    availableSeats: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    status: RideStatusVariant? = null,
    totalSeats: Int = 3,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, colors.border, cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        color = colors.white,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space12),
        ) {
            // Header: Status chip + Departure text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DepartureRow(departureText = departureText)
                if (status != null) {
                    StatusChip(status = status)
                }
            }

            // Route timeline
            RouteTimeline(
                pickupTime = pickupTime,
                pickupLocation = pickupLocation,
                dropOffTime = dropOffTime,
                dropOffLocation = dropOffLocation,
            )

            HorizontalDivider(color = colors.border, thickness = 1.dp)

            // Footer: Host info + Price + Seats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    modifier = Modifier.weight(1f),
                ) {
                    Avatar(initials = hostInitials, size = AvatarSize.SM, photoPath = hostPhotoPath)
                    Column {
                        Text(
                            text = hostName,
                            style = typography.captionStrong,
                            color = colors.navy,
                            maxLines = 1,
                        )
                        Text(
                            text = carModelColor,
                            style = typography.caption,
                            color = colors.textSecondary,
                            maxLines = 1,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    PriceTag(amountEgp = priceEgp, size = PriceTagSize.Large)
                    SeatIndicator(availableSeats = availableSeats, totalSeats = totalSeats)
                }
            }
        }
    }
}

@Preview
@Composable
fun RideCardLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            BoxPadding(modifier = Modifier.padding(spacing.space16)) {
                RideCard(
                    departureText = "Tomorrow · 07:30 AM",
                    pickupTime = "07:30",
                    pickupLocation = "Nasr City (Abbas)",
                    dropOffTime = "08:15",
                    dropOffLocation = "Smart Village (B12)",
                    hostName = "Ahmed M.",
                    hostInitials = "AM",
                    carModelColor = "Hyundai Elantra · Grey",
                    priceEgp = 45,
                    availableSeats = 2,
                    status = RideStatusVariant.Confirmed,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun BoxPadding(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}

@Preview
@Composable
fun RideCardRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        RideCardLtrPreview()
    }
}
