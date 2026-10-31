package com.hitchy.feature.profile.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.model.Gender
import com.hitchy.core.model.Role
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.auth.domain.AuthRepository
import com.hitchy.feature.auth.domain.SignUpNameHolder
import com.hitchy.feature.profile.domain.Company
import com.hitchy.feature.profile.domain.CompleteProfileInput
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.profile.domain.User
import com.hitchy.feature.profile.domain.WalletRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val MIN_NAME_LENGTH = 2

data class ProfileSetupState(
    val firstName: String = "",
    val lastName: String = "",
    val gender: Gender? = null,
    val companies: List<Company> = emptyList(),
    val selectedCompanyId: String? = null,
    val isOtherCompany: Boolean = false,
    val companyOther: String = "",
    val isSaving: Boolean = false,
    val error: AppError? = null,
) {
    val canSubmit: Boolean
        get() = firstName.trim().length >= MIN_NAME_LENGTH &&
            lastName.trim().length >= MIN_NAME_LENGTH &&
            gender != null &&
            (!isOtherCompany || companyOther.trim().length >= MIN_NAME_LENGTH)
}

sealed interface ProfileSetupAction {
    data class UpdateFirstName(val value: String) : ProfileSetupAction
    data class UpdateLastName(val value: String) : ProfileSetupAction
    data class SelectGender(val gender: Gender) : ProfileSetupAction
    data class SelectCompany(val companyId: String?) : ProfileSetupAction
    data object SelectOtherCompany : ProfileSetupAction
    data class UpdateCompanyOther(val value: String) : ProfileSetupAction
    data object Submit : ProfileSetupAction
    data object DismissError : ProfileSetupAction
}

sealed interface ProfileSetupEvent {
    data object Completed : ProfileSetupEvent
}

class ProfileSetupViewModel(
    private val profileRepository: ProfileRepository,
    private val languageSwitcher: LanguageSwitcher,
    signUpNameHolder: SignUpNameHolder? = null,
) : MviViewModel<ProfileSetupState, ProfileSetupAction, ProfileSetupEvent>(ProfileSetupState()) {

    init {
        // The Sign up screen asks for the full name first; start from it.
        signUpNameHolder?.split()?.let { (first, last) ->
            if (first.isNotBlank()) setState { copy(firstName = first, lastName = last) }
        }
        profileRepository.observeCompanies()
            .onEach { list -> setState { copy(companies = list.filter { it.active }) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: ProfileSetupAction) {
        when (action) {
            is ProfileSetupAction.UpdateFirstName -> setState { copy(firstName = action.value, error = null) }

            is ProfileSetupAction.UpdateLastName -> setState { copy(lastName = action.value, error = null) }

            is ProfileSetupAction.SelectGender -> setState { copy(gender = action.gender, error = null) }

            is ProfileSetupAction.SelectCompany -> setState {
                copy(selectedCompanyId = action.companyId, isOtherCompany = false, error = null)
            }

            ProfileSetupAction.SelectOtherCompany -> setState {
                copy(selectedCompanyId = null, isOtherCompany = true, error = null)
            }

            is ProfileSetupAction.UpdateCompanyOther -> setState { copy(companyOther = action.value, error = null) }

            ProfileSetupAction.Submit -> submit()

            ProfileSetupAction.DismissError -> setState { copy(error = null) }
        }
    }

    private fun submit() {
        val current = state.value
        val gender = current.gender
        if (!current.canSubmit || gender == null || current.isSaving) {
            setState { copy(error = AppError.Reason("INVALID_INPUT")) }
            return
        }
        setState { copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val input = CompleteProfileInput(
                firstName = current.firstName.trim(),
                lastName = current.lastName.trim(),
                gender = gender,
                companyId = current.selectedCompanyId,
                companyOther = if (current.isOtherCompany) current.companyOther.trim() else null,
                workHours = null,
                language = languageSwitcher.getCurrentLanguage(),
                referralCode = null,
            )
            when (val result = profileRepository.completeProfile(input)) {
                is AppResult.Ok -> {
                    setState { copy(isSaving = false) }
                    emit(ProfileSetupEvent.Completed)
                }

                is AppResult.Err -> setState { copy(isSaving = false, error = result.error) }
            }
        }
    }
}

data class ProfileTabState(
    val profile: User? = null,
    val walletBalanceEgp: Int = 0,
    val isSwitchingRole: Boolean = false,
    val error: AppError? = null,
)

sealed interface ProfileTabAction {
    data class SwitchRole(val role: Role) : ProfileTabAction
    data object SignOut : ProfileTabAction
    data object DismissError : ProfileTabAction
}

sealed interface ProfileTabEvent {
    data object NavigateToHostApplication : ProfileTabEvent

    /** The role changed: the app restarts from the root on the new role. */
    data object RoleSwitched : ProfileTabEvent
}

class ProfileTabViewModel(
    private val profileRepository: ProfileRepository,
    walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
) : MviViewModel<ProfileTabState, ProfileTabAction, ProfileTabEvent>(ProfileTabState()) {

    init {
        profileRepository.observeMe()
            .onEach { me -> setState { copy(profile = me) } }
            .launchIn(viewModelScope)
        walletRepository.observeBalance()
            .onEach { balance -> setState { copy(walletBalanceEgp = balance) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: ProfileTabAction) {
        when (action) {
            is ProfileTabAction.SwitchRole -> switchRole(action.role)
            ProfileTabAction.SignOut -> viewModelScope.launch { authRepository.signOut() }
            ProfileTabAction.DismissError -> setState { copy(error = null) }
        }
    }

    private fun switchRole(role: Role) {
        if (state.value.isSwitchingRole || state.value.profile?.activeRole == role) return
        setState { copy(isSwitchingRole = true, error = null) }
        viewModelScope.launch {
            when (val result = profileRepository.switchRole(role)) {
                is AppResult.Ok -> {
                    setState { copy(isSwitchingRole = false) }
                    emit(ProfileTabEvent.RoleSwitched)
                }

                is AppResult.Err -> {
                    val notApproved = (result.error as? AppError.Reason)?.code == HOST_NOT_APPROVED
                    if (notApproved) {
                        setState { copy(isSwitchingRole = false) }
                        emit(ProfileTabEvent.NavigateToHostApplication)
                    } else {
                        setState { copy(isSwitchingRole = false, error = result.error) }
                    }
                }
            }
        }
    }

    private companion object {
        const val HOST_NOT_APPROVED = "HOST_NOT_APPROVED"
    }
}
