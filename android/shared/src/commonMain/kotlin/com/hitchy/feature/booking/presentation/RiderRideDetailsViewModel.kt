package com.hitchy.feature.booking.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.booking.domain.BookingRepository
import com.hitchy.feature.booking.domain.RideCard
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.domain.RidesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val MAX_SEATS_PER_BOOKING = 3

private val ACCEPTED_STATUSES = setOf(BookingStatus.BOOKED, BookingStatus.BOARDED, BookingStatus.COMPLETED)

data class RiderRideDetailsState(
    val rideId: String = "",
    val rideCard: RideCard? = null,
    val ride: Ride? = null,
    val booking: Booking? = null,
    val seats: Int = 1,
    val requestId: String = "",
    val isRequesting: Boolean = false,
    val error: AppError? = null,
) {
    val maxSeats: Int
        get() = (ride?.seatsLeft ?: rideCard?.seatsLeft ?: 1).coerceIn(1, MAX_SEATS_PER_BOOKING)

    val pricePerSeat: Int
        get() = booking?.price ?: ride?.pricePerSeat ?: rideCard?.pricePerSeat ?: 0

    val bookedSeats: Int
        get() = booking?.seats ?: seats

    val totalEgp: Int
        get() = booking?.total ?: (pricePerSeat * seats)

    val isAccepted: Boolean
        get() = booking?.status in ACCEPTED_STATUSES

    val isRequested: Boolean
        get() = booking?.status == BookingStatus.REQUESTED

    /** True while the rider can still send a request (no live booking on this ride). */
    val canRequest: Boolean
        get() = booking == null || booking.status !in ACCEPTED_STATUSES + BookingStatus.REQUESTED
}

sealed interface RiderRideDetailsAction {
    data class Init(val rideId: String) : RiderRideDetailsAction
    data class UpdateSeats(val seats: Int) : RiderRideDetailsAction
    data object RequestRide : RiderRideDetailsAction
    data object DismissError : RiderRideDetailsAction
}

sealed interface RiderRideDetailsEvent

@OptIn(ExperimentalUuidApi::class)
class RiderRideDetailsViewModel(
    private val bookingRepository: BookingRepository,
    private val ridesRepository: RidesRepository,
) : MviViewModel<RiderRideDetailsState, RiderRideDetailsAction, RiderRideDetailsEvent>(RiderRideDetailsState()) {

    private var observeJob: Job? = null

    override fun onAction(action: RiderRideDetailsAction) {
        when (action) {
            is RiderRideDetailsAction.Init -> init(action.rideId)

            is RiderRideDetailsAction.UpdateSeats -> setState {
                // A new seat count is a new request, so it gets a new idempotency key.
                copy(seats = action.seats.coerceIn(1, maxSeats), requestId = "", error = null)
            }

            RiderRideDetailsAction.RequestRide -> request()

            RiderRideDetailsAction.DismissError -> setState { copy(error = null) }
        }
    }

    private fun init(rideId: String) {
        if (state.value.rideId == rideId && observeJob != null) return
        observeJob?.cancel()
        setState { RiderRideDetailsState(rideId = rideId) }
        observeJob = viewModelScope.launch {
            // Before acceptance the rider can't read rides/{rideId}; the search card fills the screen.
            bookingRepository.observeCachedRideCard(rideId)
                .onEach { card -> if (card != null) setState { copy(rideCard = card) } }
                .launchIn(this)
            bookingRepository.observeBooking(rideId)
                .onEach { booking -> setState { copy(booking = booking) } }
                .launchIn(this)
            ridesRepository.observeRide(rideId)
                .onEach { ride -> if (ride != null) setState { copy(ride = ride) } }
                .launchIn(this)
        }
    }

    private fun request() {
        val current = state.value
        val card = current.rideCard
        if (!current.canRequest || current.isRequesting) return
        if (current.rideId.isBlank() || card == null) return
        // The same key is reused on retry so a lost response never books twice.
        val requestId = current.requestId.ifBlank { Uuid.random().toString() }
        setState { copy(isRequesting = true, requestId = requestId, error = null) }
        viewModelScope.launch {
            val result = bookingRepository.request(
                rideId = current.rideId,
                seats = current.seats,
                pickupId = card.pickup.id,
                requestId = requestId,
            )
            when (result) {
                is AppResult.Ok -> setState { copy(isRequesting = false, rideCard = result.value.ride) }
                is AppResult.Err -> setState { copy(isRequesting = false, error = result.error) }
            }
        }
    }
}
