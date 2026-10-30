package com.hitchy.feature.inbox

import com.hitchy.core.data.AppResult
import com.hitchy.feature.inbox.domain.InboxItem
import com.hitchy.feature.inbox.domain.InboxRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeInboxRepository(
    initialItems: List<InboxItem> = emptyList(),
) : InboxRepository {
    val itemsFlow = MutableStateFlow(initialItems)
    var lastMarkedReadIds: List<String>? = null

    override fun observe(): Flow<List<InboxItem>> = itemsFlow

    override suspend fun markRead(ids: List<String>): AppResult<Unit> {
        lastMarkedReadIds = ids
        itemsFlow.value = itemsFlow.value.map { item ->
            if (item.id in ids) item.copy(readAt = "2026-10-30T10:00:00Z") else item
        }
        return AppResult.Ok(Unit)
    }
}
