package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val MENU_WIDTH_FRACTION = 0.9f

@Composable
fun <T> HitchySelect(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Select",
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = typography.captionStrong,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = spacing.space4),
            )
        }

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (enabled) colors.white else colors.disabledSurface)
                    .border(
                        width = if (expanded) 2.dp else 1.dp,
                        color = if (expanded) colors.purple else colors.border,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = enabled) { expanded = !expanded }
                    .padding(horizontal = spacing.space16, vertical = spacing.space12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = selectedItem?.let(itemLabel) ?: placeholder,
                    style = typography.body,
                    color = if (selectedItem != null) colors.textPrimary else colors.textDisabled,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(HitchyIcons.ChevronDown),
                    contentDescription = label ?: placeholder,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(MENU_WIDTH_FRACTION)
                    .background(colors.white),
            ) {
                items.forEach { item ->
                    val isSelected = item == selectedItem
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemLabel(item),
                                style = if (isSelected) typography.bodyStrong else typography.body,
                                color = if (isSelected) colors.purple else colors.textPrimary,
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    painter = painterResource(HitchyIcons.Check),
                                    contentDescription = null,
                                    tint = colors.purple,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (isSelected) colors.lavender else colors.white,
                        ),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun HitchySelectLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        val options = listOf("Smart Village", "New Cairo", "Nasr City", "Maadi")
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                HitchySelect(
                    items = options,
                    selectedItem = "Smart Village",
                    onItemSelected = {},
                    itemLabel = { it },
                    label = "Destination Zone",
                )
                HitchySelect(
                    items = options,
                    selectedItem = null,
                    onItemSelected = {},
                    itemLabel = { it },
                    label = "Pickup Zone",
                    placeholder = "Choose a zone",
                )
            }
        }
    }
}

@Preview
@Composable
fun HitchySelectRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HitchySelectLtrPreview()
    }
}
