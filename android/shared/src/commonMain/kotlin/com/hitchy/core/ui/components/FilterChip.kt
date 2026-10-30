package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val pillShape = RoundedCornerShape(50)

    val backgroundColor = when {
        !enabled -> colors.disabledSurface
        selected -> colors.lavender
        else -> colors.white
    }
    val contentColor = when {
        !enabled -> colors.textDisabled
        selected -> colors.purple
        else -> colors.textPrimary
    }
    val borderColor = when {
        !enabled -> colors.border
        selected -> colors.purple
        else -> colors.border
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(pillShape)
                .background(backgroundColor)
                .border(1.dp, borderColor, pillShape)
                .padding(horizontal = spacing.space16, vertical = spacing.space8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(HitchyIcons.Check),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = label,
                style = if (selected) typography.captionStrong else typography.caption,
                color = contentColor,
            )
        }
    }
}

@Preview
@Composable
fun FilterChipLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Row(
                modifier = Modifier.padding(spacing.space16),
                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                FilterChip(label = "Clean car", selected = true, onClick = {})
                FilterChip(label = "Punctual", selected = false, onClick = {})
                FilterChip(label = "Great music", selected = false, onClick = {}, enabled = false)
            }
        }
    }
}

@Preview
@Composable
fun FilterChipRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FilterChipLtrPreview()
    }
}
