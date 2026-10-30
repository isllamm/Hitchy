@file:Suppress("MagicNumber", "TooManyFunctions")

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.StatusChip
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.dateKey
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.core.util.isoToCairoTime
import com.hitchy.feature.booking.presentation.RidesTabViewModel
import com.hitchy.feature.booking.presentation.UnifiedRideItem
import com.hitchy.feature.booking.presentation.opensOwnerTrip
import com.hitchy.feature.booking.presentation.opensRiderTrip
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.format_egp
import hitchy.shared.generated.resources.location_dropoff_label
import hitchy.shared.generated.resources.location_pickup_label
import hitchy.shared.generated.resources.price_per_seat_large
import hitchy.shared.generated.resources.rider_home_date_format
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import hitchy.shared.generated.resources.rider_home_months
import hitchy.shared.generated.resources.rider_home_weekdays
import hitchy.shared.generated.resources.rides_design_vehicle
import hitchy.shared.generated.resources.rides_design_when
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

private val CardRadius = 20.dp
private val TileSize = 40.dp
private const val DATE_MONTH_START = 5
private const val DATE_MONTH_END = 7
private const val DATE_DAY_START = 8

/** What one card shows. Built from a booking (rider) or an offered ride (owner). */
private data class RideCardData(
    val status: RideStatusVariant,
    val dateKey: String,
    val time: String,
    val pickupName: String,
    val dropOffName: String?,
    val title: String,
    val subtitle: String?,
    val tileIcon: DrawableResource,
    val priceEgp: Int,
    val onClick: () -> Unit,
    val perSeat: Boolean = false,
)

/**
 * The single Rides list built to the demo design: big title, then one card per ride with a status pill, the
 * date and time, the route, and a footer with the price. Replaces the older RidesTabContent on the Rides tab.
 */
@Composable
fun RiderRidesTabContent(
    onBack: (() -> Unit)?,
    onOpenRiderRideDetails: (String) -> Unit,
    onOpenRiderTrip: (String) -> Unit,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    clock: Clock = koinInject(),
    viewModel: RidesTabViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val language: AppLanguage = languageSwitcher.getCurrentLanguage()
    val now = clock.now()
    val todayKey = dateKey(now)
    val tomorrowKey = dateKey(now + 1.days)

    Surface(modifier = modifier.fillMaxSize(), color = colors.white) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            RidesHeader(onBack = onBack)

            if (state.items.isEmpty() && !state.isLoading) {
                EmptyState(
                    title = stringResource(Res.string.rides_tab_empty_title),
                    message = stringResource(Res.string.rides_tab_empty_body),
                    icon = HitchyIcons.Rides,
                    modifier = Modifier.fillMaxSize(),
                )
                return@Column
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    items = state.items,
                    key = { item ->
                        when (item) {
                            is UnifiedRideItem.AsRider -> "rider_${item.rideId}"
                            is UnifiedRideItem.AsOwner -> "owner_${item.rideId}"
                        }
                    },
                ) { item ->
                    val data = item.toCardData(
                        language = language,
                        onOpenRiderRideDetails = onOpenRiderRideDetails,
                        onOpenRiderTrip = onOpenRiderTrip,
                        onOpenHostRideDetails = onOpenHostRideDetails,
                        onOpenHostTrip = onOpenHostTrip,
                    )
                    RideDesignCard(data = data, todayKey = todayKey, tomorrowKey = tomorrowKey)
                }
            }
        }
    }
}

@Composable
private fun RidesHeader(onBack: (() -> Unit)?) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onBack != null) {
            BackButton(onClick = onBack, style = BackButtonStyle.OutlinedCircle)
        }
        Text(
            text = stringResource(Res.string.rides_tab_title),
            style = typography.h1,
            color = colors.navy,
        )
    }
}

@Composable
private fun UnifiedRideItem.toCardData(
    language: AppLanguage,
    onOpenRiderRideDetails: (String) -> Unit,
    onOpenRiderTrip: (String) -> Unit,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
): RideCardData = when (this) {
    is UnifiedRideItem.AsRider -> RideCardData(
        status = booking.status.toVariant(),
        dateKey = isoToCairoDateKey(booking.departAt),
        time = isoToCairoTime(booking.pickupAt),
        pickupName = booking.pickupPointName.resolve(language),
        dropOffName = null,
        title = stringResource(Res.string.rides_tab_role_rider_badge, booking.seats),
        subtitle = null,
        tileIcon = HitchyIcons.Person,
        priceEgp = booking.total,
        onClick = {
            if (booking.status.opensRiderTrip()) {
                onOpenRiderTrip(booking.rideId)
            } else {
                onOpenRiderRideDetails(booking.rideId)
            }
        },
    )

    is UnifiedRideItem.AsOwner -> RideCardData(
        status = ride.status.toVariant(),
        dateKey = isoToCairoDateKey(ride.departAt),
        time = isoToCairoTime(ride.departAt),
        pickupName = ride.fromPointName.resolve(language),
        dropOffName = ride.toPointName.resolve(language),
        title = stringResource(
            Res.string.rides_t14_seats_booked,
            (ride.seatsTotal - ride.seatsLeft).coerceAtLeast(0),
            ride.seatsTotal,
        ),
        subtitle = stringResource(
            Res.string.rides_design_vehicle,
            ride.vehicle.make,
            ride.vehicle.model,
            ride.vehicle.color,
        ),
        tileIcon = HitchyIcons.Car,
        priceEgp = ride.pricePerSeat,
        perSeat = true,
        onClick = {
            if (ride.status.opensOwnerTrip()) {
                onOpenHostTrip(ride.id)
            } else {
                onOpenHostRideDetails(ride.id)
            }
        },
    )
}

@Composable
private fun RideDesignCard(
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
            .clickable(onClick = data.onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatusChip(status = data.status)
            Text(
                text = whenLabel(data.dateKey, data.time, todayKey, tomorrowKey),
                style = HitchyTheme.typography.caption,
                color = colors.textSecondary,
            )
        }
        RideRoute(pickupName = data.pickupName, dropOffName = data.dropOffName)
        HorizontalDivider(color = colors.border, thickness = 1.dp)
        RideFooter(data = data)
    }
}

@Composable
private fun RideRoute(
    pickupName: String,
    dropOffName: String?,
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
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(colors.deepPurple),
            )
            if (dropOffName != null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(2.dp)
                        .background(colors.lavender),
                )
                Icon(
                    painter = painterResource(HitchyIcons.Pin),
                    contentDescription = null,
                    tint = colors.pin,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            RouteStop(name = pickupName, label = stringResource(Res.string.location_pickup_label))
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
        Text(text = name, style = typography.bodyStrong, color = colors.navy, maxLines = 1)
        Text(text = label, style = typography.caption, color = colors.textSecondary)
    }
}

@Composable
private fun RideFooter(data: RideCardData) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconTile(icon = data.tileIcon, tileColor = colors.lavender, iconColor = colors.purple)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = data.title, style = typography.bodyStrong, color = colors.navy, maxLines = 1)
            if (data.subtitle != null) {
                Text(text = data.subtitle, style = typography.caption, color = colors.textSecondary, maxLines = 1)
            }
        }
        Text(
            text = if (data.perSeat) {
                stringResource(Res.string.price_per_seat_large, data.priceEgp)
            } else {
                stringResource(Res.string.format_egp, data.priceEgp.toString())
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
        modifier = Modifier.size(TileSize).clip(RoundedCornerShape(12.dp)).background(tileColor),
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

private fun BookingStatus.toVariant(): RideStatusVariant = when (this) {
    BookingStatus.REQUESTED -> RideStatusVariant.Requested
    BookingStatus.BOOKED -> RideStatusVariant.Confirmed
    BookingStatus.BOARDED -> RideStatusVariant.InProgress
    BookingStatus.COMPLETED -> RideStatusVariant.Completed
    BookingStatus.DECLINED -> RideStatusVariant.Declined
    BookingStatus.EXPIRED -> RideStatusVariant.Expired
    BookingStatus.CANCELLED_BY_RIDER, BookingStatus.CANCELLED_BY_HOST -> RideStatusVariant.Cancelled
    BookingStatus.NO_SHOW -> RideStatusVariant.NoShow
}

private fun RideStatus.toVariant(): RideStatusVariant = when (this) {
    RideStatus.OPEN, RideStatus.FULL -> RideStatusVariant.Confirmed
    RideStatus.STARTED -> RideStatusVariant.InProgress
    RideStatus.FINISHED -> RideStatusVariant.Completed
    RideStatus.CANCELLED -> RideStatusVariant.Cancelled
    RideStatus.EXPIRED -> RideStatusVariant.Expired
}
