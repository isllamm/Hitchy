package com.hitchy.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendChatMessageInputDto(
    val rideId: String,
    val riderUid: String,
    val text: String,
    val requestId: String,
)

@Serializable
data class SendChatMessageOutputDto(
    val messageId: String,
)

@Serializable
data class GetContactPhoneInputDto(
    val rideId: String,
    val otherUid: String,
)

@Serializable
data class GetContactPhoneOutputDto(
    val phone: String,
)
