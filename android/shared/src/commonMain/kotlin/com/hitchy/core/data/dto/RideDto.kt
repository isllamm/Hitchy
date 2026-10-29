package com.hitchy.core.data.dto

import com.hitchy.core.model.GeoPoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuotePriceInputDto(
    val vehicleId: String,
    val fromZone: String,
    val toZone: String,
    val departAt: String,
    val seats: Int,
)

@Serializable
data class QuoteResultDto(
    val min: Int,
    val recommended: Int,
    val max: Int,
    val trafficLevel: TrafficLevelDto,
    val distanceKm: Double,
    val fuelCostEgp: Int,
    val tripCostEgp: Int,
    val configVersion: Int,
)

typealias QuotePriceOutputDto = QuoteResultDto

@Serializable
data class CreateRideInputDto(
    val vehicleId: String,
    val fromZone: String,
    val fromPointId: String,
    val toZone: String,
    val toPointId: String,
    val direction: DirectionDto,
    val departAt: String,
    val seats: Int,
    val pricePerSeat: Int,
    val womenOnly: Boolean,
    val requestId: String,
)

@Serializable
data class CreateRideOutputDto(
    val rideId: String,
)

@Serializable
enum class CancelRideReasonDto {
    @SerialName("sick")
    SICK,

    @SerialName("car_problem")
    CAR_PROBLEM,

    @SerialName("plans_changed")
    PLANS_CHANGED,

    @SerialName("other")
    OTHER,
}

@Serializable
data class CancelRideInputDto(
    val rideId: String,
    val reason: CancelRideReasonDto,
)

@Serializable
data class CancelRideOutputDto(
    val cancelledBookings: Int,
)

@Serializable
enum class SearchSortDto {
    @SerialName("recommended")
    RECOMMENDED,

    @SerialName("earliest")
    EARLIEST,

    @SerialName("cheapest")
    CHEAPEST,
}

@Serializable
enum class SearchTimeBandDto {
    @SerialName("any")
    ANY,

    @SerialName("before")
    BEFORE,

    @SerialName("first_hour")
    FIRST_HOUR,

    @SerialName("after")
    AFTER,
}

@Serializable
data class SearchRidesInputDto(
    val fromZone: String,
    val toZone: String,
    val fromPointId: String?,
    val date: String,
    val time: String,
    val seats: Int,
    val sort: SearchSortDto,
    val timeBand: SearchTimeBandDto,
    val maxPrice: Int?,
    val minRating: Double?,
    val verifiedOnly: Boolean,
    val womenOnly: Boolean,
)

@Serializable
data class SearchRidesOutputDto(
    val rides: List<RideCardDto>,
)

@Serializable
data class RideHostDto(
    val uid: String,
    val firstName: String,
    val photoPath: String?,
    val rating: Double?,
    val ratingCount: Int,
    val trips: Int,
    val verified: Boolean,
    val tier: TierDto,
    val sameCompany: Boolean,
)

@Serializable
data class RideCardDto(
    val rideId: String,
    val departAt: String,
    val arriveAt: String,
    val fromZone: String,
    val toZone: String,
    val fromPointId: String,
    val toPointId: String,
    val fromPointName: LocalizedNameDto,
    val toPointName: LocalizedNameDto,
    val pricePerSeat: Int,
    val fairPrice: Boolean,
    val seatsLeft: Int,
    val walkM: Int?,
    val bestMatch: Boolean,
    val womenOnly: Boolean,
    val vehicleType: VehicleTypeDto,
    val fixed: Boolean,
    val host: RideHostDto,
)

@Serializable
data class RideVehicleDto(
    val make: String,
    val model: String,
    val color: String,
    val plate: String,
)

@Serializable
data class RideViewDto(
    val rideId: String,
    val departAt: String,
    val arriveAt: String,
    val fromZone: String,
    val toZone: String,
    val fromPointId: String,
    val toPointId: String,
    val fromPointName: LocalizedNameDto,
    val toPointName: LocalizedNameDto,
    val pricePerSeat: Int,
    val fairPrice: Boolean,
    val seatsLeft: Int,
    val walkM: Int?,
    val bestMatch: Boolean,
    val womenOnly: Boolean,
    val vehicleType: VehicleTypeDto,
    val fixed: Boolean,
    val host: RideHostDto,
    val status: RideStatusDto,
    val fromPoint: GeoPoint,
    val vehicle: RideVehicleDto,
)
