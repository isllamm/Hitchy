package com.hitchy.core.platform

import com.hitchy.core.model.GeoPoint

/** Finds the path along the streets between points, so the map draws a route and not a straight line. */
interface StreetRouteProvider {
    /** The street path through [points] in order, or null when it cannot be found (then a straight line is drawn). */
    suspend fun routeThrough(points: List<GeoPoint>): List<GeoPoint>?
}
