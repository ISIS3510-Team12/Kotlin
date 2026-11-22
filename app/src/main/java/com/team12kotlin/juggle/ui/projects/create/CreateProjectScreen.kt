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
import com.team12kotlin.juggle.ui.components.BackTopBar
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.SupportingTextWithCounter
import com.team12kotlin.juggle.ui.components.OfflineActionNotice
import com.team12kotlin.juggle.ui.components.ShakeToReset
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.projects.ProjectFormContent
import com.team12kotlin.juggle.ui.tasks.create.DatePickerField
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.utils.ProjectFieldLimits

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

    ShakeToReset(
        enabled = !uiState.isSaving,
        onReset = viewModel::onResetForm
    )

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
    val isOffline = rememberIsOnline() == false
    Scaffold(
        modifier = modifier,
        topBar = { BackTopBar(title = "Create project", onBack = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            ProjectFormContent(
                title = "Create a new project",
                subtitle = "Add the essentials now. You can update them later.",
                nameValue = uiState.name,
                onNameChange = onNameChange,
                descriptionValue = uiState.description,
                onDescriptionChange = onDescriptionChange,
                deadlineValue = uiState.deadline,
                onDeadlineChange = onDeadlineChange,
                isOffline = isOffline
            )

            Button(
                onClick = onCreateClick,
                enabled = uiState.canCreate && !isOffline,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSaving) "Creating..." else "Create project")
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
