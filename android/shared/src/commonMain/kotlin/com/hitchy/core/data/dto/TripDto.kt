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
