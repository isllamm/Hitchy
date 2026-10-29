package com.hitchy.core.platform

import com.hitchy.core.data.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakePhoneAuth : PhoneAuth {
    private val _events = MutableSharedFlow<PhoneAuthEvent>(replay = 1)
    override val events: Flow<PhoneAuthEvent> = _events.asSharedFlow()

    var lastStartedPhone: String? = null
    var confirmResult: AppResult<Unit> = AppResult.Ok(Unit)

    fun emitEvent(event: PhoneAuthEvent) {
        _events.tryEmit(event)
    }

    override fun start(phoneE164: String, forceResend: Boolean) {
        lastStartedPhone = phoneE164
    }

    override suspend fun confirm(code: String): AppResult<Unit> = confirmResult
}
