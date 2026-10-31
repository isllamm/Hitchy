@file:Suppress("MagicNumber")

package com.hitchy.feature.inbox.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.components.Skeleton
import com.hitchy.core.ui.components.SkeletonLine
import com.hitchy.core.ui.components.SkeletonListRow
import com.hitchy.core.ui.figmaTop
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
import hitchy.shared.generated.resources.fig_notif_accepted
import hitchy.shared.generated.resources.fig_notif_account
import hitchy.shared.generated.resources.fig_notif_declined
import hitchy.shared.generated.resources.fig_notif_rate
import hitchy.shared.generated.resources.fig_notif_receipt
import hitchy.shared.generated.resources.fig_notif_reminder
import hitchy.shared.generated.resources.fig_notif_request
import hitchy.shared.generated.resources.inbox_earlier
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
import hitchy.shared.generated.resources.inbox_today
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

/** Tile look for one notification type: its Figma icon (in its own colour) and the tile background. */
private data class TileLook(val icon: DrawableResource, val background: Color)

private val GroupShape = RoundedCornerShape(16.dp)
private val CardShape = RoundedCornerShape(4.dp)

/**
 * The demo's Notifications (676:10912): the 44dp header (back, the 18/26 title), "TODAY" with "Mark all as
 * read", the unread ones as lilac cards inside a lilac block, the read ones as plain ruled rows, then
 * "EARLIER" for older ones.
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
    val now = clock.now()
    val nowMs = now.toEpochMilliseconds()
    val todayKey = dateKey(now)
    val (today, earlier) = state.items.partition { isoToCairoDateKey(it.createdAt) == todayKey }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is InboxEvent.OpenDeepLink -> onOpenDeepLink(event.deepLink)
            }
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = colors.white) {
        Column(modifier = Modifier.fillMaxSize().padding(top = figmaTop(60.dp))) {
            NotificationsHeader(onBack = onBack)
            if (state.items.isEmpty() && state.isLoading) {
                Skeleton(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
                    SkeletonLine(width = 64.dp, modifier = Modifier.padding(bottom = 4.dp))
                    repeat(SKELETON_ROWS) { SkeletonListRow(leading = 44.dp, titleWidth = 220.dp) }
                }
                return@Column
            }
            if (state.items.isEmpty() && !state.isLoading) {
                EmptyState(
                    title = stringResource(Res.string.inbox_empty_title),
                    message = stringResource(Res.string.inbox_empty_body),
                    icon = HitchyIcons.Bell,
                    modifier = Modifier.fillMaxSize(),
                )
                return@Column
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val sections = listOf(Res.string.inbox_today to today, Res.string.inbox_earlier to earlier)
                sections.forEachIndexed { index, (title, entries) ->
                    if (entries.isEmpty()) return@forEachIndexed
                    item(key = "title_$index") {
                        SectionTitle(
                            title = stringResource(title),
                            showMarkAll = state.unreadIds.isNotEmpty() && (index == 0 || today.isEmpty()),
                            onMarkAll = { viewModel.onAction(InboxAction.MarkAllRead) },
                        )
                    }
                    val (unread, read) = entries.partition { it.isUnread }
                    if (unread.isNotEmpty()) {
                        item(key = "unread_$index") {
                            Column(
                                modifier = Modifier.fillMaxWidth().clip(GroupShape).background(colors.lilac),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                unread.forEach { entry ->
                                    NotificationCard(entry, currentLanguage, nowMs) {
                                        viewModel.onAction(InboxAction.TapNotification(entry))
                                    }
                                }
                            }
                        }
                    }
                    items(read, key = { it.id }) { entry ->
                        NotificationCard(entry, currentLanguage, nowMs) {
                            viewModel.onAction(InboxAction.TapNotification(entry))
                        }
                    }
                }
            }
        }
    }
}

/** Figma's header: the 44dp outlined back button, the 18/26 Bold title centred, a 44dp spacer. */
@Composable
private fun NotificationsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onClick = onBack, style = BackButtonStyle.OutlinedCircle, modifier = Modifier.size(44.dp))
        Text(
            text = stringResource(Res.string.inbox_title),
            style = HitchyTheme.typography.title,
            color = HitchyTheme.colors.navy,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Box(Modifier.size(44.dp))
    }
}

/** "TODAY" / "EARLIER" in Bold 12/18 gray, with "Mark all as read" in Bold 12/18 purple on the end side. */
@Composable
private fun SectionTitle(title: String, showMarkAll: Boolean, onMarkAll: () -> Unit) {
    val typography = HitchyTheme.typography
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = typography.captionStrong,
            color = HitchyTheme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        if (showMarkAll) {
            Text(
                text = stringResource(Res.string.inbox_mark_all_read),
                style = typography.captionStrong,
                color = HitchyTheme.colors.purple,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onMarkAll),
            )
        }
    }
}

/**
 * Figma's notification: a 40dp r12 tile with the type's icon, the Bold 14/22 title, the time (purple 12/18 with
 * an 8dp dot when unread, #9CA3AF when read) and the 14/22 gray message. Unread: a lilac card with a lavender
 * stroke, padding 12. Read: a white row with a #E5E7EB stroke, padding 12/4.
 */
@Composable
private fun NotificationCard(item: InboxItem, language: AppLanguage, nowMs: Long, onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val look = tileLook(item.type, colors)
    val unread = item.isUnread
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (unread) {
                    Modifier.clip(CardShape).background(colors.lilac).border(1.dp, colors.lavender, CardShape)
                } else {
                    Modifier.background(colors.white).border(1.dp, colors.border)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = if (unread) 12.dp else 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(look.background),
            contentAlignment = Alignment.Center,
        ) {
            Image(painter = painterResource(look.icon), contentDescription = null, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.title.resolve(language),
                    style = typography.bodyBold,
                    color = colors.navy,
                    modifier = Modifier.weight(1f),
                    // The backend's messages can be one long title with no body, so allow a second line.
                    maxLines = 2,
                )
                Text(
                    text = relativeTime(item.createdAt, nowMs),
                    style = typography.caption,
                    color = if (unread) colors.purple else colors.textDisabled,
                )
                if (unread) Box(Modifier.size(8.dp).clip(CircleShape).background(colors.purple))
            }
            val body = item.body.resolve(language)
            if (body.isNotBlank()) Text(text = body, style = typography.body, color = colors.textSecondary)
        }
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

/** One icon tile per notification type, with the demo frame's icons; unknown types get the request bell. */
private fun tileLook(
    type: String,
    colors: HitchyColors,
): TileLook = when (type) {
    "booking_accepted", "payment_confirmed", "package_confirmed", "host_approved", "verified_granted" ->
        TileLook(Res.drawable.fig_notif_accepted, colors.successBg)

    "booking_declined", "request_expired", "booking_cancelled", "ride_cancelled", "host_rejected",
    "package_rejected", "fee_disputed", "sos",
    -> TileLook(Res.drawable.fig_notif_declined, colors.errorBg)

    "rate_trip", "rate_riders" -> TileLook(Res.drawable.fig_notif_rate, colors.warningBg)

    "ride_reminder", "ride_started" -> TileLook(Res.drawable.fig_notif_reminder, colors.lavender)

    "account_ready", "verify_request" -> TileLook(Res.drawable.fig_notif_account, colors.lavender)

    "fee_created", "fee_paid", "fee_resolved", "low_balance" -> TileLook(Res.drawable.fig_notif_receipt, colors.infoBg)

    else -> TileLook(Res.drawable.fig_notif_request, colors.lavender)
}

private const val SKELETON_ROWS = 6
