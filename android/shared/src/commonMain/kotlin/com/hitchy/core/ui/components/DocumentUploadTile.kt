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
import hitchy.shared.generated.resources.document_upload_prompt
import hitchy.shared.generated.resources.document_upload_title
import hitchy.shared.generated.resources.needs_design_review
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DocumentUploadTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUploaded: Boolean = false,
    fileName: String? = null,
    enabled: Boolean = true,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current
    val tileShape = RoundedCornerShape(16.dp)
    val pillShape = RoundedCornerShape(50)

    val borderColor = if (isUploaded) colors.success else colors.border
    val bgColor = if (isUploaded) colors.successBg else colors.white

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(tileShape)
            .border(1.dp, borderColor, tileShape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = tileShape,
        color = bgColor,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = typography.bodyStrong,
                    color = colors.navy,
                )

                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(colors.warningBg)
                        .padding(horizontal = spacing.space8, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.needs_design_review),
                        style = typography.caption,
                        color = colors.warningText,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isUploaded) colors.white else colors.lavender),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(
                            if (isUploaded) HitchyIcons.Check else HitchyIcons.Upload,
                        ),
                        contentDescription = null,
                        tint = if (isUploaded) colors.success else colors.purple,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Column {
                    Text(
                        text = if (isUploaded) {
                            fileName ?: stringResource(Res.string.document_upload_title)
                        } else {
                            stringResource(Res.string.document_upload_prompt)
                        },
                        style = typography.caption,
                        color = if (isUploaded) colors.successText else colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun DocumentUploadTileLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                DocumentUploadTile(
                    label = "National ID (Front)",
                    onClick = {},
                    isUploaded = false,
                )
                DocumentUploadTile(
                    label = "Driver's License",
                    onClick = {},
                    isUploaded = true,
                    fileName = "license_front.jpg",
                )
            }
        }
    }
}

@Preview
@Composable
fun DocumentUploadTileRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        DocumentUploadTileLtrPreview()
    }
}
