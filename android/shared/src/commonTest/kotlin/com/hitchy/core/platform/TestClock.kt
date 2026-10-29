package com.hitchy.core.platform

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TestClock(var current: Instant) : Clock {
    override fun now(): Instant = current
}
