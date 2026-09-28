package com.team12kotlin.juggle.ui.tasks

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.dto.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DrawerItem(
    val name: String,
    val pendingTasks: Int = 0,
    val selected: Boolean = false
)

data class TasksUiState(
    val query: String = "",
    val currentGroup: String,
    val personalTasks: List<Task> = emptyList(),
    val groupTasks: List<Task> = emptyList(),
    val groups: List<DrawerItem> = emptyList()
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
            personalTasks = TaskRepository.personalTasks,
            groupTasks = TaskRepository.groupTasks,
            currentGroup = "API Pending",
            groups = listOf(
                DrawerItem(name = "App Devs", pendingTasks = 67, selected = true),
                DrawerItem(name = "Group 1"),
                DrawerItem(name = "Group 2")
            )
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

    fun onGroupSelected(group: DrawerItem) {
        _uiState.update { state ->
            state.copy(
                currentGroup = group.name,
                groups = state.groups.map { it.copy(selected = it.name == group.name) }
            )
        }
        // TODO: load the selected group's tasks when the data layer exists.
    }
}
