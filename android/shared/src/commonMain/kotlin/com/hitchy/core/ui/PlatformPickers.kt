package com.hitchy.core.ui

import androidx.compose.runtime.Composable
import com.hitchy.core.model.AppPermission

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): () -> Unit

@Composable
expect fun rememberPermissionRequest(onResult: (Boolean) -> Unit): (AppPermission) -> Unit
