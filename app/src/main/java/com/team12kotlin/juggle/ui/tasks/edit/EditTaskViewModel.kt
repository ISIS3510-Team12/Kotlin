package com.team12kotlin.juggle.ui.tasks.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest
import com.team12kotlin.juggle.ui.tasks.create.AssignableMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.team12kotlin.juggle.data.Dependencies

/**
 * UI state for the Edit Task screen. Mirrors the create form but is pre-filled from the
 * task being edited and keyed by a "selected task" dropdown.
 */
data class EditTaskUiState(
    val selectedTask: String? = null,
    val taskType: String? = null,
    val assignedMembers: List<AssignableMember> = emptyList(),
    val deadline: String = "",
    val time: String = "",
    val isPriority: Boolean = false,
    val needsHelp: Boolean = false,
    val notes: String = "",
    val taskOptions: List<String> = emptyList(),
    val taskTypeOptions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /** A task must be selected before edits can be saved. */
    val canSave: Boolean get() = !selectedTask.isNullOrBlank()
}

class EditTaskViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository: TaskRepository = Dependencies.taskRepository

    private val taskId: Int? = savedStateHandle.get<String>("taskId")?.toIntOrNull()

    private val taskTypeOptions: List<String> =
        listOf("Coding", "Design", "Research", "Writing", "Meeting")

    private var loadedTasks: List<Task> = emptyList()

    private val _uiState = MutableStateFlow(
        EditTaskUiState(taskTypeOptions = taskTypeOptions)
    )
    val uiState: StateFlow<EditTaskUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    fun loadTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val tasks = repository.getTasks()
                val selected = taskId?.let { repository.getTask(it) }
                tasks to selected
            }
                .onSuccess { (tasks, selected) ->
                    loadedTasks = tasks
                    _uiState.value = stateForTask(selected, tasks)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun stateForTask(task: Task?, tasks: List<Task>): EditTaskUiState =
        EditTaskUiState(
            selectedTask = task?.title,
            taskType = task?.taskType?.takeIf { it.isNotBlank() },
            deadline = task?.deadline.orEmpty(),
            isPriority = task?.isPriority ?: false,
            needsHelp = task?.needsHelp ?: false,
            notes = task?.description.orEmpty(),
            assignedMembers = task?.assignees
                ?.map { AssignableMember(name = it.firstName, selected = true) }
                ?: emptyList(),
            taskOptions = tasks.map { it.title }.distinct(),
            taskTypeOptions = taskTypeOptions
        )

    fun onSelectedTaskChange(title: String) {
        _uiState.value = stateForTask(
            loadedTasks.firstOrNull { it.title == title },
            loadedTasks
        )
    }

    fun onTaskTypeSelected(taskType: String) {
        _uiState.update { it.copy(taskType = taskType) }
    }

    fun onMemberToggled(member: AssignableMember) {
        _uiState.update { state ->
            state.copy(
                assignedMembers = state.assignedMembers.map {
                    if (it.name == member.name) it.copy(selected = !it.selected) else it
                }
            )
        }
    }

    fun onDeadlineChange(deadline: String) {
        _uiState.update { it.copy(deadline = deadline) }
    }

    fun onTimeChange(time: String) {
        _uiState.update { it.copy(time = time) }
    }

    fun onPriorityChange(isPriority: Boolean) {
        _uiState.update { it.copy(isPriority = isPriority) }
    }

    fun onNeedsHelpChange(needsHelp: Boolean) {
        _uiState.update { it.copy(needsHelp = needsHelp) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onEditTask() {
        val id = taskId ?: return
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.updateTask(
                    id,
                    TaskUpdateRequest(
                        title = state.selectedTask,
                        taskType = state.taskType,
                        description = state.notes,
                        isPriority = state.isPriority,
                        needsHelp = state.needsHelp,
                        deadline = state.deadline
                    )
                )
            }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }
}
