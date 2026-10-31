package com.hitchy.core.ui.map

import android.content.Context
import com.google.android.gms.maps.MapsInitializer

/**
 * Loads the Maps renderer at app start, so the first map on Home draws sooner instead of loading the
 * renderer when the screen opens. Uses the latest renderer (faster tiles, less memory).
 */
fun warmUpMaps(context: Context) {
    MapsInitializer.initialize(context.applicationContext, MapsInitializer.Renderer.LATEST) { }
}
