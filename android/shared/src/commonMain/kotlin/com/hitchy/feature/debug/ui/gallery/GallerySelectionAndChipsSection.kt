@file:Suppress("MagicNumber")

package com.hitchy.feature.debug.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.components.Badge
import com.hitchy.core.ui.components.BadgeVariant
import com.hitchy.core.ui.components.CommunityBadge
import com.hitchy.core.ui.components.CommunityBadgeType
import com.hitchy.core.ui.components.FilterChip
import com.hitchy.core.ui.components.HitchyCheckbox
import com.hitchy.core.ui.components.HitchyRadio
import com.hitchy.core.ui.components.HitchySelect
import com.hitchy.core.ui.components.HitchySwitch
import com.hitchy.core.ui.components.RecurringTag
import com.hitchy.core.ui.components.RideStatusVariant
import com.hitchy.core.ui.components.SelectChip
import com.hitchy.core.ui.components.StatusChip
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GallerySelectionAndChipsSection(modifier: Modifier = Modifier) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    val zones = listOf("Maadi", "Nasr City", "New Cairo", "6th of October", "Zamalek")
    var selectedZone by remember { mutableStateOf<String?>(zones[0]) }

    var checkbox1 by remember { mutableStateOf(true) }
    var checkbox2 by remember { mutableStateOf(false) }

    var radioIndex by remember { mutableStateOf(0) }
    val radioOptions = listOf("Standard Car", "Comfort Car", "Scooter")

    var switch1 by remember { mutableStateOf(true) }
    var switch2 by remember { mutableStateOf(false) }

    var filterActive by remember { mutableStateOf(true) }
    var selectChipActive by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.space16),
    ) {
        // Selection Controls
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
                    text = "Selection Controls",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                Text(
                    text = "HitchySelect Dropdown",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                HitchySelect(
                    items = zones,
                    selectedItem = selectedZone,
                    onItemSelected = { selectedZone = it },
                    itemLabel = { it },
                    label = "Pickup Zone",
                )

                Text(
                    text = "Checkboxes & Switches",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space16),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HitchyCheckbox(
                            checked = checkbox1,
                            onCheckedChange = { checkbox1 = it },
                        )
                        Spacer(modifier = Modifier.width(spacing.space4))
                        Text("Option A", style = typography.body)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HitchyCheckbox(
                            checked = checkbox2,
                            onCheckedChange = { checkbox2 = it },
                        )
                        Spacer(modifier = Modifier.width(spacing.space4))
                        Text("Option B", style = typography.body)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space16),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications", style = typography.body)
                        Spacer(modifier = Modifier.width(spacing.space8))
                        HitchySwitch(
                            checked = switch1,
                            onCheckedChange = { switch1 = it },
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Dark Mode", style = typography.body)
                        Spacer(modifier = Modifier.width(spacing.space8))
                        HitchySwitch(
                            checked = switch2,
                            onCheckedChange = { switch2 = it },
                        )
                    }
                }

                Text(
                    text = "Radio Group",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space16),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    radioOptions.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HitchyRadio(
                                selected = radioIndex == index,
                                onClick = { radioIndex = index },
                            )
                            Spacer(modifier = Modifier.width(spacing.space4))
                            Text(option, style = typography.body)
                        }
                    }
                }
            }
        }

        // Chips & Tags
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
                    text = "Chips & Tags",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                Text(
                    text = "FilterChip & SelectChip",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    FilterChip(
                        label = "Women only",
                        selected = filterActive,
                        onClick = { filterActive = !filterActive },
                    )
                    FilterChip(
                        label = "Morning (7-9 AM)",
                        selected = !filterActive,
                        onClick = { filterActive = !filterActive },
                    )
                    SelectChip(
                        label = "Sun - Thu",
                        selected = selectChipActive,
                        onClick = { selectChipActive = !selectChipActive },
                    )
                    SelectChip(
                        label = "One-time",
                        selected = !selectChipActive,
                        onClick = { selectChipActive = !selectChipActive },
                    )
                    RecurringTag(scheduleText = "Daily ride · Sun – Thu")
                }

                Text(
                    text = "StatusChip (All 8 Variants)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    RideStatusVariant.entries.forEach { variant ->
                        StatusChip(status = variant)
                    }
                }

                Text(
                    text = "Badges & Community Badges",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    Badge(variant = BadgeVariant.Verified)
                    Badge(variant = BadgeVariant.Pending)
                    CommunityBadge(type = CommunityBadgeType.WomenOnly)
                    CommunityBadge(type = CommunityBadgeType.SameCompany)
                    CommunityBadge(type = CommunityBadgeType.BronzeTier)
                    CommunityBadge(type = CommunityBadgeType.SilverTier)
                    CommunityBadge(type = CommunityBadgeType.GoldTier)
                }
            }
        }
    }
}
