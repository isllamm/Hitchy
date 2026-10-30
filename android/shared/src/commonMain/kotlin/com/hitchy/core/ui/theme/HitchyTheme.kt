package com.hitchy.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

fun hitchyLightColorScheme(colors: HitchyColors = HitchyColors()): ColorScheme = lightColorScheme(
    primary = colors.purple,
    onPrimary = colors.white,
    primaryContainer = colors.lavender,
    onPrimaryContainer = colors.purple,
    secondary = colors.deepPurple,
    onSecondary = colors.white,
    secondaryContainer = colors.lilac,
    onSecondaryContainer = colors.navy,
    tertiary = colors.pin,
    onTertiary = colors.white,
    background = colors.background,
    onBackground = colors.textPrimary,
    surface = colors.white,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.lilac,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.border,
    error = colors.error,
    onError = colors.white,
    errorContainer = colors.errorBg,
    onErrorContainer = colors.errorText,
)

/**
 * Hitchy app theme.
 *
 * Light theme only [DECISION-05].
 */
@Composable
fun HitchyTheme(
    colors: HitchyColors = HitchyColors(),
    typography: HitchyTypography = rememberHitchyTypography(),
    shapes: HitchyShapes = HitchyShapes(),
    spacing: HitchySpacing = HitchySpacing(),
    content: @Composable () -> Unit,
) {
    val materialColorScheme = hitchyLightColorScheme(colors)
    val materialTypography = typography.toMaterialTypography()
    val materialShapes = shapes.toMaterialShapes()

    CompositionLocalProvider(
        LocalHitchyColors provides colors,
        LocalHitchyTypography provides typography,
        LocalHitchyShapes provides shapes,
        LocalHitchySpacing provides spacing,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = materialTypography,
            shapes = materialShapes,
            content = content,
        )
    }
}

object HitchyTheme {
    val colors: HitchyColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHitchyColors.current

    val typography: HitchyTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalHitchyTypography.current

    val shapes: HitchyShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalHitchyShapes.current

    val spacing: HitchySpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalHitchySpacing.current
}
