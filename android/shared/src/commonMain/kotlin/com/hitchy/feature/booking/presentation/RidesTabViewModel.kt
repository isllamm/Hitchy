package com.hitchy.feature.booking.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.RideStatus
import com.hitchy.core.ui.MviViewModel
import com.hitchy.core.util.isoToEpochMs
import com.hitchy.feature.booking.domain.BookingRepository
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.domain.RidesRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** One row of the single Rides list (plan 2.2 #6): a booking as rider or an offered ride as owner. */
sealed interface UnifiedRideItem {
    val rideId: String
    val departAt: String

    data class AsRider(val booking: Booking) : UnifiedRideItem {
        override val rideId: String get() = booking.rideId
        override val departAt: String get() = booking.departAt
    }

    data class AsOwner(val ride: Ride) : UnifiedRideItem {
        override val rideId: String get() = ride.id
        override val departAt: String get() = ride.departAt
    }
}

data class RidesTabState(
    val items: List<UnifiedRideItem> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface RidesTabAction

sealed interface RidesTabEvent

class RidesTabViewModel(
    bookingRepository: BookingRepository,
    ridesRepository: RidesRepository,
) : MviViewModel<RidesTabState, RidesTabAction, RidesTabEvent>(RidesTabState()) {

    init {
        combine(bookingRepository.observeMyTrips(), ridesRepository.observeMyRides()) { bookings, rides ->
            mergeRides(bookings, rides)
        }
            .onEach { merged -> setState { copy(items = merged, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: RidesTabAction) = Unit
}

/** Newest departure first. */
fun mergeRides(bookings: List<Booking>, rides: List<Ride>): List<UnifiedRideItem> {
    val allItems = bookings.map { UnifiedRideItem.AsRider(it) } + rides.map { UnifiedRideItem.AsOwner(it) }
    return allItems.sortedByDescending { isoToEpochMs(it.departAt) }
}

fun BookingStatus.opensRiderTrip(): Boolean = when (this) {
    BookingStatus.BOOKED,
    BookingStatus.BOARDED,
    BookingStatus.COMPLETED,
    -> true

    else -> false
}

fun RideStatus.opensOwnerTrip(): Boolean = when (this) {
    RideStatus.STARTED,
    RideStatus.FINISHED,
    -> true

    else -> false
}
