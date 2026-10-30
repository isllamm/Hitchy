package com.hitchy.feature.inbox.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.platform.LanguageSwitcher
import com.hitchy.core.ui.components.EmptyState
import com.hitchy.core.ui.components.HitchyButton
import com.hitchy.core.ui.components.HitchyButtonVariant
import com.hitchy.core.ui.components.NotificationRow
import com.hitchy.core.ui.components.TopBar
import com.hitchy.core.ui.icon.HitchyIcons
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.util.isoToCairoTime
import com.hitchy.feature.inbox.presentation.InboxAction
import com.hitchy.feature.inbox.presentation.InboxEvent
import com.hitchy.feature.inbox.presentation.InboxViewModel
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.inbox_empty_body
import hitchy.shared.generated.resources.inbox_empty_title
import hitchy.shared.generated.resources.inbox_mark_all_read
import hitchy.shared.generated.resources.inbox_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenDeepLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    languageSwitcher: LanguageSwitcher = koinInject(),
    viewModel: InboxViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = HitchyTheme.colors
    val currentLanguage: AppLanguage = languageSwitcher.getCurrentLanguage()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is InboxEvent.OpenDeepLink -> onOpenDeepLink(event.deepLink)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = stringResource(Res.string.inbox_title),
                onBackClick = onBack,
                actions = if (state.unreadIds.isNotEmpty()) {
                    {
                        HitchyButton(
                            onClick = { viewModel.onAction(InboxAction.MarkAllRead) },
                            variant = HitchyButtonVariant.Text,
                        ) {
                            Text(text = stringResource(Res.string.inbox_mark_all_read))
                        }
                    }
                } else {
                    null
                },
            )

            if (state.items.isEmpty() && !state.isLoading) {
                EmptyState(
                    title = stringResource(Res.string.inbox_empty_title),
                    message = stringResource(Res.string.inbox_empty_body),
                    icon = HitchyIcons.Bell,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.items, key = { it.id }) { item ->
                        NotificationRow(
                            title = item.title.resolve(currentLanguage),
                            body = item.body.resolve(currentLanguage),
                            timestampText = isoToCairoTime(item.createdAt),
                            isUnread = item.isUnread,
                            onClick = { viewModel.onAction(InboxAction.TapNotification(item)) },
                        )
                        HorizontalDivider(color = colors.border, thickness = 1.dp)
                    }
                }
            }
        }
    }
}
