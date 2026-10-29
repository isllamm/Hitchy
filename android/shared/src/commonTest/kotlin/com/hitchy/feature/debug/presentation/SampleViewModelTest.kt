package com.hitchy.feature.debug.presentation

import app.cash.turbine.test
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SampleViewModelTest {

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
    fun initialStateIsDefault() {
        val vm = SampleViewModel { AppResult.Ok("ok") }
        assertEquals(SampleState(isLoading = false, data = null, error = null), vm.state.value)
    }

    @Test
    fun loadingSuccessEventFlow() = runTest(testDispatcher) {
        val deferred = CompletableDeferred<AppResult<String>>()
        val vm = SampleViewModel { deferred.await() }

        vm.state.test {
            val stateTurbine = this
            assertEquals(SampleState(isLoading = false, data = null, error = null), stateTurbine.awaitItem())

            vm.events.test {
                val eventTurbine = this

                // Trigger action
                vm.onAction(SampleAction.Load)
                testScheduler.runCurrent()

                // State transitions to loading
                val loadingState = stateTurbine.awaitItem()
                assertTrue(loadingState.isLoading)
                assertNull(loadingState.data)
                assertNull(loadingState.error)

                // Complete loader
                deferred.complete(AppResult.Ok("loaded-payload"))
                testScheduler.runCurrent()

                // State transitions to success
                val successState = stateTurbine.awaitItem()
                assertFalse(successState.isLoading)
                assertEquals("loaded-payload", successState.data)
                assertNull(successState.error)

                // Event emitted
                val event = eventTurbine.awaitItem()
                assertEquals(SampleEvent.LoadSuccess("loaded-payload"), event)

                eventTurbine.cancelAndIgnoreRemainingEvents()
            }

            stateTurbine.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun failureFlow() = runTest(testDispatcher) {
        val deferred = CompletableDeferred<AppResult<String>>()
        val vm = SampleViewModel { deferred.await() }

        vm.state.test {
            assertEquals(SampleState(isLoading = false, data = null, error = null), awaitItem())

            vm.onAction(SampleAction.Load)
            testScheduler.runCurrent()

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            deferred.complete(AppResult.Err(AppError.Network))
            testScheduler.runCurrent()

            val failureState = awaitItem()
            assertFalse(failureState.isLoading)
            assertEquals(AppError.Network, failureState.error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
