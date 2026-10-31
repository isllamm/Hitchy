@file:Suppress("MagicNumber")

package com.hitchy.feature.booking.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.TightTextGap
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tight
import com.hitchy.core.ui.tightLine
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
import hitchy.shared.generated.resources.rider_home_missing_destination_error
import hitchy.shared.generated.resources.rider_home_missing_route_error
import hitchy.shared.generated.resources.rider_home_months
import hitchy.shared.generated.resources.rider_home_notifications
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// Figma frame 390:1157 "home" (393x852): offsets from the screen top.
private val MapHeight = 422.9.dp
private val HeaderTop = 48.dp
private val SheetTop = 322.6.dp
private val ToastTop = 100.dp
private val SheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
private val CardShape = RoundedCornerShape(16.dp)

// One size for every tile and icon in the Plan your ride sheet.
private val HomeTileSize = 32.dp
private val HomeIconSize = 16.dp

/**
 * Rider Home, Figma frame 390:1157, with the validation state of 446:3702: a map with the logo
 * and the bell on top, and a rounded sheet with the trip form. The map is the real one (BUILD_PLAN
 * 5.10) with the chosen points and a straight line; Figma's locate and layers buttons are not built.
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    Box(modifier = modifier.fillMaxSize().background(colors.white)) {
        HitchyMap(
            pins = listOfNotNull(
                state.fromPoint?.let { MapPin(id = "from", point = it.location) },
                state.toPoint?.let { MapPin(id = "to", point = it.location, selected = true) },
            ),
            route = listOfNotNull(state.fromPoint?.location, state.toPoint?.location),
            modifier = Modifier.fillMaxWidth().height(MapHeight).background(colors.mapLand),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = figmaTop(HeaderTop))
                .padding(horizontal = 20.dp)
                .height(44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.img_brand_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(width = 80.dp, height = 32.dp),
            )
            BellButton(unreadCount = state.unreadCount, onClick = onOpenNotifications)
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
            modifier = Modifier.fillMaxSize().padding(top = SheetTop),
        )

        val error = state.validationError
        if (error != null) {
            ErrorToast(
                text = when (error) {
                    RiderSearchValidationError.MISSING_ROUTE -> if (state.toPoint == null) {
                        stringResource(Res.string.rider_home_missing_destination_error)
                    } else {
                        stringResource(Res.string.rider_home_missing_route_error)
                    }

                    RiderSearchValidationError.SAME_ZONE -> stringResource(Res.string.rider_home_same_zone_error)
                },
                onDismiss = { viewModel.onAction(RiderHomeAction.DismissError) },
                modifier = Modifier.padding(top = figmaTop(ToastTop)).padding(horizontal = 20.dp),
            )
        }

        if (sheet != HomeSheet.None) {
            // A window of its own, so the 45% navy scrim and the sheet cover the tab bar as in Figma.
            ModalBottomSheet(
                onDismissRequest = { sheet = HomeSheet.None },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RectangleShape,
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                scrimColor = colors.navy.copy(alpha = SCRIM_ALPHA),
                dragHandle = null,
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            ) {
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
                    )
                }
            }
        }
    }
}

private const val SCRIM_ALPHA = 0.45f

private enum class HomeSheet { None, Date, Time }

/** Figma's 40dp white circle with a 15% shadow, a 20dp bell and an 18dp pink count badge. */
@Composable
private fun BellButton(
    unreadCount: Int,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Box(modifier = Modifier.size(width = 40.dp, height = 44.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(40.dp)
                .figmaShadow(CircleShape, Color.Black.copy(alpha = 0.15f), blur = 10.dp, y = 4.dp)
                .clip(CircleShape)
                .background(colors.white)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.BellLinear),
                contentDescription = stringResource(Res.string.rider_home_notifications),
                tint = colors.navy,
                modifier = Modifier.size(20.dp),
            )
        }
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(colors.pin),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (unreadCount > MAX_BADGE) "$MAX_BADGE+" else unreadCount.toString(),
                    style = typography.caption.tight(),
                    color = colors.white,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private const val MAX_BADGE = 9

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
    val missingDestination = state.validationError == RiderSearchValidationError.MISSING_ROUTE && state.toPoint == null

    Column(
        modifier = modifier
            .figmaShadow(SheetShape, colors.shadowInk.copy(alpha = 0.05f), blur = 24.dp, y = (-8).dp)
            .clip(SheetShape)
            .background(colors.white)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 4.dp)
                .clip(CircleShape)
                .background(colors.handle),
        )
        Text(
            text = stringResource(Res.string.rider_home_plan_title),
            style = typography.h1,
            color = colors.navy,
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RouteCard(
                label = stringResource(Res.string.rider_home_from_label),
                value = state.fromPoint?.name?.resolve(language),
                placeholder = stringResource(Res.string.rider_home_from_placeholder),
                onClick = { onOpenRoutePicker(RiderHomeState.FIELD_FROM) },
                tileColor = colors.lavender,
            ) {
                Box(modifier = Modifier.size(HomeIconSize - 4.dp).clip(CircleShape).background(colors.royal))
            }
            RouteCard(
                label = stringResource(Res.string.rider_home_to_label),
                value = state.toPoint?.name?.resolve(language),
                placeholder = stringResource(Res.string.rider_home_to_placeholder),
                onClick = { onOpenRoutePicker(RiderHomeState.FIELD_TO) },
                tileColor = colors.pinSoft,
                isError = missingDestination,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.PinSmall),
                    contentDescription = null,
                    tint = colors.pin,
                    modifier = Modifier.size(HomeIconSize),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallFieldCard(
                    label = stringResource(Res.string.rider_home_date_label),
                    value = formatDateLabel(state.dateKey),
                    icon = HitchyIcons.CalendarSmall,
                    onClick = onOpenDate,
                    modifier = Modifier.weight(1f),
                )
                SmallFieldCard(
                    label = stringResource(Res.string.rider_home_time_title),
                    value = formatTime12(state.timeMinutes),
                    icon = HitchyIcons.ClockSmall,
                    onClick = onOpenTime,
                    modifier = Modifier.weight(1f),
                )
            }
            PassengersCard(seats = state.seats, onSeatsChange = { onAction(RiderHomeAction.UpdateSeats(it)) })
        }

        SearchButton(
            onClick = { onAction(RiderHomeAction.SubmitSearch) },
            modifier = Modifier.padding(vertical = 10.dp),
        )
    }
}

/** Figma's From and To cards: 353 wide, pad 10/14, r=16, a #E9EEF5 hairline and a 2% shadow. */
@Composable
private fun RouteCard(
    label: String,
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
    tileColor: Color,
    isError: Boolean = false,
    tile: @Composable () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    FormCard(onClick = onClick, isError = isError) {
        Box(
            modifier = Modifier.size(HomeTileSize).clip(RoundedCornerShape(12.dp)).background(tileColor),
            contentAlignment = Alignment.Center,
        ) { tile() }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(text = label, style = typography.body, color = colors.textSecondary)
            if (value != null) {
                Text(text = value, style = typography.button, color = colors.navy, maxLines = 1)
            } else {
                Text(
                    text = placeholder,
                    style = typography.body,
                    color = if (isError) colors.errorText else colors.slate,
                    maxLines = 1,
                )
            }
        }
        Icon(
            painter = painterResource(HitchyIcons.ChevronRightSmall),
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(width = 10.dp, height = 10.5.dp),
        )
    }
}

/** Figma's Date and Departure time cards: half width, pad 10/12, a 28dp r=8 tile. */
@Composable
private fun SmallFieldCard(
    label: String,
    value: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    FormCard(onClick = onClick, modifier = modifier, horizontalPadding = 12.dp) {
        Box(
            modifier = Modifier.size(HomeTileSize).clip(RoundedCornerShape(12.dp)).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.purpleDeep,
                modifier = Modifier.size(HomeIconSize),
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text(text = label, style = typography.body, color = colors.textSecondary, maxLines = 1)
            Text(text = value, style = typography.bodyStrong, color = colors.navy, maxLines = 1)
        }
        Icon(
            painter = painterResource(HitchyIcons.ChevronRightSmall),
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(width = 5.5.dp, height = 9.6.dp),
        )
    }
}

@Composable
private fun FormCard(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    shadow: Boolean = true,
    horizontalPadding: Dp = 14.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = HitchyTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (shadow) {
                    Modifier.figmaShadow(CardShape, Color.Black.copy(alpha = 0.02f), blur = 4.dp, y = 2.dp)
                } else {
                    Modifier
                },
            )
            .clip(CardShape)
            .background(colors.white)
            .border(if (isError) 1.5.dp else 1.dp, if (isError) colors.error else colors.hairline, CardShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = horizontalPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** Figma's Passengers card: no shadow, and the 38dp stepper (white minus, purple plus). */
@Composable
private fun PassengersCard(
    seats: Int,
    onSeatsChange: (Int) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    FormCard(onClick = null, shadow = false) {
        Box(
            modifier = Modifier.size(HomeTileSize).clip(RoundedCornerShape(12.dp)).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.PassengerSmall),
                contentDescription = null,
                tint = colors.purpleDeep,
                modifier = Modifier.size(HomeIconSize),
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            verticalArrangement = Arrangement.spacedBy(TightTextGap),
        ) {
            Text(
                text = stringResource(Res.string.rider_home_passengers_label),
                style = typography.body,
                color = colors.textSecondary,
                modifier = Modifier.tightLine(typography.body.fontSize),
            )
            Text(
                text = if (seats == 1) {
                    stringResource(Res.string.rider_home_passengers_one)
                } else {
                    stringResource(Res.string.rider_home_passengers_many, seats)
                },
                style = typography.bodyStrong,
                color = colors.navy,
                modifier = Modifier.tightLine(typography.bodyStrong.fontSize),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            StepperButton(
                icon = HitchyIcons.Minus,
                filled = false,
                enabled = seats > MIN_SEATS,
                onClick = { onSeatsChange(seats - 1) },
            )
            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = seats.toString(),
                    style = typography.title.tight(),
                    color = colors.navy,
                    textAlign = TextAlign.Center,
                )
            }
            StepperButton(
                icon = HitchyIcons.Plus,
                filled = true,
                enabled = seats < MAX_SEATS,
                onClick = { onSeatsChange(seats + 1) },
            )
        }
    }
}

@Composable
private fun StepperButton(
    icon: DrawableResource,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            // Figma's Stepper (902:13090): the minus turns flat #F3F4F6 when it can't go lower.
            .background(
                when {
                    filled -> colors.purple
                    enabled -> colors.white
                    else -> colors.disabledSurface
                },
            )
            .then(if (filled || !enabled) Modifier else Modifier.border(1.dp, colors.border, CircleShape))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = when {
                filled -> colors.white
                enabled -> colors.navy
                else -> colors.textDisabled
            },
            modifier = Modifier.size(18.dp),
        )
    }
}

private const val MIN_SEATS = 1
private const val MAX_SEATS = 4

/** Figma's 50.5dp gradient button, r=16, with a 14dp magnifier 8dp before the label. */
@Composable
private fun SearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.5.dp)
            .figmaShadow(CardShape, colors.purpleDeep.copy(alpha = 0.10f), blur = 20.dp, y = 6.dp)
            .clip(CardShape)
            .background(colors.brandGradient)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(HitchyIcons.SearchSmall),
            contentDescription = null,
            tint = colors.white,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(Res.string.rider_home_search_button),
            style = typography.button,
            color = colors.white,
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
