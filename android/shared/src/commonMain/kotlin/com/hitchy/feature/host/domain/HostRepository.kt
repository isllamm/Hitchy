package com.hitchy.feature.host.domain

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.VehicleStatus
import com.hitchy.core.model.VehicleType
import kotlinx.coroutines.flow.Flow

data class Vehicle(
    val id: String,
    val type: VehicleType,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val plate: String,
    val maxSeats: Int,
    val status: VehicleStatus,
)

data class AddVehicleInput(
    val type: VehicleType,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val plate: String,
)

interface HostRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    suspend fun addVehicle(input: AddVehicleInput): AppResult<String>
    suspend fun submit(vehicleId: String): AppResult<Unit>
    suspend fun requestVerification(): AppResult<Unit>
}
