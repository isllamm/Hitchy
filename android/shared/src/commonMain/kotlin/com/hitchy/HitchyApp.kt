package com.hitchy

import androidx.compose.runtime.Composable
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.navigation.HitchyNavHost

@Composable
fun HitchyApp() {
    HitchyTheme {
        HitchyNavHost()
    }
}
