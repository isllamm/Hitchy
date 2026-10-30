@file:Suppress("MagicNumber")

package com.hitchy.feature.booking.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.PassengersStepper
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.booking.presentation.RiderHomeAction
import com.hitchy.feature.booking.presentation.RiderHomeEvent
import com.hitchy.feature.booking.presentation.RiderHomeState
import com.hitchy.feature.booking.presentation.RiderHomeViewModel
import com.hitchy.feature.booking.presentation.RiderSearchValidationError
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.img_brand_logo
import hitchy.shared.generated.resources.rider_home_date_format
import hitchy.shared.generated.resources.rider_home_date_label
import hitchy.shared.generated.resources.rider_home_from_label
import hitchy.shared.generated.resources.rider_home_from_placeholder
import hitchy.shared.generated.resources.rider_home_missing_route_error
import hitchy.shared.generated.resources.rider_home_months
import hitchy.shared.generated.resources.rider_home_passengers_label
import hitchy.shared.generated.resources.rider_home_passengers_many
import hitchy.shared.generated.resources.rider_home_passengers_one
import hitchy.shared.generated.resources.rider_home_plan_title
import hitchy.shared.generated.resources.rider_home_same_zone_error
import hitchy.shared.generated.resources.rider_home_search_button
import hitchy.shared.generated.resources.rider_home_time_title
import hitchy.shared.generated.resources.rider_home_to_label
import hitchy.shared.generated.resources.rider_home_to_placeholder
import hitchy.shared.generated.resources.rider_home_weekdays
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val MapHeight = 330.dp
private val SheetTopOffset = 280.dp
private val SheetRadius = 28.dp
private val RowRadius = 20.dp
private val IconTileSize = 40.dp

/**
 * Rider Home built to the demo design: a full-width map with the logo and the bell on top, and a rounded
 * sheet with the trip form below. The map shows the chosen pickup and drop-off points and a straight line
 * between them (BUILD_PLAN 5.10); the design's locate and layers buttons are not built.
 */
@Composable
fun RiderHomeContent(
    onOpenNotifications: () -> Unit,
    onOpenRoutePicker: (String) -> Unit,
    onNavigateToSearchResults: (RiderHomeEvent.NavigateToSearchResults) -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    viewModel: RiderHomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val language = languageSwitcher.getCurrentLanguage()

    var sheet by remember { mutableStateOf(HomeSheet.None) }
    var pendingDate by remember { mutableStateOf(state.dateKey) }
    var pendingMinutes by remember { mutableStateOf(state.timeMinutes) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is RiderHomeEvent.NavigateToSearchResults -> onNavigateToSearchResults(event)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        HitchyMap(
            pins = listOfNotNull(
                state.fromPoint?.let { MapPin(id = "from", point = it.location) },
                state.toPoint?.let { MapPin(id = "to", point = it.location, selected = true) },
            ),
            route = listOfNotNull(state.fromPoint?.location, state.toPoint?.location),
            modifier = Modifier.fillMaxWidth().height(MapHeight),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.img_brand_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(48.dp),
            )
            BellButton(hasUnread = state.hasUnreadNotifications, onClick = onOpenNotifications)
        }

        PlanSheet(
            state = state,
            language = language,
            onOpenRoutePicker = onOpenRoutePicker,
            onAction = viewModel::onAction,
            onOpenDate = {
                pendingDate = state.dateKey
                sheet = HomeSheet.Date
            },
            onOpenTime = {
                pendingMinutes = state.timeMinutes
                sheet = HomeSheet.Time
            },
            modifier = Modifier.fillMaxSize().padding(top = SheetTopOffset),
        )

        if (sheet != HomeSheet.None) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.navy.copy(alpha = 0.5f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        sheet = HomeSheet.None
                    },
            )
            if (sheet == HomeSheet.Date) {
                DateSheet(
                    todayKey = state.todayDateKey,
                    tomorrowKey = state.tomorrowDateKey,
                    selectedKey = pendingDate,
                    onPick = { pendingDate = it },
                    onConfirm = {
                        viewModel.onAction(RiderHomeAction.SelectDate(pendingDate))
                        sheet = HomeSheet.None
                    },
                    onClose = { sheet = HomeSheet.None },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            } else {
                TimeSheet(
                    dateKey = state.dateKey,
                    selectedMinutes = pendingMinutes,
                    isToday = state.dateKey == state.todayDateKey,
                    onChange = { pendingMinutes = it },
                    onConfirm = {
                        viewModel.onAction(RiderHomeAction.SelectTime(pendingMinutes))
                        sheet = HomeSheet.None
                    },
                    onClose = { sheet = HomeSheet.None },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

private enum class HomeSheet { None, Date, Time }

@Composable
private fun BellButton(
    hasUnread: Boolean,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(colors.white)
            .clickable(onClick = onClick),
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
private fun PlanSheet(
    state: RiderHomeState,
    language: AppLanguage,
    onOpenRoutePicker: (String) -> Unit,
    onAction: (RiderHomeAction) -> Unit,
    onOpenDate: () -> Unit,
    onOpenTime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius))
            .background(colors.white)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 4.dp)
                .clip(CircleShape)
                .background(colors.border),
        )
        Text(
            text = stringResource(Res.string.rider_home_plan_title),
            style = typography.h2,
            color = colors.navy,
        )

        PlanRow(
            label = stringResource(Res.string.rider_home_from_label),
            value = state.fromPoint?.name?.resolve(language) ?: stringResource(Res.string.rider_home_from_placeholder),
            tileColor = colors.lavender,
            tileDotColor = colors.deepPurple,
            onClick = { onOpenRoutePicker(RiderHomeState.FIELD_FROM) },
        )
        PlanRow(
            label = stringResource(Res.string.rider_home_to_label),
            value = state.toPoint?.name?.resolve(language) ?: stringResource(Res.string.rider_home_to_placeholder),
            tileColor = colors.errorBg,
            tileIcon = HitchyIcons.Pin,
            tileIconColor = colors.pin,
            onClick = { onOpenRoutePicker(RiderHomeState.FIELD_TO) },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlanRow(
                label = stringResource(Res.string.rider_home_date_label),
                value = formatDateLabel(state.dateKey),
                tileColor = colors.lavender,
                tileIcon = HitchyIcons.Calendar,
                tileIconColor = colors.purple,
                onClick = onOpenDate,
                compact = true,
                modifier = Modifier.weight(1f),
            )
            PlanRow(
                label = stringResource(Res.string.rider_home_time_title),
                value = formatTime12(state.timeMinutes),
                tileColor = colors.lavender,
                tileIcon = HitchyIcons.Clock,
                tileIconColor = colors.purple,
                onClick = onOpenTime,
                compact = true,
                modifier = Modifier.weight(1f),
            )
        }
        PassengersRow(seats = state.seats, onSeatsChange = { onAction(RiderHomeAction.UpdateSeats(it)) })

        val error = state.validationError
        if (error != null) {
            val text = when (error) {
                RiderSearchValidationError.MISSING_ROUTE -> stringResource(Res.string.rider_home_missing_route_error)
                RiderSearchValidationError.SAME_ZONE -> stringResource(Res.string.rider_home_same_zone_error)
            }
            HitchyBanner(
                text = text,
                variant = BannerVariant.Error,
                onDismiss = { onAction(RiderHomeAction.DismissError) },
            )
        }

        SearchButton(onClick = { onAction(RiderHomeAction.SubmitSearch) })
    }
}

@Composable
private fun PlanRow(
    label: String,
    value: String,
    tileColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tileDotColor: Color? = null,
    tileIcon: org.jetbrains.compose.resources.DrawableResource? = null,
    tileIconColor: Color = Color.Unspecified,
    compact: Boolean = false,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(RowRadius))
            .clip(RoundedCornerShape(RowRadius))
            .clickable(onClick = onClick),
        color = colors.white,
        shape = RoundedCornerShape(RowRadius),
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 10.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 32.dp else IconTileSize)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tileColor),
                contentAlignment = Alignment.Center,
            ) {
                if (tileDotColor != null) {
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(tileDotColor))
                } else if (tileIcon != null) {
                    Icon(
                        painter = painterResource(tileIcon),
                        contentDescription = null,
                        tint = tileIconColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = typography.caption, color = colors.textSecondary)
                Text(text = value, style = typography.bodyStrong, color = colors.navy, maxLines = 1)
            }
            if (!compact) {
                Icon(
                    painter = painterResource(HitchyIcons.ChevronDown),
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp).rotate(-90f),
                )
            }
        }
    }
}

@Composable
private fun PassengersRow(
    seats: Int,
    onSeatsChange: (Int) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(RowRadius)),
        color = colors.white,
        shape = RoundedCornerShape(RowRadius),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(IconTileSize).clip(RoundedCornerShape(12.dp)).background(colors.lavender),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Person),
                    contentDescription = null,
                    tint = colors.purple,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.rider_home_passengers_label),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
                Text(
                    text = if (seats == 1) {
                        stringResource(Res.string.rider_home_passengers_one)
                    } else {
                        stringResource(Res.string.rider_home_passengers_many, seats)
                    },
                    style = typography.bodyStrong,
                    color = colors.navy,
                )
            }
            PassengersStepper(count = seats, onCountChange = onSeatsChange)
        }
    }
}

@Composable
private fun SearchButton(onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.brandGradient)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(HitchyIcons.Search),
            contentDescription = null,
            tint = colors.white,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = stringResource(Res.string.rider_home_search_button),
            style = typography.bodyStrong,
            color = colors.white,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/** "Wed, Oct 28" from a "YYYY-MM-DD" key (Cairo date). The day and month names come from string resources. */
@Composable
private fun formatDateLabel(dateKey: String): String {
    val weekdays = stringArrayResource(Res.array.rider_home_weekdays)
    val months = stringArrayResource(Res.array.rider_home_months)
    val date = runCatching { kotlinx.datetime.LocalDate.parse(dateKey) }.getOrNull() ?: return dateKey
    val month = dateKey.substring(MONTH_START, MONTH_END).toInt()
    val day = dateKey.substring(DAY_START).toInt()
    return stringResource(
        Res.string.rider_home_date_format,
        weekdays[date.dayOfWeek.ordinal],
        months[month - 1],
        day,
    )
}

private const val MONTH_START = 5
private const val MONTH_END = 7
private const val DAY_START = 8
