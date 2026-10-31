package com.team12kotlin.juggle.ui.tasks.create

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.LoadingContent
import com.team12kotlin.juggle.ui.components.SupportingTextWithCounter
import com.team12kotlin.juggle.ui.components.OfflineActionNotice
import com.team12kotlin.juggle.ui.components.ShakeToReset
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.utils.TaskFieldLimits
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun CreateTaskScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateTaskViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onTaskCreated: (taskId: Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.CreateTask, uiState.isLoading)

    LaunchedEffect(uiState.createdTaskId) {
        uiState.createdTaskId?.let(onTaskCreated)
    }

    ShakeToReset(
        enabled = !uiState.isLoading,
        onReset = viewModel::onResetForm
    )

    CreateTaskContent(
        modifier = modifier,
        uiState = uiState,
        onBackClick = onBackClick,
        onGroupSelected = viewModel::onGroupSelected,
        onTitleChange = viewModel::onTitleChange,
        onTaskTypeSelected = viewModel::onTaskTypeSelected,
        onProjectSelected = viewModel::onProjectSelected,
        onMemberToggled = viewModel::onMemberToggled,
        onDeadlineChange = viewModel::onDeadlineChange,
        onTimeChange = viewModel::onTimeChange,
        onPriorityChange = viewModel::onPriorityChange,
        onNeedsHelpChange = viewModel::onNeedsHelpChange,
        onEvidenceTaken = viewModel::onEvidenceTaken,
        onRelatedQueryChange = viewModel::onRelatedQueryChange,
        onRelatedTaskToggled = viewModel::onRelatedTaskToggled,
        onCreateClick = viewModel::onCreateTask,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateTaskContent(
    uiState: CreateTaskUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onGroupSelected: (String) -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onTaskTypeSelected: (String) -> Unit = {},
    onProjectSelected: (String) -> Unit = {},
    onMemberToggled: (AssignableMember) -> Unit = {},
    onDeadlineChange: (String) -> Unit = {},
    onTimeChange: (String) -> Unit = {},
    onPriorityChange: (Boolean) -> Unit = {},
    onNeedsHelpChange: (Boolean) -> Unit = {},
    onEvidenceTaken: (ByteArray, String) -> Unit = { _, _ -> },
    onRelatedQueryChange: (String) -> Unit = {},
    onRelatedTaskToggled: (RelatedTask) -> Unit = {},
    onCreateClick: () -> Unit = {},
) {
    val isOffline = rememberIsOnline() == false

    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopBar(title = "Create Task", onBack = onBackClick)
        }
    ) { innerPadding ->
        // Horizontal padding is applied per-item rather than on the
        // whole Column, so the assigned-members strip can scroll edge-to-edge.
        val horizontalPadding = 16.dp
        val contentPadding = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
        LoadingContent(isLoading = uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DropdownField(
                    value = uiState.selectedGroupName,
                    label = "Group",
                    options = uiState.groups.map { it.name },
                    onOptionSelected = onGroupSelected,
                    modifier = contentPadding
                )

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    modifier = contentPadding,
                    singleLine = true,
                    label = { Text("Task title") },
                    supportingText = {
                        SupportingTextWithCounter(
                            text = "",
                            length = uiState.title.length,
                            maxLength = TaskFieldLimits.TITLE_MAX_LENGTH
                        )
                    }
                )

                DropdownField(
                    value = uiState.taskType,
                    label = "Task type",
                    options = uiState.taskTypeOptions,
                    onOptionSelected = onTaskTypeSelected,
                    modifier = contentPadding
                )

                DropdownField(
                    value = uiState.selectedProjectName,
                    label = "Associated project",
                    options = uiState.projects.map { it.name },
                    onOptionSelected = onProjectSelected,
                    modifier = contentPadding
                )

                Text(
                    text = "Assigned members",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = contentPadding
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    uiState.assignedMembers.forEach { member ->
                        AssignedMemberChip(
                            member = member,
                            onClick = { onMemberToggled(member) }
                        )
                    }
                }

                Text(
                    text = "Current deadline and timing",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = contentPadding
                )
                DatePickerField(
                    value = uiState.deadline,
                    onDateSelected = onDeadlineChange,
                    modifier = contentPadding
                )
                TimePickerField(
                    value = uiState.time,
                    onTimeSelected = onTimeChange,
                    modifier = contentPadding
                )

                ToggleRow(
                    label = "Is priority",
                    checked = uiState.isPriority,
                    onCheckedChange = onPriorityChange,
                    modifier = contentPadding
                )
                ToggleRow(
                    label = "Needs help",
                    checked = uiState.needsHelp,
                    onCheckedChange = onNeedsHelpChange,
                    modifier = contentPadding
                )

                EvidencesField(
                    imageBytes = uiState.evidenceBytes,
                    onPhotoTaken = onEvidenceTaken,
                    modifier = contentPadding
                )

                if (uiState.relatedTasks.isNotEmpty()) {
                    Text(
                        text = "Related tasks",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = contentPadding
                    )
                    RelatedTasksPicker(
                        query = uiState.relatedQuery,
                        onQueryChange = onRelatedQueryChange,
                        tasks = uiState.filteredRelatedTasks,
                        onToggle = onRelatedTaskToggled,
                        modifier = contentPadding
                    )
                }

                if (!isOffline) {
                    uiState.errorMessage?.let { message ->
                        ErrorCard(message = message, modifier = contentPadding)
                    }
                }

                OfflineActionNotice(
                    isOffline = isOffline,
                    modifier = contentPadding
                )

                Button(
                    onClick = onCreateClick,
                    enabled = uiState.canCreate && !isOffline,
                    modifier = contentPadding
                ) {
                    Icon(imageVector = MaterialSymbols.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isLoading) "Creating..." else "Create Task")
                }

                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun CreateTaskContentEmptyPreview() {
    JuggleTheme {
        CreateTaskContent(
            uiState = CreateTaskUiState(
                taskTypeOptions = listOf("Coding", "Design", "Research", "Writing", "Meeting")
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun CreateTaskContentFilledPreview() {
    JuggleTheme {
        CreateTaskContent(
            uiState = CreateTaskUiState(
                title = "Design the onboarding screens",
                taskType = "Design",
                taskTypeOptions = listOf("Coding", "Design", "Research", "Writing", "Meeting"),
                deadline = "09/18/2026",
                time = "10:00",
                isPriority = true,
                assignedMembers = listOf(
                    AssignableMember(name = "Diego", userId = "u1", selected = true),
                    AssignableMember(name = "Manuela", userId = "u2"),
                    AssignableMember(name = "Shaiel", userId = "u3"),
                ),
                groups = listOf(
                    Group(name = "The best group", description = "un grupo")
                )
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun CreateTaskContentErrorPreview() {
    JuggleTheme {
        CreateTaskContent(
            uiState = CreateTaskUiState(
                title = "A".repeat(TaskFieldLimits.TITLE_MAX_LENGTH),
                errorMessage = "Pick a valid deadline",
            )
        )
    }
}