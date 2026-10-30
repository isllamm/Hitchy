package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HitchyRadio(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val radioModifier = modifier
        .size(48.dp)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
            } else {
                Modifier
            },
        )

    Box(
        modifier = radioModifier,
        contentAlignment = Alignment.Center,
    ) {
        val borderColor = when {
            !enabled -> colors.border
            selected -> colors.purple
            else -> colors.border
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(colors.white)
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (enabled) colors.purple else colors.textDisabled),
                )
            }
        }
    }
}

@Preview
@Composable
fun HitchyRadioLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        val typography = LocalHitchyTypography.current
        val colors = LocalHitchyColors.current
        Surface(color = colors.background) {
            Row(
                modifier = Modifier.padding(spacing.space16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HitchyRadio(selected = true, onClick = {})
                Text("Selected radio option", style = typography.body, color = colors.textPrimary)
            }
        }
    }
}

@Preview
@Composable
fun HitchyRadioRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchyRadioLtrPreview()
    }
}
