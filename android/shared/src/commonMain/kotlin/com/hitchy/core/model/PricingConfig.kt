package com.hitchy.core.model

import kotlinx.serialization.Serializable

@Serializable
data class TimeWindow(
    val from: String,
    val to: String,
)

@Serializable
data class VehicleLiters(
    val car: Double,
    val scooter: Double? = null,
)

@Serializable
data class VehicleRunningCost(
    val car: Double,
    val scooter: Double? = null,
)

@Serializable
data class VehicleSeats(
    val car: Int,
    val scooter: Int,
)

@Serializable
data class PricingConfig(
    val version: Int = 1,
    val fuelPriceEgpPerLiter: Double = 0.0,
    val litersPer100Km: VehicleLiters = VehicleLiters(car = 0.0),
    val runningCostEgpPerKm: VehicleRunningCost = VehicleRunningCost(car = 0.0),
    val trafficFactor: Map<String, Double> = emptyMap(),
    val peakWindows: List<TimeWindow> = emptyList(),
    val normalWindows: List<TimeWindow> = emptyList(),
    val marginCap: Double = 0.0,
    val maxSeats: VehicleSeats = VehicleSeats(car = 0, scooter = 0),
)
