package com.hitchy.feature.trip.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.ErrorText
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.StatusChip
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.components.TripStep
import com.hitchy.core.ui.components.TripStepper
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.trip.presentation.HostTripAction
import com.hitchy.feature.trip.presentation.HostTripPhase
import com.hitchy.feature.trip.presentation.HostTripState
import com.hitchy.feature.trip.presentation.HostTripViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.host_trip_boarded_count
import hitchy.shared.generated.resources.host_trip_btn_boarded
import hitchy.shared.generated.resources.host_trip_btn_confirm_payment
import hitchy.shared.generated.resources.host_trip_btn_end_trip
import hitchy.shared.generated.resources.host_trip_btn_im_here
import hitchy.shared.generated.resources.host_trip_btn_next_stop
import hitchy.shared.generated.resources.host_trip_btn_payment_done
import hitchy.shared.generated.resources.host_trip_btn_report_late
import hitchy.shared.generated.resources.host_trip_btn_start
import hitchy.shared.generated.resources.host_trip_free_wait_over
import hitchy.shared.generated.resources.host_trip_free_wait_timer
import hitchy.shared.generated.resources.host_trip_payment_done_bookings
import hitchy.shared.generated.resources.host_trip_payment_done_title
import hitchy.shared.generated.resources.host_trip_payment_done_total
import hitchy.shared.generated.resources.host_trip_status_arrived
import hitchy.shared.generated.resources.host_trip_status_at_stop
import hitchy.shared.generated.resources.host_trip_status_driving
import hitchy.shared.generated.resources.host_trip_status_in_progress
import hitchy.shared.generated.resources.host_trip_status_leaving
import hitchy.shared.generated.resources.host_trip_title
import hitchy.shared.generated.resources.host_trip_wallet_balance
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val SECONDS_PER_MINUTE = 60

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
    val colors = HitchyTheme.colors
    val spacing = HitchyTheme.spacing

    LaunchedEffect(rideId) {
        viewModel.onAction(HostTripAction.Init(rideId))
    }

    val currentStep = when (state.phase) {
        HostTripPhase.LEAVING, HostTripPhase.DRIVING_TO_STOP -> TripStep.OwnerOnTheWay
        HostTripPhase.AT_STOP -> TripStep.Arrived
        HostTripPhase.EN_ROUTE -> TripStep.InRide
        HostTripPhase.ARRIVED, HostTripPhase.PAYMENT_DONE -> TripStep.Completed
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        TopBar(
            title = stringResource(Res.string.host_trip_title),
            onBackClick = onBack,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space16),
        ) {
            item {
                Spacer(modifier = Modifier.height(spacing.space8))
                TripStepper(currentStep = currentStep)
            }

            if (state.phase == HostTripPhase.PAYMENT_DONE) {
                item {
                    PaymentDoneCard(
                        state = state,
                        onDone = onBack,
                    )
                }
            } else {
                item {
                    ActivePhaseCard(
                        state = state,
                        language = language,
                        onStartTrip = { viewModel.onAction(HostTripAction.StartTrip) },
                        onArriveAtStop = { viewModel.onAction(HostTripAction.ArriveAtStop) },
                        onNextStopOrFinish = { viewModel.onAction(HostTripAction.NextStopOrFinish) },
                        onFinishTrip = { viewModel.onAction(HostTripAction.FinishTrip) },
                        onConfirmPayment = { viewModel.onAction(HostTripAction.ConfirmPayment) },
                    )
                }

                if (state.phase == HostTripPhase.AT_STOP) {
                    item {
                        BoardingSectionHeader(state = state)
                    }

                    items(state.bookings, key = { it.riderUid }) { booking ->
                        HostRiderBoardingCard(
                            booking = booking,
                            riderName = state.riderNames[booking.riderUid],
                            isFreeWaitOver = state.isFreeWaitOver,
                            onMarkBoarded = {
                                viewModel.onAction(HostTripAction.MarkBoarded(booking.riderUid))
                            },
                            onReportLate = {
                                viewModel.onAction(HostTripAction.ReportLate(booking.riderUid))
                            },
                        )
                    }
                }
            }

            val error = state.error
            if (error != null) {
                item {
                    ErrorText(
                        error = error,
                        style = HitchyTheme.typography.caption,
                        color = colors.error,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(spacing.space32)) }
        }
    }
}

@Composable
private fun ActivePhaseCard(
    state: HostTripState,
    language: AppLanguage,
    onStartTrip: () -> Unit,
    onArriveAtStop: () -> Unit,
    onNextStopOrFinish: () -> Unit,
    onFinishTrip: () -> Unit,
    onConfirmPayment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val spacing = HitchyTheme.spacing
    val typography = HitchyTheme.typography
    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
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
            val fromName = state.ride?.fromPointName?.resolve(language).orEmpty()
            val toName = state.ride?.toPointName?.resolve(language).orEmpty()
            val routeText = "$fromName → $toName"
            when (state.phase) {
                HostTripPhase.LEAVING -> {
                    StatusChip(
                        status = RideStatusVariant.Confirmed,
                        customLabel = stringResource(Res.string.host_trip_status_leaving),
                    )
                    Text(
                        text = routeText,
                        style = typography.title,
                        color = colors.textPrimary,
                    )
                    HitchyButton(
                        onClick = onStartTrip,
                        loading = state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.host_trip_btn_start))
                    }
                }

                HostTripPhase.DRIVING_TO_STOP -> {
                    StatusChip(
                        status = RideStatusVariant.InProgress,
                        customLabel = stringResource(Res.string.host_trip_status_driving, state.currentStopName),
                    )
                    Text(
                        text = state.currentStopName,
                        style = typography.h2,
                        color = colors.textPrimary,
                    )
                    HitchyButton(
                        onClick = onArriveAtStop,
                        loading = state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.host_trip_btn_im_here))
                    }
                }

                HostTripPhase.AT_STOP -> {
                    StatusChip(
                        status = RideStatusVariant.Completed,
                        customLabel = stringResource(Res.string.host_trip_status_at_stop, state.currentStopName),
                    )
                    WaitTimerDisplay(
                        freeWaitSecondsLeft = state.freeWaitSecondsLeft,
                        isFreeWaitOver = state.isFreeWaitOver,
                        lateSeconds = state.lateSeconds,
                    )
                    HitchyButton(
                        onClick = onNextStopOrFinish,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.host_trip_btn_next_stop))
                    }
                }

                HostTripPhase.EN_ROUTE -> {
                    StatusChip(
                        status = RideStatusVariant.InProgress,
                        customLabel = stringResource(Res.string.host_trip_status_in_progress),
                    )
                    Text(
                        text = stringResource(
                            Res.string.host_trip_boarded_count,
                            state.boardedCount,
                            state.totalBookedSeats,
                        ),
                        style = typography.title,
                        color = colors.purple,
                    )
                    HitchyButton(
                        onClick = onFinishTrip,
                        loading = state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.host_trip_btn_end_trip))
                    }
                }

                HostTripPhase.ARRIVED -> {
                    StatusChip(
                        status = RideStatusVariant.Completed,
                        customLabel = stringResource(Res.string.host_trip_status_arrived),
                    )
                    Text(
                        text = state.ride?.toPointName?.resolve(language).orEmpty(),
                        style = typography.h2,
                        color = colors.textPrimary,
                    )
                    HitchyButton(
                        onClick = onConfirmPayment,
                        loading = state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.host_trip_btn_confirm_payment))
                    }
                }

                HostTripPhase.PAYMENT_DONE -> Unit
            }
        }
    }
}

@Composable
private fun WaitTimerDisplay(
    freeWaitSecondsLeft: Int,
    isFreeWaitOver: Boolean,
    lateSeconds: Int,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography

    if (!isFreeWaitOver) {
        val mins = freeWaitSecondsLeft / SECONDS_PER_MINUTE
        val secs = freeWaitSecondsLeft % SECONDS_PER_MINUTE
        Text(
            text = stringResource(Res.string.host_trip_free_wait_timer, mins, secs),
            style = typography.h2,
            color = colors.purple,
            modifier = modifier,
        )
    } else {
        val mins = lateSeconds / SECONDS_PER_MINUTE
        val secs = lateSeconds % SECONDS_PER_MINUTE
        Text(
            text = stringResource(Res.string.host_trip_free_wait_over, mins, secs),
            style = typography.h2,
            color = colors.error,
            modifier = modifier,
        )
    }
}

@Composable
private fun BoardingSectionHeader(
    state: HostTripState,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography

    Text(
        text = stringResource(
            Res.string.host_trip_boarded_count,
            state.boardedCount,
            state.totalBookedSeats,
        ),
        style = typography.title,
        color = colors.textPrimary,
        modifier = modifier,
    )
}

@Composable
private fun HostRiderBoardingCard(
    booking: Booking,
    riderName: String?,
    isFreeWaitOver: Boolean,
    onMarkBoarded: () -> Unit,
    onReportLate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val spacing = HitchyTheme.spacing
    val typography = HitchyTheme.typography
    val cardShape = RoundedCornerShape(12.dp)
    val shortName = riderName ?: booking.riderUid.take(RIDER_NAME_LENGTH).replaceFirstChar { it.uppercase() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, colors.border, cardShape),
        color = colors.white,
        shape = cardShape,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space12),
            verticalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                Avatar(initials = shortName.take(2).uppercase())
                Text(
                    text = shortName,
                    style = typography.title,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (booking.boardedAt != null) {
                    StatusChip(
                        status = RideStatusVariant.Completed,
                        customLabel = "On board",
                    )
                }
            }

            if (booking.boardedAt == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    HitchyButton(
                        onClick = onMarkBoarded,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(Res.string.host_trip_btn_boarded, shortName))
                    }
                    if (isFreeWaitOver) {
                        HitchyButton(
                            onClick = onReportLate,
                            variant = HitchyButtonVariant.Outline,
                        ) {
                            Text(text = stringResource(Res.string.host_trip_btn_report_late))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentDoneCard(
    state: HostTripState,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val spacing = HitchyTheme.spacing
    val typography = HitchyTheme.typography
    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, colors.border, cardShape),
        color = colors.white,
        shape = cardShape,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.space16),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.lavender),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Check),
                    contentDescription = null,
                    tint = colors.purple,
                    modifier = Modifier.size(32.dp),
                )
            }

            Text(
                text = stringResource(Res.string.host_trip_payment_done_title),
                style = typography.h2,
                color = colors.textPrimary,
            )

            Text(
                text = stringResource(Res.string.host_trip_payment_done_total, state.paymentTotalCollected),
                style = typography.title,
                color = colors.purple,
            )

            Text(
                text = stringResource(Res.string.host_trip_payment_done_bookings, state.paymentBookingsCount),
                style = typography.body,
                color = colors.textSecondary,
            )

            Text(
                text = stringResource(Res.string.host_trip_wallet_balance, state.walletBalance),
                style = typography.title,
                color = colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(spacing.space8))

            HitchyButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.host_trip_btn_payment_done))
            }
        }
    }
}

private const val RIDER_NAME_LENGTH = 8
