package com.hitchy.feature.host

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.VehicleStatus
import com.hitchy.core.model.VehicleType
import com.hitchy.feature.host.domain.AddVehicleInput
import com.hitchy.feature.host.domain.HostRepository
import com.hitchy.feature.host.domain.Vehicle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHostRepository(
    initialVehicles: List<Vehicle> = listOf(sampleVehicle()),
) : HostRepository {
    val vehiclesFlow = MutableStateFlow(initialVehicles)

    override fun observeVehicles(): Flow<List<Vehicle>> = vehiclesFlow

    override suspend fun addVehicle(input: AddVehicleInput): AppResult<String> {
        val vehicleId = "veh-new"
        return AppResult.Ok(vehicleId)
    }

    override suspend fun submit(vehicleId: String): AppResult<Unit> = AppResult.Ok(Unit)

    override suspend fun requestVerification(): AppResult<Unit> = AppResult.Ok(Unit)

    companion object {
        @Suppress("LongParameterList")
        fun sampleVehicle(
            id: String = "veh-1",
            type: VehicleType = VehicleType.CAR,
            make: String = "Toyota",
            model: String = "Corolla",
            year: Int = 2022,
            color: String = "Silver",
            plate: String = "أ ب ج 1234",
            maxSeats: Int = 4,
            status: VehicleStatus = VehicleStatus.APPROVED,
        ) = Vehicle(
            id = id,
            type = type,
            make = make,
            model = model,
            year = year,
            color = color,
            plate = plate,
            maxSeats = maxSeats,
            status = status,
        )
    }
}
