package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.action_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class BackButtonStyle {
    Plain,
    TonalCircle,
}

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: BackButtonStyle = BackButtonStyle.Plain,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        enabled = enabled,
    ) {
        if (style == BackButtonStyle.TonalCircle) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.lavender),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Back),
                    contentDescription = stringResource(Res.string.action_back),
                    tint = colors.purple,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            Icon(
                painter = painterResource(HitchyIcons.Back),
                contentDescription = stringResource(Res.string.action_back),
                tint = colors.textPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview
@Composable
fun BackButtonLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Row(modifier = Modifier.padding(spacing.space16)) {
                BackButton(onClick = {}, style = BackButtonStyle.Plain)
                BackButton(onClick = {}, style = BackButtonStyle.TonalCircle)
            }
        }
    }
}

@Preview
@Composable
fun BackButtonRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BackButtonLtrPreview()
    }
}
