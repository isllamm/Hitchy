package com.hitchy.feature.booking.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.ui.MviViewModel
import com.hitchy.core.util.dateKey
import com.hitchy.feature.inbox.domain.InboxRepository
import com.hitchy.feature.places.domain.Point
import com.hitchy.feature.places.domain.PointSelectionHolder
import com.hitchy.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

enum class RiderSearchValidationError {
    MISSING_ROUTE,
    SAME_ZONE,
}

private const val MINUTES_PER_HOUR = 60
private const val DEFAULT_DEPART_MINUTES = 8 * MINUTES_PER_HOUR
private const val MAX_SEATS = 3
private const val LAST_SLOT_MINUTES = 23 * MINUTES_PER_HOUR + 45

data class RiderHomeState(
    val firstName: String = "",
    val hasUnreadNotifications: Boolean = false,
    val unreadCount: Int = 0,
    val fromPoint: Point? = null,
    val toPoint: Point? = null,
    val todayDateKey: String = "",
    val tomorrowDateKey: String = "",
    val dateKey: String = "",
    val timeMinutes: Int = DEFAULT_DEPART_MINUTES,
    val seats: Int = 1,
    val showTimeSheet: Boolean = false,
    val validationError: RiderSearchValidationError? = null,
) {
    /** "HH:mm", the format searchRides takes. */
    val timeFormatted: String
        get() {
            val h = (timeMinutes / MINUTES_PER_HOUR).toString().padStart(2, '0')
            val m = (timeMinutes % MINUTES_PER_HOUR).toString().padStart(2, '0')
            return "$h:$m"
        }

    companion object {
        const val FIELD_FROM = "from"
        const val FIELD_TO = "to"
    }
}

sealed interface RiderHomeAction {
    data object SwapRoute : RiderHomeAction
    data object ClearFrom : RiderHomeAction
    data object ClearTo : RiderHomeAction
    data class SelectDate(val dateKey: String) : RiderHomeAction
    data class SelectTime(val timeMinutes: Int) : RiderHomeAction
    data class ToggleTimeSheet(val show: Boolean) : RiderHomeAction
    data class UpdateSeats(val seats: Int) : RiderHomeAction
    data object DismissError : RiderHomeAction
    data object SubmitSearch : RiderHomeAction
}

sealed interface RiderHomeEvent {
    data class NavigateToSearchResults(
        val fromZone: String,
        val toZone: String,
        val fromPointId: String,
        val date: String,
        val time: String,
        val seats: Int,
    ) : RiderHomeEvent
}

@OptIn(ExperimentalTime::class)
class RiderHomeViewModel(
    profileRepository: ProfileRepository,
    inboxRepository: InboxRepository,
    private val pointSelectionHolder: PointSelectionHolder,
    clock: Clock,
) : MviViewModel<RiderHomeState, RiderHomeAction, RiderHomeEvent>(initialState(clock)) {

    init {
        profileRepository.observeMe()
            .onEach { me -> if (me != null) setState { copy(firstName = me.firstName) } }
            .launchIn(viewModelScope)

        inboxRepository.observe()
            .onEach { items ->
                val unread = items.count { it.isUnread }
                setState { copy(hasUnreadNotifications = unread > 0, unreadCount = unread) }
            }
            .launchIn(viewModelScope)

        pointSelectionHolder.selections
            .onEach { map ->
                setState {
                    copy(
                        fromPoint = map[RiderHomeState.FIELD_FROM],
                        toPoint = map[RiderHomeState.FIELD_TO],
                        validationError = null,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: RiderHomeAction) {
        when (action) {
            RiderHomeAction.SwapRoute ->
                pointSelectionHolder.swap(RiderHomeState.FIELD_FROM, RiderHomeState.FIELD_TO)

            RiderHomeAction.ClearFrom -> pointSelectionHolder.clear(RiderHomeState.FIELD_FROM)

            RiderHomeAction.ClearTo -> pointSelectionHolder.clear(RiderHomeState.FIELD_TO)

            is RiderHomeAction.SelectDate -> setState { copy(dateKey = action.dateKey) }

            is RiderHomeAction.SelectTime -> setState {
                copy(timeMinutes = action.timeMinutes.coerceIn(0, LAST_SLOT_MINUTES), showTimeSheet = false)
            }

            is RiderHomeAction.ToggleTimeSheet -> setState { copy(showTimeSheet = action.show) }

            is RiderHomeAction.UpdateSeats -> setState { copy(seats = action.seats.coerceIn(1, MAX_SEATS)) }

            RiderHomeAction.DismissError -> setState { copy(validationError = null) }

            RiderHomeAction.SubmitSearch -> submitSearch()
        }
    }

    private fun submitSearch() {
        val current = state.value
        val from = current.fromPoint
        val to = current.toPoint
        val error = when {
            from == null || to == null -> RiderSearchValidationError.MISSING_ROUTE
            from.zoneId == to.zoneId -> RiderSearchValidationError.SAME_ZONE
            else -> null
        }
        if (error != null || from == null || to == null) {
            setState { copy(validationError = error) }
            return
        }
        viewModelScope.launch {
            emit(
                RiderHomeEvent.NavigateToSearchResults(
                    fromZone = from.zoneId,
                    toZone = to.zoneId,
                    fromPointId = from.id,
                    date = current.dateKey,
                    time = current.timeFormatted,
                    seats = current.seats,
                ),
            )
        }
    }

    private companion object {
        fun initialState(clock: Clock): RiderHomeState {
            val now = clock.now()
            val today = dateKey(now)
            return RiderHomeState(
                todayDateKey = today,
                tomorrowDateKey = dateKey(now + 1.days),
                dateKey = today,
            )
        }
    }
}
