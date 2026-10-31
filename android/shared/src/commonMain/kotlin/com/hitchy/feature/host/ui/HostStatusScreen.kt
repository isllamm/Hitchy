@file:Suppress("MagicNumber")

package com.hitchy.feature.host.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppPermission
import com.hitchy.core.model.HostStatus
import com.hitchy.core.ui.rememberPermissionRequest
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.host.domain.HostRepository
import com.hitchy.feature.profile.domain.ProfileRepository
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fig_su_approved
import hitchy.shared.generated.resources.fig_su_bell
import hitchy.shared.generated.resources.fig_su_check_small
import hitchy.shared.generated.resources.fig_su_doc_symbol
import hitchy.shared.generated.resources.fig_su_timeline_ok
import hitchy.shared.generated.resources.img_app_icon
import hitchy.shared.generated.resources.su_added
import hitchy.shared.generated.resources.su_all_set
import hitchy.shared.generated.resources.su_approved_step
import hitchy.shared.generated.resources.su_approved_title
import hitchy.shared.generated.resources.su_before_upload
import hitchy.shared.generated.resources.su_checking
import hitchy.shared.generated.resources.su_complete_profile
import hitchy.shared.generated.resources.su_fix_body
import hitchy.shared.generated.resources.su_fix_doc
import hitchy.shared.generated.resources.su_fix_doc_body
import hitchy.shared.generated.resources.su_fix_title
import hitchy.shared.generated.resources.su_item_criminal
import hitchy.shared.generated.resources.su_item_criminal_sub
import hitchy.shared.generated.resources.su_item_email
import hitchy.shared.generated.resources.su_item_email_sub
import hitchy.shared.generated.resources.su_item_emergency
import hitchy.shared.generated.resources.su_item_instapay
import hitchy.shared.generated.resources.su_item_instapay_sub
import hitchy.shared.generated.resources.su_item_vehicle
import hitchy.shared.generated.resources.su_item_vehicle_sub
import hitchy.shared.generated.resources.su_item_women
import hitchy.shared.generated.resources.su_item_women_sub
import hitchy.shared.generated.resources.su_missing
import hitchy.shared.generated.resources.su_notify
import hitchy.shared.generated.resources.su_progress
import hitchy.shared.generated.resources.su_start_offering
import hitchy.shared.generated.resources.su_submitted
import hitchy.shared.generated.resources.su_thanks
import hitchy.shared.generated.resources.su_tip_corners
import hitchy.shared.generated.resources.su_tip_flat
import hitchy.shared.generated.resources.su_tip_glare
import hitchy.shared.generated.resources.su_turn_on
import hitchy.shared.generated.resources.su_under_review
import hitchy.shared.generated.resources.su_upload_again
import hitchy.shared.generated.resources.su_verified
import hitchy.shared.generated.resources.su_welcome
import hitchy.shared.generated.resources.su_within
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Where the owner application stands (Figma DEMO): Account review while it waits, Approval once approved and
 * Document review when the team asks for changes. It follows `users/{uid}.host.status`.
 */
@Composable
fun HostStatusScreen(
    onBack: () -> Unit,
    onFix: () -> Unit,
    onDone: () -> Unit,
    profileRepository: ProfileRepository = koinInject(),
    hostRepository: HostRepository = koinInject(),
) {
    val me by profileRepository.observeMe().collectAsStateWithLifecycle(initialValue = null)
    val vehicles by hostRepository.observeVehicles().collectAsStateWithLifecycle(initialValue = emptyList())
    val user = me
    if (user == null) {
        LoadingScreen()
        return
    }
    when (user.host.status) {
        HostStatus.APPROVED -> ApprovalContent(firstName = user.firstName, hasVehicle = vehicles.isNotEmpty(), onDone)
        HostStatus.REJECTED -> FixContent(onBack = onBack, onFix = onFix)
        else -> ReviewContent(firstName = user.firstName)
    }
}

/** Figma 726:14588 "You're all set for now": the app icon, the timeline and the notification button. */
@Composable
private fun ReviewContent(firstName: String) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val askNotifications = rememberPermissionRequest { }
    SignUpFrame(
        button = stringResource(Res.string.su_turn_on),
        enabled = true,
        onButton = { askNotifications(AppPermission.NOTIFICATIONS) },
        tonal = true,
        buttonIcon = { Image(painterResource(Res.drawable.fig_su_bell), null, Modifier.size(24.dp)) },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 33.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(painterResource(Res.drawable.img_app_icon), contentDescription = null, Modifier.size(150.dp))
            StatusPill(stringResource(Res.string.su_under_review), colors.warningBg, colors.warningText)
            CenteredIntro(
                title = stringResource(Res.string.su_all_set),
                subtitle = stringResource(Res.string.su_thanks, firstName),
            )
        }
        Column(modifier = Modifier.padding(top = 24.dp, start = 12.dp)) {
            TimelineItem(stringResource(Res.string.su_submitted), null, Marker.DONE, connector = colors.purple)
            TimelineItem(
                stringResource(Res.string.su_under_review),
                stringResource(Res.string.su_checking),
                Marker.CURRENT,
                connector = colors.border,
            )
            TimelineItem(
                stringResource(Res.string.su_approved_step),
                stringResource(Res.string.su_start_offering),
                Marker.NEXT,
                connector = null,
            )
        }
        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.lilac)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(Res.string.su_within), style = typography.bodyStrong, color = colors.navy)
            Text(
                text = stringResource(Res.string.su_notify),
                style = typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private enum class Marker { DONE, CURRENT, NEXT }

/** One 72dp timeline row: a 24dp marker, a 2x52 connector under it, the title and its 12/18 line. */
@Composable
private fun TimelineItem(title: String, subtitle: String?, marker: Marker, connector: Color?) {
    val colors = HitchyTheme.colors
    Row(modifier = Modifier.height(72.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (marker) {
                Marker.DONE -> Box(
                    Modifier.size(24.dp).clip(CircleShape).background(colors.success),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painterResource(Res.drawable.fig_su_timeline_ok), null, Modifier.size(14.dp))
                }

                Marker.CURRENT -> Box(
                    Modifier
                        .size(24.dp)
                        .border(2.dp, colors.purple, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(colors.lavender)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(colors.purple),
                )

                Marker.NEXT -> Box(
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colors.white)
                        .border(1.3.dp, colors.border, CircleShape),
                )
            }
            if (connector != null) Box(Modifier.width(2.dp).height(48.dp).background(connector))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = HitchyTheme.typography.bodyStrong, color = colors.navy)
            if (subtitle != null) {
                Text(subtitle, style = HitchyTheme.typography.caption, color = colors.textSecondary)
            }
        }
    }
}

/** Figma 726:14660 "You're approved!": the gradient check, the progress and the profile checklist. */
@Composable
private fun ApprovalContent(firstName: String, hasVehicle: Boolean, onDone: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val added = stringResource(Res.string.su_added)
    val items = listOf(
        Triple(stringResource(Res.string.su_item_emergency), stringResource(Res.string.su_missing), false),
        Triple(stringResource(Res.string.su_item_vehicle), stringResource(Res.string.su_item_vehicle_sub), hasVehicle),
        Triple(stringResource(Res.string.su_item_instapay), stringResource(Res.string.su_item_instapay_sub), false),
        Triple(stringResource(Res.string.su_item_criminal), stringResource(Res.string.su_item_criminal_sub), false),
        Triple(stringResource(Res.string.su_item_women), stringResource(Res.string.su_item_women_sub), false),
        Triple(stringResource(Res.string.su_item_email), stringResource(Res.string.su_item_email_sub), false),
    )
    val done = items.count { it.third }
    SignUpFrame(button = stringResource(Res.string.su_complete_profile), enabled = true, onButton = onDone) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .shadow(20.dp, CircleShape, ambientColor = colors.purple, spotColor = colors.purple)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(colors.violet, colors.purple, colors.deepPurple))),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(Res.drawable.fig_su_approved), null, Modifier.size(75.dp))
            }
            StatusPill(stringResource(Res.string.su_verified), colors.successBg, colors.successText)
            CenteredIntro(
                title = stringResource(Res.string.su_approved_title),
                subtitle = stringResource(Res.string.su_welcome, firstName),
            )
        }
        Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.su_complete_profile),
                    style = typography.title,
                    color = colors.navy,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(Res.string.su_progress, done, items.size),
                    style = typography.body,
                    color = colors.purple,
                )
            }
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(colors.lavender)) {
                Box(
                    Modifier
                        .fillMaxWidth(done.toFloat() / items.size)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(colors.purple),
                )
            }
        }
        val shape = RoundedCornerShape(16.dp)
        Column(modifier = Modifier.padding(top = 12.dp).fillMaxWidth().clip(shape).border(1.dp, colors.border, shape)) {
            items.forEachIndexed { index, (title, subtitle, isDone) ->
                ChecklistRow(title = title, subtitle = if (isDone && index == 0) added else subtitle, done = isDone)
                if (index < items.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            }
        }
    }
}

@Composable
private fun ChecklistRow(title: String, subtitle: String, done: Boolean) {
    val colors = HitchyTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().height(69.dp).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (done) {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape).background(colors.successBg),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(Res.drawable.fig_su_check_small), null, Modifier.size(14.dp))
            }
        } else {
            Box(Modifier.size(22.dp).border(1.3.dp, colors.border, CircleShape))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = HitchyTheme.typography.bodyStrong, color = colors.navy)
            Text(subtitle, style = HitchyTheme.typography.caption, color = colors.textSecondary)
        }
    }
}

/** Figma 726:14741 "We need one quick fix": the red issue box, the upload tips and Upload again. */
@Composable
private fun FixContent(onBack: () -> Unit, onFix: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    SignUpFrame(button = stringResource(Res.string.su_upload_again), enabled = true, onButton = onFix) {
        SignUpHeader(onBack = onBack)
        SignUpIntro(
            title = stringResource(Res.string.su_fix_title),
            subtitle = stringResource(Res.string.su_fix_body),
            modifier = Modifier.padding(top = 24.dp),
        )
        Row(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.errorBg)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(painterResource(Res.drawable.fig_su_doc_symbol), null, Modifier.size(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.su_fix_doc), style = typography.bodyStrong, color = colors.errorText)
                Text(stringResource(Res.string.su_fix_doc_body), style = typography.caption, color = colors.errorText)
            }
        }
        Box(Modifier.padding(top = 24.dp)) {
            SignUpTips(
                title = stringResource(Res.string.su_before_upload),
                tips = listOf(
                    stringResource(Res.string.su_tip_flat),
                    stringResource(Res.string.su_tip_corners),
                    stringResource(Res.string.su_tip_glare),
                ),
            )
        }
    }
}

@Composable
private fun StatusPill(text: String, background: Color, content: Color) {
    Text(
        text = text,
        style = HitchyTheme.typography.bodyStrong,
        color = content,
        modifier = Modifier.clip(CircleShape).background(background).padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun CenteredIntro(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = HitchyTheme.typography.h1, color = HitchyTheme.colors.navy, textAlign = TextAlign.Center)
        Text(
            text = subtitle,
            style = HitchyTheme.typography.body,
            color = HitchyTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
