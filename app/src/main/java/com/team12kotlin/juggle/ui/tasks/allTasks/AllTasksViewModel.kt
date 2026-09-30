package com.team12kotlin.juggle.ui.tasks.allTasks

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

data class AllTasksUiState(
    val selectedFilterIndex: Int = 0,
    val personalTasks: List<Task> = emptyList(),
    val groupTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AllTasksViewModel(
    private val repository: TaskRepository = Dependencies.taskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllTasksUiState())
    val uiState: StateFlow<AllTasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getAllTasks() }
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

    fun onFilterSelected(index: Int) {
        _uiState.update { it.copy(selectedFilterIndex = index) }
    }

    fun onTaskClick(task: Task) {
    }
}
