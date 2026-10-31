@file:Suppress("TooManyFunctions")

package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.RideStatus
import com.hitchy.core.ui.WhiteStatusBarIcons
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.dateKey
import com.hitchy.feature.rides.presentation.OwnerHomeAction
import com.hitchy.feature.rides.presentation.OwnerHomeState
import com.hitchy.feature.rides.presentation.OwnerHomeStats
import com.hitchy.feature.rides.presentation.OwnerHomeViewModel
import com.hitchy.feature.rides.presentation.ownerHomeStats
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.owner_home_action_add
import hitchy.shared.generated.resources.owner_home_action_earnings
import hitchy.shared.generated.resources.owner_home_action_request_ride
import hitchy.shared.generated.resources.owner_home_action_requests
import hitchy.shared.generated.resources.owner_home_currency
import hitchy.shared.generated.resources.owner_home_month_earnings
import hitchy.shared.generated.resources.owner_home_month_seats
import hitchy.shared.generated.resources.owner_home_months_full
import hitchy.shared.generated.resources.owner_home_quick_actions
import hitchy.shared.generated.resources.owner_home_stat_points
import hitchy.shared.generated.resources.owner_home_stat_requests
import hitchy.shared.generated.resources.owner_home_stat_rides
import hitchy.shared.generated.resources.owner_home_stat_seats
import hitchy.shared.generated.resources.owner_home_view_details
import hitchy.shared.generated.resources.owner_home_welcome
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

private val HeaderShape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
private val StatShape = RoundedCornerShape(18.dp)
private val ActionShape = RoundedCornerShape(18.dp)

/** Figma's glass layers on the purple header: white at 14% (card), 22% (card stroke), 15% (bell), 50% (avatar ring). */
private const val CARD_FILL_ALPHA = 0.14f
private const val CARD_STROKE_ALPHA = 0.22f
private const val BELL_FILL_ALPHA = 0.15f
private const val BELL_SHADOW_ALPHA = 0.15f
private const val AVATAR_RING_ALPHA = 0.50f

/** Figma's stat card shadows (#0C0C0D at 5% and 10%) and the "Add a ride" tile shadow (purple at 30%). */
private const val STAT_SHADOW_SOFT = 0.05f
private const val STAT_SHADOW_ALPHA = 0.10f
private const val ADD_SHADOW_ALPHA = 0.30f

private const val MONTH_START = 5
private const val MONTH_END = 7
private const val THOUSAND = 1000

/**
 * Owner Home, Figma DEMO 726:11671: a purple header with the owner, the bell and this month's earnings,
 * four stat cards and four quick actions. The tab bar's "+" (BottomNav) also opens Offer a ride.
 */
@Suppress("LongParameterList")
@Composable
fun OwnerHomeContent(
    onOpenNotifications: () -> Unit,
    onOpenPostRide: () -> Unit,
    onOpenHostRideDetails: (String) -> Unit,
    onOpenHostTrip: (String) -> Unit,
    onOpenRidesTab: () -> Unit,
    onOpenEarnings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerHomeViewModel = koinViewModel(),
    clock: Clock = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WhiteStatusBarIcons()
    val monthKey = dateKey(clock.now()).take(MONTH_END)
    val stats = ownerHomeStats(state.upcomingRides, monthKey)
    val months = stringArrayResource(Res.array.owner_home_months_full)
    val monthName = months.getOrElse(monthKey.substring(MONTH_START).toInt() - 1) { "" }
    val requestsRideId = state.pendingRequestsCountByRide.entries.firstOrNull { it.value > 0 }?.key
    val newRequests = state.pendingRequestsCountByRide.values.sum()
    val openRequests = { requestsRideId?.let(onOpenHostRideDetails) ?: onOpenRidesTab() }
    val openRides = {
        state.upcomingRides.firstOrNull { it.status == RideStatus.STARTED }?.let { onOpenHostTrip(it.id) }
            ?: onOpenRidesTab()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HitchyTheme.colors.white)
            .verticalScroll(rememberScrollState()),
    ) {
        OwnerHeader(
            state = state,
            monthName = monthName,
            stats = stats,
            onOpenNotifications = onOpenNotifications,
            onOpenEarnings = onOpenEarnings,
        )
        Spacer(Modifier.height(20.dp))
        OwnerStats(
            points = state.pointsBalance,
            newRequests = newRequests,
            stats = stats,
            onOpenRequests = openRequests,
            onOpenRides = openRides,
        )
        Spacer(Modifier.height(24.dp))
        OwnerQuickActions(
            newRequests = newRequests,
            onAddRide = onOpenPostRide,
            onOpenRequests = openRequests,
            onRequestRide = { viewModel.onAction(OwnerHomeAction.SwitchToRider) },
            onOpenEarnings = onOpenEarnings,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OwnerHeader(
    state: OwnerHomeState,
    monthName: String,
    stats: OwnerHomeStats,
    onOpenNotifications: () -> Unit,
    onOpenEarnings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HeaderShape)
            .background(HitchyTheme.colors.purple)
            .padding(start = 16.dp, end = 16.dp, top = figmaTop(68.dp), bottom = 42.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        OwnerTopBar(
            firstName = state.firstName,
            initials = "${state.firstName.take(1)}${state.lastName.take(1)}".uppercase(),
            fullName = listOf(state.firstName, state.lastName).filter { it.isNotBlank() }.joinToString(" "),
            unreadCount = state.unreadCount,
            onOpenNotifications = onOpenNotifications,
        )
        EarningsCard(monthName = monthName, stats = stats, onOpenDetails = onOpenEarnings)
    }
}

@Composable
private fun OwnerTopBar(
    firstName: String,
    initials: String,
    fullName: String,
    unreadCount: Int,
    onOpenNotifications: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.white)
                    .border(2.dp, colors.white.copy(alpha = AVATAR_RING_ALPHA), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = initials, style = typography.bodyBold, color = colors.purple)
            }
            Column {
                Text(
                    text = stringResource(Res.string.owner_home_welcome),
                    style = typography.body,
                    color = colors.white,
                )
                Text(
                    text = fullName.ifBlank { firstName },
                    style = typography.h3,
                    color = colors.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        HeaderBell(unreadCount = unreadCount, onClick = onOpenNotifications)
    }
}

/** Figma's "Notifications" group: a 40dp white-15% circle with a shadow, the linear bell and a pink count. */
@Composable
private fun HeaderBell(unreadCount: Int, onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    Box(modifier = Modifier.size(width = 40.dp, height = 44.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(40.dp)
                .figmaShadow(CircleShape, Color.Black.copy(alpha = BELL_SHADOW_ALPHA), blur = 10.dp, y = 4.dp)
                .clip(CircleShape)
                .background(colors.white.copy(alpha = BELL_FILL_ALPHA))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.BellLinear),
                contentDescription = null,
                tint = colors.white,
                modifier = Modifier.size(20.dp),
            )
        }
        if (unreadCount > 0) {
            CountBadge(
                count = unreadCount,
                size = 18,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp),
            )
        }
    }
}

@Composable
private fun CountBadge(count: Int, size: Int, modifier: Modifier = Modifier, ring: Boolean = false) {
    val colors = HitchyTheme.colors
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(colors.pin)
            .then(if (ring) Modifier.border(2.dp, colors.white, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = count.toString(), style = HitchyTheme.typography.captionStrong, color = colors.white)
    }
}

/** Figma's "Monthly earnings" glass card: r20, white 14% with a white 22% stroke, padding 12/16, gap 4. */
@Composable
private fun EarningsCard(monthName: String, stats: OwnerHomeStats, onOpenDetails: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.white.copy(alpha = CARD_FILL_ALPHA))
            .border(1.dp, colors.white.copy(alpha = CARD_STROKE_ALPHA), shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(Res.string.owner_home_month_earnings, monthName),
            style = typography.body,
            color = colors.white,
        )
        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = groupDigits(stats.monthEarningsEgp),
                    style = typography.display,
                    color = colors.white,
                    modifier = Modifier.alignByBaseline(),
                )
                Text(
                    text = stringResource(Res.string.owner_home_currency),
                    style = typography.bodyBold,
                    color = colors.white,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.white)
                    .clickable(onClick = onOpenDetails)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.owner_home_view_details),
                    style = typography.captionStrong,
                    color = colors.purple,
                )
                Icon(
                    painter = painterResource(HitchyIcons.ChevronRightSmall),
                    contentDescription = null,
                    tint = colors.purple,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Text(
            text = stringResource(Res.string.owner_home_month_seats, stats.monthSeats),
            style = typography.caption,
            color = colors.white,
        )
    }
}

@Composable
private fun OwnerStats(
    points: Int,
    newRequests: Int,
    stats: OwnerHomeStats,
    onOpenRequests: () -> Unit,
    onOpenRides: () -> Unit,
) {
    val colors = HitchyTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(HitchyIcons.Star, points, stringResource(Res.string.owner_home_stat_points), onClick = null)
            // Figma draws this one label in navy; the other three are gray.
            StatCard(
                icon = HitchyIcons.Users20,
                value = newRequests,
                label = stringResource(Res.string.owner_home_stat_requests),
                onClick = onOpenRequests,
                labelColor = colors.navy,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(HitchyIcons.Car, stats.activeRides, stringResource(Res.string.owner_home_stat_rides), onOpenRides)
            StatCard(
                HitchyIcons.Calendar,
                stats.bookedSeats,
                stringResource(Res.string.owner_home_stat_seats),
                onOpenRides,
            )
        }
    }
}

/**
 * Figma's "Stat" card: 174x108, r18, white, two soft shadows, padding 12, gap 10, a 40dp picture,
 * the number in Cairo 800 24/30 and a 12/18 label. Figma's pictures are 3D renders; until they are
 * exported, a lavender tile with the matching icon stands in.
 */
@Composable
private fun RowScope.StatCard(
    icon: DrawableResource,
    value: Int,
    label: String,
    onClick: (() -> Unit)?,
    labelColor: Color = HitchyTheme.colors.textSecondary,
) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier
            .weight(1f)
            .height(108.dp)
            .figmaShadow(StatShape, colors.scrim.copy(alpha = STAT_SHADOW_SOFT), blur = 4.dp, y = 1.dp)
            .figmaShadow(StatShape, colors.scrim.copy(alpha = STAT_SHADOW_ALPHA), blur = 4.dp, y = 1.dp)
            .clip(StatShape)
            .background(colors.white)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(22.dp),
            )
        }
        Column {
            Text(text = value.toString(), style = HitchyTheme.typography.amount, color = colors.navy)
            Text(text = label, style = HitchyTheme.typography.caption, color = labelColor, maxLines = 2)
        }
    }
}

@Composable
private fun OwnerQuickActions(
    newRequests: Int,
    onAddRide: () -> Unit,
    onOpenRequests: () -> Unit,
    onRequestRide: () -> Unit,
    onOpenEarnings: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(Res.string.owner_home_quick_actions),
            style = HitchyTheme.typography.h3,
            color = HitchyTheme.colors.navy,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickAction(
                HitchyIcons.PlusWhite,
                stringResource(Res.string.owner_home_action_add),
                onAddRide,
                primary = true,
            )
            QuickAction(
                icon = HitchyIcons.Users20,
                label = stringResource(Res.string.owner_home_action_requests),
                onClick = onOpenRequests,
                badge = newRequests,
            )
            QuickAction(HitchyIcons.Pin20, stringResource(Res.string.owner_home_action_request_ride), onRequestRide)
            QuickAction(HitchyIcons.Wallet20, stringResource(Res.string.owner_home_action_earnings), onOpenEarnings)
        }
    }
}

/**
 * Figma's "Action" cell: 84x94, padding 4 top and bottom, gap 8, a 60dp r18 tile and a 12/18 label.
 * The first tile is the purple gradient with a purple shadow; the others are lilac.
 */
@Composable
private fun RowScope.QuickAction(
    icon: DrawableResource,
    label: String,
    onClick: () -> Unit,
    primary: Boolean = false,
    badge: Int = 0,
) {
    val colors = HitchyTheme.colors
    val tile = if (primary) {
        Modifier
            .figmaShadow(ActionShape, colors.purple.copy(alpha = ADD_SHADOW_ALPHA), blur = 14.dp, y = 6.dp)
            .clip(ActionShape)
            .background(Brush.linearGradient(listOf(colors.violet, colors.deepPurple)))
    } else {
        Modifier.clip(ActionShape).background(colors.lilac)
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .height(94.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box {
            Box(modifier = Modifier.size(60.dp).then(tile), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = if (primary) colors.white else colors.purple,
                    modifier = Modifier.size(24.dp),
                )
            }
            if (badge > 0) {
                CountBadge(
                    count = badge,
                    size = 20,
                    ring = true,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp),
                )
            }
        }
        Text(
            text = label,
            style = if (primary) HitchyTheme.typography.captionStrong else HitchyTheme.typography.caption,
            color = if (primary) colors.purple else colors.navy,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 1296 → "1,296", as Figma writes the month's earnings. */
internal fun groupDigits(value: Int): String {
    val digits = value.toString()
    if (value < THOUSAND) return digits
    return digits.reversed().chunked(3).joinToString(",").reversed()
}
