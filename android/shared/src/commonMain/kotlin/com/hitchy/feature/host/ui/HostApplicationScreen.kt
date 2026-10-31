@file:Suppress("MagicNumber")

package com.hitchy.feature.host.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.VehicleType
import com.hitchy.core.ui.StepBackHandler
import com.hitchy.core.ui.components.FloatingBanner
import com.hitchy.core.ui.components.IdentityField
import com.hitchy.core.ui.message
import com.hitchy.core.ui.rememberCameraCapture
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.host.presentation.HostApplicationAction
import com.hitchy.feature.host.presentation.HostApplicationEvent
import com.hitchy.feature.host.presentation.HostApplicationState
import com.hitchy.feature.host.presentation.HostApplicationViewModel
import com.hitchy.feature.host.presentation.HostStep
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fig_su_camera
import hitchy.shared.generated.resources.fig_su_car
import hitchy.shared.generated.resources.fig_su_helmet
import hitchy.shared.generated.resources.fig_su_moto
import hitchy.shared.generated.resources.fig_su_selected
import hitchy.shared.generated.resources.fig_su_selfie
import hitchy.shared.generated.resources.img_app_icon
import hitchy.shared.generated.resources.su_basic_sub
import hitchy.shared.generated.resources.su_basic_title
import hitchy.shared.generated.resources.su_car
import hitchy.shared.generated.resources.su_car_sub
import hitchy.shared.generated.resources.su_full_name
import hitchy.shared.generated.resources.su_full_name_id
import hitchy.shared.generated.resources.su_helmet
import hitchy.shared.generated.resources.su_loading_body
import hitchy.shared.generated.resources.su_loading_title
import hitchy.shared.generated.resources.su_moto
import hitchy.shared.generated.resources.su_moto_sub
import hitchy.shared.generated.resources.su_review_details
import hitchy.shared.generated.resources.su_selfie
import hitchy.shared.generated.resources.su_selfie_add
import hitchy.shared.generated.resources.su_selfie_added
import hitchy.shared.generated.resources.su_selfie_hint
import hitchy.shared.generated.resources.su_submit
import hitchy.shared.generated.resources.su_tip_cover
import hitchy.shared.generated.resources.su_tip_light
import hitchy.shared.generated.resources.su_tips_title
import hitchy.shared.generated.resources.su_vehicle_sub
import hitchy.shared.generated.resources.su_vehicle_title
import hitchy.shared.generated.resources.welcome_continue
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** The owner application (Figma DEMO "car owner": Choose vehicle, Basic info, Verify, Vehicle, Review, Loading). */
@Composable
fun HostApplicationScreen(
    onExit: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: HostApplicationViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HostApplicationEvent.Exit -> onExit()
                HostApplicationEvent.Submitted, HostApplicationEvent.AlreadySubmitted -> onSubmitted()
            }
        }
    }
    StepBackHandler(enabled = true) { viewModel.onAction(HostApplicationAction.Back) }
    if (state.submitting) {
        LoadingScreen()
        return
    }
    HostApplicationContent(state = state, onAction = viewModel::onAction)
}

@Composable
internal fun HostApplicationContent(state: HostApplicationState, onAction: (HostApplicationAction) -> Unit) {
    val button = when (state.step) {
        HostStep.DETAILS -> stringResource(Res.string.su_review_details)
        HostStep.REVIEW -> stringResource(Res.string.su_submit)
        else -> stringResource(Res.string.welcome_continue)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        SignUpFrame(
            button = button,
            enabled = state.canContinue,
            onButton = { onAction(HostApplicationAction.Continue) },
        ) {
            when (state.step) {
                HostStep.VEHICLE -> VehicleStep(state, onAction)
                HostStep.BASIC -> BasicStep(state, onAction)
                HostStep.DOCS -> DocsStep(state, onAction)
                HostStep.DETAILS -> DetailsStep(state, onAction)
                HostStep.REVIEW -> ReviewStep(state, onAction)
            }
        }
        state.error?.let { error ->
            FloatingBanner(
                text = stringResource(error.message()),
                onDismiss = { onAction(HostApplicationAction.DismissError) },
            )
        }
    }
    state.uploadSheetFor?.let { key -> UploadSheet(key, onAction) }
    if (state.showExit) ExitSheet(onAction)
}

/** Figma 726:13737 "What do you ride?": two 176dp cards, the helmet note under them for a motorcycle. */
@Composable
private fun VehicleStep(state: HostApplicationState, onAction: (HostApplicationAction) -> Unit) {
    val colors = HitchyTheme.colors
    SignUpHeader(onBack = { onAction(HostApplicationAction.Back) })
    SignUpIntro(
        title = stringResource(Res.string.su_vehicle_title),
        subtitle = stringResource(Res.string.su_vehicle_sub),
        modifier = Modifier.padding(top = 24.dp),
    )
    Row(modifier = Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        VehicleCard(
            icon = Res.drawable.fig_su_car,
            title = stringResource(Res.string.su_car),
            subtitle = stringResource(Res.string.su_car_sub),
            selected = state.vehicleType == VehicleType.CAR,
            onClick = { onAction(HostApplicationAction.SelectVehicle(VehicleType.CAR)) },
            modifier = Modifier.weight(1f),
        )
        VehicleCard(
            icon = Res.drawable.fig_su_moto,
            title = stringResource(Res.string.su_moto),
            subtitle = stringResource(Res.string.su_moto_sub),
            selected = state.vehicleType == VehicleType.SCOOTER,
            onClick = { onAction(HostApplicationAction.SelectVehicle(VehicleType.SCOOTER)) },
            modifier = Modifier.weight(1f),
        )
    }
    if (state.vehicleType == VehicleType.SCOOTER) {
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.infoBg)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SuIcon(Res.drawable.fig_su_helmet, 16.dp)
            Text(
                text = stringResource(Res.string.su_helmet),
                style = HitchyTheme.typography.caption,
                color = colors.infoText,
            )
        }
    }
}

@Composable
private fun VehicleCard(
    icon: DrawableResource,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(176.dp)
            .clip(shape)
            .background(if (selected) colors.lavender else colors.white)
            .border(1.3.dp, if (selected) colors.purple else colors.border, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.BottomStart), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(colors.lilac),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = HitchyTheme.typography.title, color = colors.navy)
                Text(text = subtitle, style = HitchyTheme.typography.caption, color = colors.textSecondary)
            }
        }
        if (selected) {
            Image(
                painter = painterResource(Res.drawable.fig_su_selected),
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopEnd).size(24.dp).clip(CircleShape).background(colors.purple),
            )
        }
    }
}

/** Figma 726:14030 "A little about you": the name as on the ID, a selfie and the photo tips. */
@Composable
private fun BasicStep(state: HostApplicationState, onAction: (HostApplicationAction) -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val takeSelfie = rememberCameraCapture { onAction(HostApplicationAction.SelfieTaken(it)) }
    SignUpHeader(onBack = { onAction(HostApplicationAction.Back) })
    SignUpIntro(
        title = stringResource(Res.string.su_basic_title),
        subtitle = stringResource(Res.string.su_basic_sub),
        modifier = Modifier.padding(top = 24.dp),
    )
    Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        IdentityField(
            value = state.fullName,
            onValueChange = { onAction(HostApplicationAction.UpdateName(it)) },
            label = stringResource(Res.string.su_full_name_id),
            placeholder = stringResource(Res.string.su_full_name),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(Res.string.su_selfie), style = typography.bodyStrong, color = colors.textPrimary)
            val shape = RoundedCornerShape(16.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .border(1.dp, colors.purple, shape)
                    .clickable(onClick = takeSelfie)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier.size(55.dp).clip(CircleShape).background(colors.lavender),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painterResource(Res.drawable.fig_su_selfie), contentDescription = null)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val selfieLabel = if (state.hasSelfie) Res.string.su_selfie_added else Res.string.su_selfie_add
                    Text(
                        text = stringResource(selfieLabel),
                        style = typography.bodyStrong,
                        color = colors.navy,
                    )
                    Text(
                        text = stringResource(Res.string.su_selfie_hint),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
                SuIcon(Res.drawable.fig_su_camera, 22.dp)
            }
        }
        SignUpTips(
            title = stringResource(Res.string.su_tips_title),
            tips = listOf(stringResource(Res.string.su_tip_light), stringResource(Res.string.su_tip_cover)),
        )
    }
}

/** Figma 726:14795: the app icon, a spinner and "Getting things ready", centred. */
@Composable
internal fun LoadingScreen() {
    val colors = HitchyTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().background(colors.white).padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(painterResource(Res.drawable.img_app_icon), contentDescription = null, modifier = Modifier.size(85.dp))
        Spacer(Modifier.height(32.dp))
        CircularProgressIndicator(modifier = Modifier.size(35.dp), color = colors.purple, strokeWidth = 3.dp)
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.su_loading_title),
            style = HitchyTheme.typography.bodyBold,
            color = colors.navy,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.su_loading_body),
            style = HitchyTheme.typography.caption,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
