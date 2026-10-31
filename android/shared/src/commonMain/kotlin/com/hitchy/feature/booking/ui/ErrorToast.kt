package com.hitchy.feature.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.rider_home_dismiss
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Figma's error toast (component 446:2958): 48dp, r=12, #FEE2E2, Bold #B91C1C text, a close mark. */
@Composable
internal fun ErrorToast(
    text: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.errorBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(HitchyIcons.ToastError),
            contentDescription = null,
            tint = colors.errorText,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = typography.bodyBold,
            color = colors.errorText,
            modifier = Modifier.weight(1f).padding(start = 10.dp),
        )
        Icon(
            painter = painterResource(HitchyIcons.CloseSmall),
            contentDescription = stringResource(Res.string.rider_home_dismiss),
            tint = colors.errorText,
            modifier = Modifier.size(18.dp).clip(CircleShape).clickable(onClick = onDismiss),
        )
    }
}
