package com.team12kotlin.juggle.ui.tasks.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.ProjectRepository
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.Project
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskCreateRequest
import com.team12kotlin.juggle.utils.taskDue
import com.team12kotlin.juggle.utils.toIsoDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssignableMember(
    val name: String,
    val selected: Boolean = false,
    val userId: String = ""
) {
    val initial: String get() = name.take(1).uppercase()
}

data class RelatedTask(
    val task: Task,
    val dueLabel: String,
    val selected: Boolean = false
)

data class CreateTaskUiState(
    val groups: List<Group> = emptyList(),
    val selectedGroupId: Int? = null,
    val title: String = "",
    val taskType: String? = null,
    val projects: List<Project> = emptyList(),
    val selectedProjectId: Int? = null,
    val assignedMembers: List<AssignableMember> = emptyList(),
    val deadline: String = "",
    val time: String = "",
    val isPriority: Boolean = false,
    val needsHelp: Boolean = false,
    val notes: String = "",
    val relatedTasks: List<RelatedTask> = emptyList(),
    val relatedQuery: String = "",
    val taskTypeOptions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdTaskId: Int? = null
) {
    val canCreate: Boolean
        get() = title.isNotBlank() &&
                taskType != null &&
                deadline.isNotBlank() &&
                selectedProjectId != null &&
                !isLoading

    val selectedGroupName: String?
        get() = groups.firstOrNull { it.id == selectedGroupId }?.name

    val selectedProjectName: String?
        get() = projects.firstOrNull { it.id == selectedProjectId }?.name

    val filteredRelatedTasks: List<RelatedTask>
        get() = if (relatedQuery.isBlank()) {
            relatedTasks
        } else {
            relatedTasks.filter { it.task.title.contains(relatedQuery, ignoreCase = true) }
        }
}

class CreateTaskViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskRepository: TaskRepository = Dependencies.taskRepository
    private val groupRepository: GroupRepository = Dependencies.groupRepository
    private val projectRepository: ProjectRepository = Dependencies.projectRepository

    private val navGroupId: Int? =
        savedStateHandle.get<Int>("groupId")?.takeIf { it > 0 }

    private val taskTypeOptions: List<String> =
        listOf("Coding", "Design", "Research", "Writing", "Meeting")

    private val _uiState = MutableStateFlow(CreateTaskUiState(taskTypeOptions = taskTypeOptions))
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    init {
        loadForm()
    }

    fun loadForm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { groupRepository.getGroups() }
                .onSuccess { groups ->
                    _uiState.update {
                        it.copy(
                            groups = groups,
                            selectedGroupId = navGroupId,
                            isLoading = false
                        )
                    }
                    loadGroupData(navGroupId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun onGroupSelected(groupName: String) {
        val group = _uiState.value.groups.firstOrNull { it.name == groupName }
        _uiState.update {
            it.copy(selectedGroupId = group?.id, selectedProjectId = null)
        }
        loadGroupData(group?.id)
    }

    private fun loadGroupData(groupId: Int?) {
        if (groupId == null) {
            _uiState.update {
                it.copy(
                    projects = emptyList(),
                    assignedMembers = emptyList(),
                    relatedTasks = emptyList(),
                    selectedProjectId = null
                )
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                val group = groupRepository.getGroup(groupId)
                val projects = projectRepository.getProjects(groupId)
                val allTasks = taskRepository.getAllTasks()
                Triple(group, projects, allTasks)
            }
                .onSuccess { (group, projects, allTasks) ->
                    val members: MutableList<AssignableMember> = mutableListOf()
                    for (user in group.users) {
                        members.add(AssignableMember(name = user.firstName, userId = user.userId))
                    }

                    val related: MutableList<RelatedTask> = mutableListOf()
                    for (task in allTasks) {
                        if (task.groupId == groupId) {
                            related.add(
                                RelatedTask(
                                    task = task,
                                    dueLabel = taskDue(task.deadline)?.text.orEmpty()
                                )
                            )
                        }
                    }

                    _uiState.update {
                        it.copy(
                            projects = projects,
                            assignedMembers = members,
                            relatedTasks = related
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onTaskTypeSelected(taskType: String) {
        _uiState.update { it.copy(taskType = taskType) }
    }

    fun onProjectSelected(projectName: String) {
        val project = _uiState.value.projects.firstOrNull { it.name == projectName }
        _uiState.update { it.copy(selectedProjectId = project?.id) }
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

    fun onRelatedQueryChange(query: String) {
        _uiState.update { it.copy(relatedQuery = query) }
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
        val state = _uiState.value
        if (!state.canCreate) return

        val isoDeadline = toIsoDateTime(state.deadline, state.time)
        if (isoDeadline == null) {
            _uiState.update { it.copy(errorMessage = "Pick a valid deadline") }
            return
        }

        val assigneeIds: MutableList<String> = mutableListOf()
        for (member in state.assignedMembers) {
            if (member.selected && member.userId.isNotBlank()) {
                assigneeIds.add(member.userId)
            }
        }

        val relatedTaskIds: MutableList<Int> = mutableListOf()
        for (related in state.relatedTasks) {
            if (related.selected) {
                relatedTaskIds.add(related.task.id)
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                taskRepository.createTask(
                    TaskCreateRequest(
                        title = state.title,
                        taskType = state.taskType ?: "",
                        description = state.notes.takeIf { it.isNotBlank() },
                        isPriority = state.isPriority,
                        needsHelp = state.needsHelp,
                        deadline = isoDeadline,
                        projectId = state.selectedProjectId,
                        assigneeIds = assigneeIds,
                        relatedTaskIds = relatedTaskIds
                    )
                )
            }
                .onSuccess { task ->
                    _uiState.update { it.copy(isLoading = false, createdTaskId = task.id) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }
}
