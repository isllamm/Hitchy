package com.hitchy.core.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class ClockTest {

    @Test
    fun systemClockReturnsCurrentInstant() {
        val clock: AppClock = kotlin.time.Clock.System
        val before = clock.now()
        val after = clock.now()
        assertTrue(after >= before)
    }

    @Test
    fun testClockReturnsInjectedInstantAndCanAdvance() {
        val start = Instant.fromEpochMilliseconds(1761800000000L)
        val clock = TestClock(start)
        assertEquals(start, clock.now())

        val advanced = start + 2.hours + 30.seconds
        clock.current = advanced
        assertEquals(advanced, clock.now())
    }
}
