package com.hitchy.feature.debug.domain

import com.hitchy.core.model.PricingConfig
import kotlinx.coroutines.flow.Flow

interface DebugRepository {
    fun observePricingConfig(): Flow<PricingConfig?>
    fun observeCurrentUid(): Flow<String?>
    suspend fun signInAnonymously()
    suspend fun signOut()
}
