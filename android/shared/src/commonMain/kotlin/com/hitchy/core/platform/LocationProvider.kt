package com.hitchy.core.platform

import com.hitchy.core.model.GeoPoint

interface LocationProvider {
    fun hasPermission(): Boolean
    suspend fun current(): GeoPoint?
}
