package com.hitchy.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class HitchyButtonVariant {
    Primary,
    Tonal,
    Outline,
    Text,
    Destructive,
}

@Composable
fun HitchyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HitchyButtonVariant = HitchyButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val buttonShape = RoundedCornerShape(12.dp)

    val buttonColors = when (variant) {
        HitchyButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = colors.purple,
            contentColor = colors.white,
            disabledContainerColor = colors.disabledSurface,
            disabledContentColor = colors.textDisabled,
        )

        HitchyButtonVariant.Tonal -> ButtonDefaults.buttonColors(
            containerColor = colors.lavender,
            contentColor = colors.purple,
            disabledContainerColor = colors.disabledSurface,
            disabledContentColor = colors.textDisabled,
        )

        HitchyButtonVariant.Outline -> ButtonDefaults.buttonColors(
            containerColor = colors.white,
            contentColor = colors.purple,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = colors.textDisabled,
        )

        HitchyButtonVariant.Text -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = colors.purple,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = colors.textDisabled,
        )

        HitchyButtonVariant.Destructive -> ButtonDefaults.buttonColors(
            containerColor = colors.errorBg,
            contentColor = colors.errorText,
            disabledContainerColor = colors.disabledSurface,
            disabledContentColor = colors.textDisabled,
        )
    }

    val borderStroke = if (variant == HitchyButtonVariant.Outline && enabled && !loading) {
        // Figma's Outline button (component 446:2914): white with a 1.5dp purple stroke.
        BorderStroke(1.5.dp, colors.purple)
    } else {
        null
    }

    val elevation = ButtonDefaults.buttonElevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp,
        focusedElevation = 0.dp,
        hoveredElevation = 0.dp,
        disabledElevation = 0.dp,
    )

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 52.dp),
        enabled = enabled && !loading,
        shape = buttonShape,
        colors = buttonColors,
        elevation = elevation,
        border = borderStroke,
        contentPadding = contentPadding,
    ) {
        if (loading) {
            val progressColor = when (variant) {
                HitchyButtonVariant.Primary -> colors.white
                HitchyButtonVariant.Destructive -> colors.errorText
                else -> colors.purple
            }
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = progressColor,
                strokeWidth = 2.dp,
            )
        } else {
            ProvideTextStyle(typography.button) {
                content()
            }
        }
    }
}

@Preview
@Composable
fun HitchyButtonLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                HitchyButton(onClick = {}, modifier = Modifier.fillMaxWidth(), variant = HitchyButtonVariant.Primary) {
                    Text("Primary Button")
                }
                HitchyButton(onClick = {}, modifier = Modifier.fillMaxWidth(), variant = HitchyButtonVariant.Tonal) {
                    Text("Tonal Button")
                }
                HitchyButton(onClick = {}, modifier = Modifier.fillMaxWidth(), variant = HitchyButtonVariant.Outline) {
                    Text("Outline Button")
                }
                HitchyButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    variant = HitchyButtonVariant.Destructive,
                ) {
                    Text("Destructive Button")
                }
                HitchyButton(onClick = {}, modifier = Modifier.fillMaxWidth(), loading = true) {
                    Text("Loading")
                }
                HitchyButton(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) {
                    Text("Disabled")
                }
            }
        }
    }
}

@Preview
@Composable
fun HitchyButtonRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchyButtonLtrPreview()
    }
}
