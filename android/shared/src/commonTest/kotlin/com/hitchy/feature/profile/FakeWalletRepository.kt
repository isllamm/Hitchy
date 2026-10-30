package com.hitchy.feature.profile

import com.hitchy.feature.profile.domain.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWalletRepository(
    initialBalance: Int = 100,
) : WalletRepository {
    val balanceFlow = MutableStateFlow(initialBalance)

    override fun observeBalance(): Flow<Int> = balanceFlow
}
