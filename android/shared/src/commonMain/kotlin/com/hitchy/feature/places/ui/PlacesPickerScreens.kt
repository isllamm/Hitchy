package com.hitchy.feature.places.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.SearchField
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.places.domain.Point
import com.hitchy.feature.places.presentation.PlacesPickerAction
import com.hitchy.feature.places.presentation.PlacesPickerEvent
import com.hitchy.feature.places.presentation.PlacesPickerViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.picker_choose_on_map
import hitchy.shared.generated.resources.picker_confirm_point
import hitchy.shared.generated.resources.picker_map_subtitle
import hitchy.shared.generated.resources.picker_map_title
import hitchy.shared.generated.resources.picker_select_dropoff_title
import hitchy.shared.generated.resources.picker_select_pickup_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

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
    val spacing = HitchyTheme.spacing
    val currentLanguage: AppLanguage = languageSwitcher.getCurrentLanguage()
    val zonesById = state.zones.associateBy { it.id }

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

    val title = if (field.endsWith("to")) {
        stringResource(Res.string.picker_select_dropoff_title)
    } else {
        stringResource(Res.string.picker_select_pickup_title)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = title,
                onBackClick = onBack,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                SearchField(
                    query = state.query,
                    onQueryChange = { viewModel.onAction(PlacesPickerAction.UpdateQuery(it)) },
                )

                HitchyButton(
                    onClick = { onOpenMapPicker(field) },
                    variant = HitchyButtonVariant.Tonal,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.picker_choose_on_map))
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    items(state.filteredPoints, key = { it.id }) { point ->
                        val zoneName = zonesById[point.zoneId]?.name?.resolve(currentLanguage).orEmpty()
                        PointRow(
                            point = point,
                            zoneName = zoneName,
                            language = currentLanguage,
                            selected = state.selectedPoint?.id == point.id,
                            onClick = { viewModel.onAction(PlacesPickerAction.ConfirmPoint(point)) },
                        )
                    }
                }
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
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    val currentLanguage: AppLanguage = languageSwitcher.getCurrentLanguage()
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

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = stringResource(Res.string.picker_map_title),
                onBackClick = onBack,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                // Stylized interactive pilot map preview card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.lavender)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(spacing.space16),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.space8),
                    ) {
                        Icon(
                            painter = painterResource(HitchyIcons.Pin),
                            contentDescription = null,
                            tint = colors.pin,
                            modifier = Modifier.size(32.dp),
                        )
                        val selected = state.selectedPoint
                        if (selected != null) {
                            Text(
                                text = selected.name.resolve(currentLanguage),
                                style = typography.h3,
                                color = colors.navy,
                            )
                        }
                        Text(
                            text = stringResource(Res.string.picker_map_subtitle),
                            style = typography.caption,
                            color = colors.textSecondary,
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    items(state.points, key = { it.id }) { point ->
                        val zoneName = zonesById[point.zoneId]?.name?.resolve(currentLanguage).orEmpty()
                        PointRow(
                            point = point,
                            zoneName = zoneName,
                            language = currentLanguage,
                            selected = state.selectedPoint?.id == point.id,
                            onClick = { viewModel.onAction(PlacesPickerAction.HighlightPoint(point)) },
                        )
                    }
                }

                HitchyButton(
                    onClick = {
                        val selected = state.selectedPoint
                        if (selected != null) {
                            viewModel.onAction(PlacesPickerAction.ConfirmPoint(selected))
                        }
                    },
                    enabled = state.selectedPoint != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.picker_confirm_point))
                }
            }
        }
    }
}

@Composable
private fun PointRow(
    point: Point,
    zoneName: String,
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val spacing = HitchyTheme.spacing
    val shape = RoundedCornerShape(12.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.purple else colors.border, shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (selected) colors.lilac else colors.white,
    ) {
        Row(
            modifier = Modifier.padding(spacing.space16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space12),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.lavender else colors.background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Pin),
                    contentDescription = null,
                    tint = if (selected) colors.pin else colors.navy,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = point.name.resolve(language),
                    style = typography.bodyStrong,
                    color = colors.navy,
                )
                if (zoneName.isNotEmpty()) {
                    Text(
                        text = zoneName,
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
            }
        }
    }
}
