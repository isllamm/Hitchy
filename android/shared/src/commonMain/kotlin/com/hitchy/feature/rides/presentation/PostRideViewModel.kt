package com.hitchy.feature.rides.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.model.Direction
import com.hitchy.core.model.VehicleStatus
import com.hitchy.core.ui.MviViewModel
import com.hitchy.core.util.cairoDateTime
import com.hitchy.core.util.dateKey
import com.hitchy.feature.host.domain.HostRepository
import com.hitchy.feature.host.domain.Vehicle
import com.hitchy.feature.places.domain.Point
import com.hitchy.feature.places.domain.PointSelectionHolder
import com.hitchy.feature.rides.domain.CreateRideInput
import com.hitchy.feature.rides.domain.Quote
import com.hitchy.feature.rides.domain.QuoteInput
import com.hitchy.feature.rides.domain.RideStopInput
import com.hitchy.feature.rides.domain.RidesRepository
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.post_ride_error_missing_route
import hitchy.shared.generated.resources.post_ride_error_same_zone
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

data class PostRideStopState(
    val index: Int,
    val point: Point?,
    val minutesFromDepart: Int = 15,
)

data class PostRideState(
    val fromPoint: Point? = null,
    val toPoint: Point? = null,
    val stops: List<PostRideStopState> = emptyList(),
    val todayDateKey: String = "",
    val tomorrowDateKey: String = "",
    val selectedDateKey: String = "",
    val departTimeMinutes: Int = DEFAULT_DEPART_MINUTES,
    val seats: Int = DEFAULT_SEATS,
    val maxSeats: Int = MAX_ALLOWED_SEATS,
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicleId: String? = null,
    val womenOnly: Boolean = false,
    val quote: Quote? = null,
    val isQuoting: Boolean = false,
    val pricePerSeat: Int = DEFAULT_PRICE,
    val isSubmitting: Boolean = false,
    val validationError: StringResource? = null,
    val error: AppError? = null,
) {
    val departTimeFormatted: String
        get() {
            val hours = departTimeMinutes / MINUTES_PER_HOUR
            val minutes = departTimeMinutes % MINUTES_PER_HOUR
            return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
        }

    val expectedEarnings: Int
        get() = pricePerSeat * seats

    companion object {
        const val FIELD_FROM = "post_ride_from"
        const val FIELD_TO = "post_ride_to"
        const val FIELD_STOP_PREFIX = "post_ride_stop_"
        const val DEFAULT_DEPART_MINUTES = 480 // 08:00
        const val DEFAULT_SEATS = 3
        const val MAX_ALLOWED_SEATS = 4
        const val DEFAULT_PRICE = 35
        const val MINUTES_PER_HOUR = 60
        const val MIDDAY_MINUTES = 720
        const val MAX_STOPS = 3
        const val STOP_OFFSET_MINUTES = 15
    }
}

sealed interface PostRideAction {
    data class SelectDate(val dateKey: String) : PostRideAction
    data class SelectTime(val timeMinutes: Int) : PostRideAction
    data class UpdateSeats(val seats: Int) : PostRideAction
    data class SelectVehicle(val vehicleId: String) : PostRideAction
    data class ToggleWomenOnly(val enabled: Boolean) : PostRideAction
    data class UpdatePrice(val price: Int) : PostRideAction
    data object AddStop : PostRideAction
    data class RemoveStop(val index: Int) : PostRideAction
    data object SwapRoute : PostRideAction
    data object DismissError : PostRideAction
    data object Submit : PostRideAction
}

sealed interface PostRideEvent {
    data class NavigateToRideDetails(val rideId: String) : PostRideEvent
}

@OptIn(ExperimentalTime::class)
class PostRideViewModel(
    private val ridesRepository: RidesRepository,
    hostRepository: HostRepository,
    private val pointSelectionHolder: PointSelectionHolder,
    private val clock: Clock,
) : MviViewModel<PostRideState, PostRideAction, PostRideEvent>(initialState(clock)) {

    init {
        hostRepository.observeVehicles()
            .onEach { allVehicles ->
                val approved = allVehicles.filter { it.status == VehicleStatus.APPROVED }
                val initialVehicleId = approved.firstOrNull()?.id ?: allVehicles.firstOrNull()?.id
                setState {
                    copy(
                        vehicles = approved.ifEmpty { allVehicles },
                        selectedVehicleId = selectedVehicleId ?: initialVehicleId,
                    )
                }
            }
            .launchIn(viewModelScope)

        pointSelectionHolder.selections
            .onEach { selections ->
                val from = selections[PostRideState.FIELD_FROM]
                val to = selections[PostRideState.FIELD_TO]
                val updatedStops = state.value.stops.mapIndexed { idx, stop ->
                    val point = selections["${PostRideState.FIELD_STOP_PREFIX}$idx"]
                    stop.copy(point = point)
                }
                setState {
                    copy(
                        fromPoint = from,
                        toPoint = to,
                        stops = updatedStops,
                        validationError = null,
                    )
                }
                requestQuote()
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: PostRideAction) {
        when (action) {
            is PostRideAction.SelectDate -> {
                setState { copy(selectedDateKey = action.dateKey) }
                requestQuote()
            }

            is PostRideAction.SelectTime -> {
                setState { copy(departTimeMinutes = action.timeMinutes) }
                requestQuote()
            }

            is PostRideAction.UpdateSeats -> {
                val clamped = action.seats.coerceIn(1, state.value.maxSeats)
                setState { copy(seats = clamped) }
                requestQuote()
            }

            is PostRideAction.SelectVehicle -> {
                setState { copy(selectedVehicleId = action.vehicleId) }
                requestQuote()
            }

            is PostRideAction.ToggleWomenOnly -> setState { copy(womenOnly = action.enabled) }

            is PostRideAction.UpdatePrice -> {
                val quote = state.value.quote
                val clamped = if (quote != null) {
                    action.price.coerceIn(quote.min, quote.max)
                } else {
                    action.price.coerceAtLeast(1)
                }
                setState { copy(pricePerSeat = clamped) }
            }

            PostRideAction.AddStop -> {
                val current = state.value.stops
                if (current.size < PostRideState.MAX_STOPS) {
                    val nextIdx = current.size
                    val newStop = PostRideStopState(
                        index = nextIdx,
                        point = null,
                        minutesFromDepart = (nextIdx + 1) * PostRideState.STOP_OFFSET_MINUTES,
                    )
                    setState { copy(stops = current + newStop) }
                }
            }

            is PostRideAction.RemoveStop -> {
                pointSelectionHolder.clear("${PostRideState.FIELD_STOP_PREFIX}${action.index}")
                val filtered = state.value.stops
                    .filterNot { it.index == action.index }
                    .mapIndexed { idx, s -> s.copy(index = idx) }
                setState { copy(stops = filtered) }
            }

            PostRideAction.SwapRoute -> {
                pointSelectionHolder.swap(PostRideState.FIELD_FROM, PostRideState.FIELD_TO)
            }

            PostRideAction.DismissError -> setState { copy(validationError = null, error = null) }

            PostRideAction.Submit -> submit()
        }
    }

    private fun requestQuote() {
        val current = state.value
        val from = current.fromPoint ?: return
        val to = current.toPoint ?: return
        val vehicleId = current.selectedVehicleId ?: "veh-default"
        if (from.zoneId == to.zoneId) return

        val departAtIso = runCatching {
            cairoDateTime(current.selectedDateKey, current.departTimeFormatted).toString()
        }.getOrNull() ?: return

        setState { copy(isQuoting = true) }
        viewModelScope.launch {
            val res = ridesRepository.quote(
                QuoteInput(
                    vehicleId = vehicleId,
                    fromZone = from.zoneId,
                    toZone = to.zoneId,
                    departAt = departAtIso,
                    seats = current.seats,
                ),
            )
            when (res) {
                is AppResult.Ok -> {
                    val q = res.value
                    setState {
                        copy(
                            quote = q,
                            isQuoting = false,
                            pricePerSeat = q.recommended,
                        )
                    }
                }

                is AppResult.Err -> setState { copy(isQuoting = false) }
            }
        }
    }

    private fun submit() {
        val current = state.value
        val from = current.fromPoint
        val to = current.toPoint
        if (from == null || to == null) {
            setState { copy(validationError = Res.string.post_ride_error_missing_route) }
            return
        }
        if (from.zoneId == to.zoneId) {
            setState { copy(validationError = Res.string.post_ride_error_same_zone) }
            return
        }

        val vehicleId = current.selectedVehicleId ?: "veh-1"
        val departInstant = runCatching {
            cairoDateTime(current.selectedDateKey, current.departTimeFormatted)
        }.getOrNull()
        if (departInstant == null) {
            setState { copy(validationError = Res.string.post_ride_error_missing_route) }
            return
        }

        val stopsInput = current.stops.mapNotNull { s ->
            val p = s.point ?: return@mapNotNull null
            val stopInstant = departInstant + s.minutesFromDepart.minutes
            RideStopInput(
                zone = p.zoneId,
                pointId = p.id,
                at = stopInstant.toString(),
            )
        }

        val direction = if (current.departTimeMinutes < PostRideState.MIDDAY_MINUTES) {
            Direction.TO_WORK
        } else {
            Direction.TO_HOME
        }

        setState { copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val reqId = "post-ride-${clock.now().toEpochMilliseconds()}"
            val res = ridesRepository.create(
                CreateRideInput(
                    vehicleId = vehicleId,
                    fromZone = from.zoneId,
                    fromPointId = from.id,
                    toZone = to.zoneId,
                    toPointId = to.id,
                    stops = stopsInput,
                    direction = direction,
                    departAt = departInstant.toString(),
                    seats = current.seats,
                    pricePerSeat = current.pricePerSeat,
                    womenOnly = current.womenOnly,
                    requestId = reqId,
                ),
            )
            when (res) {
                is AppResult.Ok -> {
                    setState { copy(isSubmitting = false) }
                    emit(PostRideEvent.NavigateToRideDetails(res.value))
                }

                is AppResult.Err -> {
                    setState { copy(isSubmitting = false, error = res.error) }
                }
            }
        }
    }

    companion object {
        fun initialState(clock: Clock): PostRideState {
            val now = clock.now()
            val today = dateKey(now)
            val tomorrow = dateKey(now + 1.days)
            return PostRideState(
                todayDateKey = today,
                tomorrowDateKey = tomorrow,
                selectedDateKey = today,
            )
        }
    }
}
