package com.hitchy.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    @SerialName("rider")
    RIDER,

    @SerialName("host")
    HOST
}
