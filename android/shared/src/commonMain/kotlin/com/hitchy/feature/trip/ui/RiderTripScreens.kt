package com.hitchy.feature.trip.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.CancelBookingReason
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.SelectChip
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.components.TripStepper
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.isoToCairoTime
import com.hitchy.feature.trip.presentation.RiderTripAction
import com.hitchy.feature.trip.presentation.RiderTripViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.ride_details_vehicle_label
import hitchy.shared.generated.resources.rider_trip_boarded_button
import hitchy.shared.generated.resources.rider_trip_cancel_button
import hitchy.shared.generated.resources.rider_trip_cancel_reason_found_another
import hitchy.shared.generated.resources.rider_trip_cancel_reason_host_late
import hitchy.shared.generated.resources.rider_trip_cancel_reason_other
import hitchy.shared.generated.resources.rider_trip_cancel_reason_plans_changed
import hitchy.shared.generated.resources.rider_trip_cancel_sheet_subtitle
import hitchy.shared.generated.resources.rider_trip_cancel_sheet_title
import hitchy.shared.generated.resources.rider_trip_cancelled_title
import hitchy.shared.generated.resources.rider_trip_checked_in_badge
import hitchy.shared.generated.resources.rider_trip_checkin_button
import hitchy.shared.generated.resources.rider_trip_confirm_cancel
import hitchy.shared.generated.resources.rider_trip_dropoff_at
import hitchy.shared.generated.resources.rider_trip_keep_booking
import hitchy.shared.generated.resources.rider_trip_payment_amount
import hitchy.shared.generated.resources.rider_trip_payment_done_title
import hitchy.shared.generated.resources.rider_trip_pickup_at
import hitchy.shared.generated.resources.rider_trip_title
import hitchy.shared.generated.resources.rider_trip_waiting_payment
import hitchy.shared.generated.resources.rider_trip_wallet_remaining
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderTripScreen(
    rideId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    viewModel: RiderTripViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    val language: AppLanguage = languageSwitcher.getCurrentLanguage()

    LaunchedEffect(rideId) {
        viewModel.onAction(RiderTripAction.Init(rideId))
    }

    Surface(modifier = modifier.fillMaxSize(), color = colors.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = stringResource(Res.string.rider_trip_title),
                onBackClick = onBack,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                val error = state.error
                if (error != null) {
                    HitchyBanner(
                        text = stringResource(error.message()),
                        variant = BannerVariant.Error,
                        onDismiss = { viewModel.onAction(RiderTripAction.DismissError) },
                    )
                }

                if (state.isCancelled) {
                    HitchyBanner(
                        text = stringResource(Res.string.rider_trip_cancelled_title),
                        variant = BannerVariant.Warning,
                    )
                } else {
                    TripStepper(
                        currentStep = state.tripStep,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                val booking = state.booking
                val ride = state.ride

                // Vehicle details card
                if (ride != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.white,
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.space16),
                            verticalArrangement = Arrangement.spacedBy(spacing.space8),
                        ) {
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
                            if (booking != null) {
                                val pickupTime = isoToCairoTime(booking.pickupAt)
                                val pickupName = booking.pickupPointName.resolve(language)
                                Text(
                                    text = stringResource(Res.string.rider_trip_pickup_at, "$pickupName · $pickupTime"),
                                    style = typography.caption,
                                    color = colors.textSecondary,
                                )
                                val dropName = ride.toPointName.resolve(language)
                                Text(
                                    text = stringResource(Res.string.rider_trip_dropoff_at, dropName),
                                    style = typography.caption,
                                    color = colors.textSecondary,
                                )
                            }
                        }
                    }
                }

                // Check-in action or status
                if (booking != null && !state.isCancelled) {
                    if (state.isCheckedIn) {
                        HitchyBanner(
                            text = stringResource(Res.string.rider_trip_checked_in_badge),
                            variant = BannerVariant.Success,
                        )
                    } else if (state.canCheckIn) {
                        HitchyButton(
                            onClick = { viewModel.onAction(RiderTripAction.CheckIn) },
                            loading = state.isCheckingIn,
                            enabled = !state.isCheckingIn,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = stringResource(Res.string.rider_trip_checkin_button))
                        }
                    }
                }

                // Payment / Completion card
                if (booking != null) {
                    val paymentConfirmed = booking.paymentConfirmedAt != null
                    if (paymentConfirmed) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.lilac)
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                                .padding(spacing.space16),
                            color = colors.lilac,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
                                Text(
                                    text = stringResource(Res.string.rider_trip_payment_done_title),
                                    style = typography.h2,
                                    color = colors.navy,
                                )
                                Text(
                                    text = stringResource(Res.string.rider_trip_payment_amount, booking.total),
                                    style = typography.bodyStrong,
                                    color = colors.purple,
                                )
                                Text(
                                    text = stringResource(
                                        Res.string.rider_trip_wallet_remaining,
                                        state.walletBalanceEgp,
                                    ),
                                    style = typography.caption,
                                    color = colors.textSecondary,
                                )
                            }
                        }
                    } else if (state.tripStep == com.hitchy.core.ui.components.TripStep.Completed) {
                        HitchyBanner(
                            text = stringResource(Res.string.rider_trip_waiting_payment),
                            variant = BannerVariant.Info,
                        )
                    }
                }

                // Cancel booking button (available prior to boarding)
                if (state.canCancel) {
                    HitchyButton(
                        onClick = { viewModel.onAction(RiderTripAction.ShowCancelSheet(true)) },
                        variant = HitchyButtonVariant.Destructive,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.rider_trip_cancel_button))
                    }
                }
            }
        }
    }

    if (state.showCancelSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onAction(RiderTripAction.ShowCancelSheet(false)) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.white,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                Text(
                    text = stringResource(Res.string.rider_trip_cancel_sheet_title),
                    style = typography.h2,
                    color = colors.navy,
                )
                Text(
                    text = stringResource(Res.string.rider_trip_cancel_sheet_subtitle),
                    style = typography.body,
                    color = colors.textSecondary,
                )

                val reasons = listOf(
                    CancelBookingReason.HOST_LATE to
                        stringResource(Res.string.rider_trip_cancel_reason_host_late),
                    CancelBookingReason.PLANS_CHANGED to
                        stringResource(Res.string.rider_trip_cancel_reason_plans_changed),
                    CancelBookingReason.FOUND_ANOTHER_RIDE to
                        stringResource(Res.string.rider_trip_cancel_reason_found_another),
                    CancelBookingReason.OTHER to
                        stringResource(Res.string.rider_trip_cancel_reason_other),
                )

                reasons.forEach { (reason, label) ->
                    SelectChip(
                        label = label,
                        selected = state.selectedCancelReason == reason,
                        onClick = { viewModel.onAction(RiderTripAction.SelectCancelReason(reason)) },
                        enabled = !state.isCancelling,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space12),
                ) {
                    HitchyButton(
                        onClick = { viewModel.onAction(RiderTripAction.ShowCancelSheet(false)) },
                        variant = HitchyButtonVariant.Outline,
                        enabled = !state.isCancelling,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(Res.string.rider_trip_keep_booking))
                    }
                    HitchyButton(
                        onClick = { viewModel.onAction(RiderTripAction.ConfirmCancel) },
                        variant = HitchyButtonVariant.Destructive,
                        loading = state.isCancelling,
                        enabled = !state.isCancelling,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(Res.string.rider_trip_confirm_cancel))
                    }
                }
            }
        }
    }
}
