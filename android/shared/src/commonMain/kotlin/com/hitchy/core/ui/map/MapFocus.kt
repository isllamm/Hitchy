package com.hitchy.core.ui.map

import com.hitchy.core.model.GeoPoint

private const val CAIRO_CENTER_LAT = 30.0444
private const val CAIRO_CENTER_LNG = 31.2357

object MapDefaults {
    /** The Cairo city centre. Shown only while a map has no points. */
    val center: GeoPoint = GeoPoint(lat = CAIRO_CENTER_LAT, lng = CAIRO_CENTER_LNG)
    const val ZOOM: Float = 10f
    const val SINGLE_POINT_ZOOM: Float = 15f
}

/** The part of the map the camera should show. Pure logic, so it is tested without a device. */
sealed interface MapFocus {
    /** No points: show Greater Cairo. */
    data object Default : MapFocus

    /** One point: centre on it. */
    data class Single(val point: GeoPoint) : MapFocus

    /** Two or more points: fit them all. */
    data class Fit(val southWest: GeoPoint, val northEast: GeoPoint) : MapFocus
}

fun mapFocusFor(points: List<GeoPoint>): MapFocus {
    val distinct = points.distinct()
    return when (distinct.size) {
        0 -> MapFocus.Default

        1 -> MapFocus.Single(distinct.single())

        else -> MapFocus.Fit(
            southWest = GeoPoint(lat = distinct.minOf { it.lat }, lng = distinct.minOf { it.lng }),
            northEast = GeoPoint(lat = distinct.maxOf { it.lat }, lng = distinct.maxOf { it.lng }),
        )
    }
}
