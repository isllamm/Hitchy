package com.hitchy.feature.trip.presentation

import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.CancelBookingReason
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.platform.FakeLocationProvider
import com.hitchy.core.ui.components.TripStep
import com.hitchy.feature.booking.FakeBookingRepository
import com.hitchy.feature.profile.FakeWalletRepository
import com.hitchy.feature.rides.FakeRidesRepository
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.trip.FakeTripRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RiderTripViewModelTest {

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
    fun riderTripAdvancesStepsCorrectly() = runTest(testDispatcher) {
        val bookingRepo = FakeBookingRepository()
        val ridesRepo = FakeRidesRepository()
        val tripRepo = FakeTripRepository()
        val walletRepo = FakeWalletRepository(initialBalance = 150)
        val locationProvider = FakeLocationProvider()

        val vm = RiderTripViewModel(bookingRepo, ridesRepo, tripRepo, walletRepo, locationProvider)

        val sampleBooking = sampleBooking(status = BookingStatus.BOOKED)
        bookingRepo.bookingsByRideId.value = mapOf("ride-1" to sampleBooking)

        vm.onAction(RiderTripAction.Init("ride-1"))
        advanceUntilIdle()

        // 1. Owner on the way
        assertEquals(TripStep.OwnerOnTheWay, vm.state.value.tripStep)
        assertTrue(vm.state.value.canCheckIn)
        assertTrue(vm.state.value.canCancel)

        // 2. Rider check in
        vm.onAction(RiderTripAction.CheckIn)
        advanceUntilIdle()
        assertEquals("ride-1", tripRepo.lastCheckedInRideId)

        // 3. Owner arrived
        bookingRepo.bookingsByRideId.value = mapOf(
            "ride-1" to sampleBooking.copy(hostArrivedAt = "2026-10-30T08:02:00Z"),
        )
        advanceUntilIdle()
        assertEquals(TripStep.Arrived, vm.state.value.tripStep)

        // 4. Boarded
        bookingRepo.bookingsByRideId.value = mapOf(
            "ride-1" to sampleBooking.copy(
                hostArrivedAt = "2026-10-30T08:02:00Z",
                boardedAt = "2026-10-30T08:04:00Z",
            ),
        )
        advanceUntilIdle()
        assertEquals(TripStep.InRide, vm.state.value.tripStep)
        assertFalse(vm.state.value.canCancel)

        // 5. Completed
        bookingRepo.bookingsByRideId.value = mapOf(
            "ride-1" to sampleBooking.copy(
                status = BookingStatus.COMPLETED,
                hostArrivedAt = "2026-10-30T08:02:00Z",
                boardedAt = "2026-10-30T08:04:00Z",
                paymentConfirmedAt = "2026-10-30T08:50:00Z",
            ),
        )
        advanceUntilIdle()
        assertEquals(TripStep.Completed, vm.state.value.tripStep)
    }

    @Test
    fun riderTripCancelsBooking() = runTest(testDispatcher) {
        val bookingRepo = FakeBookingRepository()
        val ridesRepo = FakeRidesRepository()
        val tripRepo = FakeTripRepository()
        val walletRepo = FakeWalletRepository()
        val locationProvider = FakeLocationProvider()

        val vm = RiderTripViewModel(bookingRepo, ridesRepo, tripRepo, walletRepo, locationProvider)
        bookingRepo.bookingsByRideId.value = mapOf("ride-1" to sampleBooking(status = BookingStatus.BOOKED))

        vm.onAction(RiderTripAction.Init("ride-1"))
        advanceUntilIdle()

        vm.onAction(RiderTripAction.SelectCancelReason(CancelBookingReason.HOST_LATE))
        vm.onAction(RiderTripAction.ConfirmCancel)
        advanceUntilIdle()

        assertEquals("ride-1", bookingRepo.lastCancelledRideId)
        assertEquals(CancelBookingReason.HOST_LATE, bookingRepo.lastCancelReason)
        assertTrue(vm.state.value.isCancelled)
    }

    private fun sampleBooking(
        status: BookingStatus = BookingStatus.BOOKED,
    ): Booking = Booking(
        rideId = "ride-1",
        riderUid = "rider-1",
        hostId = "host-1",
        departAt = "2026-10-30T08:00:00Z",
        status = status,
        seats = 1,
        price = 35,
        total = 35,
        pickupId = "start",
        pickupPointName = LocalizedName(ar = "دجلة", en = "Degla"),
        pickupPoint = GeoPoint(lat = 29.96, lng = 31.28),
        pickupAt = "2026-10-30T08:00:00Z",
        expiresAt = "2026-10-30T08:15:00Z",
        respondedAt = null,
        checkedInAt = null,
        checkInDistanceM = null,
        hostArrivedAt = null,
        boardedAt = null,
        lateFeeId = null,
        paymentConfirmedAt = null,
        verifiedTogether = false,
        rewarded = false,
        lateCancel = false,
        cancelledAt = null,
        cancelReason = null,
    )
}
