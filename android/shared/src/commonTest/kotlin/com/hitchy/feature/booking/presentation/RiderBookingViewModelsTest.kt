package com.hitchy.feature.booking.presentation

import app.cash.turbine.test
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.platform.TestClock
import com.hitchy.feature.booking.FakeBookingRepository
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
class RiderBookingViewModelsTest {

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
    fun riderHomeValidatesMissingRoute() = runTest(testDispatcher) {
        val profileRepo = FakeProfileRepository(initialUser = FakeProfileRepository.sampleUser())
        val inboxRepo = FakeInboxRepository()
        val holder = PointSelectionHolder()
        val vm = RiderHomeViewModel(profileRepo, inboxRepo, holder, clock)
        advanceUntilIdle()

        vm.onAction(RiderHomeAction.SubmitSearch)
        advanceUntilIdle()

        assertEquals(RiderSearchValidationError.MISSING_ROUTE, vm.state.value.validationError)
    }

    @Test
    fun riderHomeValidatesSameZone() = runTest(testDispatcher) {
        val profileRepo = FakeProfileRepository(initialUser = FakeProfileRepository.sampleUser())
        val inboxRepo = FakeInboxRepository()
        val holder = PointSelectionHolder()
        val vm = RiderHomeViewModel(profileRepo, inboxRepo, holder, clock)
        advanceUntilIdle()

        val p1 = Point("p1", "maadi", LocalizedName("ن1", "P1"), GeoPoint(29.0, 31.0), true)
        val p2 = Point("p2", "maadi", LocalizedName("ن2", "P2"), GeoPoint(29.1, 31.1), true)
        holder.select(RiderHomeState.FIELD_FROM, p1)
        holder.select(RiderHomeState.FIELD_TO, p2)
        advanceUntilIdle()

        vm.onAction(RiderHomeAction.SubmitSearch)
        advanceUntilIdle()

        assertEquals(RiderSearchValidationError.SAME_ZONE, vm.state.value.validationError)
    }

    @Test
    fun riderHomeEmitsNavigationOnValidRoute() = runTest(testDispatcher) {
        val profileRepo = FakeProfileRepository(initialUser = FakeProfileRepository.sampleUser())
        val inboxRepo = FakeInboxRepository()
        val holder = PointSelectionHolder()
        val vm = RiderHomeViewModel(profileRepo, inboxRepo, holder, clock)
        advanceUntilIdle()

        val p1 = Point("p1", "maadi", LocalizedName("ن1", "P1"), GeoPoint(29.0, 31.0), true)
        val p2 = Point("p2", "smart-village", LocalizedName("ن2", "P2"), GeoPoint(30.0, 31.0), true)
        holder.select(RiderHomeState.FIELD_FROM, p1)
        holder.select(RiderHomeState.FIELD_TO, p2)
        advanceUntilIdle()

        vm.events.test {
            vm.onAction(RiderHomeAction.SubmitSearch)
            testScheduler.runCurrent()

            assertNull(vm.state.value.validationError)
            val event = awaitItem() as RiderHomeEvent.NavigateToSearchResults
            assertEquals("maadi", event.fromZone)
            assertEquals("smart-village", event.toZone)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun riderRideDetailsRequestsSeatsWithIdempotency() = runTest(testDispatcher) {
        val bookingRepo = FakeBookingRepository()
        val ridesRepo = FakeRidesRepository()
        val vm = RiderRideDetailsViewModel(bookingRepo, ridesRepo)

        val card = FakeBookingRepository.sampleRideCard(rideId = "ride-1", price = 40, seatsLeft = 2)
        bookingRepo.cachedCards.value = mapOf("ride-1" to card)

        vm.onAction(RiderRideDetailsAction.Init("ride-1"))
        advanceUntilIdle()

        assertEquals(40, vm.state.value.pricePerSeat)
        assertEquals(2, vm.state.value.maxSeats)

        vm.onAction(RiderRideDetailsAction.UpdateSeats(2))
        vm.onAction(RiderRideDetailsAction.RequestRide)
        advanceUntilIdle()

        assertEquals("ride-1", bookingRepo.lastRequestedRideId)
        assertEquals(2, bookingRepo.lastRequestedSeats)
        assertEquals("start", bookingRepo.lastRequestedPickupId)
        assertTrue(!bookingRepo.lastRequestId.isNullOrBlank())
    }
}
