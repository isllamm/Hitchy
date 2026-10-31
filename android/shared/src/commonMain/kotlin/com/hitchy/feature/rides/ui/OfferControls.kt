package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.theme.HitchyTheme
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.offer_wheel_am
import hitchy.shared.generated.resources.offer_wheel_pm
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val WheelItemHeight = 44.dp
private const val WHEEL_VISIBLE = 3
private const val MINUTES_PER_HOUR = 60
private const val HOURS_HALF_DAY = 12
private const val MINUTE_STEP = 15
private const val KNOB_SHADOW_ALPHA = 0.25f

/**
 * Figma's `Time wheel` (327x132): three 80dp columns (hour, minutes, AM/PM) over a 48dp lilac band.
 * The middle row is Bold 20/28 navy, the others Regular 16/24 gray. Minutes move in 15-minute steps.
 */
@Composable
internal fun OfferTimeWheel(minutesOfDay: Int, onChange: (Int) -> Unit) {
    val colors = HitchyTheme.colors
    val hour24 = minutesOfDay / MINUTES_PER_HOUR
    val minute = minutesOfDay % MINUTES_PER_HOUR
    val isPm = hour24 >= HOURS_HALF_DAY
    val hour12 = if (hour24 % HOURS_HALF_DAY == 0) HOURS_HALF_DAY else hour24 % HOURS_HALF_DAY
    val hours = (1..HOURS_HALF_DAY).map { it.toString() }
    val minutes = (0 until MINUTES_PER_HOUR step MINUTE_STEP).map { it.toString().padStart(2, '0') }
    val halves = listOf(stringResource(Res.string.offer_wheel_am), stringResource(Res.string.offer_wheel_pm))

    fun build(h12: Int, m: Int, pm: Boolean): Int {
        val h24 = (h12 % HOURS_HALF_DAY) + if (pm) HOURS_HALF_DAY else 0
        return h24 * MINUTES_PER_HOUR + m
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(WheelItemHeight * WHEEL_VISIBLE),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.lilac)
                .border(1.dp, colors.lavender, RoundedCornerShape(12.dp)),
        )
        // A clock reads hour, minutes, AM/PM from the left in both languages.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WheelColumn(hours, hour12 - 1) { onChange(build(it + 1, minute, isPm)) }
                Text(text = ":", style = HitchyTheme.typography.h3, color = colors.navy)
                WheelColumn(minutes, minute / MINUTE_STEP) { onChange(build(hour12, it * MINUTE_STEP, isPm)) }
                WheelColumn(halves, if (isPm) 1 else 0) { onChange(build(hour12, minute, it == 1)) }
            }
        }
    }
}

@Composable
private fun WheelColumn(items: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val colors = HitchyTheme.colors
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val itemPx = with(LocalDensity.current) { WheelItemHeight.toPx() }
    val centered by remember {
        derivedStateOf {
            state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > itemPx / 2) 1 else 0
        }
    }
    val latestSelect by rememberUpdatedState(onSelect)

    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress && centered != selected) latestSelect(centered.coerceIn(items.indices))
    }
    LaunchedEffect(selected) {
        if (!state.isScrollInProgress && centered != selected) state.scrollToItem(selected)
    }

    LazyColumn(
        state = state,
        flingBehavior = rememberSnapFlingBehavior(state),
        contentPadding = PaddingValues(vertical = WheelItemHeight),
        modifier = Modifier.width(80.dp).height(WheelItemHeight * WHEEL_VISIBLE),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(items) { index, label ->
            val isCenter = index == centered
            Box(modifier = Modifier.height(WheelItemHeight).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    style = if (isCenter) HitchyTheme.typography.h3 else HitchyTheme.typography.bodyLarge,
                    color = if (isCenter) colors.navy else colors.textDisabled,
                )
            }
        }
    }
}

/**
 * Figma's price `Slider`: an 8dp lavender track, the purple part up to the price, and a 28dp white knob
 * with a 2dp purple ring and a soft purple shadow. Drag or tap to pick a whole price in [min, max].
 */
@Composable
internal fun OfferPriceSlider(value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    val colors = HitchyTheme.colors
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val span = (max - min).coerceAtLeast(1)
    val fraction = ((value - min).toFloat() / span).coerceIn(0f, 1f)
    val latestChange by rememberUpdatedState(onChange)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(28.dp)) {
        val widthPx = constraints.maxWidth.toFloat()
        fun pick(x: Float) {
            val f = (x / widthPx).coerceIn(0f, 1f).let { if (rtl) 1f - it else it }
            latestChange(min + (f * span).roundToInt())
        }
        val knob = 28.dp
        val trackWidth = maxWidth
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(min, max, widthPx) { detectTapGestures { pick(it.x) } }
                .pointerInput(
                    min,
                    max,
                    widthPx,
                ) { detectHorizontalDragGestures { change, _ -> pick(change.position.x) } },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(colors.lavender))
            Box(Modifier.fillMaxWidth(fraction).height(8.dp).clip(CircleShape).background(colors.purple))
            Box(
                modifier = Modifier
                    .offset(x = (trackWidth - knob) * fraction)
                    .size(knob)
                    .figmaShadow(CircleShape, colors.purple.copy(alpha = KNOB_SHADOW_ALPHA), blur = 8.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(colors.white)
                    .border(2.dp, colors.purple, CircleShape),
            )
        }
    }
}
