package com.hitchy.feature.rides.data

import com.hitchy.core.data.AppResult
import com.hitchy.core.data.FunctionsClient
import com.hitchy.core.data.call
import com.hitchy.core.data.dto.CancelRideInputDto
import com.hitchy.core.data.dto.CancelRideOutputDto
import com.hitchy.core.data.dto.CancelRideReasonDto
import com.hitchy.core.data.dto.CreateRideInputDto
import com.hitchy.core.data.dto.CreateRideOutputDto
import com.hitchy.core.data.dto.DirectionDto
import com.hitchy.core.data.dto.QuotePriceInputDto
import com.hitchy.core.data.dto.QuotePriceOutputDto
import com.hitchy.core.data.dto.RideStopInputDto
import com.hitchy.core.data.dto.TrafficLevelDto
import com.hitchy.core.data.map
import com.hitchy.core.data.toIsoString
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.CancelRideReason
import com.hitchy.core.model.Direction
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.model.RideStatus
import com.hitchy.core.model.TrafficLevel
import com.hitchy.core.model.VehicleType
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.rides.domain.CreateRideInput
import com.hitchy.feature.rides.domain.Quote
import com.hitchy.feature.rides.domain.QuoteInput
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.domain.RidePriceRange
import com.hitchy.feature.rides.domain.RideStop
import com.hitchy.feature.rides.domain.RideVehicle
import com.hitchy.feature.rides.domain.RidesRepository
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
internal data class RideStopFirestore(
    val id: String = "",
    val zone: String = "",
    val pointId: String = "",
    val pointName: LocalizedName = LocalizedName(ar = "", en = ""),
    val point: GeoPoint = GeoPoint(lat = 0.0, lng = 0.0),
    val at: Timestamp = Timestamp.now(),
)

@Serializable
internal data class RideVehicleFirestore(
    val type: VehicleType = VehicleType.CAR,
    val make: String = "",
    val model: String = "",
    val color: String = "",
    val plate: String = "",
)

@Serializable
internal data class RidePriceRangeFirestore(
    val min: Int = 0,
    val recommended: Int = 0,
    val max: Int = 0,
    val trafficLevel: TrafficLevel = TrafficLevel.NORMAL,
    val distanceKm: Double = 0.0,
    val configVersion: Int = 1,
)

@Serializable
internal data class RideFirestoreDoc(
    val schemaVersion: Int = 1,
    val hostId: String = "",
    val hostCompanyId: String? = null,
    val vehicleId: String = "",
    val vehicle: RideVehicleFirestore = RideVehicleFirestore(),
    val fromZone: String = "",
    val fromPointId: String = "",
    val fromPointName: LocalizedName = LocalizedName(ar = "", en = ""),
    val fromPoint: GeoPoint = GeoPoint(lat = 0.0, lng = 0.0),
    val toZone: String = "",
    val toPointId: String = "",
    val toPointName: LocalizedName = LocalizedName(ar = "", en = ""),
    val stops: List<RideStopFirestore> = emptyList(),
    val pickupZones: List<String> = emptyList(),
    val direction: Direction = Direction.TO_WORK,
    val departAt: Timestamp = Timestamp.now(),
    val dateKey: String = "",
    val seatsTotal: Int = 1,
    val seatsLeft: Int = 0,
    val pricePerSeat: Int = 0,
    val priceRange: RidePriceRangeFirestore = RidePriceRangeFirestore(),
    val womenOnly: Boolean = false,
    val status: RideStatus = RideStatus.OPEN,
    val fixedRideId: String? = null,
    val bookingsCount: Int = 0,
    val creditConsumed: Boolean = false,
    val postProcessed: Boolean = false,
    val autoFinished: Boolean = false,
    val startedAt: Timestamp? = null,
    val finishedAt: Timestamp? = null,
    val cancelledAt: Timestamp? = null,
    val cancelReason: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)

@Serializable
internal data class BookingFirestoreDoc(
    val schemaVersion: Int = 1,
    val rideId: String = "",
    val riderUid: String = "",
    val hostId: String = "",
    val departAt: Timestamp = Timestamp.now(),
    val status: BookingStatus = BookingStatus.REQUESTED,
    val seats: Int = 1,
    val price: Int = 0,
    val total: Int = 0,
    val pickupId: String = "start",
    val pickupPointName: LocalizedName = LocalizedName(ar = "", en = ""),
    val pickupPoint: GeoPoint = GeoPoint(lat = 0.0, lng = 0.0),
    val pickupAt: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp = Timestamp.now(),
    val respondedAt: Timestamp? = null,
    val checkedInAt: Timestamp? = null,
    val checkInDistanceM: Int? = null,
    val hostArrivedAt: Timestamp? = null,
    val boardedAt: Timestamp? = null,
    val lateFeeId: String? = null,
    val paymentConfirmedAt: Timestamp? = null,
    val verifiedTogether: Boolean = false,
    val rewarded: Boolean = false,
    val lateCancel: Boolean = false,
    val cancelledAt: Timestamp? = null,
    val cancelReason: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)

internal fun RideFirestoreDoc.toDomain(id: String): Ride = Ride(
    id = id,
    hostId = hostId,
    hostCompanyId = hostCompanyId,
    vehicleId = vehicleId,
    vehicle = RideVehicle(
        type = vehicle.type,
        make = vehicle.make,
        model = vehicle.model,
        color = vehicle.color,
        plate = vehicle.plate,
    ),
    fromZone = fromZone,
    fromPointId = fromPointId,
    fromPointName = fromPointName,
    fromPoint = fromPoint,
    toZone = toZone,
    toPointId = toPointId,
    toPointName = toPointName,
    stops = stops.map { stop ->
        RideStop(
            id = stop.id,
            zone = stop.zone,
            pointId = stop.pointId,
            pointName = stop.pointName,
            point = stop.point,
            at = stop.at.toIsoString(),
        )
    },
    pickupZones = pickupZones,
    direction = direction,
    departAt = departAt.toIsoString(),
    dateKey = dateKey,
    seatsTotal = seatsTotal,
    seatsLeft = seatsLeft,
    pricePerSeat = pricePerSeat,
    priceRange = RidePriceRange(
        min = priceRange.min,
        recommended = priceRange.recommended,
        max = priceRange.max,
        trafficLevel = priceRange.trafficLevel,
        distanceKm = priceRange.distanceKm,
        configVersion = priceRange.configVersion,
    ),
    womenOnly = womenOnly,
    status = status,
    fixedRideId = fixedRideId,
    bookingsCount = bookingsCount,
    startedAt = startedAt?.toIsoString(),
    finishedAt = finishedAt?.toIsoString(),
    cancelledAt = cancelledAt?.toIsoString(),
    cancelReason = cancelReason,
)

internal fun BookingFirestoreDoc.toDomain(): Booking = Booking(
    rideId = rideId,
    riderUid = riderUid,
    hostId = hostId,
    departAt = departAt.toIsoString(),
    status = status,
    seats = seats,
    price = price,
    total = total,
    pickupId = pickupId,
    pickupPointName = pickupPointName,
    pickupPoint = pickupPoint,
    pickupAt = pickupAt.toIsoString(),
    expiresAt = expiresAt.toIsoString(),
    respondedAt = respondedAt?.toIsoString(),
    checkedInAt = checkedInAt?.toIsoString(),
    checkInDistanceM = checkInDistanceM,
    hostArrivedAt = hostArrivedAt?.toIsoString(),
    boardedAt = boardedAt?.toIsoString(),
    lateFeeId = lateFeeId,
    paymentConfirmedAt = paymentConfirmedAt?.toIsoString(),
    verifiedTogether = verifiedTogether,
    rewarded = rewarded,
    lateCancel = lateCancel,
    cancelledAt = cancelledAt?.toIsoString(),
    cancelReason = cancelReason,
)

class RidesRepositoryImpl(
    private val functionsClient: FunctionsClient,
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : RidesRepository {

    override suspend fun quote(input: QuoteInput): AppResult<Quote> {
        val dto = QuotePriceInputDto(
            vehicleId = input.vehicleId,
            fromZone = input.fromZone,
            toZone = input.toZone,
            departAt = input.departAt,
            seats = input.seats,
        )
        return functionsClient.call<QuotePriceInputDto, QuotePriceOutputDto>(
            name = "quotePrice",
            input = dto,
        ).map { out ->
            Quote(
                min = out.min,
                recommended = out.recommended,
                max = out.max,
                trafficLevel = when (out.trafficLevel) {
                    TrafficLevelDto.OFF_PEAK -> TrafficLevel.OFF_PEAK
                    TrafficLevelDto.NORMAL -> TrafficLevel.NORMAL
                    TrafficLevelDto.PEAK -> TrafficLevel.PEAK
                },
                distanceKm = out.distanceKm,
                fuelCostEgp = out.fuelCostEgp,
                tripCostEgp = out.tripCostEgp,
                configVersion = out.configVersion,
            )
        }
    }

    override suspend fun create(input: CreateRideInput): AppResult<String> {
        val dto = CreateRideInputDto(
            vehicleId = input.vehicleId,
            fromZone = input.fromZone,
            fromPointId = input.fromPointId,
            toZone = input.toZone,
            toPointId = input.toPointId,
            stops = input.stops.map { stop ->
                RideStopInputDto(
                    zone = stop.zone,
                    pointId = stop.pointId,
                    at = stop.at,
                )
            },
            direction = when (input.direction) {
                Direction.TO_WORK -> DirectionDto.TO_WORK
                Direction.TO_HOME -> DirectionDto.TO_HOME
            },
            departAt = input.departAt,
            seats = input.seats,
            pricePerSeat = input.pricePerSeat,
            womenOnly = input.womenOnly,
            requestId = input.requestId,
        )
        return functionsClient.call<CreateRideInputDto, CreateRideOutputDto>(
            name = "createRide",
            input = dto,
        ).map { it.rideId }
    }

    override suspend fun cancel(rideId: String, reason: CancelRideReason): AppResult<Int> {
        val dto = CancelRideInputDto(
            rideId = rideId,
            reason = when (reason) {
                CancelRideReason.SICK -> CancelRideReasonDto.SICK
                CancelRideReason.CAR_PROBLEM -> CancelRideReasonDto.CAR_PROBLEM
                CancelRideReason.PLANS_CHANGED -> CancelRideReasonDto.PLANS_CHANGED
                CancelRideReason.OTHER -> CancelRideReasonDto.OTHER
            },
        )
        return functionsClient.call<CancelRideInputDto, CancelRideOutputDto>(
            name = "cancelRide",
            input = dto,
        ).map { it.cancelledBookings }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMyRides(): Flow<List<Ride>> = auth.authStateChanged.flatMapLatest { firebaseUser ->
        val uid = firebaseUser?.uid
        if (uid == null) {
            flowOf(emptyList())
        } else {
            firestore
                .collection("rides")
                .where { "hostId" equalTo uid }
                .snapshots
                .map { querySnapshot ->
                    querySnapshot.documents.mapNotNull { doc ->
                        runCatching {
                            doc.data<RideFirestoreDoc>().toDomain(doc.id)
                        }.getOrNull()
                    }.sortedByDescending { it.departAt }.take(50)
                }
                .catch { emit(emptyList()) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRide(rideId: String): Flow<Ride?> = auth.authStateChanged.flatMapLatest { firebaseUser ->
        if (firebaseUser == null || rideId.isBlank()) {
            flowOf(null)
        } else {
            firestore
                .collection("rides")
                .document(rideId)
                .snapshots
                .map { snapshot ->
                    if (!snapshot.exists) {
                        null
                    } else {
                        runCatching {
                            snapshot.data<RideFirestoreDoc>().toDomain(snapshot.id)
                        }.getOrNull()
                    }
                }
                .catch { emit(null) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeBookings(rideId: String): Flow<List<Booking>> {
        val trimmedId = rideId.trim()
        return auth.authStateChanged.flatMapLatest { user ->
            if (user == null || trimmedId.isEmpty()) {
                flowOf(emptyList())
            } else {
                firestore
                    .collection("rides")
                    .document(trimmedId)
                    .collection("bookings")
                    .snapshots
                    .map { querySnapshot ->
                        querySnapshot.documents.mapNotNull { doc ->
                            runCatching {
                                doc.data<BookingFirestoreDoc>().toDomain()
                            }.getOrNull()
                        }
                    }
                    .catch { emit(emptyList()) }
            }
        }
    }
}
