package com.hitchy.core.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hitchy.core.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import java.util.UUID

class DataStoreAppSettings(
    private val dataStore: DataStore<Preferences>
) : AppSettings {

    private object PreferencesKeys {
        val LAST_ROLE = stringPreferencesKey("last_role")
        val INSTALL_ID = stringPreferencesKey("install_id")
    }

    override val lastRole: Flow<Role?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_ROLE]?.let { roleStr ->
            try {
                Role.valueOf(roleStr.uppercase())
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    override suspend fun setLastRole(role: Role) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_ROLE] = role.name.lowercase()
        }
    }

    override val installId: Flow<String> = flow {
        val existing = dataStore.data.first()[PreferencesKeys.INSTALL_ID]
        if (existing == null) {
            val newId = UUID.randomUUID().toString()
            dataStore.edit { preferences ->
                if (preferences[PreferencesKeys.INSTALL_ID] == null) {
                    preferences[PreferencesKeys.INSTALL_ID] = newId
                }
            }
        }
        emitAll(dataStore.data.mapNotNull { it[PreferencesKeys.INSTALL_ID] })
    }
}
