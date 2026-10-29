package com.hitchy.feature.debug.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.ui.ErrorText
import com.hitchy.feature.debug.presentation.SampleAction
import com.hitchy.feature.debug.presentation.SampleEvent
import com.hitchy.feature.debug.presentation.SampleViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.sample_button_load
import hitchy.shared.generated.resources.sample_loaded_prefix
import hitchy.shared.generated.resources.sample_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SampleScreen(
    vm: SampleViewModel,
    modifier: Modifier = Modifier,
    onSuccessEvent: ((String) -> Unit)? = null
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is SampleEvent.LoadSuccess -> onSuccessEvent?.invoke(event.message)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.sample_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { vm.onAction(SampleAction.Load) }) {
                Text(text = stringResource(Res.string.sample_button_load))
            }
        }

        state.data?.let { data ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.sample_loaded_prefix, data),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            ErrorText(error = error)
        }
    }
}
