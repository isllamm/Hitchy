package com.hitchy.feature.debug.presentation

import androidx.lifecycle.viewModelScope
import com.hitchy.core.model.AppConfig
import com.hitchy.core.model.PricingConfig
import com.hitchy.core.ui.MviViewModel
import com.hitchy.feature.debug.domain.DebugInfo
import com.hitchy.feature.debug.domain.DebugRepository
import kotlinx.coroutines.launch

data class DebugState(
    val uid: String? = null,
    val env: String = "",
    val functionsRegion: String = "",
    val useEmulator: Boolean = false,
    val versionName: String = "",
    val fcmToken: String? = null,
    val appCheckStatus: String = "",
    val remoteConfigValues: Map<String, String> = emptyMap(),
    val pricingConfig: PricingConfig? = null,
    val isPricingLoading: Boolean = true,
    val isPlatformLoading: Boolean = true,
)

sealed interface DebugAction {
    data object Refresh : DebugAction
    data object SignInAnonymously : DebugAction
    data object SignOut : DebugAction
    data object TestCrash : DebugAction
}

sealed interface DebugEvent {
    data class Message(val text: String) : DebugEvent
}

class DebugViewModel(
    private val debugRepository: DebugRepository,
    private val debugInfo: DebugInfo,
    private val appConfig: AppConfig,
) : MviViewModel<DebugState, DebugAction, DebugEvent>(
    DebugState(
        env = appConfig.env.name,
        functionsRegion = appConfig.functionsRegion,
        useEmulator = appConfig.useEmulator,
        versionName = appConfig.versionName,
    )
) {

    init {
        observeUid()
        observePricing()
        loadPlatformInfo()
    }

    override fun onAction(action: DebugAction) {
        when (action) {
            DebugAction.Refresh -> loadPlatformInfo()
            DebugAction.SignInAnonymously -> signInAnonymously()
            DebugAction.SignOut -> signOut()
            DebugAction.TestCrash -> testCrash()
        }
    }

    private fun observeUid() {
        viewModelScope.launch {
            debugRepository.observeCurrentUid().collect { uid ->
                setState { copy(uid = uid) }
            }
        }
    }

    private fun observePricing() {
        viewModelScope.launch {
            debugRepository.observePricingConfig().collect { config ->
                setState { copy(pricingConfig = config, isPricingLoading = false) }
            }
        }
    }

    private fun loadPlatformInfo() {
        viewModelScope.launch {
            setState { copy(isPlatformLoading = true) }
            val fcm = debugInfo.getFcmToken()
            val appCheck = debugInfo.getAppCheckStatus()
            val rc = debugInfo.getRemoteConfigValues()
            setState {
                copy(
                    fcmToken = fcm,
                    appCheckStatus = appCheck,
                    remoteConfigValues = rc,
                    isPlatformLoading = false,
                )
            }
        }
    }

    private fun signInAnonymously() {
        viewModelScope.launch {
            try {
                debugRepository.signInAnonymously()
            } catch (e: Exception) {
                emit(DebugEvent.Message(e.message ?: "Anonymous sign-in failed"))
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            try {
                debugRepository.signOut()
            } catch (e: Exception) {
                emit(DebugEvent.Message(e.message ?: "Sign-out failed"))
            }
        }
    }

    private fun testCrash() {
        debugInfo.triggerTestCrash()
    }
}
