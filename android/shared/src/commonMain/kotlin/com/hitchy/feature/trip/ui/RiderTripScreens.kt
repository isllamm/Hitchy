@file:Suppress("MagicNumber")

package com.hitchy.feature.trip.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.FloatingBanner
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.message
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.trip.presentation.RiderTripAction
import com.hitchy.feature.trip.presentation.RiderTripState
import com.hitchy.feature.trip.presentation.RiderTripViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// Figma 390:1532: the map fills the screen and the back button sits 50dp down; the panel is r=24.
private val SheetTopOffset = 280.dp
private val BackTop = 50.dp

/**
 * Rider trip screen built to the design: a full-width map with the back button on top, and a rounded sheet
 * with the status, route, owner's car, seat and the cancel link. The map shows a straight line through the
 * ride's start point and stops (BUILD_PLAN 5.10); the ride has no drop-off coordinates yet. Chat, call,
 * Share trip, Report and Emergency are not built, so they are left out.
 */
@Composable
fun RiderTripScreen(
    rideId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    viewModel: RiderTripViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val language = languageSwitcher.getCurrentLanguage()

    LaunchedEffect(rideId) {
        viewModel.onAction(RiderTripAction.Init(rideId))
    }

    Box(modifier = modifier.fillMaxSize().background(colors.mapLand)) {
        HitchyMap(
            pins = tripMapPins(state),
            route = tripMapRoute(state),
            modifier = Modifier.fillMaxSize(),
        )
        BackButton(
            onClick = onBack,
            style = BackButtonStyle.OutlinedCircle,
            modifier = Modifier.padding(start = 16.dp, top = figmaTop(BackTop)).size(44.dp),
        )
        TripSheet(
            state = state,
            language = language,
            onAction = viewModel::onAction,
            modifier = Modifier.fillMaxSize().padding(top = SheetTopOffset),
        )
        state.error?.let { error ->
            FloatingBanner(
                text = stringResource(error.message()),
                onDismiss = { viewModel.onAction(RiderTripAction.DismissError) },
            )
        }
    }

    if (state.showCancelSheet && !state.isCancelled) {
        CancelBookingSheet(state = state, onAction = viewModel::onAction)
    }
    if (state.isCancelled) {
        BookingCancelledSheet(onFindAnother = onBack, onDismiss = onBack)
    }
}

private fun tripMapPins(state: RiderTripState): List<MapPin> = listOfNotNull(
    state.ride?.let { MapPin(id = "from", point = it.fromPoint) },
    state.booking?.let { MapPin(id = "pickup", point = it.pickupPoint, selected = true) },
)

private fun tripMapRoute(state: RiderTripState): List<GeoPoint> {
    val ride = state.ride ?: return emptyList()
    val points = listOf(ride.fromPoint) + ride.stops.map { it.point }
    return if (points.size >= 2) points else emptyList()
}
