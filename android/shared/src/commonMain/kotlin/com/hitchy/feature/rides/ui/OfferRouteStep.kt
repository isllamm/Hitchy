package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.booking.ui.DateSheet
import com.hitchy.feature.booking.ui.bookingDateLabel
import com.hitchy.feature.rides.presentation.PostRideAction
import com.hitchy.feature.rides.presentation.PostRideState
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.offer_custom_date
import hitchy.shared.generated.resources.offer_from
import hitchy.shared.generated.resources.offer_from_placeholder
import hitchy.shared.generated.resources.offer_leave_at
import hitchy.shared.generated.resources.offer_seats_max
import hitchy.shared.generated.resources.offer_seats_title
import hitchy.shared.generated.resources.offer_stop
import hitchy.shared.generated.resources.offer_stop_placeholder
import hitchy.shared.generated.resources.offer_to
import hitchy.shared.generated.resources.offer_to_placeholder
import hitchy.shared.generated.resources.post_ride_add_stop
import hitchy.shared.generated.resources.post_ride_stop_remove
import hitchy.shared.generated.resources.post_ride_vehicle_label
import hitchy.shared.generated.resources.post_ride_women_only
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val RowShape = RoundedCornerShape(16.dp)

/** Figma's location rows: a 2% black shadow under a #E9EEF5 stroke. */
private const val ROW_SHADOW_ALPHA = 0.02f

/**
 * D2 "Post your commute" (Figma DEMO 726:11276): the route, the departure, the seats. Figma's weekly
 * repeat, return trip and arrival line are not in the demo, so they are left out (ADR 0003).
 */
@Composable
internal fun OfferRouteStep(
    state: PostRideState,
    language: AppLanguage,
    onOpenRoutePicker: (String) -> Unit,
    onAction: (PostRideAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LocationSelector(state, language, onOpenRoutePicker, onAction)
        DepartureCard(state, onAction)
        SeatsCard(state, onAction)
    }
}

/** Figma's `Location Selector`: a bordered card (padding 16, gap 12) holding the From and To rows. */
@Composable
private fun LocationSelector(
    state: PostRideState,
    language: AppLanguage,
    onOpenRoutePicker: (String) -> Unit,
    onAction: (PostRideAction) -> Unit,
) {
    val colors = HitchyTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OfferCardShape)
            .background(colors.white)
            .border(1.dp, colors.border, OfferCardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LocationRow(
            label = stringResource(Res.string.offer_from),
            value = state.fromPoint?.name?.resolve(language),
            placeholder = stringResource(Res.string.offer_from_placeholder),
            marker = { Box(Modifier.size(12.dp).clip(CircleShape).background(colors.royal)) },
            markerBackground = colors.lavender,
            onClick = { onOpenRoutePicker(PostRideState.FIELD_FROM) },
        )
        state.stops.forEach { stop ->
            LocationRow(
                label = stringResource(Res.string.offer_stop, stop.index + 1),
                value = stop.point?.name?.resolve(language),
                placeholder = stringResource(Res.string.offer_stop_placeholder),
                marker = { Box(Modifier.size(8.dp).clip(CircleShape).background(colors.purple)) },
                markerBackground = colors.lilac,
                onClick = { onOpenRoutePicker("${PostRideState.FIELD_STOP_PREFIX}${stop.index}") },
                trailing = HitchyIcons.CloseSmall,
                trailingDescription = stringResource(Res.string.post_ride_stop_remove),
                onTrailing = { onAction(PostRideAction.RemoveStop(stop.index)) },
            )
        }
        LocationRow(
            label = stringResource(Res.string.offer_to),
            value = state.toPoint?.name?.resolve(language),
            placeholder = stringResource(Res.string.offer_to_placeholder),
            marker = {
                Icon(
                    painter = painterResource(HitchyIcons.Destination),
                    contentDescription = null,
                    tint = colors.pin,
                    modifier = Modifier.size(14.dp),
                )
            },
            markerBackground = colors.pinSoft,
            onClick = { onOpenRoutePicker(PostRideState.FIELD_TO) },
        )
        if (state.stops.size < PostRideState.MAX_STOPS) {
            Text(
                text = stringResource(Res.string.post_ride_add_stop),
                style = HitchyTheme.typography.bodyStrong,
                color = colors.purple,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAction(PostRideAction.AddStop) }
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            )
        }
    }
}

/**
 * Figma's `From` / `Destination` row: r16, #E9EEF5 stroke, padding 10/14, a 32dp r12 marker box,
 * a 14/22 gray label over the 600 14/22 place (or the slate placeholder), and a small chevron.
 */
@Composable
private fun LocationRow(
    label: String,
    value: String?,
    placeholder: String,
    marker: @Composable () -> Unit,
    markerBackground: Color,
    onClick: () -> Unit,
    trailing: DrawableResource = HitchyIcons.ChevronRightSmall,
    trailingDescription: String? = null,
    onTrailing: (() -> Unit)? = null,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 61.dp)
            .figmaShadow(RowShape, Color.Black.copy(alpha = ROW_SHADOW_ALPHA), blur = 4.dp, y = 2.dp)
            .clip(RowShape)
            .background(colors.white)
            .border(1.dp, colors.hairline, RowShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(12.dp)).background(markerBackground),
            contentAlignment = Alignment.Center,
        ) { marker() }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(text = label, style = typography.body, color = colors.textSecondary)
            if (value.isNullOrBlank()) {
                Text(text = placeholder, style = typography.body, color = colors.slate, maxLines = 1)
            } else {
                Text(
                    text = value,
                    style = typography.bodyStrong,
                    color = colors.navy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            painter = painterResource(trailing),
            contentDescription = trailingDescription,
            tint = colors.textSecondary,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(if (onTrailing != null) 20.dp else 14.dp)
                .then(if (onTrailing != null) Modifier.clip(CircleShape).clickable(onClick = onTrailing) else Modifier),
        )
    }
}

/**
 * Figma's `Departure` card: the clock header "Leave home at" and the time wheel. Figma draws no day, so Today,
 * Tomorrow and Custom pills sit above the wheel (ADR 0003); Custom opens the rider's "Select date" sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepartureCard(state: PostRideState, onAction: (PostRideAction) -> Unit) {
    var picking by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf(state.selectedDateKey) }
    val custom = state.selectedDateKey != state.todayDateKey && state.selectedDateKey != state.tomorrowDateKey
    Column(
        modifier = Modifier.fillMaxWidth().offerCard().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CardHeader(icon = HitchyIcons.ClockSmall, title = stringResource(Res.string.offer_leave_at))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            DayPill(
                text = stringResource(Res.string.rider_home_date_today),
                selected = state.selectedDateKey == state.todayDateKey,
                onClick = { onAction(PostRideAction.SelectDate(state.todayDateKey)) },
            )
            DayPill(
                text = stringResource(Res.string.rider_home_date_tomorrow),
                selected = state.selectedDateKey == state.tomorrowDateKey,
                onClick = { onAction(PostRideAction.SelectDate(state.tomorrowDateKey)) },
            )
            DayPill(
                text = if (custom) {
                    bookingDateLabel(state.selectedDateKey)
                } else {
                    stringResource(Res.string.offer_custom_date)
                },
                selected = custom,
                onClick = {
                    pending = state.selectedDateKey
                    picking = true
                },
            )
        }
        OfferTimeWheel(minutesOfDay = state.departTimeMinutes) { onAction(PostRideAction.SelectTime(it)) }
    }
    if (picking && state.todayDateKey.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { picking = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RectangleShape,
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            scrimColor = HitchyTheme.colors.navy.copy(alpha = SHEET_SCRIM_ALPHA),
            dragHandle = null,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            DateSheet(
                todayKey = state.todayDateKey,
                tomorrowKey = state.tomorrowDateKey,
                selectedKey = pending,
                onPick = { pending = it },
                onConfirm = {
                    onAction(PostRideAction.SelectDate(pending))
                    picking = false
                },
                onClose = { picking = false },
                showDayChips = false,
            )
        }
    }
}

private const val SHEET_SCRIM_ALPHA = 0.45f

/** Figma's `Day` circle as a 40dp pill: purple with white Bold 12/18 when on, #F8F9FC with a stroke when off. */
@Composable
private fun DayPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(if (selected) colors.purple else colors.background)
            .then(if (selected) Modifier else Modifier.border(1.dp, colors.border, CircleShape))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = HitchyTheme.typography.captionStrong,
            color = if (selected) colors.white else colors.textSecondary,
        )
    }
}

@Composable
private fun CardHeader(icon: DrawableResource, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = HitchyTheme.colors.purple,
            modifier = Modifier.size(20.dp),
        )
        Text(text = title, style = HitchyTheme.typography.bodyLargeStrong, color = HitchyTheme.colors.navy)
    }
}

/**
 * Figma's `Seats` card: "Seats you offer" over the purple max line, and the stepper (44dp circles, the
 * count in Bold 20/28). The plus turns #F3F4F6 at the maximum. The vehicle (when the owner has more
 * than one) and the women-only toggle sit under it, in Figma's Toggle style.
 */
@Composable
private fun SeatsCard(state: PostRideState, onAction: (PostRideAction) -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(
        modifier = Modifier.fillMaxWidth().offerCard().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(Res.string.offer_seats_title),
                    style = typography.bodyLargeStrong,
                    color = colors.navy,
                )
                Text(
                    text = stringResource(Res.string.offer_seats_max, state.maxSeats),
                    style = typography.caption,
                    color = colors.purple,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StepperButton(HitchyIcons.Minus, enabled = state.seats > 1) {
                    onAction(PostRideAction.UpdateSeats(state.seats - 1))
                }
                Text(text = state.seats.toString(), style = typography.h3, color = colors.navy)
                StepperButton(HitchyIcons.Plus, enabled = state.seats < state.maxSeats) {
                    onAction(PostRideAction.UpdateSeats(state.seats + 1))
                }
            }
        }
        if (state.vehicles.size > 1) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            Text(
                text = stringResource(Res.string.post_ride_vehicle_label),
                style = typography.bodyLargeStrong,
                color = colors.navy,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.vehicles.forEach { vehicle ->
                    DayPill(
                        text = "${vehicle.make} ${vehicle.model}",
                        selected = vehicle.id == state.selectedVehicleId,
                        onClick = { onAction(PostRideAction.SelectVehicle(vehicle.id)) },
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        OfferToggle(
            label = stringResource(Res.string.post_ride_women_only),
            checked = state.womenOnly,
            onCheckedChange = { onAction(PostRideAction.ToggleWomenOnly(it)) },
        )
    }
}

@Composable
private fun StepperButton(icon: DrawableResource, enabled: Boolean, onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (enabled) colors.white else colors.disabledSurface)
            .then(if (enabled) Modifier.border(1.dp, colors.border, CircleShape) else Modifier)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (enabled) colors.navy else colors.textDisabled,
            modifier = Modifier.size(20.dp),
        )
    }
}
