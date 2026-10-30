package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.CancelRideReason
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.ErrorText
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.OwnerRequestCard
import com.hitchy.core.ui.components.RouteTimeline
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.core.util.isoToCairoTime
import com.hitchy.feature.rides.presentation.HostRideDetailsAction
import com.hitchy.feature.rides.presentation.HostRideDetailsEvent
import com.hitchy.feature.rides.presentation.HostRideDetailsViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.host_ride_details_booked_section
import hitchy.shared.generated.resources.host_ride_details_cancel
import hitchy.shared.generated.resources.host_ride_details_expected
import hitchy.shared.generated.resources.host_ride_details_no_booked
import hitchy.shared.generated.resources.host_ride_details_no_requests
import hitchy.shared.generated.resources.host_ride_details_open_trip
import hitchy.shared.generated.resources.host_ride_details_pickup_label
import hitchy.shared.generated.resources.host_ride_details_requests_section
import hitchy.shared.generated.resources.host_ride_details_seats_count
import hitchy.shared.generated.resources.host_ride_details_start_trip
import hitchy.shared.generated.resources.host_ride_details_title
import hitchy.shared.generated.resources.owner_home_seats_booked
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

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
    val typography = HitchyTheme.typography
    val cardShape = RoundedCornerShape(16.dp)

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        TopBar(
            title = stringResource(Res.string.host_ride_details_title),
            onBackClick = onBack,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space16),
        ) {
            item { Spacer(modifier = Modifier.height(spacing.space8)) }

            // Message Alert
            if (state.messageAlert != null) {
                item {
                    HitchyBanner(
                        text = stringResource(state.messageAlert ?: Res.string.host_ride_details_title),
                        onDismiss = { viewModel.onAction(HostRideDetailsAction.DismissAlert) },
                    )
                }
            }

            // Expected Earnings Banner (plan 2.2 #7)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(colors.lavender)
                        .border(1.dp, colors.border, cardShape),
                    color = colors.lavender,
                    shape = cardShape,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.space16),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(
                                Res.string.host_ride_details_expected,
                                state.expectedMinEarnings,
                                state.expectedMaxEarnings,
                            ),
                            style = typography.title,
                            color = colors.purple,
                        )
                    }
                }
            }

            // Route & Details Card
            val r = state.ride
            if (r != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .border(1.dp, colors.border, cardShape),
                        color = colors.white,
                        shape = cardShape,
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.space16),
                            verticalArrangement = Arrangement.spacedBy(spacing.space12),
                        ) {
                            Text(
                                text = "${isoToCairoDateKey(r.departAt)} · ${isoToCairoTime(r.departAt)}",
                                style = typography.caption,
                                color = colors.textSecondary,
                            )
                            RouteTimeline(
                                pickupTime = isoToCairoTime(r.departAt),
                                pickupLocation = r.fromPointName.resolve(language),
                                dropOffTime = "",
                                dropOffLocation = r.toPointName.resolve(language),
                            )

                            val bookedCount = (r.seatsTotal - r.seatsLeft).coerceAtLeast(0)
                            Text(
                                text = stringResource(
                                    Res.string.owner_home_seats_booked,
                                    bookedCount,
                                    r.seatsTotal,
                                ),
                                style = typography.body,
                                color = colors.purple,
                            )

                            Text(
                                text = "${r.vehicle.make} ${r.vehicle.model} · ${r.vehicle.color} (${r.vehicle.plate})",
                                style = typography.caption,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            }

            // Pending Requests Section
            item {
                Text(
                    text = stringResource(
                        Res.string.host_ride_details_requests_section,
                        state.pendingBookings.size,
                    ),
                    style = typography.title,
                    color = colors.textPrimary,
                )
            }

            if (state.pendingBookings.isEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.host_ride_details_no_requests),
                        style = typography.body,
                        color = colors.textSecondary,
                    )
                }
            } else {
                items(state.pendingBookings, key = { it.riderUid }) { booking ->
                    val shortName = booking.riderUid.take(6).replaceFirstChar { it.uppercase() }
                    OwnerRequestCard(
                        riderName = shortName,
                        riderInitials = shortName.take(2).uppercase(),
                        riderRating = 5.0,
                        seatsRequested = booking.seats,
                        deadlineRemaining = "10 min",
                        onAccept = {
                            viewModel.onAction(
                                HostRideDetailsAction.RespondRequest(booking.riderUid, accept = true),
                            )
                        },
                        onDecline = {
                            viewModel.onAction(
                                HostRideDetailsAction.RespondRequest(booking.riderUid, accept = false),
                            )
                        },
                    )
                }
            }

            // Booked Riders Section
            item {
                Text(
                    text = stringResource(
                        Res.string.host_ride_details_booked_section,
                        state.confirmedBookings.size,
                    ),
                    style = typography.title,
                    color = colors.textPrimary,
                )
            }

            if (state.confirmedBookings.isEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.host_ride_details_no_booked),
                        style = typography.body,
                        color = colors.textSecondary,
                    )
                }
            } else {
                items(state.confirmedBookings, key = { it.riderUid }) { booking ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .border(1.dp, colors.border, cardShape),
                        color = colors.white,
                        shape = cardShape,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.space16),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.space12),
                        ) {
                            val shortName = booking.riderUid.take(8).replaceFirstChar { it.uppercase() }
                            Avatar(initials = shortName.take(2).uppercase())
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = shortName,
                                    style = typography.title,
                                    color = colors.textPrimary,
                                )
                                Text(
                                    text = stringResource(
                                        Res.string.host_ride_details_pickup_label,
                                        booking.pickupPointName.resolve(language),
                                    ),
                                    style = typography.caption,
                                    color = colors.textSecondary,
                                )
                            }
                            Text(
                                text = stringResource(
                                    Res.string.host_ride_details_seats_count,
                                    booking.seats,
                                ),
                                style = typography.caption,
                                color = colors.purple,
                            )
                        }
                    }
                }
            }

            val error = state.error
            if (error != null) {
                item {
                    ErrorText(
                        error = error,
                        style = typography.caption,
                        color = colors.error,
                    )
                }
            }

            // CTAs
            item {
                Spacer(modifier = Modifier.height(spacing.space8))
                if (state.isTripInProgress) {
                    HitchyButton(
                        onClick = { onOpenTrip(state.rideId) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.host_ride_details_open_trip))
                    }
                } else if (state.canStartTrip) {
                    HitchyButton(
                        onClick = { onOpenTrip(state.rideId) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.host_ride_details_start_trip))
                    }
                }

                Spacer(modifier = Modifier.height(spacing.space8))
                HitchyButton(
                    onClick = {
                        viewModel.onAction(
                            HostRideDetailsAction.CancelRide(CancelRideReason.PLANS_CHANGED),
                        )
                    },
                    loading = state.isCancelling,
                    variant = HitchyButtonVariant.Outline,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.host_ride_details_cancel))
                }
                Spacer(modifier = Modifier.height(spacing.space32))
            }
        }
    }
}
