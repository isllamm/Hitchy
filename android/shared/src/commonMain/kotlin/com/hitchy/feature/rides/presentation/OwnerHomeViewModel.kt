package com.hitchy.feature.rides.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.inbox.domain.InboxRepository
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.domain.RidesRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class OwnerHomeState(
    val firstName: String = "",
    val hasUnreadNotifications: Boolean = false,
    val upcomingRides: List<Ride> = emptyList(),
    val pendingRequestsCountByRide: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: AppError? = null,
)

sealed interface OwnerHomeAction {
    data object Refresh : OwnerHomeAction
    data object DismissError : OwnerHomeAction
}

sealed interface OwnerHomeEvent

class OwnerHomeViewModel(
    profileRepository: ProfileRepository,
    ridesRepository: RidesRepository,
    inboxRepository: InboxRepository,
) : MviViewModel<OwnerHomeState, OwnerHomeAction, OwnerHomeEvent>(OwnerHomeState()) {

    init {
        profileRepository.observeMe()
            .onEach { me ->
                if (me != null) setState { copy(firstName = me.firstName) }
            }
            .launchIn(viewModelScope)

        inboxRepository.observe()
            .onEach { items ->
                setState { copy(hasUnreadNotifications = items.any { it.isUnread }) }
            }
            .launchIn(viewModelScope)

        ridesRepository.observeMyRides()
            .onEach { rides ->
                setState { copy(upcomingRides = rides) }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: OwnerHomeAction) {
        when (action) {
            OwnerHomeAction.Refresh -> Unit
            OwnerHomeAction.DismissError -> setState { copy(error = null) }
        }
    }
}
