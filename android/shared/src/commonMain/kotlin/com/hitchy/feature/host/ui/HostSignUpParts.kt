package com.hitchy.feature.host.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.components.BackButton
import com.hitchy.core.ui.components.BackButtonStyle
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.figmaBottom
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.theme.HitchyTheme
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fig_su_dropdown
import hitchy.shared.generated.resources.fig_su_privacy
import hitchy.shared.generated.resources.su_doc_privacy
import hitchy.shared.generated.resources.su_step
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val FieldShape = RoundedCornerShape(12.dp)

/**
 * The frame every sign-up screen shares (Figma DEMO "car owner"): content 16dp from the sides and 60dp from the
 * top, scrolling, with the main button pinned 44dp above the bottom.
 */
@Composable
internal fun SignUpFrame(
    button: String,
    enabled: Boolean,
    onButton: () -> Unit,
    loading: Boolean = false,
    tonal: Boolean = false,
    buttonIcon: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(HitchyTheme.colors.white)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = figmaTop(60.dp), bottom = 16.dp),
            content = content,
        )
        HitchyButton(
            onClick = onButton,
            enabled = enabled,
            loading = loading,
            variant = if (tonal) HitchyButtonVariant.Tonal else HitchyButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = figmaBottom(44.dp)),
        ) {
            if (buttonIcon != null) {
                buttonIcon()
                Spacer(Modifier.width(8.dp))
            }
            Text(text = button)
        }
    }
}

/** Figma's header row: the 44dp outlined back button and, on the steps, "Step N of 2" (14/22 gray) centred. */
@Composable
internal fun SignUpHeader(onBack: () -> Unit, step: Int? = null) {
    Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
        BackButton(onClick = onBack, style = BackButtonStyle.OutlinedCircle, modifier = Modifier.size(44.dp))
        if (step != null) {
            Text(
                text = stringResource(Res.string.su_step, step),
                style = HitchyTheme.typography.body,
                color = HitchyTheme.colors.textSecondary,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

/** Figma's intro: the Bold 28/36 title over the 14/22 gray line, 8dp apart. */
@Composable
internal fun SignUpIntro(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = HitchyTheme.typography.h1, color = HitchyTheme.colors.navy)
        Text(text = subtitle, style = HitchyTheme.typography.body, color = HitchyTheme.colors.textSecondary)
    }
}

/** Figma's Dropdown (component 16:113): a SemiBold 14/22 label over a 52dp r12 field with the value and a chevron. */
@Composable
internal fun SignUpDropdown(
    label: String,
    value: String?,
    placeholder: String,
    options: List<String>,
    onSelect: (Int) -> Unit,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    var open by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = typography.bodyStrong, color = colors.textPrimary)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(FieldShape)
                    .background(colors.white)
                    .border(1.dp, colors.border, FieldShape)
                    .clickable(enabled = options.isNotEmpty()) { open = true }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value ?: placeholder,
                    style = typography.bodyLarge,
                    color = if (value == null) colors.textSecondary else colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (options.isNotEmpty()) {
                    Image(
                        painter = painterResource(Res.drawable.fig_su_dropdown),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(text = option, style = typography.bodyLarge, color = colors.navy) },
                        onClick = {
                            open = false
                            onSelect(index)
                        },
                    )
                }
            }
        }
    }
}

/** Figma's tips card: lilac, r16, padding 16, a SemiBold 14/22 title and 12/18 lines after 4dp violet dots. */
@Composable
internal fun SignUpTips(title: String, tips: List<String>) {
    val colors = HitchyTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.lilac).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = title, style = HitchyTheme.typography.bodyStrong, color = colors.navy)
        tips.forEach { tip ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(4.dp).clip(CircleShape).background(colors.violet))
                Text(text = tip, style = HitchyTheme.typography.caption, color = colors.textSecondary)
            }
        }
    }
}

/** A Figma icon drawable at its own colours. */
@Composable
internal fun SuIcon(icon: DrawableResource, size: Dp) {
    Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(size))
}

/** Figma's privacy line under each document: a 16dp green shield and the 12/18 gray note. */
@Composable
internal fun PrivacyNote() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SuIcon(Res.drawable.fig_su_privacy, 16.dp)
        Text(
            text = stringResource(Res.string.su_doc_privacy),
            style = HitchyTheme.typography.caption,
            color = HitchyTheme.colors.textSecondary,
        )
    }
}
