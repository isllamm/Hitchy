package com.hitchy.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartRideInputDto(
    val rideId: String,
)

@Serializable
data class CheckInInputDto(
    val rideId: String,
    val lat: Double,
    val lng: Double,
    @Serializable(with = JsNumberDoubleSerializer::class)
    val accuracyM: Double,
)

@Serializable
data class CheckInOutputDto(
    val distanceM: Int,
)

@Serializable
data class MarkBoardedInputDto(
    val rideId: String,
    val riderUid: String,
)

@Serializable
enum class LateKindDto {
    @SerialName("late")
    LATE,

    @SerialName("no_show")
    NO_SHOW,
}

@Serializable
data class ReportLateInputDto(
    val rideId: String,
    val riderUid: String,
    val kind: LateKindDto,
)

@Serializable
data class ReportLateOutputDto(
    val feeId: String,
)

@Serializable
data class FinishRideInputDto(
    val rideId: String,
)

@Serializable
data class FinishRideOutputDto(
    val completedBookings: Int,
    val creditUsed: Boolean,
)

// ---- v1.2 demo changes (BUILD_PLAN 8.16) ----

/** The owner taps "I'm here" at a pickup, starting the free wait for the riders there. */
@Serializable
data class ArriveAtStopInputDto(
    val rideId: String,
    /** "start" or a stop id ("s1".."s3"). */
    val pickupId: String,
)

@Serializable
data class ArriveAtStopOutputDto(
    val bookings: Int,
)

/** Confirm Payment: pays every boarded rider's fare and late fee from their wallet. */
@Serializable
data class ConfirmRidePaymentInputDto(
    val rideId: String,
    val requestId: String,
)

@Serializable
data class ConfirmRidePaymentOutputDto(
    val paidBookings: Int,
    val total: Int,
)
