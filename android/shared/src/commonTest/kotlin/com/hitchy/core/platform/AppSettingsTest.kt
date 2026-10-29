package com.hitchy.core.platform

import com.hitchy.core.model.Role
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppSettingsTest {

    @Test
    fun defaultSettingsHasNullRoleAndFixedInstallId() = runTest {
        val settings = InMemoryAppSettings()
        assertNull(settings.lastRole.first())
        assertEquals("test-install-id-1234", settings.installId.first())
    }

    @Test
    fun customInitialRoleIsEmitted() = runTest {
        val settings = InMemoryAppSettings(initialRole = Role.RIDER)
        assertEquals(Role.RIDER, settings.lastRole.first())
    }

    @Test
    fun setLastRoleUpdatesFlow() = runTest {
        val settings = InMemoryAppSettings()
        assertNull(settings.lastRole.first())

        settings.setLastRole(Role.HOST)
        assertEquals(Role.HOST, settings.lastRole.first())

        settings.setLastRole(Role.RIDER)
        assertEquals(Role.RIDER, settings.lastRole.first())
    }
}
