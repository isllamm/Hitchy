@file:Suppress("MagicNumber")

package com.hitchy.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.components.AvatarSize
import com.hitchy.core.ui.components.Badge
import com.hitchy.core.ui.components.BadgeVariant
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.PassengersStepper
import com.hitchy.core.ui.components.SeatIndicator
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.feature.booking.domain.RideHost
import com.hitchy.feature.booking.presentation.RiderRideDetailsState
import com.hitchy.feature.rides.domain.Ride
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.bk_arrival
import hitchy.shared.generated.resources.bk_available_seats
import hitchy.shared.generated.resources.bk_dropoff_location
import hitchy.shared.generated.resources.bk_egp_amount
import hitchy.shared.generated.resources.bk_pickup_location
import hitchy.shared.generated.resources.bk_pickup_location_walk
import hitchy.shared.generated.resources.bk_price_title
import hitchy.shared.generated.resources.bk_rating_line
import hitchy.shared.generated.resources.bk_total
import hitchy.shared.generated.resources.bk_vehicle_line_hidden
import hitchy.shared.generated.resources.bk_verified_owner
import hitchy.shared.generated.resources.bk_your_owner
import hitchy.shared.generated.resources.ride_details_late_fee_rule
import hitchy.shared.generated.resources.ride_details_price_per_seat
import hitchy.shared.generated.resources.ride_details_wallet_note
import hitchy.shared.generated.resources.rider_home_passengers_label
import hitchy.shared.generated.resources.rider_home_passengers_many
import hitchy.shared.generated.resources.rider_home_passengers_one
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val DetailsMapHeight = 170.dp

/** Everything above the bottom bar of Ride details: map, owner, route, times, seats and price. */
@Composable
internal fun RideDetailsSections(
    state: RiderRideDetailsState,
    language: AppLanguage,
    onSeatsChange: (Int) -> Unit,
) {
    val card = state.rideCard
    val ride = state.ride
    val booking = state.booking
    val pickupName = booking?.pickupPointName?.resolve(language)
        ?: card?.pickup?.name?.resolve(language).orEmpty()
    val pickupTime = booking?.let { cairoTime12(it.pickupAt) } ?: cairoTime12(card?.pickup?.at)
    val dropName = ride?.toPointName?.resolve(language) ?: card?.toPointName?.resolve(language).orEmpty()
    val dropTime = cairoTime12(card?.arriveAt)
    val dateKey = ride?.dateKey ?: isoToCairoDateKey(card?.departAt)
    val walkM = if (booking == null) card?.pickup?.walkM else null

    if (ride != null) {
        DetailsMap(points = detailsMapPoints(ride, card?.pickup?.id, booking?.pickupPoint))
    }
    if (card != null) {
        DetailsHostCard(host = card.host, ride = ride, plateHidden = !state.isAccepted)
    }
    BookingTripCard(
        fromName = pickupName,
        toName = dropName,
        metaText = bookingMetaText(dateKey, pickupTime, state.bookedSeats),
        fromLabel = if (walkM != null) {
            stringResource(Res.string.bk_pickup_location_walk, walkM)
        } else {
            stringResource(Res.string.bk_pickup_location)
        },
        toLabel = stringResource(Res.string.bk_dropoff_location),
    )
    DetailsTimeBanner(pickupTime = pickupTime, dropTime = dropTime)
    if (state.canRequest) {
        DetailsSeatsCard(
            seatsLeft = ride?.seatsLeft ?: card?.seatsLeft ?: 0,
            seats = state.seats,
            maxSeats = state.maxSeats,
            enabled = !state.isRequesting,
            onSeatsChange = onSeatsChange,
        )
    }
    DetailsPriceCard(pricePerSeat = state.pricePerSeat, total = state.totalEgp)
    Text(
        text = stringResource(Res.string.ride_details_late_fee_rule),
        style = HitchyTheme.typography.caption,
        color = HitchyTheme.colors.textSecondary,
    )
}

/**
 * The map points: the rider's pickup point, then the ride's drop-off point (the last stop that matches the ride's
 * drop-off point, or the last stop). With one point only the map shows a pin and no line.
 */
private fun detailsMapPoints(
    ride: Ride,
    pickupId: String?,
    bookingPickup: GeoPoint?,
): List<GeoPoint> {
    val pickup = bookingPickup
        ?: ride.stops.firstOrNull { it.pointId == pickupId }?.point
        ?: ride.fromPoint
    val drop = (ride.stops.lastOrNull { it.pointId == ride.toPointId } ?: ride.stops.lastOrNull())?.point
    return listOfNotNull(pickup, drop)
}

@Composable
private fun DetailsMap(points: List<GeoPoint>) {
    HitchyMap(
        pins = points.mapIndexed { index, point ->
            MapPin(id = if (index == 0) "from" else "to", point = point, selected = index > 0)
        },
        route = if (points.size > 1) points else emptyList(),
        modifier = Modifier
            .fillMaxWidth()
            .height(DetailsMapHeight)
            .clip(RoundedCornerShape(BookingCardRadius)),
    )
}

@Composable
private fun DetailsHostCard(
    host: RideHost,
    ride: Ride?,
    plateHidden: Boolean,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    BookingCardSurface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.bk_your_owner).uppercase(),
                style = typography.captionStrong,
                color = colors.navy,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Avatar(initials = host.firstName.take(2), size = AvatarSize.LG)
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = host.firstName, style = typography.bodyStrong, color = colors.navy, maxLines = 1)
                    DetailsRating(host)
                }
                if (host.verified) {
                    Badge(variant = BadgeVariant.Verified, label = stringResource(Res.string.bk_verified_owner))
                }
            }
            if (ride != null && plateHidden) {
                Text(
                    text = stringResource(
                        Res.string.bk_vehicle_line_hidden,
                        ride.vehicle.color,
                        ride.vehicle.make,
                        ride.vehicle.model,
                    ),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun DetailsRating(host: RideHost) {
    val colors = HitchyTheme.colors
    val rating = host.rating ?: return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            painter = painterResource(HitchyIcons.Star),
            contentDescription = null,
            tint = colors.warning,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(Res.string.bk_rating_line, rating.toString().take(3), host.trips),
            style = HitchyTheme.typography.caption,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun DetailsTimeBanner(
    pickupTime: String,
    dropTime: String,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BookingCardRadius))
            .background(colors.lilac)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = pickupTime, style = typography.h3, color = colors.navy)
        Icon(
            painter = painterResource(HitchyIcons.ArrowForward),
            contentDescription = null,
            tint = colors.purple,
            modifier = Modifier.size(12.dp),
        )
        Text(text = dropTime, style = typography.h3, color = colors.navy)
        Text(text = stringResource(Res.string.bk_arrival), style = typography.caption, color = colors.textSecondary)
    }
}

@Composable
private fun DetailsSeatsCard(
    seatsLeft: Int,
    seats: Int,
    maxSeats: Int,
    enabled: Boolean,
    onSeatsChange: (Int) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    BookingCardSurface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.bk_available_seats),
                    style = typography.bodyStrong,
                    color = colors.navy,
                )
                SeatIndicator(availableSeats = seatsLeft)
            }
            DetailsPassengersRow(seats = seats, maxSeats = maxSeats, enabled = enabled, onSeatsChange = onSeatsChange)
        }
    }
}

@Composable
private fun DetailsPassengersRow(
    seats: Int,
    maxSeats: Int,
    enabled: Boolean,
    onSeatsChange: (Int) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    BookingCardSurface {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Person),
                    contentDescription = null,
                    tint = colors.purple,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.rider_home_passengers_label),
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
            PassengersStepper(count = seats, maxCount = maxSeats, onCountChange = onSeatsChange, enabled = enabled)
        }
    }
}

@Composable
private fun DetailsPriceCard(
    pricePerSeat: Int,
    total: Int,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    BookingCardSurface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(Res.string.bk_price_title), style = typography.bodyLarge, color = colors.navy)
            DetailsPriceLine(
                label = stringResource(Res.string.ride_details_price_per_seat),
                amount = pricePerSeat,
            )
            HorizontalDivider(color = colors.border, thickness = 1.dp)
            DetailsPriceLine(label = stringResource(Res.string.bk_total), amount = total)
            HitchyBanner(text = stringResource(Res.string.ride_details_wallet_note), variant = BannerVariant.Info)
        }
    }
}

@Composable
private fun DetailsPriceLine(
    label: String,
    amount: Int,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = typography.bodyLarge, color = colors.navy)
        Text(text = stringResource(Res.string.bk_egp_amount, amount), style = typography.title, color = colors.navy)
    }
}
