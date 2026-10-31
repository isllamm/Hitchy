package com.hitchy.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.hitchy.core.data.AppError
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.message
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

private const val FLOAT_DISMISS_MS = 4_000L

/**
 * An error message that floats over the top of the screen. Call it from anywhere in a screen: it sits in a window
 * of its own, so it takes no space and never moves the layout. With [onDismiss] it also hides after four seconds.
 */
@Composable
fun FloatingError(text: String, onDismiss: (() -> Unit)? = null) {
    val topPx = with(LocalDensity.current) { figmaTop(12.dp).roundToPx() }
    val position = remember(topPx) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset = IntOffset((windowSize.width - popupContentSize.width) / 2, topPx)
        }
    }
    if (onDismiss != null) {
        LaunchedEffect(text) {
            delay(FLOAT_DISMISS_MS)
            onDismiss()
        }
    }
    Popup(
        popupPositionProvider = position,
        properties = PopupProperties(focusable = false, dismissOnClickOutside = false),
    ) {
        HitchyBanner(
            text = text,
            variant = BannerVariant.Error,
            onDismiss = onDismiss,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(12.dp)),
        )
    }
}

/** [FloatingError] for an [AppError], with its message from the error catalog. */
@Composable
fun FloatingError(error: AppError, onDismiss: (() -> Unit)? = null) {
    FloatingError(text = stringResource(error.message()), onDismiss = onDismiss)
}
