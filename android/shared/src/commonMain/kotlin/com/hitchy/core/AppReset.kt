package com.hitchy.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A signal that restarts the screens from the root: the back stack and the screens' own state are dropped and the app
 * opens on its current state (the role and the language that are active now). Switching the role and changing the
 * language send it.
 */
class AppReset {
    private val _generation = MutableStateFlow(0)
    val generation: StateFlow<Int> = _generation.asStateFlow()

    fun reset() {
        _generation.update { it + 1 }
    }
}
