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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
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

private val BarShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

/**
 * Figma's `_Group tab bar` (component 277:996): 76dp tall, r=16 on top, a soft shadow, four
 * 66dp tabs. The open tab shows its bold icon, a Bold purple label and a 24x3 pill under it.
 */
@Composable
fun BottomNav(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
    userInitials: String? = null,
) {
    val colors = LocalHitchyColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .figmaShadow(BarShape, Color.Black.copy(alpha = SHADOW_ALPHA), blur = 20.dp, y = 2.dp),
        color = colors.white,
        shape = BarShape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(76.dp)
                .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
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

/** Figma's tab bar shadow: black at 10%. */
private const val SHADOW_ALPHA = 0.10f

@Composable
private fun RowScope.NavItemCell(
    item: BottomNavItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    userInitials: String?,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current

    val label = when (item) {
        BottomNavItem.Home -> stringResource(Res.string.nav_home)
        BottomNavItem.Rides -> stringResource(Res.string.nav_rides)
        BottomNavItem.Shop -> stringResource(Res.string.nav_shop)
        BottomNavItem.Profile -> stringResource(Res.string.nav_profile)
    }
    val icon: DrawableResource = when (item) {
        BottomNavItem.Home -> if (isSelected) HitchyIcons.TabHomeOn else HitchyIcons.TabHomeOff
        BottomNavItem.Rides -> if (isSelected) HitchyIcons.TabRidesOn else HitchyIcons.TabRidesOff
        BottomNavItem.Shop -> if (isSelected) HitchyIcons.TabShopOn else HitchyIcons.TabShopOff
        BottomNavItem.Profile -> HitchyIcons.Profile
    }
    val tint = if (isSelected) colors.purple else colors.textSecondary

    Box(
        modifier = Modifier
            .weight(1f)
            .height(66.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (item == BottomNavItem.Profile) {
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(colors.lavender),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!userInitials.isNullOrBlank()) {
                        Text(text = userInitials, style = typography.captionStrong, color = colors.purple)
                    } else {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            tint = colors.purple,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            } else {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = label,
                style = if (isSelected) typography.bodyBold else typography.body,
                color = tint,
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 63.dp)
                    .size(width = 24.dp, height = 3.dp)
                    .clip(CircleShape)
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
