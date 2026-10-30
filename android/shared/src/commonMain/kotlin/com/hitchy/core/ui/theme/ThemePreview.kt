@file:Suppress("MagicNumber", "DEPRECATION")

package com.hitchy.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TypographySample(
    sampleText: String,
    modifier: Modifier = Modifier,
) {
    val typography = HitchyTheme.typography
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Display 32/40 Bold", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.display)
        HorizontalDivider()

        Text("H1 28/36 Bold", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.h1)
        HorizontalDivider()

        Text("H2 24/32 Bold", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.h2)
        HorizontalDivider()

        Text("H3 20/28 Bold", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.h3)
        HorizontalDivider()

        Text("Title 18/26 SemiBold", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.title)
        HorizontalDivider()

        Text("BodyLarge 16/24 Regular", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.bodyLarge)
        HorizontalDivider()

        Text("BodyLargeStrong 16/24 SemiBold", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.bodyLargeStrong)
        HorizontalDivider()

        Text("Body 14/22 Regular", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.body)
        HorizontalDivider()

        Text("BodyStrong 14/22 SemiBold", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.bodyStrong)
        HorizontalDivider()

        Text("Caption 12/18 Regular", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.caption)
        HorizontalDivider()

        Text("CaptionStrong 12/18 SemiBold", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.captionStrong)
        HorizontalDivider()

        Text("Button 16/24 Bold", style = typography.caption, color = HitchyTheme.colors.textSecondary)
        Text(sampleText, style = typography.button)
    }
}

@Preview
@Composable
fun CairoThemeEnglishPreview() {
    HitchyTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(modifier = Modifier.background(HitchyTheme.colors.background)) {
                TypographySample("The quick brown fox jumps over the lazy dog")
            }
        }
    }
}

@Preview
@Composable
fun CairoThemeArabicPreview() {
    HitchyTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(modifier = Modifier.background(HitchyTheme.colors.background)) {
                TypographySample("مشاركة الرحلات اليومية في القاهرة الكبرى")
            }
        }
    }
}
