@file:Suppress("MagicNumber")

package com.hitchy.feature.debug.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.components.FilterChip
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.gallery_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class GalleryCategory(val label: String) {
    All("All"),
    Tokens("Tokens"),
    ButtonsAndInputs("Buttons & Inputs"),
    SelectionAndChips("Selection & Chips"),
    NavigationAndUser("Navigation & User"),
    CardsAndStates("Cards & States"),
    Map("Map"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentGalleryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isArabic by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(GalleryCategory.All) }

    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        val colors = LocalHitchyColors.current
        val typography = LocalHitchyTypography.current
        val spacing = LocalHitchySpacing.current

        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isArabic) "معرض المكونات" else stringResource(Res.string.gallery_title),
                            style = typography.h2,
                            color = colors.textPrimary,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Text(
                                text = if (isArabic) "→" else "←",
                                style = typography.h2,
                                color = colors.textPrimary,
                            )
                        }
                    },
                    actions = {
                        FilterChip(
                            label = if (isArabic) "عربي (RTL)" else "English (LTR)",
                            selected = isArabic,
                            onClick = { isArabic = !isArabic },
                            modifier = Modifier.padding(end = spacing.space8),
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.white,
                    ),
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(innerPadding),
            ) {
                // Category Filter Bar
                Surface(
                    color = colors.white,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = spacing.space16, vertical = spacing.space8),
                        horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GalleryCategory.entries.forEach { category ->
                            FilterChip(
                                label = category.label,
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                            )
                        }
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(spacing.space16),
                    verticalArrangement = Arrangement.spacedBy(spacing.space24),
                ) {
                    if (selectedCategory == GalleryCategory.All || selectedCategory == GalleryCategory.Tokens) {
                        GalleryTokensSection()
                    }

                    if (selectedCategory == GalleryCategory.All ||
                        selectedCategory == GalleryCategory.ButtonsAndInputs
                    ) {
                        GalleryButtonsAndInputsSection()
                    }

                    if (selectedCategory == GalleryCategory.All ||
                        selectedCategory == GalleryCategory.SelectionAndChips
                    ) {
                        GallerySelectionAndChipsSection()
                    }

                    if (selectedCategory == GalleryCategory.All ||
                        selectedCategory == GalleryCategory.NavigationAndUser
                    ) {
                        GalleryNavigationAndUserSection()
                    }

                    if (selectedCategory == GalleryCategory.All ||
                        selectedCategory == GalleryCategory.CardsAndStates
                    ) {
                        GalleryCardsAndStatesSection()
                    }

                    if (selectedCategory == GalleryCategory.All || selectedCategory == GalleryCategory.Map) {
                        GalleryMapSection()
                    }

                    Spacer(modifier = Modifier.height(spacing.space32))
                }
            }
        }
    }
}

@Preview
@Composable
fun ComponentGalleryScreenEnglishPreview() {
    HitchyTheme {
        ComponentGalleryScreen(onBack = {})
    }
}

@Preview
@Composable
fun ComponentGalleryScreenArabicPreview() {
    HitchyTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ComponentGalleryScreen(onBack = {})
        }
    }
}
