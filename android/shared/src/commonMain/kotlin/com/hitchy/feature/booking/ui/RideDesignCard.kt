package com.hitchy.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.BookingStatus
import com.hitchy.core.model.RideStatus
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.StatusPill
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tightLine
import com.hitchy.core.util.dateKey
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.feature.booking.presentation.RidesTabViewModel
import com.hitchy.feature.booking.presentation.UnifiedRideItem
import com.hitchy.feature.booking.presentation.opensOwnerTrip
import com.hitchy.feature.booking.presentation.opensRiderTrip
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.format_egp
import hitchy.shared.generated.resources.location_dropoff_label
import hitchy.shared.generated.resources.location_pickup_label
import hitchy.shared.generated.resources.owner_design_status_posted
import hitchy.shared.generated.resources.price_per_seat_large
import hitchy.shared.generated.resources.rider_home_date_format
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import hitchy.shared.generated.resources.rider_home_months
import hitchy.shared.generated.resources.rider_home_weekdays
import hitchy.shared.generated.resources.rides_design_vehicle
import hitchy.shared.generated.resources.rides_design_when
import hitchy.shared.generated.resources.rides_filter_cancelled
import hitchy.shared.generated.resources.rides_filter_completed
import hitchy.shared.generated.resources.rides_filter_upcoming
import hitchy.shared.generated.resources.rides_t14_seats_booked
import hitchy.shared.generated.resources.rides_tab_empty_body
import hitchy.shared.generated.resources.rides_tab_empty_title
import hitchy.shared.generated.resources.rides_tab_role_owner_badge
import hitchy.shared.generated.resources.rides_tab_role_rider_badge
import hitchy.shared.generated.resources.rides_tab_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private val CardRadius = 16.dp
private const val DATE_MONTH_START = 5
private const val DATE_MONTH_END = 7
private const val DATE_DAY_START = 8

/**
 * The owner's ride pill, one rule for the Rides list and the ride details (Figma uses both words for the same
 * card): "Confirmed" once a seat is booked, "Posted" before that, then the ride's own status.
 */
@Composable
internal fun ownerRidePill(status: RideStatus, bookedSeats: Int): Pair<RideStatusVariant, String?> = when {
    status != RideStatus.OPEN && status != RideStatus.FULL -> status.toPillVariant() to null
    bookedSeats > 0 -> RideStatusVariant.Confirmed to null
    else -> RideStatusVariant.InProgress to stringResource(Res.string.owner_design_status_posted)
}

private fun RideStatus.toPillVariant(): RideStatusVariant = when (this) {
    RideStatus.OPEN, RideStatus.FULL -> RideStatusVariant.Confirmed
    RideStatus.STARTED -> RideStatusVariant.InProgress
    RideStatus.FINISHED -> RideStatusVariant.Completed
    RideStatus.CANCELLED -> RideStatusVariant.Cancelled
    RideStatus.EXPIRED -> RideStatusVariant.Expired
}

/** What one card shows. Built from a booking (rider) or an offered ride (owner). */
internal data class RideCardData(
    val status: RideStatusVariant,
    val dateKey: String,
    val time: String,
    val pickupName: String,
    val dropOffName: String?,
    val title: String,
    val subtitle: String?,
    val tileIcon: DrawableResource,
    val priceEgp: Int,
    val onClick: (() -> Unit)?,
    val perSeat: Boolean = false,
    /** Overrides the status pill text, e.g. "Posted" on the owner's ride details. */
    val statusLabel: String? = null,
    /** The first stop's label: "Pickup" for a rider, "Start" for the owner (Figma T14 and R02). */
    val startLabel: String? = null,
)

@Composable
internal fun RideDesignCard(
    data: RideCardData,
    todayKey: String,
    tomorrowKey: String,
) {
    val colors = HitchyTheme.colors
    val shape = RoundedCornerShape(CardRadius)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .then(if (data.onClick != null) Modifier.clickable(onClick = data.onClick) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatusPill(status = data.status, label = data.statusLabel)
            Text(
                text = whenLabel(data.dateKey, data.time, todayKey, tomorrowKey),
                style = HitchyTheme.typography.caption,
                color = colors.textSecondary,
            )
        }
        RideRoute(pickupName = data.pickupName, dropOffName = data.dropOffName, startLabel = data.startLabel)
        // Figma: the footer is 24dp below the route, on a 1dp rule with 12dp above its content.
        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HorizontalDivider(color = colors.border, thickness = 1.dp)
            RideSummaryRow(data.tileIcon, data.title, data.subtitle, data.priceEgp, data.perSeat)
        }
    }
}

@Composable
private fun RideRoute(
    pickupName: String,
    dropOffName: String?,
    startLabel: String?,
) {
    val colors = HitchyTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(27.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(width = 10.5.dp, height = if (dropOffName != null) 74.dp else 10.dp)) {
            if (dropOffName != null) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 5.dp)
                        .size(width = 2.dp, height = 58.dp)
                        .background(colors.lavender),
                )
                Icon(
                    painter = painterResource(HitchyIcons.PinSmall),
                    contentDescription = null,
                    tint = colors.pin,
                    modifier = Modifier.offset(y = 60.dp).size(width = 10.5.dp, height = 14.dp),
                )
            }
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.deepPurple))
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RouteStop(name = pickupName, label = startLabel ?: stringResource(Res.string.location_pickup_label))
            if (dropOffName != null) {
                RouteStop(name = dropOffName, label = stringResource(Res.string.location_dropoff_label))
            }
        }
    }
}

@Composable
private fun RouteStop(
    name: String,
    label: String,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column {
        Text(text = name, style = typography.bodyLargeStrong, color = colors.navy, maxLines = 1)
        Text(text = label, style = typography.body, color = colors.textSecondary)
    }
}

@Composable
internal fun RideSummaryRow(
    tileIcon: DrawableResource,
    title: String,
    subtitle: String?,
    priceEgp: Int,
    perSeat: Boolean,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconTile(icon = tileIcon, tileColor = colors.lavender, iconColor = colors.purple)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = typography.title, color = colors.navy, maxLines = 1, modifier = Modifier.tightLine(typography.title.fontSize))
            if (subtitle != null) {
                Text(text = subtitle, style = typography.body, color = colors.textSecondary, maxLines = 1, modifier = Modifier.tightLine(typography.body.fontSize))
            }
        }
        Text(
            text = if (perSeat) {
                stringResource(Res.string.price_per_seat_large, priceEgp)
            } else {
                stringResource(Res.string.format_egp, priceEgp.toString())
            },
            style = typography.bodyStrong,
            color = colors.navy,
        )
    }
}

@Composable
private fun IconTile(
    icon: DrawableResource,
    tileColor: Color,
    iconColor: Color,
) {
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).background(tileColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** "Today · 07:30", "Tomorrow · 07:30" or "Wed, Oct 28 · 07:30". */
@Composable
internal fun whenLabel(
    dateKey: String,
    time: String,
    todayKey: String,
    tomorrowKey: String,
): String {
    val day = when (dateKey) {
        todayKey -> stringResource(Res.string.rider_home_date_today)
        tomorrowKey -> stringResource(Res.string.rider_home_date_tomorrow)
        else -> longDate(dateKey)
    }
    return stringResource(Res.string.rides_design_when, day, time)
}

@Composable
private fun longDate(dateKey: String): String {
    val weekdays = stringArrayResource(Res.array.rider_home_weekdays)
    val months = stringArrayResource(Res.array.rider_home_months)
    val date = runCatching { kotlinx.datetime.LocalDate.parse(dateKey) }.getOrNull() ?: return dateKey
    val month = dateKey.substring(DATE_MONTH_START, DATE_MONTH_END).toInt()
    val day = dateKey.substring(DATE_DAY_START).toInt()
    return stringResource(
        Res.string.rider_home_date_format,
        weekdays[date.dayOfWeek.ordinal],
        months[month - 1],
        day,
    )
}
