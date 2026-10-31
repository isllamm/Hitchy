package com.hitchy.core.ui

import androidx.compose.runtime.Composable
import com.hitchy.core.model.AppPermission

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): () -> Unit

@Composable
expect fun rememberPermissionRequest(onResult: (Boolean) -> Unit): (AppPermission) -> Unit

/** The system back gesture while [enabled]: a screen's own steps go back before the screen closes. */
@Composable
expect fun StepBackHandler(enabled: Boolean, onBack: () -> Unit)

/** White status bar icons while this is shown (a dark or purple header under the bar); dark icons after. */
@Composable
expect fun WhiteStatusBarIcons()

/** Opens the camera for one photo; [onPhoto] gets it as JPEG bytes, or null when cancelled. */
@Composable
expect fun rememberCameraCapture(onPhoto: (ByteArray?) -> Unit): () -> Unit
