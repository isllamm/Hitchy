package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.ErrorText
import com.hitchy.core.ui.components.ErrorState
import com.hitchy.core.ui.components.FilterChip
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.HitchySwitch
import com.hitchy.core.ui.components.LocationField
import com.hitchy.core.ui.components.LocationPointType
import com.hitchy.core.ui.components.PassengersStepper
import com.hitchy.core.ui.components.PriceSlider
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.rides.presentation.PostRideAction
import com.hitchy.feature.rides.presentation.PostRideEvent
import com.hitchy.feature.rides.presentation.PostRideState
import com.hitchy.feature.rides.presentation.PostRideViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.post_ride_add_stop
import hitchy.shared.generated.resources.post_ride_date_label
import hitchy.shared.generated.resources.post_ride_expected_earnings
import hitchy.shared.generated.resources.post_ride_from_label
import hitchy.shared.generated.resources.post_ride_price_title
import hitchy.shared.generated.resources.post_ride_quoting
import hitchy.shared.generated.resources.post_ride_seats_label
import hitchy.shared.generated.resources.post_ride_stop_label
import hitchy.shared.generated.resources.post_ride_stop_remove
import hitchy.shared.generated.resources.post_ride_stops_title
import hitchy.shared.generated.resources.post_ride_submit
import hitchy.shared.generated.resources.post_ride_time_label
import hitchy.shared.generated.resources.post_ride_title
import hitchy.shared.generated.resources.post_ride_to_label
import hitchy.shared.generated.resources.post_ride_vehicle_label
import hitchy.shared.generated.resources.post_ride_women_only
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val MINUTES_STEP = 15
private const val MAX_DAY_MINUTES = 1425

@Composable
fun PostRideScreen(
    onBack: () -> Unit,
    onOpenRoutePicker: (String) -> Unit,
    onNavigateToRideDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostRideViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val languageSwitcher: LanguageSwitcher = koinInject()
    val language = languageSwitcher.getCurrentLanguage()
    val colors = HitchyTheme.colors
    val spacing = HitchyTheme.spacing
    val typography = HitchyTheme.typography
    val cardShape = RoundedCornerShape(16.dp)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PostRideEvent.NavigateToRideDetails -> onNavigateToRideDetails(event.rideId)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        TopBar(
            title = stringResource(Res.string.post_ride_title),
            onBackClick = onBack,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space16),
        ) {
            item { Spacer(modifier = Modifier.height(spacing.space8)) }

            // Route Card
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
                        LocationField(
                            value = state.fromPoint?.name?.resolve(language).orEmpty(),
                            label = stringResource(Res.string.post_ride_from_label),
                            type = LocationPointType.Pickup,
                            onClick = { onOpenRoutePicker(PostRideState.FIELD_FROM) },
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            IconButton(onClick = { viewModel.onAction(PostRideAction.SwapRoute) }) {
                                Icon(
                                    painter = painterResource(HitchyIcons.Swap),
                                    contentDescription = null,
                                    tint = colors.purple,
                                )
                            }
                        }

                        LocationField(
                            value = state.toPoint?.name?.resolve(language).orEmpty(),
                            label = stringResource(Res.string.post_ride_to_label),
                            type = LocationPointType.DropOff,
                            onClick = { onOpenRoutePicker(PostRideState.FIELD_TO) },
                        )
                    }
                }
            }

            // Stops Section
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
                            text = stringResource(Res.string.post_ride_stops_title),
                            style = typography.title,
                            color = colors.textPrimary,
                        )

                        state.stops.forEach { stop ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    val stopLabel = stringResource(Res.string.post_ride_stop_label, stop.index + 1)
                                    LocationField(
                                        value = stop.point?.name?.resolve(language).orEmpty(),
                                        label = stopLabel,
                                        type = LocationPointType.Pickup,
                                        onClick = {
                                            onOpenRoutePicker("${PostRideState.FIELD_STOP_PREFIX}${stop.index}")
                                        },
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.onAction(PostRideAction.RemoveStop(stop.index)) },
                                ) {
                                    Icon(
                                        painter = painterResource(HitchyIcons.Close),
                                        contentDescription = stringResource(Res.string.post_ride_stop_remove),
                                        tint = colors.error,
                                    )
                                }
                            }
                        }

                        if (state.stops.size < PostRideState.MAX_STOPS) {
                            HitchyButton(
                                onClick = { viewModel.onAction(PostRideAction.AddStop) },
                                variant = HitchyButtonVariant.Outline,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = stringResource(Res.string.post_ride_add_stop))
                            }
                        }
                    }
                }
            }

            // Date & Time
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
                            text = stringResource(Res.string.post_ride_date_label),
                            style = typography.title,
                            color = colors.textPrimary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space8)) {
                            FilterChip(
                                selected = state.selectedDateKey == state.todayDateKey,
                                label = stringResource(Res.string.rider_home_date_today),
                                onClick = { viewModel.onAction(PostRideAction.SelectDate(state.todayDateKey)) },
                            )
                            FilterChip(
                                selected = state.selectedDateKey == state.tomorrowDateKey,
                                label = stringResource(Res.string.rider_home_date_tomorrow),
                                onClick = { viewModel.onAction(PostRideAction.SelectDate(state.tomorrowDateKey)) },
                            )
                        }

                        Text(
                            text = stringResource(Res.string.post_ride_time_label),
                            style = typography.title,
                            color = colors.textPrimary,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = state.departTimeFormatted,
                                style = typography.h2,
                                color = colors.purple,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.space8)) {
                                FilterChip(
                                    selected = false,
                                    label = "-15m",
                                    onClick = {
                                        val prev = (state.departTimeMinutes - MINUTES_STEP).coerceAtLeast(0)
                                        viewModel.onAction(PostRideAction.SelectTime(prev))
                                    },
                                )
                                FilterChip(
                                    selected = false,
                                    label = "+15m",
                                    onClick = {
                                        val stepped = state.departTimeMinutes + MINUTES_STEP
                                        val next = stepped.coerceAtMost(MAX_DAY_MINUTES)
                                        viewModel.onAction(PostRideAction.SelectTime(next))
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Seats Stepper
            item {
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(Res.string.post_ride_seats_label),
                            style = typography.title,
                            color = colors.textPrimary,
                        )
                        PassengersStepper(
                            count = state.seats,
                            onCountChange = { viewModel.onAction(PostRideAction.UpdateSeats(it)) },
                            maxCount = state.maxSeats,
                        )
                    }
                }
            }

            // Vehicle & Women Only
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
                        val activeVeh = state.vehicles.firstOrNull { it.id == state.selectedVehicleId }
                            ?: state.vehicles.firstOrNull()
                        if (activeVeh != null) {
                            Text(
                                text = stringResource(Res.string.post_ride_vehicle_label),
                                style = typography.caption,
                                color = colors.textSecondary,
                            )
                            Text(
                                text = "${activeVeh.make} ${activeVeh.model} · ${activeVeh.color} (${activeVeh.plate})",
                                style = typography.body,
                                color = colors.textPrimary,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(Res.string.post_ride_women_only),
                                style = typography.title,
                                color = colors.textPrimary,
                            )
                            HitchySwitch(
                                checked = state.womenOnly,
                                onCheckedChange = { viewModel.onAction(PostRideAction.ToggleWomenOnly(it)) },
                            )
                        }
                    }
                }
            }

            // Price Slider & Quoting
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
                            text = stringResource(Res.string.post_ride_price_title),
                            style = typography.title,
                            color = colors.textPrimary,
                        )

                        if (state.isQuoting) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.purple,
                                )
                                Text(
                                    text = stringResource(Res.string.post_ride_quoting),
                                    style = typography.caption,
                                    color = colors.textSecondary,
                                )
                            }
                        }

                        val q = state.quote
                        if (q != null) {
                            PriceSlider(
                                priceEgp = state.pricePerSeat,
                                onPriceChange = { viewModel.onAction(PostRideAction.UpdatePrice(it)) },
                                minPriceEgp = q.min,
                                maxPriceEgp = q.max,
                                fairPriceEgp = q.recommended,
                            )
                        } else {
                            Text(
                                text = "EGP ${state.pricePerSeat}",
                                style = typography.h2,
                                color = colors.purple,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(Res.string.post_ride_expected_earnings),
                                style = typography.body,
                                color = colors.textSecondary,
                            )
                            Text(
                                text = "EGP ${state.expectedEarnings}",
                                style = typography.title,
                                color = colors.purple,
                            )
                        }
                    }
                }
            }

            // Error display
            val validationError = state.validationError
            if (validationError != null) {
                item {
                    ErrorState(
                        message = stringResource(validationError),
                        onRetry = { viewModel.onAction(PostRideAction.DismissError) },
                    )
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

            // Submit Button
            item {
                HitchyButton(
                    onClick = { viewModel.onAction(PostRideAction.Submit) },
                    loading = state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.post_ride_submit))
                }
                Spacer(modifier = Modifier.height(spacing.space32))
            }
        }
    }
}
