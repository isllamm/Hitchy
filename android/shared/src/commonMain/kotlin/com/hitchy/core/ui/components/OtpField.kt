package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val OTP_LENGTH = 6

@Composable
fun OtpField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    onComplete: ((String) -> Unit)? = null,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val boxShape = RoundedCornerShape(12.dp)

    BasicTextField(
        value = otpValue,
        onValueChange = { input ->
            val filtered = input.filter { it.isDigit() }.take(OTP_LENGTH)
            onOtpChange(filtered)
            if (filtered.length == OTP_LENGTH) {
                onComplete?.invoke(filtered)
            }
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        keyboardActions = KeyboardActions(
            onDone = {
                if (otpValue.length == OTP_LENGTH) {
                    onComplete?.invoke(otpValue)
                }
            },
        ),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.space8, Alignment.CenterHorizontally),
            ) {
                for (i in 0 until OTP_LENGTH) {
                    val char = otpValue.getOrNull(i)?.toString().orEmpty()
                    val isFocused = otpValue.length == i

                    val borderColor = when {
                        isError -> colors.error
                        isFocused -> colors.purple
                        char.isNotEmpty() -> colors.navy
                        else -> colors.border
                    }

                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(56.dp)
                            .clip(boxShape)
                            .background(if (enabled) colors.white else colors.disabledSurface)
                            .border(if (isFocused || isError) 2.dp else 1.dp, borderColor, boxShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = char,
                            style = typography.h2,
                            color = if (isError) colors.errorText else colors.navy,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        },
    )
}

@Preview
@Composable
fun OtpFieldLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Box(modifier = Modifier.padding(spacing.space16)) {
                OtpField(otpValue = "123", onOtpChange = {})
            }
        }
    }
}

@Preview
@Composable
fun OtpFieldRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        OtpFieldLtrPreview()
    }
}
