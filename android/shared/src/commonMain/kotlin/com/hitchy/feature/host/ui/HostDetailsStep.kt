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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hitchy.core.model.VehicleType
import com.hitchy.core.ui.components.IdentityField
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.host.domain.KycDocKey
import com.hitchy.feature.host.presentation.HostApplicationAction
import com.hitchy.feature.host.presentation.HostApplicationState
import com.hitchy.feature.host.presentation.HostStep
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.color_black
import hitchy.shared.generated.resources.color_blue
import hitchy.shared.generated.resources.color_gray
import hitchy.shared.generated.resources.color_other
import hitchy.shared.generated.resources.color_red
import hitchy.shared.generated.resources.color_silver
import hitchy.shared.generated.resources.color_white
import hitchy.shared.generated.resources.fig_su_car
import hitchy.shared.generated.resources.fig_su_checkbox
import hitchy.shared.generated.resources.fig_su_edit
import hitchy.shared.generated.resources.fig_su_moto
import hitchy.shared.generated.resources.su_basic_info
import hitchy.shared.generated.resources.su_car
import hitchy.shared.generated.resources.su_change
import hitchy.shared.generated.resources.su_color
import hitchy.shared.generated.resources.su_color_hint
import hitchy.shared.generated.resources.su_color_seats
import hitchy.shared.generated.resources.su_confirm
import hitchy.shared.generated.resources.su_details_sub
import hitchy.shared.generated.resources.su_details_title
import hitchy.shared.generated.resources.su_documents
import hitchy.shared.generated.resources.su_edit
import hitchy.shared.generated.resources.su_full_name
import hitchy.shared.generated.resources.su_id_added
import hitchy.shared.generated.resources.su_licenses
import hitchy.shared.generated.resources.su_licenses_added
import hitchy.shared.generated.resources.su_make_model
import hitchy.shared.generated.resources.su_make_model_hint
import hitchy.shared.generated.resources.su_make_model_short
import hitchy.shared.generated.resources.su_missing
import hitchy.shared.generated.resources.su_moto
import hitchy.shared.generated.resources.su_national_id
import hitchy.shared.generated.resources.su_passenger_seat
import hitchy.shared.generated.resources.su_phone
import hitchy.shared.generated.resources.su_plate
import hitchy.shared.generated.resources.su_plate_hint
import hitchy.shared.generated.resources.su_review_sub
import hitchy.shared.generated.resources.su_review_title
import hitchy.shared.generated.resources.su_seats
import hitchy.shared.generated.resources.su_seats_value
import hitchy.shared.generated.resources.su_vehicle_section
import hitchy.shared.generated.resources.su_year
import hitchy.shared.generated.resources.su_year_hint
import hitchy.shared.generated.resources.su_year_plate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** "3 seats" for a car, "Passenger seat" for a motorcycle (Figma 726:14329). */
@Composable
private fun seatsText(state: HostApplicationState): String = if (state.vehicleType == VehicleType.SCOOTER) {
    stringResource(Res.string.su_passenger_seat)
} else {
    stringResource(Res.string.su_seats_value, state.seats)
}

@Composable
private fun vehicleName(type: VehicleType?): String {
    val name = if (type == VehicleType.SCOOTER) Res.string.su_moto else Res.string.su_car
    return stringResource(name)
}

/** Figma 726:14329 "Tell us about your vehicle": the chosen type with Change, then the vehicle fields. */
@Composable
internal fun DetailsStep(state: HostApplicationState, onAction: (HostApplicationAction) -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val scooter = state.vehicleType == VehicleType.SCOOTER
    SignUpHeader(onBack = { onAction(HostApplicationAction.Back) }, step = 2)
    Row(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.lavender)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(
                if (scooter) Res.drawable.fig_su_moto else Res.drawable.fig_su_car,
            ),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = vehicleName(state.vehicleType),
            style = typography.body,
            color = colors.navy,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(Res.string.su_change),
            style = typography.bodyStrong,
            color = colors.purple,
            modifier = Modifier.clickable { onAction(HostApplicationAction.Edit(HostStep.VEHICLE)) },
        )
    }
    SignUpIntro(
        title = stringResource(Res.string.su_details_title),
        subtitle = stringResource(Res.string.su_details_sub),
        modifier = Modifier.padding(top = 24.dp),
    )
    val colorNames = listOf(
        Res.string.color_white,
        Res.string.color_black,
        Res.string.color_silver,
        Res.string.color_gray,
        Res.string.color_red,
        Res.string.color_blue,
        Res.string.color_other,
    ).map { stringResource(it) }
    Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IdentityField(
            value = state.makeModel,
            onValueChange = { onAction(HostApplicationAction.UpdateMakeModel(it)) },
            label = stringResource(Res.string.su_make_model),
            placeholder = stringResource(Res.string.su_make_model_hint),
        )
        SignUpDropdown(
            label = stringResource(Res.string.su_year),
            value = state.year?.toString(),
            placeholder = stringResource(Res.string.su_year_hint),
            options = state.years.map { it.toString() },
            onSelect = { onAction(HostApplicationAction.SelectYear(state.years[it])) },
        )
        IdentityField(
            value = state.plate,
            onValueChange = { onAction(HostApplicationAction.UpdatePlate(it)) },
            label = stringResource(Res.string.su_plate),
            placeholder = stringResource(Res.string.su_plate_hint),
        )
        SignUpDropdown(
            label = stringResource(Res.string.su_color),
            value = state.color.ifBlank { null },
            placeholder = stringResource(Res.string.su_color_hint),
            options = colorNames,
            onSelect = { onAction(HostApplicationAction.SelectColor(colorNames[it])) },
        )
        SignUpDropdown(
            label = stringResource(Res.string.su_seats),
            value = seatsText(state),
            placeholder = "",
            options = emptyList(),
            onSelect = {},
        )
    }
}

/** Figma 726:14497 "Review your details": three cards with Edit, the confirmation box, then Submit. */
@Composable
internal fun ReviewStep(state: HostApplicationState, onAction: (HostApplicationAction) -> Unit) {
    val colors = HitchyTheme.colors
    val missing = stringResource(Res.string.su_missing)
    SignUpHeader(onBack = { onAction(HostApplicationAction.Back) })
    SignUpIntro(
        title = stringResource(Res.string.su_review_title),
        subtitle = stringResource(Res.string.su_review_sub),
        modifier = Modifier.padding(top = 24.dp),
    )
    Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReviewCard(
            title = stringResource(Res.string.su_basic_info),
            rows = listOf(
                stringResource(Res.string.su_full_name) to state.fullName,
                stringResource(Res.string.su_phone) to state.phone,
            ),
            onEdit = { onAction(HostApplicationAction.Edit(HostStep.BASIC)) },
        )
        val idDone = state.docs.containsAll(listOf(KycDocKey.ID_FRONT, KycDocKey.ID_BACK))
        val licensesDone = state.docs.containsAll(listOf(KycDocKey.DRIVING_LICENSE, KycDocKey.VEHICLE_LICENSE))
        ReviewCard(
            title = stringResource(Res.string.su_documents),
            rows = listOf(
                stringResource(Res.string.su_national_id) to
                    if (idDone) stringResource(Res.string.su_id_added) else missing,
                stringResource(Res.string.su_licenses) to
                    if (licensesDone) stringResource(Res.string.su_licenses_added) else missing,
            ),
            onEdit = { onAction(HostApplicationAction.Edit(HostStep.DOCS)) },
        )
        ReviewCard(
            title = stringResource(Res.string.su_vehicle_section, vehicleName(state.vehicleType)),
            rows = listOf(
                stringResource(Res.string.su_make_model_short) to state.makeModel,
                stringResource(Res.string.su_color_seats) to "${state.color} · ${seatsText(state)}",
                stringResource(Res.string.su_year_plate) to "${state.year ?: ""} · ${state.plate}",
            ),
            onEdit = { onAction(HostApplicationAction.Edit(HostStep.DETAILS)) },
        )
    }
    Row(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .clickable { onAction(HostApplicationAction.ToggleConfirm) },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val box = RoundedCornerShape(6.dp)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(box)
                .background(if (state.confirmed) colors.purple else colors.white)
                .then(if (state.confirmed) Modifier else Modifier.border(1.dp, colors.border, box)),
            contentAlignment = Alignment.Center,
        ) {
            if (state.confirmed) {
                Image(
                    painter = painterResource(Res.drawable.fig_su_checkbox),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            text = stringResource(Res.string.su_confirm),
            style = HitchyTheme.typography.caption,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ReviewCard(title: String, rows: List<Pair<String, String>>, onEdit: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = typography.bodyStrong, color = colors.navy, modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.clickable(onClick = onEdit),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.fig_su_edit),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                )
                Text(text = stringResource(Res.string.su_edit), style = typography.captionStrong, color = colors.purple)
            }
        }
        rows.forEach { (label, value) ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = label, style = typography.caption, color = colors.textSecondary)
                Text(
                    text = value,
                    style = typography.caption,
                    color = colors.navy,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
