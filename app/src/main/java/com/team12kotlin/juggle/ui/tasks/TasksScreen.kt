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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Keyboard_arrow_down
import com.composables.icons.materialsymbols.outlined.Search
import com.composables.icons.materialsymbols.outlinedfilled.Edit
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(),
    onTaskClick: (Task) -> Unit = {},
    onEditGroupClick: () -> Unit = {},
    onAllTasksClick: () -> Unit = {},
    onNavigateToCreateTask: () -> Unit = {},
    onNavigateToEditGroup: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState(uiState.query)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
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
                    onEditGroupClick()
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
            tasks = uiState.filteredPersonalTasks,
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = {
                    viewModel.onAllTasksClick()
                    onAllTasksClick()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Outlined.Keyboard_arrow_down,
                        contentDescription = "All tasks"
                    )
                    Text(text = "All tasks")
                }
            }
        }
        // Clearance so the floating bottom bar never covers the button above,
        // even when scrolled to the end.
        Spacer(modifier = Modifier.height(136.dp))
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
