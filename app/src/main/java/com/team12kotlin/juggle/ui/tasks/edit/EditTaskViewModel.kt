package com.team12kotlin.juggle.ui.tasks.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest
import com.team12kotlin.juggle.ui.tasks.create.AssignableMember
import com.team12kotlin.juggle.utils.splitDeadline
import com.team12kotlin.juggle.utils.toIsoDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.team12kotlin.juggle.data.Dependencies

data class EditTaskUiState(
    val title: String = "",
    val currentGroup: String = "",
    val taskType: String? = null,
    val assignedMembers: List<AssignableMember> = emptyList(),
    val deadline: String = "",
    val time: String = "",
    val isPriority: Boolean = false,
    val needsHelp: Boolean = false,
    val notes: String = "",
    val taskTypeOptions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
) {
    val canSave: Boolean get() = title.isNotBlank() && !isLoading
}

class EditTaskViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository: TaskRepository = Dependencies.taskRepository
    private val groupRepository: GroupRepository = Dependencies.groupRepository

    private val taskId: Int? = savedStateHandle.get<String>("taskId")?.toIntOrNull()

    private val taskTypeOptions: List<String> =
        listOf("Coding", "Design", "Research", "Writing", "Meeting")

    private val _uiState = MutableStateFlow(
        EditTaskUiState(taskTypeOptions = taskTypeOptions)
    )
    val uiState: StateFlow<EditTaskUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    fun loadTask() {
        val id = taskId
        if (id == null) {
            _uiState.update { it.copy(errorMessage = "Missing task id") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val task = repository.getTask(id)
                val groups = runCatching { groupRepository.getGroups() }.getOrDefault(emptyList())
                task to groups
            }
                .onSuccess { (task, groups) ->
                    _uiState.value = stateForTask(task, groups)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun stateForTask(task: Task, groups: List<Group>): EditTaskUiState {
        val currentGroup = groups.firstOrNull { it.id == task.groupId }?.name.orEmpty()

        val members: MutableList<AssignableMember> = mutableListOf()
        val seen: MutableSet<String> = mutableSetOf()

        for (assignee in task.assignees) {
            if (seen.add(assignee.userId)) {
                members.add(
                    AssignableMember(
                        name = assignee.firstName,
                        selected = true,
                        userId = assignee.userId
                    )
                )
            }
        }
        for (group in groups) {
            for (user in group.users) {
                if (seen.add(user.userId)) {
                    members.add(
                        AssignableMember(
                            name = user.firstName,
                            selected = false,
                            userId = user.userId
                        )
                    )
                }
            }
        }

        val (date, time) = splitDeadline(task.deadline)

        return EditTaskUiState(
            title = task.title,
            currentGroup = currentGroup,
            taskType = task.taskType.takeIf { it.isNotBlank() },
            assignedMembers = members,
            deadline = date,
            time = time,
            isPriority = task.isPriority,
            needsHelp = task.needsHelp,
            notes = task.description,
            taskTypeOptions = taskTypeOptions
        )
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onTaskTypeSelected(taskType: String) {
        _uiState.update { it.copy(taskType = taskType) }
    }

    fun onMemberToggled(member: AssignableMember) {
        _uiState.update { state ->
            state.copy(
                assignedMembers = state.assignedMembers.map {
                    if (it.userId == member.userId && it.name == member.name) {
                        it.copy(selected = !it.selected)
                    } else {
                        it
                    }
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
        if (state.title.isBlank()) return

        val assigneeIds: MutableList<String> = mutableListOf()
        for (member in state.assignedMembers) {
            if (member.selected && member.userId.isNotBlank()) {
                assigneeIds.add(member.userId)
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.updateTask(
                    id,
                    TaskUpdateRequest(
                        title = state.title,
                        taskType = state.taskType,
                        description = state.notes,
                        isPriority = state.isPriority,
                        needsHelp = state.needsHelp,
                        deadline = toIsoDateTime(state.deadline, state.time),
                        assigneeIds = assigneeIds
                    )
                )
            }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, savedSuccessfully = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun onSaved() {
        _uiState.update { it.copy(savedSuccessfully = false) }
    }
}
