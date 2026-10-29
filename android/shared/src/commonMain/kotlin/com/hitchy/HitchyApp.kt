package com.hitchy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.hello_hitchy
import org.jetbrains.compose.resources.stringResource

@Composable
fun HitchyApp() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(Res.string.hello_hitchy))
        }
    }
}
