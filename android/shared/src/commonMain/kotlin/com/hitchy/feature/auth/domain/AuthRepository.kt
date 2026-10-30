package com.hitchy.feature.auth.domain

import kotlinx.coroutines.flow.Flow

sealed interface AuthState {
    data object SignedOut : AuthState
    data class SignedIn(val uid: String) : AuthState
}

interface AuthRepository {
    val authState: Flow<AuthState>
    suspend fun signOut()
}
