package com.hitchy.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.cairo_bold
import hitchy.shared.generated.resources.cairo_regular
import hitchy.shared.generated.resources.cairo_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun cairoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.cairo_regular, weight = FontWeight.Normal),
    Font(Res.font.cairo_semibold, weight = FontWeight.SemiBold),
    Font(Res.font.cairo_bold, weight = FontWeight.Bold),
)

internal val DefaultPlatformTextStyle = PlatformTextStyle(includeFontPadding = false)

@Immutable
data class HitchyTypography(
    val display: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val title: TextStyle,
    val bodyLarge: TextStyle,
    val bodyLargeStrong: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val caption: TextStyle,
    val captionStrong: TextStyle,
    val button: TextStyle,
) {
    constructor(fontFamily: FontFamily = FontFamily.Default) : this(
        display = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        h1 = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        h2 = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        h3 = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        title = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 26.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        bodyLargeStrong = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        body = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        bodyStrong = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        caption = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        captionStrong = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
        button = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            platformStyle = DefaultPlatformTextStyle,
        ),
    )
}

@Composable
fun rememberHitchyTypography(
    fontFamily: FontFamily = cairoFontFamily(),
): HitchyTypography = HitchyTypography(fontFamily = fontFamily)

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
