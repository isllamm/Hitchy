package com.hitchy.core.platform

import com.hitchy.core.model.Role
import kotlinx.coroutines.flow.Flow

interface AppSettings {
    val lastRole: Flow<Role?>
    suspend fun setLastRole(role: Role)
    val installId: Flow<String>
}
