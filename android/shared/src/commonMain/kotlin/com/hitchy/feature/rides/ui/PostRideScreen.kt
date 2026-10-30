@file:Suppress("MagicNumber", "TooManyFunctions")

package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.ErrorText
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.ErrorState
import com.hitchy.core.ui.components.FilterChip
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.HitchySwitch
import com.hitchy.core.ui.components.LocationField
import com.hitchy.core.ui.components.LocationPointType
import com.hitchy.core.ui.components.PassengersStepper
import com.hitchy.core.ui.components.PriceSlider
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.rides.presentation.PostRideAction
import com.hitchy.feature.rides.presentation.PostRideEvent
import com.hitchy.feature.rides.presentation.PostRideState
import com.hitchy.feature.rides.presentation.PostRideViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.format_egp
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
import hitchy.shared.generated.resources.post_ride_time_earlier
import hitchy.shared.generated.resources.post_ride_time_label
import hitchy.shared.generated.resources.post_ride_time_later
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

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PostRideEvent.NavigateToRideDetails -> onNavigateToRideDetails(event.rideId)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(colors.white).statusBarsPadding()) {
        PostRideHeader(onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            postRideRouteItems(state, language, onOpenRoutePicker, viewModel::onAction)
            postRideWhenItems(state, viewModel::onAction)
            postRideOptionItems(state, viewModel::onAction)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.white)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PostRideErrors(state, viewModel::onAction)
            OwnerGradientButton(
                text = stringResource(Res.string.post_ride_submit),
                onClick = { viewModel.onAction(PostRideAction.Submit) },
                loading = state.isSubmitting,
            )
        }
    }
}

@Composable
private fun PostRideHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BackButton(onClick = onBack, style = BackButtonStyle.OutlinedCircle)
        Text(
            text = stringResource(Res.string.post_ride_title),
            style = HitchyTheme.typography.h1,
            color = HitchyTheme.colors.navy,
        )
    }
}

private fun LazyListScope.postRideRouteItems(
    state: PostRideState,
    language: AppLanguage,
    onOpenRoutePicker: (String) -> Unit,
    onAction: (PostRideAction) -> Unit,
) {
    item {
        val activeVehicle = state.vehicles.firstOrNull { it.id == state.selectedVehicleId }
            ?: state.vehicles.firstOrNull()
        OwnerSectionCard(icon = HitchyIcons.Car, title = stringResource(Res.string.post_ride_vehicle_label)) {
            if (activeVehicle != null) {
                Text(
                    text = "${activeVehicle.make} ${activeVehicle.model} · ${activeVehicle.color} " +
                        "(${activeVehicle.plate})",
                    style = HitchyTheme.typography.body,
                    color = HitchyTheme.colors.navy,
                )
            }
        }
    }
    item {
        OwnerSectionCard(
            icon = HitchyIcons.LocationDot,
            title = stringResource(Res.string.post_ride_from_label),
            trailing = {
                IconButton(onClick = { onAction(PostRideAction.SwapRoute) }) {
                    Icon(
                        painter = painterResource(HitchyIcons.Swap),
                        contentDescription = null,
                        tint = HitchyTheme.colors.purple,
                    )
                }
            },
        ) {
            LocationField(
                value = state.fromPoint?.name?.resolve(language).orEmpty(),
                label = stringResource(Res.string.post_ride_from_label),
                type = LocationPointType.Pickup,
                onClick = { onOpenRoutePicker(PostRideState.FIELD_FROM) },
            )
        }
    }
    item {
        OwnerSectionCard(icon = HitchyIcons.Pin, title = stringResource(Res.string.post_ride_to_label)) {
            LocationField(
                value = state.toPoint?.name?.resolve(language).orEmpty(),
                label = stringResource(Res.string.post_ride_to_label),
                type = LocationPointType.DropOff,
                onClick = { onOpenRoutePicker(PostRideState.FIELD_TO) },
            )
        }
    }
    item { StopsCard(state, language, onOpenRoutePicker, onAction) }
}

@Composable
private fun StopsCard(
    state: PostRideState,
    language: AppLanguage,
    onOpenRoutePicker: (String) -> Unit,
    onAction: (PostRideAction) -> Unit,
) {
    OwnerSectionCard(icon = HitchyIcons.Repeat, title = stringResource(Res.string.post_ride_stops_title)) {
        state.stops.forEach { stop ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LocationField(
                        value = stop.point?.name?.resolve(language).orEmpty(),
                        label = stringResource(Res.string.post_ride_stop_label, stop.index + 1),
                        type = LocationPointType.Pickup,
                        onClick = { onOpenRoutePicker("${PostRideState.FIELD_STOP_PREFIX}${stop.index}") },
                    )
                }
                IconButton(onClick = { onAction(PostRideAction.RemoveStop(stop.index)) }) {
                    Icon(
                        painter = painterResource(HitchyIcons.Close),
                        contentDescription = stringResource(Res.string.post_ride_stop_remove),
                        tint = HitchyTheme.colors.error,
                    )
                }
            }
        }
        if (state.stops.size < PostRideState.MAX_STOPS) {
            HitchyButton(
                onClick = { onAction(PostRideAction.AddStop) },
                variant = HitchyButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.post_ride_add_stop))
            }
        }
    }
}

private fun LazyListScope.postRideWhenItems(
    state: PostRideState,
    onAction: (PostRideAction) -> Unit,
) {
    item {
        OwnerSectionCard(icon = HitchyIcons.Calendar, title = stringResource(Res.string.post_ride_date_label)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.selectedDateKey == state.todayDateKey,
                    label = stringResource(Res.string.rider_home_date_today),
                    onClick = { onAction(PostRideAction.SelectDate(state.todayDateKey)) },
                )
                FilterChip(
                    selected = state.selectedDateKey == state.tomorrowDateKey,
                    label = stringResource(Res.string.rider_home_date_tomorrow),
                    onClick = { onAction(PostRideAction.SelectDate(state.tomorrowDateKey)) },
                )
            }
        }
    }
    item { TimeCard(state, onAction) }
}

@Composable
private fun TimeCard(
    state: PostRideState,
    onAction: (PostRideAction) -> Unit,
) {
    OwnerSectionCard(
        icon = HitchyIcons.Clock,
        title = stringResource(Res.string.post_ride_time_label),
        trailing = {
            Text(text = state.departTimeFormatted, style = HitchyTheme.typography.h2, color = HitchyTheme.colors.purple)
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = false,
                label = stringResource(Res.string.post_ride_time_earlier),
                onClick = {
                    val prev = (state.departTimeMinutes - MINUTES_STEP).coerceAtLeast(0)
                    onAction(PostRideAction.SelectTime(prev))
                },
            )
            FilterChip(
                selected = false,
                label = stringResource(Res.string.post_ride_time_later),
                onClick = {
                    val next = (state.departTimeMinutes + MINUTES_STEP).coerceAtMost(MAX_DAY_MINUTES)
                    onAction(PostRideAction.SelectTime(next))
                },
            )
        }
    }
}

private fun LazyListScope.postRideOptionItems(
    state: PostRideState,
    onAction: (PostRideAction) -> Unit,
) {
    item {
        OwnerSectionCard(
            icon = HitchyIcons.Person,
            title = stringResource(Res.string.post_ride_seats_label),
            trailing = {
                PassengersStepper(
                    count = state.seats,
                    onCountChange = { onAction(PostRideAction.UpdateSeats(it)) },
                    maxCount = state.maxSeats,
                )
            },
        )
    }
    item { PriceCard(state, onAction) }
    item {
        OwnerSectionCard(
            icon = HitchyIcons.ShieldCheck,
            title = stringResource(Res.string.post_ride_women_only),
            trailing = {
                HitchySwitch(
                    checked = state.womenOnly,
                    onCheckedChange = { onAction(PostRideAction.ToggleWomenOnly(it)) },
                )
            },
        )
    }
}

@Composable
private fun PriceCard(
    state: PostRideState,
    onAction: (PostRideAction) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    OwnerSectionCard(icon = HitchyIcons.Star, title = stringResource(Res.string.post_ride_price_title)) {
        if (state.isQuoting) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.purple)
                Text(
                    text = stringResource(Res.string.post_ride_quoting),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
        val quote = state.quote
        if (quote != null) {
            PriceSlider(
                priceEgp = state.pricePerSeat,
                onPriceChange = { onAction(PostRideAction.UpdatePrice(it)) },
                minPriceEgp = quote.min,
                maxPriceEgp = quote.max,
                fairPriceEgp = quote.recommended,
            )
        } else {
            Text(
                text = stringResource(Res.string.format_egp, state.pricePerSeat.toString()),
                style = typography.h2,
                color = colors.purple,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(Res.string.post_ride_expected_earnings),
                style = typography.body,
                color = colors.textSecondary,
            )
            Text(
                text = stringResource(Res.string.format_egp, state.expectedEarnings.toString()),
                style = typography.bodyStrong,
                color = colors.purple,
            )
        }
    }
}

@Composable
private fun PostRideErrors(
    state: PostRideState,
    onAction: (PostRideAction) -> Unit,
) {
    val validationError = state.validationError
    if (validationError != null) {
        HitchyBanner(
            text = stringResource(validationError),
            variant = BannerVariant.Error,
            onDismiss = { onAction(PostRideAction.DismissError) },
        )
    }
    val error = state.error
    if (error != null) {
        ErrorText(error = error, style = HitchyTheme.typography.caption, color = HitchyTheme.colors.error)
    }
}
