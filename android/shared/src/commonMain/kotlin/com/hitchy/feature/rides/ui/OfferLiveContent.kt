package com.hitchy.feature.rides.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.rides.presentation.PostRideState
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.offer_invite_body
import hitchy.shared.generated.resources.offer_invite_button
import hitchy.shared.generated.resources.offer_invite_title
import hitchy.shared.generated.resources.offer_live_body
import hitchy.shared.generated.resources.offer_live_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * D5 "Your commute is live" (Figma DEMO 726:11594): the green success mark, the title, the commute
 * summary and the invite card. Figma's "No charge until riders fill 10 seats" card is left out: owners
 * buy trip packages in the plan (BUILD_PLAN R2), so the card's promise is not the product's.
 */
@Composable
internal fun OfferLiveContent(
    state: PostRideState,
    language: AppLanguage,
    onShareInvite: (() -> Unit)?,
) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.size(88.dp).clip(CircleShape).background(colors.successBg),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(colors.success),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(HitchyIcons.CheckSmall),
                        contentDescription = null,
                        tint = colors.white,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.offer_live_title),
                    style = typography.h2,
                    color = colors.navy,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.offer_live_body),
                    style = typography.body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        OfferCommuteSummary(stops = offerTimeline(state, language), tags = offerTags(state), gap = 12.dp)
        if (onShareInvite != null) InviteCard(onShareInvite)
    }
}

/** Figma's `Invite colleagues` card: a 40dp lavender icon box, the copy, and the outlined share button. */
@Composable
private fun InviteCard(onShare: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    Column(
        modifier = Modifier.fillMaxWidth().offerCard().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(HitchyIcons.Users20),
                    contentDescription = null,
                    tint = colors.purple,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(Res.string.offer_invite_title),
                    style = typography.bodyBold,
                    color = colors.navy,
                )
                Text(
                    text = stringResource(Res.string.offer_invite_body),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
        HitchyButton(onClick = onShare, variant = HitchyButtonVariant.Outline, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(Res.string.offer_invite_button), style = typography.button)
        }
    }
}
