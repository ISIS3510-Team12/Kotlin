package com.team12kotlin.juggle.ui.tasks.view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.team12kotlin.juggle.data.Dependencies

enum class TaskAction(val label: String) {
    MARK_AS_COMPLETE("Mark as complete"),
    EDIT_TASK("Edit Task"),
    DELETE_TASK("Delete Task"),
    ASK_FOR_HELP("Ask for help"),
    MARK_AS_STARTED("Mark as started"),
    ASSIGN_TIME_SLOT("Assign a time slot")
}

data class ViewTaskUiState(
    val task: Task,
    val isFabMenuExpanded: Boolean = false,
    val errorMessage: String? = null
)

class ViewTaskViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository: TaskRepository = Dependencies.taskRepository

    private val taskId: Int? = savedStateHandle.get<String>("taskId")?.toIntOrNull()

    private val _uiState = MutableStateFlow(ViewTaskUiState(task = MISSING_TASK))
    val uiState: StateFlow<ViewTaskUiState> = _uiState.asStateFlow()

    fun loadTask() {
        val id = taskId
        if (id == null) {
            return
        }
        viewModelScope.launch {
            runCatching { repository.getTask(id) }
                .onSuccess { task ->
                    _uiState.update { it.copy(task = task) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun onFabMenuToggle() {
        _uiState.update { it.copy(isFabMenuExpanded = !it.isFabMenuExpanded) }
    }

    fun onFabMenuDismiss() {
        _uiState.update { it.copy(isFabMenuExpanded = false) }
    }

    fun onTaskAction(action: TaskAction) {
        _uiState.update { it.copy(isFabMenuExpanded = false) }
        when (action) {
            TaskAction.MARK_AS_COMPLETE -> onMarkAsComplete()
            TaskAction.EDIT_TASK -> Unit
            TaskAction.DELETE_TASK -> onDeleteTask()
            TaskAction.ASK_FOR_HELP -> onAskForHelp()
            TaskAction.MARK_AS_STARTED -> onMarkAsStarted()
            TaskAction.ASSIGN_TIME_SLOT -> onAssignTimeSlot()
        }
    }

    fun onReminderToggle(enabled: Boolean) {
        val id = taskId ?: return
        val reminder = _uiState.value.task.reminders.firstOrNull() ?: return

        // Optimistically reflect the toggle, then persist it.
        _uiState.update { state ->
            state.copy(
                task = state.task.copy(
                    reminders = state.task.reminders.map {
                        if (it.id == reminder.id) it.copy(enabled = enabled) else it
                    }
                )
            )
        }

        viewModelScope.launch {
            runCatching { repository.setReminderEnabled(id, reminder.id, enabled) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun onRelatedTaskClick(task: Task) {
        // TODO: navigate to the related task's detail.
    }

    /** Runs a repository action for the current task, then refreshes it. */
    private fun runAction(action: suspend (taskId: Int) -> Unit) {
        val id = taskId ?: return
        viewModelScope.launch {
            runCatching { action(id) }
                .onSuccess { loadTask() }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    private fun onMarkAsComplete() = runAction { id ->
        repository.changeStatus(id, TaskStatus.COMPLETED)
    }

    private fun onMarkAsStarted() = runAction { id ->
        repository.changeStatus(id, TaskStatus.IN_PROGRESS)
    }

    private fun onAskForHelp() = runAction { id ->
        repository.updateTask(id, TaskUpdateRequest(needsHelp = true))
    }

    private fun onDeleteTask() {
        val id = taskId ?: return
        viewModelScope.launch {
            runCatching { repository.deleteTask(id) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    private fun onAssignTimeSlot() {
        // TODO: open the time-slot picker, then call repository.createTimeBlock(...).
    }

    private companion object {
        // Fallback shown when a task id is missing or not found.
        val MISSING_TASK = Task(
            id = 0,
            title = "Task not found",
            status = TaskStatus.NOT_STARTED
        )
    }
}
