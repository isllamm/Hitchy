@file:Suppress("MagicNumber")

package com.hitchy.feature.inbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyColors
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.dateKey
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.core.util.isoToEpochMs
import com.hitchy.feature.inbox.domain.InboxItem
import com.hitchy.feature.inbox.presentation.InboxAction
import com.hitchy.feature.inbox.presentation.InboxEvent
import com.hitchy.feature.inbox.presentation.InboxViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.action_back
import hitchy.shared.generated.resources.inbox_empty_body
import hitchy.shared.generated.resources.inbox_empty_title
import hitchy.shared.generated.resources.inbox_hours_ago
import hitchy.shared.generated.resources.inbox_just_now
import hitchy.shared.generated.resources.inbox_mark_all_read
import hitchy.shared.generated.resources.inbox_minutes_ago
import hitchy.shared.generated.resources.inbox_new
import hitchy.shared.generated.resources.inbox_tab_account
import hitchy.shared.generated.resources.inbox_tab_all
import hitchy.shared.generated.resources.inbox_tab_rides
import hitchy.shared.generated.resources.inbox_title
import hitchy.shared.generated.resources.notif_design_earlier
import hitchy.shared.generated.resources.notif_design_short_date
import hitchy.shared.generated.resources.notif_design_today
import hitchy.shared.generated.resources.rider_home_months
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

private const val DATE_MONTH_START = 5
private const val DATE_MONTH_END = 7
private const val DATE_DAY_START = 8

/** Tile look for one notification type: background, icon and icon color. */
private data class TileLook(val icon: DrawableResource, val background: Color, val tint: Color)

/**
 * Figma frame 398:6013 "Notifications": a 60dp header (back, title, Mark all read), the tab list and
 * full-width rows. The Messages tab is left out: there is no chat in the demo.
 */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenDeepLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    clock: Clock = koinInject(),
    viewModel: InboxViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val currentLanguage: AppLanguage = languageSwitcher.getCurrentLanguage()
    val nowMs = clock.now().toEpochMilliseconds()
    var tab by remember { mutableStateOf(InboxTab.All) }
    val shown = state.items.filter { tab == InboxTab.All || it.tab() == tab }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is InboxEvent.OpenDeepLink -> onOpenDeepLink(event.deepLink)
            }
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = colors.white) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            NotificationsHeader(
                onBack = onBack,
                showMarkAll = state.unreadIds.isNotEmpty(),
                onMarkAll = { viewModel.onAction(InboxAction.MarkAllRead) },
            )
            InboxTabs(selected = tab, onSelect = { tab = it })
            if (shown.isEmpty() && !state.isLoading) {
                EmptyState(
                    title = stringResource(Res.string.inbox_empty_title),
                    message = stringResource(Res.string.inbox_empty_body),
                    icon = HitchyIcons.Bell,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(shown, key = { it.id }) { entry ->
                        NotificationDesignRow(
                            item = entry,
                            language = currentLanguage,
                            nowMs = nowMs,
                            onClick = { viewModel.onAction(InboxAction.TapNotification(entry)) },
                        )
                    }
                }
            }
        }
    }
}

private enum class InboxTab { All, Rides, Account }

private fun InboxItem.tab(): InboxTab = when {
    type.startsWith("booking") || type.startsWith("ride") || type.startsWith("payment") ||
        type.startsWith("request") || type.startsWith("rate") || type == "low_balance" -> InboxTab.Rides

    else -> InboxTab.Account
}

/** Figma's header: a 44dp plain back button, the 18/26 title centred, "Mark all read" in Bold 13/16 purple. */
@Composable
private fun NotificationsHeader(
    onBack: () -> Unit,
    showMarkAll: Boolean,
    onMarkAll: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Box(modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 4.dp)) {
        Box(
            modifier = Modifier.align(Alignment.CenterStart).size(44.dp).clip(CircleShape).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.Back22),
                contentDescription = stringResource(Res.string.action_back),
                tint = colors.navy,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = stringResource(Res.string.inbox_title),
            style = typography.title,
            color = colors.navy,
            modifier = Modifier.align(Alignment.Center),
        )
        if (showMarkAll) {
            Text(
                text = stringResource(Res.string.inbox_mark_all_read),
                style = typography.bodyBold.copy(fontSize = 13.sp, lineHeight = 16.sp),
                color = colors.purple,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).clickable(onClick = onMarkAll),
            )
        }
    }
}

/** Figma's `Tab List`: 44dp tabs, SemiBold 13/18; the open one purple with a 1dp underline, on a 1dp rule. */
@Composable
private fun InboxTabs(
    selected: InboxTab,
    onSelect: (InboxTab) -> Unit,
) {
    val colors = HitchyTheme.colors
    val style = HitchyTheme.typography.bodyStrong.copy(fontSize = 13.sp, lineHeight = 18.sp)
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp)) {
            InboxTab.entries.forEach { tab ->
                val active = tab == selected
                Column(
                    modifier = Modifier.weight(1f).height(44.dp).clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(
                                when (tab) {
                                    InboxTab.All -> Res.string.inbox_tab_all
                                    InboxTab.Rides -> Res.string.inbox_tab_rides
                                    InboxTab.Account -> Res.string.inbox_tab_account
                                },
                            ),
                            style = style,
                            color = if (active) colors.purple else colors.textSecondary,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (active) colors.purple else Color.Transparent),
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

/**
 * Figma's row: full width, pad 16/20, unread on #F5F3FF; a 40dp r=12 tile, a Bold 14/20 title with
 * "New", a 12/18 body, a 10/15 #9CA3AF time and an 18dp chevron, over a 1dp rule.
 */
@Composable
private fun NotificationDesignRow(
    item: InboxItem,
    language: AppLanguage,
    nowMs: Long,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val look = tileLook(item.type, colors)
    val unread = item.isUnread
    val small = typography.caption.copy(fontSize = 10.sp, lineHeight = 15.sp)

    Column(modifier = Modifier.fillMaxWidth().background(if (unread) colors.lilac else colors.white)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(
                horizontal = 20.dp,
                vertical = 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(look.background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(look.icon),
                    contentDescription = null,
                    tint = look.tint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.title.resolve(language),
                        style = typography.bodyBold.copy(lineHeight = 20.sp),
                        color = colors.navy,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    if (unread) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(colors.purple))
                            Text(
                                text = stringResource(Res.string.inbox_new),
                                style = small.copy(fontWeight = typography.bodyBold.fontWeight),
                                color = colors.purple,
                            )
                        }
                    }
                }
                Text(
                    text = item.body.resolve(language),
                    style = typography.caption,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = relativeTime(item.createdAt, nowMs),
                    style = small,
                    color = colors.textDisabled,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
            Icon(
                painter = painterResource(HitchyIcons.Chevron18),
                contentDescription = null,
                tint = colors.navy,
                modifier = Modifier.size(18.dp),
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

/** "Just now", "2 min ago", "1 hr ago", or "Oct 22", as Figma writes the time. */
@Composable
private fun relativeTime(
    createdAt: String,
    nowMs: Long,
): String {
    val minutes = ((nowMs - isoToEpochMs(createdAt)) / MS_PER_MINUTE).coerceAtLeast(0)
    return when {
        minutes < 1 -> stringResource(Res.string.inbox_just_now)

        minutes < MINUTES_PER_HOUR -> stringResource(Res.string.inbox_minutes_ago, minutes.toInt())

        minutes < MINUTES_PER_DAY -> stringResource(Res.string.inbox_hours_ago, (minutes / MINUTES_PER_HOUR).toInt())

        else -> {
            val key = isoToCairoDateKey(createdAt)
            val months = stringArrayResource(Res.array.rider_home_months)
            val month = key.substring(DATE_MONTH_START, DATE_MONTH_END).toIntOrNull() ?: return key
            val day = key.substring(DATE_DAY_START).toIntOrNull() ?: return key
            stringResource(Res.string.notif_design_short_date, months[month - 1], day)
        }
    }
}

private const val MS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val MINUTES_PER_DAY = 1_440L

/** One icon tile look per notification type; unknown types get the bell. */
private fun tileLook(
    type: String,
    colors: HitchyColors,
): TileLook = when (type) {
    "booking_accepted", "payment_confirmed", "package_confirmed", "host_approved", "verified_granted" ->
        TileLook(HitchyIcons.Success, colors.successBg, colors.success)

    "booking_declined", "request_expired", "booking_cancelled", "ride_cancelled", "host_rejected",
    "package_rejected", "fee_disputed", "sos",
    -> TileLook(HitchyIcons.Error, colors.errorBg, colors.error)

    "rate_trip", "rate_riders" -> TileLook(HitchyIcons.Star, colors.warningBg, colors.warning)

    "ride_reminder", "ride_started" -> TileLook(HitchyIcons.Clock, colors.lavender, colors.purple)

    "account_ready", "verify_request" -> TileLook(HitchyIcons.ShieldCheck, colors.lavender, colors.purple)

    "fee_created", "fee_paid", "fee_resolved", "low_balance" -> TileLook(HitchyIcons.Info, colors.infoBg, colors.info)

    else -> TileLook(HitchyIcons.Bell, colors.lavender, colors.purple)
}
