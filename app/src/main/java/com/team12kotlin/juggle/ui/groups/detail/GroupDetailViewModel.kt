package com.team12kotlin.juggle.ui.groups.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.ProjectRepository
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val group: Group? = null,
    val relatedProjects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class GroupDetailViewModel(
    private val groupRepository: GroupRepository = Dependencies.groupRepository,
    private val projectRepository: ProjectRepository = Dependencies.projectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    fun load(groupId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                groupRepository.getGroup(groupId) to projectRepository.getProjects(groupId)
            }
                .onSuccess { (group, projects) ->
                    _uiState.update {
                        it.copy(group = group, relatedProjects = projects, isLoading = false)
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun onCreateProject() {
        // TODO: navigate to create-project once that flow exists
    }
}
