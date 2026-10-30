package com.hitchy.core.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.ui.theme.LocalHitchyColors
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.map_content_description
import org.jetbrains.compose.resources.stringResource

private val FitPadding = 48.dp
private val RouteWidth = 4.dp
private const val CAMERA_ANIMATION_MS = 400
private const val SELECTED_PIN_Z_INDEX = 1f

// Plan 5.10: the free map only. No map ID, no traffic, no buildings, no my-location, no extra buttons.
private val MapSettings = MapProperties(
    isBuildingEnabled = false,
    isIndoorEnabled = false,
    isMyLocationEnabled = false,
    isTrafficEnabled = false,
    mapType = MapType.NORMAL,
)
private val MapControls = MapUiSettings(
    compassEnabled = false,
    indoorLevelPickerEnabled = false,
    mapToolbarEnabled = false,
    myLocationButtonEnabled = false,
    rotationGesturesEnabled = false,
    tiltGesturesEnabled = false,
    zoomControlsEnabled = false,
)

@Composable
actual fun HitchyMap(
    pins: List<MapPin>,
    modifier: Modifier,
    route: List<GeoPoint>,
    onPinClick: (String) -> Unit,
    contentDescription: String?,
) {
    val colors = LocalHitchyColors.current
    val density = LocalDensity.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MapDefaults.center.toLatLng(), MapDefaults.ZOOM)
    }
    var mapLoaded by remember { mutableStateOf(false) }
    var fitted by remember { mutableStateOf(false) }
    val focus = remember(pins, route) { mapFocusFor(pins.map { it.point } + route) }
    val fitPaddingPx = with(density) { FitPadding.roundToPx() }

    // The camera can only move after the map has a size, so wait for onMapLoaded.
    LaunchedEffect(focus, mapLoaded) {
        if (!mapLoaded) return@LaunchedEffect
        val update = focus.toCameraUpdate(fitPaddingPx)
        if (fitted) {
            cameraPositionState.animate(update, CAMERA_ANIMATION_MS)
        } else {
            cameraPositionState.move(update)
        }
        fitted = true
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        contentDescription = contentDescription ?: stringResource(Res.string.map_content_description),
        properties = MapSettings,
        uiSettings = MapControls,
        mapColorScheme = ComposeMapColorScheme.LIGHT,
        onMapLoaded = { mapLoaded = true },
    ) {
        if (route.size >= 2) {
            Polyline(
                points = route.map { it.toLatLng() },
                color = colors.purple,
                width = with(density) { RouteWidth.toPx() },
                jointType = JointType.ROUND,
                geodesic = false,
            )
        }
        pins.forEach { pin ->
            key(pin.id) {
                MarkerComposable(
                    pin.id,
                    pin.selected,
                    state = rememberUpdatedMarkerState(position = pin.point.toLatLng()),
                    anchor = Offset(x = 0.5f, y = 0.5f),
                    zIndex = if (pin.selected) SELECTED_PIN_Z_INDEX else 0f,
                    onClick = {
                        onPinClick(pin.id)
                        true
                    },
                ) {
                    MapPinMarker(selected = pin.selected)
                }
            }
        }
    }
}

private fun GeoPoint.toLatLng(): LatLng = LatLng(lat, lng)

private fun MapFocus.toCameraUpdate(paddingPx: Int): CameraUpdate = when (this) {
    MapFocus.Default ->
        CameraUpdateFactory.newLatLngZoom(MapDefaults.center.toLatLng(), MapDefaults.ZOOM)

    is MapFocus.Single ->
        CameraUpdateFactory.newLatLngZoom(point.toLatLng(), MapDefaults.SINGLE_POINT_ZOOM)

    is MapFocus.Fit ->
        CameraUpdateFactory.newLatLngBounds(
            LatLngBounds(southWest.toLatLng(), northEast.toLatLng()),
            paddingPx,
        )
}
