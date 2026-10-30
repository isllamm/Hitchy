package com.hitchy.feature.places.data

import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import com.hitchy.feature.places.domain.PlacesRepository
import com.hitchy.feature.places.domain.Point
import com.hitchy.feature.places.domain.Zone
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
import kotlinx.serialization.Serializable

@Serializable
private data class ZoneFirestoreDoc(
    val name: LocalizedName = LocalizedName(ar = "", en = ""),
    val active: Boolean = true,
    val order: Int = 0,
)

@Serializable
private data class PointFirestoreDoc(
    val name: LocalizedName = LocalizedName(ar = "", en = ""),
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val active: Boolean = true,
)

class PlacesRepositoryImpl(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : PlacesRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeZones(): Flow<List<Zone>> = auth.authStateChanged.flatMapLatest { firebaseUser ->
        if (firebaseUser == null) {
            flowOf(emptyList())
        } else {
            firestore
                .collection("zones")
                .snapshots
                .map { querySnapshot ->
                    querySnapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val data = doc.data<ZoneFirestoreDoc>()
                            Zone(
                                id = doc.id,
                                name = data.name,
                                active = data.active,
                                order = data.order,
                            )
                        }.getOrNull()
                    }
                        .filter { it.active }
                        .sortedBy { it.order }
                }
                .catch { emit(emptyList()) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observePoints(zoneId: String): Flow<List<Point>> {
        val trimmedZone = zoneId.trim()
        return auth.authStateChanged.flatMapLatest { user ->
            if (user == null || trimmedZone.isEmpty()) {
                flowOf(emptyList())
            } else {
                firestore
                    .collection("zones")
                    .document(trimmedZone)
                    .collection("points")
                    .snapshots
                    .map { querySnapshot ->
                        querySnapshot.documents.mapNotNull { doc ->
                            runCatching {
                                val data = doc.data<PointFirestoreDoc>()
                                Point(
                                    id = doc.id,
                                    zoneId = trimmedZone,
                                    name = data.name,
                                    location = GeoPoint(lat = data.lat, lng = data.lng),
                                    active = data.active,
                                )
                            }.getOrNull()
                        }.filter { it.active }
                    }
                    .catch { emit(emptyList()) }
            }
        }
    }
}
