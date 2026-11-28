package com.team12kotlin.juggle.ui.tasks

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.composables.icons.materialsymbols.outlined.Keyboard_arrow_down
import com.composables.icons.materialsymbols.outlined.Search
import com.composables.icons.materialsymbols.outlinedfilled.Edit
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.LoadingContent
import com.team12kotlin.juggle.ui.components.ViewToggle
import com.team12kotlin.juggle.ui.connectivity.RefreshOnReconnect
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.tasks.board.TaskBoard
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.topbar.AppTopBar
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(),
    onTaskClick: (Task) -> Unit = {},
    onEditGroupClick: (Int) -> Unit = {},
    onAllTasksClick: () -> Unit = {},
    onProfileClick: (NavigationDestination) -> Unit = {},
    onNavigateToCreateTask: (groupId: Int) -> Unit = {},
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToEditGroup: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.Tasks, uiState.isLoading)
    val isOnline = rememberIsOnline()
    val isOffline = isOnline == false
    RefreshOnReconnect(isOnline = isOnline, onReconnect = viewModel::loadGroups)

    LifecycleResumeEffect(Unit) {
        viewModel.refreshCurrentGroup()
        onPauseOrDispose { }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.moveResult) {
        when (val result = uiState.moveResult ?: return@LaunchedEffect) {
            is MoveResult.Moved -> {
                val answer = snackbarHostState.showSnackbar(
                    message = "Moved to ${result.task.status.label}",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Long
                )
                if (answer == SnackbarResult.ActionPerformed) viewModel.moveTask(result.task, result.from)
            }
            MoveResult.Offline -> snackbarHostState.showSnackbar(OFFLINE_MOVE_MESSAGE)
            MoveResult.Failed -> snackbarHostState.showSnackbar("We couldn't move the task. Try again.")
        }
        viewModel.onMoveResultShown()
    }

    // The "Your Groups" drawer lives in the Tasks view and opens from the top bar menu button.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GroupsDrawer(
                groups = uiState.groups,
                onGroupClick = { group ->
                    viewModel.onGroupSelected(group)
                    scope.launch { drawerState.close() }
                },
                onAllTasksClick = {
                    scope.launch { drawerState.close() }
                    viewModel.onAllTasksClick()
                    onAllTasksClick()
                },
                onNewGroupClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToCreateGroup()
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                AppTopBar(
                    showGroupIcon = true,
                    showCachedData = isOnline == false &&
                        (uiState.groups.isNotEmpty() || uiState.ownTasks.isNotEmpty() || uiState.groupTasks.isNotEmpty()),
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onProfileClick = onProfileClick
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 104.dp))
            },
            floatingActionButton = {
                // The board uses the whole screen, so the button would cover its columns.
                if (uiState.selectedGroupId != null && uiState.viewMode == TasksViewMode.List) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.onCreateTask()
                            uiState.selectedGroupId?.let(onNavigateToCreateTask)
                        },
                        modifier = Modifier.padding(bottom = 104.dp),
                        icon = {
                            Icon(
                                MaterialSymbols.Outlined.Add,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                        text = {
                            Text(
                                text = "Create Task",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        ) { innerPadding ->
            LoadingContent(isLoading = uiState.isLoading) {
                val tasksHeader: @Composable () -> Unit = {
                    TasksHeader(
                        uiState = uiState,
                        isOffline = isOffline,
                        onRetry = viewModel::loadGroups,
                        onEditGroupClick = {
                            viewModel.onEditGroupClick()
                            uiState.selectedGroupId?.let(onEditGroupClick)
                        },
                        onViewModeChange = { mode ->
                            if (mode == TasksViewMode.Board) viewModel.showBoard() else viewModel.showList()
                        },
                        onQueryChange = viewModel::onQueryChange
                    )
                }

                Crossfade(targetState = uiState.viewMode, label = "tasksView") { mode ->
                    if (mode == TasksViewMode.Board && uiState.selectedGroupId != null) {
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        ) {
                            tasksHeader()
                            TaskBoard(
                                uiState = uiState,
                                isOffline = isOffline,
                                modifier = Modifier.weight(1f),
                                onTaskClick = {
                                    viewModel.onTaskClick(it)
                                    onTaskClick(it)
                                },
                                onMoveTask = viewModel::moveTask,
                                onOfflineMoveAttempt = {
                                    scope.launch { snackbarHostState.showSnackbar(OFFLINE_MOVE_MESSAGE) }
                                }
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 136.dp)
                        ) {
                            item { tasksHeader() }
                            val couldNotLoad = uiState.loadFailed
                            val hasAnyTasks =
                                uiState.filteredOwnTasks.isNotEmpty() || uiState.filteredGroupTasks.isNotEmpty()
                            if (hasAnyTasks || !couldNotLoad) {
                                tasksSection(
                                    key = "own",
                                    title = "Your pending tasks",
                                    tasks = uiState.filteredOwnTasks,
                                    emptyMessage = "No pending tasks.",
                                    onTaskClick = {
                                        viewModel.onTaskClick(it)
                                        onTaskClick(it)
                                    }
                                )
                                tasksSection(
                                    key = "group",
                                    title = "Pending group tasks",
                                    tasks = uiState.filteredGroupTasks,
                                    emptyMessage = "No pending group tasks.",
                                    onTaskClick = {
                                        viewModel.onTaskClick(it)
                                        onTaskClick(it)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val OFFLINE_MOVE_MESSAGE = "No connection. Reconnect to move this task."

@Composable
private fun TasksHeader(
    uiState: TasksUiState,
    isOffline: Boolean,
    onRetry: () -> Unit,
    onEditGroupClick: () -> Unit,
    onViewModeChange: (TasksViewMode) -> Unit,
    onQueryChange: (String) -> Unit
) {
    Column {
        uiState.errorMessage?.let { message ->
            ErrorCard(
                message = message,
                onRetry = onRetry,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }
        if (uiState.selectedGroupId != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        style = MaterialTheme.typography.headlineMedium,
                        text = "Current Group"
                    )
                    Text(
                        style = MaterialTheme.typography.headlineSmall,
                        text = uiState.currentGroup
                    )
                }
                TextButton(
                    onClick = onEditGroupClick,
                    enabled = !isOffline,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.OutlinedFilled.Edit,
                        contentDescription = "Edit group"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit group")
                }
            }
            ViewToggle(
                entries = TasksViewMode.entries,
                selected = uiState.viewMode,
                label = { it.label },
                onSelect = onViewModeChange,
                modifier = Modifier.padding(start = 18.dp, top = 6.dp, end = 18.dp)
            )
        }
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            singleLine = true,
            shape = RoundedCornerShape(100.dp),
            placeholder = { Text("Search for a task...") },
            trailingIcon = {
                Icon(
                    imageVector = MaterialSymbols.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        )
    }
}

// A title and its tasks inside the screen's lazy list. A task can be in both sections, so keys are per section.
private fun LazyListScope.tasksSection(
    key: String,
    title: String,
    tasks: List<Task>,
    emptyMessage: String,
    onTaskClick: (Task) -> Unit
) {
    item(key = "$key-title") {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 18.dp),
            style = MaterialTheme.typography.headlineMedium
        )
        if (tasks.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )
        }
    }
    items(tasks, key = { "$key-${it.id}" }) { task ->
        TaskCard(
            task = task,
            onClick = { onTaskClick(task) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TasksScreenPreview() {
    JuggleTheme {
        TasksScreen()
    }
}
