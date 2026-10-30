@file:Suppress("MagicNumber")

package com.hitchy.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.bk_trip_meta
import hitchy.shared.generated.resources.location_dropoff_label
import hitchy.shared.generated.resources.location_pickup_label
import hitchy.shared.generated.resources.rider_home_date_format
import hitchy.shared.generated.resources.rider_home_months
import hitchy.shared.generated.resources.rider_home_passengers_many
import hitchy.shared.generated.resources.rider_home_passengers_one
import hitchy.shared.generated.resources.rider_home_weekdays
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

// Figma's cards on the rider screens are r=16.
internal val BookingCardRadius = 16.dp

private const val BOOKING_DATE_MONTH_START = 5
private const val BOOKING_DATE_MONTH_END = 7
private const val BOOKING_DATE_DAY_START = 8

/** A white card with a 1dp border and a rounded corner, the card style of the rider screens. */
@Composable
internal fun BookingCardSurface(
    modifier: Modifier = Modifier,
    background: Color = HitchyTheme.colors.white,
    borderColor: Color = HitchyTheme.colors.border,
    radius: Dp = BookingCardRadius,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth().border(1.dp, borderColor, RoundedCornerShape(radius)),
        shape = RoundedCornerShape(radius),
        color = background,
        content = content,
    )
}

/** Figma's screen header: 44dp back button, an 18/26 centred title and a 44dp spacer, 60dp from the top. */
@Composable
internal fun BookingHeaderBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = figmaTop(HEADER_TOP), start = 16.dp, end = 16.dp, bottom = 16.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onClick = onBack, style = BackButtonStyle.OutlinedCircle, modifier = Modifier.size(44.dp))
        Text(
            text = title,
            style = typography.title,
            color = colors.navy,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        Box(modifier = Modifier.size(44.dp))
    }
}

private val HEADER_TOP = 60.dp

/** Figma's `Trip summary` (449:4164): pad 12/14, r=16, markers 27dp left of the names, a 12/18 meta line. */
@Composable
internal fun BookingTripCard(
    fromName: String,
    toName: String,
    metaText: String,
    modifier: Modifier = Modifier,
    fromLabel: String = stringResource(Res.string.location_pickup_label),
    toLabel: String = stringResource(Res.string.location_dropoff_label),
) {
    val typography = HitchyTheme.typography
    val colors = HitchyTheme.colors
    BookingCardSurface(modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(27.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BookingMarkerColumn()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BookingPointText(name = fromName, label = fromLabel)
                    BookingPointText(name = toName, label = toLabel)
                }
            }
            Text(text = metaText, style = typography.caption, color = colors.textSecondary)
        }
    }
}

@Composable
private fun BookingMarkerColumn() {
    val colors = HitchyTheme.colors
    // Figma: a 10dp dot, a 2x58 line from 5dp down, and the 10.5x14 pin at 60dp, in a 10x74 column.
    Box(modifier = Modifier.size(width = 10.5.dp, height = 74.dp)) {
        Box(modifier = Modifier.offset(x = 4.dp, y = 5.dp).size(width = 2.dp, height = 58.dp).background(colors.lavender))
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.deepPurple))
        Icon(
            painter = painterResource(HitchyIcons.PinSmall),
            contentDescription = null,
            tint = colors.pin,
            modifier = Modifier.offset(y = 60.dp).size(width = 10.5.dp, height = 14.dp),
        )
    }
}

@Composable
private fun BookingPointText(
    name: String,
    label: String,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column {
        Text(text = name, style = typography.bodyLargeStrong, color = colors.navy, maxLines = 1)
        Text(text = label, style = typography.body, color = colors.textSecondary, maxLines = 1)
    }
}

/** "Wed, Oct 28 · 07:30 · 2 passengers" from a "YYYY-MM-DD" key, an "HH:mm" time and a seat count. */
@Composable
internal fun bookingMetaText(
    dateKey: String,
    time: String,
    seats: Int,
): String {
    val passengers = if (seats == 1) {
        stringResource(Res.string.rider_home_passengers_one)
    } else {
        stringResource(Res.string.rider_home_passengers_many, seats)
    }
    return stringResource(Res.string.bk_trip_meta, bookingDateLabel(dateKey), bookingTime12(time), passengers)
}

/** "7:30 AM" from an "HH:mm" time, as Figma writes times; anything else is shown as given. */
@Composable
internal fun bookingTime12(time: String): String {
    val parts = time.split(":")
    val hours = parts.getOrNull(0)?.toIntOrNull() ?: return time
    val minutes = parts.getOrNull(1)?.toIntOrNull() ?: return time
    return formatTime12(hours * MINUTES_PER_HOUR_BOOKING + minutes)
}

private const val MINUTES_PER_HOUR_BOOKING = 60

/** "Wed, Oct 28" from a "YYYY-MM-DD" key (Cairo date). Day and month names come from string resources. */
@Composable
internal fun bookingDateLabel(dateKey: String): String {
    val weekdays = stringArrayResource(Res.array.rider_home_weekdays)
    val months = stringArrayResource(Res.array.rider_home_months)
    val date = runCatching { kotlinx.datetime.LocalDate.parse(dateKey) }.getOrNull() ?: return dateKey
    val month = dateKey.substring(BOOKING_DATE_MONTH_START, BOOKING_DATE_MONTH_END).toInt()
    val day = dateKey.substring(BOOKING_DATE_DAY_START).toInt()
    return stringResource(
        Res.string.rider_home_date_format,
        weekdays[date.dayOfWeek.ordinal],
        months[month - 1],
        day,
    )
}
