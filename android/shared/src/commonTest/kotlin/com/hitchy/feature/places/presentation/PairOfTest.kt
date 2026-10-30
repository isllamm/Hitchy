package com.hitchy.feature.places.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PairOfTest {

    @Test
    fun riderFieldsArePaired() {
        assertEquals("from" to "to", pairOf("from"))
        assertEquals("from" to "to", pairOf("to"))
    }

    @Test
    fun ownerFieldsArePaired() {
        assertEquals("post_ride_from" to "post_ride_to", pairOf("post_ride_from"))
        assertEquals("post_ride_from" to "post_ride_to", pairOf("post_ride_to"))
    }

    @Test
    fun aStopStandsAlone() {
        assertNull(pairOf("post_ride_stop_1"))
    }
}
