@file:Suppress("TooManyFunctions")

package com.hitchy.feature.rides.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hitchy.core.model.Tier
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tight
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.feature.rides.presentation.OwnerHomeState
import com.hitchy.feature.rides.presentation.OwnerHomeStats
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.badge_tier_bronze
import hitchy.shared.generated.resources.badge_tier_gold
import hitchy.shared.generated.resources.badge_tier_silver
import hitchy.shared.generated.resources.fig_oh_add
import hitchy.shared.generated.resources.fig_oh_bell
import hitchy.shared.generated.resources.fig_oh_building
import hitchy.shared.generated.resources.fig_oh_car
import hitchy.shared.generated.resources.fig_oh_chevron_right
import hitchy.shared.generated.resources.fig_oh_earnings
import hitchy.shared.generated.resources.fig_oh_package
import hitchy.shared.generated.resources.fig_oh_request_ride
import hitchy.shared.generated.resources.fig_oh_requests
import hitchy.shared.generated.resources.fig_oh_route
import hitchy.shared.generated.resources.img_stat_calendar
import hitchy.shared.generated.resources.img_stat_car
import hitchy.shared.generated.resources.img_stat_gift
import hitchy.shared.generated.resources.img_stat_users
import hitchy.shared.generated.resources.owner_home_action_add
import hitchy.shared.generated.resources.owner_home_action_earnings
import hitchy.shared.generated.resources.owner_home_action_request_ride
import hitchy.shared.generated.resources.owner_home_action_requests
import hitchy.shared.generated.resources.owner_home_next_ride
import hitchy.shared.generated.resources.owner_home_points_tier
import hitchy.shared.generated.resources.owner_home_pts
import hitchy.shared.generated.resources.owner_home_quick_actions
import hitchy.shared.generated.resources.owner_home_reply_by
import hitchy.shared.generated.resources.owner_home_stat_requests
import hitchy.shared.generated.resources.owner_home_stat_rides
import hitchy.shared.generated.resources.owner_home_stat_seats
import hitchy.shared.generated.resources.owner_one_seat_open
import hitchy.shared.generated.resources.owner_seats_open_count
import hitchy.shared.generated.resources.owner_setup_car
import hitchy.shared.generated.resources.owner_setup_car_sub
import hitchy.shared.generated.resources.owner_setup_commute
import hitchy.shared.generated.resources.owner_setup_commute_sub
import hitchy.shared.generated.resources.owner_setup_done
import hitchy.shared.generated.resources.owner_setup_next
import hitchy.shared.generated.resources.owner_setup_package
import hitchy.shared.generated.resources.owner_setup_package_sub
import hitchy.shared.generated.resources.owner_setup_workplace
import hitchy.shared.generated.resources.owner_setup_workplace_sub
import hitchy.shared.generated.resources.rider_home_date_today
import hitchy.shared.generated.resources.rider_home_date_tomorrow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val StatShape = RoundedCornerShape(18.dp)
private val ActionShape = RoundedCornerShape(18.dp)

/** Figma's shadows: stat cards #0C0C0D at 5% and 10%; the add tile purple at 30%; the bell #3A1B4B at 7%. */
private const val STAT_SHADOW_SOFT = 0.05f
private const val STAT_SHADOW = 0.10f
private const val ADD_SHADOW = 0.30f
private const val BELL_FILL = 0.15f
private const val BELL_SHADOW = 0.07f
private const val CHECKLIST_SHADOW = 0.05f
private const val CHECKLIST_SHADOW_SOFT = 0.04f

/** Figma's bell circle (49.6dp), its 24.8dp icon and the 22.3dp badge. */
private val BellSize = 49.6.dp
private val BellIcon = 24.8.dp
private val BellBadge = 22.3.dp
private val BellBadgeX = 29.7.dp

/**
 * Figma's "Notifications" group (52dp): a white 15% circle with a soft shadow, the linear bell, and the pink
 * count badge on the top end.
 */
@Composable
internal fun HomeBell(unreadCount: Int, onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    Box(modifier = Modifier.size(52.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 2.4.dp)
                .size(BellSize)
                .figmaShadow(CircleShape, colors.navy.copy(alpha = BELL_SHADOW), blur = 4.7.dp, y = 4.7.dp)
                .clip(CircleShape)
                .background(colors.white.copy(alpha = BELL_FILL))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.fig_oh_bell),
                contentDescription = null,
                modifier = Modifier.size(BellIcon),
            )
        }
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = BellBadgeX)
                    .size(BellBadge)
                    .clip(CircleShape)
                    .background(colors.pin),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = unreadCount.toString(),
                    style = HitchyTheme.typography.body.tight(),
                    color = colors.white,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Figma's "Statistics" (two rows, 12dp gaps): points with the tier, new requests with the reply time, booked
 * seats with the open ones, and the owner's rides with the next one (purple).
 */
@Suppress("LongParameterList")
@Composable
internal fun OwnerStats(
    state: OwnerHomeState,
    stats: OwnerHomeStats,
    newRequests: Int,
    todayKey: String,
    tomorrowKey: String,
    onOpenRequests: () -> Unit,
    onOpenRides: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val tier = stringResource(
        when (state.hostTier) {
            Tier.BRONZE -> Res.string.badge_tier_bronze
            Tier.SILVER -> Res.string.badge_tier_silver
            Tier.GOLD -> Res.string.badge_tier_gold
        },
    )
    val next = stats.nextRide?.let { ride ->
        val day = when (isoToCairoDateKey(ride.departAt)) {
            todayKey -> stringResource(Res.string.rider_home_date_today)
            tomorrowKey -> stringResource(Res.string.rider_home_date_tomorrow)
            else -> isoToCairoDateKey(ride.departAt)
        }
        stringResource(Res.string.owner_home_next_ride, day, cairoTime12(ride.departAt))
    }
    // Each row is as tall as its tallest card, so the four cards line up (IntrinsicSize.Min).
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                image = Res.drawable.img_stat_gift,
                value = state.pointsBalance,
                unit = stringResource(Res.string.owner_home_pts),
                sub = stringResource(Res.string.owner_home_points_tier, tier),
                onClick = null,
            )
            StatCard(
                image = Res.drawable.img_stat_users,
                value = newRequests,
                unit = stringResource(Res.string.owner_home_stat_requests),
                sub = state.firstRequestExpiresAt?.let {
                    stringResource(Res.string.owner_home_reply_by, cairoTime12(it))
                },
                onClick = onOpenRequests,
            )
        }
        Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                image = Res.drawable.img_stat_calendar,
                value = stats.bookedSeats,
                unit = stringResource(Res.string.owner_home_stat_seats),
                sub = if (stats.openSeats == 1) {
                    stringResource(Res.string.owner_one_seat_open)
                } else {
                    stringResource(Res.string.owner_seats_open_count, stats.openSeats)
                },
                onClick = onOpenRides,
                subSize = 11,
            )
            StatCard(
                image = Res.drawable.img_stat_car,
                value = stats.activeRides,
                unit = stringResource(Res.string.owner_home_stat_rides),
                sub = next,
                onClick = onOpenRides,
                subColor = colors.purple,
            )
        }
    }
}

/**
 * Figma's stat card: r18, white, two soft shadows, padding 12, every card in a row the same height; a 40dp
 * 3D picture, the number (Cairo 800 24/30) with its Bold 12/18 unit, and a 12/18 line under them.
 */
@Suppress("LongParameterList")
@Composable
private fun RowScope.StatCard(
    image: DrawableResource,
    value: Int,
    unit: String,
    sub: String?,
    onClick: (() -> Unit)?,
    subColor: Color = HitchyTheme.colors.textSecondary,
    subSize: Int = 12,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .heightIn(min = 76.dp)
            .figmaShadow(StatShape, colors.scrim.copy(alpha = STAT_SHADOW_SOFT), blur = 4.dp, y = 1.dp)
            .figmaShadow(StatShape, colors.scrim.copy(alpha = STAT_SHADOW), blur = 4.dp, y = 1.dp)
            .clip(StatShape)
            .background(colors.white)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(painter = painterResource(image), contentDescription = null, modifier = Modifier.size(40.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = value.toString(),
                    style = typography.amount.copy(letterSpacing = (-0.1).sp),
                    color = colors.navy,
                    modifier = Modifier.alignByBaseline(),
                )
                Text(
                    text = unit,
                    style = typography.captionStrong,
                    color = colors.navy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            if (sub != null) {
                Text(
                    text = sub,
                    style = typography.caption.copy(fontSize = subSize.sp).tight(),
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Figma's "Quick actions": the Bold 20/28 title and four 84x94 cells, 8dp apart. */
@Composable
internal fun OwnerQuickActions(
    newRequests: Int,
    onAddRide: () -> Unit,
    onOpenRequests: () -> Unit,
    onRequestRide: () -> Unit,
    onOpenEarnings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(Res.string.owner_home_quick_actions),
            style = HitchyTheme.typography.h3,
            color = HitchyTheme.colors.navy,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickAction(
                Res.drawable.fig_oh_add,
                stringResource(Res.string.owner_home_action_add),
                onAddRide,
                primary = true,
            )
            QuickAction(
                Res.drawable.fig_oh_requests,
                stringResource(Res.string.owner_home_action_requests),
                onOpenRequests,
                badge = newRequests,
            )
            QuickAction(
                Res.drawable.fig_oh_request_ride,
                stringResource(Res.string.owner_home_action_request_ride),
                onRequestRide,
            )
            QuickAction(
                Res.drawable.fig_oh_earnings,
                stringResource(Res.string.owner_home_action_earnings),
                onOpenEarnings,
            )
        }
    }
}

/** Figma's action cell: padding 4, gap 8, a 60dp r18 tile (the first a purple gradient with a shadow) and a label. */
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
            .figmaShadow(ActionShape, colors.purple.copy(alpha = ADD_SHADOW), blur = 14.dp, y = 6.dp)
            .clip(ActionShape)
            .background(Brush.horizontalGradient(listOf(colors.violet, colors.deepPurple)))
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
                Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
            }
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(colors.pin)
                        .border(2.dp, colors.white, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = badge.toString(), style = HitchyTheme.typography.captionStrong, color = colors.white)
                }
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

/** One step of the new owner's checklist: done, next (the first open one) or later. */
private enum class StepState { Done, Next, Later }

/**
 * Figma's "Setup checklist" (832:14231): white, r24, two soft shadows, padding 16, four 40dp rows split by
 * rules. Your car is done once the owner has one; the workplace once a company is set; the daily commute
 * opens Offer a ride; price and package has no screen yet.
 */
@Composable
internal fun SetupChecklist(vehicleName: String?, hasWorkplace: Boolean, onOpenPostRide: () -> Unit) {
    val colors = HitchyTheme.colors
    val shape = RoundedCornerShape(24.dp)
    val done = listOf(vehicleName != null, hasWorkplace, false, false)
    val nextIndex = done.indexOfFirst { !it }
    fun stateOf(i: Int) = when {
        done[i] -> StepState.Done
        i == nextIndex -> StepState.Next
        else -> StepState.Later
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .figmaShadow(shape, colors.navy.copy(alpha = CHECKLIST_SHADOW), blur = 24.dp, y = 8.dp)
            .figmaShadow(shape, colors.navy.copy(alpha = CHECKLIST_SHADOW_SOFT), blur = 2.dp, y = 1.dp)
            .clip(shape)
            .background(colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ChecklistRow(
            Res.drawable.fig_oh_car,
            stringResource(Res.string.owner_setup_car),
            vehicleName?.let { stringResource(Res.string.owner_setup_car_sub, it) }.orEmpty(),
            stateOf(0),
            onClick = null,
        )
        Rule()
        ChecklistRow(
            Res.drawable.fig_oh_building,
            stringResource(Res.string.owner_setup_workplace),
            stringResource(Res.string.owner_setup_workplace_sub),
            stateOf(1),
            onClick = null,
        )
        Rule()
        ChecklistRow(
            Res.drawable.fig_oh_route,
            stringResource(Res.string.owner_setup_commute),
            stringResource(Res.string.owner_setup_commute_sub),
            stateOf(2),
            onClick = onOpenPostRide,
        )
        Rule()
        ChecklistRow(
            Res.drawable.fig_oh_package,
            stringResource(Res.string.owner_setup_package),
            stringResource(Res.string.owner_setup_package_sub),
            stateOf(3),
            onClick = null,
        )
    }
}

@Composable
private fun Rule() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(HitchyTheme.colors.border))
}

/**
 * A checklist row: a 36dp circle (green when done, purple when next, lilac after), Bold 14/22 over 12/18,
 * the pill and a chevron.
 */
@Composable
private fun ChecklistRow(
    icon: DrawableResource,
    title: String,
    sub: String,
    step: StepState,
    onClick: (() -> Unit)?,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val circle = when (step) {
        StepState.Done -> colors.successBg
        StepState.Next -> colors.purple
        StepState.Later -> colors.lilac
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(circle),
            contentAlignment = Alignment.Center,
        ) {
            Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = typography.bodyBold, color = colors.navy, maxLines = 1)
            Text(text = sub, style = typography.caption, color = colors.textSecondary, maxLines = 1)
        }
        when (step) {
            StepState.Done -> StepPill(
                stringResource(Res.string.owner_setup_done),
                colors.successBg,
                colors.successText,
            )

            StepState.Next -> StepPill(stringResource(Res.string.owner_setup_next), colors.purple, colors.white)

            StepState.Later -> Unit
        }
        Image(
            painter = painterResource(Res.drawable.fig_oh_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun StepPill(text: String, background: Color, content: Color) {
    Box(
        modifier = Modifier
            .height(26.dp)
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = HitchyTheme.typography.captionStrong, color = content)
    }
}
