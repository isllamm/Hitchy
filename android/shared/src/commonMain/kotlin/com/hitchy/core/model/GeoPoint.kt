package com.hitchy.core.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoPoint(
    val lat: Double,
    val lng: Double,
)
