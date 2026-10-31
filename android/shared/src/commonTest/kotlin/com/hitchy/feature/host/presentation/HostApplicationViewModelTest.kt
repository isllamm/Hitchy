package com.hitchy.feature.host.presentation

import com.hitchy.core.model.HostStatus
import com.hitchy.core.model.VehicleType
import com.hitchy.core.platform.TestClock
import com.hitchy.feature.host.FakeHostRepository
import com.hitchy.feature.host.domain.HostDraft
import com.hitchy.feature.host.domain.KycDocKey
import com.hitchy.feature.profile.FakeProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HostApplicationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val clock = TestClock(Instant.parse("2026-10-30T08:00:00Z"))
    private val draft = HostDraft()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(hostStatus: HostStatus = HostStatus.NONE): HostApplicationViewModel {
        val profile = FakeProfileRepository(FakeProfileRepository.sampleUser(hostStatus = hostStatus))
        val vm = HostApplicationViewModel(FakeHostRepository(), profile, draft, clock)
        advanceUntilIdle()
        return vm
    }

    private fun HostApplicationViewModel.fillToReview() {
        onAction(HostApplicationAction.SelectVehicle(VehicleType.CAR))
        onAction(HostApplicationAction.Continue)
        onAction(HostApplicationAction.Continue)
        KycDocKey.entries.forEach { onAction(HostApplicationAction.DocTaken(it, byteArrayOf(1))) }
        onAction(HostApplicationAction.Continue)
        onAction(HostApplicationAction.UpdateMakeModel("Make Model"))
        onAction(HostApplicationAction.SelectYear(2020))
        onAction(HostApplicationAction.UpdatePlate("ABC 123"))
        onAction(HostApplicationAction.SelectColor("White"))
        onAction(HostApplicationAction.Continue)
    }

    @Test
    fun eachStepWaitsForItsFields() = runTest(testDispatcher) {
        val vm = viewModel()
        assertEquals(HostStep.VEHICLE, vm.state.value.step)
        assertFalse(vm.state.value.canContinue)
        vm.onAction(HostApplicationAction.SelectVehicle(VehicleType.SCOOTER))
        vm.onAction(HostApplicationAction.Continue)
        assertEquals(HostStep.BASIC, vm.state.value.step)
        assertEquals(1, vm.state.value.seats)
        // The name comes from the profile.
        assertTrue(vm.state.value.canContinue)
        vm.onAction(HostApplicationAction.Continue)
        assertEquals(HostStep.DOCS, vm.state.value.step)
        vm.onAction(HostApplicationAction.DocTaken(KycDocKey.ID_FRONT, byteArrayOf(1)))
        vm.onAction(HostApplicationAction.Continue)
        assertEquals(HostStep.DOCS, vm.state.value.step)
    }

    @Test
    fun yearsStartAtThisYearInCairo() = runTest(testDispatcher) {
        val vm = viewModel()
        assertEquals(2026, vm.state.value.years.first())
        assertEquals(HostApplicationState.YEAR_CHOICES, vm.state.value.years.size)
    }

    @Test
    fun backFromTheDocumentsAsksBeforeLeaving() = runTest(testDispatcher) {
        val vm = viewModel()
        vm.onAction(HostApplicationAction.SelectVehicle(VehicleType.CAR))
        vm.onAction(HostApplicationAction.Continue)
        vm.onAction(HostApplicationAction.Continue)
        vm.onAction(HostApplicationAction.Back)
        assertTrue(vm.state.value.showExit)
        vm.onAction(HostApplicationAction.KeepGoing)
        assertFalse(vm.state.value.showExit)
        assertEquals(HostStep.DOCS, vm.state.value.step)

        var exited = false
        val job = launch { vm.events.first { it == HostApplicationEvent.Exit }.also { exited = true } }
        vm.onAction(HostApplicationAction.Back)
        vm.onAction(HostApplicationAction.ConfirmExit)
        advanceUntilIdle()
        job.cancel()
        assertTrue(exited)
        // The draft keeps what was entered for later.
        assertEquals(VehicleType.CAR, draft.vehicleType)
    }

    @Test
    fun submitNeedsTheConfirmationThenClearsTheDraft() = runTest(testDispatcher) {
        val vm = viewModel()
        vm.fillToReview()
        assertEquals(HostStep.REVIEW, vm.state.value.step)
        assertFalse(vm.state.value.canContinue)
        vm.onAction(HostApplicationAction.ToggleConfirm)

        var submitted = false
        val job = launch { vm.events.first { it == HostApplicationEvent.Submitted }.also { submitted = true } }
        vm.onAction(HostApplicationAction.Continue)
        advanceUntilIdle()
        job.cancel()
        assertTrue(submitted)
        assertTrue(draft.docs.isEmpty())
        assertEquals(null, draft.vehicleType)
    }

    @Test
    fun aSubmittedApplicationOpensItsStatus() = runTest(testDispatcher) {
        val profile = FakeProfileRepository(FakeProfileRepository.sampleUser(hostStatus = HostStatus.SUBMITTED))
        val vm = HostApplicationViewModel(FakeHostRepository(), profile, draft, clock)
        var redirected = false
        val job = launch { vm.events.first { it == HostApplicationEvent.AlreadySubmitted }.also { redirected = true } }
        advanceUntilIdle()
        job.cancel()
        assertTrue(redirected)
    }
}
