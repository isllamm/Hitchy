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

private val CardRadius = 20.dp
private val IconTileSize = 40.dp

private fun Modifier.tripCard(borderColor: Color): Modifier {
    val shape = RoundedCornerShape(CardRadius)
    return this.fillMaxWidth().border(1.dp, borderColor, shape)
}

@Composable
internal fun TripRouteCard(
    pickupName: String,
    dropOffName: String,
) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier
            .tripCard(colors.border)
            .padding(16.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().width(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.deepPurple))
            val lineColor = colors.border
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
                    .width(1.dp)
                    .drawBehind {
                        drawLine(
                            color = lineColor,
                            start = Offset(size.width / 2f, 0f),
                            end = Offset(size.width / 2f, size.height),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                        )
                    },
            )
            Icon(
                painter = painterResource(HitchyIcons.Pin),
                contentDescription = null,
                tint = colors.pin,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
        Text(text = name, style = HitchyTheme.typography.bodyStrong, color = HitchyTheme.colors.navy)
        Text(text = label, style = HitchyTheme.typography.caption, color = HitchyTheme.colors.textSecondary)
    }
}

@Composable
internal fun TripCarCard(vehicle: RideVehicle) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.tripCard(colors.border).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(IconTileSize).clip(RoundedCornerShape(12.dp)).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.Car),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.trip_design_owner_car_label),
                style = typography.caption,
                color = colors.textSecondary,
            )
            Text(
                text = stringResource(Res.string.trip_design_vehicle_line, vehicle.make, vehicle.model),
                style = typography.bodyStrong,
                color = colors.navy,
            )
            Text(
                text = stringResource(Res.string.trip_design_vehicle_detail, vehicle.color, vehicle.plate),
                style = typography.caption,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
internal fun TripSeatCard(
    seats: Int,
    total: Int,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.tripCard(colors.border).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
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
                style = typography.bodyStrong,
                color = colors.navy,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(Res.string.trip_design_total),
                style = typography.caption,
                color = colors.textSecondary,
            )
            Text(
                text = stringResource(Res.string.format_egp, total.toString()),
                style = typography.bodyStrong,
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
