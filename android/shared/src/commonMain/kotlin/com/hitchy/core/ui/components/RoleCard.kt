package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

    val borderColor = if (selected) colors.purple else colors.border
    val borderWidth = if (selected) 2.dp else 1.dp
    val bgColor = if (selected) colors.lilac else colors.white

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(borderWidth, borderColor, cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        color = bgColor,
    ) {
        Row(
            modifier = Modifier.padding(spacing.space16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space16),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.lavender else colors.background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = if (selected) colors.purple else colors.navy,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = typography.title,
                    color = colors.navy,
                )
                Text(
                    text = description,
                    style = typography.body,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colors.purple),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(HitchyIcons.Check),
                        contentDescription = null,
                        tint = colors.white,
                        modifier = Modifier.size(14.dp),
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
