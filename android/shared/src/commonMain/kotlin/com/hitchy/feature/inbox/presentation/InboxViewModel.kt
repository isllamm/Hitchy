package com.hitchy.feature.inbox.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.inbox.domain.InboxItem
import com.hitchy.feature.inbox.domain.InboxRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class InboxState(
    val items: List<InboxItem> = emptyList(),
    val isLoading: Boolean = true,
) {
    val unreadIds: List<String>
        get() = items.filter { it.isUnread }.map { it.id }
}

sealed interface InboxAction {
    data class TapNotification(val item: InboxItem) : InboxAction
    data object MarkAllRead : InboxAction
}

sealed interface InboxEvent {
    data class OpenDeepLink(val deepLink: String) : InboxEvent
}

class InboxViewModel(
    private val inboxRepository: InboxRepository,
) : MviViewModel<InboxState, InboxAction, InboxEvent>(InboxState()) {

    init {
        inboxRepository.observe()
            .onEach { list -> setState { copy(items = list, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: InboxAction) {
        when (action) {
            is InboxAction.TapNotification -> {
                viewModelScope.launch {
                    if (action.item.isUnread) {
                        inboxRepository.markRead(listOf(action.item.id))
                    }
                    emit(InboxEvent.OpenDeepLink(action.item.deepLink))
                }
            }

            InboxAction.MarkAllRead -> {
                val unread = state.value.unreadIds
                if (unread.isNotEmpty()) {
                    viewModelScope.launch {
                        inboxRepository.markRead(unread)
                    }
                }
            }
        }
    }
}
