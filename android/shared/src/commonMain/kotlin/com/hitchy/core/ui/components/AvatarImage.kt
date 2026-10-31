package com.hitchy.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import coil3.compose.AsyncImage
import com.hitchy.core.platform.PhotoUrlResolver

/** Set once at the app root. Previews and the gallery leave it null, so they show initials. */
val LocalPhotoUrlResolver = staticCompositionLocalOf<PhotoUrlResolver?> { null }

/**
 * The inside of an avatar circle: the person's picture when [photoPath] resolves, their initials otherwise.
 * The parent draws the circle (clip and background); this fills it.
 */
@Composable
fun AvatarImage(
    photoPath: String?,
    initials: String,
    textStyle: TextStyle,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    val resolver = LocalPhotoUrlResolver.current
    val url by produceState<String?>(initialValue = null, photoPath, resolver) {
        value = if (photoPath != null && resolver != null) resolver.urlFor(photoPath) else null
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val shown = url
        if (shown != null) {
            AsyncImage(
                model = shown,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(text = initials, style = textStyle, color = textColor)
        }
    }
}
