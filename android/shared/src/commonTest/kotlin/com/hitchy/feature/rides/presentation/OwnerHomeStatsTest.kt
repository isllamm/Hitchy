package com.hitchy.feature.rides.presentation

import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.RideStatus
import com.hitchy.feature.booking.FakeBookingRepository
import com.hitchy.feature.inbox.FakeInboxRepository
import com.hitchy.feature.profile.FakeProfileRepository
import com.hitchy.feature.rides.FakeRidesRepository
import com.hitchy.feature.rides.ui.groupDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OwnerHomeStatsTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun statsCountThisMonthsFinishedRidesAndTheActiveOnes() {
        val rides = listOf(
            FakeRidesRepository.sampleRide(id = "a", status = RideStatus.FINISHED, seatsTotal = 3, seatsLeft = 1),
            FakeRidesRepository.sampleRide(id = "b", status = RideStatus.OPEN, seatsTotal = 4, seatsLeft = 1),
            FakeRidesRepository.sampleRide(id = "c", status = RideStatus.CANCELLED, seatsTotal = 3, seatsLeft = 0),
        )

        val stats = ownerHomeStats(rides, monthKey = "2026-10")

        assertEquals(OwnerHomeStats(monthEarningsEgp = 70, monthSeats = 2, activeRides = 1, bookedSeats = 3), stats)
        assertEquals(0, ownerHomeStats(rides, monthKey = "2026-11").monthEarningsEgp)
    }

    @Test
    fun heldRequestSeatsAreNotBooked() {
        val ride = FakeRidesRepository.sampleRide(id = "b", status = RideStatus.OPEN, seatsTotal = 3, seatsLeft = 0)
        val rides = listOf(ride)

        val stats = ownerHomeStats(rides, monthKey = "2026-10", pendingSeatsByRide = mapOf("b" to 2))

        assertEquals(1, stats.bookedSeats)
    }

    @Test
    fun groupDigitsWritesThousandsLikeFigma() {
        assertEquals("0", groupDigits(0))
        assertEquals("999", groupDigits(999))
        assertEquals("1,296", groupDigits(1296))
        assertEquals("1,000,000", groupDigits(1_000_000))
    }

    @Test
    fun ownerHomeCountsRequestsAndPoints() = runTest(testDispatcher) {
        val profileRepo = FakeProfileRepository(
            initialUser = FakeProfileRepository.sampleUser(firstName = "Ahmed").copy(pointsBalance = 200),
        )
        val ridesRepo = FakeRidesRepository()
        ridesRepo.myRidesFlow.value = listOf(FakeRidesRepository.sampleRide(id = "ride-1"))
        ridesRepo.bookingsByRideId.value = mapOf(
            "ride-1" to listOf(
                FakeBookingRepository.sampleBooking(status = BookingStatus.REQUESTED),
                FakeBookingRepository.sampleBooking(riderUid = "rider-2", status = BookingStatus.REQUESTED),
                FakeBookingRepository.sampleBooking(riderUid = "rider-3", status = BookingStatus.BOOKED),
            ),
        )

        val vm = OwnerHomeViewModel(profileRepo, ridesRepo, FakeInboxRepository())
        advanceUntilIdle()

        assertEquals(200, vm.state.value.pointsBalance)
        assertEquals(mapOf("ride-1" to 2), vm.state.value.pendingRequestsCountByRide)
        assertEquals(mapOf("ride-1" to 2), vm.state.value.pendingSeatsByRide)
    }
}
