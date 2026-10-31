@file:Suppress("MagicNumber", "TooManyFunctions")

package com.hitchy.feature.booking.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.RideStatus
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.StatusPill
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.dateKey
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.feature.booking.presentation.RidesTabViewModel
import com.hitchy.feature.booking.presentation.UnifiedRideItem
import com.hitchy.feature.booking.presentation.opensOwnerTrip
import com.hitchy.feature.booking.presentation.opensRiderTrip
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fig_rides_search
import hitchy.shared.generated.resources.format_egp
import hitchy.shared.generated.resources.img_empty_rides
import hitchy.shared.generated.resources.location_dropoff_label
import hitchy.shared.generated.resources.location_pickup_label
import hitchy.shared.generated.resources.owner_design_route_start
import hitchy.shared.generated.resources.price_per_seat_large
import hitchy.shared.generated.resources.rider_home_date_format
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import hitchy.shared.generated.resources.rider_home_months
import hitchy.shared.generated.resources.rider_home_weekdays
import hitchy.shared.generated.resources.rides_design_vehicle
import hitchy.shared.generated.resources.rides_design_when
import hitchy.shared.generated.resources.rides_filter_cancelled
import hitchy.shared.generated.resources.rides_filter_completed
import hitchy.shared.generated.resources.rides_filter_upcoming
import hitchy.shared.generated.resources.rides_t14_seats_booked
import hitchy.shared.generated.resources.rides_tab_empty_body
import hitchy.shared.generated.resources.rides_tab_empty_title
import hitchy.shared.generated.resources.rides_tab_role_owner_badge
import hitchy.shared.generated.resources.rides_tab_role_rider_badge
import hitchy.shared.generated.resources.rides_tab_role_rider_one
import hitchy.shared.generated.resources.rides_tab_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * The single Rides list built to the demo design: big title, then one card per ride with a status pill, the
 * date and time, the route, and a footer with the price. Replaces the older RidesTabContent on the Rides tab.
 */
@Composable
fun RiderRidesTabContent(
    onBack: (() -> Unit)?,
    onOpenRiderRideDetails: (String) -> Unit,
    onOpenRiderTrip: (String) -> Unit,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    clock: Clock = koinInject(),
    viewModel: RidesTabViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val language: AppLanguage = languageSwitcher.getCurrentLanguage()
    val now = clock.now()
    val todayKey = dateKey(now)
    val tomorrowKey = dateKey(now + 1.days)

    // The demo's My Rides (676:9758) has no tabs: upcoming rides first, then completed, then cancelled.
    val shown = state.items.sortedBy { it.filter().ordinal }

    Surface(modifier = modifier.fillMaxSize(), color = colors.white) {
        Column(modifier = Modifier.fillMaxSize()) {
            RidesHeader(onBack = onBack)

            if (shown.isEmpty() && !state.isLoading) {
                RidesEmpty()
                return@Column
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    items = shown,
                    key = { item ->
                        when (item) {
                            is UnifiedRideItem.AsRider -> "rider_${item.rideId}"
                            is UnifiedRideItem.AsOwner -> "owner_${item.rideId}"
                        }
                    },
                ) { item ->
                    val data = item.toCardData(
                        language = language,
                        onOpenRiderRideDetails = onOpenRiderRideDetails,
                        onOpenRiderTrip = onOpenRiderTrip,
                        onOpenHostRideDetails = onOpenHostRideDetails,
                        onOpenHostTrip = onOpenHostTrip,
                    )
                    RideDesignCard(data = data, todayKey = todayKey, tomorrowKey = tomorrowKey)
                }
            }
        }
    }
}

/** The empty list, laid out like the Shop's Coming Soon: the 300x200 car picture, the Bold 28/36 title and line. */
@Composable
private fun RidesEmpty() {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        // 30.5dp more than the Shop's 145dp: this header is shorter, so the picture lands where the Shop's does.
        Column(modifier = Modifier.padding(top = 175.5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.img_empty_rides),
                contentDescription = null,
                modifier = Modifier.size(width = 300.dp, height = 200.dp),
            )
            Text(text = stringResource(Res.string.rides_tab_empty_title), style = typography.h1, color = colors.navy)
            Text(
                text = stringResource(Res.string.rides_tab_empty_body),
                style = typography.bodyLarge,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(290.dp).padding(top = 8.78.dp),
            )
        }
    }
}

@Composable
private fun RidesHeader(onBack: (() -> Unit)?) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    // Figma frame 390:1366: a 48dp title row 49dp from the top.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = figmaTop(TITLE_TOP), start = 16.dp, end = 16.dp, bottom = 16.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onBack != null) {
            BackButton(onClick = onBack, style = BackButtonStyle.OutlinedCircle, modifier = Modifier.size(44.dp))
        }
        Text(
            text = stringResource(Res.string.rides_tab_title),
            style = typography.h1,
            color = colors.navy,
            modifier = Modifier.weight(1f),
        )
        // Figma's 48dp white search circle; there is no ride search yet, so it does nothing.
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.white),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.fig_rides_search),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun UnifiedRideItem.toCardData(
    language: AppLanguage,
    onOpenRiderRideDetails: (String) -> Unit,
    onOpenRiderTrip: (String) -> Unit,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
): RideCardData = when (this) {
    is UnifiedRideItem.AsRider -> RideCardData(
        status = booking.status.toVariant(),
        dateKey = isoToCairoDateKey(booking.departAt),
        time = cairoTime12(booking.pickupAt),
        pickupName = booking.pickupPointName.resolve(language),
        dropOffName = null,
        title = if (booking.seats == 1) {
            stringResource(Res.string.rides_tab_role_rider_one)
        } else {
            stringResource(Res.string.rides_tab_role_rider_badge, booking.seats)
        },
        subtitle = null,
        tileIcon = HitchyIcons.Person,
        priceEgp = booking.total,
        onClick = {
            if (booking.status.opensRiderTrip()) {
                onOpenRiderTrip(booking.rideId)
            } else {
                onOpenRiderRideDetails(booking.rideId)
            }
        },
    )

    is UnifiedRideItem.AsOwner -> ownerCardData(this, language, onOpenHostRideDetails, onOpenHostTrip)
}

@Composable
private fun ownerCardData(
    item: UnifiedRideItem.AsOwner,
    language: AppLanguage,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
): RideCardData {
    val ride = item.ride
    val booked = (ride.seatsTotal - ride.seatsLeft).coerceAtLeast(0)
    val (pill, pillLabel) = ownerRidePill(ride.status, booked)
    return RideCardData(
        status = pill,
        statusLabel = pillLabel,
        dateKey = isoToCairoDateKey(ride.departAt),
        time = cairoTime12(ride.departAt),
        pickupName = ride.fromPointName.resolve(language),
        dropOffName = ride.toPointName.resolve(language),
        title = stringResource(Res.string.rides_t14_seats_booked, booked, ride.seatsTotal),
        subtitle = stringResource(
            Res.string.rides_design_vehicle,
            ride.vehicle.make,
            ride.vehicle.model,
            ride.vehicle.color,
        ),
        tileIcon = HitchyIcons.Person,
        priceEgp = ride.pricePerSeat,
        perSeat = true,
        startLabel = stringResource(Res.string.owner_design_route_start),
        onClick = {
            if (ride.status.opensOwnerTrip()) {
                onOpenHostTrip(ride.id)
            } else {
                onOpenHostRideDetails(ride.id)
            }
        },
    )
}

private fun BookingStatus.toVariant(): RideStatusVariant = when (this) {
    BookingStatus.REQUESTED -> RideStatusVariant.Requested
    BookingStatus.BOOKED -> RideStatusVariant.Confirmed
    BookingStatus.BOARDED -> RideStatusVariant.InProgress
    BookingStatus.COMPLETED -> RideStatusVariant.Completed
    BookingStatus.DECLINED -> RideStatusVariant.Declined
    BookingStatus.EXPIRED -> RideStatusVariant.Expired
    BookingStatus.CANCELLED_BY_RIDER, BookingStatus.CANCELLED_BY_HOST -> RideStatusVariant.Cancelled
    BookingStatus.NO_SHOW -> RideStatusVariant.NoShow
}

private fun RideStatus.toVariant(): RideStatusVariant = when (this) {
    RideStatus.OPEN, RideStatus.FULL -> RideStatusVariant.Confirmed
    RideStatus.STARTED -> RideStatusVariant.InProgress
    RideStatus.FINISHED -> RideStatusVariant.Completed
    RideStatus.CANCELLED -> RideStatusVariant.Cancelled
    RideStatus.EXPIRED -> RideStatusVariant.Expired
}

private val TITLE_TOP = 49.dp

/** Figma's three tabs on My rides. */
private enum class RidesFilter { Upcoming, Completed, Cancelled }

private fun UnifiedRideItem.filter(): RidesFilter {
    val variant = when (this) {
        is UnifiedRideItem.AsRider -> booking.status.toVariant()
        is UnifiedRideItem.AsOwner -> ride.status.toVariant()
    }
    return when (variant) {
        RideStatusVariant.Completed -> RidesFilter.Completed

        RideStatusVariant.Cancelled, RideStatusVariant.Declined, RideStatusVariant.Expired, RideStatusVariant.NoShow ->
            RidesFilter.Cancelled

        else -> RidesFilter.Upcoming
    }
}
