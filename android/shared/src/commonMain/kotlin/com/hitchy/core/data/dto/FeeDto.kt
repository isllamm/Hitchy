package com.hitchy.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class DisputeFeeInputDto(
    val feeId: String,
    val text: String,
)

@Serializable
data class MarkFeePaidInputDto(
    val feeId: String,
)
