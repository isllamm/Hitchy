package com.hitchy.feature.rides.presentation

import app.cash.turbine.test
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.platform.TestClock
import com.hitchy.feature.host.FakeHostRepository
import com.hitchy.feature.places.domain.Point
import com.hitchy.feature.places.domain.PointSelectionHolder
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
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class PostRideStepsTest {

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
    fun continueChecksTheRouteBeforeTheReviewStep() = runTest(testDispatcher) {
        val holder = PointSelectionHolder()
        val vm = PostRideViewModel(FakeRidesRepository(), FakeHostRepository(), holder, clock)
        advanceUntilIdle()

        vm.onAction(PostRideAction.Continue)
        advanceUntilIdle()
        assertNotNull(vm.state.value.validationError)

        holder.select(PostRideState.FIELD_FROM, point("p1", "maadi"))
        holder.select(PostRideState.FIELD_TO, point("p2", "smart-village"))
        advanceUntilIdle()

        vm.events.test {
            vm.onAction(PostRideAction.Continue)
            testScheduler.runCurrent()
            assertEquals(PostRideEvent.ShowReview, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(vm.state.value.validationError)
    }

    @Test
    fun continueRefusesTheSameZone() = runTest(testDispatcher) {
        val holder = PointSelectionHolder()
        val vm = PostRideViewModel(FakeRidesRepository(), FakeHostRepository(), holder, clock)
        holder.select(PostRideState.FIELD_FROM, point("p1", "maadi"))
        holder.select(PostRideState.FIELD_TO, point("p2", "maadi"))
        advanceUntilIdle()

        vm.onAction(PostRideAction.Continue)
        advanceUntilIdle()

        assertNotNull(vm.state.value.validationError)
    }

    private fun point(id: String, zone: String) = Point(
        id = id,
        zoneId = zone,
        name = LocalizedName(ar = "نقطة", en = "Point"),
        location = GeoPoint(30.0, 31.0),
        active = true,
    )
}
