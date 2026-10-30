package com.hitchy.feature.booking.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.booking.domain.BookingRepository
import com.hitchy.feature.booking.domain.RideCard
import com.hitchy.feature.booking.domain.SearchInput
import kotlinx.coroutines.launch

data class SearchResultsState(
    val input: SearchInput? = null,
    val isLoading: Boolean = true,
    val rides: List<RideCard> = emptyList(),
    val error: AppError? = null,
)

sealed interface SearchResultsAction {
    data class Search(val input: SearchInput) : SearchResultsAction
    data object Retry : SearchResultsAction
}

sealed interface SearchResultsEvent

class SearchResultsViewModel(
    private val bookingRepository: BookingRepository,
) : MviViewModel<SearchResultsState, SearchResultsAction, SearchResultsEvent>(SearchResultsState()) {

    override fun onAction(action: SearchResultsAction) {
        when (action) {
            is SearchResultsAction.Search -> if (state.value.input != action.input) run(action.input)
            SearchResultsAction.Retry -> state.value.input?.let { run(it) }
        }
    }

    private fun run(input: SearchInput) {
        setState { copy(input = input, isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = bookingRepository.search(input)) {
                is AppResult.Ok -> setState { copy(isLoading = false, rides = result.value) }
                is AppResult.Err -> setState { copy(isLoading = false, rides = emptyList(), error = result.error) }
            }
        }
    }
}
