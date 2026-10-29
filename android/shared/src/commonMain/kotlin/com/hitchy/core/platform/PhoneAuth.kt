package com.hitchy.core.platform

import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import kotlinx.coroutines.flow.Flow

interface PhoneAuth { // implemented in Phase 4
    val events: Flow<PhoneAuthEvent>
    fun start(phoneE164: String, forceResend: Boolean = false)
    suspend fun confirm(code: String): AppResult<Unit>
}

sealed interface PhoneAuthEvent {
    data class CodeSent(val canResendAtMs: Long) : PhoneAuthEvent
    data object AutoVerified : PhoneAuthEvent
    data class Failed(val error: AppError) : PhoneAuthEvent
}
