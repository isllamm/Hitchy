package com.hitchy

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.hitchy.navigation.HitchyNavHost

@Composable
fun HitchyApp() {
    MaterialTheme {
        HitchyNavHost()
    }
}
