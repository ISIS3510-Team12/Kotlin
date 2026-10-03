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

enum class AllTasksFilter(val label: String) {
    URGENT("Urgent"),
    DUE_SOON("Due soon"),
    ASSIGNED_TO_ME("Assigned to me")
}

data class AllTasksUiState(
    val selectedFilterIndex: Int = 0,
    val tasks: List<Task> = emptyList(),
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

            val filter = selectedFilter()
            runCatching {
                repository.getAllTasks(
                    dueWithinDays = if (filter == AllTasksFilter.DUE_SOON) DUE_SOON_DAYS else null,
                    mine = filter == AllTasksFilter.ASSIGNED_TO_ME,
                    priority = filter == AllTasksFilter.URGENT
                )
            }
                .onSuccess { tasks ->
                    _uiState.update {
                        it.copy(
                            tasks = tasks,
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
        loadTasks()
    }

    fun onTaskClick(task: Task) {
    }

    private fun selectedFilter(): AllTasksFilter =
        AllTasksFilter.entries.getOrElse(_uiState.value.selectedFilterIndex) {
            AllTasksFilter.URGENT
        }

    private companion object {
        const val DUE_SOON_DAYS = 7
    }
}
