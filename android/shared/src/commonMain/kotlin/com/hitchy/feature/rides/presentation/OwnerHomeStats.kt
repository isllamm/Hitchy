package com.hitchy.feature.rides.presentation

import com.hitchy.core.model.RideStatus
import com.hitchy.feature.rides.domain.Ride

/** The numbers on Owner Home (Figma 726:11671): this month's earnings card and the four stat cards. */
data class OwnerHomeStats(
    val monthEarningsEgp: Int,
    val monthSeats: Int,
    val activeRides: Int,
    val bookedSeats: Int,
)

/**
 * Builds the Owner Home numbers from the owner's rides. [monthKey] is the Cairo month, "YYYY-MM".
 * Earnings count the booked seats of the rides finished this month at their seat price; the active
 * rides are the open, full and started ones, with their booked seats.
 */
fun ownerHomeStats(rides: List<Ride>, monthKey: String): OwnerHomeStats {
    val finished = rides.filter { it.status == RideStatus.FINISHED && it.dateKey.startsWith(monthKey) }
    val active = rides.filter {
        it.status == RideStatus.OPEN || it.status == RideStatus.FULL || it.status == RideStatus.STARTED
    }
    return OwnerHomeStats(
        monthEarningsEgp = finished.sumOf { it.bookedSeats() * it.pricePerSeat },
        monthSeats = finished.sumOf { it.bookedSeats() },
        activeRides = active.size,
        bookedSeats = active.sumOf { it.bookedSeats() },
    )
}

private fun Ride.bookedSeats(): Int = (seatsTotal - seatsLeft).coerceAtLeast(0)
