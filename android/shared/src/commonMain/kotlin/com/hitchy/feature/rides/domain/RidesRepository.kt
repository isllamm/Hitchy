package com.hitchy.feature.rides.domain

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.CancelRideReason
import com.hitchy.core.model.Direction
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.model.RideStatus
import com.hitchy.core.model.TrafficLevel
import com.hitchy.core.model.VehicleType
import kotlinx.coroutines.flow.Flow

data class RideStop(
    val id: String,
    val zone: String,
    val pointId: String,
    val pointName: LocalizedName,
    val point: GeoPoint,
    val at: String,
)

data class RideVehicle(
    val type: VehicleType,
    val make: String,
    val model: String,
    val color: String,
    val plate: String,
)

data class RidePriceRange(
    val min: Int,
    val recommended: Int,
    val max: Int,
    val trafficLevel: TrafficLevel,
    val distanceKm: Double,
    val configVersion: Int,
)

data class Ride(
    val id: String,
    val hostId: String,
    val hostCompanyId: String?,
    val vehicleId: String,
    val vehicle: RideVehicle,
    val fromZone: String,
    val fromPointId: String,
    val fromPointName: LocalizedName,
    val fromPoint: GeoPoint,
    val toZone: String,
    val toPointId: String,
    val toPointName: LocalizedName,
    val stops: List<RideStop>,
    val pickupZones: List<String>,
    val direction: Direction,
    val departAt: String,
    val dateKey: String,
    val seatsTotal: Int,
    val seatsLeft: Int,
    val pricePerSeat: Int,
    val priceRange: RidePriceRange,
    val womenOnly: Boolean,
    val status: RideStatus,
    val fixedRideId: String?,
    val bookingsCount: Int,
    val startedAt: String?,
    val finishedAt: String?,
    val cancelledAt: String?,
    val cancelReason: String?,
)

data class Booking(
    val rideId: String,
    val riderUid: String,
    val hostId: String,
    val departAt: String,
    val status: BookingStatus,
    val seats: Int,
    val price: Int,
    val total: Int,
    val pickupId: String,
    val pickupPointName: LocalizedName,
    val pickupPoint: GeoPoint,
    val pickupAt: String,
    val expiresAt: String,
    val respondedAt: String?,
    val checkedInAt: String?,
    val checkInDistanceM: Int?,
    val hostArrivedAt: String?,
    val boardedAt: String?,
    val lateFeeId: String?,
    val paymentConfirmedAt: String?,
    val verifiedTogether: Boolean,
    val rewarded: Boolean,
    val lateCancel: Boolean,
    val cancelledAt: String?,
    val cancelReason: String?,
)

data class QuoteInput(
    val vehicleId: String,
    val fromZone: String,
    val toZone: String,
    val departAt: String,
    val seats: Int,
)

data class Quote(
    val min: Int,
    val recommended: Int,
    val max: Int,
    val trafficLevel: TrafficLevel,
    val distanceKm: Double,
    val fuelCostEgp: Int,
    val tripCostEgp: Int,
    val configVersion: Int,
)

data class RideStopInput(
    val zone: String,
    val pointId: String,
    val at: String,
)

data class CreateRideInput(
    val vehicleId: String,
    val fromZone: String,
    val fromPointId: String,
    val toZone: String,
    val toPointId: String,
    val stops: List<RideStopInput>,
    val direction: Direction,
    val departAt: String,
    val seats: Int,
    val pricePerSeat: Int,
    val womenOnly: Boolean,
    val requestId: String,
)

interface RidesRepository {
    suspend fun quote(input: QuoteInput): AppResult<Quote>
    suspend fun create(input: CreateRideInput): AppResult<String>
    suspend fun cancel(rideId: String, reason: CancelRideReason): AppResult<Int>
    fun observeMyRides(): Flow<List<Ride>>
    fun observeRide(rideId: String): Flow<Ride?>
    fun observeBookings(rideId: String): Flow<List<Booking>>
}
