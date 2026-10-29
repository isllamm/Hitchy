package com.hitchy.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.hitchy.core.data.AppError
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.error_internal
import hitchy.shared.generated.resources.error_network
import hitchy.shared.generated.resources.error_unauthenticated
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun AppError.message(): StringResource {
    return when (this) {
        is AppError.Network -> Res.string.error_network
        is AppError.NotSignedIn -> Res.string.error_unauthenticated
        is AppError.Reason -> {
            when (code.uppercase()) {
                "UNAUTHENTICATED" -> Res.string.error_unauthenticated
                "INTERNAL" -> Res.string.error_internal
                else -> Res.string.error_internal
            }
        }
        is AppError.Unknown -> Res.string.error_internal
    }
}

@Composable
fun ErrorText(
    error: AppError,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.error,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = stringResource(error.message()),
        modifier = modifier,
        color = color,
        style = style
    )
}
