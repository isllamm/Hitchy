package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.role_host
import hitchy.shared.generated.resources.role_host_desc
import hitchy.shared.generated.resources.role_rider
import hitchy.shared.generated.resources.role_rider_desc
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class AppRoleType {
    Rider,
    Host,
}

@Composable
fun RoleCard(
    role: AppRoleType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val cardShape = RoundedCornerShape(16.dp)

    val title = when (role) {
        AppRoleType.Rider -> stringResource(Res.string.role_rider)
        AppRoleType.Host -> stringResource(Res.string.role_host)
    }
    val description = when (role) {
        AppRoleType.Rider -> stringResource(Res.string.role_rider_desc)
        AppRoleType.Host -> stringResource(Res.string.role_host_desc)
    }
    val icon: DrawableResource = when (role) {
        AppRoleType.Rider -> HitchyIcons.Person
        AppRoleType.Host -> HitchyIcons.Car
    }

    // Figma: 361x96, pad 16/24, r=16. Unselected white on a #E5E7EB stroke; selected #EDE9FE on purple.
    val borderColor = if (selected) colors.purple else colors.border
    val bgColor = if (selected) colors.lavender else colors.white

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(cardShape)
            .border(1.3.dp, borderColor, cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        color = bgColor,
    ) {
        Row(
            modifier = Modifier.padding(vertical = spacing.space16, horizontal = spacing.space24),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.lilac),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = colors.purple,
                        modifier = Modifier.size(30.dp),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(spacing.space4)) {
                    Text(
                        text = title,
                        style = typography.h3,
                        color = colors.navy,
                    )
                    Text(
                        text = description,
                        style = typography.bodyLarge,
                        color = colors.textSecondary,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.purple else Color.Transparent)
                    .border(1.32.dp, if (selected) colors.purple else colors.border, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        painter = painterResource(HitchyIcons.Check),
                        contentDescription = null,
                        tint = colors.white,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RoleCardLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                RoleCard(role = AppRoleType.Rider, selected = true, onClick = {})
                RoleCard(role = AppRoleType.Host, selected = false, onClick = {})
            }
        }
    }
}

@Preview
@Composable
fun RoleCardRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        RoleCardLtrPreview()
    }
}
