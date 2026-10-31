package com.hitchy.core.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.figmaTop
import kotlinx.coroutines.delay

private const val AUTO_DISMISS_MS = 4_000L

/**
 * A [HitchyBanner] that floats over the top of the screen instead of pushing the content down. Put it last in
 * the screen's root Box. With [onDismiss] it also hides by itself after four seconds.
 */
@Composable
fun BoxScope.FloatingBanner(
    text: String,
    variant: BannerVariant = BannerVariant.Error,
    onDismiss: (() -> Unit)? = null,
) {
    if (onDismiss != null) {
        LaunchedEffect(text) {
            delay(AUTO_DISMISS_MS)
            onDismiss()
        }
    }
    HitchyBanner(
        text = text,
        variant = variant,
        onDismiss = onDismiss,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(start = 16.dp, end = 16.dp, top = figmaTop(12.dp))
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(12.dp)),
    )
}
