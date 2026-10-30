@file:Suppress("MagicNumber")

package com.hitchy.feature.trip.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
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
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.rides.domain.RideVehicle
import hitchy.shared.generated.resources.Res
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
            Box(modifier = Modifier.offset(x = 4.dp, y = 5.dp).size(width = 2.dp, height = 58.dp).background(colors.lavender))
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
 * Figma's `RideDetails` card: pad 14/16, the vehicle row (28dp car, a 10/18 label, a Bold name and the
 * colour and plate), then a top-ruled row with the seats on the left and the amount on the right.
 */
@Composable
internal fun TripVehicleSeatCard(
    vehicle: RideVehicle,
    seats: Int,
    total: Int,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(
        modifier = Modifier.tripCard(colors.border).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                painter = painterResource(HitchyIcons.Car28),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(28.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(Res.string.trip_design_owner_car_label),
                    style = typography.caption.copy(fontSize = 10.sp),
                    color = colors.textSecondary,
                )
                Text(
                    text = stringResource(Res.string.trip_design_vehicle_line, vehicle.make, vehicle.model),
                    style = typography.bodyBold,
                    color = colors.navy,
                )
                Text(
                    text = stringResource(Res.string.trip_design_vehicle_detail, vehicle.color, vehicle.plate),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = stringResource(Res.string.trip_design_your_seat), style = typography.caption, color = colors.textSecondary)
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
                Text(text = stringResource(Res.string.trip_design_total), style = typography.caption, color = colors.textSecondary)
                Text(
                    text = stringResource(Res.string.format_egp, total.toString()),
                    style = typography.captionStrong,
                    color = colors.navy,
                )
            }
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
