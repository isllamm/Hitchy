package com.hitchy.feature.inbox.data

import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.data.mapFunctionsError
import com.hitchy.core.data.toIsoString
import com.hitchy.core.model.LocalizedName
import com.hitchy.feature.inbox.domain.InboxItem
import com.hitchy.feature.inbox.domain.InboxRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
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
private data class NotificationFirestoreDoc(
    val type: String = "",
    val title: LocalizedName = LocalizedName(ar = "", en = ""),
    val body: LocalizedName = LocalizedName(ar = "", en = ""),
    val deepLink: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val readAt: Timestamp? = null,
    val expireAt: Timestamp? = null,
)

class InboxRepositoryImpl(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : InboxRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<List<InboxItem>> = auth.authStateChanged.flatMapLatest { firebaseUser ->
        val uid = firebaseUser?.uid
        if (uid == null) {
            flowOf(emptyList())
        } else {
            firestore
                .collection("users")
                .document(uid)
                .collection("notifications")
                .snapshots
                .map { querySnapshot ->
                    querySnapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val data = doc.data<NotificationFirestoreDoc>()
                            InboxItem(
                                id = doc.id,
                                type = data.type,
                                title = data.title,
                                body = data.body,
                                deepLink = data.deepLink,
                                createdAt = data.createdAt.toIsoString(),
                                readAt = data.readAt?.toIsoString(),
                            )
                        }.getOrNull()
                    }.sortedByDescending { it.createdAt }
                }
                .catch { emit(emptyList()) }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun markRead(ids: List<String>): AppResult<Unit> {
        val uid = auth.currentUser?.uid ?: return AppResult.Err(AppError.NotSignedIn)
        if (ids.isEmpty()) return AppResult.Ok(Unit)
        return try {
            val batch = firestore.batch()
            val col = firestore.collection("users").document(uid).collection("notifications")
            ids.forEach { id ->
                batch.update(col.document(id), "readAt" to FieldValue.serverTimestamp)
            }
            batch.commit()
            AppResult.Ok(Unit)
        } catch (t: Throwable) {
            AppResult.Err(mapFunctionsError(t))
        }
    }
}
