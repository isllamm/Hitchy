package com.hitchy.core.platform

import com.hitchy.core.model.GeoPoint

class FakeLocationProvider(
    var permissionGranted: Boolean = true,
    var currentLocation: GeoPoint? = GeoPoint(lat = 30.0444, lng = 31.2357),
) : LocationProvider {

    override fun hasPermission(): Boolean = permissionGranted

    override suspend fun current(): GeoPoint? {
        if (!permissionGranted) return null
        return currentLocation
    }
}
