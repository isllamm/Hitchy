package com.hitchy.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RatingTagDto {
    @SerialName("on_time")
    ON_TIME,

    @SerialName("safe_driving")
    SAFE_DRIVING,

    @SerialName("clean_car")
    CLEAN_CAR,

    @SerialName("friendly")
    FRIENDLY,

    @SerialName("followed_route")
    FOLLOWED_ROUTE,

    @SerialName("late")
    LATE,

    @SerialName("unsafe_driving")
    UNSAFE_DRIVING,

    @SerialName("rude")
    RUDE,

    @SerialName("polite")
    POLITE,

    @SerialName("easy_pickup")
    EASY_PICKUP,
}

@Serializable
data class SubmitRatingInputDto(
    val rideId: String,
    val toUid: String,
    val stars: Int,
    val tags: List<RatingTagDto>,
)
