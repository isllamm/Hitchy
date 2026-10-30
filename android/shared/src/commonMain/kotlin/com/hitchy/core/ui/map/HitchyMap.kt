package com.hitchy.core.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hitchy.core.model.GeoPoint

/** A fixed point on the map. Only points appear, never free pins (BUILD_PLAN 5.10). */
data class MapPin(
    val id: String,
    val point: GeoPoint,
    val selected: Boolean = false,
)

/**
 * The Hitchy map (BUILD_PLAN 5.10): fixed point pins and an optional straight route line.
 * The camera fits every pin and every route point by itself. Taps on empty map do nothing.
 * `contentDescription` defaults to the string `map_content_description`.
 */
@Composable
expect fun HitchyMap(
    pins: List<MapPin>,
    modifier: Modifier = Modifier,
    route: List<GeoPoint> = emptyList(),
    onPinClick: (String) -> Unit = {},
    contentDescription: String? = null,
)
