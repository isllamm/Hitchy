package com.hitchy.feature.inbox.domain

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.LocalizedName
import kotlinx.coroutines.flow.Flow

data class InboxItem(
    val id: String,
    val type: String,
    val title: LocalizedName,
    val body: LocalizedName,
    val deepLink: String,
    val createdAt: String,
    val readAt: String?,
) {
    val isUnread: Boolean
        get() = readAt == null
}

interface InboxRepository {
    fun observe(): Flow<List<InboxItem>>
    suspend fun markRead(ids: List<String>): AppResult<Unit>
}
