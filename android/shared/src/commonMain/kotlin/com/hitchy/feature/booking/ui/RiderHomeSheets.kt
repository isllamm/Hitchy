@file:Suppress("MagicNumber", "TooManyFunctions")

package com.hitchy.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.SelectChip
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import hitchy.shared.generated.resources.rider_home_months
import hitchy.shared.generated.resources.rider_sheet_am
import hitchy.shared.generated.resources.rider_sheet_confirm_date
import hitchy.shared.generated.resources.rider_sheet_confirm_time
import hitchy.shared.generated.resources.rider_sheet_date_format
import hitchy.shared.generated.resources.rider_sheet_date_title
import hitchy.shared.generated.resources.rider_sheet_month_year
import hitchy.shared.generated.resources.rider_sheet_months_full
import hitchy.shared.generated.resources.rider_sheet_pm
import hitchy.shared.generated.resources.rider_sheet_selected
import hitchy.shared.generated.resources.rider_sheet_time_note
import hitchy.shared.generated.resources.rider_sheet_time_title
import hitchy.shared.generated.resources.rider_sheet_weekdays_full
import hitchy.shared.generated.resources.rider_sheet_weekdays_sun_first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

private val CellSize = 44.dp
private val WheelRowHeight = 44.dp
private const val DAYS_AHEAD = 7
private const val DAYS_IN_WEEK = 7
private const val MINUTES_PER_HOUR = 60
private const val HOURS_PER_HALF_DAY = 12
private const val WHEEL_VISIBLE_ROWS = 5
private val MinuteSteps = listOf(0, 15, 30, 45)

/** The white sheet frame of the two Home sheets: handle, title, close button. */
@Composable
private fun SheetFrame(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(colors.white)
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(width = 40.dp, height = 4.dp).clip(CircleShape).background(colors.border))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = typography.h2, color = colors.navy)
            Icon(
                painter = painterResource(HitchyIcons.Close),
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(28.dp).clickable(onClick = onClose),
            )
        }
        content()
    }
}

/** "Select date": Today and Tomorrow chips, a month calendar (today to 7 days ahead), and Confirm. */
@Composable
fun DateSheet(
    todayKey: String,
    tomorrowKey: String,
    selectedKey: String,
    onPick: (String) -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val maxKey = remember(todayKey) { LocalDate.parse(todayKey).plus(DAYS_AHEAD, DateTimeUnit.DAY).toString() }
    var shown by remember { mutableStateOf(monthOf(selectedKey)) }

    SheetFrame(title = stringResource(Res.string.rider_sheet_date_title), onClose = onClose, modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectChip(
                label = stringResource(Res.string.rider_home_date_today),
                selected = selectedKey == todayKey,
                onClick = {
                    onPick(todayKey)
                    shown = monthOf(todayKey)
                },
            )
            SelectChip(
                label = stringResource(Res.string.rider_home_date_tomorrow),
                selected = selectedKey == tomorrowKey,
                onClick = {
                    onPick(tomorrowKey)
                    shown = monthOf(tomorrowKey)
                },
            )
        }
        MonthHeader(
            shown = shown,
            canGoBack = monthIndex(shown) > monthIndex(monthOf(todayKey)),
            canGoForward = monthIndex(shown) < monthIndex(monthOf(maxKey)),
            onMonthChange = { shown = it },
        )
        WeekdayHeader()
        MonthGrid(shown = shown, todayKey = todayKey, maxKey = maxKey, selectedKey = selectedKey, onPick = onPick)
        Text(
            text = stringResource(Res.string.rider_sheet_selected, fullDateLabel(selectedKey)),
            style = typography.caption,
            color = colors.textSecondary,
        )
        HitchyButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(Res.string.rider_sheet_confirm_date))
        }
    }
}

@Composable
private fun MonthHeader(
    shown: Pair<Int, Int>,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onMonthChange: (Pair<Int, Int>) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val months = stringArrayResource(Res.array.rider_sheet_months_full)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(HitchyIcons.ChevronDown),
            contentDescription = null,
            tint = if (canGoBack) colors.navy else colors.textDisabled,
            modifier = Modifier
                .size(28.dp)
                .rotate(90f)
                .clickable(enabled = canGoBack) { onMonthChange(shiftMonth(shown, -1)) },
        )
        Text(
            text = stringResource(Res.string.rider_sheet_month_year, months[shown.second - 1], shown.first),
            style = typography.bodyStrong,
            color = colors.navy,
        )
        Icon(
            painter = painterResource(HitchyIcons.ChevronDown),
            contentDescription = null,
            tint = if (canGoForward) colors.navy else colors.textDisabled,
            modifier = Modifier
                .size(28.dp)
                .rotate(-90f)
                .clickable(enabled = canGoForward) { onMonthChange(shiftMonth(shown, 1)) },
        )
    }
}

@Composable
private fun WeekdayHeader() {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val days = stringArrayResource(Res.array.rider_sheet_weekdays_sun_first)
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day,
                style = typography.captionStrong,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    shown: Pair<Int, Int>,
    todayKey: String,
    maxKey: String,
    selectedKey: String,
    onPick: (String) -> Unit,
) {
    val firstOffset = remember(shown) { firstWeekdaySunFirst(shown) }
    val daysInMonth = remember(shown) { daysIn(shown) }
    val cells = remember(shown) { List(firstOffset) { 0 } + (1..daysInMonth).toList() }
    val rows = remember(cells) { cells.chunked(DAYS_IN_WEEK) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (i in 0 until DAYS_IN_WEEK) {
                    val day = week.getOrNull(i) ?: 0
                    val key = if (day == 0) "" else keyOf(shown, day)
                    DayCell(
                        day = day,
                        enabled = day != 0 && key >= todayKey && key <= maxKey,
                        selected = key == selectedKey,
                        isToday = key == todayKey,
                        onClick = { onPick(key) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    enabled: Boolean,
    selected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Box(modifier = modifier.height(CellSize), contentAlignment = Alignment.Center) {
        if (day != 0) {
            val base = Modifier.size(CellSize).clip(CircleShape)
            val shape = when {
                selected -> base.background(colors.brandGradient)
                isToday -> base.border(1.dp, colors.purple, CircleShape)
                else -> base
            }
            Box(
                modifier = shape.clickable(enabled = enabled, onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.toString(),
                    style = typography.body,
                    color = when {
                        selected -> colors.white
                        enabled -> colors.navy
                        else -> colors.textDisabled
                    },
                )
            }
        }
    }
}

/** "Departure time": hour, minute and AM/PM wheels, a note and Confirm. */
@Composable
fun TimeSheet(
    dateKey: String,
    selectedMinutes: Int,
    isToday: Boolean,
    onChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val hour24 = selectedMinutes / MINUTES_PER_HOUR
    val minute = MinuteSteps.minByOrNull { kotlin.math.abs(it - selectedMinutes % MINUTES_PER_HOUR) } ?: 0
    val isPm = hour24 >= HOURS_PER_HALF_DAY
    val hour12 = (hour24 % HOURS_PER_HALF_DAY).let { if (it == 0) HOURS_PER_HALF_DAY else it }
    fun emit(h12: Int, m: Int, pm: Boolean) {
        val h = (h12 % HOURS_PER_HALF_DAY) + if (pm) HOURS_PER_HALF_DAY else 0
        onChange(h * MINUTES_PER_HOUR + m)
    }

    SheetFrame(title = stringResource(Res.string.rider_sheet_time_title), onClose = onClose, modifier = modifier) {
        Text(text = fullDateLabel(dateKey), style = typography.body, color = colors.textSecondary)
        Box(modifier = Modifier.fillMaxWidth().height(WheelRowHeight * WHEEL_VISIBLE_ROWS)) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(WheelRowHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.lilac),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                val hours = (1..HOURS_PER_HALF_DAY).map { it.toString() }
                Wheel(hours, hour12 - 1, wrap = true, modifier = Modifier.weight(1f)) { emit(it + 1, minute, isPm) }
                val minutes = MinuteSteps.map { it.toString().padStart(2, '0') }
                Wheel(minutes, MinuteSteps.indexOf(minute), wrap = true, modifier = Modifier.weight(1f)) {
                    emit(hour12, MinuteSteps[it], isPm)
                }
                val half = listOf(stringResource(Res.string.rider_sheet_am), stringResource(Res.string.rider_sheet_pm))
                Wheel(half, if (isPm) 1 else 0, wrap = false, modifier = Modifier.weight(1f)) {
                    emit(hour12, minute, it == 1)
                }
            }
        }
        if (isToday) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.lilac)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Info),
                    contentDescription = null,
                    tint = colors.purple,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(Res.string.rider_sheet_time_note),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
        HitchyButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(Res.string.rider_sheet_confirm_time, formatTime12(selectedMinutes)))
        }
    }
}

/** A drum of values: five rows, the middle one chosen. Drag up or down, or tap a neighbour. */
@Composable
private fun Wheel(
    items: List<String>,
    selectedIndex: Int,
    wrap: Boolean,
    modifier: Modifier = Modifier,
    onSelected: (Int) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val current by rememberUpdatedState(selectedIndex)
    val select by rememberUpdatedState(onSelected)
    val stepPx = with(LocalDensity.current) { WheelRowHeight.toPx() }

    fun move(by: Int) {
        val target = current + by
        val next = if (wrap) wrapIndex(target, items.size) else target.coerceIn(0, items.size - 1)
        if (next != current) select(next)
    }

    Column(
        modifier = modifier.pointerInput(items.size) {
            var carried = 0f
            detectVerticalDragGestures(onDragEnd = { carried = 0f }) { _, dy ->
                carried += dy
                while (carried <= -stepPx) {
                    move(1)
                    carried += stepPx
                }
                while (carried >= stepPx) {
                    move(-1)
                    carried -= stepPx
                }
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (offset in -2..2) {
            val raw = selectedIndex + offset
            val index = if (wrap) wrapIndex(raw, items.size) else raw
            Box(
                modifier = Modifier.height(WheelRowHeight).fillMaxWidth().clickable(enabled = index in items.indices) {
                    if (index in items.indices) select(index)
                },
                contentAlignment = Alignment.Center,
            ) {
                if (index in items.indices) {
                    Text(
                        text = items[index],
                        style = if (offset == 0) typography.h3 else typography.bodyLarge,
                        color = if (offset == 0) colors.navy else colors.textDisabled,
                    )
                }
            }
        }
    }
}

/** "7:30 AM" from minutes since midnight. */
@Composable
internal fun formatTime12(minutes: Int): String {
    val hour24 = minutes / MINUTES_PER_HOUR
    val hour12 = (hour24 % HOURS_PER_HALF_DAY).let { if (it == 0) HOURS_PER_HALF_DAY else it }
    val isPm = hour24 >= HOURS_PER_HALF_DAY
    val suffix = stringResource(if (isPm) Res.string.rider_sheet_pm else Res.string.rider_sheet_am)
    return "$hour12:${(minutes % MINUTES_PER_HOUR).toString().padStart(2, '0')} $suffix"
}

/** "Wednesday, Oct 28" for a "YYYY-MM-DD" key, from string resources. */
@Composable
private fun fullDateLabel(dateKey: String): String {
    val weekdays = stringArrayResource(Res.array.rider_sheet_weekdays_full)
    val months = stringArrayResource(Res.array.rider_home_months)
    val date = runCatching { LocalDate.parse(dateKey) }.getOrNull() ?: return dateKey
    val (_, month) = monthOf(dateKey)
    val day = dateKey.substring(DAY_START).toInt()
    return stringResource(Res.string.rider_sheet_date_format, weekdays[date.dayOfWeek.ordinal], months[month - 1], day)
}

private const val DAY_START = 8

private fun wrapIndex(value: Int, size: Int): Int = ((value % size) + size) % size

private fun monthIndex(month: Pair<Int, Int>): Int = month.first * HOURS_PER_HALF_DAY + month.second

private fun monthOf(key: String): Pair<Int, Int> = key.substring(0, 4).toInt() to key.substring(5, 7).toInt()

private fun keyOf(month: Pair<Int, Int>, day: Int): String {
    val mm = month.second.toString().padStart(2, '0')
    val dd = day.toString().padStart(2, '0')
    return "${month.first}-$mm-$dd"
}

private fun shiftMonth(month: Pair<Int, Int>, by: Int): Pair<Int, Int> {
    val index = month.first * HOURS_PER_HALF_DAY + (month.second - 1) + by
    return index / HOURS_PER_HALF_DAY to (index % HOURS_PER_HALF_DAY) + 1
}

private fun daysIn(month: Pair<Int, Int>): Int {
    val (year, m) = month
    val leap = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
    return when (m) {
        2 -> if (leap) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
}

/** 0 = Sunday ... 6 = Saturday for the first day of the month. */
private fun firstWeekdaySunFirst(month: Pair<Int, Int>): Int {
    val first = LocalDate(month.first, month.second, 1)
    return (first.dayOfWeek.ordinal + 1) % DAYS_IN_WEEK
}
