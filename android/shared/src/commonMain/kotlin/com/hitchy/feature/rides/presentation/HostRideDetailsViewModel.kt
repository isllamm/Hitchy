package com.hitchy.feature.rides.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.CancelRideReason
import com.hitchy.core.model.RideStatus
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.booking.domain.BookingRepository
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.domain.RidesRepository
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.host_ride_details_accepted_alert
import hitchy.shared.generated.resources.host_ride_details_declined_alert
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

data class HostRideDetailsState(
    val rideId: String = "",
    val ride: Ride? = null,
    val pendingBookings: List<Booking> = emptyList(),
    val confirmedBookings: List<Booking> = emptyList(),
    val riderNames: Map<String, String> = emptyMap(),
    val riderPhotos: Map<String, String?> = emptyMap(),
    val isResponding: Boolean = false,
    val isCancelling: Boolean = false,
    val messageAlert: StringResource? = null,
    val error: AppError? = null,
) {
    val expectedMinEarnings: Int
        get() = (ride?.pricePerSeat ?: 0) * confirmedBookings.sumOf { it.seats }

    val expectedMaxEarnings: Int
        get() = (ride?.pricePerSeat ?: 0) * (ride?.seatsTotal ?: 0)

    val canStartTrip: Boolean
        get() {
            val r = ride ?: return false
            return r.status == RideStatus.OPEN || r.status == RideStatus.FULL
        }

    val isTripInProgress: Boolean
        get() = ride?.status == RideStatus.STARTED
}

sealed interface HostRideDetailsAction {
    data class Init(val rideId: String) : HostRideDetailsAction
    data class RespondRequest(val riderUid: String, val accept: Boolean) : HostRideDetailsAction
    data class CancelRide(val reason: CancelRideReason) : HostRideDetailsAction
    data object DismissAlert : HostRideDetailsAction
}

sealed interface HostRideDetailsEvent {
    data class NavigateToTrip(val rideId: String) : HostRideDetailsEvent
    data object RideCancelled : HostRideDetailsEvent
}

class HostRideDetailsViewModel(
    private val ridesRepository: RidesRepository,
    private val bookingRepository: BookingRepository,
    private val profileRepository: ProfileRepository,
) : MviViewModel<HostRideDetailsState, HostRideDetailsAction, HostRideDetailsEvent>(HostRideDetailsState()) {

    private val namesRequested = mutableSetOf<String>()

    override fun onAction(action: HostRideDetailsAction) {
        when (action) {
            is HostRideDetailsAction.Init -> init(action.rideId)
            is HostRideDetailsAction.RespondRequest -> respondRequest(action.riderUid, action.accept)
            is HostRideDetailsAction.CancelRide -> cancelRide(action.reason)
            HostRideDetailsAction.DismissAlert -> setState { copy(messageAlert = null, error = null) }
        }
    }

    private fun init(rideId: String) {
        if (state.value.rideId == rideId) return
        setState { copy(rideId = rideId) }

        ridesRepository.observeRide(rideId)
            .onEach { r -> setState { copy(ride = r) } }
            .launchIn(viewModelScope)

        ridesRepository.observeBookings(rideId)
            .onEach { allBookings ->
                loadRiderNames(allBookings.map { it.riderUid })
                val pending = allBookings.filter { it.status == BookingStatus.REQUESTED }
                val confirmed = allBookings.filter {
                    it.status == BookingStatus.BOOKED || it.status == BookingStatus.COMPLETED
                }
                setState {
                    copy(
                        pendingBookings = pending,
                        confirmedBookings = confirmed,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadRiderNames(uids: List<String>) {
        for (uid in uids) {
            if (!namesRequested.add(uid)) continue
            profileRepository.observePublicProfile(uid)
                .onEach { profile ->
                    if (profile != null) {
                        setState {
                            copy(
                                riderNames = riderNames + (uid to profile.firstName),
                                riderPhotos = riderPhotos + (uid to profile.photoPath),
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun respondRequest(riderUid: String, accept: Boolean) {
        val rideId = state.value.rideId
        if (rideId.isBlank()) return

        setState { copy(isResponding = true, error = null) }
        viewModelScope.launch {
            val res = bookingRepository.respond(rideId = rideId, riderUid = riderUid, accept = accept)
            when (res) {
                is AppResult.Ok -> {
                    val alert = if (accept) {
                        Res.string.host_ride_details_accepted_alert
                    } else {
                        Res.string.host_ride_details_declined_alert
                    }
                    setState { copy(isResponding = false, messageAlert = alert) }
                }

                is AppResult.Err -> setState { copy(isResponding = false, error = res.error) }
            }
        }
    }

    private fun cancelRide(reason: CancelRideReason) {
        val rideId = state.value.rideId
        if (rideId.isBlank()) return

        setState { copy(isCancelling = true, error = null) }
        viewModelScope.launch {
            val res = ridesRepository.cancel(rideId, reason)
            when (res) {
                is AppResult.Ok -> {
                    setState { copy(isCancelling = false) }
                    emit(HostRideDetailsEvent.RideCancelled)
                }

                is AppResult.Err -> setState { copy(isCancelling = false, error = res.error) }
            }
        }
    }
}
