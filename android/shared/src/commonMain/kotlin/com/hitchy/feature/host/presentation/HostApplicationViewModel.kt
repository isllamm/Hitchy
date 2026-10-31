package com.hitchy.feature.host.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.model.HostStatus
import com.hitchy.core.model.VehicleType
import com.hitchy.core.ui.MviViewModel
import com.hitchy.core.util.toCairo
import com.hitchy.feature.host.domain.AddVehicleInput
import com.hitchy.feature.host.domain.HostDraft
import com.hitchy.feature.host.domain.HostRepository
import com.hitchy.feature.host.domain.KycDocKey
import com.hitchy.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Clock

/** This year in Cairo and the ones before it, newest first. */
private fun recentYears(clock: Clock): List<Int> {
    val now = toCairo(clock.now()).year
    return (now downTo now - HostApplicationState.YEAR_CHOICES + 1).toList()
}

/** The application's screens, in Figma's order. */
enum class HostStep { VEHICLE, BASIC, DOCS, DETAILS, REVIEW }

data class HostApplicationState(
    val step: HostStep = HostStep.VEHICLE,
    val vehicleType: VehicleType? = null,
    val fullName: String = "",
    val phone: String = "",
    val hasSelfie: Boolean = false,
    val docs: Set<KycDocKey> = emptySet(),
    val failedDocs: Set<KycDocKey> = emptySet(),
    val makeModel: String = "",
    val year: Int? = null,
    val plate: String = "",
    val color: String = "",
    val confirmed: Boolean = false,
    val uploadSheetFor: KycDocKey? = null,
    val showExit: Boolean = false,
    val submitting: Boolean = false,
    val error: AppError? = null,
    /** The Manufacturing year choices, newest first. */
    val years: List<Int> = emptyList(),
) {
    val seats: Int get() = if (vehicleType == VehicleType.SCOOTER) 1 else CAR_SEATS

    val canContinue: Boolean
        get() = when (step) {
            HostStep.VEHICLE -> vehicleType != null
            HostStep.BASIC -> fullName.isNotBlank()
            HostStep.DOCS -> docs.containsAll(KycDocKey.entries)
            HostStep.DETAILS -> makeModel.isNotBlank() && year != null && plate.isNotBlank() && color.isNotBlank()
            HostStep.REVIEW -> confirmed && !submitting
        }

    companion object {
        const val CAR_SEATS = 3
        const val YEAR_CHOICES = 25
    }
}

sealed interface HostApplicationAction {
    data class SelectVehicle(val type: VehicleType) : HostApplicationAction
    data class UpdateName(val value: String) : HostApplicationAction
    data class SelfieTaken(val jpeg: ByteArray?) : HostApplicationAction
    data class OpenUpload(val key: KycDocKey) : HostApplicationAction
    data object DismissUpload : HostApplicationAction
    data class DocTaken(val key: KycDocKey, val jpeg: ByteArray?) : HostApplicationAction
    data class UpdateMakeModel(val value: String) : HostApplicationAction
    data class SelectYear(val year: Int) : HostApplicationAction
    data class UpdatePlate(val value: String) : HostApplicationAction
    data class SelectColor(val value: String) : HostApplicationAction
    data object ToggleConfirm : HostApplicationAction
    data class Edit(val step: HostStep) : HostApplicationAction
    data object Continue : HostApplicationAction
    data object Back : HostApplicationAction
    data object KeepGoing : HostApplicationAction
    data object ConfirmExit : HostApplicationAction
    data object DismissError : HostApplicationAction
}

sealed interface HostApplicationEvent {
    data object Exit : HostApplicationEvent
    data object Submitted : HostApplicationEvent

    /** The application is already with the team: show where it stands instead (HostStatus). */
    data object AlreadySubmitted : HostApplicationEvent
}

/**
 * The owner application wizard (plan P5-A02, Figma DEMO "car owner" frames). Photos stay in memory in the
 * [HostDraft]; Submit adds the vehicle and submits the application (P5-B01 and P5-B02 callables).
 */
class HostApplicationViewModel(
    private val hostRepository: HostRepository,
    private val profileRepository: ProfileRepository,
    private val draft: HostDraft,
    clock: Clock,
) : MviViewModel<HostApplicationState, HostApplicationAction, HostApplicationEvent>(HostApplicationState()) {

    init {
        setState {
            copy(
                step = if (draft.vehicleType == null) HostStep.VEHICLE else HostStep.BASIC,
                vehicleType = draft.vehicleType,
                fullName = draft.fullName,
                hasSelfie = draft.selfie != null,
                docs = draft.docs.keys.toSet(),
                makeModel = draft.makeModel,
                year = draft.year,
                plate = draft.plate,
                color = draft.color,
                years = recentYears(clock),
            )
        }
        viewModelScope.launch {
            val me = profileRepository.observeMe().first() ?: return@launch
            if (me.host.status == HostStatus.SUBMITTED || me.host.status == HostStatus.APPROVED) {
                emit(HostApplicationEvent.AlreadySubmitted)
                return@launch
            }
            setState {
                copy(
                    phone = me.phone,
                    fullName = fullName.ifBlank { listOf(me.firstName, me.lastName).joinToString(" ").trim() },
                )
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override fun onAction(action: HostApplicationAction) {
        when (action) {
            is HostApplicationAction.SelectVehicle -> {
                draft.vehicleType = action.type
                setState { copy(vehicleType = action.type) }
            }

            is HostApplicationAction.UpdateName -> {
                draft.fullName = action.value
                setState { copy(fullName = action.value) }
            }

            is HostApplicationAction.SelfieTaken -> if (action.jpeg != null) {
                draft.selfie = action.jpeg
                setState { copy(hasSelfie = true) }
            }

            is HostApplicationAction.OpenUpload -> setState { copy(uploadSheetFor = action.key) }

            HostApplicationAction.DismissUpload -> setState { copy(uploadSheetFor = null) }

            is HostApplicationAction.DocTaken -> docTaken(action.key, action.jpeg)

            is HostApplicationAction.UpdateMakeModel -> {
                draft.makeModel = action.value
                setState { copy(makeModel = action.value) }
            }

            is HostApplicationAction.SelectYear -> {
                draft.year = action.year
                setState { copy(year = action.year) }
            }

            is HostApplicationAction.UpdatePlate -> {
                draft.plate = action.value
                setState { copy(plate = action.value) }
            }

            is HostApplicationAction.SelectColor -> {
                draft.color = action.value
                setState { copy(color = action.value) }
            }

            HostApplicationAction.ToggleConfirm -> setState { copy(confirmed = !confirmed) }

            is HostApplicationAction.Edit -> setState { copy(step = action.step) }

            HostApplicationAction.Continue -> next()

            HostApplicationAction.Back -> back()

            HostApplicationAction.KeepGoing -> setState { copy(showExit = false) }

            HostApplicationAction.ConfirmExit -> {
                setState { copy(showExit = false) }
                viewModelScope.launch { emit(HostApplicationEvent.Exit) }
            }

            HostApplicationAction.DismissError -> setState { copy(error = null) }
        }
    }

    private fun docTaken(key: KycDocKey, jpeg: ByteArray?) {
        setState { copy(uploadSheetFor = null) }
        if (jpeg == null) return
        draft.docs[key] = jpeg
        setState { copy(docs = docs + key, failedDocs = failedDocs - key) }
    }

    private fun next() {
        val state = state.value
        if (!state.canContinue) return
        when (state.step) {
            HostStep.VEHICLE -> setState { copy(step = HostStep.BASIC) }
            HostStep.BASIC -> setState { copy(step = HostStep.DOCS) }
            HostStep.DOCS -> setState { copy(step = HostStep.DETAILS) }
            HostStep.DETAILS -> setState { copy(step = HostStep.REVIEW) }
            HostStep.REVIEW -> submit()
        }
    }

    /** Back: the previous step; leaving the documents or later asks "Leave for now?" first (Figma). */
    private fun back() {
        when (state.value.step) {
            HostStep.VEHICLE -> viewModelScope.launch { emit(HostApplicationEvent.Exit) }
            HostStep.BASIC -> setState { copy(step = HostStep.VEHICLE) }
            HostStep.DOCS, HostStep.DETAILS -> setState { copy(showExit = true) }
            HostStep.REVIEW -> setState { copy(step = HostStep.DETAILS) }
        }
    }

    private fun submit() {
        val state = state.value
        val type = state.vehicleType ?: return
        val words = state.makeModel.trim().split(Regex("\\s+"), limit = 2)
        setState { copy(submitting = true, error = null) }
        viewModelScope.launch {
            val added = hostRepository.addVehicle(
                AddVehicleInput(
                    type = type,
                    make = words.first(),
                    model = words.getOrElse(1) { words.first() },
                    year = state.year ?: return@launch,
                    color = state.color,
                    plate = state.plate.trim(),
                ),
            )
            val result = when (added) {
                is AppResult.Ok -> hostRepository.submit(added.value)
                is AppResult.Err -> added
            }
            when (result) {
                is AppResult.Ok -> {
                    setState { copy(submitting = false) }
                    draft.clear()
                    emit(HostApplicationEvent.Submitted)
                }

                is AppResult.Err -> setState { copy(submitting = false, error = result.error) }
            }
        }
    }
}
