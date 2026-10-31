@file:Suppress("TooManyFunctions")

package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.CancelRideReason
import com.hitchy.core.model.RideStatus
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.AvatarImage
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.FloatingBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.OwnerRequestCard
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.SkeletonDetails
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tightLine
import com.hitchy.core.util.dateKey
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.feature.booking.ui.RideCardData
import com.hitchy.feature.booking.ui.RideDesignCard
import com.hitchy.feature.booking.ui.ownerRidePill
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.rides.presentation.HostRideDetailsAction
import com.hitchy.feature.rides.presentation.HostRideDetailsEvent
import com.hitchy.feature.rides.presentation.HostRideDetailsState
import com.hitchy.feature.rides.presentation.HostRideDetailsViewModel
import com.hitchy.feature.trip.ui.CancelReasonRow
import com.hitchy.feature.trip.ui.FigmaSheet
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.host_ride_details_cancel
import hitchy.shared.generated.resources.host_ride_details_no_booked
import hitchy.shared.generated.resources.host_ride_details_open_trip
import hitchy.shared.generated.resources.host_ride_details_requests_section
import hitchy.shared.generated.resources.host_ride_details_start_trip
import hitchy.shared.generated.resources.host_ride_details_title
import hitchy.shared.generated.resources.offer_fair_price
import hitchy.shared.generated.resources.owner_cancel_car_problem
import hitchy.shared.generated.resources.owner_cancel_sick
import hitchy.shared.generated.resources.owner_cancel_title
import hitchy.shared.generated.resources.owner_design_egp
import hitchy.shared.generated.resources.owner_design_expected
import hitchy.shared.generated.resources.owner_design_expected_range
import hitchy.shared.generated.resources.owner_design_pickup_line
import hitchy.shared.generated.resources.owner_design_riders_title
import hitchy.shared.generated.resources.owner_design_route_start
import hitchy.shared.generated.resources.owner_design_seat_price
import hitchy.shared.generated.resources.owner_design_status_posted
import hitchy.shared.generated.resources.owner_home_seats_booked
import hitchy.shared.generated.resources.owner_keep_ride
import hitchy.shared.generated.resources.owner_one_request_waiting
import hitchy.shared.generated.resources.owner_one_seat_open
import hitchy.shared.generated.resources.owner_requests_waiting
import hitchy.shared.generated.resources.owner_review_one_request
import hitchy.shared.generated.resources.owner_review_requests
import hitchy.shared.generated.resources.owner_seats_open_count
import hitchy.shared.generated.resources.rider_trip_cancel_reason_other
import hitchy.shared.generated.resources.rider_trip_cancel_reason_plans_changed
import hitchy.shared.generated.resources.rides_design_vehicle
import hitchy.shared.generated.resources.rides_t14_seats_booked
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private val CardShape = RoundedCornerShape(16.dp)
private const val PENDING_NAME_LENGTH = 6
private const val BOOKED_NAME_LENGTH = 8

/** The list position of the requests title: after the alert (when shown) and the ride card. */
private const val REQUESTS_ITEM_OFFSET = 1

/**
 * Owner ride details, Figma DEMO R02 (726:11883 and 726:11968): the shared Ride Card, the requests,
 * the Riders card, the price card, and the footer ("Review N requests" or "Start trip", then Cancel).
 * Figma's message and call buttons, ratings and "ID verified" chips are not in the demo.
 */
@Composable
fun HostRideDetailsScreen(
    rideId: String,
    onBack: () -> Unit,
    onOpenTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HostRideDetailsViewModel = koinViewModel(),
    languageSwitcher: LanguageSwitcher = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = languageSwitcher.getCurrentLanguage()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showCancel by remember { mutableStateOf(false) }

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

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(HitchyTheme.colors.white)) {
            OfferTopBar(title = stringResource(Res.string.host_ride_details_title), onBack = onBack, horizontal = 16.dp)
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                detailsItems(
                    state = state,
                    language = language,
                    onRespond = { uid, accept ->
                        viewModel.onAction(HostRideDetailsAction.RespondRequest(uid, accept))
                    },
                )
            }
            DetailsFooter(
                state = state,
                onReviewRequests = {
                    scope.launch { listState.animateScrollToItem(REQUESTS_ITEM_OFFSET) }
                },
                onOpenTrip = onOpenTrip,
                onCancel = { showCancel = true },
            )
        }
        state.messageAlert?.let { alert ->
            FloatingBanner(
                text = stringResource(alert),
                variant = BannerVariant.Success,
                onDismiss = { viewModel.onAction(HostRideDetailsAction.DismissAlert) },
            )
        }
        state.error?.let { error ->
            FloatingBanner(
                text = stringResource(error.message()),
                onDismiss = { viewModel.onAction(HostRideDetailsAction.DismissAlert) },
            )
        }
    }

    if (showCancel) {
        OwnerCancelSheet(
            isCancelling = state.isCancelling,
            onConfirm = { viewModel.onAction(HostRideDetailsAction.CancelRide(it)) },
            onDismiss = { showCancel = false },
        )
    }
}

private fun LazyListScope.detailsItems(
    state: HostRideDetailsState,
    language: AppLanguage,
    onRespond: (String, Boolean) -> Unit,
) {
    if (state.ride == null && state.error == null) {
        // The ride has not answered yet.
        item { SkeletonDetails(showMap = false) }
        return
    }
    if (state.ride != null) {
        item { OwnerRideCard(state = state, language = language) }
    }
    if (state.pendingBookings.isNotEmpty()) {
        item {
            Text(
                text = stringResource(Res.string.host_ride_details_requests_section, state.pendingBookings.size),
                style = HitchyTheme.typography.bodyBold,
                color = HitchyTheme.colors.textSecondary,
            )
        }
        items(state.pendingBookings, key = { it.riderUid }) { booking ->
            val shortName = riderName(state, booking.riderUid, PENDING_NAME_LENGTH)
            OwnerRequestCard(
                riderName = shortName,
                riderInitials = shortName.take(2).uppercase(),
                riderPhotoPath = state.riderPhotos[booking.riderUid],
                riderRating = 5.0,
                seatsRequested = booking.seats,
                deadlineRemaining = "10 min",
                onAccept = { onRespond(booking.riderUid, true) },
                onDecline = { onRespond(booking.riderUid, false) },
                showReviewBadge = false,
            )
        }
    }
    if (state.ride != null) {
        item { RidersSection(state = state, language = language) }
        item { PriceCard(state = state) }
    }
}

private fun riderName(state: HostRideDetailsState, uid: String, fallbackLength: Int): String {
    val known = state.riderNames[uid]
    if (known != null) return known
    return uid.take(fallbackLength).replaceFirstChar { it.uppercase() }
}

/** Figma's `Ride Card` (component 367:2121), the same card as the Rides list, with "Posted" and the requests. */
@Composable
private fun OwnerRideCard(
    state: HostRideDetailsState,
    language: AppLanguage,
    clock: Clock = koinInject(),
) {
    val ride = state.ride ?: return
    val now = clock.now()
    val waiting = state.pendingBookings.size
    val booked = state.confirmedBookings.sumOf { it.seats }
    val (pill, pillLabel) = ownerRidePill(ride.status, booked)
    RideDesignCard(
        data = RideCardData(
            status = pill,
            statusLabel = pillLabel,
            dateKey = isoToCairoDateKey(ride.departAt),
            time = cairoTime12(ride.departAt),
            pickupName = ride.fromPointName.resolve(language),
            dropOffName = ride.toPointName.resolve(language),
            title = stringResource(Res.string.rides_t14_seats_booked, booked, ride.seatsTotal),
            subtitle = if (waiting > 0) {
                requestsWaiting(waiting)
            } else {
                stringResource(
                    Res.string.rides_design_vehicle,
                    ride.vehicle.make,
                    ride.vehicle.model,
                    ride.vehicle.color,
                )
            },
            tileIcon = HitchyIcons.Person,
            priceEgp = ride.pricePerSeat,
            onClick = null,
            perSeat = true,
            startLabel = stringResource(Res.string.owner_design_route_start),
        ),
        todayKey = dateKey(now),
        tomorrowKey = dateKey(now + 1.days),
    )
}

/**
 * Figma's Riders block: "Riders" (Bold 14/24 gray) and "1 of 3 booked" (600 purple), then a card
 * (r16, #E5E7EB stroke, padding 12/16) with one row per booked rider and the open seats row.
 */
@Composable
private fun RidersSection(state: HostRideDetailsState, language: AppLanguage) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val ride = state.ride
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.owner_design_riders_title),
                style = typography.bodyBold,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            if (ride != null) {
                Text(
                    text = stringResource(
                        Res.string.owner_home_seats_booked,
                        state.confirmedBookings.sumOf { it.seats },
                        ride.seatsTotal,
                    ),
                    style = typography.bodyStrong,
                    color = colors.purple,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .border(1.dp, colors.border, CardShape)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.confirmedBookings.isEmpty() && ride == null) {
                Text(
                    text = stringResource(Res.string.host_ride_details_no_booked),
                    style = typography.body,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            // Figma counts the seats not booked yet as open, held ones included.
            val bookedSeats = state.confirmedBookings.sumOf { it.seats }
            val open = ride?.let { (it.seatsTotal - bookedSeats).coerceAtLeast(0) } ?: 0
            val rows = state.confirmedBookings.size + if (open > 0) 1 else 0
            state.confirmedBookings.forEachIndexed { index, booking ->
                RiderRow(
                    booking = booking,
                    name = riderName(state, booking.riderUid, BOOKED_NAME_LENGTH),
                    photoPath = state.riderPhotos[booking.riderUid],
                    language = language,
                    divider = index < rows - 1,
                )
            }
            if (open > 0) SeatsOpenRow(seatsOpen = open, waiting = state.pendingBookings.size)
        }
    }
}

/** Figma's rider row: a 48dp lavender avatar with Bold 14/24 initials, the 600 14/22 name, the purple pickup line. */
@Composable
private fun RiderRow(booking: Booking, name: String, photoPath: String?, language: AppLanguage, divider: Boolean) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.lavender),
                contentAlignment = Alignment.Center,
            ) {
                AvatarImage(
                    photoPath = photoPath,
                    initials = name.take(2).uppercase(),
                    textStyle = typography.bodyBold,
                    textColor = colors.purple,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = name, style = typography.bodyStrong, color = colors.navy, modifier = Modifier.tightLine(typography.bodyStrong.fontSize))
                Text(
                    text = stringResource(
                        Res.string.owner_design_pickup_line,
                        cairoTime12(booking.pickupAt),
                        booking.pickupPointName.resolve(language),
                    ),
                    style = typography.caption,
                    modifier = Modifier.tightLine(typography.caption.fontSize),
                    color = colors.purple,
                )
            }
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

/** Figma's open seats row: a 48dp lavender circle with a purple ring and the add-person icon. */
@Composable
private fun SeatsOpenRow(seatsOpen: Int, waiting: Int) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.lavender)
                .border(1.dp, colors.purple, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.AddPerson),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(24.dp),
            )
        }
        Column {
            Text(
                text = if (seatsOpen == 1) {
                    stringResource(Res.string.owner_one_seat_open)
                } else {
                    stringResource(Res.string.owner_seats_open_count, seatsOpen)
                },
                style = typography.bodyStrong,
                color = colors.navy,
            )
            if (waiting > 0) {
                Text(
                    text = requestsWaiting(waiting),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

/**
 * Figma's `RideDetails` price card: padding 12/16, r16. "Seat price" and "Expected" (12/18 gray) on the
 * start side; "EGP 56 · Fair price" and "EGP 56 · up to EGP 168" (Bold 12/18) on the end side.
 */
@Composable
private fun PriceCard(state: HostRideDetailsState) {
    val ride = state.ride ?: return
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .border(1.dp, colors.border, CardShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(Res.string.owner_design_seat_price),
                style = typography.caption,
                color = colors.textSecondary,
            )
            Text(
                stringResource(Res.string.owner_design_expected),
                style = typography.caption,
                color = colors.textSecondary,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(Res.string.owner_design_egp, ride.pricePerSeat) + " ·",
                    style = typography.captionStrong,
                    color = colors.navy,
                )
                Text(
                    text = stringResource(Res.string.offer_fair_price),
                    style = typography.captionStrong,
                    color = colors.successText,
                )
            }
            Text(
                text = stringResource(
                    Res.string.owner_design_expected_range,
                    state.expectedMinEarnings,
                    state.expectedMaxEarnings,
                ),
                style = typography.captionStrong,
                color = colors.navy,
                textAlign = TextAlign.End,
            )
        }
    }
}

/**
 * Figma's `Booking Actions`: the main button ("Review N requests" while requests wait, else "Start trip")
 * and the red "Cancel" button while the ride is posted.
 */
@Composable
private fun DetailsFooter(
    state: HostRideDetailsState,
    onReviewRequests: () -> Unit,
    onOpenTrip: (String) -> Unit,
    onCancel: () -> Unit,
) {
    if (!state.isTripInProgress && !state.canStartTrip) return
    val waiting = state.pendingBookings.size
    OfferFooter {
        when {
            state.isTripInProgress -> FooterButton(stringResource(Res.string.host_ride_details_open_trip)) {
                onOpenTrip(state.rideId)
            }

            waiting > 0 -> FooterButton(
                if (waiting == 1) {
                    stringResource(Res.string.owner_review_one_request)
                } else {
                    stringResource(Res.string.owner_review_requests, waiting)
                },
                onReviewRequests,
            )

            else -> FooterButton(stringResource(Res.string.host_ride_details_start_trip)) { onOpenTrip(state.rideId) }
        }
        if (state.canStartTrip) {
            HitchyButton(
                onClick = onCancel,
                variant = HitchyButtonVariant.Destructive,
                enabled = !state.isCancelling,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.host_ride_details_cancel))
            }
        }
    }
}

@Composable
private fun FooterButton(text: String, onClick: () -> Unit) {
    HitchyButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text = text) }
}

/** The owner's cancel sheet in the rider's Figma cancel style (390:1968), with the ride's own reasons. */
@Composable
private fun OwnerCancelSheet(
    isCancelling: Boolean,
    onConfirm: (CancelRideReason) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    var selected by remember { mutableStateOf(CancelRideReason.PLANS_CHANGED) }
    val reasons = listOf(
        CancelRideReason.PLANS_CHANGED to stringResource(Res.string.rider_trip_cancel_reason_plans_changed),
        CancelRideReason.SICK to stringResource(Res.string.owner_cancel_sick),
        CancelRideReason.CAR_PROBLEM to stringResource(Res.string.owner_cancel_car_problem),
        CancelRideReason.OTHER to stringResource(Res.string.rider_trip_cancel_reason_other),
    )
    FigmaSheet(onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(Res.string.owner_cancel_title),
                style = typography.h1,
                color = colors.navy,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Column {
                reasons.forEach { (reason, label) ->
                    CancelReasonRow(
                        label = label,
                        selected = selected == reason,
                        enabled = !isCancelling,
                        onClick = { selected = reason },
                    )
                }
            }
            HitchyButton(
                onClick = { onConfirm(selected) },
                variant = HitchyButtonVariant.Destructive,
                enabled = !isCancelling,
                loading = isCancelling,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.host_ride_details_cancel))
            }
            Text(
                text = stringResource(Res.string.owner_keep_ride),
                style = typography.bodyBold,
                color = if (isCancelling) colors.textDisabled else colors.purple,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .then(if (isCancelling) Modifier else Modifier.clickableShape(RoundedCornerShape(8.dp), onDismiss))
                    .padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun requestsWaiting(count: Int): String = if (count == 1) {
    stringResource(Res.string.owner_one_request_waiting)
} else {
    stringResource(Res.string.owner_requests_waiting, count)
}
