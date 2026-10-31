package com.hitchy.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Hitchy custom color tokens from BUILD_PLAN section 3.1.
 *
 * Light theme only [DECISION-05].
 *
 * Note: [brandGradient] is reserved for splash, hero, and branding decoration only;
 * buttons remain solid purple.
 */
@Immutable
data class HitchyColors(
    val purple: Color = Purple,
    val deepPurple: Color = DeepPurple,
    val navy: Color = Navy,
    val violet: Color = Violet,
    val pin: Color = Pin,
    val lavender: Color = Lavender,
    val lilac: Color = Lilac,
    val white: Color = White,
    val scrim: Color = Scrim,
    val hairline: Color = Hairline,
    val royal: Color = Royal,
    val pinSoft: Color = PinSoft,
    val pinTint: Color = PinTint,
    val purpleDeep: Color = PurpleDeep,
    val handle: Color = Handle,
    val slate: Color = Slate,
    val mapLand: Color = MapLand,
    val shadowInk: Color = ShadowInk,
    val gray300: Color = Gray300,
    val slate900: Color = Slate900,
    val steel: Color = Steel,
    val tileGray: Color = TileGray,
    val alertRed: Color = AlertRed,
    val periwinkle: Color = Periwinkle,
    val background: Color = Background,
    val border: Color = Border,
    val disabledSurface: Color = DisabledSurface,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val textDisabled: Color = TextDisabled,
    val success: Color = Success,
    val successBg: Color = SuccessBg,
    val successText: Color = SuccessText,
    val warning: Color = Warning,
    val warningBg: Color = WarningBg,
    val warningText: Color = WarningText,
    val error: Color = Error,
    val errorBg: Color = ErrorBg,
    val errorText: Color = ErrorText,
    val info: Color = Info,
    val infoBg: Color = InfoBg,
    val infoText: Color = InfoText,
    val brandGradientColors: List<Color> = listOf(Violet, Purple, DeepPurple),
    val brandGradient: Brush = Brush.linearGradient(colors = brandGradientColors),
)

val LocalHitchyColors = staticCompositionLocalOf { HitchyColors() }
