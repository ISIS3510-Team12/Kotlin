package com.team12kotlin.juggle.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlinedfilled.Circle_notifications
import com.composables.icons.materialsymbols.outlinedfilled.Assignment_late
import com.composables.icons.materialsymbols.roundedfilled.Groups
import com.composables.icons.materialsymbols.roundedfilled.List_alt
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.LoadingContent
import com.team12kotlin.juggle.ui.components.NotificationCard
import com.team12kotlin.juggle.ui.components.OverviewCard
import com.team12kotlin.juggle.ui.components.QuickActionItem
import com.team12kotlin.juggle.ui.connectivity.RefreshOnReconnect
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.dto.Notification
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.tasks.TaskCard
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.topbar.AppTopBar
import kotlinx.coroutines.FlowPreview
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun HomeScreen (
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onProfileClick: (NavigationDestination) -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onNextTaskClick: (Int) -> Unit = {},
    onNotificationClick: (Notification) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onUrgentTasksClick: () -> Unit = {},
    onCreateTaskClick: (Int) -> Unit = {},
    onCreateGroupClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.Home, uiState.isLoading)
    val isOnline = rememberIsOnline()
    RefreshOnReconnect(isOnline = isOnline, onReconnect = viewModel::load)
    val showNextTask = !(uiState.nextTask is NextTaskUiState.Error && isOnline == false)
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(onProfileClick = onProfileClick)
        },
    ) { innerPadding ->
        LoadingContent(isLoading = uiState.isLoading) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 136.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            uiState.errorMessage?.let { message ->
                                ErrorCard(
                                    message = message,
                                    onRetry = viewModel::load,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Column(modifier = Modifier) {
                                    Text(
                                        text = "Hi " + uiState.user.firstName + "!",
                                        style = MaterialTheme.typography.displayMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = LocalDate.now().format(HOME_DATE),
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        OutlinedButton(
                                            onClick = { viewModel.onQuickActionPress() },
                                            modifier = Modifier.padding(start = 8.dp),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onBackground
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "Quick Actions",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Overview",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        10.dp,
                                        Alignment.CenterHorizontally
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    OverviewCard(
                                        icon = MaterialSymbols.OutlinedFilled.Assignment_late,
                                        label = "Urgent Tasks",
                                        unit = "Tasks",
                                        count = uiState.urgentCount.takeIf { uiState.urgentLoaded },
                                        modifier = Modifier.weight(1f),
                                        onClick = onUrgentTasksClick,
                                    )
                                    OverviewCard(
                                        icon = MaterialSymbols.OutlinedFilled.Circle_notifications,
                                        label = "Today's Notifications",
                                        unit = "Notifications",
                                        count = uiState.notificationCount.takeIf { uiState.notificationsLoaded },
                                        modifier = Modifier.weight(1f),
                                        onClick = onNotificationsClick,
                                    )
                                }
                            }
                        }
                    }
                    if (showNextTask) {
                        item {
                            Text(
                                text = "Up next",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 0.dp)
                            )
                        }
                        item {
                            NextTaskSection(
                                state = uiState.nextTask,
                                modifier = Modifier.padding(vertical = 8.dp),
                                onTaskClick = onNextTaskClick,
                                onRetry = viewModel::loadNextTask
                            )
                        }
                    }
                    item {
                        PrimaryTabRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            selectedTabIndex = uiState.selectedTab
                        ) {
                            Tab(
                                selected = uiState.selectedTab == 0,
                                onClick = { viewModel.onTabSelected(0) },
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
                                selected = uiState.selectedTab == 1,
                                onClick = { viewModel.onTabSelected(1) },
                                text = {
                                    Text(
                                        text = "Recent activity",
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                    if (uiState.selectedTab == 0) {
                        items(uiState.upcomingTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onClick = { onTaskClick(task) }
                            )
                        }
                        if (uiState.upcomingTasks.isEmpty() && uiState.tasksLoaded) {
                            item {
                                Text(
                                    text = "No upcoming tasks",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp, top = 12.dp, end = 12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(uiState.notifications, key = { it.id }) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = { onNotificationClick(notification) }
                            )
                        }
                        if (uiState.notifications.isEmpty() && uiState.notificationsLoaded) {
                            item {
                                Text(
                                    text = "There are no notifications today!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp, top = 12.dp, end = 12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (uiState.showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.onDismissalBottomSheet() },
                        sheetState = sheetState
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(start = 30.dp, top = 10.dp, end = 30.dp, bottom = 30.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    10.dp,
                                    Alignment.CenterHorizontally
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Quick Actions",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                QuickActionItem(
                                    title = "Add task",
                                    description = "Create a new task instantly",
                                    icon = MaterialSymbols.RoundedFilled.List_alt,
                                    onClick = { uiState.firstGroupId?.let(onCreateTaskClick) }
                                )
                                QuickActionItem(
                                    title = "Create new group",
                                    description = "Create a group workspace",
                                    icon = MaterialSymbols.RoundedFilled.Groups,
                                    onClick = onCreateGroupClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val HOME_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    JuggleTheme {
        HomeScreen()
    }
}