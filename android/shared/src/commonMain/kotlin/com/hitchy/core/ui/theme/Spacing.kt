@file:Suppress("MatchingDeclarationName")

package com.hitchy.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class HitchySpacing(
    val space4: Dp = 4.dp,
    val space8: Dp = 8.dp,
    val space12: Dp = 12.dp,
    val space16: Dp = 16.dp,
    val space24: Dp = 24.dp,
    val space32: Dp = 32.dp,
    val space40: Dp = 40.dp,
    val space48: Dp = 48.dp,
    val screenPadding: Dp = 16.dp,
    val minTouchTarget: Dp = 48.dp,
    val iconSize: Dp = 24.dp,
)

val LocalHitchySpacing = staticCompositionLocalOf { HitchySpacing() }
