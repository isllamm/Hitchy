package com.hitchy.feature.debug.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hitchy.core.model.PricingConfig
import com.hitchy.feature.debug.presentation.DebugAction
import com.hitchy.feature.debug.presentation.DebugState
import com.hitchy.feature.debug.presentation.DebugViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.action_back
import hitchy.shared.generated.resources.debug_car_consumption
import hitchy.shared.generated.resources.debug_component_gallery
import hitchy.shared.generated.resources.debug_disabled
import hitchy.shared.generated.resources.debug_fuel_price
import hitchy.shared.generated.resources.debug_label_app_check
import hitchy.shared.generated.resources.debug_label_emulator
import hitchy.shared.generated.resources.debug_label_env
import hitchy.shared.generated.resources.debug_label_fcm
import hitchy.shared.generated.resources.debug_label_region
import hitchy.shared.generated.resources.debug_label_remote_config
import hitchy.shared.generated.resources.debug_label_uid
import hitchy.shared.generated.resources.debug_label_version
import hitchy.shared.generated.resources.debug_margin_cap
import hitchy.shared.generated.resources.debug_max_seats
import hitchy.shared.generated.resources.debug_not_signed_in
import hitchy.shared.generated.resources.debug_pricing_loading
import hitchy.shared.generated.resources.debug_pricing_sign_in_required
import hitchy.shared.generated.resources.debug_refresh
import hitchy.shared.generated.resources.debug_running_cost
import hitchy.shared.generated.resources.debug_scooter_consumption
import hitchy.shared.generated.resources.debug_section_build
import hitchy.shared.generated.resources.debug_section_firebase
import hitchy.shared.generated.resources.debug_section_pricing
import hitchy.shared.generated.resources.debug_sign_in_anon
import hitchy.shared.generated.resources.debug_sign_in_required
import hitchy.shared.generated.resources.debug_sign_out
import hitchy.shared.generated.resources.debug_test_crash
import hitchy.shared.generated.resources.debug_title
import hitchy.shared.generated.resources.debug_traffic_factors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToGallery: () -> Unit = {},
    viewModel: DebugViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.debug_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(text = "←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.onAction(DebugAction.Refresh) },
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                    ) {
                        Text(text = stringResource(Res.string.debug_refresh))
                    }
                },
            )
        },
    ) { innerPadding ->
        DebugContent(
            state = state,
            onAction = viewModel::onAction,
            onNavigateToGallery = onNavigateToGallery,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun DebugContent(
    state: DebugState,
    onAction: (DebugAction) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToGallery: () -> Unit = {},
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Section 1: Build & Environment
        DebugCard(title = stringResource(Res.string.debug_section_build)) {
            DebugRow(label = stringResource(Res.string.debug_label_env), value = state.env)
            DebugRow(label = stringResource(Res.string.debug_label_region), value = state.functionsRegion)
            DebugRow(label = stringResource(Res.string.debug_label_emulator), value = state.useEmulator.toString())
            DebugRow(label = stringResource(Res.string.debug_label_version), value = state.versionName)
            DebugRow(
                label = stringResource(Res.string.debug_label_uid),
                value = state.uid ?: stringResource(Res.string.debug_not_signed_in),
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.uid == null) {
                if (state.useEmulator) {
                    Button(
                        onClick = { onAction(DebugAction.SignInAnonymously) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(Res.string.debug_sign_in_anon))
                    }
                } else {
                    Text(
                        text = stringResource(Res.string.debug_sign_in_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { onAction(DebugAction.SignOut) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.debug_sign_out))
                }
            }
        }

        // Section 2: Firebase Services
        DebugCard(title = stringResource(Res.string.debug_section_firebase)) {
            DebugRow(label = stringResource(Res.string.debug_label_fcm), value = state.fcmToken ?: "Loading...")
            DebugRow(label = stringResource(Res.string.debug_label_app_check), value = state.appCheckStatus)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.debug_label_remote_config),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (state.remoteConfigValues.isEmpty()) {
                Text(text = "Loading...", style = MaterialTheme.typography.bodySmall)
            } else {
                state.remoteConfigValues.forEach { (key, value) ->
                    DebugRow(label = "  $key", value = value)
                }
            }
        }

        // Section 3: Pricing Config (Live)
        DebugCard(title = stringResource(Res.string.debug_section_pricing)) {
            if (state.uid == null) {
                Text(
                    text = stringResource(Res.string.debug_pricing_sign_in_required),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (state.isPricingLoading && state.pricingConfig == null) {
                Text(
                    text = stringResource(Res.string.debug_pricing_loading),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else if (state.pricingConfig != null) {
                PricingRows(config = state.pricingConfig)
            } else {
                Text(
                    text = "No pricing config found",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // Section 4: Actions
        Button(
            onClick = onNavigateToGallery,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.debug_component_gallery))
        }

        Button(
            onClick = { onAction(DebugAction.TestCrash) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.debug_test_crash))
        }
    }
}

@Composable
private fun PricingRows(config: PricingConfig) {
    DebugRow(
        label = stringResource(Res.string.debug_fuel_price),
        value = "${config.fuelPriceEgpPerLiter} EGP",
    )
    DebugRow(
        label = stringResource(Res.string.debug_car_consumption),
        value = "${config.litersPer100Km.car} L/100km",
    )
    val scooterLiters = config.litersPer100Km.scooter?.let { "$it L/100km" }
        ?: stringResource(Res.string.debug_disabled)
    DebugRow(
        label = stringResource(Res.string.debug_scooter_consumption),
        value = scooterLiters,
    )
    DebugRow(
        label = stringResource(Res.string.debug_running_cost),
        value = "${config.runningCostEgpPerKm.car} EGP/km",
    )
    val trafficStr = config.trafficFactor.entries.joinToString(", ") { "${it.key}:${it.value}" }
    DebugRow(
        label = stringResource(Res.string.debug_traffic_factors),
        value = trafficStr,
    )
    DebugRow(
        label = stringResource(Res.string.debug_margin_cap),
        value = "${(config.marginCap * 100).toInt()}%",
    )
    DebugRow(
        label = stringResource(Res.string.debug_max_seats),
        value = "Car ${config.maxSeats.car}, Scooter ${config.maxSeats.scooter}",
    )
}

@Composable
private fun DebugCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}

@Composable
private fun DebugRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
