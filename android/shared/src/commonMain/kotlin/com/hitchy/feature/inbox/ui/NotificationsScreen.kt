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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.ui.cairoTime12
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyColors
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.dateKey
import com.hitchy.core.util.isoToCairoDateKey
import com.hitchy.feature.inbox.domain.InboxItem
import com.hitchy.feature.inbox.presentation.InboxAction
import com.hitchy.feature.inbox.presentation.InboxEvent
import com.hitchy.feature.inbox.presentation.InboxViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.inbox_empty_body
import hitchy.shared.generated.resources.inbox_empty_title
import hitchy.shared.generated.resources.inbox_mark_all_read
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
 * Notifications built to the design: a round back button with the title, then a Today and an Earlier group.
 * Unread items sit on a lilac block with a purple dot; each type gets its own icon tile.
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
    val todayKey = dateKey(clock.now())

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is InboxEvent.OpenDeepLink -> onOpenDeepLink(event.deepLink)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.white,
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            NotificationsHeader(onBack = onBack)

            if (state.items.isEmpty() && !state.isLoading) {
                EmptyState(
                    title = stringResource(Res.string.inbox_empty_title),
                    message = stringResource(Res.string.inbox_empty_body),
                    icon = HitchyIcons.Bell,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                val (today, earlier) = state.items.partition { isoToCairoDateKey(it.createdAt) == todayKey }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    notificationSection(
                        title = Res.string.notif_design_today,
                        entries = today,
                        showMarkAll = state.unreadIds.isNotEmpty(),
                        language = currentLanguage,
                        isToday = true,
                        onMarkAll = { viewModel.onAction(InboxAction.MarkAllRead) },
                        onTap = { viewModel.onAction(InboxAction.TapNotification(it)) },
                    )
                    notificationSection(
                        title = Res.string.notif_design_earlier,
                        entries = earlier,
                        showMarkAll = state.unreadIds.isNotEmpty() && today.isEmpty(),
                        language = currentLanguage,
                        isToday = false,
                        onMarkAll = { viewModel.onAction(InboxAction.MarkAllRead) },
                        onTap = { viewModel.onAction(InboxAction.TapNotification(it)) },
                    )
                }
            }
        }
    }
}

@Suppress("LongParameterList")
private fun LazyListScope.notificationSection(
    title: StringResource,
    entries: List<InboxItem>,
    showMarkAll: Boolean,
    language: AppLanguage,
    isToday: Boolean,
    onMarkAll: () -> Unit,
    onTap: (InboxItem) -> Unit,
) {
    if (entries.isEmpty()) return
    item(key = "header_$isToday") {
        SectionHeader(title = stringResource(title), showMarkAll = showMarkAll, onMarkAll = onMarkAll)
    }
    items(entries, key = { it.id }) { entry ->
        NotificationDesignRow(item = entry, language = language, isToday = isToday, onClick = { onTap(entry) })
    }
}

@Composable
private fun NotificationsHeader(onBack: () -> Unit) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        BackButton(
            onClick = onBack,
            style = BackButtonStyle.OutlinedCircle,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        Text(
            text = stringResource(Res.string.inbox_title),
            style = HitchyTheme.typography.h3,
            color = colors.navy,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    showMarkAll: Boolean,
    onMarkAll: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title.uppercase(), style = typography.captionStrong, color = colors.textSecondary)
        if (showMarkAll) {
            Text(
                text = stringResource(Res.string.inbox_mark_all_read),
                style = typography.captionStrong,
                color = colors.purple,
                modifier = Modifier.clickable(onClick = onMarkAll),
            )
        }
    }
}

@Composable
private fun NotificationDesignRow(
    item: InboxItem,
    language: AppLanguage,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val look = tileLook(item.type, colors)
    val unread = item.isUnread

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (unread) colors.lilac else colors.white)
                .clickable(onClick = onClick)
                .padding(12.dp),
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
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.title.resolve(language),
                        style = typography.bodyStrong,
                        color = colors.navy,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = notificationTime(item.createdAt, isToday),
                        style = typography.caption,
                        color = if (unread) colors.purple else colors.textSecondary,
                    )
                    if (unread) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.purple))
                    }
                }
                Text(text = item.body.resolve(language), style = typography.body, color = colors.textSecondary)
            }
        }
        if (!unread) {
            HorizontalDivider(color = colors.border, thickness = 1.dp)
        }
    }
}

/** The time of day for today's items, "Oct 22" for older ones. */
@Composable
private fun notificationTime(
    createdAt: String,
    isToday: Boolean,
): String {
    if (isToday) return cairoTime12(createdAt)
    val key = isoToCairoDateKey(createdAt)
    if (key.length < DATE_DAY_START + 1) return key
    val months = stringArrayResource(Res.array.rider_home_months)
    val month = key.substring(DATE_MONTH_START, DATE_MONTH_END).toIntOrNull() ?: return key
    val day = key.substring(DATE_DAY_START).toIntOrNull() ?: return key
    return stringResource(Res.string.notif_design_short_date, months[month - 1], day)
}

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
