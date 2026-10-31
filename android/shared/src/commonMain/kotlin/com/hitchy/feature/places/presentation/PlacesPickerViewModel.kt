package com.hitchy.feature.places.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.places.domain.PlacesRepository
import com.hitchy.feature.places.domain.Point
import com.hitchy.feature.places.domain.PointSelectionHolder
import com.hitchy.feature.places.domain.Zone
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class PlacesPickerState(
    val field: String = "from",
    val query: String = "",
    val zones: List<Zone> = emptyList(),
    val points: List<Point> = emptyList(),
    /** True once the pickup points have answered at least once. */
    val pointsLoaded: Boolean = false,
    val selectedPoint: Point? = null,
    /** The pickup and destination keys when [field] is one of a pair ("from"/"to"), else null. */
    val pair: Pair<String, String>? = null,
    val fromPoint: Point? = null,
    val toPoint: Point? = null,
) {
    val isFromActive: Boolean get() = pair?.first == this.field

    val filteredPoints: List<Point>
        get() {
            val q = query.trim().lowercase()
            if (q.isEmpty()) return points
            val zonesById = zones.associateBy { it.id }
            return points.filter { pt ->
                val zone = zonesById[pt.zoneId]
                pt.name.en.lowercase().contains(q) ||
                    pt.name.ar.lowercase().contains(q) ||
                    zone?.name?.en?.lowercase()?.contains(q) == true ||
                    zone?.name?.ar?.lowercase()?.contains(q) == true
            }
        }
}

sealed interface PlacesPickerAction {
    data class Init(val field: String) : PlacesPickerAction
    data class UpdateQuery(val query: String) : PlacesPickerAction
    data class HighlightPoint(val point: Point) : PlacesPickerAction

    /** [moveToEmptyField]: after a pickup, go on to an empty destination instead of closing. */
    data class ConfirmPoint(val point: Point, val moveToEmptyField: Boolean = false) : PlacesPickerAction

    /** Make the other field of the pair the one being picked. */
    data class SwitchField(val field: String) : PlacesPickerAction
    data object Swap : PlacesPickerAction
}

sealed interface PlacesPickerEvent {
    data object PointConfirmed : PlacesPickerEvent
}

class PlacesPickerViewModel(
    private val placesRepository: PlacesRepository,
    private val selectionHolder: PointSelectionHolder,
) : MviViewModel<PlacesPickerState, PlacesPickerAction, PlacesPickerEvent>(PlacesPickerState()) {

    init {
        placesRepository.observeZones()
            .onEach { list -> setState { copy(zones = list) } }
            .launchIn(viewModelScope)

        placesRepository.observeAllPoints()
            .onEach { list ->
                setState {
                    copy(
                        points = list,
                        pointsLoaded = true,
                        selectedPoint = selectedPoint ?: selectionHolder.get(field) ?: list.firstOrNull(),
                    )
                }
            }
            .launchIn(viewModelScope)

        selectionHolder.selections
            .onEach { map ->
                setState {
                    val p = pair ?: return@setState this
                    copy(fromPoint = map[p.first], toPoint = map[p.second])
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: PlacesPickerAction) {
        when (action) {
            is PlacesPickerAction.Init -> {
                val existing = selectionHolder.get(action.field)
                val pair = pairOf(action.field)
                setState {
                    copy(
                        field = action.field,
                        selectedPoint = existing ?: points.firstOrNull(),
                        pair = pair,
                        fromPoint = pair?.let { selectionHolder.get(it.first) },
                        toPoint = pair?.let { selectionHolder.get(it.second) },
                    )
                }
            }

            is PlacesPickerAction.UpdateQuery -> setState { copy(query = action.query) }

            is PlacesPickerAction.HighlightPoint -> setState { copy(selectedPoint = action.point) }

            is PlacesPickerAction.ConfirmPoint -> confirm(action)

            is PlacesPickerAction.SwitchField -> setState {
                copy(
                    field = action.field,
                    query = "",
                    selectedPoint = selectionHolder.get(action.field) ?: selectedPoint,
                )
            }

            PlacesPickerAction.Swap -> state.value.pair?.let { selectionHolder.swap(it.first, it.second) }
        }
    }
    private fun confirm(action: PlacesPickerAction.ConfirmPoint) {
        val current = state.value
        selectionHolder.select(current.field, action.point)
        val pair = current.pair
        val other = pair?.let { if (current.field == it.first) it.second else it.first }
        if (action.moveToEmptyField && other != null && selectionHolder.get(other) == null) {
            setState { copy(field = other, query = "", selectedPoint = points.firstOrNull()) }
        } else {
            viewModelScope.launch { emit(PlacesPickerEvent.PointConfirmed) }
        }
    }
}

/** "from" and "to" (or "post_ride_from" and "post_ride_to") belong together; other fields stand alone. */
internal fun pairOf(field: String): Pair<String, String>? = when {
    field.endsWith(FROM_SUFFIX) -> field to field.removeSuffix(FROM_SUFFIX) + TO_SUFFIX
    field.endsWith(TO_SUFFIX) -> field.removeSuffix(TO_SUFFIX) + FROM_SUFFIX to field
    else -> null
}

private const val FROM_SUFFIX = "from"
private const val TO_SUFFIX = "to"
