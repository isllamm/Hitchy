@file:Suppress("MagicNumber")

package com.hitchy.feature.trip.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.HitchyRadio
import com.hitchy.core.ui.figmaBottom
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

// Figma frames 390:1968 (Cancel booking) and 390:2079 (Booking cancelled).
private val SheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private const val SCRIM_ALPHA = 0.42f

/** The two sheets' frame: r=24, pad 8/16/42/16, 24dp gaps, a 40x4 handle, over a 42% navy scrim. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FigmaSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = HitchyTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.white,
        shape = SheetShape,
        scrimColor = colors.navy.copy(alpha = SCRIM_ALPHA),
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = figmaBottom(42.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(modifier = Modifier.size(width = 40.dp, height = 4.dp).clip(CircleShape).background(colors.border))
            content()
        }
    }
}

@Composable
internal fun CancelBookingSheet(
    state: RiderTripState,
    onAction: (RiderTripAction) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val reasons = listOf(
        CancelBookingReason.PLANS_CHANGED to stringResource(Res.string.rider_trip_cancel_reason_plans_changed),
        CancelBookingReason.FOUND_ANOTHER_RIDE to stringResource(Res.string.rider_trip_cancel_reason_found_another),
        CancelBookingReason.HOST_LATE to stringResource(Res.string.rider_trip_cancel_reason_host_late),
        CancelBookingReason.OTHER to stringResource(Res.string.rider_trip_cancel_reason_other),
    )
    FigmaSheet(onDismiss = { onAction(RiderTripAction.ShowCancelSheet(false)) }) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = stringResource(Res.string.trip_design_cancel_title),
                    style = typography.h1,
                    color = colors.navy,
                    textAlign = TextAlign.Center,
                )
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.infoBg)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(HitchyIcons.Info16),
                            contentDescription = null,
                            tint = colors.deepPurple,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(Res.string.trip_design_cancel_info),
                            style = typography.caption,
                            color = colors.infoText,
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HitchyButton(
                    onClick = { onAction(RiderTripAction.ConfirmCancel) },
                    variant = HitchyButtonVariant.Destructive,
                    enabled = !state.isCancelling,
                    loading = state.isCancelling,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.rider_trip_cancel_button))
                }
                Text(
                    text = stringResource(Res.string.rider_trip_keep_booking),
                    style = typography.bodyBold,
                    color = if (state.isCancelling) colors.textDisabled else colors.purple,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !state.isCancelling) { onAction(RiderTripAction.ShowCancelSheet(false)) },
                )
            }
        }
    }
}

/** Figma's 48dp reason row: a 22dp radio (2dp stroke; the chosen one purple with a 10dp dot) and a 14/22 label. */
@Composable
internal fun CancelReasonRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (selected) colors.purple else colors.border, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.purple))
            }
            Text(text = label, style = HitchyTheme.typography.body, color = colors.navy)
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

@Composable
internal fun BookingCancelledSheet(
    onFindAnother: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    FigmaSheet(onDismiss = onDismiss) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.rider_trip_cancelled_title),
                        style = typography.h1,
                        color = colors.navy,
                    )
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(colors.lavender),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(HitchyIcons.CancelledCheck),
                            contentDescription = null,
                            tint = colors.violet,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
                Text(
                    text = stringResource(Res.string.trip_design_cancelled_body),
                    style = typography.body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            HitchyButton(onClick = onFindAnother, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(Res.string.trip_design_find_another))
            }
        }
    }
}
