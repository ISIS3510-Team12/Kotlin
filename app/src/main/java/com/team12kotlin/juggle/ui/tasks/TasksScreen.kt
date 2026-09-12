package com.team12kotlin.juggle.ui.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Search
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(),
    onTaskClick: (Task) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState(uiState.query)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
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
            modifier = Modifier.padding(20.dp)
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
