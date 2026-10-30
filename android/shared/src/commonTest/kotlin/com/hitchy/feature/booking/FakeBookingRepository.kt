package com.hitchy.feature.booking

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.CancelBookingReason
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.model.Tier
import com.hitchy.core.model.VehicleType
import com.hitchy.feature.booking.domain.BookingRepository
import com.hitchy.feature.booking.domain.RequestResult
import com.hitchy.feature.booking.domain.RequestedBooking
import com.hitchy.feature.booking.domain.RideCard
import com.hitchy.feature.booking.domain.RideHost
import com.hitchy.feature.booking.domain.RidePickup
import com.hitchy.feature.booking.domain.SearchInput
import com.hitchy.feature.rides.domain.Booking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBookingRepository : BookingRepository {
    val searchResultsFlow = MutableStateFlow<List<RideCard>>(emptyList())
    val myTripsFlow = MutableStateFlow<List<Booking>>(emptyList())
    val bookingsByRideId = MutableStateFlow<Map<String, Booking>>(emptyMap())
    val cachedCards = MutableStateFlow<Map<String, RideCard>>(emptyMap())

    var lastSearchInput: SearchInput? = null
    var lastRequestId: String? = null
    var lastRequestedRideId: String? = null
    var lastRequestedSeats: Int? = null
    var lastRequestedPickupId: String? = null
    var lastCancelledRideId: String? = null
    var lastCancelReason: CancelBookingReason? = null

    var searchResult: AppResult<List<RideCard>>? = null
    var requestResult: AppResult<RequestResult>? = null
    var cancelResult: AppResult<Boolean> = AppResult.Ok(true)

    override suspend fun search(input: SearchInput): AppResult<List<RideCard>> {
        lastSearchInput = input
        return searchResult ?: AppResult.Ok(searchResultsFlow.value)
    }

    override suspend fun request(
        rideId: String,
        seats: Int,
        pickupId: String,
        requestId: String,
    ): AppResult<RequestResult> {
        lastRequestedRideId = rideId
        lastRequestedSeats = seats
        lastRequestedPickupId = pickupId
        lastRequestId = requestId

        val custom = requestResult
        if (custom != null) return custom

        val sampleCard = cachedCards.value[rideId] ?: sampleRideCard(rideId = rideId)
        val sampleReq = RequestedBooking(
            rideId = rideId,
            status = BookingStatus.REQUESTED,
            seats = seats,
            price = sampleCard.pricePerSeat,
            total = sampleCard.pricePerSeat * seats,
            expiresAt = "2026-10-30T10:15:00Z",
        )
        return AppResult.Ok(RequestResult(booking = sampleReq, ride = sampleCard))
    }

    override suspend fun respond(rideId: String, riderUid: String, accept: Boolean): AppResult<BookingStatus> {
        val newStatus = if (accept) BookingStatus.BOOKED else BookingStatus.DECLINED
        return AppResult.Ok(newStatus)
    }

    override suspend fun cancel(rideId: String, reason: CancelBookingReason): AppResult<Boolean> {
        lastCancelledRideId = rideId
        lastCancelReason = reason
        val existing = bookingsByRideId.value[rideId]
        if (existing != null) {
            val cancelled = existing.copy(status = BookingStatus.CANCELLED_BY_RIDER)
            bookingsByRideId.value = bookingsByRideId.value + (rideId to cancelled)
        }
        return cancelResult
    }

    override fun observeMyTrips(): Flow<List<Booking>> = myTripsFlow

    override fun observeBooking(rideId: String): Flow<Booking?> = bookingsByRideId.map { it[rideId] }

    override fun observeCachedRideCard(rideId: String): Flow<RideCard?> = cachedCards.map { it[rideId] }

    companion object {
        fun sampleRideCard(
            rideId: String = "ride-1",
            hostName: String = "Ahmed",
            price: Int = 35,
            seatsLeft: Int = 3,
        ): RideCard = RideCard(
            rideId = rideId,
            departAt = "2026-10-30T08:00:00Z",
            arriveAt = "2026-10-30T08:45:00Z",
            fromZone = "maadi",
            toZone = "smart-village",
            fromPointId = "degla",
            toPointId = "gate-1",
            fromPointName = LocalizedName(ar = "دجلة", en = "Degla"),
            toPointName = LocalizedName(ar = "البوابة 1", en = "Gate 1"),
            pricePerSeat = price,
            fairPrice = true,
            seatsLeft = seatsLeft,
            walkM = 250,
            pickup = RidePickup(
                id = "start",
                name = LocalizedName(ar = "دجلة", en = "Degla"),
                at = "08:00",
                walkM = 250,
            ),
            bestMatch = true,
            womenOnly = false,
            vehicleType = VehicleType.CAR,
            fixed = false,
            host = RideHost(
                uid = "host-1",
                firstName = hostName,
                photoPath = null,
                rating = 4.9,
                ratingCount = 28,
                trips = 42,
                verified = true,
                tier = Tier.SILVER,
                sameCompany = false,
            ),
        )

        fun sampleBooking(
            rideId: String = "ride-1",
            riderUid: String = "rider-1",
            hostId: String = "host-1",
            departAt: String = "2026-10-30T08:00:00Z",
            status: BookingStatus = BookingStatus.BOOKED,
            seats: Int = 1,
            price: Int = 35,
            total: Int = 35,
            pickupId: String = "start",
            pickupPointName: LocalizedName = LocalizedName(ar = "دجلة", en = "Degla"),
            pickupPoint: GeoPoint = GeoPoint(lat = 29.96, lng = 31.28),
            pickupAt: String = "2026-10-30T08:00:00Z",
            expiresAt: String = "2026-10-30T08:15:00Z",
            respondedAt: String? = null,
            checkedInAt: String? = null,
            checkInDistanceM: Int? = null,
            hostArrivedAt: String? = null,
            boardedAt: String? = null,
            lateFeeId: String? = null,
            paymentConfirmedAt: String? = null,
            verifiedTogether: Boolean = false,
            rewarded: Boolean = false,
            lateCancel: Boolean = false,
            cancelledAt: String? = null,
            cancelReason: String? = null,
        ): Booking = Booking(
            rideId = rideId,
            riderUid = riderUid,
            hostId = hostId,
            departAt = departAt,
            status = status,
            seats = seats,
            price = price,
            total = total,
            pickupId = pickupId,
            pickupPointName = pickupPointName,
            pickupPoint = pickupPoint,
            pickupAt = pickupAt,
            expiresAt = expiresAt,
            respondedAt = respondedAt,
            checkedInAt = checkedInAt,
            checkInDistanceM = checkInDistanceM,
            hostArrivedAt = hostArrivedAt,
            boardedAt = boardedAt,
            lateFeeId = lateFeeId,
            paymentConfirmedAt = paymentConfirmedAt,
            verifiedTogether = verifiedTogether,
            rewarded = rewarded,
            lateCancel = lateCancel,
            cancelledAt = cancelledAt,
            cancelReason = cancelReason,
        )
    }
}
