package com.hitchy.feature.profile.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.figmaShadow
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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

/** The avatar card: a 72dp gradient circle with a white ring, the name at 20/28 and a 12/18 line. */
@Composable
internal fun AvatarCard(
    initials: String,
    name: String,
    detail: String,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    FigmaSection(background = Brush.linearGradient(listOf(colors.white.copy(alpha = HALF), colors.lilac))) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(colors.lavender)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(colors.white)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(colors.brandGradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = initials, style = typography.h3, color = colors.white)
            }
            Column {
                Text(text = name, style = typography.h3, color = colors.navy, maxLines = 1)
                Text(text = detail, style = typography.caption, color = colors.textSecondary, maxLines = 1)
            }
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
            Icon(painter = painterResource(icon), contentDescription = null, tint = colors.purple, modifier = Modifier.size(22.dp))
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
            modifier = Modifier.fillMaxWidth().height(56.dp).clickable(enabled = enabled, onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(tileColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
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
                            Modifier.figmaShadow(segment, colors.navy.copy(alpha = SEGMENT_SHADOW), blur = 4.dp, y = 1.dp)
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
                    style = HitchyTheme.typography.captionStrong.copy(fontWeight = HitchyTheme.typography.bodyStrong.fontWeight),
                    color = if (active) colors.purple else colors.textSecondary,
                )
            }
        }
    }
}

private const val SHADOW_ALPHA = 0.04f
private const val SEGMENT_SHADOW = 0.10f
private const val HALF = 0.5f
