package com.hitchy.feature.rides.presentation

import com.hitchy.core.model.RideStatus
import com.hitchy.feature.rides.domain.Ride

/** The numbers on Owner Home (Figma 726:11671): this month's earnings card and the four stat cards. */
data class OwnerHomeStats(
    val monthEarningsEgp: Int,
    val monthSeats: Int,
    val activeRides: Int,
    val bookedSeats: Int,
    val monthTrips: Int = 0,
    /** This month's full-car trip cost (recommended seat price x seats), what the earnings cover. */
    val monthTripCostEgp: Int = 0,
    val openSeats: Int = 0,
    val nextRide: Ride? = null,
) {
    /** Share of the month's trip cost the seat payments covered, 0 to 100. */
    val coveredPercent: Int
        get() = if (monthTripCostEgp <= 0) 0 else (monthEarningsEgp * PERCENT / monthTripCostEgp).coerceIn(0, PERCENT)
}

private const val PERCENT = 100

/**
 * Builds the Owner Home numbers from the owner's rides. [monthKey] is the Cairo month, "YYYY-MM".
 * Earnings count the booked seats of the rides finished this month at their seat price; the active
 * rides are the open, full and started ones, with their booked seats (held request seats left out).
 */
fun ownerHomeStats(
    rides: List<Ride>,
    monthKey: String,
    pendingSeatsByRide: Map<String, Int> = emptyMap(),
): OwnerHomeStats {
    val finished = rides.filter { it.status == RideStatus.FINISHED && it.dateKey.startsWith(monthKey) }
    val active = rides.filter {
        it.status == RideStatus.OPEN || it.status == RideStatus.FULL || it.status == RideStatus.STARTED
    }
    // A request holds its seats until the owner answers; those are not booked yet.
    val booked = active.associate { it.id to (it.bookedSeats() - (pendingSeatsByRide[it.id] ?: 0)).coerceAtLeast(0) }
    return OwnerHomeStats(
        monthEarningsEgp = finished.sumOf { it.bookedSeats() * it.pricePerSeat },
        monthSeats = finished.sumOf { it.bookedSeats() },
        activeRides = active.size,
        bookedSeats = booked.values.sum(),
        monthTrips = finished.size,
        monthTripCostEgp = finished.sumOf { it.priceRange.recommended * it.seatsTotal },
        openSeats = active.sumOf { (it.seatsTotal - (booked[it.id] ?: 0)).coerceAtLeast(0) },
        nextRide = active.minByOrNull { it.departAt },
    )
}

private fun Ride.bookedSeats(): Int = (seatsTotal - seatsLeft).coerceAtLeast(0)
