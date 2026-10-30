package com.hitchy.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.RideStatus
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.RideCard
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.core.util.isoToCairoTime
import com.hitchy.feature.booking.domain.SearchInput
import com.hitchy.feature.booking.presentation.RiderRideDetailsAction
import com.hitchy.feature.booking.presentation.RiderRideDetailsState
import com.hitchy.feature.booking.presentation.RiderRideDetailsViewModel
import com.hitchy.feature.booking.presentation.RidesTabViewModel
import com.hitchy.feature.booking.presentation.SearchResultsAction
import com.hitchy.feature.booking.presentation.SearchResultsViewModel
import com.hitchy.feature.booking.presentation.UnifiedRideItem
import com.hitchy.feature.booking.presentation.opensOwnerTrip
import com.hitchy.feature.booking.presentation.opensRiderTrip
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.action_retry
import hitchy.shared.generated.resources.bk_egp_amount
import hitchy.shared.generated.resources.bk_request_ride
import hitchy.shared.generated.resources.ride_details_booking_accepted_title
import hitchy.shared.generated.resources.ride_details_open_trip_button
import hitchy.shared.generated.resources.ride_details_request_sent_body
import hitchy.shared.generated.resources.ride_details_request_sent_title
import hitchy.shared.generated.resources.ride_details_title
import hitchy.shared.generated.resources.ride_details_vehicle_label
import hitchy.shared.generated.resources.rides_tab_empty_body
import hitchy.shared.generated.resources.rides_tab_empty_title
import hitchy.shared.generated.resources.rides_tab_role_owner_badge
import hitchy.shared.generated.resources.rides_tab_role_rider_badge
import hitchy.shared.generated.resources.rides_tab_title
import hitchy.shared.generated.resources.search_results_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchResultsScreen(
    input: SearchInput,
    onBack: () -> Unit,
    onOpenRideDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    viewModel: SearchResultsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language: AppLanguage = languageSwitcher.getCurrentLanguage()
    val summary = rememberSearchSummary(input, language)

    LaunchedEffect(input) { viewModel.onAction(SearchResultsAction.Search(input)) }

    Surface(modifier = modifier.fillMaxSize(), color = HitchyTheme.colors.white) {
        val error = state.error
        when {
            state.isLoading -> SearchFindingContent(summary = summary, onBack = onBack)

            error != null -> SearchErrorContent(
                message = stringResource(error.message()),
                onBack = onBack,
                onRetry = { viewModel.onAction(SearchResultsAction.Retry) },
            )

            state.rides.isEmpty() -> SearchEmptyContent(
                summary = summary,
                onBack = onBack,
                onSearchAgain = { viewModel.onAction(SearchResultsAction.Retry) },
            )

            else -> SearchResultsBody(
                summary = summary,
                rides = state.rides,
                language = language,
                onBack = onBack,
                onOpenRideDetails = onOpenRideDetails,
            )
        }
    }
}

@Composable
private fun SearchErrorContent(
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val spacing = HitchyTheme.spacing
    Column(modifier = Modifier.fillMaxSize()) {
        BookingHeaderBar(title = stringResource(Res.string.search_results_title), onBack = onBack)
        Column(
            modifier = Modifier.fillMaxSize().padding(spacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.space16),
        ) {
            HitchyBanner(text = message, variant = BannerVariant.Error)
            HitchyButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(Res.string.action_retry))
            }
        }
    }
}

@Composable
fun RiderRideDetailsScreen(
    rideId: String,
    onBack: () -> Unit,
    onOpenTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    viewModel: RiderRideDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language: AppLanguage = languageSwitcher.getCurrentLanguage()

    LaunchedEffect(rideId) { viewModel.onAction(RiderRideDetailsAction.Init(rideId)) }

    Surface(modifier = modifier.fillMaxSize(), color = HitchyTheme.colors.white) {
        Column(modifier = Modifier.fillMaxSize()) {
            BookingHeaderBar(title = stringResource(Res.string.ride_details_title), onBack = onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                state.error?.let { error ->
                    HitchyBanner(
                        text = stringResource(error.message()),
                        variant = BannerVariant.Error,
                        onDismiss = { viewModel.onAction(RiderRideDetailsAction.DismissError) },
                    )
                }
                RideDetailsSections(
                    state = state,
                    language = language,
                    onSeatsChange = { viewModel.onAction(RiderRideDetailsAction.UpdateSeats(it)) },
                )
            }
            RideDetailsFooter(
                state = state,
                onRequest = { viewModel.onAction(RiderRideDetailsAction.RequestRide) },
                onOpenTrip = { onOpenTrip(rideId) },
            )
        }
    }
}

@Composable
private fun RideDetailsFooter(
    state: RiderRideDetailsState,
    onRequest: () -> Unit,
    onOpenTrip: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val ride = state.ride
    Column(modifier = Modifier.fillMaxWidth().background(colors.white)) {
        HorizontalDivider(color = colors.border, thickness = 1.dp)
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                state.isAccepted -> {
                    HitchyBanner(
                        text = stringResource(Res.string.ride_details_booking_accepted_title),
                        variant = BannerVariant.Success,
                    )
                    if (ride != null) {
                        Text(
                            text = stringResource(
                                Res.string.ride_details_vehicle_label,
                                ride.vehicle.make,
                                ride.vehicle.model,
                                ride.vehicle.color,
                                ride.vehicle.plate,
                            ),
                            style = typography.bodyStrong,
                            color = colors.navy,
                        )
                    }
                    HitchyButton(onClick = onOpenTrip, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(Res.string.ride_details_open_trip_button))
                    }
                }

                state.isRequested -> {
                    Text(
                        text = stringResource(Res.string.ride_details_request_sent_title),
                        style = typography.h3,
                        color = colors.navy,
                    )
                    HitchyBanner(
                        text = stringResource(Res.string.ride_details_request_sent_body),
                        variant = BannerVariant.Info,
                    )
                }

                else -> HitchyButton(
                    onClick = onRequest,
                    enabled = state.rideCard != null && !state.isRequesting,
                    loading = state.isRequesting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.bk_request_ride), modifier = Modifier.weight(1f))
                    Text(text = stringResource(Res.string.bk_egp_amount, state.totalEgp))
                }
            }
        }
    }
}

@Composable
fun RidesTabContent(
    onBack: (() -> Unit)?,
    onOpenRiderRideDetails: (String) -> Unit,
    onOpenRiderTrip: (String) -> Unit,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    viewModel: RidesTabViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val spacing = HitchyTheme.spacing
    val language: AppLanguage = languageSwitcher.getCurrentLanguage()

    Surface(modifier = modifier.fillMaxSize(), color = colors.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(title = stringResource(Res.string.rides_tab_title), onBackClick = onBack)

            if (state.items.isEmpty() && !state.isLoading) {
                EmptyState(
                    title = stringResource(Res.string.rides_tab_empty_title),
                    message = stringResource(Res.string.rides_tab_empty_body),
                    icon = HitchyIcons.Rides,
                    modifier = Modifier.fillMaxSize(),
                )
                return@Column
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                items(
                    items = state.items,
                    key = { item ->
                        when (item) {
                            is UnifiedRideItem.AsRider -> "rider_${item.rideId}"
                            is UnifiedRideItem.AsOwner -> "owner_${item.rideId}"
                        }
                    },
                ) { item ->
                    when (item) {
                        is UnifiedRideItem.AsRider -> {
                            val booking = item.booking
                            RideCard(
                                departureText = isoToCairoDateKey(booking.departAt),
                                pickupTime = isoToCairoTime(booking.pickupAt),
                                pickupLocation = booking.pickupPointName.resolve(language),
                                dropOffTime = "",
                                dropOffLocation = "",
                                hostName = stringResource(Res.string.rides_tab_role_rider_badge, booking.seats),
                                hostInitials = "",
                                carModelColor = "",
                                priceEgp = booking.price,
                                availableSeats = booking.seats,
                                status = booking.status.toStatusVariant(),
                                onClick = {
                                    if (booking.status.opensRiderTrip()) {
                                        onOpenRiderTrip(booking.rideId)
                                    } else {
                                        onOpenRiderRideDetails(booking.rideId)
                                    }
                                },
                            )
                        }

                        is UnifiedRideItem.AsOwner -> {
                            val ride = item.ride
                            val booked = (ride.seatsTotal - ride.seatsLeft).coerceAtLeast(0)
                            RideCard(
                                departureText = ride.dateKey,
                                pickupTime = isoToCairoTime(ride.departAt),
                                pickupLocation = ride.fromPointName.resolve(language),
                                dropOffTime = "",
                                dropOffLocation = ride.toPointName.resolve(language),
                                hostName = stringResource(
                                    Res.string.rides_tab_role_owner_badge,
                                    booked,
                                    ride.seatsTotal,
                                ),
                                hostInitials = "",
                                carModelColor = "",
                                priceEgp = ride.pricePerSeat,
                                availableSeats = ride.seatsLeft,
                                totalSeats = ride.seatsTotal,
                                status = ride.status.toStatusVariant(),
                                onClick = {
                                    if (ride.status.opensOwnerTrip()) {
                                        onOpenHostTrip(ride.id)
                                    } else {
                                        onOpenHostRideDetails(ride.id)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SecondaryAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    HitchyButton(onClick = onClick, variant = HitchyButtonVariant.Outline, modifier = modifier.fillMaxWidth()) {
        Text(text = label)
    }
}

private fun BookingStatus.toStatusVariant(): RideStatusVariant = when (this) {
    BookingStatus.REQUESTED -> RideStatusVariant.Requested
    BookingStatus.BOOKED -> RideStatusVariant.Confirmed
    BookingStatus.BOARDED -> RideStatusVariant.InProgress
    BookingStatus.COMPLETED -> RideStatusVariant.Completed
    BookingStatus.DECLINED -> RideStatusVariant.Declined
    BookingStatus.EXPIRED -> RideStatusVariant.Expired
    BookingStatus.CANCELLED_BY_RIDER, BookingStatus.CANCELLED_BY_HOST -> RideStatusVariant.Cancelled
    BookingStatus.NO_SHOW -> RideStatusVariant.NoShow
}

private fun RideStatus.toStatusVariant(): RideStatusVariant = when (this) {
    RideStatus.OPEN, RideStatus.FULL -> RideStatusVariant.Confirmed
    RideStatus.STARTED -> RideStatusVariant.InProgress
    RideStatus.FINISHED -> RideStatusVariant.Completed
    RideStatus.CANCELLED -> RideStatusVariant.Cancelled
    RideStatus.EXPIRED -> RideStatusVariant.Expired
}
