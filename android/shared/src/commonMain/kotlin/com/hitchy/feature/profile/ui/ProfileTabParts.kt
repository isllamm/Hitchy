@file:Suppress("MagicNumber")

package com.hitchy.feature.profile.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.components.Avatar
import com.hitchy.core.ui.components.AvatarSize
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.profile.domain.User
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.format_egp
import hitchy.shared.generated.resources.profile_design_rating
import hitchy.shared.generated.resources.profile_wallet_balance_label
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val CardRadius = 20.dp
private val HeaderRadius = 28.dp

/** Gradient header of the Profile tab: initials avatar, full name and the rating line. */
@Composable
internal fun ProfileHeader(profile: User?) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = HeaderRadius, bottomEnd = HeaderRadius))
            .background(colors.brandGradient)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (profile != null) {
            Avatar(initials = profile.firstName.take(1), size = AvatarSize.LG)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${profile.firstName} ${profile.lastName}",
                    style = typography.h2,
                    color = colors.white,
                )
                val ratingAvg = profile.rider.ratingAvg
                if (ratingAvg != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            painter = painterResource(HitchyIcons.Star),
                            contentDescription = null,
                            tint = colors.warning,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(
                                Res.string.profile_design_rating,
                                ratingAvg.toString().take(3),
                                profile.rider.ratingCount,
                            ),
                            style = typography.caption,
                            color = colors.white,
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.size(AvatarSize.LG.dp))
        }
    }
}

/** The wallet balance (demo) as a Figma profile section: 12/18 label, the amount in Cairo 800 24/30 purple. */
@Composable
internal fun WalletCard(balanceEgp: Int) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    FigmaSection {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.profile_wallet_balance_label),
                style = typography.caption,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(Res.string.format_egp, balanceEgp.toString()),
                style = typography.amount,
                color = colors.purple,
            )
        }
    }
}

/** A white card with a 1dp border, a bold title and a column of rows. */
@Composable
internal fun ProfileCard(
    title: String,
    content: @Composable () -> Unit,
) {
    val colors = HitchyTheme.colors
    val shape = RoundedCornerShape(CardRadius)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .background(colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = title, style = HitchyTheme.typography.bodyStrong, color = colors.navy)
        content()
    }
}

/** One tappable row: a 40dp lilac tile with an icon, the label, and a chevron. */
@Composable
internal fun ProfileRow(
    icon: DrawableResource,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RowTile(icon = icon, tileColor = colors.lavender, tint = colors.purple)
        Text(
            text = label,
            style = HitchyTheme.typography.body,
            color = colors.navy,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(HitchyIcons.ChevronDown),
            contentDescription = null,
            tint = colors.navy,
            modifier = Modifier
                .size(20.dp)
                .rotate(if (LocalLayoutDirection.current == LayoutDirection.Rtl) 90f else -90f),
        )
    }
}

/** The red sign-out row at the bottom of the screen. */
@Composable
internal fun SignOutRow(
    label: String,
    onClick: () -> Unit,
) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RowTile(icon = HitchyIcons.Back, tileColor = colors.errorBg, tint = colors.error)
        Text(text = label, style = HitchyTheme.typography.bodyStrong, color = colors.error)
    }
}

@Composable
private fun RowTile(
    icon: DrawableResource,
    tileColor: Color,
    tint: Color,
) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(tileColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}
