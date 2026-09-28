package com.team12kotlin.juggle.ui.tasks.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.tasks.TaskRepository
import com.team12kotlin.juggle.ui.tasks.create.AssignableMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * UI state for the Edit Task screen. Mirrors the create form but is pre-filled from the
 * task being edited and keyed by a "selected task" dropdown.
 */
data class EditTaskUiState(
    val currentGroup: String = "App Devs",
    val selectedTask: String? = null,
    val taskType: String? = null,
    val assignedMembers: List<AssignableMember> = emptyList(),
    val deadline: String = "",
    val time: String = "",
    val isPriority: Boolean = false,
    val needsHelp: Boolean = false,
    val notes: String = "",
    val taskOptions: List<String> = emptyList(),
    val taskTypeOptions: List<String> = emptyList()
) {
    /** A task must be selected before edits can be saved. */
    val canSave: Boolean get() = !selectedTask.isNullOrBlank()
}

class EditTaskViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: String? = savedStateHandle["taskId"]

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<EditTaskUiState> = _uiState.asStateFlow()

    private fun buildInitialState(): EditTaskUiState {
        val task = taskId?.let { TaskRepository.findById(it) }
        val allTaskTitles = (TaskRepository.personalTasks + TaskRepository.groupTasks)
            .map { it.title }
            .distinct()

        return EditTaskUiState(
            selectedTask = task?.title,
            taskType = task?.taskType?.takeIf { it.isNotBlank() },
            deadline = task?.deadline.orEmpty(),
            isPriority = task?.isPriority ?: false,
            needsHelp = task?.needsHelp ?: false,
            notes = task?.description.orEmpty(),
            assignedMembers = task?.members?.map { AssignableMember(name = it.firstName, selected = true) }
                ?: emptyList(),
            taskOptions = allTaskTitles,
            taskTypeOptions = listOf("Coding", "Design", "Research", "Writing", "Meeting")
        )
    }

    fun onSelectedTaskChange(task: String) {
        _uiState.update { it.copy(selectedTask = task) }
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
        // TODO: persist the task edits when the data layer exists.
    }
}
