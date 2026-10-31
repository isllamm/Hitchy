package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchyTypography

/**
 * Figma's Identity Field (component 16:35): a SemiBold 14/22 label over a 52dp r=12 field with a 1dp #E5E7EB
 * stroke and 16/24 text, 6dp apart. The same look as [PhoneField] without the +20 prefix.
 */
@Composable
fun IdentityField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val fieldShape = RoundedCornerShape(12.dp)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = typography.bodyStrong, color = colors.textPrimary)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = typography.bodyLarge.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.purple),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(fieldShape)
                        .background(colors.white)
                        .border(1.dp, colors.border, fieldShape)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = typography.bodyLarge, color = colors.textSecondary)
                    }
                    innerTextField()
                }
            },
        )
    }
}
