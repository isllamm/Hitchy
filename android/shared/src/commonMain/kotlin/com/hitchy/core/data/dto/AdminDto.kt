package com.hitchy.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Admin-only callable DTOs mirroring `backend/functions/src/schemas/`.
 *
 * The Android app never calls these endpoints in production; they are defined solely so the
 * shared contract test suite strictly verifies every fixture pair in `contract/fixtures/`.
 */

@Serializable
data class ApproveHostInputDto(
    val uid: String,
    val vehicleId: String,
)

@Serializable
data class ApproveHostOutputDto(
    val freeUntil: String,
)

@Serializable
enum class KycRejectReasonDto {
    @SerialName("blurry")
    BLURRY,

    @SerialName("expired")
    EXPIRED,

    @SerialName("mismatch")
    MISMATCH,

    @SerialName("missing")
    MISSING,

    @SerialName("other")
    OTHER,
}

@Serializable
data class KycRejectionDto(
    val doc: KycDocKeyDto,
    val reason: KycRejectReasonDto,
)

@Serializable
data class RejectHostInputDto(
    val uid: String,
    val rejections: List<KycRejectionDto>,
    val note: String?,
)

@Serializable
data class ApproveVehicleInputDto(
    val uid: String,
    val vehicleId: String,
    val approve: Boolean,
)

@Serializable
data class SetVerifiedInputDto(
    val uid: String,
    val verified: Boolean,
)

@Serializable
data class SetGenderInputDto(
    val uid: String,
    val gender: GenderDto,
)

@Serializable
enum class FeeDecisionDto {
    @SerialName("keep")
    KEEP,

    @SerialName("waive")
    WAIVE,
}

@Serializable
data class ResolveFeeInputDto(
    val feeId: String,
    val decision: FeeDecisionDto,
    val note: String?,
)

@Serializable
enum class ConfigNameDto {
    @SerialName("pricing")
    PRICING,

    @SerialName("packages")
    PACKAGES,

    @SerialName("limits")
    LIMITS,

    @SerialName("rewards")
    REWARDS,

    @SerialName("impact")
    IMPACT,
}

@Serializable
data class UpdateConfigInputDto(
    val name: ConfigNameDto,
    val value: Map<String, JsonElement>,
)

@Serializable
data class UpdateConfigOutputDto(
    val version: Int,
)

/** Admin adds balance to a user's wallet (v1.2, R19). */
@Serializable
data class TopUpWalletInputDto(
    val uid: String,
    val amount: Int,
    val requestId: String,
)

@Serializable
data class TopUpWalletOutputDto(
    val balance: Int,
)
