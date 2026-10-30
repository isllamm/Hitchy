package com.hitchy.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.model.HostStatus
import com.hitchy.core.model.Role
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.auth.domain.AuthRepository
import com.hitchy.feature.auth.domain.AuthState
import com.hitchy.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
            .onEach { next -> setState { next } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: RootAction) {
        when (action) {
            RootAction.SignOut -> viewModelScope.launch {
                authRepository.signOut()
            }
        }
    }
}
