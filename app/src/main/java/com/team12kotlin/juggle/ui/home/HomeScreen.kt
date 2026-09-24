package com.team12kotlin.juggle.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlinedfilled.Circle_notifications
import com.composables.icons.materialsymbols.roundedfilled.List_alt
import com.team12kotlin.juggle.ui.components.NotificationCard
import com.team12kotlin.juggle.ui.components.OverviewCard
import com.team12kotlin.juggle.ui.dto.Notification
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.tasks.TaskCard
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.topbar.AppTopBar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun HomeScreen (
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onProfileClick: (NavigationDestination) -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onNotificationClick: (Notification) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val startCorner = 20.dp
    val endCorner = 20.dp
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(onProfileClick = onProfileClick)
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = modifier
                    .padding(8.dp)
                    .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = modifier.padding(
                        start = 0.dp,
                        top = 0.dp,
                        end = 0.dp,
                        bottom = 0.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Column(modifier = modifier) {
                        Text(
                            text = "Hi " + uiState.user.firstName + "!",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Row(
                            modifier = modifier
                                .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Saturday, September 12, 2026",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedButton(
                                onClick = { viewModel.onQuickActionPress() },
                                modifier = modifier
                                    .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                                shape = RoundedCornerShape(
                                    topStart = startCorner,
                                    bottomStart = startCorner,
                                    topEnd = endCorner,
                                    bottomEnd = endCorner
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onBackground
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Quick Actions",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            10.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OverviewCard(
                            icon = MaterialSymbols.RoundedFilled.List_alt,
                            title = "Tasks",
                            count = uiState.tasks.size
                        )
                        OverviewCard(
                            icon = MaterialSymbols.OutlinedFilled.Circle_notifications,
                            title = "Notifications",
                            count = uiState.notifications.size
                        )
                    }
                    val selectedTab = uiState.selectedTab
                    PrimaryTabRow(
                        modifier = modifier
                            .fillMaxWidth(),
                        selectedTabIndex = uiState.selectedTab
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { viewModel.onTabSelected(1) },
                            text = {
                                Text(
                                    text = "Upcoming tasks",
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { viewModel.onTabSelected(0) },
                            text = {
                                Text(
                                    text = "Recent Activity",
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )

                    }

                }
            }
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.Center
            ) {
                if (uiState.selectedTab == 0) {
                    if (uiState.upcomingTasks.isNotEmpty()) {
                        uiState.upcomingTasks.forEach { task ->
                            TaskCard(
                                task = task,
                                onClick = { onTaskClick(task) }
                            )
                        }
                    } else {
                        Text(
                            text = "There are no upcoming tasks for today!",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = modifier
                                .padding(start = 26.dp, top = 16.dp, end = 26.dp, bottom = 0.dp)
                        )
                    }
                } else {
                    if (uiState.notifications.isNotEmpty()) {
                        uiState.notifications.forEach { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = { onNotificationClick(notification) }
                            )
                        }
                    } else {
                        Text(
                            text = "There are no notifications today!",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = modifier
                                .padding(start = 26.dp, top = 16.dp, end = 26.dp, bottom = 0.dp)
                        )
                    }
                }
                if (uiState.showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            viewModel.onDismissalBottomSheet()
                        },
                        sheetState = sheetState
                    ) {
                        // Sheet content
                        Button(onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    viewModel.onDismissalBottomSheet()
                                }
                            }
                        }) {
                            Text("Hide bottom sheet")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    JuggleTheme() {
        HomeScreen()
    }
}