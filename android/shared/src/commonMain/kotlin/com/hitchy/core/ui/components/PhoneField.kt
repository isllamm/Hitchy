package com.hitchy.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(Res.string.phone_field_label),
    placeholder: String = "10 1234 5678",
    helperText: String? = stringResource(Res.string.phone_field_helper),
    isError: Boolean = false,
    errorMessage: String? = stringResource(Res.string.phone_field_error),
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    HitchyTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        helperText = helperText,
        errorText = if (isError) errorMessage else null,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        keyboardActions = keyboardActions,
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = spacing.space12, end = spacing.space8),
            ) {
                Text(
                    text = "+20",
                    style = typography.bodyStrong,
                    color = colors.textPrimary,
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = spacing.space8)
                        .height(20.dp)
                        .width(1.dp),
                ) {
                    VerticalDivider(color = colors.border)
                }
            }
        },
    )
}

@Preview
@Composable
fun PhoneFieldLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                PhoneField(
                    value = "",
                    onValueChange = {},
                )
                PhoneField(
                    value = "1012345678",
                    onValueChange = {},
                )
                PhoneField(
                    value = "123",
                    onValueChange = {},
                    isError = true,
                )
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
