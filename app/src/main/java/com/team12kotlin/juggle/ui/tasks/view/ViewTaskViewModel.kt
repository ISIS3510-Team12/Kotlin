package com.team12kotlin.juggle.ui.tasks.view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus
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
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository = Dependencies.taskRepository
) : ViewModel() {

    private val taskId: Int? = savedStateHandle.get<String>("taskId")?.toIntOrNull()

    private val _uiState = MutableStateFlow(ViewTaskUiState(task = MISSING_TASK))
    val uiState: StateFlow<ViewTaskUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

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
        _uiState.update { state ->
            val reminder = state.task.reminder ?: return@update state
            state.copy(task = state.task.copy(reminder = reminder.copy(enabled = enabled)))
        }
    }

    fun onRelatedTaskClick(task: Task) {
        // TODO: navigate to the related task's detail.
    }

    private fun onMarkAsComplete() {
        // TODO: persist the completed status when the data layer exists.
    }

    private fun onDeleteTask() {
        // TODO: delete the task and navigate back when the data layer exists.
    }

    private fun onAskForHelp() {
        // TODO: flag the task as needing help when the data layer exists.
    }

    private fun onMarkAsStarted() {
        // TODO: move the task to in-progress when the data layer exists.
    }

    private fun onAssignTimeSlot() {
        // TODO: open the time-slot picker when it exists.
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
