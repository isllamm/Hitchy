@file:Suppress("TooManyFunctions")

package com.hitchy.feature.rides.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.RideStatus
import com.hitchy.core.ui.WhiteStatusBarIcons
import com.hitchy.core.ui.components.AvatarImage
import com.hitchy.core.ui.figmaLinearGradient
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tight
import com.hitchy.core.ui.tightLine
import com.hitchy.core.util.dateKey
import com.hitchy.feature.rides.presentation.OwnerHomeAction
import com.hitchy.feature.rides.presentation.OwnerHomeState
import com.hitchy.feature.rides.presentation.OwnerHomeStats
import com.hitchy.feature.rides.presentation.OwnerHomeViewModel
import com.hitchy.feature.rides.presentation.ownerHomeStats
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fig_oh_chevron
import hitchy.shared.generated.resources.fig_oh_gift
import hitchy.shared.generated.resources.fig_oh_plan_chevron
import hitchy.shared.generated.resources.img_home_coins
import hitchy.shared.generated.resources.owner_home_currency
import hitchy.shared.generated.resources.owner_home_days_left
import hitchy.shared.generated.resources.owner_home_free_month
import hitchy.shared.generated.resources.owner_home_free_until
import hitchy.shared.generated.resources.owner_home_fuel_body
import hitchy.shared.generated.resources.owner_home_fuel_covered
import hitchy.shared.generated.resources.owner_home_fuel_title
import hitchy.shared.generated.resources.owner_home_months_full
import hitchy.shared.generated.resources.owner_home_seat_payments
import hitchy.shared.generated.resources.owner_home_setup_body
import hitchy.shared.generated.resources.owner_home_view_earnings
import hitchy.shared.generated.resources.owner_home_welcome
import hitchy.shared.generated.resources.owner_home_welcome_name
import hitchy.shared.generated.resources.rider_home_months
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

// Figma DEMO 813:13739 (an active owner), 832:14288 (the same with no numbers yet) and 832:14156 (a new
// owner with no ride: the welcome and the setup checklist).
private val HeaderHeight = 444.dp
private val HeaderShape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
private val CardShape = RoundedCornerShape(20.dp)

/** Figma's header gradient: violet, purple at 56%, then deep purple fading out at 88%; handles in fractions. */
private const val STOP_PURPLE = 0.5625f
private const val STOP_FADE = 0.8808f
private const val GRAD_START_X = 0.4506f
private const val GRAD_START_Y = -0.0664f
private const val GRAD_END_X = 0.7045f
private const val GRAD_END_Y = 1.1063f
private const val GRAD_BAND_X = 0.0243f
private const val GRAD_BAND_Y = -0.0735f

/** Figma's glass card: white at 14% with a white 22% stroke; the progress track is white at 49%. */
private const val CARD_FILL_ALPHA = 0.14f
private const val CARD_STROKE_ALPHA = 0.22f
private const val TRACK_ALPHA = 0.49f
private const val AVATAR_RING_ALPHA = 0.5f
private const val PERCENT = 100f

private const val MONTH_START = 5
private const val MONTH_END = 7
private const val DAY_START = 8
private const val THOUSAND = 1000
private const val DAY_SECONDS = 86_400L
private const val PLACEHOLDER = "\u0000"

/**
 * Owner Home, Figma DEMO 813:13739: the gradient header with the owner, the bell and this month's seat
 * payments, the free-month card, four stat cards and the quick actions. A new owner with no ride sees
 * 832:14156 instead: the welcome and the setup checklist. The tab bar's "+" (BottomNav) opens Offer a ride.
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
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnerHomeViewModel = koinViewModel(),
    clock: Clock = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WhiteStatusBarIcons()
    val now = clock.now()
    val monthKey = dateKey(now).take(MONTH_END)
    val stats = ownerHomeStats(state.upcomingRides, monthKey, state.pendingSeatsByRide)
    val requestsRideId = state.pendingRequestsCountByRide.entries.firstOrNull { it.value > 0 }?.key
    val newRequests = state.pendingRequestsCountByRide.values.sum()
    val openRequests = { requestsRideId?.let(onOpenHostRideDetails) ?: onOpenRidesTab() }
    val openRides = {
        state.upcomingRides.firstOrNull { it.status == RideStatus.STARTED }?.let { onOpenHostTrip(it.id) }
            ?: onOpenRidesTab()
    }
    val isNewOwner = state.isNewOwner

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HitchyTheme.colors.white)
            .verticalScroll(rememberScrollState()),
    ) {
        HeaderBackground()
        if (!state.isReady) {
            // The rides and cars have not answered yet: the skeleton, never the new-owner setup screen.
            OwnerHomeSkeleton(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = figmaTop(56.dp)))
            return@Box
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = figmaTop(if (isNewOwner) 68.dp else 56.dp), bottom = 24.dp),
        ) {
            if (isNewOwner) {
                WelcomeTopBar(state, onOpenNotifications, onOpenProfile)
                Box(Modifier.height(24.dp))
            } else {
                OwnerTopBar(state, onOpenNotifications, onOpenProfile)
                Box(Modifier.height(16.dp))
            }
            if (stats.monthEarningsEgp == 0) {
                FuelMoneyCard(onClick = onOpenPostRide)
            } else {
                EarningsCard(monthKey = monthKey, stats = stats, onOpenEarnings = onOpenEarnings)
            }
            Box(Modifier.height(if (isNewOwner) 24.dp else 20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                state.freeUntil?.let { PlanCard(freeUntilIso = it, now = now) }
                if (isNewOwner) {
                    SetupChecklist(
                        vehicleName = state.vehicleName,
                        hasWorkplace = state.hasWorkplace,
                        onOpenPostRide = onOpenPostRide,
                    )
                } else {
                    OwnerStats(
                        state = state,
                        stats = stats,
                        newRequests = newRequests,
                        todayKey = dateKey(now),
                        tomorrowKey = dateKey(now + 1.days),
                        onOpenRequests = openRequests,
                        onOpenRides = openRides,
                    )
                }
            }
            if (!isNewOwner) {
                Box(Modifier.height(24.dp))
                OwnerQuickActions(
                    newRequests = newRequests,
                    onAddRide = onOpenPostRide,
                    onOpenRequests = openRequests,
                    onRequestRide = { viewModel.onAction(OwnerHomeAction.SwitchToRider) },
                    onOpenEarnings = onOpenEarnings,
                )
            }
        }
    }
}

/** Figma's 444dp header: the brand gradient fading out at the bottom, with 16dp bottom corners. */
@Composable
private fun HeaderBackground() {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .clip(HeaderShape)
            .drawBehind {
                drawRect(
                    figmaLinearGradient(
                        colorStops = arrayOf(
                            0f to colors.violet,
                            STOP_PURPLE to colors.purple,
                            STOP_FADE to colors.deepPurple.copy(alpha = 0f),
                        ),
                        start = Offset(GRAD_START_X, GRAD_START_Y),
                        end = Offset(GRAD_END_X, GRAD_END_Y),
                        band = Offset(GRAD_BAND_X, GRAD_BAND_Y),
                        size = size,
                    ),
                )
            },
    )
}

/**
 * Figma's top bar: the avatar with its top on the bell's top, "Welcome back," over the name in a block as tall
 * as the avatar, and the bell.
 */
@Composable
private fun OwnerTopBar(state: OwnerHomeState, onOpenNotifications: () -> Unit, onOpenProfile: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    HeaderRow(state, onOpenNotifications, onOpenProfile) {
        Text(
            text = stringResource(Res.string.owner_home_welcome),
            style = typography.body,
            color = colors.white,
            modifier = Modifier.tightLine(typography.body.fontSize),
        )
        Text(
            text = fullName(state),
            style = typography.h3,
            color = colors.white,
            maxLines = 1,
            modifier = Modifier.tightLine(typography.h3.fontSize),
        )
    }
}

/** Figma 832:14186: "Welcome, Omar" over the three-steps line, in the same header row. */
@Composable
private fun WelcomeTopBar(state: OwnerHomeState, onOpenNotifications: () -> Unit, onOpenProfile: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    HeaderRow(state, onOpenNotifications, onOpenProfile) {
        Text(
            text = stringResource(Res.string.owner_home_welcome_name, state.firstName),
            style = typography.h3,
            color = colors.white,
            maxLines = 1,
            modifier = Modifier.tightLine(typography.h3.fontSize),
        )
        Text(
            text = stringResource(Res.string.owner_home_setup_body),
            style = typography.caption,
            color = colors.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.tightLine(typography.caption.fontSize),
        )
    }
}

/** The avatar and the bell share one top line; the two text lines sit centred in the avatar's height. */
@Composable
private fun HeaderRow(
    state: OwnerHomeState,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    lines: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = BELL_TOP)) { OwnerAvatar(state, onOpenProfile) }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp, top = BELL_TOP)
                .heightIn(min = AvatarSize),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        ) { lines() }
        HomeBell(unreadCount = state.unreadCount, onClick = onOpenNotifications)
    }
}

// The bell's circle starts 2.4dp into its 52dp box (HomeBell) and is 49.6dp wide.
private val BELL_TOP = 2.4.dp
private val AvatarSize = 49.6.dp

@Composable
private fun OwnerAvatar(state: OwnerHomeState, onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .size(AvatarSize)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(colors.white)
            .border(2.dp, colors.white.copy(alpha = AVATAR_RING_ALPHA), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AvatarImage(
            photoPath = state.photoPath,
            initials = "${state.firstName.take(1)}${state.lastName.take(1)}".uppercase(),
            textStyle = HitchyTheme.typography.bodyBold,
            textColor = colors.purple,
        )
    }
}

private fun fullName(state: OwnerHomeState): String {
    val parts = listOf(state.firstName, state.lastName)
    return parts.filter { it.isNotBlank() }.joinToString(" ")
}

// Figma's earnings card is 361x120; the empty-month card takes the same space so nothing below moves.
private val EarningsCardHeight = 120.dp

/**
 * Shown instead of the earnings card while this month's earnings are 0 (design from Islam, 2026-10-31): the
 * earnings card's glass background with "Turn your empty seats into fuel money" (Bold 20/28), the 14/22 line and
 * the coins picture on the end side. A tap opens Add ride.
 */
@Composable
private fun FuelMoneyCard(onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .height(EarningsCardHeight)
            .background(colors.white.copy(alpha = CARD_FILL_ALPHA))
            .border(1.dp, colors.white.copy(alpha = CARD_STROKE_ALPHA), CardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(Res.string.owner_home_fuel_title),
                style = HitchyTheme.typography.h3.tight(),
                color = colors.white,
                maxLines = 2,
            )
            Text(
                text = stringResource(Res.string.owner_home_fuel_body),
                style = HitchyTheme.typography.caption.tight(),
                color = colors.white,
                maxLines = 2,
            )
        }
        Image(
            painter = painterResource(Res.drawable.img_home_coins),
            contentDescription = null,
            modifier = Modifier.size(width = 89.dp, height = 87.dp),
        )
    }
}

/**
 * Figma's "Monthly earnings" glass card (361x120, r20, padding 12/16, gap 4): "October · seat payments" with
 * "View earnings ›", the amount (Bold 32/40) and EGP, a 6dp bar of the trip cost covered, and the summary.
 */
@Composable
private fun EarningsCard(monthKey: String, stats: OwnerHomeStats, onOpenEarnings: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val months = stringArrayResource(Res.array.owner_home_months_full)
    val monthName = months.getOrElse(monthKey.substring(MONTH_START).toInt() - 1) { "" }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .heightIn(min = EarningsCardHeight)
            .background(colors.white.copy(alpha = CARD_FILL_ALPHA))
            .border(1.dp, colors.white.copy(alpha = CARD_STROKE_ALPHA), CardShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.owner_home_seat_payments, monthName),
                style = typography.caption,
                color = colors.white,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onOpenEarnings),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.owner_home_view_earnings),
                    style = typography.captionStrong,
                    color = colors.white,
                )
                Image(
                    painter = painterResource(Res.drawable.fig_oh_chevron),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Row(modifier = Modifier.heightIn(min = 40.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(colors.white.copy(alpha = TRACK_ALPHA)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(stats.coveredPercent / PERCENT)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(colors.white),
            )
        }
        Text(
            text = stringResource(
                Res.string.owner_home_fuel_covered,
                "${stats.coveredPercent}%",
                stats.monthSeats,
                stats.monthTrips,
            ),
            style = typography.caption,
            color = colors.white,
        )
    }
}

/**
 * Figma's "Current plan" card (353x62, lilac, r16, padding 8/16): the gift, "Free month" (Bold 18/26),
 * "N days left ›" (600 14/22 purple), and "Trips are free until **30 Nov**" (12/18 gray, the date Bold).
 */
@Composable
private fun PlanCard(freeUntilIso: String, now: Instant) {
    val until = runCatching { Instant.parse(freeUntilIso) }.getOrNull() ?: return
    if (until <= now) return
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val daysLeft = ((until.epochSeconds - now.epochSeconds + DAY_SECONDS - 1) / DAY_SECONDS).toInt()
    val key = dateKey(until)
    val shortMonths = stringArrayResource(Res.array.rider_home_months)
    val month = shortMonths.getOrElse(key.substring(MONTH_START, MONTH_END).toInt() - 1) { "" }
    val dateText = "${key.substring(DAY_START).toInt()} $month"
    val template = stringResource(Res.string.owner_home_free_until, PLACEHOLDER)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.lilac)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Image(
                painter = painterResource(Res.drawable.fig_oh_gift),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(Res.string.owner_home_free_month),
                style = typography.title,
                color = colors.navy,
                modifier = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(Res.string.owner_home_days_left, daysLeft),
                    style = typography.bodyStrong,
                    color = colors.purple,
                )
                Image(
                    painter = painterResource(Res.drawable.fig_oh_plan_chevron),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Text(
            text = buildAnnotatedString {
                val at = template.indexOf(PLACEHOLDER)
                append(template.substring(0, at.coerceAtLeast(0)))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(dateText) }
                if (at >= 0) append(template.substring(at + PLACEHOLDER.length))
            },
            style = typography.caption,
            color = colors.textSecondary,
        )
    }
}

/** 1296 → "1,296", as Figma writes the month's earnings. */
internal fun groupDigits(value: Int): String {
    val digits = value.toString()
    if (value < THOUSAND) return digits
    return digits.reversed().chunked(3).joinToString(",").reversed()
}
