@file:Suppress("MagicNumber")

package com.hitchy.feature.trip.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.SkeletonDetails
import com.hitchy.core.ui.components.StatusPill
import com.hitchy.core.ui.components.TripStep
import com.hitchy.core.ui.components.TripStepper
import com.hitchy.core.ui.figmaBottom
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.dateKey
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.feature.booking.ui.bookingDateLabel
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.trip.presentation.RiderTripAction
import com.hitchy.feature.trip.presentation.RiderTripState
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import hitchy.shared.generated.resources.rider_trip_cancel_button
import hitchy.shared.generated.resources.rider_trip_checked_in_badge
import hitchy.shared.generated.resources.rider_trip_checkin_button
import hitchy.shared.generated.resources.rider_trip_waiting_payment
import hitchy.shared.generated.resources.trip_design_schedule
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private val SheetRadius = 24.dp

@Composable
internal fun TripSheet(
    state: RiderTripState,
    language: AppLanguage,
    onAction: (RiderTripAction) -> Unit,
    modifier: Modifier = Modifier,
    profileRepository: ProfileRepository = koinInject(),
) {
    val colors = HitchyTheme.colors
    val booking = state.booking
    val ride = state.ride
    val hostFlow = remember(ride?.hostId) { ride?.hostId?.let { profileRepository.observePublicProfile(it) } }
    val host by (hostFlow ?: flowOf(null)).collectAsStateWithLifecycle(initialValue = null)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius))
            .background(colors.white)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = figmaBottom(42.dp)),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
        if ((ride == null || booking == null) && error == null) {
            // The ride and the booking have not both answered yet.
            SkeletonDetails(showMap = false)
        }
        if (booking != null) {
            TripStatusRow(state = state, booking = booking)
        }
        if (ride != null && booking != null) {
            TripRouteCard(
                pickupName = booking.pickupPointName.resolve(language),
                dropOffName = ride.toPointName.resolve(language),
            )
            TripHostCard(host = host, vehicle = ride.vehicle)
            TripSeatCard(seats = booking.seats, total = booking.total)
        }
        if (booking != null) {
            TripActions(state = state, booking = booking, onAction = onAction)
        }
        if (state.canCancel) {
            // Figma (demo 676:9909): a 52dp text button, Bold 16/24 red, 24dp under the cards.
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAction(RiderTripAction.ShowCancelSheet(true)) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.rider_trip_cancel_button),
                    style = HitchyTheme.typography.button,
                    color = colors.error,
                )
            }
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
        StatusPill(status = variant)
        Text(
            text = stringResource(
                Res.string.trip_design_schedule,
                tripDayLabel(isoToCairoDateKey(booking.pickupAt)),
                cairoTime12(booking.pickupAt),
            ),
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

/** Figma's trip schedule says "Today" or "Tomorrow" for those days, else the date. */
@Composable
private fun tripDayLabel(dateKey: String, clock: Clock = koinInject()): String {
    val now = clock.now()
    return when (dateKey) {
        dateKey(now) -> stringResource(Res.string.rider_home_date_today)
        dateKey(now + 1.days) -> stringResource(Res.string.rider_home_date_tomorrow)
        else -> bookingDateLabel(dateKey)
    }
}
