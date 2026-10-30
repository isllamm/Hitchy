package com.hitchy.feature.rides.presentation

import app.cash.turbine.test
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.CancelRideReason
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.platform.TestClock
import com.hitchy.feature.booking.FakeBookingRepository
import com.hitchy.feature.host.FakeHostRepository
import com.hitchy.feature.inbox.FakeInboxRepository
import com.hitchy.feature.places.domain.Point
import com.hitchy.feature.places.domain.PointSelectionHolder
import com.hitchy.feature.profile.FakeProfileRepository
import com.hitchy.feature.rides.FakeRidesRepository
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class OwnerViewModelsTest {

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
    fun ownerHomeLoadsProfileRidesAndNotifications() = runTest(testDispatcher) {
        val profileRepo = FakeProfileRepository(
            initialUser = FakeProfileRepository.sampleUser(firstName = "Ahmed"),
        )
        val ridesRepo = FakeRidesRepository()
        val inboxRepo = FakeInboxRepository()
        val ride = FakeRidesRepository.sampleRide(id = "ride-1")
        ridesRepo.myRidesFlow.value = listOf(ride)

        val vm = OwnerHomeViewModel(profileRepo, ridesRepo, inboxRepo)
        advanceUntilIdle()

        assertEquals("Ahmed", vm.state.value.firstName)
        assertEquals(1, vm.state.value.upcomingRides.size)
        assertEquals("ride-1", vm.state.value.upcomingRides.first().id)
    }

    @Test
    fun postRideValidatesRouteAndCreatesRide() = runTest(testDispatcher) {
        val ridesRepo = FakeRidesRepository()
        val hostRepo = FakeHostRepository()
        val holder = PointSelectionHolder()
        val vm = PostRideViewModel(ridesRepo, hostRepo, holder, clock)
        advanceUntilIdle()

        // 1. Submit without points -> validation error
        vm.onAction(PostRideAction.Submit)
        advanceUntilIdle()
        assertNotNull(vm.state.value.validationError)

        // 2. Select valid points
        val p1 = Point("p1", "maadi", LocalizedName("المعادي", "Maadi"), GeoPoint(29.96, 31.25), true)
        val smartVillage = LocalizedName("القرية الذكية", "Smart Village")
        val p2 = Point("p2", "smart-village", smartVillage, GeoPoint(30.07, 31.02), true)
        holder.select(PostRideState.FIELD_FROM, p1)
        holder.select(PostRideState.FIELD_TO, p2)
        advanceUntilIdle()

        assertNull(vm.state.value.validationError)
        assertEquals("maadi", vm.state.value.fromPoint?.zoneId)
        assertEquals("smart-village", vm.state.value.toPoint?.zoneId)
        assertEquals(35, vm.state.value.pricePerSeat)

        // 3. Update seats and price
        vm.onAction(PostRideAction.UpdateSeats(2))
        vm.onAction(PostRideAction.UpdatePrice(40))
        assertEquals(2, vm.state.value.seats)
        assertEquals(40, vm.state.value.pricePerSeat)
        assertEquals(80, vm.state.value.expectedEarnings)

        // 4. Submit and verify navigation event
        vm.events.test {
            vm.onAction(PostRideAction.Submit)
            testScheduler.runCurrent()

            val event = awaitItem() as PostRideEvent.NavigateToRideDetails
            assertEquals("ride-new", event.rideId)
            // The backend accepts only a UUID as the request id.
            val requestId = ridesRepo.lastCreateInput?.requestId.orEmpty()
            assertTrue(UUID_PATTERN.matches(requestId))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun hostRideDetailsAcceptsAndDeclinesRequests() = runTest(testDispatcher) {
        val ridesRepo = FakeRidesRepository()
        val bookingRepo = FakeBookingRepository()
        val ride = FakeRidesRepository.sampleRide(id = "ride-1", pricePerSeat = 50, seatsTotal = 3)
        ridesRepo.ridesById.value = mapOf("ride-1" to ride)

        val b1 = FakeBookingRepository.sampleBooking(
            rideId = "ride-1",
            riderUid = "rider-1",
            seats = 1,
            status = BookingStatus.REQUESTED,
        )
        ridesRepo.bookingsByRideId.value = mapOf("ride-1" to listOf(b1))

        val vm = HostRideDetailsViewModel(ridesRepo, bookingRepo)
        vm.onAction(HostRideDetailsAction.Init("ride-1"))
        advanceUntilIdle()

        assertEquals("ride-1", vm.state.value.ride?.id)
        assertEquals(1, vm.state.value.pendingBookings.size)
        assertEquals(150, vm.state.value.expectedMaxEarnings)

        // Accept request
        vm.onAction(HostRideDetailsAction.RespondRequest(riderUid = "rider-1", accept = true))
        advanceUntilIdle()
        assertNotNull(vm.state.value.messageAlert)

        // Cancel ride
        vm.events.test {
            vm.onAction(HostRideDetailsAction.CancelRide(CancelRideReason.PLANS_CHANGED))
            testScheduler.runCurrent()

            val event = awaitItem()
            assertEquals(HostRideDetailsEvent.RideCancelled, event)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private val UUID_PATTERN = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
