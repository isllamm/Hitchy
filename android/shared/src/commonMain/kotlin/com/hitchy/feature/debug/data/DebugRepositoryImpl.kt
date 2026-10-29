package com.hitchy.feature.debug.data

import com.hitchy.core.model.PricingConfig
import com.hitchy.feature.debug.domain.DebugRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class DebugRepositoryImpl(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : DebugRepository {

    override fun observeCurrentUid(): Flow<String?> =
        auth.authStateChanged.map { it?.uid }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observePricingConfig(): Flow<PricingConfig?> =
        auth.authStateChanged.flatMapLatest { user ->
            if (user == null) {
                flowOf(null)
            } else {
                firestore
                    .collection("config")
                    .document("pricing")
                    .snapshots
                    .map { snapshot ->
                        if (snapshot.exists) {
                            try {
                                snapshot.data<PricingConfig>()
                            } catch (_: Exception) {
                                null
                            }
                        } else {
                            null
                        }
                    }
                    .catch { emit(null) }
            }
        }

    override suspend fun signInAnonymously() {
        auth.signInAnonymously()
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
