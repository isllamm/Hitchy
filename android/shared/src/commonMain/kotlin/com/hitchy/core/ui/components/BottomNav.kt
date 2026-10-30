package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.nav_home
import hitchy.shared.generated.resources.nav_profile
import hitchy.shared.generated.resources.nav_rides
import hitchy.shared.generated.resources.nav_shop
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class BottomNavItem {
    Home,
    Rides,
    Shop,
    Profile,
}

@Composable
fun BottomNav(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
    userInitials: String? = null,
) {
    val colors = LocalHitchyColors.current
    val spacing = LocalHitchySpacing.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.white,
    ) {
        Column {
            HorizontalDivider(color = colors.border, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = spacing.space8),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                BottomNavItem.entries.forEach { item ->
                    NavItemCell(
                        item = item,
                        isSelected = item == selectedItem,
                        onSelect = { onItemSelected(item) },
                        userInitials = userInitials,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavItemCell(
    item: BottomNavItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    userInitials: String?,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    val label = when (item) {
        BottomNavItem.Home -> stringResource(Res.string.nav_home)
        BottomNavItem.Rides -> stringResource(Res.string.nav_rides)
        BottomNavItem.Shop -> stringResource(Res.string.nav_shop)
        BottomNavItem.Profile -> stringResource(Res.string.nav_profile)
    }
    val icon: DrawableResource = when (item) {
        BottomNavItem.Home -> HitchyIcons.Home
        BottomNavItem.Rides -> HitchyIcons.Rides
        BottomNavItem.Shop -> HitchyIcons.Star
        BottomNavItem.Profile -> HitchyIcons.Profile
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clickable(onClick = onSelect)
            .padding(vertical = spacing.space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (item == BottomNavItem.Profile && !userInitials.isNullOrBlank()) {
            Avatar(
                initials = userInitials,
                size = AvatarSize.XS,
            )
        } else {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = if (isSelected) colors.purple else colors.textSecondary,
                modifier = Modifier.size(24.dp),
            )
        }

        Text(
            text = label,
            style = if (isSelected) typography.captionStrong else typography.caption,
            color = if (isSelected) colors.purple else colors.textSecondary,
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(width = 16.dp, height = 2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(colors.purple),
            )
        }
    }
}

@Preview
@Composable
fun BottomNavLtrPreview() {
    HitchyTheme {
        Surface(color = LocalHitchyColors.current.background) {
            BottomNav(
                selectedItem = BottomNavItem.Home,
                onItemSelected = {},
                userInitials = "IA",
            )
        }
    }
}

@Preview
@Composable
fun BottomNavRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BottomNavLtrPreview()
    }
}
