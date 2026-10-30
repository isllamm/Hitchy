package com.hitchy.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HitchyTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = spacing.space12),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = title,
                        style = if (isSelected) typography.bodyStrong else typography.body,
                        color = if (isSelected) colors.purple else colors.textSecondary,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(colors.border),
        ) {
            if (tabs.isNotEmpty() && selectedIndex in tabs.indices) {
                val tabWidthFraction = 1f / tabs.size
                Box(
                    modifier = Modifier
                        .fillMaxWidth(tabWidthFraction)
                        .height(2.dp)
                        .align(
                            when (selectedIndex) {
                                0 -> Alignment.CenterStart
                                tabs.size - 1 -> Alignment.CenterEnd
                                else -> Alignment.Center
                            },
                        )
                        .background(colors.purple),
                )
            }
        }
    }
}

@Preview
@Composable
fun HitchyTabsLtrPreview() {
    HitchyTheme {
        Surface(color = LocalHitchyColors.current.background) {
            HitchyTabs(
                tabs = listOf("Upcoming", "Completed", "Cancelled"),
                selectedIndex = 0,
                onTabSelected = {},
            )
        }
    }
}

@Preview
@Composable
fun HitchyTabsRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchyTheme {
            Surface(color = LocalHitchyColors.current.background) {
                HitchyTabs(
                    tabs = listOf("القادمة", "المكتملة", "الملغاة"),
                    selectedIndex = 0,
                    onTabSelected = {},
                )
            }
        }
    }
}
