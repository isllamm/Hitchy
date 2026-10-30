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
import hitchy.shared.generated.resources.action_pay_now
import hitchy.shared.generated.resources.late_fee_title
import hitchy.shared.generated.resources.needs_design_review
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LateFeeCard(
    amountEgp: Int,
    reason: String,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier,
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
            .border(1.5.dp, colors.warning, cardShape),
        shape = cardShape,
        color = colors.warningBg,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.late_fee_title),
                    style = typography.title,
                    color = colors.warningText,
                )

                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(colors.white)
                        .padding(horizontal = spacing.space8, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.needs_design_review),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
            }

            Text(
                text = "EGP $amountEgp",
                style = typography.h2,
                color = colors.navy,
            )

            Text(
                text = reason,
                style = typography.body,
                color = colors.warningText,
            )

            HitchyButton(
                onClick = onPayClick,
                variant = HitchyButtonVariant.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.space8),
            ) {
                Text(stringResource(Res.string.action_pay_now))
            }
        }
    }
}

@Preview
@Composable
fun LateFeeCardLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Box(modifier = Modifier.padding(spacing.space16)) {
                LateFeeCard(
                    amountEgp = 20,
                    reason = "Late cancellation fee for ride on 2026-10-28",
                    onPayClick = {},
                )
            }
        }
    }
}

@Preview
@Composable
fun LateFeeCardRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LateFeeCardLtrPreview()
    }
}
