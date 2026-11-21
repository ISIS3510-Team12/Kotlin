package com.team12kotlin.juggle.ui.projects.edit

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.LoadingContent
import com.team12kotlin.juggle.ui.components.OfflineActionNotice
import com.team12kotlin.juggle.ui.components.ShakeToReset
import com.team12kotlin.juggle.ui.components.SupportingTextWithCounter
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.tasks.create.DatePickerField
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.utils.ProjectFieldLimits

@Composable
fun EditProjectScreen(
    projectId: Int,
    modifier: Modifier = Modifier,
    viewModel: EditProjectViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProjectSaved: () -> Unit = {},
) {
    LaunchedEffect(projectId) { viewModel.load(projectId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.EditProject, uiState.isLoading) 

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onProjectSaved()
    }

    ShakeToReset(
        enabled = !uiState.isLoading,
        onReset = viewModel::onResetForm
    )

    EditProjectContent(
        modifier = modifier,
        uiState = uiState,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onDeadlineChange = viewModel::onDeadlineChange,
        onSaveClick = viewModel::onSaveProject,
    )
}

@Composable
internal fun EditProjectContent(
    uiState: EditProjectUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onDeadlineChange: (String) -> Unit = {},
    onSaveClick: () -> Unit = {},
) {
    val isOffline = rememberIsOnline() == false
    Scaffold(
        modifier = modifier,
        topBar = { BackTopBar(title = "Edit project", onBack = onBackClick) }
    ) { innerPadding ->
        LoadingContent(isLoading = uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Edit project",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Update the project details.",
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
                        placeholder = { Text("Enter project name") },
                        supportingText = {
                            SupportingTextWithCounter(
                                text = "",
                                length = uiState.name.length,
                                maxLength = ProjectFieldLimits.NAME_MAX_LENGTH
                            )
                        }
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description") },
                        placeholder = { Text("Describe the project") },
                        supportingText = {
                            SupportingTextWithCounter(
                                text = "Required",
                                length = uiState.description.length,
                                maxLength = ProjectFieldLimits.DESCRIPTION_MAX_LENGTH
                            )
                        }
                    )
                    DatePickerField(
                        value = uiState.deadline,
                        onDateSelected = onDeadlineChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OfflineActionNotice(isOffline = isOffline)

                Button(
                    onClick = onSaveClick,
                    enabled = uiState.canSave && !isOffline,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save changes")
                }

                if (!isOffline) {
                    uiState.errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(8.dp))
                        ErrorCard(message = message)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 414, heightDp = 892)
@Composable
private fun EditProjectContentPreview() {
    JuggleTheme {
        EditProjectContent(
            uiState = EditProjectUiState(
                projectId = 1,
                groupId = 1,
                name = "Sprint 2 Planning",
                description = "Coordinate the team's upcoming sprint work",
                deadline = "09/18/2026",
            )
        )
    }
}