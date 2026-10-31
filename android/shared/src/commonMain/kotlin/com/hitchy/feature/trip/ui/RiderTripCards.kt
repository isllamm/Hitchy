@file:Suppress("MagicNumber")

package com.hitchy.feature.trip.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hitchy.core.ui.components.AvatarImage
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tightLine
import com.hitchy.feature.profile.domain.PublicProfile
import com.hitchy.feature.rides.domain.RideVehicle
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.bk_rating_line
import hitchy.shared.generated.resources.fig_trip_star
import hitchy.shared.generated.resources.fig_trip_verified
import hitchy.shared.generated.resources.format_egp
import hitchy.shared.generated.resources.rider_home_passengers_many
import hitchy.shared.generated.resources.rider_home_passengers_one
import hitchy.shared.generated.resources.rider_trip_payment_amount
import hitchy.shared.generated.resources.rider_trip_payment_done_title
import hitchy.shared.generated.resources.rider_trip_wallet_remaining
import hitchy.shared.generated.resources.trip_design_dropoff_label
import hitchy.shared.generated.resources.trip_design_owner_car_label
import hitchy.shared.generated.resources.trip_design_pickup_label
import hitchy.shared.generated.resources.trip_design_total
import hitchy.shared.generated.resources.trip_design_vehicle_detail
import hitchy.shared.generated.resources.trip_design_vehicle_line
import hitchy.shared.generated.resources.trip_design_your_seat
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Figma frame 390:1450 "Upcoming ride details": r=16 cards with a 1dp #E5E7EB stroke.
private val CardRadius = 16.dp

private fun Modifier.tripCard(borderColor: Color): Modifier {
    val shape = RoundedCornerShape(CardRadius)
    return this.fillMaxWidth().border(1.dp, borderColor, shape)
}

/** Figma's `Route` card: pad 16, the 10x74 marker column 27dp left of two 16/24 names. */
@Composable
internal fun TripRouteCard(
    pickupName: String,
    dropOffName: String,
) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier.tripCard(colors.border).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(27.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(width = 10.5.dp, height = 74.dp)) {
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = 5.dp)
                    .size(width = 2.dp, height = 58.dp)
                    .background(colors.lavender),
            )
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.deepPurple))
            Icon(
                painter = painterResource(HitchyIcons.PinSmall),
                contentDescription = null,
                tint = colors.pin,
                modifier = Modifier.offset(y = 60.dp).size(width = 10.5.dp, height = 14.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlaceText(name = pickupName, label = stringResource(Res.string.trip_design_pickup_label))
            PlaceText(name = dropOffName, label = stringResource(Res.string.trip_design_dropoff_label))
        }
    }
}

@Composable
private fun PlaceText(
    name: String,
    label: String,
) {
    Column {
        Text(text = name, style = HitchyTheme.typography.bodyLargeStrong, color = HitchyTheme.colors.navy, maxLines = 1)
        Text(text = label, style = HitchyTheme.typography.body, color = HitchyTheme.colors.textSecondary)
    }
}

/**
 * Figma's `Driver Information` card (demo 676:9863): r16, a #E5E7EB stroke, padding 16/8; a 48dp avatar, the
 * owner's name (600 14/22) with the green verified mark, the star and "4.9 · 128 rides", and the car with its
 * plate. Figma's message and call buttons are not in the demo.
 */
@Composable
internal fun TripHostCard(host: PublicProfile?, vehicle: RideVehicle) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.tripCard(colors.border).padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            AvatarImage(
                photoPath = host?.photoPath,
                initials = host?.firstName?.take(2)?.uppercase().orEmpty(),
                textStyle = typography.bodyBold,
                textColor = colors.purple,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = host?.firstName.orEmpty(),
                    style = typography.bodyStrong,
                    modifier = Modifier.tightLine(typography.bodyStrong.fontSize),
                    color = colors.navy,
                    maxLines = 1,
                )
                if (host?.verified == true) {
                    Image(
                        painter = painterResource(Res.drawable.fig_trip_verified),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            val rating = host?.hostRatingAvg
            if (host != null && rating != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.fig_trip_star),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(Res.string.bk_rating_line, rating.toString().take(3), host.hostTrips),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
            }
            Text(
                text = stringResource(
                    Res.string.trip_design_vehicle_detail,
                    stringResource(Res.string.trip_design_vehicle_line, vehicle.make, vehicle.model),
                    stringResource(Res.string.trip_design_vehicle_detail, vehicle.color, vehicle.plate),
                ),
                style = typography.caption,
                color = colors.textSecondary,
            )
        }
    }
}

/**
 * Figma's `RideDetails` card (demo 676:9884): padding 14/16, "Your seat" over the passengers (Bold 12/18) on
 * the start side and the amount on the end side.
 */
@Composable
internal fun TripSeatCard(seats: Int, total: Int) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.tripCard(colors.border).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(Res.string.trip_design_your_seat),
                style = typography.caption,
                color = colors.textSecondary,
            )
            Text(
                text = if (seats == 1) {
                    stringResource(Res.string.rider_home_passengers_one)
                } else {
                    stringResource(Res.string.rider_home_passengers_many, seats)
                },
                style = typography.captionStrong,
                color = colors.navy,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(Res.string.trip_design_total),
                style = typography.caption,
                color = colors.textSecondary,
            )
            Text(
                text = stringResource(Res.string.format_egp, total.toString()),
                style = typography.captionStrong,
                color = colors.navy,
            )
        }
    }
}

@Composable
internal fun TripPaymentCard(
    total: Int,
    walletBalance: Int,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(
        modifier = Modifier
            .tripCard(colors.border)
            .clip(RoundedCornerShape(CardRadius))
            .background(colors.lilac)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.rider_trip_payment_done_title),
            style = typography.h2,
            color = colors.navy,
        )
        Text(
            text = stringResource(Res.string.rider_trip_payment_amount, total),
            style = typography.bodyStrong,
            color = colors.purple,
        )
        Text(
            text = stringResource(Res.string.rider_trip_wallet_remaining, walletBalance),
            style = typography.caption,
            color = colors.textSecondary,
        )
    }
}
