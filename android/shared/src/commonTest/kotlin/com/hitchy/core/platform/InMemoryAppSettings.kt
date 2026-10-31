package com.hitchy.core.platform

import com.hitchy.core.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAppSettings(
    initialRole: Role? = null,
    val initialInstallId: String = "test-install-id-1234",
) : AppSettings {

    private val _lastRole = MutableStateFlow(initialRole)
    override val lastRole: Flow<Role?> = _lastRole.asStateFlow()

    private val _installId = MutableStateFlow(initialInstallId)
    override val installId: Flow<String> = _installId.asStateFlow()

    override suspend fun setLastRole(role: Role) {
        _lastRole.value = role
    }

    private val _onboardingSeen = MutableStateFlow(false)
    override val onboardingSeen: Flow<Boolean> = _onboardingSeen.asStateFlow()

    override suspend fun setOnboardingSeen() {
        _onboardingSeen.value = true
    }
}
