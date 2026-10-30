@file:Suppress("MagicNumber")

package com.hitchy.feature.debug.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.components.AvatarSize
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.BottomNav
import com.hitchy.core.ui.components.BottomNavItem
import com.hitchy.core.ui.components.GreetingHeader
import com.hitchy.core.ui.components.HitchyTabs
import com.hitchy.core.ui.components.HostRow
import com.hitchy.core.ui.components.RatingDisplay
import com.hitchy.core.ui.components.RatingStarsInput
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalleryNavigationAndUserSection(modifier: Modifier = Modifier) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    val tabs = listOf("Upcoming", "Completed", "Cancelled")
    var selectedTab by remember { mutableStateOf(0) }

    var bottomNavItem by remember { mutableStateOf(BottomNavItem.Home) }
    var interactiveRating by remember { mutableStateOf(4) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.space16),
    ) {
        // Headers & Navigation
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
                    text = "Headers & Navigation",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                Text(
                    text = "TopBar",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                TopBar(
                    title = "Ride Details",
                    onBackClick = {},
                )

                Text(
                    text = "GreetingHeader",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                GreetingHeader(
                    firstName = "Ahmed",
                    onNotificationsClick = {},
                )

                Text(
                    text = "BackButton Styles",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.space16)) {
                    BackButton(onClick = {}, style = BackButtonStyle.Plain)
                    BackButton(onClick = {}, style = BackButtonStyle.TonalCircle)
                }

                Text(
                    text = "HitchyTabs (Interactive)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                HitchyTabs(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                )

                Text(
                    text = "BottomNav Bar",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                BottomNav(
                    selectedItem = bottomNavItem,
                    onItemSelected = { bottomNavItem = it },
                    userInitials = "AM",
                )
            }
        }

        // User & Ratings
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
                    text = "User & Ratings",
                    style = typography.h3,
                    color = colors.textPrimary,
                )

                Text(
                    text = "Avatar Sizes (XS, SM, MD, LG)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space16),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(initials = "AM", size = AvatarSize.XS)
                    Avatar(initials = "AM", size = AvatarSize.SM)
                    Avatar(initials = "AM", size = AvatarSize.MD)
                    Avatar(initials = "AM", size = AvatarSize.LG)
                }

                Text(
                    text = "Rating Displays",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space16),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    RatingDisplay(rating = 4.9)
                    RatingDisplay(rating = 4.85, rideCount = 142)
                }

                Text(
                    text = "RatingStarsInput (Interactive)",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                RatingStarsInput(
                    rating = interactiveRating,
                    onRatingChange = { interactiveRating = it },
                )

                Text(
                    text = "HostRow",
                    style = typography.bodyStrong,
                    color = colors.textSecondary,
                )
                HostRow(
                    name = "Ahmed Mahmoud",
                    initials = "AM",
                    rating = 4.9,
                    rideCount = 142,
                )
            }
        }
    }
}
