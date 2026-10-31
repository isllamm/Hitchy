@file:Suppress("TooManyFunctions")

package com.hitchy.feature.profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.ui.components.AvatarImage
import com.hitchy.core.ui.figmaLinearGradient
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.tightLine
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fig_more_chevron
import hitchy.shared.generated.resources.fig_more_edit
import hitchy.shared.generated.resources.fig_more_logout
import hitchy.shared.generated.resources.fig_more_star
import hitchy.shared.generated.resources.fig_more_verified
import hitchy.shared.generated.resources.fig_more_wallet
import hitchy.shared.generated.resources.profile_edit
import hitchy.shared.generated.resources.profile_logout
import hitchy.shared.generated.resources.profile_wallet_amount
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Figma DEMO 676:10783 "المزيد": the profile tab of the demo.
private val SectionShape = RoundedCornerShape(16.dp)
private val HeaderShape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)

private const val SECTION_SHADOW_ALPHA = 0.04f
private const val SECTION_STROKE = 0.6f
private const val DECOR_ALPHA = 0.15f
private const val WALLET_TILE_ALPHA = 0.32f
private const val LOGOUT_TILE_ALPHA = 0.12f
private const val HALF_TURN = 180f

/** Figma's gradient handles on the header, as fractions of its size. */
private const val GRAD_START_X = -0.12f
private const val GRAD_START_Y = 0.16f
private const val GRAD_END_X = 0.68f
private const val GRAD_END_Y = 0.76f
private const val GRAD_BAND_X = -0.42f
private const val GRAD_BAND_Y = 0.56f
private const val HALF = 0.5f

/** Figma's status bar height: the header starts its content 16dp under it. */
private val STATUS_BAR = 52.dp

/**
 * Figma's header (676:10865): 148dp with the brand gradient and 12dp bottom corners, a dim 100dp circle in
 * the corner, the 56dp avatar, the name (Bold 24/24 white) with the verified mark, the star and
 * "4.9 · 128 rides" (12/18 #E5E7EB), and "Edit Profile" (Bold 12/20 lavender) on the end side.
 */
@Composable
internal fun MoreHeader(
    initials: String,
    photoPath: String?,
    name: String,
    verified: Boolean,
    ratingLine: String?,
    onEditProfile: (() -> Unit)?,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HeaderShape)
            .drawBehind {
                drawRect(
                    figmaLinearGradient(
                        colorStops = arrayOf(0f to colors.violet, HALF to colors.purple, 1f to colors.deepPurple),
                        start = Offset(GRAD_START_X, GRAD_START_Y),
                        end = Offset(GRAD_END_X, GRAD_END_Y),
                        band = Offset(GRAD_BAND_X, GRAD_BAND_Y),
                        size = size,
                    ),
                )
            }
            .height(figmaTop(STATUS_BAR) + 96.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 46.dp, y = 47.dp)
                .size(100.dp)
                .clip(CircleShape)
                .background(colors.steel.copy(alpha = DECOR_ALPHA)),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = figmaTop(STATUS_BAR) + 16.dp)
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(colors.white),
                contentAlignment = Alignment.Center,
            ) {
                AvatarImage(
                    photoPath = photoPath,
                    initials = initials,
                    textStyle = typography.h3,
                    textColor = colors.purple,
                )
            }
            Column(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = name,
                        style = typography.h2.copy(lineHeight = 24.sp),
                        modifier = Modifier.tightLine(typography.h2.fontSize),
                        color = colors.white,
                        maxLines = 1,
                    )
                    if (verified) {
                        Image(
                            painter = painterResource(Res.drawable.fig_more_verified),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                if (ratingLine != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.fig_more_star),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(text = ratingLine, style = typography.caption, color = colors.border)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .then(if (onEditProfile != null) Modifier.clickable(onClick = onEditProfile) else Modifier),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.fig_more_edit),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(Res.string.profile_edit),
                    style = typography.captionStrong.copy(lineHeight = 20.sp),
                    color = colors.lavender,
                )
            }
        }
    }
}

/** Figma's wallet card: lilac, r16, padding 16, a 44dp #D2D6DB tile with the wallet, the balance in purple. */
@Composable
internal fun MoreWalletCard(balanceEgp: Int) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SectionShape)
            .background(colors.lilac)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(Res.drawable.fig_more_wallet, colors.tileGray.copy(alpha = WALLET_TILE_ALPHA))
        Box(Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.profile_wallet_amount, balanceEgp),
            style = HitchyTheme.typography.h3.copy(lineHeight = 22.sp, letterSpacing = 0.2.sp),
            color = colors.purple,
        )
    }
}

/**
 * Figma's section: white, r16, a 0.6dp #E5E7EB stroke, a 4% navy shadow, padding 16, a Bold 16/20 title
 * and 16dp row gaps.
 */
@Composable
internal fun MoreSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = HitchyTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .figmaShadow(SectionShape, colors.navy.copy(alpha = SECTION_SHADOW_ALPHA), blur = 8.dp, y = 2.dp)
            .clip(SectionShape)
            .background(colors.white)
            .border(SECTION_STROKE.dp, colors.border, SectionShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = title,
            style = HitchyTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 20.sp),
            color = colors.slate900,
        )
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

/**
 * Figma's menu row: 44dp, a 44dp lavender circle with the purple icon, the 600 14/22 label and a trailing
 * part (the 18dp chevron by default). [onClick] null means the row has no screen yet and does nothing.
 */
@Composable
internal fun MoreRow(
    icon: DrawableResource,
    label: String,
    onClick: (() -> Unit)?,
    trailing: @Composable () -> Unit = { Chevron() },
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconTile(icon, HitchyTheme.colors.lavender)
        Text(
            text = label,
            style = HitchyTheme.typography.bodyStrong,
            color = HitchyTheme.colors.navy,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Composable
private fun Chevron() {
    Image(
        painter = painterResource(Res.drawable.fig_more_chevron),
        contentDescription = null,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
private fun IconTile(icon: DrawableResource, background: Color) {
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

/** Figma's switch (component 277:1653): 44x24, purple, a 24dp white knob with a 2dp purple ring at the end. */
@Composable
internal fun MoreSwitch(checked: Boolean) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(CircleShape)
            .background(if (checked) colors.purple else colors.border),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colors.white)
                .border(2.dp, if (checked) colors.purple else colors.border, CircleShape),
        )
    }
}

/** Figma's language toggle: white r12, padding 8, two 22dp r8 segments, "ع" and "EN"; the current one purple. */
@Composable
internal fun LanguageToggle(current: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(HitchyTheme.colors.white)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LanguageSegment("ع", current == AppLanguage.ARABIC) { onSelect(AppLanguage.ARABIC) }
        LanguageSegment("EN", current == AppLanguage.ENGLISH) { onSelect(AppLanguage.ENGLISH) }
    }
}

@Composable
private fun LanguageSegment(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    Box(
        modifier = Modifier
            .height(22.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) colors.purple else Color.Transparent)
            .clickable(enabled = !active, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = HitchyTheme.typography.caption.copy(lineHeight = 14.sp),
            color = if (active) colors.white else colors.purple,
        )
    }
}

/** Figma's logout row: a 44dp red-12% circle with the turned logout icon and "Logout" in Bold 16/22 red. */
@Composable
internal fun LogoutRow(onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .rotate(HALF_TURN)
                .clip(CircleShape)
                .background(colors.alertRed.copy(alpha = LOGOUT_TILE_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.fig_more_logout),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(Res.string.profile_logout),
            style = HitchyTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp),
            color = colors.alertRed,
        )
        Box(Modifier.width(1.dp))
    }
}
