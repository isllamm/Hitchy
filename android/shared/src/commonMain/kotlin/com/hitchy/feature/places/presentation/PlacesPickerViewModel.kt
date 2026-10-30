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
    val selectedPoint: Point? = null,
) {
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
    data class ConfirmPoint(val point: Point) : PlacesPickerAction
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
                        selectedPoint = selectedPoint ?: selectionHolder.get(field) ?: list.firstOrNull(),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: PlacesPickerAction) {
        when (action) {
            is PlacesPickerAction.Init -> {
                val existing = selectionHolder.get(action.field)
                setState {
                    copy(
                        field = action.field,
                        selectedPoint = existing ?: points.firstOrNull(),
                    )
                }
            }

            is PlacesPickerAction.UpdateQuery -> setState { copy(query = action.query) }

            is PlacesPickerAction.HighlightPoint -> setState { copy(selectedPoint = action.point) }

            is PlacesPickerAction.ConfirmPoint -> {
                selectionHolder.select(state.value.field, action.point)
                viewModelScope.launch {
                    emit(PlacesPickerEvent.PointConfirmed)
                }
            }
        }
    }
}
