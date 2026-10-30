package com.hitchy.feature.places.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.figmaBottom
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.places.domain.Point
import com.hitchy.feature.places.presentation.PlacesPickerAction
import com.hitchy.feature.places.presentation.PlacesPickerEvent
import com.hitchy.feature.places.presentation.PlacesPickerState
import com.hitchy.feature.places.presentation.PlacesPickerViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.picker_choose_on_map
import hitchy.shared.generated.resources.picker_clear
import hitchy.shared.generated.resources.picker_confirm_destination
import hitchy.shared.generated.resources.picker_confirm_pickup
import hitchy.shared.generated.resources.picker_from_label
import hitchy.shared.generated.resources.picker_from_placeholder
import hitchy.shared.generated.resources.picker_map_hint_destination
import hitchy.shared.generated.resources.picker_map_hint_pickup
import hitchy.shared.generated.resources.picker_map_subtitle
import hitchy.shared.generated.resources.picker_map_title_destination
import hitchy.shared.generated.resources.picker_map_title_pickup
import hitchy.shared.generated.resources.picker_pickup_points
import hitchy.shared.generated.resources.picker_results_for
import hitchy.shared.generated.resources.picker_route_title
import hitchy.shared.generated.resources.picker_suggested_destinations
import hitchy.shared.generated.resources.picker_swap
import hitchy.shared.generated.resources.picker_to_label
import hitchy.shared.generated.resources.picker_to_placeholder
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Figma frames 452:4365 to 452:4496 "Route · …": the route card with both fields (the one being
 * picked has a purple stroke and the search caret), the swap button, "Choose on map", and the
 * demo pickup points. Figma's current location, saved places and recents have no data in the
 * demo and are not shown.
 */
@Composable
fun RoutePickerScreen(
    field: String,
    onBack: () -> Unit,
    onOpenMapPicker: (String) -> Unit,
    onPointSelected: () -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    viewModel: PlacesPickerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val language: AppLanguage = languageSwitcher.getCurrentLanguage()
    val zonesById = state.zones.associateBy { it.id }
    val pickingFrom = state.pair?.let { state.isFromActive } ?: !state.field.endsWith("to")

    LaunchedEffect(field) {
        viewModel.onAction(PlacesPickerAction.Init(field))
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PlacesPickerEvent.PointConfirmed -> onPointSelected()
            }
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = colors.white) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = figmaTop(CONTENT_TOP), bottom = 24.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                    BackButton(
                        onClick = onBack,
                        style = BackButtonStyle.OutlinedCircle,
                        modifier = Modifier.size(44.dp),
                    )
                    Text(
                        text = stringResource(Res.string.picker_route_title),
                        style = typography.title,
                        color = colors.navy,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    Box(modifier = Modifier.size(44.dp))
                }
            }
            item {
                RouteFieldsCard(
                    state = state,
                    language = language,
                    pickingFrom = pickingFrom,
                    onQueryChange = { viewModel.onAction(PlacesPickerAction.UpdateQuery(it)) },
                    onFieldTap = { viewModel.onAction(PlacesPickerAction.SwitchField(it)) },
                    onSwap = { viewModel.onAction(PlacesPickerAction.Swap) },
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            item {
                LocationRow(
                    icon = HitchyIcons.MapFold,
                    title = stringResource(Res.string.picker_choose_on_map),
                    subtitle = stringResource(
                        if (pickingFrom) Res.string.picker_map_hint_pickup else Res.string.picker_map_hint_destination,
                    ),
                    onClick = { onOpenMapPicker(state.field) },
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            item {
                Box(modifier = Modifier.padding(top = 16.dp).fillMaxWidth().height(1.dp).background(colors.border))
            }
            item {
                val header = when {
                    state.query.isNotBlank() -> stringResource(Res.string.picker_results_for, state.query.trim())
                    pickingFrom -> stringResource(Res.string.picker_pickup_points)
                    else -> stringResource(Res.string.picker_suggested_destinations)
                }
                Text(
                    text = header.uppercase(),
                    style = typography.captionStrong,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            // Point ids repeat across zones ("p1" in each), so the key carries the zone too.
            items(state.filteredPoints, key = { "${it.zoneId}/${it.id}" }) { point ->
                LocationRow(
                    icon = HitchyIcons.Place,
                    title = point.name.resolve(language),
                    subtitle = zonesById[point.zoneId]?.name?.resolve(language).orEmpty(),
                    onClick = { viewModel.onAction(PlacesPickerAction.ConfirmPoint(point, moveToEmptyField = true)) },
                )
            }
        }
    }
}

private val CONTENT_TOP = 60.dp
private val FieldShape = RoundedCornerShape(12.dp)
private val CardShape = RoundedCornerShape(16.dp)

/** Figma's `Route Fields`: 361 wide, pad 8, r=16, a 1dp stroke and a 6% navy shadow; fields 4dp apart. */
@Composable
private fun RouteFieldsCard(
    state: PlacesPickerState,
    language: AppLanguage,
    pickingFrom: Boolean,
    onQueryChange: (String) -> Unit,
    onFieldTap: (String) -> Unit,
    onSwap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val pair = state.pair
    Row(
        modifier = modifier
            .fillMaxWidth()
            .figmaShadow(CardShape, colors.navy.copy(alpha = 0.06f), blur = 16.dp, y = 4.dp)
            .clip(CardShape)
            .background(colors.white)
            .border(1.dp, colors.border, CardShape)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (pair == null || pickingFrom) {
                RouteField(
                    isFrom = pickingFrom,
                    active = true,
                    query = state.query,
                    value = null,
                    onQueryChange = onQueryChange,
                    onTap = {},
                )
            } else {
                RouteField(
                    isFrom = true,
                    active = false,
                    query = "",
                    value = state.fromPoint?.name?.resolve(language),
                    onQueryChange = {},
                    onTap = { onFieldTap(pair.first) },
                )
            }
            if (pair != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(10.dp).padding(start = 27.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(width = 2.dp, height = 10.dp).background(colors.lavender))
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(colors.border))
                }
                if (pickingFrom) {
                    RouteField(
                        isFrom = false,
                        active = false,
                        query = "",
                        value = state.toPoint?.name?.resolve(language),
                        onQueryChange = {},
                        onTap = { onFieldTap(pair.second) },
                    )
                } else {
                    RouteField(
                        isFrom = false,
                        active = true,
                        query = state.query,
                        value = null,
                        onQueryChange = onQueryChange,
                        onTap = {},
                    )
                }
            }
        }
        if (pair != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.white)
                    .border(1.dp, colors.border, CircleShape)
                    .clickable(onClick = onSwap),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Swap20),
                    contentDescription = stringResource(Res.string.picker_swap),
                    tint = colors.purple,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/** One field: a 40dp r=12 tile, a 12/18 label and the value; the active one is outlined and editable. */
@Composable
private fun RouteField(
    isFrom: Boolean,
    active: Boolean,
    query: String,
    value: String?,
    onQueryChange: (String) -> Unit,
    onTap: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val focus = remember { FocusRequester() }
    if (active) LaunchedEffect(isFrom) { focus.requestFocus() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .then(
                if (active) {
                    Modifier
                        .border(1.5.dp, colors.purple, FieldShape)
                } else {
                    Modifier
                        .clickable(onClick = onTap)
                },
            )
            .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(FieldShape)
                .background(if (isFrom) colors.lavender else colors.pinTint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(if (isFrom) HitchyIcons.Pickup else HitchyIcons.Destination),
                contentDescription = null,
                tint = if (isFrom) colors.purple else colors.pin,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(if (isFrom) Res.string.picker_from_label else Res.string.picker_to_label),
                style = typography.caption,
                color = colors.textSecondary,
            )
            val placeholder = stringResource(
                if (isFrom) Res.string.picker_from_placeholder else Res.string.picker_to_placeholder,
            )
            if (active) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = typography.bodyBold.copy(color = colors.navy),
                    cursorBrush = SolidColor(colors.purple),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = typography.body,
                                    color = colors.textDisabled,
                                )
                            }
                            inner()
                        }
                    },
                )
            } else {
                Text(
                    text = value ?: placeholder,
                    style = if (value != null) typography.bodyBold else typography.body,
                    color = if (value != null) colors.navy else colors.textDisabled,
                    maxLines = 1,
                )
            }
        }
        if (active && query.isNotEmpty()) {
            Image(
                painter = painterResource(HitchyIcons.ClearField),
                contentDescription = stringResource(Res.string.picker_clear),
                modifier = Modifier.size(18.dp).clip(CircleShape).clickable { onQueryChange("") },
            )
        }
    }
}

/** Figma's `Location Row` (component 452:4358): 64dp, a 40dp r=12 lavender tile, Bold title, grey subtitle. */
@Composable
private fun LocationRow(
    icon: DrawableResource,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick)
            .padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(FieldShape).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = typography.bodyBold, color = colors.navy, maxLines = 1)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, style = typography.caption, color = colors.textSecondary, maxLines = 1)
            }
        }
    }
}

@Composable
fun MapPointPickerScreen(
    field: String,
    onBack: () -> Unit,
    onPointConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    viewModel: PlacesPickerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val language: AppLanguage = languageSwitcher.getCurrentLanguage()
    val zonesById = state.zones.associateBy { it.id }

    LaunchedEffect(field) {
        viewModel.onAction(PlacesPickerAction.Init(field))
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PlacesPickerEvent.PointConfirmed -> onPointConfirmed()
            }
        }
    }

    val selected = state.selectedPoint
    val pointsById = state.points.associateBy { it.id }

    Box(modifier = modifier.fillMaxSize().background(colors.mapLand)) {
        HitchyMap(
            pins = state.points.map { MapPin(id = it.id, point = it.location, selected = it.id == selected?.id) },
            onPinClick = { id -> pointsById[id]?.let { viewModel.onAction(PlacesPickerAction.HighlightPoint(it)) } },
            modifier = Modifier.fillMaxSize(),
        )
        BackButton(
            onClick = onBack,
            style = BackButtonStyle.OutlinedCircle,
            modifier = Modifier.padding(start = 16.dp, top = figmaTop(CONTENT_TOP)).size(44.dp),
        )
        PointSheet(
            selected = selected,
            zoneName = zonesById[selected?.zoneId]?.name?.resolve(language).orEmpty(),
            language = language,
            pickingFrom = !state.field.endsWith("to"),
            onConfirm = { selected?.let { viewModel.onAction(PlacesPickerAction.ConfirmPoint(it)) } },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** Figma's "Destination · Choose on map" sheet (726:11468): r=24, pad 12/16/24/16, 16dp gaps. */
@Composable
private fun PointSheet(
    selected: Point?,
    zoneName: String,
    language: AppLanguage,
    pickingFrom: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(colors.white)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = figmaBottom(24.dp)),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.border),
        )
        Text(
            text = stringResource(
                if (pickingFrom) Res.string.picker_map_title_pickup else Res.string.picker_map_title_destination,
            ),
            style = typography.h3,
            color = colors.navy,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(CardShape)
                .border(1.dp, colors.lavender, CardShape)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(FieldShape)
                    .background(if (pickingFrom) colors.lavender else colors.pinTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (pickingFrom) HitchyIcons.Pickup else HitchyIcons.Destination),
                    contentDescription = null,
                    tint = if (pickingFrom) colors.purple else colors.pin,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selected?.name?.resolve(language) ?: stringResource(Res.string.picker_map_subtitle),
                    style = typography.bodyBold,
                    color = colors.navy,
                    maxLines = 1,
                )
                if (selected != null && zoneName.isNotEmpty()) {
                    Text(text = zoneName, style = typography.caption, color = colors.textSecondary, maxLines = 1)
                }
            }
            Icon(
                painter = painterResource(HitchyIcons.ChevronRight20),
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        HitchyButton(onClick = onConfirm, enabled = selected != null, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(
                    if (pickingFrom) Res.string.picker_confirm_pickup else Res.string.picker_confirm_destination,
                ),
            )
        }
    }
}
