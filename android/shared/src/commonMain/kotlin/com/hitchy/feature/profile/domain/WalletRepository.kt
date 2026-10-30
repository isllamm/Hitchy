package com.hitchy.feature.profile.domain

import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeBalance(): Flow<Int>
}
