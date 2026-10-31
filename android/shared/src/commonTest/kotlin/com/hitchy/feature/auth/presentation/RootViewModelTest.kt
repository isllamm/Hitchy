package com.hitchy.feature.auth.presentation

import com.hitchy.core.model.HostStatus
import com.hitchy.core.model.Role
import com.hitchy.feature.auth.FakeAuthRepository
import com.hitchy.feature.auth.domain.AuthState
import com.hitchy.feature.host.FakeHostRepository
import com.hitchy.feature.profile.FakeProfileRepository
import com.hitchy.feature.rides.FakeRidesRepository
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.domain.RidesRepository
import com.hitchy.navigation.Home
import com.hitchy.navigation.Onboarding
import com.hitchy.navigation.ProfileSetup
import com.hitchy.navigation.toStartDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModelTest {

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
    fun transitionsAcrossAllFourStatesAndRoleSwitch() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepository(initialState = null)
        val profileRepo = FakeProfileRepository(initialUser = null)
        val vm = RootViewModel(authRepo, profileRepo, FakeRidesRepository(), FakeHostRepository())

        // 1. Loading before authState emits
        assertEquals(RootState.Loading, vm.state.value)
        assertNull(vm.state.value.toStartDestination())

        // 2. SignedOut
        authRepo.stateFlow.value = AuthState.SignedOut
        advanceUntilIdle()
        assertEquals(RootState.SignedOut, vm.state.value)
        assertEquals(Onboarding, vm.state.value.toStartDestination())

        // 3. SignedIn without profile -> NeedsProfile
        authRepo.stateFlow.value = AuthState.SignedIn("rider-test-1")
        advanceUntilIdle()
        assertEquals(RootState.NeedsProfile, vm.state.value)
        assertEquals(ProfileSetup, vm.state.value.toStartDestination())

        // 4. Profile appears -> Ready(RIDER)
        profileRepo.meFlow.value = FakeProfileRepository.sampleUser(
            activeRole = Role.RIDER,
            hostStatus = HostStatus.APPROVED,
        )
        advanceUntilIdle()
        assertEquals(
            RootState.Ready(activeRole = Role.RIDER, hostStatus = HostStatus.APPROVED),
            vm.state.value,
        )
        assertEquals(Home, vm.state.value.toStartDestination())

        // 5. Role switches to HOST -> Ready(HOST)
        profileRepo.meFlow.value = FakeProfileRepository.sampleUser(
            activeRole = Role.HOST,
            hostStatus = HostStatus.APPROVED,
        )
        advanceUntilIdle()
        assertEquals(
            RootState.Ready(activeRole = Role.HOST, hostStatus = HostStatus.APPROVED),
            vm.state.value,
        )

        // 6. SignOut action -> SignedOut
        vm.onAction(RootAction.SignOut)
        advanceUntilIdle()
        assertTrue(authRepo.signOutCalled)
        assertEquals(RootState.SignedOut, vm.state.value)
    }

    @Test
    fun ownerStartWaitsForTheirRidesBeforeLeavingTheSplash() = runTest(testDispatcher) {
        val rides = MutableSharedFlow<List<Ride>>()
        val vm = ownerRootViewModel(rides)

        runCurrent()
        // Signed in as an owner, but the rides have not answered: the splash stays.
        assertEquals(RootState.Loading, vm.state.value)

        rides.emit(emptyList())
        runCurrent()
        assertEquals(RootState.Ready(activeRole = Role.HOST, hostStatus = HostStatus.APPROVED), vm.state.value)
    }

    @Test
    fun ownerStartLeavesTheSplashAfterThreeSecondsWithoutAnAnswer() = runTest(testDispatcher) {
        val vm = ownerRootViewModel(MutableSharedFlow())

        advanceTimeBy(2_999)
        assertEquals(RootState.Loading, vm.state.value)

        advanceTimeBy(2)
        runCurrent()
        assertEquals(RootState.Ready(activeRole = Role.HOST, hostStatus = HostStatus.APPROVED), vm.state.value)
    }

    private fun ownerRootViewModel(rides: Flow<List<Ride>>): RootViewModel {
        val ridesRepo = object : RidesRepository by FakeRidesRepository() {
            override fun observeMyRides(): Flow<List<Ride>> = rides
        }
        return RootViewModel(
            authRepository = FakeAuthRepository(initialState = AuthState.SignedIn("host-test-1")),
            profileRepository = FakeProfileRepository(
                initialUser = FakeProfileRepository.sampleUser(activeRole = Role.HOST, hostStatus = HostStatus.APPROVED),
            ),
            ridesRepository = ridesRepo,
            hostRepository = FakeHostRepository(),
        )
    }
}
