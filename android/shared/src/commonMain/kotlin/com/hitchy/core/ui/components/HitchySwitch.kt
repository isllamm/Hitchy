package com.hitchy.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
fun HitchySwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledSurface
            checked -> colors.purple
            else -> colors.border
        },
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
    )

    val switchModifier = modifier
        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
        .then(
            if (onCheckedChange != null) {
                Modifier.toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                )
            } else {
                Modifier
            },
        )

    Box(
        modifier = switchModifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(trackColor)
                .padding(2.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colors.white),
            )
        }
    }
}

@Preview
@Composable
fun HitchySwitchLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        val typography = LocalHitchyTypography.current
        val colors = LocalHitchyColors.current
        Surface(color = colors.background) {
            Row(
                modifier = Modifier.padding(spacing.space16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Notifications", style = typography.body, color = colors.textPrimary)
                HitchySwitch(checked = true, onCheckedChange = {})
            }
        }
    }
}

@Preview
@Composable
fun HitchySwitchRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchySwitchLtrPreview()
    }
}
