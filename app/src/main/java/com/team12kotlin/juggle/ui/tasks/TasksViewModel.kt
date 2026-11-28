package com.team12kotlin.juggle.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.ui.utils.toUserMessage
import java.io.IOException

data class DrawerItem(
    val id: Int = 0,
    val name: String,
    val pendingTasks: Int = 0,
    val selected: Boolean = false
)

enum class TasksViewMode(val label: String) { List("List"), Board("Board") }

sealed interface MoveResult {
    /** [task] already has its new status; [from] is the one to go back to on undo. */
    data class Moved(val task: Task, val from: TaskStatus) : MoveResult
    data object Offline : MoveResult
    data object Failed : MoveResult
}

data class TasksUiState(
    val query: String = "",
    val currentGroup: String = "",
    val ownTasks: List<Task> = emptyList(),
    val groupTasks: List<Task> = emptyList(),
    val groups: List<DrawerItem> = emptyList(),
    val selectedGroupId: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loadFailed: Boolean = false,
    val viewMode: TasksViewMode = TasksViewMode.List,
    // Tasks the group endpoints leave out, plus the ones just moved while the lists catch up.
    val completedTasks: List<Task> = emptyList(),
    val completedUnavailable: Boolean = false,
    val movingTaskIds: Set<Int> = emptySet(),
    val lastMovedTaskId: Int? = null,
    val moveResult: MoveResult? = null
) {
    val filteredOwnTasks: List<Task>
        get() = ownTasks.filterNot { it.status == TaskStatus.COMPLETED }.filterFor(query)
    val filteredGroupTasks: List<Task>
        get() = groupTasks.filterNot { it.status == TaskStatus.COMPLETED }.filterFor(query)

    /** The tasks of one board column, with the one that was just moved on top. */
    fun boardTasks(status: TaskStatus): List<Task> =
        (ownTasks + groupTasks + completedTasks)
            .distinctBy { it.id }
            .filter { it.status == status }
            .filterFor(query)
            .sortedWith(
                compareBy<Task> { it.id != lastMovedTaskId }
                    .thenBy { it.deadline == null }
                    .thenBy { it.deadline }
                    .thenByDescending { it.isPriority }
                    .thenBy { it.id }
            )

    fun withStatus(taskId: Int, status: TaskStatus): TasksUiState {
        val current = (ownTasks + groupTasks + completedTasks).firstOrNull { it.id == taskId }
            ?: return this
        val moved = current.copy(status = status)
        fun List<Task>.replaced() = map { if (it.id == taskId) moved else it }
        return copy(
            ownTasks = ownTasks.replaced(),
            groupTasks = groupTasks.replaced(),
            completedTasks = if (status == TaskStatus.COMPLETED) {
                (completedTasks.replaced() + moved).distinctBy { it.id }
            } else {
                completedTasks.replaced()
            }
        )
    }

    fun withTask(task: Task): TasksUiState {
        fun List<Task>.replaced() = map { if (it.id == task.id) task else it }
        return copy(
            ownTasks = ownTasks.replaced(),
            groupTasks = groupTasks.replaced(),
            completedTasks = completedTasks.replaced()
        )
    }

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
        viewModelScope.launch {
            Dependencies.pendingTaskStore.synced.collect { loadGroups() }
        }
    }

    fun refreshCurrentGroup() {
        // Reloads groups too, so renames/new members made elsewhere show up; the selection is preserved.
        if (_uiState.value.selectedGroupId != null) loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !hasLoaded, errorMessage = null) }

            val groupsResult = runCatching { groupRepository.getGroupsCached() }
            val cachedGroups = groupsResult.getOrNull()
            val groups = cachedGroups?.data.orEmpty()
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
                    errorMessage = groupsResult.exceptionOrNull()?.toUserMessage(),
                    loadFailed = groupsResult.isFailure
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
        // The completed tasks load at the same time as the open ones; they touch different cached rows.
        if (_uiState.value.viewMode == TasksViewMode.Board) loadCompletedTasks(groupId)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = showLoading, errorMessage = null) }

            val groupResult = runCatching { repository.getGroupTasksCached(groupId) }
            val ownResult = runCatching { repository.getOwnTasksCached(groupId) }

            val error = ownResult.exceptionOrNull() ?: groupResult.exceptionOrNull()
            if (error is CancellationException) throw error

            val cachedOwnTasks = ownResult.getOrNull()
            val cachedGroupTasks = groupResult.getOrNull()

            hasLoaded = true
            _uiState.update {
                it.copy(
                    ownTasks = cachedOwnTasks?.data ?: it.ownTasks,
                    groupTasks = cachedGroupTasks?.data ?: it.groupTasks,
                    // A reopened task now comes with the open ones, so its leftover copy goes away.
                    completedTasks = if (error == null) {
                        it.completedTasks.filter { task ->
                            task.status == TaskStatus.COMPLETED || task.id in it.movingTaskIds
                        }
                    } else {
                        it.completedTasks
                    },
                    isLoading = false,
                    errorMessage = error?.toUserMessage(),
                    loadFailed = error != null
                )
            }
        }
    }

    fun showBoard() {
        _uiState.update { it.copy(viewMode = TasksViewMode.Board) }
        _uiState.value.selectedGroupId?.let(::loadCompletedTasks)
    }

    fun showList() {
        _uiState.update { it.copy(viewMode = TasksViewMode.List) }
    }

    private fun loadCompletedTasks(groupId: Int) {
        viewModelScope.launch {
            val result = runCatching { repository.getCompletedTasksCached(groupId) }
            val error = result.exceptionOrNull()
            if (error is CancellationException) throw error

            val cached = result.getOrNull()
            _uiState.update { state ->
                if (state.selectedGroupId != groupId) return@update state
                if (cached == null || cached.isCacheMiss) {
                    state.copy(completedUnavailable = true)
                } else {
                    // Tasks being moved right now keep what the board shows until the server answers.
                    state.copy(
                        completedTasks = (cached.data + state.completedTasks.filter { it.id in state.movingTaskIds })
                            .distinctBy { it.id },
                        completedUnavailable = false
                    )
                }
            }
        }
    }

    fun moveTask(task: Task, status: TaskStatus) {
        // Tasks waiting to upload have no server id yet, and a task can only be on one move at a time.
        val state = _uiState.value
        if (task.status == status || task.id < 0 || task.id in state.movingTaskIds) return

        _uiState.update {
            it.withStatus(task.id, status).copy(
                movingTaskIds = it.movingTaskIds + task.id,
                lastMovedTaskId = task.id,
                moveResult = null
            )
        }
        viewModelScope.launch {
            val result = runCatching { repository.changeStatus(task.id, status) }
            val error = result.exceptionOrNull()
            if (error is CancellationException) throw error

            val updated = result.getOrNull()
            _uiState.update {
                val settled = it.copy(movingTaskIds = it.movingTaskIds - task.id)
                if (updated != null) {
                    settled.withTask(updated).copy(moveResult = MoveResult.Moved(updated, task.status))
                } else {
                    settled.withStatus(task.id, task.status).copy(
                        lastMovedTaskId = null,
                        moveResult = if (error is IOException) MoveResult.Offline else MoveResult.Failed
                    )
                }
            }
            // The lists catch up with the server, which also moves the task out of the old column.
            if (updated != null) state.selectedGroupId?.let { loadTasksForGroup(it, showLoading = false) }
        }
    }

    fun onMoveResultShown() {
        _uiState.update { it.copy(moveResult = null) }
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
                groups = state.groups.map { it.copy(selected = it.id == group.id) },
                completedTasks = emptyList(),
                completedUnavailable = false
            )
        }
        loadTasksForGroup(group.id)
    }
}
