package com.hitchy.feature.debug

import com.hitchy.core.model.PricingConfig
import com.hitchy.feature.debug.domain.DebugRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeDebugRepository(
    initialUid: String? = null,
    initialPricingConfig: PricingConfig? = null,
) : DebugRepository {

    val uidFlow = MutableStateFlow(initialUid)
    val pricingFlow = MutableStateFlow(initialPricingConfig)

    var signInAnonymouslyCalled = false
    var signOutCalled = false

    override fun observePricingConfig(): Flow<PricingConfig?> = pricingFlow.asStateFlow()

    override fun observeCurrentUid(): Flow<String?> = uidFlow.asStateFlow()

    override suspend fun signInAnonymously() {
        signInAnonymouslyCalled = true
        uidFlow.value = "test-anon-uid"
    }

    override suspend fun signOut() {
        signOutCalled = true
        uidFlow.value = null
    }
}
