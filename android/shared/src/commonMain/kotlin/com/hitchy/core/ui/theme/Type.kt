package com.hitchy.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal val HitchyFontFamily: FontFamily = FontFamily.Default

private val DefaultPlatformTextStyle = PlatformTextStyle(includeFontPadding = false)

@Immutable
data class HitchyTypography(
    val display: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val h1: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val h2: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val h3: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val title: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val bodyLargeStrong: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val body: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val bodyStrong: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val captionStrong: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
    val button: TextStyle = TextStyle(
        fontFamily = HitchyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        platformStyle = DefaultPlatformTextStyle,
    ),
)

fun HitchyTypography.toMaterialTypography(): Typography = Typography(
    displaySmall = display,
    headlineLarge = h1,
    headlineMedium = h2,
    headlineSmall = h3,
    titleLarge = title,
    titleMedium = bodyLargeStrong,
    titleSmall = bodyStrong,
    bodyLarge = bodyLarge,
    bodyMedium = body,
    bodySmall = caption,
    labelLarge = button,
    labelSmall = captionStrong,
)

val LocalHitchyTypography = staticCompositionLocalOf { HitchyTypography() }
