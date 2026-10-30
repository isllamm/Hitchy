package com.hitchy.feature.rides.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.model.Direction
import com.hitchy.core.model.Gender
import com.hitchy.core.platform.AppClock
import com.hitchy.core.ui.MviViewModel
import com.hitchy.core.util.cairoDateTime
import com.hitchy.core.util.dateKey
import com.hitchy.feature.host.domain.HostRepository
import com.hitchy.feature.host.domain.Vehicle
import com.hitchy.feature.places.domain.Point
import com.hitchy.feature.places.domain.PointSelectionHolder
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.rides.domain.CreateRideInput
import com.hitchy.feature.rides.domain.Quote
import com.hitchy.feature.rides.domain.QuoteInput
import com.hitchy.feature.rides.domain.RidesRepository
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** The two picker fields this wizard reads back from the point picker. */
const val FIELD_POST_FROM = "post_from"
const val FIELD_POST_TO = "post_to"

/** D2 is step 1, D4 is step 2, D5 is the result. D3's preferences are not built (ADR row 94). */
enum class PostRideStep { Route, Review, Live }

data class PostRideState(
    val step: PostRideStep = PostRideStep.Route,
    val vehicle: Vehicle? = null,
    val from: Point? = null,
    val to: Point? = null,
    val timeHHmm: String = "07:30",
    val seats: Int = 3,
    val womenOnly: Boolean = false,
    val canOfferWomenOnly: Boolean = false,
    val quote: Quote? = null,
    val pricePerSeat: Int = 0,
    val quoting: Boolean = false,
    val publishing: Boolean = false,
    val error: AppError? = null,
    val publishedRideId: String? = null,
) {
    /** The owner's route has to be two different fixed points before anything can be priced. */
    val routeReady: Boolean
        get() = from != null && to != null && from.zoneId != to.zoneId && vehicle != null

    val canContinue: Boolean get() = routeReady && !quoting

    /** Fair price is at or below the server's recommended figure (R6). */
    val fairPrice: Boolean get() = quote?.let { pricePerSeat <= it.recommended } == true

    val canPublish: Boolean get() = quote != null && pricePerSeat > 0 && !publishing

    /** The owner keeps the rest of the car, so the seats on offer never exceed the vehicle's. */
    val maxSeats: Int get() = vehicle?.maxSeats ?: 1
}

sealed interface PostRideAction {
    data class TimeChanged(val hhmm: String) : PostRideAction
    data class SeatsChanged(val seats: Int) : PostRideAction
    data class WomenOnlyChanged(val womenOnly: Boolean) : PostRideAction
    data class PriceChanged(val pricePerSeat: Int) : PostRideAction
    data object Continue : PostRideAction
    data object Back : PostRideAction
    data object Publish : PostRideAction
}

sealed interface PostRideEvent {
    data class Published(val rideId: String) : PostRideEvent
    data object Dismissed : PostRideEvent
}

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class PostRideViewModel(
    private val rides: RidesRepository,
    private val host: HostRepository,
    private val points: PointSelectionHolder,
    private val clock: AppClock,
    private val profile: ProfileRepository,
) : MviViewModel<PostRideState, PostRideAction, PostRideEvent>(PostRideState()) {

    init {
        // Only a woman owner may offer a Women-only ride (R8).
        profile.observeMe()
            .onEach { me -> setState { copy(canOfferWomenOnly = me?.gender == Gender.FEMALE) } }
            .launchIn(viewModelScope)

        // The first approved vehicle is the one the ride is posted with; P6-A02 adds the picker.
        host.observeVehicles()
            .onEach { list ->
                setState { copy(vehicle = vehicle ?: list.firstOrNull()) }
            }
            .launchIn(viewModelScope)

        // The point picker writes the chosen point back through the shared holder.
        points.selections
            .onEach { selected ->
                setState {
                    copy(from = selected[FIELD_POST_FROM] ?: from, to = selected[FIELD_POST_TO] ?: to)
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: PostRideAction) {
        when (action) {
            is PostRideAction.TimeChanged -> setState { copy(timeHHmm = action.hhmm, error = null) }

            is PostRideAction.SeatsChanged ->
                setState { copy(seats = action.seats.coerceIn(1, maxSeats), error = null) }

            is PostRideAction.WomenOnlyChanged ->
                setState { copy(womenOnly = action.womenOnly && canOfferWomenOnly) }

            is PostRideAction.PriceChanged -> {
                val range = state.value.quote ?: return
                setState { copy(pricePerSeat = action.pricePerSeat.coerceIn(range.min, range.max)) }
            }

            PostRideAction.Continue -> if (state.value.canContinue) loadQuote()

            PostRideAction.Back -> when (state.value.step) {
                PostRideStep.Route -> viewModelScope.launch { emit(PostRideEvent.Dismissed) }
                PostRideStep.Review -> setState { copy(step = PostRideStep.Route, error = null) }
                PostRideStep.Live -> viewModelScope.launch { emit(PostRideEvent.Dismissed) }
            }

            PostRideAction.Publish -> if (state.value.canPublish) publish()
        }
    }

    /** The server prices the ride; the slider only ever moves inside the range it returns (R6). */
    private fun loadQuote() {
        val current = state.value
        val vehicle = current.vehicle ?: return
        val from = current.from ?: return
        val to = current.to ?: return

        setState { copy(quoting = true, error = null) }
        viewModelScope.launch {
            val result = rides.quote(
                QuoteInput(
                    vehicleId = vehicle.id,
                    fromZone = from.zoneId,
                    toZone = to.zoneId,
                    departAt = departAtIso(),
                    seats = current.seats,
                ),
            )
            when (result) {
                is AppResult.Ok -> setState {
                    copy(
                        quoting = false,
                        quote = result.value,
                        // The slider opens on the recommended price, as P6-A02 asks.
                        pricePerSeat = result.value.recommended,
                        step = PostRideStep.Review,
                    )
                }

                is AppResult.Err -> setState { copy(quoting = false, error = result.error) }
            }
        }
    }

    private fun publish() {
        val current = state.value
        val vehicle = current.vehicle ?: return
        val from = current.from ?: return
        val to = current.to ?: return

        setState { copy(publishing = true, error = null) }
        viewModelScope.launch {
            val result = rides.create(
                CreateRideInput(
                    vehicleId = vehicle.id,
                    fromZone = from.zoneId,
                    fromPointId = from.id,
                    toZone = to.zoneId,
                    toPointId = to.id,
                    // Stops come with the owner's stop editor in P6-A02.
                    stops = emptyList(),
                    direction = directionFor(current.timeHHmm),
                    departAt = departAtIso(),
                    seats = current.seats,
                    pricePerSeat = current.pricePerSeat,
                    womenOnly = current.womenOnly,
                    requestId = newRequestId(),
                ),
            )
            when (result) {
                is AppResult.Ok -> {
                    setState {
                        copy(publishing = false, publishedRideId = result.value, step = PostRideStep.Live)
                    }
                    emit(PostRideEvent.Published(result.value))
                }

                is AppResult.Err -> setState { copy(publishing = false, error = result.error) }
            }
        }
    }

    /** Today in Cairo at the chosen time; the server refuses a departure too soon or too far. */
    private fun departAtIso(): String =
        cairoDateTime(dateKey(clock.now()), state.value.timeHHmm).toString()

    /** Before noon the owner is heading to work, after it they are heading home (P6-A02). */
    private fun directionFor(hhmm: String): Direction =
        if (hhmm.substringBefore(":").toInt() < NOON_HOUR) Direction.TO_WORK else Direction.TO_HOME

    /**
     * One id per publish attempt, so tapping Publish twice posts one ride (5.4). createRide's zod
     * input requires a UUID, so this is a real one rather than a composed string.
     */
    private fun newRequestId(): String = Uuid.random().toString()

    private companion object {
        const val NOON_HOUR = 12
    }
}
