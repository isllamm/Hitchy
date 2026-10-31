@file:Suppress("MagicNumber")

package com.hitchy.feature.host.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.rememberCameraCapture
import com.hitchy.core.ui.rememberImagePicker
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.feature.host.domain.KycDocKey
import com.hitchy.feature.host.presentation.HostApplicationAction
import com.hitchy.feature.host.presentation.HostApplicationState
import com.hitchy.feature.trip.ui.FigmaSheet
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.fig_su_chevron
import hitchy.shared.generated.resources.fig_su_doc_add
import hitchy.shared.generated.resources.fig_su_doc_error
import hitchy.shared.generated.resources.fig_su_doc_ok
import hitchy.shared.generated.resources.fig_su_exit
import hitchy.shared.generated.resources.fig_su_sheet_camera
import hitchy.shared.generated.resources.su_cancel
import hitchy.shared.generated.resources.su_choose_photo
import hitchy.shared.generated.resources.su_doc_driving
import hitchy.shared.generated.resources.su_doc_failed
import hitchy.shared.generated.resources.su_doc_id_back
import hitchy.shared.generated.resources.su_doc_id_front
import hitchy.shared.generated.resources.su_doc_missing
import hitchy.shared.generated.resources.su_doc_uploaded
import hitchy.shared.generated.resources.su_doc_vehicle
import hitchy.shared.generated.resources.su_docs_sub
import hitchy.shared.generated.resources.su_docs_title
import hitchy.shared.generated.resources.su_exit
import hitchy.shared.generated.resources.su_exit_body
import hitchy.shared.generated.resources.su_exit_title
import hitchy.shared.generated.resources.su_keep_going
import hitchy.shared.generated.resources.su_sheet_title
import hitchy.shared.generated.resources.su_take_photo
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal fun KycDocKey.label(): StringResource = when (this) {
    KycDocKey.ID_FRONT -> Res.string.su_doc_id_front
    KycDocKey.ID_BACK -> Res.string.su_doc_id_back
    KycDocKey.DRIVING_LICENSE -> Res.string.su_doc_driving
    KycDocKey.VEHICLE_LICENSE -> Res.string.su_doc_vehicle
}

internal enum class DocState { UPLOADED, MISSING, FAILED }

/** Figma 726:14090 "Verify your account": "Step 1 of 2" and the four document cards, each with its privacy note. */
@Composable
internal fun DocsStep(state: HostApplicationState, onAction: (HostApplicationAction) -> Unit) {
    SignUpHeader(onBack = { onAction(HostApplicationAction.Back) }, step = 1)
    SignUpIntro(
        title = stringResource(Res.string.su_docs_title),
        subtitle = stringResource(Res.string.su_docs_sub),
        modifier = Modifier.padding(top = 24.dp),
    )
    Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        KycDocKey.entries.forEach { key ->
            val docState = when (key) {
                in state.failedDocs -> DocState.FAILED
                in state.docs -> DocState.UPLOADED
                else -> DocState.MISSING
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DocumentCard(
                    title = stringResource(key.label()),
                    state = docState,
                    onClick = { onAction(HostApplicationAction.OpenUpload(key)) },
                )
                PrivacyNote()
            }
        }
    }
}

/** Figma's document card: 70dp, r16; uploaded and missing are white with a stroke, a failed photo is red. */
@Composable
internal fun DocumentCard(title: String, state: DocState, onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val shape = RoundedCornerShape(16.dp)
    val failed = state == DocState.FAILED
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(shape)
            .background(if (failed) colors.errorBg else colors.white)
            .then(if (failed) Modifier else Modifier.border(1.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DocumentTile(state)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = typography.bodyStrong, color = colors.navy)
            val subtitle = when (state) {
                DocState.UPLOADED -> Res.string.su_doc_uploaded
                DocState.MISSING -> Res.string.su_doc_missing
                DocState.FAILED -> Res.string.su_doc_failed
            }
            Text(
                text = stringResource(subtitle),
                style = typography.caption,
                color = if (failed) colors.errorText else colors.textSecondary,
            )
        }
        Image(painterResource(Res.drawable.fig_su_chevron), contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun DocumentTile(state: DocState) {
    val colors = HitchyTheme.colors
    when (state) {
        DocState.UPLOADED -> Box(modifier = Modifier.size(48.dp)) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(colors.lavender))
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colors.success)
                    .border(1.5.dp, colors.white, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                SuIcon(Res.drawable.fig_su_doc_ok, 12.dp)
            }
        }

        DocState.MISSING -> IconTile(Res.drawable.fig_su_doc_add, colors.lavender, size = 48)

        DocState.FAILED -> IconTile(Res.drawable.fig_su_doc_error, colors.white, size = 48)
    }
}

@Composable
private fun IconTile(icon: DrawableResource, background: Color, size: Int) {
    Box(
        modifier = Modifier.size(size.dp).clip(RoundedCornerShape(12.dp)).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp))
    }
}

/** Figma 726:14228: "Add <document>" with Take a photo, Choose from photos and Cancel. */
@Composable
internal fun UploadSheet(key: KycDocKey, onAction: (HostApplicationAction) -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    val takePhoto = rememberCameraCapture { onAction(HostApplicationAction.DocTaken(key, it)) }
    val choosePhoto = rememberImagePicker { onAction(HostApplicationAction.DocTaken(key, it)) }
    FigmaSheet(onDismiss = { onAction(HostApplicationAction.DismissUpload) }) {
        Text(
            text = stringResource(Res.string.su_sheet_title, stringResource(key.label())),
            style = typography.h1,
            color = colors.navy,
            textAlign = TextAlign.Center,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SheetAction(Res.drawable.fig_su_sheet_camera, stringResource(Res.string.su_take_photo), takePhoto)
            SheetAction(Res.drawable.fig_su_doc_add, stringResource(Res.string.su_choose_photo), choosePhoto)
        }
        Text(
            text = stringResource(Res.string.su_cancel),
            style = typography.bodyStrong,
            color = colors.purple,
            modifier = Modifier.clickable { onAction(HostApplicationAction.DismissUpload) },
        )
    }
}

@Composable
private fun SheetAction(icon: DrawableResource, label: String, onClick: () -> Unit) {
    val colors = HitchyTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            style = HitchyTheme.typography.captionStrong,
            color = colors.navy,
            modifier = Modifier.weight(1f),
        )
        Image(painterResource(Res.drawable.fig_su_chevron), contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

/** Figma 726:14314 "Leave for now?": the icon, the promise to keep the entries, Keep going and Exit. */
@Composable
internal fun ExitSheet(onAction: (HostApplicationAction) -> Unit) {
    val colors = HitchyTheme.colors
    val typography = HitchyTheme.typography
    FigmaSheet(onDismiss = { onAction(HostApplicationAction.KeepGoing) }) {
        Text(
            text = stringResource(Res.string.su_exit_title),
            style = typography.h1,
            color = colors.navy,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(colors.lavender),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(Res.drawable.fig_su_exit), contentDescription = null, modifier = Modifier.size(28.dp))
        }
        Text(
            text = stringResource(Res.string.su_exit_body),
            style = typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        HitchyButton(onClick = { onAction(HostApplicationAction.KeepGoing) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(Res.string.su_keep_going))
        }
        Text(
            text = stringResource(Res.string.su_exit),
            style = typography.bodyStrong,
            color = colors.purple,
            modifier = Modifier.clickable { onAction(HostApplicationAction.ConfirmExit) },
        )
    }
}
