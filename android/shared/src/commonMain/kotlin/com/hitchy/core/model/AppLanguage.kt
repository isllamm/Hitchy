package com.hitchy.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(val code: String) {
    @SerialName("ar")
    ARABIC("ar"),

    @SerialName("en")
    ENGLISH("en")
}
