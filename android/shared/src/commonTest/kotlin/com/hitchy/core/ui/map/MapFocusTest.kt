package com.hitchy.core.ui.map

import com.hitchy.core.model.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals

class MapFocusTest {
    private val a = GeoPoint(lat = 30.0, lng = 31.0)
    private val b = GeoPoint(lat = 30.2, lng = 31.4)
    private val c = GeoPoint(lat = 29.9, lng = 31.2)

    @Test
    fun noPointsShowsGreaterCairo() {
        assertEquals(MapFocus.Default, mapFocusFor(emptyList()))
    }

    @Test
    fun onePointCentresOnIt() {
        assertEquals(MapFocus.Single(a), mapFocusFor(listOf(a)))
    }

    @Test
    fun theSamePointTwiceIsOnePoint() {
        assertEquals(MapFocus.Single(a), mapFocusFor(listOf(a, a)))
    }

    @Test
    fun manyPointsFitTheirBounds() {
        val expected = MapFocus.Fit(
            southWest = GeoPoint(lat = 29.9, lng = 31.0),
            northEast = GeoPoint(lat = 30.2, lng = 31.4),
        )
        assertEquals(expected, mapFocusFor(listOf(a, b, c)))
    }

    @Test
    fun theOrderOfThePointsDoesNotMatter() {
        assertEquals(mapFocusFor(listOf(a, b, c)), mapFocusFor(listOf(c, a, b)))
    }

    @Test
    fun theDefaultCentreIsInGreaterCairo() {
        assertEquals(30.0444, MapDefaults.center.lat)
        assertEquals(31.2357, MapDefaults.center.lng)
    }
}
