package com.hitchy.feature.trip.domain

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LateKind

data class FinishResult(
    val completedBookings: Int,
    val creditUsed: Boolean,
)

data class ConfirmPaymentResult(
    val paidBookings: Int,
    val total: Int,
)

interface TripRepository {
    suspend fun start(rideId: String): AppResult<Unit>
    suspend fun arriveAtStop(rideId: String, pickupId: String): AppResult<Int>
    suspend fun checkIn(rideId: String, at: GeoPoint, accuracyM: Double): AppResult<Int>
    suspend fun markBoarded(rideId: String, riderUid: String): AppResult<Unit>
    suspend fun reportLate(rideId: String, riderUid: String, kind: LateKind): AppResult<String>
    suspend fun finish(rideId: String): AppResult<FinishResult>
    suspend fun confirmRidePayment(rideId: String, requestId: String): AppResult<ConfirmPaymentResult>
}
