package com.team12kotlin.juggle.ui.profile.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.team12kotlin.juggle.ui.components.NotificationCard
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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Notifications") },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(
                            imageVector = MaterialSymbols.Outlined.Arrow_back,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 5.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
            ) {
                uiState.notifications.forEach { notification ->
                    NotificationCard(
                        notification= notification,
                        showDismiss = true,
                        outlined = true,
                        onDismiss = { viewModel.onNotificationDismissed(notification.id) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    JuggleTheme() {
        NotificationsScreen()
    }
}