@file:Suppress("TooManyFunctions")

package com.hitchy.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.profile_edit
import hitchy.shared.generated.resources.profile_identity_verified
import hitchy.shared.generated.resources.profile_stat_mode
import hitchy.shared.generated.resources.profile_stat_rides
import hitchy.shared.generated.resources.profile_travel_safely
import hitchy.shared.generated.resources.profile_travel_safely_sub
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Figma frames 398:6594 "profile 2" and 398:5806 (the avatar card).
private val CardShape = RoundedCornerShape(16.dp)

/** Figma's profile section: r=16, a 1.3dp #E5E7EB stroke and a 4% navy shadow 2dp down. */
@Composable
internal fun FigmaSection(
    modifier: Modifier = Modifier,
    background: Brush? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = HitchyTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .figmaShadow(CardShape, colors.navy.copy(alpha = SHADOW_ALPHA), blur = 8.dp, y = 2.dp)
            .clip(CardShape)
            .then(if (background != null) Modifier.background(background) else Modifier.background(colors.white))
            .border(1.3.dp, colors.border, CardShape),
        content = content,
    )
}

/** What the profile card's stats row shows: the rating ("—" without one), finished rides and the mode. */
internal data class ProfileStats(val rating: String, val rides: Int, val mode: String)

/**
 * Figma's profile card (398:6705, 354x220): a white-to-lilac gradient, the 72dp gradient avatar with a white
 * ring and a lavender halo, the name (Bold 20/28), a 12/18 line, the green "Identity verified" badge (only
 * for a verified account) and "Edit Profile" on the end side; under a rule, three stat columns split by
 * lines: rating with its star, rides, and the mode.
 */
@Composable
internal fun AvatarCard(
    initials: String,
    name: String,
    detail: String,
    verified: Boolean,
    stats: ProfileStats,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    FigmaSection(background = Brush.linearGradient(listOf(colors.white.copy(alpha = HALF), colors.lilac))) {
        Row(
            modifier = Modifier.fillMaxWidth().height(148.dp).padding(start = 21.dp, end = 12.dp, top = 21.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(colors.lavender)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(colors.white)
                    .padding(AVATAR_RING)
                    .clip(CircleShape)
                    .background(colors.brandGradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = initials, style = typography.h3, color = colors.white)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = typography.h3, color = colors.navy, maxLines = 1)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = detail,
                        style = typography.caption,
                        color = colors.textSecondary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    EditProfileLink()
                }
                if (verified) VerifiedBadge(modifier = Modifier.padding(top = 7.dp))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        StatsRow(stats)
    }
}

/** Figma's "Edit Profile": a 14dp pen and Bold 13/24 purple. No edit screen yet, so it does nothing. */
@Composable
private fun EditProfileLink() {
    val colors = HitchyTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            painter = painterResource(HitchyIcons.Edit14),
            contentDescription = null,
            tint = colors.purple,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(Res.string.profile_edit),
            style = HitchyTheme.typography.captionStrong.copy(fontSize = 13.sp, lineHeight = 24.sp),
            color = colors.purple,
        )
    }
}

/** Figma's badge: #DCFCE7, padding 3/9, a 13dp check and SemiBold 11/18 #166534. */
@Composable
private fun VerifiedBadge(modifier: Modifier = Modifier) {
    val colors = HitchyTheme.colors
    Row(
        modifier = modifier.clip(CircleShape).background(colors.successBg).padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            painter = painterResource(HitchyIcons.Check13),
            contentDescription = null,
            tint = colors.successText,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = stringResource(Res.string.profile_identity_verified),
            style = HitchyTheme.typography.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = colors.successText,
        )
    }
}

/** Figma's stats row: 72dp tall, columns 98 / 130 / 126 wide split by 1dp lines, Bold 16/26 over 12/18 gray. */
@Composable
private fun StatsRow(stats: ProfileStats) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val value = typography.bodyLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 26.sp)
    Row(modifier = Modifier.fillMaxWidth().height(72.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(RATING_WEIGHT), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stats.rating, style = value, color = colors.navy)
            Icon(
                painter = painterResource(HitchyIcons.Star20),
                contentDescription = null,
                tint = colors.warningText,
                modifier = Modifier.size(20.dp),
            )
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(colors.border))
        Column(modifier = Modifier.weight(RIDES_WEIGHT), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stats.rides.toString(), style = value, color = colors.navy)
            Text(
                text = stringResource(Res.string.profile_stat_rides),
                style = typography.caption,
                color = colors.textSecondary,
            )
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(colors.border))
        Column(modifier = Modifier.weight(MODE_WEIGHT), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stats.mode, style = value, color = colors.navy)
            Text(
                text = stringResource(Res.string.profile_stat_mode),
                style = typography.caption,
                color = colors.textSecondary,
            )
        }
    }
}

/**
 * Figma's "Travel safely" button (398:6694): 64dp, r16, a #E5E7EB stroke, padding 10/16, a 20dp green
 * shield, Bold 14/22 over 12/18 gray, an 18dp chevron. There is no safety screen yet, so it does nothing.
 */
@Composable
internal fun SafetyRow() {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(CardShape)
            .border(1.dp, colors.border, CardShape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(HitchyIcons.Shield20),
            contentDescription = null,
            tint = colors.success,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.profile_travel_safely),
                style = typography.bodyBold,
                color = colors.navy,
            )
            Text(
                text = stringResource(Res.string.profile_travel_safely_sub),
                style = typography.caption,
                color = colors.textSecondary,
            )
        }
        Icon(
            painter = painterResource(HitchyIcons.Chevron18),
            contentDescription = null,
            tint = colors.navy,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** Figma's header bar: 60dp, #773BEC (the brand purple), the title (Bold 18/26 white) and a 44dp gear button. */
@Composable
internal fun ProfileHeaderBar(title: String, onOpenSettings: () -> Unit) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.purple)
            .statusBarsPadding()
            .height(60.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = title, style = HitchyTheme.typography.title, color = colors.white)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HitchyIcons.Gear22),
                contentDescription = null,
                tint = colors.white,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** An info row: a 22dp purple icon, a 12/18 grey label over a 14/22 SemiBold value, 67dp tall. */
@Composable
internal fun InfoRow(
    icon: DrawableResource,
    label: String,
    value: String,
    divider: Boolean,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(22.dp),
            )
            Column {
                Text(text = label, style = typography.caption, color = colors.textSecondary)
                Text(text = value, style = typography.bodyStrong, color = colors.navy)
            }
        }
        if (divider) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

/** A 56dp menu row: a 34dp r=10 lilac tile with a 20dp icon, a 14/22 SemiBold label and an 18dp chevron. */
@Composable
internal fun MenuRow(
    icon: DrawableResource,
    label: String,
    onClick: () -> Unit,
    divider: Boolean,
    labelColor: Color = HitchyTheme.colors.navy,
    tileColor: Color = HitchyTheme.colors.lilac,
    iconColor: Color = HitchyTheme.colors.purple,
    enabled: Boolean = true,
) {
    val colors = HitchyTheme.colors
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(tileColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = label,
                style = HitchyTheme.typography.bodyStrong,
                color = labelColor,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(HitchyIcons.Chevron18),
                contentDescription = null,
                tint = colors.navy,
                modifier = Modifier.size(18.dp),
            )
        }
        if (divider) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

/** Figma's `Account mode` switch: #F3F4F6 r=12 track, the chosen side white r=9 with a 10% shadow. */
@Composable
internal fun ModeSwitch(
    options: List<String>,
    selected: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    val colors = HitchyTheme.colors
    val segment = RoundedCornerShape(9.dp)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.disabledSurface)
            .padding(3.dp),
    ) {
        options.forEachIndexed { index, label ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .then(
                        if (active) {
                            Modifier.figmaShadow(
                                segment,
                                colors.navy.copy(alpha = SEGMENT_SHADOW),
                                blur = 4.dp,
                                y = 1.dp,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clip(segment)
                    .background(if (active) colors.white else Color.Transparent)
                    .clickable(enabled = enabled && !active) { onSelect(index) }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    // Figma: SemiBold 12/18.
                    style = HitchyTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = if (active) colors.purple else colors.textSecondary,
                )
            }
        }
    }
}

private const val SHADOW_ALPHA = 0.04f
private const val SEGMENT_SHADOW = 0.10f
private const val HALF = 0.5f
private val AVATAR_RING = 3.9.dp
private const val RATING_WEIGHT = 98f
private const val RIDES_WEIGHT = 130f
private const val MODE_WEIGHT = 126f
