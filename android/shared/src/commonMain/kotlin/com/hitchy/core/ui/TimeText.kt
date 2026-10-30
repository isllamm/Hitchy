package com.hitchy.core.ui

import androidx.compose.runtime.Composable
import com.hitchy.core.util.isoToCairoTime
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.rider_sheet_am
import hitchy.shared.generated.resources.rider_sheet_pm
import org.jetbrains.compose.resources.stringResource

/** Cairo time of an ISO instant as Figma writes it, "7:30 AM"; "--:--" when it can't be parsed. */
@Composable
fun cairoTime12(iso: String?): String {
    val hhmm = isoToCairoTime(iso)
    val parts = hhmm.split(":")
    val hours = parts.getOrNull(0)?.toIntOrNull() ?: return hhmm
    val minutes = parts.getOrNull(1) ?: return hhmm
    val suffix = stringResource(if (hours >= NOON) Res.string.rider_sheet_pm else Res.string.rider_sheet_am)
    val hour12 = (hours % NOON).let { if (it == 0) NOON else it }
    return "$hour12:$minutes $suffix"
}

private const val NOON = 12
