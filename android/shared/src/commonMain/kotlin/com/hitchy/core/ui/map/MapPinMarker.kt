package com.hitchy.core.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.LocalHitchyColors
import org.jetbrains.compose.resources.painterResource

private val NormalSize = 36.dp
private val SelectedSize = 44.dp
private val BorderWidth = 2.dp
private val IconSize = 20.dp

/**
 * A map pin built from Phase 3 tokens only. No pin is designed yet (`[FIGMA]`).
 * Normal: white circle, purple border, purple icon. Selected: purple circle, white icon.
 */
@Composable
internal fun MapPinMarker(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHitchyColors.current
    Box(
        modifier = modifier
            .size(if (selected) SelectedSize else NormalSize)
            .background(color = if (selected) colors.purple else colors.white, shape = CircleShape)
            .border(width = BorderWidth, color = colors.purple, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(HitchyIcons.Pin),
            contentDescription = null,
            tint = if (selected) colors.white else colors.purple,
            modifier = Modifier.size(IconSize),
        )
    }
}
