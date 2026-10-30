package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.phone_field_error
import hitchy.shared.generated.resources.phone_field_helper
import hitchy.shared.generated.resources.phone_field_label
import hitchy.shared.generated.resources.phone_field_placeholder
import hitchy.shared.generated.resources.phone_field_prefix
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Figma's Phone Input: a 361x104 column of label, a 52dp r=12 field and a helper line,
 * 6dp apart. The field keeps the "+20" prefix and a 1x24 divider inside 16dp padding.
 * Material's OutlinedTextField cannot be 52dp tall, so the field is a BasicTextField.
 */
@Composable
fun PhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(Res.string.phone_field_label),
    placeholder: String = stringResource(Res.string.phone_field_placeholder),
    helperText: String? = stringResource(Res.string.phone_field_helper),
    isError: Boolean = false,
    errorMessage: String? = stringResource(Res.string.phone_field_error),
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val fieldShape = RoundedCornerShape(12.dp)
    val helper = if (isError) errorMessage else helperText

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = typography.bodyStrong,
            color = colors.textPrimary,
        )

        BasicTextField(
            value = value,
            onValueChange = { input -> onValueChange(input.filter { it.isDigit() }) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            textStyle = typography.bodyLarge.copy(
                color = if (enabled) colors.textPrimary else colors.textDisabled,
            ),
            cursorBrush = SolidColor(colors.purple),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            keyboardActions = keyboardActions,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(fieldShape)
                        .background(if (enabled) colors.white else colors.disabledSurface)
                        .border(
                            width = if (isError) 2.dp else 1.dp,
                            color = if (isError) colors.error else colors.border,
                            shape = fieldShape,
                        )
                        .padding(horizontal = spacing.space16),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space12),
                ) {
                    Text(
                        text = stringResource(Res.string.phone_field_prefix),
                        style = typography.bodyLargeStrong,
                        color = colors.textPrimary,
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(colors.border),
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = typography.bodyLarge,
                                color = colors.textSecondary,
                            )
                        }
                        innerTextField()
                    }
                }
            },
        )

        if (!helper.isNullOrBlank()) {
            Text(
                text = helper,
                style = typography.caption,
                color = if (isError) colors.errorText else colors.textSecondary,
            )
        }
    }
}

@Preview
@Composable
fun PhoneFieldLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.white) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                PhoneField(value = "", onValueChange = {})
                PhoneField(value = "1012345678", onValueChange = {})
                PhoneField(value = "1234", onValueChange = {}, isError = true)
            }
        }
    }
}

@Preview
@Composable
fun PhoneFieldRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        PhoneFieldLtrPreview()
    }
}
