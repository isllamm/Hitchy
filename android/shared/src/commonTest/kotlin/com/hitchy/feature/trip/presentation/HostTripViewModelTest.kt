package com.hitchy.feature.trip.presentation

import com.hitchy.core.model.BookingStatus
import com.hitchy.core.platform.TestClock
import com.hitchy.feature.booking.FakeBookingRepository
import com.hitchy.feature.profile.FakeWalletRepository
import com.hitchy.feature.rides.FakeRidesRepository
import com.hitchy.feature.trip.FakeTripRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class HostTripViewModelTest {

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
    fun hostTripProgressesThroughAllPhases() = runTest(testDispatcher) {
        val ridesRepo = FakeRidesRepository()
        val tripRepo = FakeTripRepository()
        val walletRepo = FakeWalletRepository(initialBalance = 100)
        val ride = FakeRidesRepository.sampleRide(id = "ride-1")
        val booking = FakeBookingRepository.sampleBooking(
            rideId = "ride-1",
            riderUid = "rider-1",
            status = BookingStatus.BOOKED,
        )
        ridesRepo.ridesById.value = mapOf("ride-1" to ride)
        ridesRepo.bookingsByRideId.value = mapOf("ride-1" to listOf(booking))

        val vm = HostTripViewModel(ridesRepo, tripRepo, walletRepo, clock)
        vm.onAction(HostTripAction.Init("ride-1"))
        advanceUntilIdle()

        // 1. Initial phase
        assertEquals(HostTripPhase.LEAVING, vm.state.value.phase)
        assertEquals(1, vm.state.value.bookings.size)

        // 2. Start Trip -> DRIVING_TO_STOP
        vm.onAction(HostTripAction.StartTrip)
        advanceUntilIdle()
        assertEquals(HostTripPhase.DRIVING_TO_STOP, vm.state.value.phase)

        // 3. Arrive At Stop -> AT_STOP
        vm.onAction(HostTripAction.ArriveAtStop)
        advanceUntilIdle()
        assertEquals(HostTripPhase.AT_STOP, vm.state.value.phase)
        assertFalse(vm.state.value.isFreeWaitOver)

        // Advance clock past 5 minutes (310 seconds)
        clock.current += 310.seconds
        advanceTimeBy(310_000)
        assertTrue(vm.state.value.isFreeWaitOver)

        // 4. Mark Boarded
        vm.onAction(HostTripAction.MarkBoarded("rider-1"))
        advanceUntilIdle()
        assertEquals(1, vm.state.value.boardedCount)

        // 5. Next Stop / En Route
        vm.onAction(HostTripAction.NextStopOrFinish)
        advanceUntilIdle()
        assertEquals(HostTripPhase.EN_ROUTE, vm.state.value.phase)

        // 6. Finish Trip -> ARRIVED
        vm.onAction(HostTripAction.FinishTrip)
        advanceUntilIdle()
        assertEquals(HostTripPhase.ARRIVED, vm.state.value.phase)

        // 7. Confirm Payment -> PAYMENT_DONE
        vm.onAction(HostTripAction.ConfirmPayment)
        advanceUntilIdle()
        assertEquals(HostTripPhase.PAYMENT_DONE, vm.state.value.phase)
        // The backend accepts only a UUID as the request id.
        assertTrue(UUID_PATTERN.matches(tripRepo.lastConfirmRequestId.orEmpty()))
        assertEquals(35, vm.state.value.paymentTotalCollected)
        assertEquals(1, vm.state.value.paymentBookingsCount)
    }
}

private val UUID_PATTERN = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
