@file:Suppress("MagicNumber")

package com.hitchy.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeTokensTest {

    @Test
    fun testAllColorHexValues() {
        val colors = HitchyColors()

        // Brand
        assertEquals(Color(0xFF7C3AED), colors.purple)
        assertEquals(Color(0xFF4F46E5), colors.deepPurple)
        assertEquals(Color(0xFF1E1B4B), colors.navy)
        assertEquals(Color(0xFFA855F7), colors.violet)
        assertEquals(Color(0xFFFF3366), colors.pin)
        assertEquals(Color(0xFFEDE9FE), colors.lavender)
        assertEquals(Color(0xFFF5F3FF), colors.lilac)

        // Neutral
        assertEquals(Color(0xFFFFFFFF), colors.white)
        assertEquals(Color(0xFF0F0F0F), colors.scrim)
        assertEquals(Color(0xFFE9EEF5), colors.hairline)
        assertEquals(Color(0xFF4318FF), colors.royal)
        assertEquals(Color(0xFFFFF0F4), colors.pinSoft)
        assertEquals(Color(0xFF5B21E6), colors.purpleDeep)
        assertEquals(Color(0xFFCBD5E1), colors.handle)
        assertEquals(Color(0xFF64748B), colors.slate)
        assertEquals(Color(0xFFEDF3ED), colors.mapLand)
        assertEquals(Color(0xFF111827), colors.shadowInk)
        assertEquals(Color(0xFFD1D5DB), colors.gray300)
        assertEquals(Color(0xFFF8F9FC), colors.background)
        assertEquals(Color(0xFFE5E7EB), colors.border)
        assertEquals(Color(0xFFF3F4F6), colors.disabledSurface)

        // Text
        assertEquals(Color(0xFF1E1B4B), colors.textPrimary)
        assertEquals(Color(0xFF6B7280), colors.textSecondary)
        assertEquals(Color(0xFF9CA3AF), colors.textDisabled)

        // Semantic - Success
        assertEquals(Color(0xFF16A34A), colors.success)
        assertEquals(Color(0xFFDCFCE7), colors.successBg)
        assertEquals(Color(0xFF166534), colors.successText)

        // Semantic - Warning
        assertEquals(Color(0xFFF59E0B), colors.warning)
        assertEquals(Color(0xFFFEF3C7), colors.warningBg)
        assertEquals(Color(0xFF92400E), colors.warningText)

        // Semantic - Error
        assertEquals(Color(0xFFEF4444), colors.error)
        assertEquals(Color(0xFFFEE2E2), colors.errorBg)
        assertEquals(Color(0xFFB91C1C), colors.errorText)

        // Semantic - Info
        assertEquals(Color(0xFF3B82F6), colors.info)
        assertEquals(Color(0xFFDBEAFE), colors.infoBg)
        assertEquals(Color(0xFF1D4ED8), colors.infoText)

        // Gradient stops: violet -> purple -> deepPurple
        assertEquals(
            listOf(Color(0xFFA855F7), Color(0xFF7C3AED), Color(0xFF4F46E5)),
            colors.brandGradientColors,
        )
    }

    @Test
    fun testMaterialColorSchemeMapping() {
        val colors = HitchyColors()
        val scheme = hitchyLightColorScheme(colors)

        assertEquals(colors.purple, scheme.primary)
        assertEquals(colors.white, scheme.onPrimary)
        assertEquals(colors.lavender, scheme.primaryContainer)
        assertEquals(colors.purple, scheme.onPrimaryContainer)
        assertEquals(colors.deepPurple, scheme.secondary)
        assertEquals(colors.white, scheme.onSecondary)
        assertEquals(colors.pin, scheme.tertiary)
        assertEquals(colors.background, scheme.background)
        assertEquals(colors.textPrimary, scheme.onBackground)
        assertEquals(colors.white, scheme.surface)
        assertEquals(colors.textPrimary, scheme.onSurface)
        assertEquals(colors.lilac, scheme.surfaceVariant)
        assertEquals(colors.textSecondary, scheme.onSurfaceVariant)
        assertEquals(colors.border, scheme.outline)
        assertEquals(colors.error, scheme.error)
        assertEquals(colors.white, scheme.onError)
        assertEquals(colors.errorBg, scheme.errorContainer)
        assertEquals(colors.errorText, scheme.onErrorContainer)
    }

    @Test
    fun testTypographyTokensAndMaterialSlots() {
        val typography = HitchyTypography()
        val material = typography.toMaterialTypography()

        // Display -> displaySmall
        assertEquals(32.sp, typography.display.fontSize)
        assertEquals(40.sp, typography.display.lineHeight)
        assertEquals(FontWeight.Bold, typography.display.fontWeight)
        assertEquals(typography.display, material.displaySmall)

        // H1 -> headlineLarge
        assertEquals(28.sp, typography.h1.fontSize)
        assertEquals(36.sp, typography.h1.lineHeight)
        assertEquals(FontWeight.Bold, typography.h1.fontWeight)
        assertEquals(typography.h1, material.headlineLarge)

        // H2 -> headlineMedium
        assertEquals(24.sp, typography.h2.fontSize)
        assertEquals(32.sp, typography.h2.lineHeight)
        assertEquals(FontWeight.Bold, typography.h2.fontWeight)
        assertEquals(typography.h2, material.headlineMedium)

        // H3 -> headlineSmall
        assertEquals(20.sp, typography.h3.fontSize)
        assertEquals(28.sp, typography.h3.lineHeight)
        assertEquals(FontWeight.Bold, typography.h3.fontWeight)
        assertEquals(typography.h3, material.headlineSmall)

        // Amount -> no Material slot; Figma's Cairo 800 24/30
        assertEquals(24.sp, typography.amount.fontSize)
        assertEquals(30.sp, typography.amount.lineHeight)
        assertEquals(FontWeight.ExtraBold, typography.amount.fontWeight)

        // Title -> titleLarge
        assertEquals(18.sp, typography.title.fontSize)
        assertEquals(26.sp, typography.title.lineHeight)
        assertEquals(FontWeight.Bold, typography.title.fontWeight)
        assertEquals(typography.title, material.titleLarge)

        // Body Large -> bodyLarge
        assertEquals(16.sp, typography.bodyLarge.fontSize)
        assertEquals(24.sp, typography.bodyLarge.lineHeight)
        assertEquals(FontWeight.Normal, typography.bodyLarge.fontWeight)
        assertEquals(typography.bodyLarge, material.bodyLarge)

        // Body Large SemiBold -> titleMedium
        assertEquals(16.sp, typography.bodyLargeStrong.fontSize)
        assertEquals(24.sp, typography.bodyLargeStrong.lineHeight)
        assertEquals(FontWeight.SemiBold, typography.bodyLargeStrong.fontWeight)
        assertEquals(typography.bodyLargeStrong, material.titleMedium)

        // Body -> bodyMedium
        assertEquals(14.sp, typography.body.fontSize)
        assertEquals(22.sp, typography.body.lineHeight)
        assertEquals(FontWeight.Normal, typography.body.fontWeight)
        assertEquals(typography.body, material.bodyMedium)

        // Body SemiBold -> titleSmall
        assertEquals(14.sp, typography.bodyStrong.fontSize)
        assertEquals(22.sp, typography.bodyStrong.lineHeight)
        assertEquals(FontWeight.SemiBold, typography.bodyStrong.fontWeight)
        assertEquals(typography.bodyStrong, material.titleSmall)

        // Caption -> bodySmall
        assertEquals(12.sp, typography.caption.fontSize)
        assertEquals(18.sp, typography.caption.lineHeight)
        assertEquals(FontWeight.Normal, typography.caption.fontWeight)
        assertEquals(typography.caption, material.bodySmall)

        // Body Bold -> no Material slot; Figma's Cairo 700 14/22
        assertEquals(14.sp, typography.bodyBold.fontSize)
        assertEquals(22.sp, typography.bodyBold.lineHeight)
        assertEquals(FontWeight.Bold, typography.bodyBold.fontWeight)

        // Caption Bold -> labelSmall
        assertEquals(12.sp, typography.captionStrong.fontSize)
        assertEquals(18.sp, typography.captionStrong.lineHeight)
        assertEquals(FontWeight.Bold, typography.captionStrong.fontWeight)
        assertEquals(typography.captionStrong, material.labelSmall)

        // Button -> labelLarge
        assertEquals(16.sp, typography.button.fontSize)
        assertEquals(24.sp, typography.button.lineHeight)
        assertEquals(FontWeight.Bold, typography.button.fontWeight)
        assertEquals(typography.button, material.labelLarge)
    }

    @Test
    fun testSpacingTokens() {
        val spacing = HitchySpacing()

        assertEquals(4.dp, spacing.space4)
        assertEquals(8.dp, spacing.space8)
        assertEquals(12.dp, spacing.space12)
        assertEquals(16.dp, spacing.space16)
        assertEquals(24.dp, spacing.space24)
        assertEquals(32.dp, spacing.space32)
        assertEquals(40.dp, spacing.space40)
        assertEquals(48.dp, spacing.space48)
        assertEquals(16.dp, spacing.screenPadding)
        assertEquals(48.dp, spacing.minTouchTarget)
        assertEquals(24.dp, spacing.iconSize)
    }

    @Test
    fun testShapeTokens() {
        val shapes = HitchyShapes()
        val material = shapes.toMaterialShapes()

        assertEquals(RoundedCornerShape(8.dp), shapes.sm)
        assertEquals(shapes.sm, material.small)

        assertEquals(RoundedCornerShape(12.dp), shapes.md)
        assertEquals(shapes.md, material.medium)

        assertEquals(RoundedCornerShape(16.dp), shapes.lg)
        assertEquals(shapes.lg, material.large)

        assertEquals(RoundedCornerShape(24.dp), shapes.xl)
        assertEquals(shapes.xl, material.extraLarge)

        assertEquals(RoundedCornerShape(50), shapes.pill)
    }
}
