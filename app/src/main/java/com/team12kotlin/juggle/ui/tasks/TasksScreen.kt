package com.team12kotlin.juggle.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
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

    LifecycleResumeEffect(Unit) {
        viewModel.refreshCurrentGroup()
        onPauseOrDispose { }
    }

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState(uiState.query)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onProfileClick = onProfileClick
                )
            },
            floatingActionButton = {
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
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            style = MaterialTheme.typography.headlineMedium, text = "Current Group"
                        )
                        Text(
                            style = MaterialTheme.typography.headlineSmall, text = uiState.currentGroup
                        )
                    }
                    TextButton(
                        onClick = {
                            viewModel.onEditGroupClick()
                            uiState.selectedGroupId?.let(onEditGroupClick)
                        },

                    ) {
                        Icon(
                            imageVector = MaterialSymbols.OutlinedFilled.Edit,
                            contentDescription = "Edit group"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit group")
                    }
                }
                SearchBar(
                    state = searchBarState,
                    inputField = {
                        SearchBarDefaults.InputField(
                            searchBarState = searchBarState,
                            textFieldState = textFieldState,
                            onSearch = { viewModel.onSearch(it) },
                            placeholder = {
                                Text("Search for a task...")
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = MaterialSymbols.Outlined.Search,
                                    contentDescription = "Search"
                                )
                            }
                        )
                    },
                    modifier = Modifier.padding(20.dp).fillMaxWidth()
                )
                TasksSection(
                    title = "Your pending tasks",
                    tasks = uiState.filteredOwnTasks,
                    onTaskClick = {
                        viewModel.onTaskClick(it)
                        onTaskClick(it)
                    }
                )
                TasksSection(
                    title = "Pending group tasks",
                    tasks = uiState.filteredGroupTasks,
                    onTaskClick = {
                        viewModel.onTaskClick(it)
                        onTaskClick(it)
                    }
                )
                Spacer(modifier = Modifier.height(136.dp))
            }
        }
    }
}

@Composable
private fun TasksSection(
    title: String,
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 10.dp),
            style = MaterialTheme.typography.headlineMedium
        )
        tasks.forEach { task ->
            TaskCard(
                task = task,
                onClick = { onTaskClick(task) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TasksScreenPreview() {
    JuggleTheme {
        TasksScreen()
    }
}
