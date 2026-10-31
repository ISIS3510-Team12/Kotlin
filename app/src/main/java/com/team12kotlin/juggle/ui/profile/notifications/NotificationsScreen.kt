package com.team12kotlin.juggle.ui.profile.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.LoadingContent
import com.team12kotlin.juggle.ui.components.NotificationCard
import com.team12kotlin.juggle.ui.connectivity.RefreshOnReconnect
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.dto.Notification
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = viewModel(),
    goBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.Notifications, uiState.isLoading)
    val isOnline = rememberIsOnline()
    RefreshOnReconnect(isOnline = isOnline, onReconnect = viewModel::loadNotifications)

    NotificationsContent(
        uiState = uiState,
        modifier = modifier,
        goBack = goBack,
        showCachedData = isOnline == false && uiState.notifications.isNotEmpty(),
        onNotificationDismissed = viewModel::onNotificationDismissed,
        onRetry = viewModel::loadNotifications,
    )
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun NotificationsContent(
    uiState: NotificationsUiState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit = {},
    showCachedData: Boolean = false,
    onNotificationDismissed: (Int) -> Unit = {},
    onRetry: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopBar(
                title = "Notifications",
                onBack = goBack,
                showCachedData = showCachedData
            )
        }
    ) { innerPadding ->
        LoadingContent(isLoading = uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 5.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                    horizontalAlignment = Alignment.Start,
                ) {
                    uiState.errorMessage?.let { message ->
                        ErrorCard(
                            message = message,
                            onRetry = onRetry
                        )
                    }
                    if (uiState.notifications.isNotEmpty()) {
                        uiState.notifications.forEach { notification ->
                            NotificationCard(
                                notification= notification,
                                showDismiss = true,
                                outlined = true,
                                onDismiss = { onNotificationDismissed(notification.id) }
                            )
                        }
                    } else if (!uiState.isLoading && !uiState.loadFailed) {
                        Text(
                            text = "No pending notifications",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoNotificationsScreenPreview() {
    JuggleTheme {
        NotificationsContent(
            uiState = NotificationsUiState().copy()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    JuggleTheme {
        NotificationsContent(
            uiState = NotificationsUiState().copy(
                notifications = listOf(
                    Notification(id = 1, title = "New task", date = "October 31", origin = "Group dev", type = "create"),
                    Notification(id = 1, title = "New task", date = "October 31", origin = "Group dev", type = "create")
                )
            )
        )
    }
}