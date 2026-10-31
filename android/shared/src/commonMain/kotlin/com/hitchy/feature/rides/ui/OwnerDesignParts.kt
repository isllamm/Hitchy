@file:Suppress("MagicNumber", "TooManyFunctions")

package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.RideStatus
import com.hitchy.core.ui.TightTextGap
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.StatusChip
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tight
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.feature.rides.domain.Ride
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.format_egp
import hitchy.shared.generated.resources.location_dropoff_label
import hitchy.shared.generated.resources.location_pickup_label
import hitchy.shared.generated.resources.owner_home_wallet_label
import hitchy.shared.generated.resources.price_per_seat_large
import hitchy.shared.generated.resources.rider_home_date_format
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import hitchy.shared.generated.resources.rider_home_months
import hitchy.shared.generated.resources.rider_home_weekdays
import hitchy.shared.generated.resources.rides_design_vehicle
import hitchy.shared.generated.resources.rides_design_when
import hitchy.shared.generated.resources.rides_t14_seats_booked
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

private val CardRadius = 20.dp
private const val DATE_MONTH_START = 5
private const val DATE_MONTH_END = 7
private const val DATE_DAY_START = 8

/** A bordered 20dp card with a lilac icon tile and a title; optional trailing control and body. */
@Composable
internal fun OwnerSectionCard(
    icon: DrawableResource,
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = HitchyTheme.colors
    val shape = RoundedCornerShape(CardRadius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.white)
            .border(1.dp, colors.border, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OwnerIconTile(icon = icon)
            Text(
                text = title,
                style = HitchyTheme.typography.bodyStrong,
                color = colors.navy,
                modifier = Modifier.weight(1f),
            )
            trailing?.invoke()
        }
        content?.invoke(this)
    }
}

@Composable
internal fun OwnerIconTile(icon: DrawableResource) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.purple,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** The big gradient button used on Owner Home and on the Offer a ride form. */
@Composable
internal fun OwnerGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    loading: Boolean = false,
) {
    val colors = HitchyTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.brandGradient)
            .clickable(enabled = !loading, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = colors.white)
        } else {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = colors.white,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = text,
                style = HitchyTheme.typography.bodyStrong,
                color = colors.white,
                modifier = Modifier.padding(start = if (icon != null) 8.dp else 0.dp),
            )
        }
    }
}

@Composable
internal fun OwnerBellButton(
    hasUnread: Boolean,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier.size(52.dp).clip(CircleShape).background(colors.white).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(HitchyIcons.Bell),
            contentDescription = null,
            tint = colors.navy,
            modifier = Modifier.size(24.dp),
        )
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(colors.pin),
            )
        }
    }
}

@Composable
internal fun OwnerWalletCard(balanceEgp: Int) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(colors.brandGradient)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(Res.string.owner_home_wallet_label),
            style = typography.caption,
            color = colors.white,
        )
        Text(
            text = stringResource(Res.string.format_egp, balanceEgp.toString()),
            style = typography.h1,
            color = colors.white,
        )
    }
}

/** The owner's ride card, same look as the Rides tab card: status pill, route, footer. */
@Composable
internal fun OwnerRideCard(
    ride: Ride,
    language: AppLanguage,
    todayKey: String,
    tomorrowKey: String,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val shape = RoundedCornerShape(CardRadius)
    val whenText = whenLabel(isoToCairoDateKey(ride.departAt), cairoTime12(ride.departAt), todayKey, tomorrowKey)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatusChip(status = ride.status.toVariant())
            Text(
                text = whenText,
                style = HitchyTheme.typography.caption,
                color = colors.textSecondary,
            )
        }
        OwnerRideRoute(
            pickupName = ride.fromPointName.resolve(language),
            dropOffName = ride.toPointName.resolve(language),
        )
        HorizontalDivider(color = colors.border, thickness = 1.dp)
        OwnerRideFooter(ride = ride)
    }
}

@Composable
private fun OwnerRideRoute(
    pickupName: String,
    dropOffName: String,
) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().width(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.padding(top = 6.dp).size(12.dp).clip(CircleShape).background(colors.deepPurple),
            )
            Box(modifier = Modifier.weight(1f).width(2.dp).background(colors.lavender))
            Icon(
                painter = painterResource(HitchyIcons.Pin),
                contentDescription = null,
                tint = colors.pin,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OwnerRouteStop(name = pickupName, label = stringResource(Res.string.location_pickup_label))
            OwnerRouteStop(name = dropOffName, label = stringResource(Res.string.location_dropoff_label))
        }
    }
}

@Composable
private fun OwnerRouteStop(
    name: String,
    label: String,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column {
        Text(text = name, style = typography.bodyStrong, color = colors.navy, maxLines = 1)
        Text(text = label, style = typography.caption, color = colors.textSecondary)
    }
}

@Composable
private fun OwnerRideFooter(ride: Ride) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OwnerIconTile(icon = HitchyIcons.Car)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TightTextGap)) {
            Text(
                text = stringResource(
                    Res.string.rides_t14_seats_booked,
                    (ride.seatsTotal - ride.seatsLeft).coerceAtLeast(0),
                    ride.seatsTotal,
                ),
                style = typography.bodyStrong.tight(),
                color = colors.navy,
                maxLines = 1,
            )
            Text(
                text = stringResource(
                    Res.string.rides_design_vehicle,
                    ride.vehicle.make,
                    ride.vehicle.model,
                    ride.vehicle.color,
                ),
                style = typography.caption.tight(),
                color = colors.textSecondary,
                maxLines = 1,
            )
        }
        Text(
            text = stringResource(Res.string.price_per_seat_large, ride.pricePerSeat),
            style = typography.bodyStrong,
            color = colors.navy,
        )
    }
}

/** "Today · 07:30", "Tomorrow · 07:30" or "Wed, Oct 28 · 07:30". */
@Composable
private fun whenLabel(
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

private fun RideStatus.toVariant(): RideStatusVariant = when (this) {
    RideStatus.OPEN, RideStatus.FULL -> RideStatusVariant.Confirmed
    RideStatus.STARTED -> RideStatusVariant.InProgress
    RideStatus.FINISHED -> RideStatusVariant.Completed
    RideStatus.CANCELLED -> RideStatusVariant.Cancelled
    RideStatus.EXPIRED -> RideStatusVariant.Expired
}
