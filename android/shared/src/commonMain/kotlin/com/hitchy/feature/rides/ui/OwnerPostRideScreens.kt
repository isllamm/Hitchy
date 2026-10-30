package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.HitchySelect
import com.hitchy.core.ui.components.HitchySwitch
import com.hitchy.core.ui.components.PassengersStepper
import com.hitchy.core.ui.components.PriceSlider
import com.hitchy.core.ui.components.RouteInput
import com.hitchy.core.ui.components.RouteTimeline
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.rides.presentation.FIELD_POST_FROM
import com.hitchy.feature.rides.presentation.FIELD_POST_TO
import com.hitchy.feature.rides.presentation.PostRideAction
import com.hitchy.feature.rides.presentation.PostRideEvent
import com.hitchy.feature.rides.presentation.PostRideState
import com.hitchy.feature.rides.presentation.PostRideStep
import com.hitchy.feature.rides.presentation.PostRideViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.post_ride_continue
import hitchy.shared.generated.resources.post_ride_fair_price
import hitchy.shared.generated.resources.post_ride_leave_at
import hitchy.shared.generated.resources.post_ride_live_body
import hitchy.shared.generated.resources.post_ride_live_go_to_rides
import hitchy.shared.generated.resources.post_ride_live_title
import hitchy.shared.generated.resources.post_ride_per_seat
import hitchy.shared.generated.resources.post_ride_price_breakdown_fuel
import hitchy.shared.generated.resources.post_ride_price_breakdown_running
import hitchy.shared.generated.resources.post_ride_price_breakdown_trip
import hitchy.shared.generated.resources.post_ride_price_header
import hitchy.shared.generated.resources.post_ride_price_max
import hitchy.shared.generated.resources.post_ride_price_min
import hitchy.shared.generated.resources.post_ride_price_recommended
import hitchy.shared.generated.resources.post_ride_publish
import hitchy.shared.generated.resources.post_ride_review_title
import hitchy.shared.generated.resources.post_ride_route_title
import hitchy.shared.generated.resources.post_ride_seats_hint
import hitchy.shared.generated.resources.post_ride_seats_title
import hitchy.shared.generated.resources.post_ride_seats_open
import hitchy.shared.generated.resources.post_ride_step_one
import hitchy.shared.generated.resources.post_ride_step_two
import hitchy.shared.generated.resources.post_ride_summary_title
import hitchy.shared.generated.resources.post_ride_women_only
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val MINUTES_PER_HOUR = 60
private const val STEP_MINUTES = 5

/**
 * The drop-off time needs the route's usual minutes, and `quotePrice` does not return them
 * (QuoteResult has distanceKm only), so the owner's summary leaves it blank rather than repeat
 * the departure time. Noted for P6-B01 (ADR 0003 row 95).
 */
private const val UNKNOWN_TIME = "—"

/**
 * The owner's ride-posting wizard: route and schedule, then price and publish, then the result.
 * Built from the D2, D4 and D5 specs. D3's ride-vibe preferences are not built, because only
 * Women only has a field in `createRide` (ADR 0003 row 94), so the wizard has two steps.
 */
@Composable
fun PostRideScreen(
    onDismiss: () -> Unit,
    onOpenPointPicker: (field: String) -> Unit,
    onOpenMyRides: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostRideViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PostRideEvent.Published -> Unit // the Live step shows the result
                PostRideEvent.Dismissed -> onDismiss()
            }
        }
    }

    when (state.step) {
        PostRideStep.Route -> RouteStep(
            state = state,
            onAction = viewModel::onAction,
            onOpenPointPicker = onOpenPointPicker,
            modifier = modifier,
        )

        PostRideStep.Review -> ReviewStep(
            state = state,
            onAction = viewModel::onAction,
            modifier = modifier,
        )

        PostRideStep.Live -> LiveStep(
            state = state,
            onGoToMyRides = onOpenMyRides,
            modifier = modifier,
        )
    }
}

@Composable
private fun StepProgress(label: String, fraction: Float, modifier: Modifier = Modifier) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
        Text(text = label, style = typography.captionStrong, color = colors.textSecondary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(HitchyTheme.shapes.pill)
                .background(colors.disabledSurface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(HitchyTheme.shapes.pill)
                    .background(colors.purple),
            )
        }
    }
}

@Composable
private fun RouteStep(
    state: PostRideState,
    onAction: (PostRideAction) -> Unit,
    onOpenPointPicker: (field: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing

    Surface(modifier = modifier.fillMaxSize(), color = colors.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = stringResource(Res.string.post_ride_route_title),
                onBackClick = { onAction(PostRideAction.Back) },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                StepProgress(label = stringResource(Res.string.post_ride_step_one), fraction = 0.5f)

                RouteInput(
                    pickupValue = state.from?.name?.en ?: "",
                    dropOffValue = state.to?.name?.en ?: "",
                    onPickupClick = { onOpenPointPicker(FIELD_POST_FROM) },
                    onDropOffClick = { onOpenPointPicker(FIELD_POST_TO) },
                )

                Column(verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
                    Text(
                        text = stringResource(Res.string.post_ride_leave_at),
                        style = typography.bodyStrong,
                        color = colors.navy,
                    )
                    HitchySelect(
                        items = departureTimes(),
                        selectedItem = state.timeHHmm,
                        onItemSelected = { onAction(PostRideAction.TimeChanged(it)) },
                        itemLabel = { it },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.post_ride_seats_title),
                            style = typography.bodyStrong,
                            color = colors.navy,
                        )
                        Text(
                            text = stringResource(Res.string.post_ride_seats_hint, state.maxSeats),
                            style = typography.caption,
                            color = colors.purple,
                        )
                    }
                    PassengersStepper(
                        count = state.seats,
                        onCountChange = { onAction(PostRideAction.SeatsChanged(it)) },
                        maxCount = state.maxSeats,
                    )
                }

                // Only a woman owner may offer a Women-only ride (R8); it moved here from D3.
                if (state.canOfferWomenOnly) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.post_ride_women_only),
                            style = typography.body,
                            color = colors.navy,
                        )
                        HitchySwitch(
                            checked = state.womenOnly,
                            onCheckedChange = { onAction(PostRideAction.WomenOnlyChanged(it)) },
                        )
                    }
                }

                state.error?.let { error ->
                    Text(text = stringResource(error.message()), style = typography.body, color = colors.error)
                }
            }

            Column(modifier = Modifier.padding(spacing.screenPadding)) {
                HitchyButton(
                    onClick = { onAction(PostRideAction.Continue) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canContinue,
                    loading = state.quoting,
                ) {
                    Text(text = stringResource(Res.string.post_ride_continue))
                }
            }
        }
    }
}

@Composable
private fun ReviewStep(
    state: PostRideState,
    onAction: (PostRideAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    val quote = state.quote ?: return

    Surface(modifier = modifier.fillMaxSize(), color = colors.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = stringResource(Res.string.post_ride_review_title),
                onBackClick = { onAction(PostRideAction.Back) },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                StepProgress(label = stringResource(Res.string.post_ride_step_two), fraction = 1f)

                Column(verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.post_ride_price_header),
                            style = typography.captionStrong,
                            color = colors.textSecondary,
                        )
                        if (state.fairPrice) {
                            Box(
                                modifier = Modifier
                                    .clip(HitchyTheme.shapes.pill)
                                    .background(colors.successBg)
                                    .padding(horizontal = spacing.space12, vertical = spacing.space4),
                            ) {
                                Text(
                                    text = stringResource(Res.string.post_ride_fair_price),
                                    style = typography.captionStrong,
                                    color = colors.successText,
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(
                            Res.string.post_ride_per_seat,
                            state.pricePerSeat,
                        ),
                        style = typography.h2,
                        color = colors.purple,
                    )

                    PriceSlider(
                        priceEgp = state.pricePerSeat,
                        onPriceChange = { onAction(PostRideAction.PriceChanged(it)) },
                        minPriceEgp = quote.min,
                        maxPriceEgp = quote.max,
                        fairPriceEgp = quote.recommended,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        PriceLabel(stringResource(Res.string.post_ride_price_min), quote.min)
                        PriceLabel(stringResource(Res.string.post_ride_price_recommended), quote.recommended)
                        PriceLabel(stringResource(Res.string.post_ride_price_max), quote.max)
                    }
                }

                // The server's own numbers, so the owner sees where the price comes from (R6).
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
                    BreakdownRow(
                        stringResource(Res.string.post_ride_price_breakdown_fuel),
                        quote.fuelCostEgp,
                    )
                    BreakdownRow(
                        stringResource(Res.string.post_ride_price_breakdown_running),
                        quote.tripCostEgp - quote.fuelCostEgp,
                    )
                    BreakdownRow(
                        stringResource(Res.string.post_ride_price_breakdown_trip, state.seats),
                        quote.tripCostEgp,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
                    Text(
                        text = stringResource(Res.string.post_ride_summary_title),
                        style = typography.captionStrong,
                        color = colors.textSecondary,
                    )
                    RouteTimeline(
                        pickupTime = state.timeHHmm,
                        pickupLocation = state.from?.name?.en ?: "",
                        dropOffTime = UNKNOWN_TIME,
                        dropOffLocation = state.to?.name?.en ?: "",
                    )
                    Text(
                        text = stringResource(Res.string.post_ride_seats_open, state.seats),
                        style = typography.captionStrong,
                        color = colors.textSecondary,
                    )
                }

                state.error?.let { error ->
                    Text(text = stringResource(error.message()), style = typography.body, color = colors.error)
                }
            }

            Column(modifier = Modifier.padding(spacing.screenPadding)) {
                HitchyButton(
                    onClick = { onAction(PostRideAction.Publish) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canPublish,
                    loading = state.publishing,
                ) {
                    Text(text = stringResource(Res.string.post_ride_publish))
                }
            }
        }
    }
}

@Composable
private fun PriceLabel(label: String, amountEgp: Int) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "EGP $amountEgp",
            style = typography.bodyStrong,
            color = colors.navy,
        )
        Text(text = label, style = typography.caption, color = colors.textSecondary)
    }
}

@Composable
private fun BreakdownRow(label: String, amountEgp: Int) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = typography.body, color = colors.textSecondary)
        Text(text = "EGP $amountEgp", style = typography.bodyStrong, color = colors.navy)
    }
}

@Composable
private fun LiveStep(
    state: PostRideState,
    onGoToMyRides: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing

    Surface(modifier = modifier.fillMaxSize(), color = colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.screenPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.padding(top = spacing.space32),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(colors.successBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "✓", style = typography.display, color = colors.successText)
                }
                Text(
                    text = stringResource(Res.string.post_ride_live_title),
                    style = typography.h1,
                    color = colors.navy,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.post_ride_live_body),
                    style = typography.body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                RouteTimeline(
                    pickupTime = state.timeHHmm,
                    pickupLocation = state.from?.name?.en ?: "",
                    dropOffTime = UNKNOWN_TIME,
                    dropOffLocation = state.to?.name?.en ?: "",
                )
                Text(
                    text = stringResource(Res.string.post_ride_seats_open, state.seats),
                    style = typography.captionStrong,
                    color = colors.textSecondary,
                )
            }

            HitchyButton(
                onClick = onGoToMyRides,
                modifier = Modifier.fillMaxWidth(),
                variant = HitchyButtonVariant.Primary,
            ) {
                Text(text = stringResource(Res.string.post_ride_live_go_to_rides))
            }
        }
    }
}

/** Departure times in 5-minute steps, as P6-A02 specifies. */
private fun departureTimes(): List<String> = buildList {
    for (hour in 0..23) {
        for (minute in 0 until MINUTES_PER_HOUR step STEP_MINUTES) {
            add("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
        }
    }
}
