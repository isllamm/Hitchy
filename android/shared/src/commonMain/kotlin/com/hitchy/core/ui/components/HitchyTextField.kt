package com.hitchy.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HitchyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val isError = !errorText.isNullOrBlank()

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = typography.captionStrong,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = spacing.space4),
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            textStyle = typography.body.copy(
                color = if (enabled) colors.textPrimary else colors.textDisabled,
            ),
            placeholder = if (!placeholder.isNullOrBlank()) {
                {
                    Text(
                        text = placeholder,
                        style = typography.body,
                        color = colors.textDisabled,
                    )
                }
            } else {
                null
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.white,
                unfocusedContainerColor = colors.white,
                disabledContainerColor = colors.disabledSurface,
                errorContainerColor = colors.white,
                focusedBorderColor = colors.purple,
                unfocusedBorderColor = colors.border,
                disabledBorderColor = colors.border,
                errorBorderColor = colors.error,
                cursorColor = colors.purple,
                errorCursorColor = colors.error,
            ),
        )

        val bottomText = errorText ?: helperText
        if (!bottomText.isNullOrBlank()) {
            Text(
                text = bottomText,
                style = typography.caption,
                color = if (isError) colors.errorText else colors.textSecondary,
                modifier = Modifier.padding(top = spacing.space4, start = spacing.space4),
            )
        }
    }
}

@Preview
@Composable
fun HitchyTextFieldLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                HitchyTextField(
                    value = "",
                    onValueChange = {},
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    helperText = "Matches your national ID",
                )
                HitchyTextField(
                    value = "Ahmed Ali",
                    onValueChange = {},
                    label = "Full Name",
                    placeholder = "Enter your full name",
                )
                HitchyTextField(
                    value = "invalid",
                    onValueChange = {},
                    label = "Email Address",
                    errorText = "Enter a valid email address",
                )
                HitchyTextField(
                    value = "Disabled content",
                    onValueChange = {},
                    label = "National ID",
                    enabled = false,
                )
            }
        }
    }
}

@Preview
@Composable
fun HitchyTextFieldRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchyTextFieldLtrPreview()
    }
}
