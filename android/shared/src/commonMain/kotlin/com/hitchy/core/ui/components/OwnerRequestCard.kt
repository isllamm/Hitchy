package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.action_accept
import hitchy.shared.generated.resources.action_decline
import hitchy.shared.generated.resources.expires_in
import hitchy.shared.generated.resources.needs_design_review
import hitchy.shared.generated.resources.owner_request_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun OwnerRequestCard(
    riderName: String,
    riderInitials: String,
    riderRating: Double,
    seatsRequested: Int,
    deadlineRemaining: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val cardShape = RoundedCornerShape(16.dp)
    val pillShape = RoundedCornerShape(50)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, colors.border, cardShape),
        shape = cardShape,
        color = colors.white,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space12),
        ) {
            // Header: Title + Needs review badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.owner_request_title),
                    style = typography.captionStrong,
                    color = colors.purple,
                )

                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(colors.warningBg)
                        .padding(horizontal = spacing.space8, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.needs_design_review),
                        style = typography.caption,
                        color = colors.warningText,
                    )
                }
            }

            // Rider info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                Avatar(initials = riderInitials, size = AvatarSize.MD)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = riderName,
                        style = typography.bodyStrong,
                        color = colors.navy,
                    )
                    RatingDisplay(
                        rating = riderRating,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                // Seats tag
                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(colors.lavender)
                        .padding(horizontal = spacing.space12, vertical = spacing.space4),
                ) {
                    Text(
                        text = "$seatsRequested seat${if (seatsRequested > 1) "s" else ""}",
                        style = typography.captionStrong,
                        color = colors.purple,
                    )
                }
            }

            // Countdown timer
            Text(
                text = stringResource(Res.string.expires_in, deadlineRemaining),
                style = typography.caption,
                color = colors.textSecondary,
            )

            // Accept & Decline buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                HitchyButton(
                    onClick = onDecline,
                    variant = HitchyButtonVariant.Outline,
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                ) {
                    Text(stringResource(Res.string.action_decline))
                }

                HitchyButton(
                    onClick = onAccept,
                    variant = HitchyButtonVariant.Primary,
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                ) {
                    Text(stringResource(Res.string.action_accept))
                }
            }
        }
    }
}

@Preview
@Composable
fun OwnerRequestCardLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Box(modifier = Modifier.padding(spacing.space16)) {
                OwnerRequestCard(
                    riderName = "Kareem Hassan",
                    riderInitials = "KH",
                    riderRating = 4.8,
                    seatsRequested = 2,
                    deadlineRemaining = "01:45:00",
                    onAccept = {},
                    onDecline = {},
                )
            }
        }
    }
}

@Preview
@Composable
fun OwnerRequestCardRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        OwnerRequestCardLtrPreview()
    }
}
