package com.team12kotlin.juggle.ui.tasks.edit

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.Cancel
import com.composables.icons.materialsymbols.outlined.Check
import com.team12kotlin.juggle.ui.tasks.create.AssignedMemberChip
import com.team12kotlin.juggle.ui.tasks.create.DatePickerField
import com.team12kotlin.juggle.ui.tasks.create.DropdownField
import com.team12kotlin.juggle.ui.tasks.create.EvidencesField
import com.team12kotlin.juggle.ui.tasks.create.RelatedTasksPicker
import com.team12kotlin.juggle.ui.tasks.create.TimePickerField
import com.team12kotlin.juggle.ui.tasks.create.ToggleRow
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    modifier: Modifier = Modifier,
    viewModel: EditTaskViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onTaskEdited: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            viewModel.onSaved()
            onTaskEdited()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Edit Task") },
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
        val horizontalPadding = 16.dp
        val contentPadding = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
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
                onOptionSelected = viewModel::onGroupSelected,
                modifier = contentPadding
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                modifier = contentPadding,
                label = { Text("Task title") },
                isError = uiState.title.isBlank()
            )

            DropdownField(
                value = uiState.taskType,
                label = "Task type",
                options = uiState.taskTypeOptions,
                onOptionSelected = viewModel::onTaskTypeSelected,
                modifier = contentPadding
            )

            DropdownField(
                value = uiState.selectedProjectName,
                label = "Associated project",
                options = uiState.projects.map { it.name },
                onOptionSelected = viewModel::onProjectSelected,
                modifier = contentPadding
            )

            Text(
                text = "Current assigned members",
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
                        onClick = { viewModel.onMemberToggled(member) }
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
                onDateSelected = viewModel::onDeadlineChange,
                modifier = contentPadding
            )
            TimePickerField(
                value = uiState.time,
                onTimeSelected = viewModel::onTimeChange,
                modifier = contentPadding
            )

            ToggleRow(
                label = "Is priority",
                checked = uiState.isPriority,
                onCheckedChange = viewModel::onPriorityChange,
                modifier = contentPadding
            )
            ToggleRow(
                label = "Needs help",
                checked = uiState.needsHelp,
                onCheckedChange = viewModel::onNeedsHelpChange,
                modifier = contentPadding
            )

            EvidencesField(
                imageBytes = uiState.evidenceBytes,
                onPhotoTaken = viewModel::onEvidenceTaken,
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
                    onQueryChange = viewModel::onRelatedQueryChange,
                    tasks = uiState.filteredRelatedTasks,
                    onToggle = viewModel::onRelatedTaskToggled,
                    modifier = contentPadding
                )
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = contentPadding
                )
            }

            Row(
                modifier = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = viewModel::onEditTask,
                    enabled = uiState.canSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = MaterialSymbols.Outlined.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isLoading) "Saving..." else "Edit task")
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = MaterialSymbols.Outlined.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel")
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditTaskScreenPreview() {
    JuggleTheme {
        EditTaskScreen()
    }
}
