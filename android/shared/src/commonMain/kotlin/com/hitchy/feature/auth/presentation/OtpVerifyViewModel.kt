package com.hitchy.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.platform.PhoneAuth
import com.hitchy.core.platform.PhoneAuthEvent
import com.hitchy.core.ui.MviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class OtpVerifyState(
    val phoneE164: String = "",
    val code: String = "",
    val isVerifying: Boolean = false,
    val resendSecondsLeft: Int = 0,
    val error: AppError? = null,
) {
    val canResend: Boolean get() = resendSecondsLeft <= 0 && !isVerifying
}

sealed interface OtpVerifyAction {
    data class Init(val phoneE164: String) : OtpVerifyAction
    data class UpdateCode(val code: String) : OtpVerifyAction
    data object Submit : OtpVerifyAction
    data object Resend : OtpVerifyAction
}

sealed interface OtpVerifyEvent {
    data object Verified : OtpVerifyEvent
}

@OptIn(ExperimentalTime::class)
class OtpVerifyViewModel(
    private val phoneAuth: PhoneAuth,
    private val clock: Clock,
) : MviViewModel<OtpVerifyState, OtpVerifyAction, OtpVerifyEvent>(OtpVerifyState()) {

    private var countdownJob: Job? = null

    init {
        phoneAuth.events
            .onEach { event ->
                when (event) {
                    is PhoneAuthEvent.CodeSent -> startCountdown(event.canResendAtMs)

                    PhoneAuthEvent.AutoVerified -> {
                        setState { copy(isVerifying = false, error = null) }
                        emit(OtpVerifyEvent.Verified)
                    }

                    is PhoneAuthEvent.Failed -> setState { copy(isVerifying = false, error = event.error) }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: OtpVerifyAction) {
        when (action) {
            is OtpVerifyAction.Init -> setState { copy(phoneE164 = action.phoneE164) }

            is OtpVerifyAction.UpdateCode -> {
                val digits = action.code.filter { it.isDigit() }.take(OTP_LENGTH)
                setState { copy(code = digits, error = null) }
                if (digits.length == OTP_LENGTH && !state.value.isVerifying) verify(digits)
            }

            OtpVerifyAction.Submit -> {
                val code = state.value.code
                if (code.length == OTP_LENGTH) {
                    verify(code)
                } else {
                    setState { copy(error = AppError.Reason("OTP_INVALID")) }
                }
            }

            OtpVerifyAction.Resend -> {
                val current = state.value
                if (current.phoneE164.isNotBlank() && current.canResend) {
                    setState { copy(error = null, code = "") }
                    phoneAuth.start(current.phoneE164, forceResend = true)
                }
            }
        }
    }

    private fun startCountdown(canResendAtMs: Long) {
        countdownJob?.cancel()
        val nowMs = clock.now().toEpochMilliseconds()
        val seconds = ((canResendAtMs - nowMs + MS_PER_SECOND - 1) / MS_PER_SECOND).toInt().coerceAtLeast(0)
        setState { copy(resendSecondsLeft = seconds) }
        countdownJob = viewModelScope.launch {
            while (state.value.resendSecondsLeft > 0) {
                delay(MS_PER_SECOND)
                setState { copy(resendSecondsLeft = (resendSecondsLeft - 1).coerceAtLeast(0)) }
            }
        }
    }

    private fun verify(code: String) {
        setState { copy(isVerifying = true, error = null) }
        viewModelScope.launch {
            when (val result = phoneAuth.confirm(code)) {
                is AppResult.Ok -> {
                    setState { copy(isVerifying = false) }
                    emit(OtpVerifyEvent.Verified)
                }

                is AppResult.Err -> setState { copy(isVerifying = false, error = result.error) }
            }
        }
    }

    private companion object {
        const val OTP_LENGTH = 6
        const val MS_PER_SECOND = 1_000L
    }
}
