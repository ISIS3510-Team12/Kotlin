package com.team12kotlin.juggle.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.team12kotlin.juggle.data.Dependencies

data class DrawerItem(
    val id: Int = 0,
    val name: String,
    val pendingTasks: Int = 0,
    val selected: Boolean = false
)

data class TasksUiState(
    val query: String = "",
    val currentGroup: String = "",
    val ownTasks: List<Task> = emptyList(),
    val groupTasks: List<Task> = emptyList(),
    val groups: List<DrawerItem> = emptyList(),
    val selectedGroupId: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredOwnTasks: List<Task>
        get() = ownTasks.filterFor(query)
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
    private val repository: TaskRepository = Dependencies.taskRepository,
    private val groupRepository: GroupRepository = Dependencies.groupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun refreshCurrentGroup() {
        _uiState.value.selectedGroupId?.let { loadTasksForGroup(it) }
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val groupsResult = runCatching { groupRepository.getGroups() }
            val groups = groupsResult.getOrDefault(emptyList())
            val selectedId = groups.firstOrNull()?.id

            val drawerItems = mutableListOf<DrawerItem>()
            for (group in groups) {
                drawerItems.add(
                    DrawerItem(
                        id = group.id,
                        name = group.name,
                        pendingTasks = group.pendingTaskCount,
                        selected = group.id == selectedId
                    )
                )
            }

            _uiState.update {
                it.copy(
                    groups = drawerItems,
                    selectedGroupId = selectedId,
                    currentGroup = drawerItems.firstOrNull()?.name ?: it.currentGroup,
                    isLoading = false,
                    errorMessage = groupsResult.exceptionOrNull()?.message
                )
            }

            if (selectedId != null) {
                loadTasksForGroup(selectedId)
            }
        }
    }

    fun loadTasksForGroup(groupId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val ownResult = runCatching { repository.getOwnTasks(groupId) }
            val groupResult = runCatching { repository.getGroupTasks(groupId) }

            val error = ownResult.exceptionOrNull() ?: groupResult.exceptionOrNull()

            _uiState.update {
                it.copy(
                    ownTasks = ownResult.getOrDefault(it.ownTasks),
                    groupTasks = groupResult.getOrDefault(it.groupTasks),
                    isLoading = false,
                    errorMessage = error?.message
                )
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
                selectedGroupId = group.id,
                groups = state.groups.map { it.copy(selected = it.id == group.id) }
            )
        }
        loadTasksForGroup(group.id)
    }
}
