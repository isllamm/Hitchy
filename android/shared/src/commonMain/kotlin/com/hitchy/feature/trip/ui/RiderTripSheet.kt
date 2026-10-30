@file:Suppress("MagicNumber")

package com.hitchy.feature.trip.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.StatusChip
import com.hitchy.core.ui.components.TripStep
import com.hitchy.core.ui.components.TripStepper
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.isoToCairoTime
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.trip.presentation.RiderTripAction
import com.hitchy.feature.trip.presentation.RiderTripState
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.rider_trip_cancel_button
import hitchy.shared.generated.resources.rider_trip_checked_in_badge
import hitchy.shared.generated.resources.rider_trip_checkin_button
import hitchy.shared.generated.resources.rider_trip_waiting_payment
import org.jetbrains.compose.resources.stringResource

private val SheetRadius = 28.dp

@Composable
internal fun TripSheet(
    state: RiderTripState,
    language: AppLanguage,
    onAction: (RiderTripAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val booking = state.booking
    val ride = state.ride

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius))
            .background(colors.white)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 4.dp)
                .clip(CircleShape)
                .background(colors.border),
        )
        val error = state.error
        if (error != null) {
            HitchyBanner(
                text = stringResource(error.message()),
                variant = BannerVariant.Error,
                onDismiss = { onAction(RiderTripAction.DismissError) },
            )
        }
        if (booking != null) {
            TripStatusRow(state = state, booking = booking)
        }
        if (!state.isCancelled) {
            TripStepper(currentStep = state.tripStep, modifier = Modifier.fillMaxWidth())
        }
        if (ride != null && booking != null) {
            TripRouteCard(
                pickupName = booking.pickupPointName.resolve(language),
                dropOffName = ride.toPointName.resolve(language),
            )
            TripCarCard(vehicle = ride.vehicle)
            TripSeatCard(seats = booking.seats, total = booking.total)
        }
        if (booking != null) {
            TripActions(state = state, booking = booking, onAction = onAction)
        }
        if (state.canCancel) {
            Text(
                text = stringResource(Res.string.rider_trip_cancel_button),
                style = HitchyTheme.typography.bodyStrong,
                color = colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAction(RiderTripAction.ShowCancelSheet(true)) }
                    .padding(vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun TripStatusRow(
    state: RiderTripState,
    booking: Booking,
) {
    val variant = when {
        state.isCancelled -> RideStatusVariant.Cancelled
        state.tripStep == TripStep.Completed -> RideStatusVariant.Completed
        state.tripStep == TripStep.InRide -> RideStatusVariant.InProgress
        else -> RideStatusVariant.Confirmed
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusChip(status = variant)
        Text(
            text = isoToCairoTime(booking.pickupAt),
            style = HitchyTheme.typography.caption,
            color = HitchyTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun TripActions(
    state: RiderTripState,
    booking: Booking,
    onAction: (RiderTripAction) -> Unit,
) {
    if (!state.isCancelled) {
        if (state.isCheckedIn) {
            HitchyBanner(
                text = stringResource(Res.string.rider_trip_checked_in_badge),
                variant = BannerVariant.Success,
            )
        } else if (state.canCheckIn) {
            HitchyButton(
                onClick = { onAction(RiderTripAction.CheckIn) },
                loading = state.isCheckingIn,
                enabled = !state.isCheckingIn,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.rider_trip_checkin_button))
            }
        }
    }
    if (booking.paymentConfirmedAt != null) {
        TripPaymentCard(total = booking.total, walletBalance = state.walletBalanceEgp)
    } else if (state.tripStep == TripStep.Completed) {
        HitchyBanner(
            text = stringResource(Res.string.rider_trip_waiting_payment),
            variant = BannerVariant.Info,
        )
    }
}
