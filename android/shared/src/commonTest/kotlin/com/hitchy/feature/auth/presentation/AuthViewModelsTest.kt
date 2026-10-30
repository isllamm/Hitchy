package com.hitchy.feature.auth.presentation

import app.cash.turbine.test
import com.hitchy.core.platform.FakePhoneAuth
import com.hitchy.core.platform.PhoneAuthEvent
import com.hitchy.core.platform.TestClock
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class AuthViewModelsTest {

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
    fun phoneEntryNormalizesAndSubmits() = runTest(testDispatcher) {
        val phoneAuth = FakePhoneAuth()
        val vm = PhoneEntryViewModel(phoneAuth)

        vm.onAction(PhoneEntryAction.UpdatePhone("01012345678"))
        assertTrue(vm.state.value.isValid)
        assertEquals("+201012345678", vm.state.value.normalizedPhone)

        vm.events.test {
            vm.onAction(PhoneEntryAction.Submit)
            testScheduler.runCurrent()

            assertEquals("+201012345678", phoneAuth.lastStartedPhone)
            val event = awaitItem()
            assertEquals(PhoneEntryEvent.NavigateToOtp("+201012345678"), event)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun phoneEntryAcceptsTheNumberAfterTheFixedPrefix() = runTest(testDispatcher) {
        val vm = PhoneEntryViewModel(FakePhoneAuth())

        vm.onAction(PhoneEntryAction.UpdatePhone("1012345678"))

        assertTrue(vm.state.value.isValid)
        assertEquals("+201012345678", vm.state.value.normalizedPhone)
    }

    @Test
    fun phoneEntryRejectsInvalidNumber() = runTest(testDispatcher) {
        val phoneAuth = FakePhoneAuth()
        val vm = PhoneEntryViewModel(phoneAuth)

        vm.onAction(PhoneEntryAction.UpdatePhone("12345"))
        assertFalse(vm.state.value.isValid)

        vm.onAction(PhoneEntryAction.Submit)
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertNull(phoneAuth.lastStartedPhone)
    }

    @Test
    fun otpVerifyAutoSubmitsOnSixDigits() = runTest(testDispatcher) {
        val phoneAuth = FakePhoneAuth()
        val vm = OtpVerifyViewModel(phoneAuth, clock)

        vm.onAction(OtpVerifyAction.Init("+201012345678"))
        assertEquals("+201012345678", vm.state.value.phoneE164)

        vm.events.test {
            // Send 6 digits
            vm.onAction(OtpVerifyAction.UpdateCode("123456"))
            testScheduler.runCurrent()

            val event = awaitItem()
            assertEquals(OtpVerifyEvent.Verified, event)
            assertNull(vm.state.value.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun otpVerifyHandlesCountdownAndResend() = runTest(testDispatcher) {
        val phoneAuth = FakePhoneAuth()
        val vm = OtpVerifyViewModel(phoneAuth, clock)
        vm.onAction(OtpVerifyAction.Init("+201012345678"))

        // Receive CodeSent with 30s resend timer
        val resendAt = clock.now() + 30.seconds
        phoneAuth.emitEvent(PhoneAuthEvent.CodeSent(resendAt.toEpochMilliseconds()))
        testScheduler.runCurrent()

        assertEquals(30, vm.state.value.resendSecondsLeft)
        assertFalse(vm.state.value.canResend)

        // Resend after countdown finishes
        advanceUntilIdle()
        assertEquals(0, vm.state.value.resendSecondsLeft)
        assertTrue(vm.state.value.canResend)

        vm.onAction(OtpVerifyAction.Resend)
        assertEquals("+201012345678", phoneAuth.lastStartedPhone)
    }
}
