package com.hitchy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.hitchy.core.platform.PhotoUrlResolver
import com.hitchy.core.ui.components.LocalPhotoUrlResolver
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.navigation.HitchyNavHost
import org.koin.compose.koinInject

@Composable
fun HitchyApp() {
    HitchyTheme {
        CompositionLocalProvider(LocalPhotoUrlResolver provides koinInject<PhotoUrlResolver>()) {
            HitchyNavHost()
        }
    }
}
