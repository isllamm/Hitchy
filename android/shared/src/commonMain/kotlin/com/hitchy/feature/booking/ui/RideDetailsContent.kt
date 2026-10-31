@file:Suppress("MagicNumber")

package com.hitchy.feature.booking.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.components.AvatarImage
import com.hitchy.core.ui.components.AvatarSize
import com.hitchy.core.ui.components.Badge
import com.hitchy.core.ui.components.BadgeVariant
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.PassengersStepper
import com.hitchy.core.ui.components.SeatIndicator
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tightLine
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
import hitchy.shared.generated.resources.bk_verified_short
import hitchy.shared.generated.resources.bk_your_owner
import hitchy.shared.generated.resources.fig_result_verified
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

/**
 * The demo's host card (676:10678): r16, a 1.1dp lilac stroke and two 4% shadows, padding 15. "YOUR HOST" in
 * Bold 11/16 (0.5 spacing), then a 48dp lavender avatar with a white ring and a #BCCDFF halo (800 13/20
 * initials), the name (Bold 14/21), the rating, the car line (10/15), and the green verified pill on the end side.
 */
@Composable
private fun DetailsHostCard(
    host: RideHost,
    ride: Ride?,
    plateHidden: Boolean,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .figmaShadow(shape, colors.shadowInk.copy(alpha = HOST_SHADOW), blur = 16.dp, y = 4.dp)
            .figmaShadow(shape, colors.shadowInk.copy(alpha = HOST_SHADOW), blur = 2.dp, y = 1.dp)
            .clip(shape)
            .background(colors.white)
            .border(1.1.dp, colors.lilac, shape)
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Text(
            text = stringResource(Res.string.bk_your_owner).uppercase(),
            style = typography.captionStrong.copy(fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
            color = colors.navy,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.periwinkle)
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(colors.white)
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(colors.lavender),
                contentAlignment = Alignment.Center,
            ) {
                AvatarImage(
                    photoPath = host.photoPath,
                    initials = host.firstName.take(2).uppercase(),
                    textStyle = typography.caption.copy(
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    textColor = colors.purple,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = host.firstName,
                    style = typography.bodyBold.copy(lineHeight = 21.sp),
                    modifier = Modifier.tightLine(typography.bodyBold.fontSize),
                    color = colors.navy,
                    maxLines = 1,
                )
                DetailsRating(host)
                if (ride != null && plateHidden) {
                    Text(
                        text = stringResource(
                            Res.string.bk_vehicle_line_hidden,
                            ride.vehicle.color,
                            ride.vehicle.make,
                            ride.vehicle.model,
                        ),
                        style = typography.caption.copy(fontSize = 10.sp, lineHeight = 15.sp),
                        color = colors.navy,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            if (host.verified) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.successBg)
                        .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.fig_result_verified),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(Res.string.bk_verified_owner),
                        style = typography.captionStrong,
                        color = colors.successText,
                    )
                }
            }
        }
    }
}

/** The host card's shadow strength (#171B22 at 4%). */
private const val HOST_SHADOW = 0.04f

@Composable
private fun DetailsRating(host: RideHost) {
    val colors = HitchyTheme.colors
    val rating = host.rating ?: return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "★", style = HitchyTheme.typography.captionStrong, color = colors.warning)
        Text(
            text = stringResource(Res.string.bk_rating_line, rating.toString().take(3), host.trips),
            style = HitchyTheme.typography.caption,
            color = colors.textSecondary,
        )
    }
}

/** Figma's trip box: lilac, r=12, pad 12, 18/26 Bold times around a purple arrow. */
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
            .clip(RoundedCornerShape(12.dp))
            .background(colors.lilac)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = pickupTime, style = typography.title, color = colors.navy)
        Text(text = "→", style = typography.body, color = colors.purple)
        Text(text = dropTime, style = typography.title, color = colors.navy)
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.bk_available_seats),
                    style = typography.bodyBold,
                    color = colors.navy,
                )
                SeatIndicator(availableSeats = seatsLeft)
            }
            DetailsPassengersRow(seats = seats, maxSeats = maxSeats, enabled = enabled, onSeatsChange = onSeatsChange)
        }
    }
}

/** Home's Passengers row (Figma 390:1307): a 32dp tile, 14/22 texts, the 38dp white and purple stepper. */
@Composable
private fun DetailsPassengersRow(
    seats: Int,
    maxSeats: Int,
    enabled: Boolean,
    onSeatsChange: (Int) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.hairline, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.PassengerSmall),
                contentDescription = null,
                tint = colors.purpleDeep,
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = stringResource(Res.string.rider_home_passengers_label),
                style = typography.body,
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            StepCircle(HitchyIcons.Minus, filled = false, enabled = enabled && seats > 1) { onSeatsChange(seats - 1) }
            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                Text(text = seats.toString(), style = typography.title, color = colors.navy)
            }
            StepCircle(
                HitchyIcons.Plus,
                filled = true,
                enabled = enabled && seats < maxSeats,
            ) { onSeatsChange(seats + 1) }
        }
    }
}

@Composable
private fun StepCircle(
    icon: org.jetbrains.compose.resources.DrawableResource,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            // Figma's Stepper (902:13090): the minus is white with a stroke, flat #F3F4F6 when it can't go lower.
            .background(
                when {
                    filled -> colors.purple
                    enabled -> colors.white
                    else -> colors.disabledSurface
                },
            )
            .then(if (filled || !enabled) Modifier else Modifier.border(1.dp, colors.border, CircleShape))
            .alpha(if (enabled || !filled) 1f else DISABLED_ALPHA)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = when {
                filled -> colors.white
                enabled -> colors.navy
                else -> colors.textDisabled
            },
            modifier = Modifier.size(18.dp),
        )
    }
}

private const val DISABLED_ALPHA = 0.4f

/** Figma's price breakdown: 12/18 rows, a Bold "Total" with the 18/26 amount, and the blue info note. */
@Composable
private fun DetailsPriceCard(
    pricePerSeat: Int,
    total: Int,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    BookingCardSurface {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(Res.string.ride_details_price_per_seat),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
                Text(
                    text = stringResource(Res.string.bk_egp_amount, pricePerSeat),
                    style = typography.captionStrong,
                    color = colors.navy,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.bk_total),
                    style = typography.bodyBold,
                    color = colors.textSecondary,
                )
                Text(
                    text = stringResource(Res.string.bk_egp_amount, total),
                    style = typography.title,
                    color = colors.navy,
                )
            }
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.infoBg)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Info16),
                    contentDescription = null,
                    tint = colors.deepPurple,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(Res.string.ride_details_wallet_note),
                    style = typography.caption,
                    color = colors.infoText,
                )
            }
        }
    }
}
