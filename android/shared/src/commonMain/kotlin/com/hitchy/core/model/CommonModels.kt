package com.hitchy.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalizedName(
    val ar: String,
    val en: String,
) {
    fun resolve(language: AppLanguage): String = when (language) {
        AppLanguage.ARABIC -> ar
        AppLanguage.ENGLISH -> en
    }
}

@Serializable
data class WorkHours(
    val start: String,
    val end: String,
)

@Serializable
enum class SavedPlaceLabel {
    @SerialName("home")
    HOME,

    @SerialName("work")
    WORK,

    @SerialName("other")
    OTHER,
}

@Serializable
data class SavedPlace(
    val label: SavedPlaceLabel,
    val zoneId: String,
    val pointId: String,
)

@Serializable
enum class Gender {
    @SerialName("female")
    FEMALE,

    @SerialName("male")
    MALE,
}

@Serializable
enum class Tier {
    @SerialName("bronze")
    BRONZE,

    @SerialName("silver")
    SILVER,

    @SerialName("gold")
    GOLD,
}

@Serializable
enum class HostStatus {
    @SerialName("none")
    NONE,

    @SerialName("pending")
    PENDING,

    @SerialName("submitted")
    SUBMITTED,

    @SerialName("approved")
    APPROVED,

    @SerialName("rejected")
    REJECTED,
}

@Serializable
enum class VehicleType {
    @SerialName("car")
    CAR,

    @SerialName("scooter")
    SCOOTER,
}

@Serializable
enum class VehicleStatus {
    @SerialName("pending")
    PENDING,

    @SerialName("approved")
    APPROVED,

    @SerialName("rejected")
    REJECTED,
}

@Serializable
enum class Direction {
    @SerialName("to_work")
    TO_WORK,

    @SerialName("to_home")
    TO_HOME,
}

@Serializable
enum class TrafficLevel {
    @SerialName("offPeak")
    OFF_PEAK,

    @SerialName("normal")
    NORMAL,

    @SerialName("peak")
    PEAK,
}

@Serializable
enum class RideStatus {
    @SerialName("open")
    OPEN,

    @SerialName("full")
    FULL,

    @SerialName("started")
    STARTED,

    @SerialName("finished")
    FINISHED,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("expired")
    EXPIRED,
}

@Serializable
enum class BookingStatus {
    @SerialName("requested")
    REQUESTED,

    @SerialName("booked")
    BOOKED,

    @SerialName("boarded")
    BOARDED,

    @SerialName("completed")
    COMPLETED,

    @SerialName("declined")
    DECLINED,

    @SerialName("expired")
    EXPIRED,

    @SerialName("cancelled_by_rider")
    CANCELLED_BY_RIDER,

    @SerialName("cancelled_by_host")
    CANCELLED_BY_HOST,

    @SerialName("no_show")
    NO_SHOW,
}

@Serializable
enum class CancelRideReason {
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
enum class CancelBookingReason {
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
enum class LateKind {
    @SerialName("late")
    LATE,

    @SerialName("no_show")
    NO_SHOW,
}

@Serializable
enum class SearchSort {
    @SerialName("recommended")
    RECOMMENDED,

    @SerialName("earliest")
    EARLIEST,

    @SerialName("cheapest")
    CHEAPEST,
}

@Serializable
enum class SearchTimeBand {
    @SerialName("any")
    ANY,

    @SerialName("before")
    BEFORE,

    @SerialName("first_hour")
    FIRST_HOUR,

    @SerialName("after")
    AFTER,
}
