package com.hitchy.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.model.HostStatus
import com.hitchy.core.model.Role
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.auth.domain.AuthRepository
import com.hitchy.feature.auth.domain.AuthState
import com.hitchy.feature.host.domain.HostRepository
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.rides.domain.RidesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed interface RootState {
    data object Loading : RootState
    data object SignedOut : RootState
    data object NeedsProfile : RootState
    data class Ready(
        val activeRole: Role,
        val hostStatus: HostStatus = HostStatus.NONE,
    ) : RootState
}

sealed interface RootAction {
    data object SignOut : RootAction
}

sealed interface RootEvent

@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val ridesRepository: RidesRepository,
    private val hostRepository: HostRepository,
) : MviViewModel<RootState, RootAction, RootEvent>(RootState.Loading) {

    init {
        authRepository.authState
            .flatMapLatest { auth ->
                when (auth) {
                    AuthState.SignedOut -> flowOf(RootState.SignedOut)

                    is AuthState.SignedIn ->
                        profileRepository.observeMe().map { user ->
                            if (user == null) {
                                RootState.NeedsProfile
                            } else {
                                RootState.Ready(
                                    activeRole = user.activeRole,
                                    hostStatus = user.host.status,
                                )
                            }
                        }
                }
            }
            .onEach { next ->
                if (state.value == RootState.Loading && next is RootState.Ready && next.activeRole == Role.HOST) {
                    prewarmOwnerHome()
                }
                setState { next }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Keeps the splash screen up until the owner's rides and cars have answered once (at most
     * [PREWARM_TIMEOUT_MS]), so Owner Home opens on the right layout. Firestore keeps the answer in its cache,
     * and Owner Home's own listeners get it at once.
     */
    private suspend fun prewarmOwnerHome() {
        withTimeoutOrNull(PREWARM_TIMEOUT_MS) {
            coroutineScope {
                val rides = async { ridesRepository.observeMyRides().firstOrNull() }
                val vehicles = async { hostRepository.observeVehicles().firstOrNull() }
                rides.await()
                vehicles.await()
            }
        }
    }

    override fun onAction(action: RootAction) {
        when (action) {
            RootAction.SignOut -> viewModelScope.launch {
                authRepository.signOut()
            }
        }
    }
}

private const val PREWARM_TIMEOUT_MS = 3_000L
