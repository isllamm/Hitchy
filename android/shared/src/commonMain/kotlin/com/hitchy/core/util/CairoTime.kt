package com.hitchy.core.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

const val CAIRO = "Africa/Cairo"
val CairoTimeZone: TimeZone = TimeZone.of(CAIRO)

private const val MINUTES_PER_HOUR = 60
private const val MAX_HOUR = 23
private const val MAX_MINUTE = 59

// Minutes since midnight from "HH:mm", 0 … 1439. Throws if invalid or out of range.
fun hhmmToMinutes(time: String): Int {
    val match = Regex("""^(\d{2}):(\d{2})$""").matchEntire(time)
        ?: throw IllegalArgumentException("Invalid time format, expected \"HH:mm\": $time")
    val hours = match.groupValues[1].toInt()
    val minutes = match.groupValues[2].toInt()
    if (hours !in 0..MAX_HOUR || minutes !in 0..MAX_MINUTE) {
        throw IndexOutOfBoundsException("Time out of range: $time")
    }
    return hours * MINUTES_PER_HOUR + minutes
}

@OptIn(ExperimentalTime::class)
fun toCairo(instant: Instant): LocalDateTime = instant.toLocalDateTime(CairoTimeZone)

// The Cairo calendar date, "YYYY-MM-DD".
@OptIn(ExperimentalTime::class)
fun dateKey(instant: Instant): String = toCairo(instant).date.toString()

// Minutes since Cairo midnight, 0 … 1439.
@OptIn(ExperimentalTime::class)
fun minuteOfDay(instant: Instant): Int {
    val t = toCairo(instant)
    return t.hour * MINUTES_PER_HOUR + t.minute
}

// Formats Cairo time as "HH:mm".
@OptIn(ExperimentalTime::class)
fun formatTime(instant: Instant): String {
    val t = toCairo(instant)
    val h = t.hour.toString().padStart(2, '0')
    val m = t.minute.toString().padStart(2, '0')
    return "$h:$m"
}

// The instant of "HH:mm" on a Cairo date. A time skipped by the summer time change
// moves forward by the skipped hour (luxon's / java.time's rule).
@OptIn(ExperimentalTime::class)
fun cairoDateTime(key: String, time: String): Instant {
    val date = LocalDate.parse(key)
    val match = Regex("""^(\d{2}):(\d{2})$""").matchEntire(time)
        ?: throw IllegalArgumentException("Invalid time format, expected \"HH:mm\": $time")
    val hours = match.groupValues[1].toInt()
    val minutes = match.groupValues[2].toInt()
    if (hours !in 0..MAX_HOUR || minutes !in 0..MAX_MINUTE) {
        throw IndexOutOfBoundsException("Time out of range: $time")
    }
    val local = LocalDateTime(date, LocalTime(hours, minutes))
    return local.toInstant(CairoTimeZone)
}

// The first instant of the Cairo day (01:00 on a day whose midnight is skipped).
@OptIn(ExperimentalTime::class)
fun startOfCairoDay(instant: Instant): Instant {
    val date = toCairo(instant).date
    return date.atStartOfDayIn(CairoTimeZone)
}

// The first instant of the Cairo week. Weeks start on Sunday.
@OptIn(ExperimentalTime::class)
fun startOfCairoWeek(instant: Instant): Instant {
    val cairoDate = toCairo(instant).date
    val daysSinceSunday = cairoDate.dayOfWeek.isoDayNumber % 7
    val sundayDate = cairoDate.minus(DatePeriod(days = daysSinceSunday))
    return sundayDate.atStartOfDayIn(CairoTimeZone)
}
