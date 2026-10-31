package com.hitchy.feature.shop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.figmaTop
import com.hitchy.core.ui.theme.HitchyTheme
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.img_shop_bag
import hitchy.shared.generated.resources.img_shop_logo
import hitchy.shared.generated.resources.shop_coming_body
import hitchy.shared.generated.resources.shop_coming_soon
import hitchy.shared.generated.resources.shop_subtitle
import hitchy.shared.generated.resources.shop_title_word
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Shop tab, Figma 390:1336: the hitchy logo with "Shop" (Bold 28/36 purple, 12dp over the logo), the
 * 16/24 subtitle, then centred the 300x200 bag and wheel picture, "Coming Soon" (Bold 28/36) and the 16/24 line.
 */
@Composable
fun ShopTabContent(modifier: Modifier = Modifier) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(modifier = modifier.fillMaxSize().background(colors.white)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = figmaTop(56.dp)),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(modifier = Modifier.heightIn(min = 36.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.img_shop_logo),
                    contentDescription = null,
                    modifier = Modifier.size(width = 89.9.dp, height = 36.dp),
                )
                Text(
                    text = stringResource(Res.string.shop_title_word),
                    style = typography.h1,
                    color = colors.purple,
                    modifier = Modifier.offset(x = (-1).dp),
                )
            }
            Text(
                text = stringResource(Res.string.shop_subtitle),
                style = typography.bodyLarge,
                color = colors.textSecondary,
            )
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
            Column(
                // Figma: the picture starts 145dp under the subtitle.
                modifier = Modifier.padding(top = 145.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(Res.drawable.img_shop_bag),
                    contentDescription = null,
                    modifier = Modifier.size(width = 300.dp, height = 200.dp),
                )
                Text(text = stringResource(Res.string.shop_coming_soon), style = typography.h1, color = colors.navy)
                Text(
                    text = stringResource(Res.string.shop_coming_body),
                    style = typography.bodyLarge,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(290.dp).padding(top = 8.78.dp),
                )
            }
        }
    }
}
