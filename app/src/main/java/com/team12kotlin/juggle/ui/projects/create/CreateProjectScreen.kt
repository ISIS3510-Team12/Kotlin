package com.team12kotlin.juggle.ui.projects.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.team12kotlin.juggle.ui.projects.ProjectTopBar
import com.team12kotlin.juggle.ui.tasks.create.DatePickerField
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun CreateProjectScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateProjectViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProjectCreated: (Int) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.CreateProject)

    LaunchedEffect(uiState.createdProjectId) {
        uiState.createdProjectId?.let(onProjectCreated)
    }

    CreateProjectContent(
        modifier = modifier,
        uiState = uiState,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onDeadlineChange = viewModel::onDeadlineChange,
        onCreateClick = viewModel::onCreateProject,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateProjectContent(
    uiState: CreateProjectUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onDeadlineChange: (String) -> Unit = {},
    onCreateClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = { ProjectTopBar(title = "Create project", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create a new project",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Add the essentials now. You can update them later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Project name") },
                    placeholder = { Text("Enter project name") }
                )
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    placeholder = { Text("Describe the project") },
                    supportingText = { Text("Optional") }
                )
                DatePickerField(
                    value = uiState.deadline,
                    onDateSelected = onDeadlineChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCreateClick,
                enabled = uiState.canCreate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSaving) "Creating..." else "Create project")
            }

            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun CreateProjectContentPreview() {
    JuggleTheme {
        CreateProjectContent(uiState = CreateProjectUiState(groupId = 1))
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun CreateProjectContentFilledPreview() {
    JuggleTheme {
        CreateProjectContent(
            uiState = CreateProjectUiState(
                name = "Sprint 2 Planning",
                description = "Coordinate the team's upcoming sprint work",
                deadline = "09/18/2026",
                groupId = 1,
            )
        )
    }
}
