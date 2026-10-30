package com.hitchy.feature.auth

import com.hitchy.feature.auth.domain.AuthRepository
import com.hitchy.feature.auth.domain.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    initialState: AuthState? = null,
) : AuthRepository {
    val stateFlow = MutableStateFlow(initialState)
    var signOutCalled = false

    override val authState: Flow<AuthState> = kotlinx.coroutines.flow.flow {
        stateFlow.collect { state ->
            if (state != null) {
                emit(state)
            }
        }
    }

    override suspend fun signOut() {
        signOutCalled = true
        stateFlow.value = AuthState.SignedOut
    }
}
