package com.hitchy.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestSeatsInputDto(
    val rideId: String,
    val seats: Int,
    val requestId: String,
)

@Serializable
data class RequestedBookingDto(
    val rideId: String,
    val status: BookingStatusDto,
    val seats: Int,
    val price: Int,
    val total: Int,
    val expiresAt: String,
)

@Serializable
data class RequestSeatsOutputDto(
    val booking: RequestedBookingDto,
    val ride: RideCardDto,
)

@Serializable
data class RespondToRequestInputDto(
    val rideId: String,
    val riderUid: String,
    val accept: Boolean,
)

@Serializable
data class RespondToRequestOutputDto(
    val status: BookingStatusDto,
)

@Serializable
enum class CancelBookingReasonDto {
    @SerialName("host_late")
    HOST_LATE,

    @SerialName("plans_changed")
    PLANS_CHANGED,

    @SerialName("found_another_ride")
    FOUND_ANOTHER_RIDE,

    @SerialName("other")
    OTHER,
}

@Serializable
data class CancelBookingInputDto(
    val rideId: String,
    val reason: CancelBookingReasonDto,
)

@Serializable
data class CancelBookingOutputDto(
    val lateCancel: Boolean,
)

@Serializable
data class ConfirmSeatPaymentInputDto(
    val rideId: String,
    val riderUid: String,
)
