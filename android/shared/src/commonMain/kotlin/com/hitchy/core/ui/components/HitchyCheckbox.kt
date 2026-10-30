package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HitchyCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val boxModifier = modifier
        .size(48.dp)
        .then(
            if (onCheckedChange != null) {
                Modifier.clickable(
                    enabled = enabled,
                    role = Role.Checkbox,
                    onClick = { onCheckedChange(!checked) },
                )
            } else {
                Modifier
            },
        )

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center,
    ) {
        val backgroundColor = when {
            !enabled -> colors.disabledSurface
            checked -> colors.purple
            else -> colors.white
        }
        val borderColor = when {
            !enabled -> colors.border
            checked -> colors.purple
            else -> colors.border
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .border(1.5.dp, borderColor, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    painter = painterResource(HitchyIcons.Check),
                    contentDescription = null,
                    tint = if (enabled) colors.white else colors.textDisabled,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun HitchyCheckboxLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        val typography = LocalHitchyTypography.current
        val colors = LocalHitchyColors.current
        Surface(color = colors.background) {
            Row(
                modifier = Modifier.padding(spacing.space16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HitchyCheckbox(checked = true, onCheckedChange = {})
                Text("Women-only ride", style = typography.body, color = colors.textPrimary)
            }
        }
    }
}

@Preview
@Composable
fun HitchyCheckboxRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchyCheckboxLtrPreview()
    }
}
