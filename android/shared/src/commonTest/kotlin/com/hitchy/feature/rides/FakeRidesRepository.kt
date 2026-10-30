package com.hitchy.feature.rides

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.CancelRideReason
import com.hitchy.core.model.Direction
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.model.RideStatus
import com.hitchy.core.model.TrafficLevel
import com.hitchy.core.model.VehicleType
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.rides.domain.CreateRideInput
import com.hitchy.feature.rides.domain.Quote
import com.hitchy.feature.rides.domain.QuoteInput
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.domain.RidePriceRange
import com.hitchy.feature.rides.domain.RideVehicle
import com.hitchy.feature.rides.domain.RidesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRidesRepository : RidesRepository {
    val myRidesFlow = MutableStateFlow<List<Ride>>(emptyList())
    var lastCreateInput: CreateRideInput? = null
    val ridesById = MutableStateFlow<Map<String, Ride>>(emptyMap())
    val bookingsByRideId = MutableStateFlow<Map<String, List<Booking>>>(emptyMap())

    override suspend fun quote(input: QuoteInput): AppResult<Quote> = AppResult.Ok(
        Quote(
            min = 25,
            recommended = 35,
            max = 50,
            trafficLevel = TrafficLevel.NORMAL,
            distanceKm = 24.5,
            fuelCostEgp = 30,
            tripCostEgp = 35,
            configVersion = 1,
        ),
    )

    override suspend fun create(input: CreateRideInput): AppResult<String> {
        lastCreateInput = input
        val rideId = "ride-new"
        return AppResult.Ok(rideId)
    }

    override suspend fun cancel(rideId: String, reason: CancelRideReason): AppResult<Int> = AppResult.Ok(1)

    override fun observeMyRides(): Flow<List<Ride>> = myRidesFlow

    override fun observeRide(rideId: String): Flow<Ride?> = ridesById.map { it[rideId] }

    override fun observeBookings(rideId: String): Flow<List<Booking>> = bookingsByRideId.map { it[rideId].orEmpty() }

    companion object {
        @Suppress("LongParameterList")
        fun sampleRide(
            id: String = "ride-1",
            hostId: String = "host-1",
            status: RideStatus = RideStatus.OPEN,
            seatsTotal: Int = 3,
            seatsLeft: Int = 3,
            pricePerSeat: Int = 35,
        ): Ride = Ride(
            id = id,
            hostId = hostId,
            hostCompanyId = null,
            vehicleId = "veh-1",
            vehicle = RideVehicle(
                type = VehicleType.CAR,
                make = "Fiat",
                model = "Tipo",
                color = "White",
                plate = "123 ABC",
            ),
            fromZone = "maadi",
            fromPointId = "degla",
            fromPointName = LocalizedName(ar = "دجلة", en = "Degla"),
            fromPoint = GeoPoint(lat = 29.96, lng = 31.28),
            toZone = "smart-village",
            toPointId = "gate-1",
            toPointName = LocalizedName(ar = "البوابة 1", en = "Gate 1"),
            stops = emptyList(),
            pickupZones = listOf("maadi"),
            direction = Direction.TO_WORK,
            departAt = "2026-10-30T08:00:00Z",
            dateKey = "2026-10-30",
            seatsTotal = seatsTotal,
            seatsLeft = seatsLeft,
            pricePerSeat = pricePerSeat,
            priceRange = RidePriceRange(
                min = 25,
                recommended = 35,
                max = 50,
                trafficLevel = TrafficLevel.NORMAL,
                distanceKm = 24.5,
                configVersion = 1,
            ),
            womenOnly = false,
            status = status,
            fixedRideId = null,
            bookingsCount = seatsTotal - seatsLeft,
            startedAt = null,
            finishedAt = null,
            cancelledAt = null,
            cancelReason = null,
        )
    }
}
