package com.team12kotlin.juggle.ui.projects.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.ProjectRepository
import com.team12kotlin.juggle.ui.dto.ProjectCreateRequest
import com.team12kotlin.juggle.utils.toIsoDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateProjectUiState(
    val name: String = "",
    val description: String = "",
    val deadline: String = "",
    val groupId: Int? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val createdProjectId: Int? = null,
) {
    val canCreate: Boolean
        get() = name.isNotBlank() && deadline.isNotBlank() && groupId != null && !isSaving
}

class CreateProjectViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectRepository: ProjectRepository = Dependencies.projectRepository
    private val groupRepository: GroupRepository = Dependencies.groupRepository

    private val navGroupId: Int? = savedStateHandle.get<Int>("groupId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(CreateProjectUiState(groupId = navGroupId))
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    init {
        if (navGroupId == null) loadFallbackGroup()
    }

    /**
     * Only used if the screen is opened without a groupId
     */
    private fun loadFallbackGroup() {
        viewModelScope.launch {
            runCatching {
                val groups = groupRepository.getGroups()
                groups.firstOrNull { !it.isPersonal }?.id ?: groups.firstOrNull()?.id
            }
                .onSuccess { id -> _uiState.update { it.copy(groupId = id) } }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onDeadlineChange(value: String) {
        _uiState.update { it.copy(deadline = value) }
    }

    fun onCreateProject() {
        val state = _uiState.value
        if (!state.canCreate) return

        val isoDeadline = toIsoDateTime(state.deadline, "")
        if (isoDeadline == null) {
            _uiState.update { it.copy(errorMessage = "Pick a valid date") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                projectRepository.createProject(
                    ProjectCreateRequest(
                        name = state.name.trim(),
                        description = state.description.trim(),
                        deadline = isoDeadline,
                        groupId = state.groupId ?: return@launch
                    )
                )
            }
                .onSuccess { project ->
                    _uiState.update { it.copy(isSaving = false, createdProjectId = project.id) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.message) }
                }
        }
    }
}
