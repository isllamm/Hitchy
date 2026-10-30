package com.hitchy.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.platform.PhoneAuth
import com.hitchy.core.ui.MviViewModel
import com.hitchy.core.util.PhoneNumber
import kotlinx.coroutines.launch

data class PhoneEntryState(
    val phoneInput: String = "",
    val error: AppError? = null,
) {
    // The field shows a fixed "+20", so "1012345678" is the number the user is asked for.
    val normalizedPhone: String?
        get() = PhoneNumber.normalize(phoneInput) ?: PhoneNumber.normalize("$EGYPT_PREFIX$phoneInput")

    val isValid: Boolean
        get() = normalizedPhone != null
}

private const val EGYPT_PREFIX = "+20"

sealed interface PhoneEntryAction {
    data class UpdatePhone(val input: String) : PhoneEntryAction
    data object Submit : PhoneEntryAction
}

sealed interface PhoneEntryEvent {
    data class NavigateToOtp(val phoneE164: String) : PhoneEntryEvent
}

class PhoneEntryViewModel(
    private val phoneAuth: PhoneAuth,
) : MviViewModel<PhoneEntryState, PhoneEntryAction, PhoneEntryEvent>(PhoneEntryState()) {

    override fun onAction(action: PhoneEntryAction) {
        when (action) {
            is PhoneEntryAction.UpdatePhone -> setState { copy(phoneInput = action.input, error = null) }
            PhoneEntryAction.Submit -> submit()
        }
    }

    private fun submit() {
        val e164 = state.value.normalizedPhone
        if (e164 == null) {
            setState { copy(error = AppError.Reason(INVALID_INPUT)) }
            return
        }
        // Sending happens on the OTP screen's events; the code-sent / failed events reach it there.
        phoneAuth.start(e164)
        viewModelScope.launch { emit(PhoneEntryEvent.NavigateToOtp(e164)) }
    }

    private companion object {
        const val INVALID_INPUT = "INVALID_INPUT"
    }
}
