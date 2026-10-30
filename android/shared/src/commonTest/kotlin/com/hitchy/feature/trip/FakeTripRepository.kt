package com.hitchy.feature.trip

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LateKind
import com.hitchy.feature.trip.domain.ConfirmPaymentResult
import com.hitchy.feature.trip.domain.FinishResult
import com.hitchy.feature.trip.domain.TripRepository

class FakeTripRepository : TripRepository {
    var lastCheckedInRideId: String? = null
    var lastConfirmRequestId: String? = null
    var lastCheckInPoint: GeoPoint? = null
    var checkInResult: AppResult<Int> = AppResult.Ok(15)

    override suspend fun start(rideId: String): AppResult<Unit> = AppResult.Ok(Unit)

    override suspend fun arriveAtStop(rideId: String, pickupId: String): AppResult<Int> = AppResult.Ok(1)

    override suspend fun checkIn(rideId: String, at: GeoPoint, accuracyM: Double): AppResult<Int> {
        lastCheckedInRideId = rideId
        lastCheckInPoint = at
        return checkInResult
    }

    override suspend fun markBoarded(rideId: String, riderUid: String): AppResult<Unit> = AppResult.Ok(Unit)

    override suspend fun reportLate(rideId: String, riderUid: String, kind: LateKind): AppResult<String> {
        val feeId = "fee-1"
        return AppResult.Ok(feeId)
    }

    override suspend fun finish(rideId: String): AppResult<FinishResult> {
        val res = FinishResult(completedBookings = 1, creditUsed = false)
        return AppResult.Ok(res)
    }

    override suspend fun confirmRidePayment(
        rideId: String,
        requestId: String,
    ): AppResult<ConfirmPaymentResult> {
        lastConfirmRequestId = requestId
        val res = ConfirmPaymentResult(paidBookings = 1, total = 35)
        return AppResult.Ok(res)
    }
}
