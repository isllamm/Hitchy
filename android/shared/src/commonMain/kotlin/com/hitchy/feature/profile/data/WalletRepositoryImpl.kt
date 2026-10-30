package com.hitchy.feature.profile.data

import com.hitchy.feature.profile.domain.WalletRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class WalletFirestoreDoc(
    val balance: Int = 0,
    val updatedAt: Timestamp? = null,
)

class WalletRepositoryImpl(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : WalletRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeBalance(): Flow<Int> = auth.authStateChanged.flatMapLatest { firebaseUser ->
        val uid = firebaseUser?.uid
        if (uid == null) {
            flowOf(0)
        } else {
            firestore
                .collection("wallets")
                .document(uid)
                .snapshots
                .map { snapshot ->
                    if (!snapshot.exists) {
                        0
                    } else {
                        runCatching {
                            snapshot.data<WalletFirestoreDoc>().balance
                        }.getOrDefault(0)
                    }
                }
                .catch { emit(0) }
        }
    }
}
