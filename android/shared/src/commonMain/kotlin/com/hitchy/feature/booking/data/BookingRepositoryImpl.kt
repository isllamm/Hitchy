package com.hitchy.feature.booking.data

import com.hitchy.core.data.AppResult
import com.hitchy.core.data.FunctionsClient
import com.hitchy.core.data.call
import com.hitchy.core.data.dto.BookingStatusDto
import com.hitchy.core.data.dto.CancelBookingInputDto
import com.hitchy.core.data.dto.CancelBookingOutputDto
import com.hitchy.core.data.dto.CancelBookingReasonDto
import com.hitchy.core.data.dto.RequestSeatsInputDto
import com.hitchy.core.data.dto.RequestSeatsOutputDto
import com.hitchy.core.data.dto.RespondToRequestInputDto
import com.hitchy.core.data.dto.RespondToRequestOutputDto
import com.hitchy.core.data.dto.RideCardDto
import com.hitchy.core.data.dto.SearchRidesInputDto
import com.hitchy.core.data.dto.SearchRidesOutputDto
import com.hitchy.core.data.dto.SearchSortDto
import com.hitchy.core.data.dto.SearchTimeBandDto
import com.hitchy.core.data.dto.TierDto
import com.hitchy.core.data.dto.VehicleTypeDto
import com.hitchy.core.data.map
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.CancelBookingReason
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.model.SearchSort
import com.hitchy.core.model.SearchTimeBand
import com.hitchy.core.model.Tier
import com.hitchy.core.model.VehicleType
import com.hitchy.feature.booking.domain.BookingRepository
import com.hitchy.feature.booking.domain.RequestResult
import com.hitchy.feature.booking.domain.RequestedBooking
import com.hitchy.feature.booking.domain.RideCard
import com.hitchy.feature.booking.domain.RideHost
import com.hitchy.feature.booking.domain.RidePickup
import com.hitchy.feature.booking.domain.SearchInput
import com.hitchy.feature.rides.data.BookingFirestoreDoc
import com.hitchy.feature.rides.data.toDomain
import com.hitchy.feature.rides.domain.Booking
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class BookingRepositoryImpl(
    private val functionsClient: FunctionsClient,
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : BookingRepository {

    private val cachedCards = MutableStateFlow<Map<String, RideCard>>(emptyMap())

    override suspend fun search(input: SearchInput): AppResult<List<RideCard>> {
        val dto = SearchRidesInputDto(
            fromZone = input.fromZone,
            toZone = input.toZone,
            fromPointId = input.fromPointId,
            date = input.date,
            time = input.time,
            seats = input.seats,
            sort = when (input.sort) {
                SearchSort.RECOMMENDED -> SearchSortDto.RECOMMENDED
                SearchSort.EARLIEST -> SearchSortDto.EARLIEST
                SearchSort.CHEAPEST -> SearchSortDto.CHEAPEST
            },
            timeBand = when (input.timeBand) {
                SearchTimeBand.ANY -> SearchTimeBandDto.ANY
                SearchTimeBand.BEFORE -> SearchTimeBandDto.BEFORE
                SearchTimeBand.FIRST_HOUR -> SearchTimeBandDto.FIRST_HOUR
                SearchTimeBand.AFTER -> SearchTimeBandDto.AFTER
            },
            maxPrice = input.maxPrice,
            minRating = input.minRating,
            verifiedOnly = input.verifiedOnly,
            womenOnly = input.womenOnly,
        )
        return functionsClient.call<SearchRidesInputDto, SearchRidesOutputDto>(
            name = "searchRides",
            input = dto,
        ).map { out ->
            val cards = out.rides.map { it.toDomain() }
            cachedCards.update { prev -> prev + cards.associateBy { it.rideId } }
            cards
        }
    }

    override suspend fun request(
        rideId: String,
        seats: Int,
        pickupId: String,
        requestId: String,
    ): AppResult<RequestResult> = functionsClient.call<RequestSeatsInputDto, RequestSeatsOutputDto>(
        name = "requestSeats",
        input = RequestSeatsInputDto(
            rideId = rideId,
            seats = seats,
            pickupId = pickupId,
            requestId = requestId,
        ),
    ).map { out ->
        val card = out.ride.toDomain()
        cachedCards.update { prev -> prev + (card.rideId to card) }
        RequestResult(
            booking = RequestedBooking(
                rideId = out.booking.rideId,
                status = out.booking.status.toDomain(),
                seats = out.booking.seats,
                price = out.booking.price,
                total = out.booking.total,
                expiresAt = out.booking.expiresAt,
            ),
            ride = card,
        )
    }

    override suspend fun respond(
        rideId: String,
        riderUid: String,
        accept: Boolean,
    ): AppResult<BookingStatus> = functionsClient.call<RespondToRequestInputDto, RespondToRequestOutputDto>(
        name = "respondToRequest",
        input = RespondToRequestInputDto(
            rideId = rideId,
            riderUid = riderUid,
            accept = accept,
        ),
    ).map { it.status.toDomain() }

    override suspend fun cancel(
        rideId: String,
        reason: CancelBookingReason,
    ): AppResult<Boolean> = functionsClient.call<CancelBookingInputDto, CancelBookingOutputDto>(
        name = "cancelBooking",
        input = CancelBookingInputDto(
            rideId = rideId,
            reason = when (reason) {
                CancelBookingReason.HOST_LATE -> CancelBookingReasonDto.HOST_LATE
                CancelBookingReason.PLANS_CHANGED -> CancelBookingReasonDto.PLANS_CHANGED
                CancelBookingReason.FOUND_ANOTHER_RIDE -> CancelBookingReasonDto.FOUND_ANOTHER_RIDE
                CancelBookingReason.OTHER -> CancelBookingReasonDto.OTHER
            },
        ),
    ).map { it.lateCancel }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMyTrips(): Flow<List<Booking>> = auth.authStateChanged.flatMapLatest { firebaseUser ->
        val uid = firebaseUser?.uid
        if (uid == null) {
            flowOf(emptyList())
        } else {
            firestore
                .collectionGroup("bookings")
                .where { "riderUid" equalTo uid }
                .snapshots
                .map { querySnapshot ->
                    querySnapshot.documents.mapNotNull { doc ->
                        runCatching {
                            doc.data<BookingFirestoreDoc>().toDomain()
                        }.getOrNull()
                    }.sortedByDescending { it.departAt }.take(50)
                }
                .catch { emit(emptyList()) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeBooking(rideId: String): Flow<Booking?> = auth.authStateChanged.flatMapLatest { firebaseUser ->
        val uid = firebaseUser?.uid
        if (uid == null || rideId.isBlank()) {
            flowOf(null)
        } else {
            firestore
                .collection("rides")
                .document(rideId)
                .collection("bookings")
                .document(uid)
                .snapshots
                .map { snapshot ->
                    if (!snapshot.exists) {
                        null
                    } else {
                        runCatching {
                            snapshot.data<BookingFirestoreDoc>().toDomain()
                        }.getOrNull()
                    }
                }
                .catch { emit(null) }
        }
    }

    override fun observeCachedRideCard(rideId: String): Flow<RideCard?> = cachedCards.map { it[rideId] }
}

private fun RideCardDto.toDomain(): RideCard = RideCard(
    rideId = rideId,
    departAt = departAt,
    arriveAt = arriveAt,
    fromZone = fromZone,
    toZone = toZone,
    fromPointId = fromPointId,
    toPointId = toPointId,
    fromPointName = LocalizedName(ar = fromPointName.ar, en = fromPointName.en),
    toPointName = LocalizedName(ar = toPointName.ar, en = toPointName.en),
    pricePerSeat = pricePerSeat,
    fairPrice = fairPrice,
    seatsLeft = seatsLeft,
    walkM = walkM,
    pickup = RidePickup(
        id = pickup.id,
        name = LocalizedName(ar = pickup.name.ar, en = pickup.name.en),
        at = pickup.at,
        walkM = pickup.walkM,
    ),
    bestMatch = bestMatch,
    womenOnly = womenOnly,
    vehicleType = when (vehicleType) {
        VehicleTypeDto.CAR -> VehicleType.CAR
        VehicleTypeDto.SCOOTER -> VehicleType.SCOOTER
    },
    fixed = fixed,
    host = RideHost(
        uid = host.uid,
        firstName = host.firstName,
        photoPath = host.photoPath,
        rating = host.rating,
        ratingCount = host.ratingCount,
        trips = host.trips,
        verified = host.verified,
        tier = when (host.tier) {
            TierDto.BRONZE -> Tier.BRONZE
            TierDto.SILVER -> Tier.SILVER
            TierDto.GOLD -> Tier.GOLD
        },
        sameCompany = host.sameCompany,
    ),
)

private fun BookingStatusDto.toDomain(): BookingStatus = when (this) {
    BookingStatusDto.REQUESTED -> BookingStatus.REQUESTED
    BookingStatusDto.BOOKED -> BookingStatus.BOOKED
    BookingStatusDto.BOARDED -> BookingStatus.BOARDED
    BookingStatusDto.COMPLETED -> BookingStatus.COMPLETED
    BookingStatusDto.DECLINED -> BookingStatus.DECLINED
    BookingStatusDto.EXPIRED -> BookingStatus.EXPIRED
    BookingStatusDto.CANCELLED_BY_RIDER -> BookingStatus.CANCELLED_BY_RIDER
    BookingStatusDto.CANCELLED_BY_HOST -> BookingStatus.CANCELLED_BY_HOST
    BookingStatusDto.NO_SHOW -> BookingStatus.NO_SHOW
}
