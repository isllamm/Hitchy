package com.hitchy.feature.trip.data

import com.hitchy.core.data.AppResult
import com.hitchy.core.data.FunctionsClient
import com.hitchy.core.data.call
import com.hitchy.core.data.dto.ArriveAtStopInputDto
import com.hitchy.core.data.dto.ArriveAtStopOutputDto
import com.hitchy.core.data.dto.CheckInInputDto
import com.hitchy.core.data.dto.CheckInOutputDto
import com.hitchy.core.data.dto.ConfirmRidePaymentInputDto
import com.hitchy.core.data.dto.ConfirmRidePaymentOutputDto
import com.hitchy.core.data.dto.EmptyOutputDto
import com.hitchy.core.data.dto.FinishRideInputDto
import com.hitchy.core.data.dto.FinishRideOutputDto
import com.hitchy.core.data.dto.LateKindDto
import com.hitchy.core.data.dto.MarkBoardedInputDto
import com.hitchy.core.data.dto.ReportLateInputDto
import com.hitchy.core.data.dto.ReportLateOutputDto
import com.hitchy.core.data.dto.StartRideInputDto
import com.hitchy.core.data.map
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LateKind
import com.hitchy.feature.trip.domain.ConfirmPaymentResult
import com.hitchy.feature.trip.domain.FinishResult
import com.hitchy.feature.trip.domain.TripRepository

class TripRepositoryImpl(
    private val functionsClient: FunctionsClient,
) : TripRepository {

    override suspend fun start(rideId: String): AppResult<Unit> {
        val dto = StartRideInputDto(rideId = rideId)
        return functionsClient.call<StartRideInputDto, EmptyOutputDto>(
            name = "startRide",
            input = dto,
        ).map { Unit }
    }

    override suspend fun arriveAtStop(rideId: String, pickupId: String): AppResult<Int> {
        val dto = ArriveAtStopInputDto(rideId = rideId, pickupId = pickupId)
        return functionsClient.call<ArriveAtStopInputDto, ArriveAtStopOutputDto>(
            name = "arriveAtStop",
            input = dto,
        ).map { it.bookings }
    }

    override suspend fun checkIn(
        rideId: String,
        at: GeoPoint,
        accuracyM: Double,
    ): AppResult<Int> {
        val dto = CheckInInputDto(
            rideId = rideId,
            lat = at.lat,
            lng = at.lng,
            accuracyM = accuracyM,
        )
        return functionsClient.call<CheckInInputDto, CheckInOutputDto>(
            name = "checkIn",
            input = dto,
        ).map { it.distanceM }
    }

    override suspend fun markBoarded(rideId: String, riderUid: String): AppResult<Unit> {
        val dto = MarkBoardedInputDto(rideId = rideId, riderUid = riderUid)
        return functionsClient.call<MarkBoardedInputDto, EmptyOutputDto>(
            name = "markBoarded",
            input = dto,
        ).map { Unit }
    }

    override suspend fun reportLate(
        rideId: String,
        riderUid: String,
        kind: LateKind,
    ): AppResult<String> {
        val dto = ReportLateInputDto(
            rideId = rideId,
            riderUid = riderUid,
            kind = when (kind) {
                LateKind.LATE -> LateKindDto.LATE
                LateKind.NO_SHOW -> LateKindDto.NO_SHOW
            },
        )
        return functionsClient.call<ReportLateInputDto, ReportLateOutputDto>(
            name = "reportLate",
            input = dto,
        ).map { it.feeId }
    }

    override suspend fun finish(rideId: String): AppResult<FinishResult> {
        val dto = FinishRideInputDto(rideId = rideId)
        return functionsClient.call<FinishRideInputDto, FinishRideOutputDto>(
            name = "finishRide",
            input = dto,
        ).map { out ->
            FinishResult(
                completedBookings = out.completedBookings,
                creditUsed = out.creditUsed,
            )
        }
    }

    override suspend fun confirmRidePayment(
        rideId: String,
        requestId: String,
    ): AppResult<ConfirmPaymentResult> {
        val dto = ConfirmRidePaymentInputDto(rideId = rideId, requestId = requestId)
        return functionsClient.call<ConfirmRidePaymentInputDto, ConfirmRidePaymentOutputDto>(
            name = "confirmRidePayment",
            input = dto,
        ).map { out ->
            ConfirmPaymentResult(
                paidBookings = out.paidBookings,
                total = out.total,
            )
        }
    }
}
