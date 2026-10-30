package com.hitchy.feature.booking.domain

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.CancelBookingReason
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.model.SearchSort
import com.hitchy.core.model.SearchTimeBand
import com.hitchy.core.model.Tier
import com.hitchy.core.model.VehicleType
import com.hitchy.feature.rides.domain.Booking
import kotlinx.coroutines.flow.Flow

data class RideHost(
    val uid: String,
    val firstName: String,
    val photoPath: String?,
    val rating: Double?,
    val ratingCount: Int,
    val trips: Int,
    val verified: Boolean,
    val tier: Tier,
    val sameCompany: Boolean,
)

data class RidePickup(
    val id: String,
    val name: LocalizedName,
    val at: String,
    val walkM: Int,
)

data class RideCard(
    val rideId: String,
    val departAt: String,
    val arriveAt: String,
    val fromZone: String,
    val toZone: String,
    val fromPointId: String,
    val toPointId: String,
    val fromPointName: LocalizedName,
    val toPointName: LocalizedName,
    val pricePerSeat: Int,
    val fairPrice: Boolean,
    val seatsLeft: Int,
    val walkM: Int?,
    val pickup: RidePickup,
    val bestMatch: Boolean,
    val womenOnly: Boolean,
    val vehicleType: VehicleType,
    val fixed: Boolean,
    val host: RideHost,
)

data class SearchInput(
    val fromZone: String,
    val toZone: String,
    val fromPointId: String?,
    val date: String,
    val time: String,
    val seats: Int = 1,
    val sort: SearchSort = SearchSort.RECOMMENDED,
    val timeBand: SearchTimeBand = SearchTimeBand.ANY,
    val maxPrice: Int? = null,
    val minRating: Double? = null,
    val verifiedOnly: Boolean = false,
    val womenOnly: Boolean = false,
)

data class RequestedBooking(
    val rideId: String,
    val status: BookingStatus,
    val seats: Int,
    val price: Int,
    val total: Int,
    val expiresAt: String,
)

data class RequestResult(
    val booking: RequestedBooking,
    val ride: RideCard,
)

interface BookingRepository {
    suspend fun search(input: SearchInput): AppResult<List<RideCard>>
    suspend fun request(
        rideId: String,
        seats: Int,
        pickupId: String,
        requestId: String,
    ): AppResult<RequestResult>
    suspend fun respond(
        rideId: String,
        riderUid: String,
        accept: Boolean,
    ): AppResult<BookingStatus>
    suspend fun cancel(
        rideId: String,
        reason: CancelBookingReason,
    ): AppResult<Boolean>
    fun observeMyTrips(): Flow<List<Booking>>
    fun observeBooking(rideId: String): Flow<Booking?>
    fun observeCachedRideCard(rideId: String): Flow<RideCard?>
}
