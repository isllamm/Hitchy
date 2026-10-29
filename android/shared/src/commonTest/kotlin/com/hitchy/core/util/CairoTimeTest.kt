package com.hitchy.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.offsetIn

@OptIn(ExperimentalTime::class)
class CairoTimeTest {

    private data class OffsetChange(
        val at: Instant,
        val from: Int, // minutes east of UTC
        val to: Int
    )

    // Finds when Cairo's UTC offset changes in a year, from time zone data,
    // so the summer time dates are never hard-coded.
    private fun offsetChanges(year: Int): List<OffsetChange> {
        fun offsetMinutesAt(instant: Instant): Int =
            instant.offsetIn(CairoTimeZone).totalSeconds / 60

        val found = mutableListOf<OffsetChange>()
        val start = Instant.parse("$year-01-01T00:00:00Z")
        val end = Instant.parse("${year + 1}-01-01T00:00:00Z")

        var t = start
        while (t < end) {
            val next = t + 1.hours
            if (offsetMinutesAt(t) != offsetMinutesAt(next)) {
                var at = t
                val currentOffset = offsetMinutesAt(t)
                while (offsetMinutesAt(at) == currentOffset) {
                    at += 1.minutes
                }
                found.add(OffsetChange(at = at, from = currentOffset, to = offsetMinutesAt(at)))
            }
            t = next
        }
        return found
    }

    private val changes2026: List<OffsetChange> by lazy { offsetChanges(2026) }

    private fun change(kind: String): Instant {
        val c = changes2026.firstOrNull { if (kind == "start") it.to > it.from else it.to < it.from }
            ?: throw IllegalStateException("No summer time $kind found in 2026")
        return c.at
    }

    @Test
    fun findsEgyptsTwoSummerTimeChangesIn2026() {
        assertEquals(2, changes2026.size)
    }

    @Test
    fun readsPlainWinterInstant() {
        val t = Instant.parse("2026-11-02T05:30:00Z")
        assertEquals("2026-11-02", dateKey(t))
        assertEquals(7 * 60 + 30, minuteOfDay(t))
        assertEquals("07:30", formatTime(t))
    }

    @Test
    fun handlesSummerTimeStartWhenMidnightJumpsToOneAm() {
        val t = change("start")

        // dateKey moves to the new day at the jump
        assertNotEquals(dateKey(t - 1.milliseconds), dateKey(t))

        // minuteOfDay goes from 23:59 to 01:00
        assertEquals(23 * 60 + 59, minuteOfDay(t - 1.minutes))
        assertEquals(60, minuteOfDay(t))

        // the day starts at 01:00
        assertEquals(t, startOfCairoDay(t + 11.hours))

        // a skipped time moves forward by the skipped hour
        assertEquals(t + 30.minutes, cairoDateTime(dateKey(t), "00:30"))
        assertEquals(t + 30.minutes, cairoDateTime(dateKey(t), "01:30"))

        // that week is one hour short
        val week = startOfCairoWeek(t)
        val next = startOfCairoWeek(week + 8.days)
        assertEquals(7.days - 1.hours, next - week)
    }

    @Test
    fun handlesSummerTimeEndWhenMidnightGoesBackToElevenPm() {
        val t = change("end")

        // dateKey stays on the same day through the repeated hour
        assertEquals(dateKey(t - 1.milliseconds), dateKey(t))

        // minuteOfDay goes from 23:59 back to 23:00
        assertEquals(23 * 60 + 59, minuteOfDay(t - 1.minutes))
        assertEquals(23 * 60, minuteOfDay(t))

        // the next day starts an hour later
        assertNotEquals(dateKey(t + 1.hours), dateKey(t))
        assertEquals(0, minuteOfDay(t + 1.hours))
        assertEquals(t + 1.hours, startOfCairoDay(t + 2.hours))

        // that week is one hour long
        val week = startOfCairoWeek(t)
        val next = startOfCairoWeek(week + 8.days)
        assertEquals(7.days + 1.hours, next - week)
    }

    @Test
    fun startsEveryWeekOf2026OnSundayAtMidnight() {
        var day = Instant.parse("2026-01-01T12:00:00Z")
        val end = Instant.parse("2027-01-01T00:00:00Z")
        while (day < end) {
            val start = startOfCairoWeek(day)
            assertEquals(DayOfWeek.SUNDAY, toCairo(start).dayOfWeek)
            assertEquals(0, minuteOfDay(start))
            assertTrue(start <= day)
            assertTrue(day - start < 7.days + 1.hours)
            day += 1.days
        }
    }

    @Test
    fun hhmmToMinutesConvertsValidTimes() {
        assertEquals(0, hhmmToMinutes("00:00"))
        assertEquals(1439, hhmmToMinutes("23:59"))
        assertEquals(450, hhmmToMinutes("07:30"))
    }

    @Test
    fun hhmmToMinutesRejectsInvalidOrOutOfRangeTimes() {
        assertFailsWith<IndexOutOfBoundsException> { hhmmToMinutes("24:00") }
        assertFailsWith<IndexOutOfBoundsException> { hhmmToMinutes("12:60") }
        assertFailsWith<IllegalArgumentException> { hhmmToMinutes("-1:00") }
        assertFailsWith<IllegalArgumentException> { hhmmToMinutes("invalid") }
        assertFailsWith<IllegalArgumentException> { hhmmToMinutes("7:30") }
    }
}
