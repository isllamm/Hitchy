package com.hitchy.feature.trip.presentation

import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.platform.TestClock
import com.hitchy.feature.booking.FakeBookingRepository
import com.hitchy.feature.profile.FakeProfileRepository
import com.hitchy.feature.profile.FakeWalletRepository
import com.hitchy.feature.rides.FakeRidesRepository
import com.hitchy.feature.rides.domain.RideStop
import com.hitchy.feature.trip.FakeTripRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class HostTripPickupOrderTest {

    private val testDispatcher = StandardTestDispatcher()
    private val clock = TestClock(Instant.parse("2026-10-30T08:00:00Z"))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun theOwnerStopsAtTheStartThenAtEachStopWithRiders() = runTest(testDispatcher) {
        val stop = { id: String ->
            RideStop(id, "maadi", "p-$id", LocalizedName(ar = "توقف", en = "Stop $id"), GeoPoint(29.9, 31.2), "")
        }
        val ride = FakeRidesRepository.sampleRide(id = "ride-1").copy(stops = listOf(stop("s1"), stop("s2")))
        val atStart = FakeBookingRepository.sampleBooking(riderUid = "r1", status = BookingStatus.BOOKED)
        val atSecondStop = FakeBookingRepository.sampleBooking(
            riderUid = "r2",
            status = BookingStatus.BOOKED,
            pickupId = "s2",
        )
        val ridesRepo = FakeRidesRepository()
        ridesRepo.ridesById.value = mapOf("ride-1" to ride)
        ridesRepo.bookingsByRideId.value = mapOf("ride-1" to listOf(atStart, atSecondStop))
        val tripRepo = FakeTripRepository()
        val vm = HostTripViewModel(ridesRepo, tripRepo, FakeWalletRepository(), FakeProfileRepository(), clock)
        vm.onAction(HostTripAction.Init("ride-1"))
        advanceUntilIdle()

        vm.onAction(HostTripAction.StartTrip)
        advanceUntilIdle()
        assertEquals("start", vm.state.value.currentPickupId)

        vm.onAction(HostTripAction.ArriveAtStop)
        runCurrent()
        vm.onAction(HostTripAction.MarkBoarded("r1"))
        runCurrent()
        val boarded = atStart.copy(status = BookingStatus.BOARDED, boardedAt = "2026-10-30T08:01:00Z")
        ridesRepo.bookingsByRideId.value = mapOf("ride-1" to listOf(boarded, atSecondStop))
        runCurrent()
        // A boarded rider stays on the list and counts as on board.
        assertEquals(2, vm.state.value.bookings.size)
        assertEquals(1, vm.state.value.boardedCount)

        // s1 has nobody to board, so the owner goes straight to s2.
        vm.onAction(HostTripAction.NextStopOrFinish)
        runCurrent()
        assertEquals("s2", vm.state.value.currentPickupId)
        vm.onAction(HostTripAction.ArriveAtStop)
        runCurrent()

        assertEquals(listOf("start", "s2"), tripRepo.arrivedPickups)

        // Moving on stops the free-wait timer, so the test can end.
        vm.onAction(HostTripAction.NextStopOrFinish)
        runCurrent()
        assertEquals(HostTripPhase.EN_ROUTE, vm.state.value.phase)
    }
}
