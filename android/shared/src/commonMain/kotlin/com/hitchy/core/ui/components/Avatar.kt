package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class AvatarSize(val dp: Dp, val fontSize: Int) {
    XS(24.dp, 10),
    SM(32.dp, 12),
    MD(40.dp, 14),
    LG(56.dp, 18),
}

@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.MD,
    photoPath: String? = null,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(colors.lavender),
        contentAlignment = Alignment.Center,
    ) {
        AvatarImage(
            photoPath = photoPath,
            initials = initials.take(2).uppercase(),
            textStyle = typography.captionStrong.copy(fontSize = size.fontSize.sp),
            textColor = colors.purple,
        )
    }
}

@Preview
@Composable
fun AvatarLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Row(
                modifier = Modifier.padding(spacing.space16),
                horizontalArrangement = Arrangement.spacedBy(spacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(initials = "IA", size = AvatarSize.XS)
                Avatar(initials = "IA", size = AvatarSize.SM)
                Avatar(initials = "IA", size = AvatarSize.MD)
                Avatar(initials = "IA", size = AvatarSize.LG)
            }
        }
    }
}

@Preview
@Composable
fun AvatarRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AvatarLtrPreview()
    }
}
