@file:Suppress("MagicNumber", "DEPRECATION")

package com.hitchy.core.ui.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun IconGallery(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HitchyIcons.all.forEach { (name, resource) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(resource),
                        contentDescription = name,
                        modifier = Modifier.size(24.dp),
                        tint = HitchyTheme.colors.textPrimary,
                    )
                }
                Text(
                    text = name,
                    style = HitchyTheme.typography.body,
                    color = HitchyTheme.colors.textPrimary,
                )
            }
        }
    }
}

@Preview
@Composable
fun IconGalleryLtrPreview() {
    HitchyTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(modifier = Modifier.background(HitchyTheme.colors.background)) {
                IconGallery()
            }
        }
    }
}

@Preview
@Composable
fun IconGalleryRtlPreview() {
    HitchyTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(modifier = Modifier.background(HitchyTheme.colors.background)) {
                IconGallery()
            }
        }
    }
}
