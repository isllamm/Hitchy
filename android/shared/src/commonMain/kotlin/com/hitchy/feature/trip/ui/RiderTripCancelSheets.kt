@file:Suppress("MagicNumber")

package com.hitchy.feature.trip.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hitchy.core.model.CancelBookingReason
import com.hitchy.core.ui.components.BannerVariant
import com.hitchy.core.ui.components.HitchyBanner
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyRadio
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.trip.presentation.RiderTripAction
import com.hitchy.feature.trip.presentation.RiderTripState
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.rider_trip_cancel_button
import hitchy.shared.generated.resources.rider_trip_cancel_reason_found_another
import hitchy.shared.generated.resources.rider_trip_cancel_reason_host_late
import hitchy.shared.generated.resources.rider_trip_cancel_reason_other
import hitchy.shared.generated.resources.rider_trip_cancel_reason_plans_changed
import hitchy.shared.generated.resources.rider_trip_cancelled_title
import hitchy.shared.generated.resources.rider_trip_keep_booking
import hitchy.shared.generated.resources.trip_design_cancel_info
import hitchy.shared.generated.resources.trip_design_cancel_title
import hitchy.shared.generated.resources.trip_design_cancelled_body
import hitchy.shared.generated.resources.trip_design_find_another
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val SheetRadius = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CancelBookingSheet(
    state: RiderTripState,
    onAction: (RiderTripAction) -> Unit,
) {
    val colors = HitchyTheme.colors
    val reasons = listOf(
        CancelBookingReason.PLANS_CHANGED to stringResource(Res.string.rider_trip_cancel_reason_plans_changed),
        CancelBookingReason.FOUND_ANOTHER_RIDE to stringResource(Res.string.rider_trip_cancel_reason_found_another),
        CancelBookingReason.HOST_LATE to stringResource(Res.string.rider_trip_cancel_reason_host_late),
        CancelBookingReason.OTHER to stringResource(Res.string.rider_trip_cancel_reason_other),
    )
    ModalBottomSheet(
        onDismissRequest = { onAction(RiderTripAction.ShowCancelSheet(false)) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.white,
        shape = RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.trip_design_cancel_title),
                style = HitchyTheme.typography.h2,
                color = colors.navy,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Column {
                reasons.forEach { (reason, label) ->
                    CancelReasonRow(
                        label = label,
                        selected = state.selectedCancelReason == reason,
                        enabled = !state.isCancelling,
                        onClick = { onAction(RiderTripAction.SelectCancelReason(reason)) },
                    )
                }
            }
            HitchyBanner(text = stringResource(Res.string.trip_design_cancel_info), variant = BannerVariant.Info)
            SheetTextAction(
                text = stringResource(Res.string.rider_trip_cancel_button),
                color = colors.error,
                enabled = !state.isCancelling,
                onClick = { onAction(RiderTripAction.ConfirmCancel) },
            )
            SheetTextAction(
                text = stringResource(Res.string.rider_trip_keep_booking),
                color = colors.purple,
                enabled = !state.isCancelling,
                onClick = { onAction(RiderTripAction.ShowCancelSheet(false)) },
            )
        }
    }
}

@Composable
private fun CancelReasonRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HitchyRadio(selected = selected, onClick = null, enabled = enabled)
            Text(text = label, style = HitchyTheme.typography.body, color = colors.navy)
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

@Composable
private fun SheetTextAction(
    text: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style = HitchyTheme.typography.bodyStrong,
        color = if (enabled) color else HitchyTheme.colors.textDisabled,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookingCancelledSheet(
    onFindAnother: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = HitchyTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.white,
        shape = RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.rider_trip_cancelled_title),
                style = HitchyTheme.typography.h2,
                color = colors.navy,
            )
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(colors.lavender),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Check),
                    contentDescription = null,
                    tint = colors.purple,
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = stringResource(Res.string.trip_design_cancelled_body),
                style = HitchyTheme.typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            HitchyButton(onClick = onFindAnother, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(Res.string.trip_design_find_another))
            }
        }
    }
}
