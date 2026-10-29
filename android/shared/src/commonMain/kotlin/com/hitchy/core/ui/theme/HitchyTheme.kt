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
    tertiary = colors.pin,
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
 * Main theme composable for Hitchy.
 *
 * Light theme only [DECISION-05].
 */
@Composable
fun HitchyTheme(
    colors: HitchyColors = HitchyColors(),
    typography: HitchyTypography = HitchyTypography(),
    spacing: HitchySpacing = HitchySpacing(),
    shapes: HitchyShapes = HitchyShapes(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalHitchyColors provides colors,
        LocalHitchyTypography provides typography,
        LocalHitchySpacing provides spacing,
        LocalHitchyShapes provides shapes,
    ) {
        MaterialTheme(
            colorScheme = hitchyLightColorScheme(colors),
            typography = typography.toMaterialTypography(),
            shapes = shapes.toMaterialShapes(),
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

    val spacing: HitchySpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalHitchySpacing.current

    val shapes: HitchyShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalHitchyShapes.current
}
