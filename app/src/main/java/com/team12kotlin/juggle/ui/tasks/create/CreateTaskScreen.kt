package com.team12kotlin.juggle.ui.tasks.create

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateTaskViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onTaskCreated: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Create Task") },
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
        // Horizontal padding is applied per-item (via `contentPadding`) rather than on the
        // whole Column, so the assigned-members strip can scroll edge-to-edge.
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
            CurrentGroupTab(groupName = uiState.currentGroup, modifier = contentPadding)

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                modifier = contentPadding,
                singleLine = true,
                label = { Text("Task title") }
            )

            DropdownField(
                value = uiState.taskType,
                label = "Task type",
                options = uiState.taskTypeOptions,
                onOptionSelected = viewModel::onTaskTypeSelected,
                modifier = contentPadding
            )

            DropdownField(
                value = uiState.associatedProject,
                label = "Associated project",
                options = uiState.projectOptions,
                onOptionSelected = viewModel::onAssociatedProjectSelected,
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

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                modifier = contentPadding,
                label = { Text("Notes & deliverable link") }
            )

            if (uiState.relatedTasks.isNotEmpty()) {
                Text(
                    text = "Related tasks",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = contentPadding
                )
                uiState.relatedTasks.forEach { related ->
                    RelatedTaskItem(
                        relatedTask = related,
                        onToggle = { viewModel.onRelatedTaskToggled(related) },
                        modifier = contentPadding
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.onCreateTask()
                    onTaskCreated()
                },
                enabled = uiState.canCreate,
                modifier = contentPadding
            ) {
                Icon(imageVector = MaterialSymbols.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Task")
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateTaskScreenPreview() {
    JuggleTheme {
        CreateTaskScreen()
    }
}
