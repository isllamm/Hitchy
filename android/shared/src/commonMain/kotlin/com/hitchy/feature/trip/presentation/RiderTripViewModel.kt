package com.hitchy.feature.trip.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.CancelBookingReason
import com.hitchy.core.model.RideStatus
import com.hitchy.core.platform.LocationProvider
import com.hitchy.core.ui.MviViewModel
import com.hitchy.core.ui.components.TripStep
import com.hitchy.feature.booking.domain.BookingRepository
import com.hitchy.feature.profile.domain.WalletRepository
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.domain.RidesRepository
import com.hitchy.feature.trip.domain.TripRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class RiderTripState(
    val rideId: String = "",
    val booking: Booking? = null,
    val ride: Ride? = null,
    val walletBalanceEgp: Int = 0,
    val isCheckingIn: Boolean = false,
    val isCancelling: Boolean = false,
    val showCancelSheet: Boolean = false,
    val selectedCancelReason: CancelBookingReason = CancelBookingReason.PLANS_CHANGED,
    val error: AppError? = null,
) {
    val tripStep: TripStep
        get() = when {
            booking?.status == BookingStatus.COMPLETED || ride?.status == RideStatus.FINISHED -> TripStep.Completed
            booking?.boardedAt != null -> TripStep.InRide
            booking?.hostArrivedAt != null -> TripStep.Arrived
            else -> TripStep.OwnerOnTheWay
        }

    val isCheckedIn: Boolean
        get() = booking?.checkedInAt != null

    val isCancelled: Boolean
        get() = booking?.status == BookingStatus.CANCELLED_BY_RIDER ||
            booking?.status == BookingStatus.CANCELLED_BY_HOST

    val canCancel: Boolean
        get() = booking != null &&
            booking.status == BookingStatus.BOOKED &&
            booking.boardedAt == null &&
            !isCancelled

    val canCheckIn: Boolean
        get() = booking != null &&
            booking.status == BookingStatus.BOOKED &&
            !isCheckedIn &&
            !isCancelled &&
            !isCheckingIn
}

sealed interface RiderTripAction {
    data class Init(val rideId: String) : RiderTripAction
    data object CheckIn : RiderTripAction
    data class ShowCancelSheet(val show: Boolean) : RiderTripAction
    data class SelectCancelReason(val reason: CancelBookingReason) : RiderTripAction
    data object ConfirmCancel : RiderTripAction
    data object DismissError : RiderTripAction
}

sealed interface RiderTripEvent

class RiderTripViewModel(
    private val bookingRepository: BookingRepository,
    private val ridesRepository: RidesRepository,
    private val tripRepository: TripRepository,
    private val walletRepository: WalletRepository,
    private val locationProvider: LocationProvider,
) : MviViewModel<RiderTripState, RiderTripAction, RiderTripEvent>(RiderTripState()) {

    private var observeJob: Job? = null

    override fun onAction(action: RiderTripAction) {
        when (action) {
            is RiderTripAction.Init -> initRide(action.rideId)
            RiderTripAction.CheckIn -> checkIn()
            is RiderTripAction.ShowCancelSheet -> setState { copy(showCancelSheet = action.show) }
            is RiderTripAction.SelectCancelReason -> setState { copy(selectedCancelReason = action.reason) }
            RiderTripAction.ConfirmCancel -> cancelBooking()
            RiderTripAction.DismissError -> setState { copy(error = null) }
        }
    }

    private fun initRide(rideId: String) {
        if (state.value.rideId == rideId && observeJob != null) return
        observeJob?.cancel()
        setState { copy(rideId = rideId, error = null) }

        observeJob = viewModelScope.launch {
            bookingRepository.observeBooking(rideId)
                .onEach { bk -> setState { copy(booking = bk) } }
                .launchIn(this)

            ridesRepository.observeRide(rideId)
                .onEach { r -> setState { copy(ride = r) } }
                .launchIn(this)

            walletRepository.observeBalance()
                .onEach { balance -> setState { copy(walletBalanceEgp = balance) } }
                .launchIn(this)
        }
    }

    private fun checkIn() {
        val current = state.value
        val bk = current.booking ?: return
        if (!current.canCheckIn) return

        setState { copy(isCheckingIn = true, error = null) }
        viewModelScope.launch {
            val location = locationProvider.current() ?: bk.pickupPoint
            val result = tripRepository.checkIn(
                rideId = current.rideId,
                at = location,
                accuracyM = CHECK_IN_ACCURACY_M,
            )
            when (result) {
                is AppResult.Ok -> setState { copy(isCheckingIn = false) }
                is AppResult.Err -> setState { copy(isCheckingIn = false, error = result.error) }
            }
        }
    }

    private fun cancelBooking() {
        val current = state.value
        if (!current.canCancel || current.isCancelling) return

        setState { copy(isCancelling = true, error = null) }
        viewModelScope.launch {
            val result = bookingRepository.cancel(
                rideId = current.rideId,
                reason = current.selectedCancelReason,
            )
            when (result) {
                is AppResult.Ok -> setState { copy(isCancelling = false, showCancelSheet = false) }
                is AppResult.Err -> setState { copy(isCancelling = false, error = result.error) }
            }
        }
    }

    private companion object {
        const val CHECK_IN_ACCURACY_M = 10.0
    }
}
