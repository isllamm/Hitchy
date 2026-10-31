@file:Suppress("MagicNumber", "TooManyFunctions")

package com.hitchy.feature.trip.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.ErrorText
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.SkeletonDetails
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.dateKey
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.trip.presentation.HostTripAction
import com.hitchy.feature.trip.presentation.HostTripPhase
import com.hitchy.feature.trip.presentation.HostTripState
import com.hitchy.feature.trip.presentation.HostTripViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.host_trip_btn_boarded
import hitchy.shared.generated.resources.host_trip_btn_confirm_payment
import hitchy.shared.generated.resources.host_trip_btn_end_trip
import hitchy.shared.generated.resources.host_trip_btn_im_here
import hitchy.shared.generated.resources.host_trip_btn_next_stop
import hitchy.shared.generated.resources.host_trip_btn_payment_done
import hitchy.shared.generated.resources.host_trip_btn_start
import hitchy.shared.generated.resources.host_trip_status_completed
import hitchy.shared.generated.resources.host_trip_status_leaving
import hitchy.shared.generated.resources.owner_design_all_on_board
import hitchy.shared.generated.resources.owner_design_arrived_at
import hitchy.shared.generated.resources.owner_design_eyebrow_arrived
import hitchy.shared.generated.resources.owner_design_eyebrow_at_pickups
import hitchy.shared.generated.resources.owner_design_heading_to
import hitchy.shared.generated.resources.owner_design_leave_at
import hitchy.shared.generated.resources.owner_design_on_board_count
import hitchy.shared.generated.resources.owner_design_report_late
import hitchy.shared.generated.resources.owner_design_riders_count
import hitchy.shared.generated.resources.owner_design_step_completed
import hitchy.shared.generated.resources.owner_design_step_on_the_way
import hitchy.shared.generated.resources.owner_design_step_pickups
import hitchy.shared.generated.resources.owner_design_waiting_for
import hitchy.shared.generated.resources.owner_one_rider
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private val MapHeight = 330.dp
private val SheetTopOffset = 280.dp
private val SheetRadius = 24.dp
private const val ALL_STEPS_DONE = 4

/**
 * Owner trip day built to the demo design: the map with a straight route line on top, and a rounded sheet with
 * the headline, the four steps, the riders, the wait timer and the big action button.
 */
@Composable
fun HostTripScreen(
    rideId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HostTripViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val languageSwitcher: LanguageSwitcher = koinInject()
    val language = languageSwitcher.getCurrentLanguage()
    var selectedUid by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(rideId) {
        viewModel.onAction(HostTripAction.Init(rideId))
    }

    val current = currentRider(state.bookings, selectedUid, state.currentPickupId)
    Box(modifier = modifier.fillMaxSize().background(HitchyTheme.colors.background)) {
        HitchyMap(
            pins = tripPins(state.ride),
            route = tripRoute(state.ride),
            modifier = Modifier.fillMaxWidth().height(MapHeight),
        )
        BackButton(
            onClick = onBack,
            style = BackButtonStyle.OutlinedCircle,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 8.dp, top = 8.dp),
        )
        TripSheet(
            state = state,
            language = language,
            current = current,
            onSelectRider = { selectedUid = it },
            onAction = viewModel::onAction,
            onDone = onBack,
            modifier = Modifier.fillMaxSize().padding(top = SheetTopOffset),
        )
    }
}

/** Fixed points only: the start and the ride's stops (the ride has no destination coordinates). */
private fun tripRoute(ride: Ride?): List<GeoPoint> {
    if (ride == null) return emptyList()
    return listOf(ride.fromPoint) + ride.stops.map { stop -> stop.point }
}

private fun tripPins(ride: Ride?): List<MapPin> {
    if (ride == null) return emptyList()
    val stops = ride.stops.mapIndexed { index, stop -> MapPin(id = "stop_$index", point = stop.point) }
    return listOf(MapPin(id = "from", point = ride.fromPoint)) + stops
}

@Composable
private fun TripSheet(
    state: HostTripState,
    language: AppLanguage,
    current: Booking?,
    onSelectRider: (String) -> Unit,
    onAction: (HostTripAction) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius))
            .background(colors.white),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 4.dp)
                .clip(CircleShape)
                .background(colors.border),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (state.ride == null && state.error == null) {
                // The ride has not answered yet.
                SkeletonDetails(showMap = false)
            } else {
                SheetHeader(state = state, language = language, current = current)
                OwnerTripProgress(currentIndex = progressIndex(state.phase))
                SheetBody(state = state, language = language, current = current, onSelectRider = onSelectRider)
            }
            val error = state.error
            if (error != null) {
                ErrorText(error = error, style = HitchyTheme.typography.caption, color = colors.error)
            }
        }
        ActionBar(state = state, current = current, onAction = onAction, onDone = onDone)
    }
}

@Composable
private fun SheetBody(
    state: HostTripState,
    language: AppLanguage,
    current: Booking?,
    onSelectRider: (String) -> Unit,
) {
    if (state.phase == HostTripPhase.PAYMENT_DONE) {
        PaymentSummaryCard(state = state)
        return
    }
    if (state.bookings.isNotEmpty()) {
        RiderStatusCard(
            state = state,
            language = language,
            currentUid = current?.riderUid,
            onSelect = onSelectRider,
        )
    }
    if (state.phase == HostTripPhase.AT_STOP) {
        WaitTimerCard(state = state)
    }
}

private fun progressIndex(phase: HostTripPhase): Int = when (phase) {
    HostTripPhase.LEAVING -> 0
    HostTripPhase.DRIVING_TO_STOP, HostTripPhase.AT_STOP -> 1
    HostTripPhase.EN_ROUTE -> 2
    HostTripPhase.ARRIVED, HostTripPhase.PAYMENT_DONE -> ALL_STEPS_DONE
}

@Composable
private fun SheetHeader(
    state: HostTripState,
    language: AppLanguage,
    current: Booking?,
) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = eyebrow(state),
                style = HitchyTheme.typography.captionStrong,
                color = colors.purple,
            )
            Text(
                text = headline(state, language, current),
                style = HitchyTheme.typography.amount,
                color = colors.navy,
                maxLines = 2,
            )
        }
        CountPill(state = state)
    }
}

/** Figma's eyebrow: the day ("Today") before leaving, then the step. */
@Composable
private fun eyebrow(state: HostTripState, clock: Clock = koinInject()): String {
    if (state.phase == HostTripPhase.LEAVING) {
        val day = state.ride?.dateKey
        val now = clock.now()
        when (day) {
            dateKey(now) -> return stringResource(Res.string.rider_home_date_today)
            dateKey(now + 1.days) -> return stringResource(Res.string.rider_home_date_tomorrow)
        }
    }
    return stringResource(eyebrowRes(state.phase))
}

private fun eyebrowRes(phase: HostTripPhase): StringResource = when (phase) {
    HostTripPhase.LEAVING -> Res.string.host_trip_status_leaving
    HostTripPhase.DRIVING_TO_STOP -> Res.string.owner_design_step_pickups
    HostTripPhase.AT_STOP -> Res.string.owner_design_eyebrow_at_pickups
    HostTripPhase.EN_ROUTE -> Res.string.owner_design_step_on_the_way
    HostTripPhase.ARRIVED -> Res.string.owner_design_eyebrow_arrived
    HostTripPhase.PAYMENT_DONE -> Res.string.owner_design_step_completed
}

@Composable
private fun headline(
    state: HostTripState,
    language: AppLanguage,
    current: Booking?,
): String {
    val destination = state.ride?.toPointName?.resolve(language).orEmpty()
    return when (state.phase) {
        HostTripPhase.LEAVING ->
            stringResource(Res.string.owner_design_leave_at, cairoTime12(state.ride?.departAt))

        HostTripPhase.DRIVING_TO_STOP ->
            stringResource(Res.string.owner_design_heading_to, currentStopName(state, language))

        HostTripPhase.AT_STOP -> if (current != null) {
            stringResource(Res.string.owner_design_waiting_for, riderDisplayName(state.riderNames, current.riderUid))
        } else {
            stringResource(Res.string.owner_design_all_on_board)
        }

        HostTripPhase.EN_ROUTE -> stringResource(Res.string.owner_design_heading_to, destination)

        HostTripPhase.ARRIVED -> stringResource(Res.string.owner_design_arrived_at, destination)

        HostTripPhase.PAYMENT_DONE -> stringResource(Res.string.host_trip_status_completed)
    }
}

private fun currentStopName(state: HostTripState, language: AppLanguage): String {
    val ride = state.ride
    val point = if (state.currentStopIndex == 0) {
        ride?.fromPointName
    } else {
        ride?.stops?.getOrNull(state.currentStopIndex - 1)?.pointName
    }
    val stopName = point?.resolve(language)
    return stopName?.takeIf { it.isNotBlank() } ?: state.currentStopName
}

@Composable
private fun CountPill(state: HostTripState) {
    val total = state.bookings.size
    when {
        state.phase == HostTripPhase.LEAVING ->
            CountChip(
                if (total == 1) {
                    stringResource(Res.string.owner_one_rider)
                } else {
                    stringResource(Res.string.owner_design_riders_count, total)
                },
                PillTone.Warning,
            )

        else -> {
            val everyone = total > 0 && state.boardedCount == total
            CountChip(
                text = stringResource(Res.string.owner_design_on_board_count, state.boardedCount),
                tone = if (everyone) PillTone.Success else PillTone.Warning,
            )
        }
    }
}

@Composable
private fun ActionBar(
    state: HostTripState,
    current: Booking?,
    onAction: (HostTripAction) -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (state.phase) {
            HostTripPhase.LEAVING ->
                MainAction(Res.string.host_trip_btn_start, state.isLoading) { onAction(HostTripAction.StartTrip) }

            HostTripPhase.DRIVING_TO_STOP ->
                MainAction(Res.string.host_trip_btn_im_here, state.isLoading) { onAction(HostTripAction.ArriveAtStop) }

            HostTripPhase.AT_STOP -> AtStopActions(state = state, current = current, onAction = onAction)

            HostTripPhase.EN_ROUTE ->
                MainAction(Res.string.host_trip_btn_end_trip, state.isLoading) { onAction(HostTripAction.FinishTrip) }

            HostTripPhase.ARRIVED ->
                MainAction(Res.string.host_trip_btn_confirm_payment, state.isLoading) {
                    onAction(HostTripAction.ConfirmPayment)
                }

            HostTripPhase.PAYMENT_DONE -> MainAction(Res.string.host_trip_btn_payment_done, false, onDone)
        }
    }
}

@Composable
private fun AtStopActions(
    state: HostTripState,
    current: Booking?,
    onAction: (HostTripAction) -> Unit,
) {
    if (current == null) {
        MainAction(Res.string.host_trip_btn_next_stop, false) { onAction(HostTripAction.NextStopOrFinish) }
        return
    }
    val name = riderDisplayName(state.riderNames, current.riderUid)
    HitchyButton(
        onClick = { onAction(HostTripAction.MarkBoarded(current.riderUid)) },
        loading = state.isLoading,
        variant = if (state.isFreeWaitOver) HitchyButtonVariant.Primary else HitchyButtonVariant.Tonal,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(Res.string.host_trip_btn_boarded, name))
    }
    if (state.isFreeWaitOver && current.lateFeeId == null) {
        HitchyButton(
            onClick = { onAction(HostTripAction.ReportLate(current.riderUid)) },
            loading = state.isLoading,
            variant = HitchyButtonVariant.Destructive,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.owner_design_report_late, name))
        }
    }
    HitchyButton(
        onClick = { onAction(HostTripAction.NextStopOrFinish) },
        variant = HitchyButtonVariant.Outline,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(Res.string.host_trip_btn_next_stop))
    }
}

@Composable
private fun MainAction(
    label: StringResource,
    loading: Boolean,
    onClick: () -> Unit,
) {
    HitchyButton(onClick = onClick, loading = loading, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(label))
    }
}
