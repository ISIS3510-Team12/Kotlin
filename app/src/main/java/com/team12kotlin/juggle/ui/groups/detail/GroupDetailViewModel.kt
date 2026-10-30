package com.team12kotlin.juggle.ui.groups.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.ProjectRepository
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.Project
import com.team12kotlin.juggle.ui.utils.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    private val _uiState = MutableStateFlow(GroupDetailUiState(isLoading = true))
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    private var loadedGroupId: Int? = null
    private var hasLoaded = false

    fun load(groupId: Int) {
        loadedGroupId = groupId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !hasLoaded, errorMessage = null) }
            runCatching {
                coroutineScope {
                    val group = async { groupRepository.getGroup(groupId) }
                    val projects = async { projectRepository.getProjects(groupId) }
                    group.await() to projects.await()
                }
            }
                .onSuccess { (group, projects) ->
                    hasLoaded = true
                    _uiState.update {
                        it.copy(group = group, relatedProjects = projects, isLoading = false)
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
        }
    }

    fun leaveGroup(onLeft: () -> Unit) {
        val groupId = loadedGroupId ?: return
        viewModelScope.launch {
            runCatching { groupRepository.leaveGroup(groupId) }
                .onSuccess { onLeft() }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.toUserMessage()) }
                }
        }
    }

    fun onCreateProject() {
        // TODO: navigate to create-project once that flow exists
    }
}
