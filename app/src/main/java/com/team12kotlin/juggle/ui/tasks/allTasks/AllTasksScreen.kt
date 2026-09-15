package com.team12kotlin.juggle.ui.tasks.allTasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.tasks.TaskCard
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasksScreen(
    modifier: Modifier = Modifier,
    viewModel: AllTasksViewModel = viewModel(),
    onTaskClick: (Task) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "All tasks") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ButtonGroupFilter(
                filters = listOf("Urgent", "Due soon", "Assigned to me"),
                selectedIndex = uiState.selectedFilterIndex,
                onFilterSelected = { viewModel.onFilterSelected(it) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            (uiState.personalTasks + uiState.groupTasks).forEach { task ->
                TaskCard(
                    task = task,
                    onClick = {
                        viewModel.onTaskClick(task)
                        onTaskClick(task)
                    }
                )
            }
            Spacer(modifier = Modifier.height(136.dp))
        }
    }
}

@Composable
fun ButtonGroupFilter(
    filters: List<String>,
    selectedIndex: Int = 0,
    onFilterSelected: (index: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEachIndexed { index, filterText ->
            val isSelected = index == selectedIndex
            val startCorner = if (index == 0) 20.dp else 0.dp
            val endCorner = if (index == filters.lastIndex) 20.dp else 0.dp

            Button(
                onClick = { onFilterSelected(index) },
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                shape = RoundedCornerShape(
                    topStart = startCorner,
                    bottomStart = startCorner,
                    topEnd = endCorner,
                    bottomEnd = endCorner
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = filterText,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ButtonGroupFilterPreview() {
    JuggleTheme {
        ButtonGroupFilter(
            filters = listOf("Urgent", "Due soon", "Assigned to me"),
            onFilterSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AllTasksScreenPreview() {
    JuggleTheme {
        AllTasksScreen()
    }
}
