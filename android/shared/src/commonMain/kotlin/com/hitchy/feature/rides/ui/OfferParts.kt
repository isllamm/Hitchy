@file:Suppress("TooManyFunctions")

package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.theme.HitchyTheme
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.offer_wheel_am
import hitchy.shared.generated.resources.offer_wheel_pm
import org.jetbrains.compose.resources.stringResource

internal val OfferCardShape = RoundedCornerShape(16.dp)

/** Figma's card shadows on the Offer a ride screens: navy at 4%, 0/4 blur 16 and 0/1 blur 2. */
private const val CARD_SHADOW_ALPHA = 0.04f
private const val MINUTES_PER_HOUR = 60
private const val HOURS_HALF_DAY = 12
private const val NOON_MINUTES = 720

/** Figma's white card with a #E5E7EB stroke and two soft navy shadows (Departure, Seats, Commute summary). */
@Composable
internal fun Modifier.offerCard(): Modifier {
    val colors = HitchyTheme.colors
    return this
        .figmaShadow(OfferCardShape, colors.navy.copy(alpha = CARD_SHADOW_ALPHA), blur = 16.dp, y = 4.dp)
        .figmaShadow(OfferCardShape, colors.navy.copy(alpha = CARD_SHADOW_ALPHA), blur = 2.dp, y = 1.dp)
        .clip(OfferCardShape)
        .background(colors.white)
        .border(1.dp, colors.border, OfferCardShape)
}

/** Figma's `Top App Bar` (60dp, padding 20): the outlined back button, an 18/26 title, a 44dp spacer. */
@Composable
internal fun OfferTopBar(title: String, onBack: (() -> Unit)?, horizontal: Dp = 20.dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = figmaTop(60.dp) - 8.dp, start = horizontal, end = horizontal)
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            BackButton(onClick = onBack, style = BackButtonStyle.OutlinedCircle, modifier = Modifier.size(44.dp))
        } else {
            Box(Modifier.size(44.dp))
        }
        Text(
            text = title,
            style = HitchyTheme.typography.title,
            color = HitchyTheme.colors.navy,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        Box(Modifier.size(44.dp))
    }
}

/** Figma's `Step`: a 12/18 bold gray line over a 6dp progress bar. */
@Composable
internal fun OfferStepHeader(text: String, fraction: Float) {
    val colors = HitchyTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = text, style = HitchyTheme.typography.captionStrong, color = colors.textSecondary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(colors.disabledSurface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(colors.purple),
            )
        }
    }
}

/** Figma's `Footer`: white, a #E5E7EB top line, padding 16, the main button. */
@Composable
internal fun OfferFooter(content: @Composable ColumnScope.() -> Unit) {
    val colors = HitchyTheme.colors
    Column(modifier = Modifier.fillMaxWidth().background(colors.white)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

/** Figma's `Toggle`: a 16/24 label and a 48x28 track (padding 3) with a 22dp knob. */
@Composable
internal fun OfferToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = HitchyTheme.typography.bodyLarge,
            color = colors.navy,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 28.dp)
                .clip(CircleShape)
                .background(if (checked) colors.purple else colors.border)
                .padding(3.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(Modifier.size(22.dp).clip(CircleShape).background(colors.white))
        }
    }
}

/** A Figma pill tag: 26dp, padding 4/12, Bold 12/18. */
@Composable
internal fun OfferTag(text: String, background: Color, color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = text, style = HitchyTheme.typography.captionStrong, color = color)
    }
}

/** One stop of Figma's `Route Timeline`: a 72dp time column, a 12dp marker with its line, and the place. */
internal data class TimelineStop(val time: String, val label: String, val place: String)

/**
 * Figma's `Route Timeline` (component 18:28): the first stop has a filled purple dot, the middle stops a
 * smaller purple dot, the last one a white dot with a 2.5dp navy ring. A 2dp #E5E7EB line joins them.
 */
@Composable
internal fun OfferRouteTimeline(stops: List<TimelineStop>) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column {
        stops.forEachIndexed { index, stop ->
            val last = index == stops.lastIndex
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stop.time,
                    style = typography.bodyBold,
                    color = colors.navy,
                    modifier = Modifier.width(72.dp),
                )
                Column(
                    modifier = Modifier.width(12.dp).padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TimelineMarker(index = index, last = last)
                    if (!last) Box(Modifier.width(2.dp).height(36.dp).background(colors.border))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stop.label, style = typography.caption, color = colors.textSecondary)
                    Text(text = stop.place, style = typography.bodyBold, color = colors.navy)
                }
            }
        }
    }
}

@Composable
private fun TimelineMarker(index: Int, last: Boolean) {
    val colors = HitchyTheme.colors
    when {
        last -> Box(
            Modifier.size(12.dp).clip(CircleShape).background(colors.white).border(2.5.dp, colors.navy, CircleShape),
        )

        index == 0 -> Box(Modifier.size(12.dp).clip(CircleShape).background(colors.purple))

        else -> Box(Modifier.size(12.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(colors.purple))
        }
    }
}

/** Figma's `Commute summary` card: the route timeline over the tags. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OfferCommuteSummary(stops: List<TimelineStop>, tags: List<String>, gap: Dp = 16.dp) {
    val colors = HitchyTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().offerCard().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        OfferRouteTimeline(stops)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags.forEachIndexed { index, tag ->
                // Figma: the seats tag is gray on #F8F9FC; the ride tags are purple on lavender.
                if (index == 0) {
                    OfferTag(tag, colors.background, colors.textSecondary)
                } else {
                    OfferTag(tag, colors.lavender, colors.purple)
                }
            }
        }
    }
}

/** A clickable row helper: clips to [shape] first so the ripple keeps the corners. */
internal fun Modifier.clickableShape(shape: RoundedCornerShape, onClick: () -> Unit): Modifier = this
    .clip(shape)
    .clickable(onClick = onClick)

/** 450 minutes → "07:30 AM", as Figma writes the departure (two-digit hour, AM or PM). */
@Composable
internal fun clock12(minutesOfDay: Int): String {
    val minutes = ((minutesOfDay % (HOURS_HALF_DAY * 2 * MINUTES_PER_HOUR)) + HOURS_HALF_DAY * 2 * MINUTES_PER_HOUR) %
        (HOURS_HALF_DAY * 2 * MINUTES_PER_HOUR)
    val hour24 = minutes / MINUTES_PER_HOUR
    val hour12 = if (hour24 % HOURS_HALF_DAY == 0) HOURS_HALF_DAY else hour24 % HOURS_HALF_DAY
    val half = stringResource(if (minutes < NOON_MINUTES) Res.string.offer_wheel_am else Res.string.offer_wheel_pm)
    val mm = (minutes % MINUTES_PER_HOUR).toString().padStart(2, '0')
    return "${hour12.toString().padStart(2, '0')}:$mm $half"
}
