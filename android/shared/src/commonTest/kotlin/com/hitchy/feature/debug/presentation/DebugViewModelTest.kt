package com.hitchy.feature.debug.presentation

import app.cash.turbine.test
import com.hitchy.core.model.AppConfig
import com.hitchy.core.model.Env
import com.hitchy.core.model.PricingConfig
import com.hitchy.core.model.VehicleLiters
import com.hitchy.core.model.VehicleRunningCost
import com.hitchy.core.model.VehicleSeats
import com.hitchy.feature.debug.FakeDebugInfo
import com.hitchy.feature.debug.FakeDebugRepository
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
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DebugViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val appConfig = AppConfig(
        env = Env.DEV,
        functionsRegion = "europe-west1",
        useEmulator = true,
        versionName = "1.0.0-dev",
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateLoadsConfigAndPlatformValues() = runTest(testDispatcher) {
        val repo = FakeDebugRepository(initialUid = null)
        val info = FakeDebugInfo()
        val vm = DebugViewModel(repo, info, appConfig)

        vm.state.test {
            val initial = awaitItem()
            assertEquals("DEV", initial.env)
            assertEquals("europe-west1", initial.functionsRegion)
            assertTrue(initial.useEmulator)
            assertEquals("1.0.0-dev", initial.versionName)

            advanceUntilIdle()

            val loaded = expectMostRecentItem()
            assertEquals("fake-fcm-token", loaded.fcmToken)
            assertEquals("OK", loaded.appCheckStatus)
            assertEquals(mapOf("minVersionCode" to "1"), loaded.remoteConfigValues)
            assertNull(loaded.uid)
        }
    }

    @Test
    fun observeUidUpdatesState() = runTest(testDispatcher) {
        val repo = FakeDebugRepository(initialUid = "user-123")
        val info = FakeDebugInfo()
        val vm = DebugViewModel(repo, info, appConfig)

        advanceUntilIdle()
        assertEquals("user-123", vm.state.value.uid)

        repo.uidFlow.value = "user-456"
        advanceUntilIdle()
        assertEquals("user-456", vm.state.value.uid)
    }

    @Test
    fun livePricingConfigUpdatesState() = runTest(testDispatcher) {
        val repo = FakeDebugRepository(initialUid = "user-123")
        val info = FakeDebugInfo()
        val vm = DebugViewModel(repo, info, appConfig)

        advanceUntilIdle()
        assertNull(vm.state.value.pricingConfig)

        val sampleConfig = PricingConfig(
            version = 1,
            fuelPriceEgpPerLiter = 22.25,
            litersPer100Km = VehicleLiters(car = 8.0, scooter = null),
            runningCostEgpPerKm = VehicleRunningCost(car = 0.75, scooter = null),
            trafficFactor = mapOf("offPeak" to 1.0, "normal" to 1.15, "peak" to 1.3),
            marginCap = 0.2,
            maxSeats = VehicleSeats(car = 3, scooter = 1),
        )
        repo.pricingFlow.value = sampleConfig
        advanceUntilIdle()

        val state = vm.state.value
        val config = state.pricingConfig
        assertNotNull(config)
        assertEquals(22.25, config.fuelPriceEgpPerLiter)
        assertEquals(8.0, config.litersPer100Km.car)
        assertEquals(0.2, config.marginCap)
    }

    @Test
    fun signInAnonymouslyActionCallsRepository() = runTest(testDispatcher) {
        val repo = FakeDebugRepository(initialUid = null)
        val info = FakeDebugInfo()
        val vm = DebugViewModel(repo, info, appConfig)

        advanceUntilIdle()
        assertNull(vm.state.value.uid)

        vm.onAction(DebugAction.SignInAnonymously)
        advanceUntilIdle()

        assertTrue(repo.signInAnonymouslyCalled)
        assertEquals("test-anon-uid", vm.state.value.uid)
    }

    @Test
    fun signOutActionCallsRepository() = runTest(testDispatcher) {
        val repo = FakeDebugRepository(initialUid = "user-123")
        val info = FakeDebugInfo()
        val vm = DebugViewModel(repo, info, appConfig)

        advanceUntilIdle()
        assertEquals("user-123", vm.state.value.uid)

        vm.onAction(DebugAction.SignOut)
        advanceUntilIdle()

        assertTrue(repo.signOutCalled)
        assertNull(vm.state.value.uid)
    }

    @Test
    fun testCrashActionThrows() = runTest(testDispatcher) {
        val repo = FakeDebugRepository()
        val info = FakeDebugInfo()
        val vm = DebugViewModel(repo, info, appConfig)

        assertFailsWith<RuntimeException> {
            vm.onAction(DebugAction.TestCrash)
        }
        assertTrue(info.testCrashTriggered)
    }
}
