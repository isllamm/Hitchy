package com.hitchy.feature.rides.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.RideStatus
import com.hitchy.core.model.Role
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.inbox.domain.InboxRepository
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.rides.domain.Ride
import com.hitchy.feature.rides.domain.RidesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class OwnerHomeState(
    val firstName: String = "",
    val lastName: String = "",
    val pointsBalance: Int = 0,
    val hasUnreadNotifications: Boolean = false,
    val unreadCount: Int = 0,
    val isSwitchingRole: Boolean = false,
    val upcomingRides: List<Ride> = emptyList(),
    val pendingRequestsCountByRide: Map<String, Int> = emptyMap(),
    /** Seats held by requests that wait for the owner's answer, per open ride. */
    val pendingSeatsByRide: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: AppError? = null,
)

sealed interface OwnerHomeAction {
    data object Refresh : OwnerHomeAction
    data object DismissError : OwnerHomeAction

    /** Owner Home's "Request a ride" quick action: back to rider mode. */
    data object SwitchToRider : OwnerHomeAction
}

sealed interface OwnerHomeEvent

class OwnerHomeViewModel(
    private val profileRepository: ProfileRepository,
    ridesRepository: RidesRepository,
    inboxRepository: InboxRepository,
) : MviViewModel<OwnerHomeState, OwnerHomeAction, OwnerHomeEvent>(OwnerHomeState()) {

    init {
        profileRepository.observeMe()
            .onEach { me ->
                if (me != null) {
                    setState {
                        copy(firstName = me.firstName, lastName = me.lastName, pointsBalance = me.pointsBalance)
                    }
                }
            }
            .launchIn(viewModelScope)

        inboxRepository.observe()
            .onEach { items ->
                val unread = items.count { it.isUnread }
                setState { copy(hasUnreadNotifications = unread > 0, unreadCount = unread) }
            }
            .launchIn(viewModelScope)

        ridesRepository.observeMyRides()
            .onEach { rides ->
                setState { copy(upcomingRides = rides) }
            }
            .launchIn(viewModelScope)

        observePendingRequests(ridesRepository)
    }

    /** Counts the requests waiting for the owner's answer on each open ride (Owner Home's "New requests"). */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePendingRequests(ridesRepository: RidesRepository) {
        ridesRepository.observeMyRides()
            .flatMapLatest { rides ->
                val open = rides.filter { it.status == RideStatus.OPEN || it.status == RideStatus.FULL }
                if (open.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(open.map { ride -> ridesRepository.observeBookings(ride.id) }) { lists ->
                        open.indices.map { i -> open[i].id to lists[i].filter { it.status == BookingStatus.REQUESTED } }
                    }
                }
            }
            .onEach { pending ->
                setState {
                    copy(
                        pendingRequestsCountByRide = pending.associate { (id, list) -> id to list.size },
                        pendingSeatsByRide = pending.associate { (id, list) -> id to list.sumOf { it.seats } },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: OwnerHomeAction) {
        when (action) {
            OwnerHomeAction.Refresh -> Unit
            OwnerHomeAction.DismissError -> setState { copy(error = null) }
            OwnerHomeAction.SwitchToRider -> switchToRider()
        }
    }

    private fun switchToRider() {
        if (state.value.isSwitchingRole) return
        setState { copy(isSwitchingRole = true, error = null) }
        viewModelScope.launch {
            when (val result = profileRepository.switchRole(Role.RIDER)) {
                is AppResult.Ok -> setState { copy(isSwitchingRole = false) }
                is AppResult.Err -> setState { copy(isSwitchingRole = false, error = result.error) }
            }
        }
    }
}
