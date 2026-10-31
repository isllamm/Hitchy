package com.hitchy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.AppReset
import com.hitchy.core.platform.PhotoUrlResolver
import com.hitchy.core.platform.StreetRouteProvider
import com.hitchy.core.ui.components.LocalPhotoUrlResolver
import com.hitchy.core.ui.map.LocalStreetRouteProvider
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.navigation.HitchyNavHost
import org.koin.compose.koinInject

@Composable
fun HitchyApp() {
    val appReset: AppReset = koinInject()
    val generation by appReset.generation.collectAsStateWithLifecycle()
    HitchyTheme {
        CompositionLocalProvider(
            LocalPhotoUrlResolver provides koinInject<PhotoUrlResolver>(),
            LocalStreetRouteProvider provides koinInject<StreetRouteProvider>(),
        ) {
            // A new generation drops every screen and its state: the app opens again on the current role and language.
            key(generation) {
                HitchyNavHost()
            }
        }
    }
}
