package com.hitchy.feature.places.domain

import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import kotlinx.coroutines.flow.Flow

data class Zone(
    val id: String,
    val name: LocalizedName,
    val active: Boolean,
    val order: Int,
)

data class Point(
    val id: String,
    val zoneId: String,
    val name: LocalizedName,
    val location: GeoPoint,
    val active: Boolean,
)

interface PlacesRepository {
    fun observeZones(): Flow<List<Zone>>
    fun observePoints(zoneId: String): Flow<List<Point>>
}
