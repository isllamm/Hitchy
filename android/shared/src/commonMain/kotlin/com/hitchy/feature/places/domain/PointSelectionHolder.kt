package com.hitchy.feature.places.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PointSelectionHolder {
    private val _selections = MutableStateFlow<Map<String, Point>>(emptyMap())
    val selections: StateFlow<Map<String, Point>> = _selections.asStateFlow()

    fun select(field: String, point: Point) {
        _selections.update { current -> current + (field to point) }
    }

    fun clear(field: String) {
        _selections.update { current -> current - field }
    }

    fun get(field: String): Point? = _selections.value[field]

    fun swap(fieldA: String, fieldB: String) {
        _selections.update { current ->
            val a = current[fieldA]
            val b = current[fieldB]
            val updated = current.toMutableMap()
            if (b != null) updated[fieldA] = b else updated.remove(fieldA)
            if (a != null) updated[fieldB] = a else updated.remove(fieldB)
            updated
        }
    }
}
