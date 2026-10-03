package com.team12kotlin.juggle.ui.tasks.view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.RelatedTask
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.telemetry.TelemetryReporter
import java.time.LocalDateTime

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
    val evidenceBytes: ByteArray? = null,
    val isFabMenuExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ViewTaskViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository: TaskRepository = Dependencies.taskRepository

    // Analytics: one visit to this screen, from when it opens until it is closed.
    private val openedAt = LocalDateTime.now()
    private var taskLoaded = false
    private var progressUpdated = false
    private var taskDeleted = false

    private val taskId: Int? = savedStateHandle.get<String>("taskId")?.toIntOrNull()

    private val _uiState = MutableStateFlow(ViewTaskUiState(task = MISSING_TASK))
    val uiState: StateFlow<ViewTaskUiState> = _uiState.asStateFlow()

    fun loadTask() {
        val id = taskId
        if (id == null) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val task = repository.getTask(id)
                val photo = if (task.hasPhoto) repository.getTaskPhoto(id) else null
                task to photo
            }
                .onSuccess { (task, photo) ->
                    taskLoaded = true
                    _uiState.update { it.copy(task = task, evidenceBytes = photo, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message, isLoading = false) }
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

    fun onRelatedTaskClick(related: RelatedTask) {
        // TODO: navigate to the related task's detail.
    }



    /** Runs a repository action for the current task, then refreshes it. */
    private fun runAction(updatesProgress: Boolean = false, action: suspend (taskId: Int) -> Unit) {
        val id = taskId ?: return
        viewModelScope.launch {
            runCatching { action(id) }
                .onSuccess {
                    if (updatesProgress) progressUpdated = true
                    loadTask()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    private fun onMarkAsComplete() = runAction(updatesProgress = true) { id ->
        repository.changeStatus(id, TaskStatus.COMPLETED)
    }

    private fun onMarkAsStarted() = runAction(updatesProgress = true) { id ->
        repository.changeStatus(id, TaskStatus.IN_PROGRESS)
    }

    private fun onAskForHelp() = runAction { id ->
        repository.updateTask(id, TaskUpdateRequest(needsHelp = true))
    }

    private fun onDeleteTask() {
        val id = taskId ?: return
        viewModelScope.launch {
            runCatching { repository.deleteTask(id) }
                .onSuccess { taskDeleted = true }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    override fun onCleared() {
        reportTaskDetailSession()
        super.onCleared()
    }

    private fun reportTaskDetailSession() {
        val id = taskId ?: return
        if (!taskLoaded || taskDeleted) return
        TelemetryReporter.reportTaskDetailSession(id, openedAt, LocalDateTime.now(), progressUpdated)
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
