package com.hitchy.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import kotlin.math.roundToInt

// Cairo's font box is about 1.87x its size; the designs' text boxes are about 1.2x. Each side loses the half.
private const val CAIRO_EXTRA_PER_SIDE = 0.33f

/** The same style with the space above the first line and below the last one trimmed, where the font allows it. */
fun TextStyle.tight(): TextStyle = copy(lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both))

/**
 * Cuts Cairo's extra space above and below a one-line text of [fontSize], so a name and the line under it sit
 * close together as in Figma. The glyphs are not clipped; only the box the text takes in the layout shrinks.
 */
fun Modifier.tightLine(fontSize: TextUnit): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val trim = (fontSize.toPx() * CAIRO_EXTRA_PER_SIDE).roundToInt().coerceAtMost(placeable.height / 4)
    layout(placeable.width, placeable.height - 2 * trim) { placeable.place(0, -trim) }
}
