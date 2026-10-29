@file:Suppress("MatchingDeclarationName", "MagicNumber")

package com.hitchy.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

@Immutable
data class HitchyShapes(
    val sm: RoundedCornerShape = RoundedCornerShape(8.dp),
    val md: RoundedCornerShape = RoundedCornerShape(12.dp),
    val lg: RoundedCornerShape = RoundedCornerShape(16.dp),
    val xl: RoundedCornerShape = RoundedCornerShape(24.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(50),
)

fun HitchyShapes.toMaterialShapes(): Shapes = Shapes(
    small = sm,
    medium = md,
    large = lg,
    extraLarge = xl,
)

val LocalHitchyShapes = staticCompositionLocalOf { HitchyShapes() }
