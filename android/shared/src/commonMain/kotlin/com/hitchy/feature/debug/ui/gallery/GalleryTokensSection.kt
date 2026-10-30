@file:Suppress("MagicNumber")

package com.hitchy.feature.debug.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.icon.IconGallery
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalleryTokensSection(modifier: Modifier = Modifier) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.space16),
    ) {
        // Brand Gradient Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(spacing.space16)) {
                Text(
                    text = "Brand Gradient",
                    style = typography.h3,
                    color = colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(spacing.space8))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.brandGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Hitchy Brand Gradient (#A855F7 → #4F46E5)",
                        style = typography.bodyStrong,
                        color = colors.white,
                    )
                }
            }
        }

        // Color Palette Swatches
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                Text(
                    text = "Color Palette",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                Text(
                    text = "Brand & Neutral Colors",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    ColorSwatch("Purple", colors.purple, "#7C3AED")
                    ColorSwatch("DeepPurple", colors.deepPurple, "#4F46E5")
                    ColorSwatch("Navy", colors.navy, "#1E1B4B")
                    ColorSwatch("Violet", colors.violet, "#A855F7")
                    ColorSwatch("Pin", colors.pin, "#FF3366")
                    ColorSwatch("Lavender", colors.lavender, "#EDE9FE")
                    ColorSwatch("Lilac", colors.lilac, "#F5F3FF")
                    ColorSwatch("White", colors.white, "#FFFFFF")
                    ColorSwatch("Background", colors.background, "#F8F9FC")
                    ColorSwatch("Border", colors.border, "#E5E7EB")
                }

                Text(
                    text = "Text Hierarchy",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    ColorSwatch("Text Primary", colors.textPrimary, "#1E1B4B")
                    ColorSwatch("Text Secondary", colors.textSecondary, "#6B7280")
                    ColorSwatch("Text Disabled", colors.textDisabled, "#9CA3AF")
                }

                Text(
                    text = "Semantic Signals",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    ColorSwatch("Success", colors.success, "#16A34A")
                    ColorSwatch("Success Bg", colors.successBg, "#DCFCE7")
                    ColorSwatch("Warning", colors.warning, "#F59E0B")
                    ColorSwatch("Warning Bg", colors.warningBg, "#FEF3C7")
                    ColorSwatch("Error", colors.error, "#EF4444")
                    ColorSwatch("Error Bg", colors.errorBg, "#FEE2E2")
                    ColorSwatch("Info", colors.info, "#3B82F6")
                    ColorSwatch("Info Bg", colors.infoBg, "#DBEAFE")
                }
            }
        }

        // Typography Scale (Cairo font)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                Text(
                    text = "Typography Scale (Cairo Font)",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                TypographyRow("Display (32/40 Bold)", typography.display)
                TypographyRow("H1 (28/36 Bold)", typography.h1)
                TypographyRow("H2 (24/32 Bold)", typography.h2)
                TypographyRow("H3 (20/28 Bold)", typography.h3)
                TypographyRow("Title (18/24 SemiBold)", typography.title)
                TypographyRow("BodyLarge (16/24 Regular)", typography.bodyLarge)
                TypographyRow("BodyLargeStrong (16/24 SemiBold)", typography.bodyLargeStrong)
                TypographyRow("Body (14/20 Regular)", typography.body)
                TypographyRow("BodyStrong (14/20 SemiBold)", typography.bodyStrong)
                TypographyRow("Caption (12/16 Regular)", typography.caption)
                TypographyRow("CaptionStrong (12/16 SemiBold)", typography.captionStrong)
                TypographyRow("Button (16/20 Bold)", typography.button)
            }
        }

        // Hitchy Icons Catalog
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(spacing.space16)) {
                Text(
                    text = "Hitchy Icons (32 Drawables)",
                    style = typography.h3,
                    color = colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(spacing.space8))
                IconGallery(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    label: String,
    color: Color,
    hex: String,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    Column(
        modifier = Modifier
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .padding(spacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, colors.border, CircleShape),
        )
        Text(
            text = label,
            style = typography.caption,
            color = colors.textPrimary,
        )
        Text(
            text = hex,
            style = typography.caption,
            color = colors.textDisabled,
        )
    }
}

@Composable
private fun TypographyRow(
    styleName: String,
    style: TextStyle,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .padding(spacing.space12),
        verticalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        Text(
            text = styleName,
            style = typography.caption,
            color = colors.textSecondary,
        )
        Text(
            text = "Greater Cairo Commuters",
            style = style,
            color = colors.textPrimary,
        )
        Text(
            text = "التنقل الذكي في القاهرة الكبرى",
            style = style,
            color = colors.purple,
        )
    }
}
