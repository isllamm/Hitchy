package com.hitchy.core.platform

class FakeTripTracker : TripTracker {
    var activeRideId: String? = null
    var isTracking: Boolean = false

    override fun start(rideId: String) {
        activeRideId = rideId
        isTracking = true
    }

    override fun stop() {
        activeRideId = null
        isTracking = false
    }
}
