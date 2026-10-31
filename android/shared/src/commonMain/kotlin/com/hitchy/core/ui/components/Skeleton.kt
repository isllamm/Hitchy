package com.hitchy.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.skeleton_loading
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Loading skeletons: light grey blocks in the shape of the screen's own cards and rows, with a soft white light
 * that sweeps across every [SWEEP_MS] ms (the iOS shimmer). Every block inside one [Skeleton] shares the same sweep,
 * measured across the whole [Skeleton], so the light moves over the screen as one band, from the reading start
 * (left in English, right in Arabic). Colors are the Phase 3 tokens: disabledSurface for the block, white for the
 * light; on the purple header, white at low alpha ([SkeletonTone.OnBrand]).
 */
@Composable
fun Skeleton(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    tone: SkeletonTone = SkeletonTone.OnLight,
    content: @Composable ColumnScope.() -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SWEEP_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "skeleton-sweep",
    )
    val sweep = remember(progress) { SkeletonSweep(progress) }
    val description = stringResource(Res.string.skeleton_loading)
    CompositionLocalProvider(LocalSkeletonSweep provides sweep, LocalSkeletonTone provides tone) {
        Column(
            modifier = modifier
                .semantics { contentDescription = description }
                .onGloballyPositioned { coords ->
                    sweep.left = coords.positionInRoot().x
                    sweep.width = coords.size.width.toFloat()
                },
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

/** Which surface the skeleton sits on: the white screen, or the brand gradient header. */
enum class SkeletonTone { OnLight, OnBrand }

/** One grey block of a skeleton, in the shape of the text, picture or card it stands for. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    val colors = HitchyTheme.colors
    val tone = LocalSkeletonTone.current
    val base = if (tone == SkeletonTone.OnBrand) colors.white.copy(alpha = ON_BRAND_BASE) else colors.disabledSurface
    val light = if (tone == SkeletonTone.OnBrand) colors.white.copy(alpha = ON_BRAND_LIGHT) else colors.white
    val sweep = LocalSkeletonSweep.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val bandPx = with(LocalDensity.current) { BAND.toPx() }
    var blockLeft by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = modifier
            .onGloballyPositioned { blockLeft = it.positionInRoot().x }
            .clip(shape)
            .drawBehind {
                drawRect(base)
                if (sweep == null) return@drawBehind
                // Read in the draw phase only: the sweep redraws the blocks without recomposing the screen.
                val travel = sweep.width + 2 * bandPx
                val p = if (rtl) 1f - sweep.progress.value else sweep.progress.value
                val centre = sweep.left - bandPx + p * travel - blockLeft
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, light, Color.Transparent),
                        start = Offset(centre - bandPx / 2, 0f),
                        end = Offset(centre + bandPx / 2, size.height),
                    ),
                )
            },
    )
}

/** A line of text: [height] is the text's cap height, not its line box, so lines sit as close as real text. */
@Composable
fun SkeletonLine(width: Dp, modifier: Modifier = Modifier, height: Dp = 12.dp) {
    SkeletonBlock(modifier = modifier.size(width = width, height = height), shape = RoundedCornerShape(height / 2))
}

/** A round photo or icon. */
@Composable
fun SkeletonCircle(size: Dp, modifier: Modifier = Modifier) {
    SkeletonBlock(modifier = modifier.size(size), shape = CircleShape)
}

/**
 * The skeleton of a white card in the app's card style (16dp corners, the hairline border), holding [content].
 * Used for ride cards, the summary cards and the detail sections.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HitchyTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.white)
            .border(1.dp, colors.hairline, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

/** A ride card: the status pill and date, the two route points, then the footer with the car and the price. */
@Composable
fun SkeletonRideCard(modifier: Modifier = Modifier) {
    SkeletonCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SkeletonBlock(Modifier.size(width = 72.dp, height = 24.dp), RoundedCornerShape(12.dp))
            Box(Modifier.weight(1f))
            SkeletonLine(width = 96.dp)
        }
        SkeletonRoutePoint(width = 180.dp)
        SkeletonRoutePoint(width = 140.dp)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SkeletonCircle(size = 32.dp)
            SkeletonLine(width = 88.dp, modifier = Modifier.padding(start = 8.dp))
            Box(Modifier.weight(1f))
            SkeletonLine(width = 64.dp, height = 16.dp)
        }
    }
}

/** A list row: the round icon or photo, a title over a subtitle. Notifications, places and passengers. */
@Composable
fun SkeletonListRow(modifier: Modifier = Modifier, leading: Dp = 40.dp, titleWidth: Dp = 160.dp) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonCircle(size = leading)
        Column(modifier = Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonLine(width = titleWidth, height = 14.dp)
            SkeletonLine(width = titleWidth * SUBTITLE_RATIO)
        }
    }
}

/**
 * A details page before its ride arrives: the map, the person card, the route card, the times banner and the
 * price card. Ride details for riders and owners, and the trip screens.
 */
@Composable
fun SkeletonDetails(modifier: Modifier = Modifier, showMap: Boolean = true) {
    Skeleton(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (showMap) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(170.dp), shape = RoundedCornerShape(16.dp))
        }
        SkeletonCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonCircle(size = 48.dp)
                Column(modifier = Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonLine(width = 120.dp, height = 14.dp)
                    SkeletonLine(width = 160.dp)
                }
            }
        }
        SkeletonCard {
            SkeletonRoutePoint(width = 180.dp)
            SkeletonRoutePoint(width = 140.dp)
        }
        SkeletonBlock(modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp))
        SkeletonCard {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SkeletonLine(width = 96.dp)
                Box(Modifier.weight(1f))
                SkeletonLine(width = 56.dp, height = 16.dp)
            }
        }
    }
}

@Composable
private fun SkeletonRoutePoint(width: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SkeletonCircle(size = 12.dp)
        Column(modifier = Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonLine(width = width, height = 14.dp)
            SkeletonLine(width = width * SUBTITLE_RATIO, height = 10.dp)
        }
    }
}

/** The shared sweep: its progress, and where the [Skeleton] sits, so every block lights up in one band. */
@Stable
private class SkeletonSweep(val progress: State<Float>) {
    var left by mutableFloatStateOf(0f)
    var width by mutableFloatStateOf(0f)
}

private val LocalSkeletonSweep = staticCompositionLocalOf<SkeletonSweep?> { null }
private val LocalSkeletonTone = staticCompositionLocalOf { SkeletonTone.OnLight }

private const val SWEEP_MS = 1200
private val BAND = 160.dp
private const val ON_BRAND_BASE = 0.18f
private const val ON_BRAND_LIGHT = 0.32f
private const val SUBTITLE_RATIO = 0.6f

@Preview
@Composable
fun SkeletonPreview() {
    HitchyTheme {
        Surface(color = HitchyTheme.colors.white) {
            Skeleton(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SkeletonRideCard()
                SkeletonRideCard()
                SkeletonListRow()
                SkeletonListRow()
                SkeletonLine(width = 200.dp, height = 20.dp)
            }
        }
    }
}
