@file:Suppress("MagicNumber", "TooManyFunctions")

package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.RideStatus
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.ErrorText
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.OwnerRequestCard
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.StatusChip
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.presentation.HostRideDetailsAction
import com.hitchy.feature.rides.presentation.HostRideDetailsEvent
import com.hitchy.feature.rides.presentation.HostRideDetailsState
import com.hitchy.feature.rides.presentation.HostRideDetailsViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.host_ride_details_no_booked
import hitchy.shared.generated.resources.host_ride_details_open_trip
import hitchy.shared.generated.resources.host_ride_details_requests_section
import hitchy.shared.generated.resources.host_ride_details_start_trip
import hitchy.shared.generated.resources.host_ride_details_title
import hitchy.shared.generated.resources.owner_design_egp
import hitchy.shared.generated.resources.owner_design_expected
import hitchy.shared.generated.resources.owner_design_expected_range
import hitchy.shared.generated.resources.owner_design_pickup_line
import hitchy.shared.generated.resources.owner_design_requests_waiting
import hitchy.shared.generated.resources.owner_design_riders_title
import hitchy.shared.generated.resources.owner_design_route_dropoff
import hitchy.shared.generated.resources.owner_design_route_start
import hitchy.shared.generated.resources.owner_design_seat_price
import hitchy.shared.generated.resources.owner_design_seats_open
import hitchy.shared.generated.resources.owner_design_status_posted
import hitchy.shared.generated.resources.owner_home_seats_booked
import hitchy.shared.generated.resources.owner_home_upcoming_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val CardRadius = 20.dp
private const val PENDING_NAME_LENGTH = 6
private const val BOOKED_NAME_LENGTH = 8

/** Owner ride details built to the demo design: route card, riders card, price card and the start button. */
@Composable
fun HostRideDetailsScreen(
    rideId: String,
    onBack: () -> Unit,
    onOpenTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HostRideDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val languageSwitcher: LanguageSwitcher = koinInject()
    val language = languageSwitcher.getCurrentLanguage()
    val colors = HitchyTheme.colors
    val spacing = HitchyTheme.spacing

    LaunchedEffect(rideId) {
        viewModel.onAction(HostRideDetailsAction.Init(rideId))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HostRideDetailsEvent.NavigateToTrip -> onOpenTrip(event.rideId)
                HostRideDetailsEvent.RideCancelled -> onBack()
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        DetailsHeader(onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space16),
        ) {
            detailsItems(
                state = state,
                language = language,
                onDismissAlert = { viewModel.onAction(HostRideDetailsAction.DismissAlert) },
                onRespond = { uid, accept ->
                    viewModel.onAction(HostRideDetailsAction.RespondRequest(uid, accept))
                },
            )
        }
        DetailsFooter(state = state, onOpenTrip = onOpenTrip)
    }
}

private fun LazyListScope.detailsItems(
    state: HostRideDetailsState,
    language: AppLanguage,
    onDismissAlert: () -> Unit,
    onRespond: (String, Boolean) -> Unit,
) {
    val alert = state.messageAlert
    if (alert != null) {
        item { HitchyBanner(text = stringResource(alert), onDismiss = onDismissAlert) }
    }
    val ride = state.ride
    if (ride != null) {
        item { RouteCard(ride = ride, language = language) }
    }
    if (state.pendingBookings.isNotEmpty()) {
        item { RequestsTitle(count = state.pendingBookings.size) }
        items(state.pendingBookings, key = { it.riderUid }) { booking ->
            val shortName = riderName(state, booking.riderUid, PENDING_NAME_LENGTH)
            OwnerRequestCard(
                riderName = shortName,
                riderInitials = shortName.take(2).uppercase(),
                riderRating = 5.0,
                seatsRequested = booking.seats,
                deadlineRemaining = "10 min",
                onAccept = { onRespond(booking.riderUid, true) },
                onDecline = { onRespond(booking.riderUid, false) },
            )
        }
    }
    item { RidersSection(state = state, language = language) }
    if (ride != null) {
        item { PriceCard(state = state) }
    }
    val error = state.error
    if (error != null) {
        item { ErrorText(error = error, style = HitchyTheme.typography.caption, color = HitchyTheme.colors.error) }
    }
}

private fun riderName(state: HostRideDetailsState, uid: String, fallbackLength: Int): String {
    val known = state.riderNames[uid]
    if (known != null) return known
    return uid.take(fallbackLength).replaceFirstChar { it.uppercase() }
}

@Composable
private fun DetailsHeader(onBack: () -> Unit) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        BackButton(
            onClick = onBack,
            style = BackButtonStyle.OutlinedCircle,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        Text(
            text = stringResource(Res.string.owner_home_upcoming_title),
            style = HitchyTheme.typography.h3,
            color = colors.navy,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun CardColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = HitchyTheme.colors
    val shape = RoundedCornerShape(CardRadius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.white)
            .border(1.dp, colors.border, shape),
    ) {
        content()
    }
}

@Composable
private fun RouteCard(ride: Ride, language: AppLanguage) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    CardColumn {
        Column(
            modifier = Modifier.padding(HitchyTheme.spacing.space16),
            verticalArrangement = Arrangement.spacedBy(HitchyTheme.spacing.space16),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RideStatusChip(status = ride.status)
                Text(
                    text = "${isoToCairoDateKey(ride.departAt)} · ${cairoTime12(ride.departAt)}",
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
            RoutePoints(
                fromName = ride.fromPointName.resolve(language),
                toName = ride.toPointName.resolve(language),
            )
        }
    }
}

@Composable
private fun RideStatusChip(status: RideStatus) {
    when (status) {
        RideStatus.OPEN,
        RideStatus.FULL,
        -> StatusChip(
            status = RideStatusVariant.InProgress,
            customLabel = stringResource(Res.string.owner_design_status_posted),
        )

        RideStatus.STARTED -> StatusChip(status = RideStatusVariant.InProgress)

        RideStatus.FINISHED -> StatusChip(status = RideStatusVariant.Completed)

        RideStatus.CANCELLED -> StatusChip(status = RideStatusVariant.Cancelled)

        RideStatus.EXPIRED -> StatusChip(status = RideStatusVariant.Expired)
    }
}

@Composable
private fun RoutePoints(fromName: String, toName: String) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(colors.deepPurple))
            }
            Column {
                Text(text = fromName, style = typography.bodyLargeStrong, color = colors.navy)
                Text(
                    text = stringResource(Res.string.owner_design_route_start),
                    style = typography.body,
                    color = colors.textSecondary,
                )
            }
        }
        Box(modifier = Modifier.width(24.dp).height(20.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.width(2.dp).height(20.dp).background(colors.lavender))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(HitchyIcons.Pin),
                    contentDescription = null,
                    tint = colors.pin,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column {
                Text(text = toName, style = typography.bodyLargeStrong, color = colors.navy)
                Text(
                    text = stringResource(Res.string.owner_design_route_dropoff),
                    style = typography.body,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun RequestsTitle(count: Int) {
    Text(
        text = stringResource(Res.string.host_ride_details_requests_section, count),
        style = HitchyTheme.typography.title,
        color = HitchyTheme.colors.textPrimary,
    )
}

@Composable
private fun RidersSection(state: HostRideDetailsState, language: AppLanguage) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val ride = state.ride
    Column(verticalArrangement = Arrangement.spacedBy(HitchyTheme.spacing.space12)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.owner_design_riders_title),
                style = typography.bodyStrong,
                color = colors.textSecondary,
            )
            if (ride != null) {
                Text(
                    text = stringResource(
                        Res.string.owner_home_seats_booked,
                        (ride.seatsTotal - ride.seatsLeft).coerceAtLeast(0),
                        ride.seatsTotal,
                    ),
                    style = typography.bodyStrong,
                    color = colors.purple,
                )
            }
        }
        CardColumn {
            if (state.confirmedBookings.isEmpty()) {
                Text(
                    text = stringResource(Res.string.host_ride_details_no_booked),
                    style = typography.body,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(HitchyTheme.spacing.space16),
                )
            }
            state.confirmedBookings.forEachIndexed { index, booking ->
                if (index > 0) CardDivider()
                RiderRow(
                    booking = booking,
                    name = riderName(state, booking.riderUid, BOOKED_NAME_LENGTH),
                    language = language,
                )
            }
            if (ride != null && ride.seatsLeft > 0) {
                if (state.confirmedBookings.isNotEmpty()) CardDivider()
                SeatsOpenRow(seatsLeft = ride.seatsLeft, waiting = state.pendingBookings.size)
            }
        }
    }
}

@Composable
private fun CardDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = HitchyTheme.spacing.space16)
            .fillMaxWidth()
            .height(1.dp)
            .background(HitchyTheme.colors.border),
    )
}

@Composable
private fun RiderRow(booking: Booking, name: String, language: AppLanguage) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(HitchyTheme.spacing.space16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HitchyTheme.spacing.space12),
    ) {
        Avatar(initials = name.take(2).uppercase())
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = typography.bodyStrong, color = colors.navy)
            Text(
                text = stringResource(
                    Res.string.owner_design_pickup_line,
                    cairoTime12(booking.pickupAt),
                    booking.pickupPointName.resolve(language),
                ),
                style = typography.caption,
                color = colors.purple,
            )
        }
    }
}

@Composable
private fun SeatsOpenRow(seatsLeft: Int, waiting: Int) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(HitchyTheme.spacing.space16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HitchyTheme.spacing.space12),
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.AddPerson),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.owner_design_seats_open, seatsLeft),
                style = typography.bodyStrong,
                color = colors.navy,
            )
            if (waiting > 0) {
                Text(
                    text = stringResource(Res.string.owner_design_requests_waiting, waiting),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun PriceCard(state: HostRideDetailsState) {
    val ride = state.ride ?: return
    CardColumn {
        Column(
            modifier = Modifier.padding(HitchyTheme.spacing.space16),
            verticalArrangement = Arrangement.spacedBy(HitchyTheme.spacing.space8),
        ) {
            PriceLine(
                label = stringResource(Res.string.owner_design_seat_price),
                value = stringResource(Res.string.owner_design_egp, ride.pricePerSeat),
            )
            PriceLine(
                label = stringResource(Res.string.owner_design_expected),
                value = stringResource(
                    Res.string.owner_design_expected_range,
                    state.expectedMinEarnings,
                    state.expectedMaxEarnings,
                ),
            )
        }
    }
}

@Composable
private fun PriceLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = HitchyTheme.typography.body, color = HitchyTheme.colors.textSecondary)
        Text(text = value, style = HitchyTheme.typography.bodyStrong, color = HitchyTheme.colors.navy)
    }
}

@Composable
private fun DetailsFooter(state: HostRideDetailsState, onOpenTrip: (String) -> Unit) {
    val spacing = HitchyTheme.spacing
    if (!state.isTripInProgress && !state.canStartTrip) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = spacing.space16, vertical = spacing.space16),
    ) {
        HitchyButton(onClick = { onOpenTrip(state.rideId) }, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(
                    if (state.isTripInProgress) {
                        Res.string.host_ride_details_open_trip
                    } else {
                        Res.string.host_ride_details_start_trip
                    },
                ),
            )
        }
    }
}
