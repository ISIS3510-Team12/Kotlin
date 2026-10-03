package com.team12kotlin.juggle.ui.projects.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.ProjectRepository
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Project
import com.team12kotlin.juggle.ui.dto.ProjectDeadlinePrediction
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProjectTaskFilter(val label: String) {
    IN_PROGRESS("In progress"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed")
}

data class ProjectDetailUiState(
    val project: Project? = null,
    val tasks: List<Task> = emptyList(),
    val prediction: ProjectDeadlinePrediction? = null,
    val selectedFilter: ProjectTaskFilter = ProjectTaskFilter.IN_PROGRESS,
    val errorMessage: String? = null,
) {
    val completedCount: Int get() = tasks.count { it.status == TaskStatus.COMPLETED }
    val totalCount: Int get() = tasks.size

    val filteredTasks: List<Task>
        get() = when (selectedFilter) {
            ProjectTaskFilter.IN_PROGRESS -> tasks.filter { it.status == TaskStatus.IN_PROGRESS }
            ProjectTaskFilter.UPCOMING -> tasks.filter { it.status == TaskStatus.NOT_STARTED }
            ProjectTaskFilter.COMPLETED -> tasks.filter { it.status == TaskStatus.COMPLETED }
        }

    val showPaceWarning: Boolean
        get() = prediction?.let { !it.willMeetDeadline } == true
}

class ProjectDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectRepository: ProjectRepository = Dependencies.projectRepository
    private val taskRepository: TaskRepository = Dependencies.taskRepository

    private val projectId: Int = savedStateHandle.get<Int>("projectId") ?: 0

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            runCatching {
                val project = projectRepository.getProject(projectId)
                val tasks = taskRepository.getAllTasks().filter { it.projectId == projectId }
                val prediction = runCatching {
                    projectRepository.getDeadlinePrediction(projectId)
                }.getOrNull()
                Triple(project, tasks, prediction)
            }
                .onSuccess { (project, tasks, prediction) ->
                    _uiState.update {
                        it.copy(project = project, tasks = tasks, prediction = prediction)
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun onFilterSelected(filter: ProjectTaskFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
