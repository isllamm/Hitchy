package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.action_call
import hitchy.shared.generated.resources.action_emergency
import hitchy.shared.generated.resources.action_message
import hitchy.shared.generated.resources.action_report
import hitchy.shared.generated.resources.action_share_trip
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class TripActionType {
    Message,
    Call,
    ShareTrip,
    Report,
    Emergency,
}

@Composable
fun ActionTile(
    type: TripActionType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    val label = when (type) {
        TripActionType.Message -> stringResource(Res.string.action_message)
        TripActionType.Call -> stringResource(Res.string.action_call)
        TripActionType.ShareTrip -> stringResource(Res.string.action_share_trip)
        TripActionType.Report -> stringResource(Res.string.action_report)
        TripActionType.Emergency -> stringResource(Res.string.action_emergency)
    }

    val icon: DrawableResource = when (type) {
        TripActionType.Message -> HitchyIcons.Mail
        TripActionType.Call -> HitchyIcons.More
        TripActionType.ShareTrip -> HitchyIcons.Upload
        TripActionType.Report -> HitchyIcons.Warning
        TripActionType.Emergency -> HitchyIcons.ShieldCheck
    }

    val isEmergency = type == TripActionType.Emergency
    val circleBg = if (isEmergency) colors.errorBg else colors.lavender
    val iconTint = if (isEmergency) colors.error else colors.purple
    val textColor = if (isEmergency) colors.errorText else colors.navy

    Column(
        modifier = modifier
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(spacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }

        Text(
            text = label,
            style = typography.caption,
            color = textColor,
        )
    }
}

@Composable
fun ActionTilesRow(
    onMessage: () -> Unit,
    onCall: () -> Unit,
    onShare: () -> Unit,
    onEmergency: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionTile(type = TripActionType.Message, onClick = onMessage)
        ActionTile(type = TripActionType.Call, onClick = onCall)
        ActionTile(type = TripActionType.ShareTrip, onClick = onShare)
        ActionTile(type = TripActionType.Emergency, onClick = onEmergency)
    }
}

@Preview
@Composable
fun ActionTileLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Box(modifier = Modifier.padding(spacing.space16)) {
                ActionTilesRow(
                    onMessage = {},
                    onCall = {},
                    onShare = {},
                    onEmergency = {},
                )
            }
        }
    }
}

@Preview
@Composable
fun ActionTileRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ActionTileLtrPreview()
    }
}
