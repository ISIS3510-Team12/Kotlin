package com.team12kotlin.juggle.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Task
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.ui.utils.toUserMessage

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
                    it.assignees.any { member -> member.firstName.lowercase().contains(q) }
        }
    }
}

class TasksViewModel(
    private val repository: TaskRepository = Dependencies.taskRepository,
    private val groupRepository: GroupRepository = Dependencies.groupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState(isLoading = true))
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private var hasLoaded = false

    init {
        loadGroups()
    }

    fun refreshCurrentGroup() {
        // Reloads groups too, so renames/new members made elsewhere show up; the selection is preserved.
        if (_uiState.value.selectedGroupId != null) loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !hasLoaded, errorMessage = null) }

            val groupsResult = runCatching { groupRepository.getGroups() }
            val groups = groupsResult.getOrDefault(emptyList())
            val selectedId = groups.firstOrNull { it.id == _uiState.value.selectedGroupId }?.id
                ?: groups.firstOrNull()?.id

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
                    currentGroup = drawerItems.firstOrNull { item -> item.selected }?.name ?: it.currentGroup,
                    errorMessage = groupsResult.exceptionOrNull()?.toUserMessage()
                )
            }

            if (selectedId != null) {
                // Keep isLoading true: loading the tasks for the selected group is part of the same load.
                loadTasksForGroup(selectedId, showLoading = !hasLoaded)
            } else {
                hasLoaded = true
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadTasksForGroup(groupId: Int, showLoading: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = showLoading, errorMessage = null) }

            val (ownResult, groupResult) = coroutineScope {
                val own = async { runCatching { repository.getOwnTasks(groupId) } }
                val group = async { runCatching { repository.getGroupTasks(groupId) } }
                own.await() to group.await()
            }

            val error = ownResult.exceptionOrNull() ?: groupResult.exceptionOrNull()
            if (error is CancellationException) throw error

            hasLoaded = true
            _uiState.update {
                it.copy(
                    ownTasks = ownResult.getOrDefault(it.ownTasks),
                    groupTasks = groupResult.getOrDefault(it.groupTasks),
                    isLoading = false,
                    errorMessage = error?.toUserMessage()
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
