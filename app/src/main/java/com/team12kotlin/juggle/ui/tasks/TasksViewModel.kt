package com.team12kotlin.juggle.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.team12kotlin.juggle.data.Dependencies

data class DrawerItem(
    val name: String,
    val pendingTasks: Int = 0,
    val selected: Boolean = false
)

data class TasksUiState(
    val query: String = "",
    val currentGroup: String = "",
    val personalTasks: List<Task> = emptyList(),
    val groupTasks: List<Task> = emptyList(),
    val groups: List<DrawerItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredPersonalTasks: List<Task>
        get() = personalTasks.filterFor(query)
    val filteredGroupTasks: List<Task>
        get() = groupTasks.filterFor(query)

    private fun List<Task>.filterFor(query: String): List<Task> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return this
        return filter {
            it.title.lowercase().contains(q) ||
                    (it.member?.lowercase()?.contains(q) == true)
        }
    }
}

class TasksViewModel(
    private val repository: TaskRepository = Dependencies.taskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getTasks() }
                .onSuccess { tasks ->
                    _uiState.update {
                        it.copy(
                            personalTasks = tasks.filter { task -> task.projectId == null },
                            groupTasks = tasks.filter { task -> task.projectId != null },
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onTaskClick(task: Task) {
    }

    fun onEditGroupClick() {
    }

    fun onAllTasksClick() {
    }

    fun onCreateTask() {
    }

    fun onGroupSelected(group: DrawerItem) {
        _uiState.update { state ->
            state.copy(
                currentGroup = group.name,
                groups = state.groups.map { it.copy(selected = it.name == group.name) }
            )
        }
    }
}
