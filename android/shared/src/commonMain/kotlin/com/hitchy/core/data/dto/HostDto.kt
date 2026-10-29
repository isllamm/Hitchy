package com.hitchy.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddVehicleInputDto(
    val type: VehicleTypeDto,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val plate: String,
)

@Serializable
data class AddVehicleOutputDto(
    val vehicleId: String,
)

@Serializable
data class SubmitHostApplicationInputDto(
    val vehicleId: String,
)

@Serializable
data class SubmitHostApplicationOutputDto(
    val status: HostStatusDto,
)

@Serializable
class RequestVerificationInputDto {
    override fun equals(other: Any?): Boolean = other is RequestVerificationInputDto

    override fun hashCode(): Int = RequestVerificationInputDto::class.hashCode()
}
