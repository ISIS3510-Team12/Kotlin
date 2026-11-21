package com.team12kotlin.juggle.ui.projects.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.ProjectRepository
import com.team12kotlin.juggle.ui.dto.ProjectUpdateRequest
import com.team12kotlin.juggle.ui.utils.ProjectFieldLimits
import com.team12kotlin.juggle.ui.utils.toUserMessage
import com.team12kotlin.juggle.utils.toIsoDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProjectUiState(
    val projectId: Int = 0,
    val name: String = "",
    val description: String = "",
    val deadline: String = "",
    val groupId: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false
) {
    val canSave: Boolean
        get() = name.isNotBlank() &&
                description.isNotBlank() &&
                deadline.isNotBlank() &&
                !isLoading
}

class EditProjectViewModel(
    private val projectRepository: ProjectRepository = Dependencies.projectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProjectUiState(isLoading = true))
    val uiState: StateFlow<EditProjectUiState> = _uiState.asStateFlow()

    private var loadedProjectId: Int? = null

    // Last saved name, description, and deadline, restored when the form is reset
    private var savedName: String? = null
    private var savedDescription: String = ""
    private var savedDeadline: String = ""

    fun load(projectId: Int) {
        if (loadedProjectId == projectId) return
        loadedProjectId = projectId

        viewModelScope.launch {
            _uiState.update { it.copy(projectId = projectId, isLoading = true, errorMessage = null) }
            runCatching {
                coroutineScope {
                    projectRepository.getProject(projectId)
                }
            }
                .onSuccess { project ->
                    val displayDeadline = isoToPickerDate(project.deadline)
                    savedName = project.name
                    savedDescription = project.description
                    savedDeadline = displayDeadline

                    _uiState.update {
                        it.copy(
                            name = project.name,
                            description = project.description,
                            deadline = displayDeadline,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    loadedProjectId = null
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value.take(ProjectFieldLimits.NAME_MAX_LENGTH)) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update {
            it.copy(description = value.take(ProjectFieldLimits.DESCRIPTION_MAX_LENGTH))
        }
    }

    fun onDeadlineChange(value: String) {
        _uiState.update { it.copy(deadline = value) }
    }

    fun onResetForm() {
        val name = savedName ?: return
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                name = name,
                description = savedDescription,
                deadline = savedDeadline,
                errorMessage = null
            )
        }
    }

    fun onSaveProject() {
        val state = _uiState.value
        if (!state.canSave) return

        val isoDeadline = toIsoDateTime(state.deadline, "")
        if (isoDeadline == null) {
            _uiState.update { it.copy(errorMessage = "Pick a valid date") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                projectRepository.updateProject(
                    state.projectId,
                    ProjectUpdateRequest(
                        name = state.name.trim(),
                        description = state.description.trim(),
                        deadline = isoDeadline,
                        groupId = state.groupId
                    )
                )
            }
                .onSuccess {
                    savedName = state.name.trim()
                    savedDescription = state.description.trim()
                    savedDeadline = state.deadline
                    _uiState.update { it.copy(isLoading = false, saved = true) }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
        }
    }
}

private fun isoToPickerDate(iso: String?): String {
    if (iso == null) return ""
    val parts = iso.take(10).split("-")
    return if (parts.size == 3) "${parts[1]}/${parts[2]}/${parts[0]}" else iso
}