package com.team12kotlin.juggle.ui.tasks.create

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.dto.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A member that can be assigned to a task, rendered as a monogram + name chip.
 */
data class AssignableMember(
    val name: String,
    val selected: Boolean = false
) {
    val initial: String get() = name.take(1).uppercase()
}

/**
 * UI state for the Create Task screen.
 */
data class CreateTaskUiState(
    val currentGroup: String = "App Devs",
    val title: String = "",
    val taskType: String? = null,
    val associatedProject: String? = null,
    val assignedMembers: List<AssignableMember> = emptyList(),
    val deadline: String = "",
    val time: String = "",
    val isPriority: Boolean = false,
    val needsHelp: Boolean = false,
    val notes: String = "",
    val relatedTasks: List<RelatedTask> = emptyList(),
    val taskTypeOptions: List<String> = emptyList(),
    val projectOptions: List<String> = emptyList()
) {
    /** The task title is required, so creation is only enabled once it is filled. */
    val canCreate: Boolean get() = title.isNotBlank()
}

/**
 * A task that can be linked to the one being created.
 */
data class RelatedTask(
    val task: Task,
    val dueLabel: String,
    val selected: Boolean = false
)

class CreateTaskViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreateTaskUiState(
            currentGroup = "App Devs",
            taskTypeOptions = listOf("Coding", "Design", "Research", "Writing", "Meeting"),
            projectOptions = listOf("Project #1", "Project #2", "Project #3"),
            assignedMembers = listOf(
                AssignableMember(name = "Diego"),
                AssignableMember(name = "Manuela"),
                AssignableMember(name = "Shaiel"),
                AssignableMember(name = "Victoria"),
                AssignableMember(name = "Chu"),
                AssignableMember(name = "Gabriel"),
                AssignableMember(name = "Cristian")
            ),
            relatedTasks = listOf(
                RelatedTask(
                    task = Task(id = "r1", title = "Finish the figma", member = "Diego"),
                    dueLabel = "Due date: Tomorrow"
                )
            )
        )
    )
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onTaskTypeSelected(taskType: String) {
        _uiState.update { it.copy(taskType = taskType) }
    }

    fun onAssociatedProjectSelected(project: String) {
        _uiState.update { it.copy(associatedProject = project) }
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

    fun onRelatedTaskToggled(relatedTask: RelatedTask) {
        _uiState.update { state ->
            state.copy(
                relatedTasks = state.relatedTasks.map {
                    if (it.task.id == relatedTask.task.id) it.copy(selected = !it.selected) else it
                }
            )
        }
    }

    fun onCreateTask() {
        // TODO: connect with API to create the task once the data layer exists.
    }
}
