package com.hitchy.feature.trip.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.LateKind
import com.hitchy.core.model.RideStatus
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.profile.domain.WalletRepository
import com.hitchy.feature.rides.domain.Booking
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.domain.RidesRepository
import com.hitchy.feature.trip.domain.TripRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class HostTripPhase {
    LEAVING,
    DRIVING_TO_STOP,
    AT_STOP,
    EN_ROUTE,
    ARRIVED,
    PAYMENT_DONE,
}

data class HostTripState(
    val rideId: String = "",
    val ride: Ride? = null,
    val bookings: List<Booking> = emptyList(),
    val riderNames: Map<String, String> = emptyMap(),
    val riderPhotos: Map<String, String?> = emptyMap(),
    val phase: HostTripPhase = HostTripPhase.LEAVING,
    val currentStopIndex: Int = 0,
    val currentStopName: String = "",
    val freeWaitSecondsLeft: Int = FREE_WAIT_SECONDS,
    val isFreeWaitOver: Boolean = false,
    val lateSeconds: Int = 0,
    val isLoading: Boolean = false,
    val paymentTotalCollected: Int = 0,
    val paymentBookingsCount: Int = 0,
    val walletBalance: Int = 0,
    val error: AppError? = null,
) {
    val boardedCount: Int
        get() = bookings.count { it.boardedAt != null }

    val totalBookedSeats: Int
        get() = bookings.sumOf { it.seats }

    /** The ride's pickups in driving order: the start point ("start"), then each stop. */
    val pickupIds: List<String>
        get() = listOf(START_PICKUP) + ride?.stops.orEmpty().map { it.id }

    /** The pickup the owner is driving to or waiting at; [currentStopIndex] counts the start as 0. */
    val currentPickupId: String
        get() = pickupIds.getOrElse(currentStopIndex) { START_PICKUP }

    /** The first pickup from [from] that still has a rider to board, or null when everyone is on board. */
    fun nextPickupIndex(from: Int): Int? = (from until pickupIds.size).firstOrNull { i ->
        bookings.any { it.pickupId == pickupIds[i] && it.boardedAt == null }
    }

    companion object {
        const val FREE_WAIT_SECONDS = 300
        const val START_PICKUP = "start"
        const val TIMER_DELAY_MS = 1000L
    }
}

sealed interface HostTripAction {
    data class Init(val rideId: String) : HostTripAction
    data object StartTrip : HostTripAction
    data object ArriveAtStop : HostTripAction
    data class MarkBoarded(val riderUid: String) : HostTripAction
    data class ReportLate(val riderUid: String) : HostTripAction
    data object NextStopOrFinish : HostTripAction
    data object FinishTrip : HostTripAction
    data object ConfirmPayment : HostTripAction
    data object DismissError : HostTripAction
}

sealed interface HostTripEvent {
    data object TripFinished : HostTripEvent
}

@Suppress("TooManyFunctions")
@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class HostTripViewModel(
    private val ridesRepository: RidesRepository,
    private val tripRepository: TripRepository,
    private val walletRepository: WalletRepository,
    private val profileRepository: ProfileRepository,
    private val clock: Clock,
) : MviViewModel<HostTripState, HostTripAction, HostTripEvent>(HostTripState()) {

    private var timerJob: Job? = null
    private val namesRequested = mutableSetOf<String>()

    override fun onAction(action: HostTripAction) {
        when (action) {
            is HostTripAction.Init -> init(action.rideId)
            HostTripAction.StartTrip -> startTrip()
            HostTripAction.ArriveAtStop -> arriveAtStop()
            is HostTripAction.MarkBoarded -> markBoarded(action.riderUid)
            is HostTripAction.ReportLate -> reportLate(action.riderUid)
            HostTripAction.NextStopOrFinish -> nextStopOrFinish()
            HostTripAction.FinishTrip -> finishTrip()
            HostTripAction.ConfirmPayment -> confirmPayment()
            HostTripAction.DismissError -> setState { copy(error = null) }
        }
    }

    private fun init(rideId: String) {
        if (state.value.rideId == rideId) return
        setState { copy(rideId = rideId) }

        ridesRepository.observeRide(rideId)
            .onEach { r ->
                if (r != null) {
                    val initialPhase = when (r.status) {
                        RideStatus.OPEN, RideStatus.FULL -> HostTripPhase.LEAVING

                        RideStatus.STARTED -> {
                            if (state.value.phase == HostTripPhase.LEAVING) {
                                HostTripPhase.DRIVING_TO_STOP
                            } else {
                                state.value.phase
                            }
                        }

                        RideStatus.FINISHED -> {
                            if (state.value.phase == HostTripPhase.PAYMENT_DONE) {
                                HostTripPhase.PAYMENT_DONE
                            } else {
                                HostTripPhase.ARRIVED
                            }
                        }

                        RideStatus.CANCELLED -> HostTripPhase.LEAVING

                        RideStatus.EXPIRED -> HostTripPhase.LEAVING
                    }
                    val stopName = r.fromPointName.let { it.en.ifBlank { it.ar } }
                    setState {
                        copy(
                            ride = r,
                            phase = initialPhase,
                            currentStopName = currentStopName.ifBlank { stopName },
                        )
                    }
                    skipEmptyPickup()
                }
            }
            .launchIn(viewModelScope)

        ridesRepository.observeBookings(rideId)
            .onEach { allBookings ->
                loadRiderNames(allBookings.map { it.riderUid })
                // Boarded riders stay on the list: the panel counts them as on board.
                val active = allBookings.filter {
                    it.status == BookingStatus.BOOKED ||
                        it.status == BookingStatus.BOARDED ||
                        it.status == BookingStatus.COMPLETED
                }
                setState { copy(bookings = active) }
                skipEmptyPickup()
            }
            .launchIn(viewModelScope)

        walletRepository.observeBalance()
            .onEach { balance -> setState { copy(walletBalance = balance) } }
            .launchIn(viewModelScope)
    }

    private fun loadRiderNames(uids: List<String>) {
        for (uid in uids) {
            if (!namesRequested.add(uid)) continue
            profileRepository.observePublicProfile(uid)
                .onEach { profile ->
                    if (profile != null) {
                        setState {
                            copy(
                                riderNames = riderNames + (uid to profile.firstName),
                                riderPhotos = riderPhotos + (uid to profile.photoPath),
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun startTrip() {
        val rideId = state.value.rideId
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val res = tripRepository.start(rideId)) {
                is AppResult.Ok -> {
                    val first = state.value.nextPickupIndex(0)
                    setState {
                        if (first == null) {
                            copy(isLoading = false, phase = HostTripPhase.EN_ROUTE)
                        } else {
                            copy(
                                isLoading = false,
                                phase = HostTripPhase.DRIVING_TO_STOP,
                                currentStopIndex = first,
                                currentStopName = pickupName(first),
                            )
                        }
                    }
                }

                is AppResult.Err -> setState { copy(isLoading = false, error = res.error) }
            }
        }
    }

    private fun arriveAtStop() {
        val rideId = state.value.rideId
        val pickupId = state.value.currentPickupId

        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val res = tripRepository.arriveAtStop(rideId, pickupId)) {
                is AppResult.Ok -> {
                    setState {
                        copy(
                            isLoading = false,
                            phase = HostTripPhase.AT_STOP,
                            freeWaitSecondsLeft = HostTripState.FREE_WAIT_SECONDS,
                            isFreeWaitOver = false,
                            lateSeconds = 0,
                        )
                    }
                    startWaitTimer()
                }

                is AppResult.Err -> setState { copy(isLoading = false, error = res.error) }
            }
        }
    }

    private fun startWaitTimer() {
        timerJob?.cancel()
        val startMs = clock.now().toEpochMilliseconds()
        timerJob = viewModelScope.launch {
            while (state.value.phase == HostTripPhase.AT_STOP) {
                delay(HostTripState.TIMER_DELAY_MS)
                val elapsedSec = ((clock.now().toEpochMilliseconds() - startMs) / MILLIS_PER_SECOND).toInt()
                val remaining = HostTripState.FREE_WAIT_SECONDS - elapsedSec
                if (remaining >= 0) {
                    setState {
                        copy(
                            freeWaitSecondsLeft = remaining,
                            isFreeWaitOver = false,
                            lateSeconds = 0,
                        )
                    }
                } else {
                    setState {
                        copy(
                            freeWaitSecondsLeft = 0,
                            isFreeWaitOver = true,
                            lateSeconds = -remaining,
                        )
                    }
                }
            }
        }
    }

    private fun markBoarded(riderUid: String) {
        val rideId = state.value.rideId
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val res = tripRepository.markBoarded(rideId, riderUid)) {
                is AppResult.Ok -> {
                    val updated = state.value.bookings.map { b ->
                        if (b.riderUid == riderUid) b.copy(boardedAt = clock.now().toString()) else b
                    }
                    setState { copy(isLoading = false, bookings = updated) }
                }

                is AppResult.Err -> setState { copy(isLoading = false, error = res.error) }
            }
        }
    }

    private fun reportLate(riderUid: String) {
        val rideId = state.value.rideId
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val res = tripRepository.reportLate(rideId, riderUid, LateKind.LATE)) {
                is AppResult.Ok -> {
                    val updated = state.value.bookings.map { b ->
                        if (b.riderUid == riderUid) b.copy(lateFeeId = res.value) else b
                    }
                    setState { copy(isLoading = false, bookings = updated) }
                }

                is AppResult.Err -> setState { copy(isLoading = false, error = res.error) }
            }
        }
    }

    private fun nextStopOrFinish() {
        timerJob?.cancel()
        // Pickups after this one that still have a rider to board; the rest are skipped.
        val nextIdx = state.value.nextPickupIndex(state.value.currentStopIndex + 1)
        if (nextIdx != null) {
            setState {
                copy(
                    currentStopIndex = nextIdx,
                    currentStopName = pickupName(nextIdx),
                    phase = HostTripPhase.DRIVING_TO_STOP,
                )
            }
        } else {
            // All stops visited, on the way to destination
            setState { copy(phase = HostTripPhase.EN_ROUTE) }
        }
    }

    /**
     * While driving to a pickup whose riders are all on board (the screen was reopened mid-trip), move on to
     * the next pickup with someone to board, or to the destination.
     */
    private fun skipEmptyPickup() {
        val st = state.value
        // Both the ride (its stops) and the bookings must be in before the pickups can be judged.
        if (st.phase != HostTripPhase.DRIVING_TO_STOP || st.ride == null || st.bookings.isEmpty()) return
        when (val next = st.nextPickupIndex(st.currentStopIndex)) {
            null -> setState { copy(phase = HostTripPhase.EN_ROUTE) }
            st.currentStopIndex -> Unit
            else -> setState { copy(currentStopIndex = next, currentStopName = pickupName(next)) }
        }
    }

    /** The English name (else Arabic) of pickup [index]: 0 is the start point, then the stops. */
    private fun pickupName(index: Int): String {
        val r = state.value.ride ?: return ""
        val name = if (index == 0) r.fromPointName else r.stops.getOrNull(index - 1)?.pointName
        return name?.let { it.en.ifBlank { it.ar } }.orEmpty()
    }

    private fun finishTrip() {
        val rideId = state.value.rideId
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val res = tripRepository.finish(rideId)) {
                is AppResult.Ok -> {
                    setState {
                        copy(
                            isLoading = false,
                            phase = HostTripPhase.ARRIVED,
                        )
                    }
                }

                is AppResult.Err -> setState { copy(isLoading = false, error = res.error) }
            }
        }
    }

    private fun confirmPayment() {
        val rideId = state.value.rideId
        // The server needs a UUID request id; it makes a repeated tap pay only once.
        val reqId = Uuid.random().toString()
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val res = tripRepository.confirmRidePayment(rideId, reqId)) {
                is AppResult.Ok -> {
                    setState {
                        copy(
                            isLoading = false,
                            phase = HostTripPhase.PAYMENT_DONE,
                            paymentTotalCollected = res.value.total,
                            paymentBookingsCount = res.value.paidBookings,
                        )
                    }
                }

                is AppResult.Err -> setState { copy(isLoading = false, error = res.error) }
            }
        }
    }
}

private const val MILLIS_PER_SECOND = 1000L
