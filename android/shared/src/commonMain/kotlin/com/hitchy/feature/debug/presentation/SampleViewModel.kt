package com.hitchy.feature.debug.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.ui.MviViewModel
import kotlinx.coroutines.launch

data class SampleState(
    val isLoading: Boolean = false,
    val data: String? = null,
    val error: AppError? = null
)

sealed interface SampleAction {
    data object Load : SampleAction
}

sealed interface SampleEvent {
    data class LoadSuccess(val message: String) : SampleEvent
}

class SampleViewModel(
    private val loader: suspend () -> AppResult<String>
) : MviViewModel<SampleState, SampleAction, SampleEvent>(SampleState()) {

    override fun onAction(action: SampleAction) {
        when (action) {
            SampleAction.Load -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            when (val result = loader()) {
                is AppResult.Ok -> {
                    setState { copy(isLoading = false, data = result.value) }
                    emit(SampleEvent.LoadSuccess(result.value))
                }
                is AppResult.Err -> {
                    setState { copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
}
