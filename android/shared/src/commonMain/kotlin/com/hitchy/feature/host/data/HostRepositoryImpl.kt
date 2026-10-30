package com.hitchy.feature.host.data

import com.hitchy.core.data.AppResult
import com.hitchy.core.data.FunctionsClient
import com.hitchy.core.data.call
import com.hitchy.core.data.dto.AddVehicleInputDto
import com.hitchy.core.data.dto.AddVehicleOutputDto
import com.hitchy.core.data.dto.EmptyOutputDto
import com.hitchy.core.data.dto.RequestVerificationInputDto
import com.hitchy.core.data.dto.SubmitHostApplicationInputDto
import com.hitchy.core.data.dto.SubmitHostApplicationOutputDto
import com.hitchy.core.data.dto.VehicleTypeDto
import com.hitchy.core.data.map
import com.hitchy.core.model.VehicleStatus
import com.hitchy.core.model.VehicleType
import com.hitchy.feature.host.domain.AddVehicleInput
import com.hitchy.feature.host.domain.HostRepository
import com.hitchy.feature.host.domain.Vehicle
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
private data class VehicleFirestoreDoc(
    val schemaVersion: Int = 1,
    val type: VehicleType = VehicleType.CAR,
    val make: String = "",
    val model: String = "",
    val year: Int = 2020,
    val color: String = "",
    val plate: String = "",
    val maxSeats: Int = 3,
    val status: VehicleStatus = VehicleStatus.APPROVED,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)

class HostRepositoryImpl(
    private val functionsClient: FunctionsClient,
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : HostRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeVehicles(): Flow<List<Vehicle>> = auth.authStateChanged.flatMapLatest { firebaseUser ->
        val uid = firebaseUser?.uid
        if (uid == null) {
            flowOf(emptyList())
        } else {
            firestore
                .collection("users")
                .document(uid)
                .collection("vehicles")
                .snapshots
                .map { querySnapshot ->
                    querySnapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val data = doc.data<VehicleFirestoreDoc>()
                            Vehicle(
                                id = doc.id,
                                type = data.type,
                                make = data.make,
                                model = data.model,
                                year = data.year,
                                color = data.color,
                                plate = data.plate,
                                maxSeats = data.maxSeats,
                                status = data.status,
                            )
                        }.getOrNull()
                    }
                }
                .catch { emit(emptyList()) }
        }
    }

    override suspend fun addVehicle(input: AddVehicleInput): AppResult<String> {
        val dto = AddVehicleInputDto(
            type = when (input.type) {
                VehicleType.CAR -> VehicleTypeDto.CAR
                VehicleType.SCOOTER -> VehicleTypeDto.SCOOTER
            },
            make = input.make,
            model = input.model,
            year = input.year,
            color = input.color,
            plate = input.plate,
        )
        return functionsClient.call<AddVehicleInputDto, AddVehicleOutputDto>(
            name = "addVehicle",
            input = dto,
        ).map { it.vehicleId }
    }

    override suspend fun submit(vehicleId: String): AppResult<Unit> {
        val dto = SubmitHostApplicationInputDto(vehicleId = vehicleId)
        return functionsClient.call<SubmitHostApplicationInputDto, SubmitHostApplicationOutputDto>(
            name = "submitHostApplication",
            input = dto,
        ).map { Unit }
    }

    override suspend fun requestVerification(): AppResult<Unit> {
        val dto = RequestVerificationInputDto()
        return functionsClient.call<RequestVerificationInputDto, EmptyOutputDto>(
            name = "requestVerification",
            input = dto,
        ).map { Unit }
    }
}
