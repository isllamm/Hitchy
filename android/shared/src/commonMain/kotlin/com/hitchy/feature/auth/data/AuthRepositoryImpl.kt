package com.hitchy.feature.auth.data

import com.hitchy.feature.auth.domain.AuthRepository
import com.hitchy.feature.auth.domain.AuthState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = Firebase.auth,
) : AuthRepository {

    override val authState: Flow<AuthState> = auth.authStateChanged.map { user ->
        if (user == null) {
            AuthState.SignedOut
        } else {
            AuthState.SignedIn(user.uid)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
