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
import hitchy.shared.generated.resources.balance_credits
import hitchy.shared.generated.resources.balance_points
import hitchy.shared.generated.resources.needs_design_review
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BalanceCard(
    creditsEgp: Int,
    points: Int,
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
            .border(1.dp, colors.border, cardShape),
        shape = cardShape,
        color = colors.white,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space12),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "My Balance",
                    style = typography.title,
                    color = colors.navy,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Credits
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "EGP $creditsEgp",
                        style = typography.h2,
                        color = colors.purple,
                    )
                    Text(
                        text = stringResource(Res.string.balance_credits),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }

                // Points
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = points.toString(),
                        style = typography.h2,
                        color = colors.navy,
                    )
                    Text(
                        text = stringResource(Res.string.balance_points),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BalanceCardLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Box(modifier = Modifier.padding(spacing.space16)) {
                BalanceCard(creditsEgp = 150, points = 350)
            }
        }
    }
}

@Preview
@Composable
fun BalanceCardRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BalanceCardLtrPreview()
    }
}
