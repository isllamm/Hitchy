package com.hitchy.core.ui

import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val ExpandedGap = 8.dp
private const val FLING_VELOCITY = 1_200f

/**
 * A sheet over a map that starts at [collapsedTop] and can be dragged, or scrolled, up to cover the map. Scrolling
 * the content up first lifts the sheet to the top; pulling down at the top of the content lowers it again. On
 * release it settles open or closed. [content] gets the sheet's full remaining height.
 */
@Composable
fun ExpandableMapSheet(
    collapsedTop: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val expandedTopDp = figmaTop(ExpandedGap)
    Box(modifier = modifier.fillMaxSize()) {
        val collapsedPx = with(density) { collapsedTop.toPx() }
        val expandedPx = with(density) { expandedTopDp.toPx() }.coerceAtMost(collapsedPx)
        var offset by remember(collapsedPx) { mutableFloatStateOf(collapsedPx) }

        fun moveBy(delta: Float): Float {
            val target = (offset + delta).coerceIn(expandedPx, collapsedPx)
            val used = target - offset
            offset = target
            return used
        }

        suspend fun settle(velocity: Float) {
            val middle = (expandedPx + collapsedPx) / 2
            val open = when {
                velocity < -FLING_VELOCITY -> true
                velocity > FLING_VELOCITY -> false
                else -> offset < middle
            }
            animate(offset, if (open) expandedPx else collapsedPx) { value, _ -> offset = value }
        }

        val connection = remember(collapsedPx, expandedPx) {
            object : NestedScrollConnection {
                // Content moving up: lift the sheet first.
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val lift = available.y < 0 && offset > expandedPx
                    return if (lift) Offset(0f, moveBy(available.y)) else Offset.Zero
                }

                // Content already at its top and pulled down: lower the sheet.
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    val lower = available.y > 0 && offset < collapsedPx
                    return if (lower) Offset(0f, moveBy(available.y)) else Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    val between = offset > expandedPx && offset < collapsedPx
                    if (!between) return Velocity.Zero
                    settle(available.y)
                    return available
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    if (offset > expandedPx && offset < collapsedPx) {
                        settle(available.y)
                    }
                    return Velocity.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val top = offset.roundToInt()
                    val height = (constraints.maxHeight - top).coerceAtLeast(0)
                    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, top) }
                }
                .nestedScroll(connection)
                .draggable(
                    state = rememberDraggableState { delta -> moveBy(delta) },
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity -> settle(velocity) },
                ),
        ) {
            content()
        }
    }
}
