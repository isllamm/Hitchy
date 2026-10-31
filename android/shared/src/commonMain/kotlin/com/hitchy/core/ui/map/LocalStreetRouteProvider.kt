package com.hitchy.core.ui.map

import androidx.compose.runtime.staticCompositionLocalOf
import com.hitchy.core.platform.StreetRouteProvider

/** Set once at the app root. Previews and the gallery leave it null, so their route is a straight line. */
val LocalStreetRouteProvider = staticCompositionLocalOf<StreetRouteProvider?> { null }
