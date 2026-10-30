package com.hitchy.core.util

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val NO_TIME = "--:--"

/** Cairo "HH:mm" for an ISO-8601 instant string; "--:--" when it can't be parsed. */
@OptIn(ExperimentalTime::class)
fun isoToCairoTime(iso: String?): String {
    if (iso.isNullOrBlank()) return NO_TIME
    return runCatching { formatTime(Instant.parse(iso)) }.getOrDefault(NO_TIME)
}

/** Cairo "YYYY-MM-DD" for an ISO-8601 instant string; empty when it can't be parsed. */
@OptIn(ExperimentalTime::class)
fun isoToCairoDateKey(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching { dateKey(Instant.parse(iso)) }.getOrDefault("")
}

/** Epoch milliseconds for an ISO-8601 instant string; 0 when it can't be parsed. */
@OptIn(ExperimentalTime::class)
fun isoToEpochMs(iso: String?): Long {
    if (iso.isNullOrBlank()) return 0L
    return runCatching { Instant.parse(iso).toEpochMilliseconds() }.getOrDefault(0L)
}
