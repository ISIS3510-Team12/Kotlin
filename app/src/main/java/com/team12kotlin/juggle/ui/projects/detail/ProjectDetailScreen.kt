package com.team12kotlin.juggle.ui.projects.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team12kotlin.juggle.ui.dto.Project
import com.team12kotlin.juggle.ui.dto.ProjectDeadlinePrediction
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.projects.PaceWarningBanner
import com.team12kotlin.juggle.ui.projects.ProjectProgressBar
import com.team12kotlin.juggle.ui.projects.ProjectTopBar
import com.team12kotlin.juggle.ui.projects.StatSummary
import com.team12kotlin.juggle.ui.tasks.TaskCard
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.utils.splitDeadline

@Composable
fun ProjectDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: ProjectDetailViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onTaskClick: (Int) -> Unit = {},
    onAddTaskClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProjectDetailContent(
        modifier = modifier,
        uiState = uiState,
        onBackClick = onBackClick,
        onTaskClick = onTaskClick,
        onFilterSelected = viewModel::onFilterSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectDetailContent(
    uiState: ProjectDetailUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onTaskClick: (Int) -> Unit = {},
    onFilterSelected: (ProjectTaskFilter) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = { ProjectTopBar(title = "Project detail", onBackClick = onBackClick) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = uiState.project?.name.orEmpty(),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = uiState.project?.description.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatSummary(
                        label = "Progress",
                        value = "${uiState.completedCount} of ${uiState.totalCount} tasks complete"
                    )
                    ProjectProgressBar(
                        completed = uiState.completedCount,
                        total = uiState.totalCount,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                StatSummary(
                    label = "Deadline",
                    value = uiState.project?.deadline?.let { splitDeadline(it).first }.orEmpty()
                )
            }

            item {
                StatSummary(
                    label = "Current pace",
                    value = uiState.prediction
                        ?.let { "${it.paceTasksPerDay} completed tasks per day" }
                        .orEmpty()
                )
            }

            if (uiState.showPaceWarning) {
                item {
                    PaceWarningBanner(
                        title = "Pace too slow",
                        message = "Your current pace won't complete all planned tasks before the deadline."
                    )
                }
            }

            item {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProjectTaskFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = uiState.selectedFilter == filter,
                            onClick = { onFilterSelected(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors()
                        )
                    }
                }
            }

            items(uiState.filteredTasks, key = { it.id }) { task ->
                TaskCard(task = task, onClick = { onTaskClick(task.id) })
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
    }
}

private val SAMPLE_PROJECT = Project(
    id = 1,
    name = "Sprint 2 Planning",
    description = "Coordinate the team's upcoming sprint work",
    deadline = "2026-09-18T23:59:00",
    groupId = 1,
)

private val SAMPLE_TASKS = listOf(
    Task(
        id = 1,
        title = "Finish the sprint 2 Figma",
        status = TaskStatus.IN_PROGRESS,
        projectId = 1,
        assignees = listOf(User(userId = "u1", firstName = "Diego", lastName = "Munevar")),
    ),
    Task(
        id = 2,
        title = "Prepare the sprint review",
        status = TaskStatus.NOT_STARTED,
        projectId = 1,
        assignees = listOf(User(userId = "u1", firstName = "Diego", lastName = "Munevar")),
    ),
)

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun ProjectDetailContentPreview() {
    JuggleTheme {
        ProjectDetailContent(
            uiState = ProjectDetailUiState(project = SAMPLE_PROJECT, tasks = SAMPLE_TASKS)
        )
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun ProjectDetailContentPaceWarningPreview() {
    JuggleTheme {
        ProjectDetailContent(
            uiState = ProjectDetailUiState(
                project = SAMPLE_PROJECT,
                tasks = SAMPLE_TASKS,
                prediction = ProjectDeadlinePrediction(
                    projectId = 1,
                    totalTasks = 2,
                    completedTasks = 0,
                    remainingTasks = 2,
                    paceTasksPerDay = 0.4,
                    willMeetDeadline = false,
                )
            )
        )
    }
}
