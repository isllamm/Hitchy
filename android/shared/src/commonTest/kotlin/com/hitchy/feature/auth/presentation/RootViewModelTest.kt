package com.hitchy.feature.auth.presentation

import com.hitchy.core.model.HostStatus
import com.hitchy.core.model.Role
import com.hitchy.feature.auth.FakeAuthRepository
import com.hitchy.feature.auth.domain.AuthState
import com.hitchy.feature.profile.FakeProfileRepository
import com.hitchy.navigation.Home
import com.hitchy.navigation.Onboarding
import com.hitchy.navigation.ProfileSetup
import com.hitchy.navigation.toStartDestination
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
        val vm = RootViewModel(authRepo, profileRepo)

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
}
