package com.team12kotlin.juggle.ui.tasks

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.dto.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TasksUiState(
    val query: String = "",
    val currentGroup: String,
    val personalTasks: List<Task> = emptyList(),
    val groupTasks: List<Task> = emptyList()
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

class TasksViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        TasksUiState(
            personalTasks = listOf(
                Task(id = "p1", title = "Finish Something bruh", isImportant = true),
                Task(id = "p2", title = "Terminar"),
                Task(id = "p3", title = "Work"),
                Task(id = "p4", title = "Finish Something bruh"),
                Task(id = "p5", title = "Terminar"),
                Task(id = "p6", title = "Work")
            ),
            groupTasks = listOf(
                Task(
                    id = "g1",
                    title = "Finish Something bruh",
                    member = "Diego",
                    isImportant = true
                ),
                Task(id = "g2", title = "Terminar", member = "Manuela"),
                Task(id = "g3", title = "Work", member = "Shaiel")
            ),
            currentGroup = "API Pending"
        )
    )
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(query = query) }
        // TODO: trigger repository search / navigation when data layer exists
    }

    fun onTaskClick(task: Task) {
        // TODO: navigate to task detail for task.id
    }

    fun onEditGroupClick() {
        // TODO: navigate to edit-group for currentGroup
    }

    fun onAllTasksClick() {
        // TODO: navigate to the all-tasks list
    }

    fun onCreateTask() {
        // TODO: navigate to create-task
    }
}
