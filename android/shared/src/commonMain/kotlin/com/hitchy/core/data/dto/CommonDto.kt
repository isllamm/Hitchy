package com.hitchy.core.data.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Shared contract DTOs and enums mirroring `backend/functions/src/schemas/`.
 *
 * DTO optionality rule: a field the zod schema requires has no Kotlin default (including required
 * `.nullable()` fields, which serialize explicit `null`); an optional zod field is nullable with
 * `= null` so `encodeDefaults = false` omits it when unset.
 */
@Serializable
class EmptyOutputDto {
    override fun equals(other: Any?): Boolean = other is EmptyOutputDto

    override fun hashCode(): Int = EmptyOutputDto::class.hashCode()
}

@Serializable
data class LocalizedNameDto(
    val ar: String,
    val en: String,
)

@Serializable
data class WorkHoursDto(
    val start: String,
    val end: String,
)

@Serializable
enum class GenderDto {
    @SerialName("female")
    FEMALE,

    @SerialName("male")
    MALE,
}

@Serializable
enum class VehicleTypeDto {
    @SerialName("car")
    CAR,

    @SerialName("scooter")
    SCOOTER,
}

@Serializable
enum class DirectionDto {
    @SerialName("to_work")
    TO_WORK,

    @SerialName("to_home")
    TO_HOME,
}

@Serializable
enum class TierDto {
    @SerialName("bronze")
    BRONZE,

    @SerialName("silver")
    SILVER,

    @SerialName("gold")
    GOLD,
}

@Serializable
enum class TrafficLevelDto {
    @SerialName("offPeak")
    OFF_PEAK,

    @SerialName("normal")
    NORMAL,

    @SerialName("peak")
    PEAK,
}

@Serializable
enum class HostStatusDto {
    @SerialName("none")
    NONE,

    @SerialName("submitted")
    SUBMITTED,

    @SerialName("approved")
    APPROVED,

    @SerialName("rejected")
    REJECTED,
}

@Serializable
enum class RideStatusDto {
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
enum class BookingStatusDto {
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
enum class KycDocKeyDto {
    @SerialName("idFront")
    ID_FRONT,

    @SerialName("idBack")
    ID_BACK,

    @SerialName("drivingLicense")
    DRIVING_LICENSE,

    @SerialName("vehicleLicense")
    VEHICLE_LICENSE,

    @SerialName("criminalRecord")
    CRIMINAL_RECORD,
}

/**
 * Serializes [Double] values using JavaScript `JSON.stringify` number formatting: whole numbers
 * encode without a trailing `.0` (e.g. `10.0` -> `10`), while fractional numbers encode as
 * standard decimals.
 */
internal object JsNumberDoubleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("JsNumberDouble", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: Double) {
        if (value.isFinite() && value % 1.0 == 0.0) {
            encoder.encodeLong(value.toLong())
        } else {
            encoder.encodeDouble(value)
        }
    }

    override fun deserialize(decoder: Decoder): Double = decoder.decodeDouble()
}
