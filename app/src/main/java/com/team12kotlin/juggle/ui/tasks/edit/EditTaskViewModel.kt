package com.team12kotlin.juggle.ui.tasks.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.ProjectRepository
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.Project
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskUpdateRequest
import com.team12kotlin.juggle.ui.tasks.create.AssignableMember
import com.team12kotlin.juggle.ui.tasks.create.RelatedTask
import com.team12kotlin.juggle.utils.splitDeadline
import com.team12kotlin.juggle.utils.taskDue
import com.team12kotlin.juggle.utils.toIsoDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.team12kotlin.juggle.data.Dependencies

data class EditTaskUiState(
    val title: String = "",
    val groups: List<Group> = emptyList(),
    val selectedGroupId: Int? = null,
    val projects: List<Project> = emptyList(),
    val selectedProjectId: Int? = null,
    val taskType: String? = null,
    val assignedMembers: List<AssignableMember> = emptyList(),
    val relatedTasks: List<RelatedTask> = emptyList(),
    val relatedQuery: String = "",
    val deadline: String = "",
    val time: String = "",
    val isPriority: Boolean = false,
    val needsHelp: Boolean = false,
    val notes: String = "",
    val evidenceBytes: ByteArray? = null,
    val evidenceMimeType: String? = null,
    val evidenceDirty: Boolean = false,
    val taskTypeOptions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
) {
    val canSave: Boolean get() = title.isNotBlank() && !isLoading

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

class EditTaskViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository: TaskRepository = Dependencies.taskRepository
    private val groupRepository: GroupRepository = Dependencies.groupRepository
    private val projectRepository: ProjectRepository = Dependencies.projectRepository

    private val taskId: Int? = savedStateHandle.get<String>("taskId")?.toIntOrNull()
    private val navGroupId: Int? = savedStateHandle.get<Int>("groupId")?.takeIf { it > 0 }

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
                val groupId = navGroupId ?: task.groupId
                val projects = loadProjects(groupId)
                val relatedIds = task.relatedTasks.map { it.id }.toSet()
                val related = loadRelated(groupId, relatedIds)
                val photo = if (task.hasPhoto) repository.getTaskPhoto(id) else null
                TaskEditData(task, groups, groupId, projects, related, photo)
            }
                .onSuccess { data ->
                    _uiState.value = stateForTask(data)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun onGroupSelected(groupName: String) {
        val group = _uiState.value.groups.firstOrNull { it.name == groupName }
        val keepSelected = _uiState.value.relatedTasks
            .filter { it.selected }
            .map { it.task.id }
            .toSet()

        _uiState.update {
            it.copy(selectedGroupId = group?.id, selectedProjectId = null, projects = emptyList())
        }

        viewModelScope.launch {
            val projects = loadProjects(group?.id)
            val related = loadRelated(group?.id, keepSelected)
            _uiState.update { it.copy(projects = projects, relatedTasks = related) }
        }
    }

    fun onProjectSelected(projectName: String) {
        val project = _uiState.value.projects.firstOrNull { it.name == projectName }
        _uiState.update { it.copy(selectedProjectId = project?.id) }
    }

    private suspend fun loadProjects(groupId: Int?): List<Project> {
        if (groupId == null) return emptyList()
        return runCatching { projectRepository.getProjects(groupId) }.getOrDefault(emptyList())
    }

    private suspend fun loadRelated(groupId: Int?, selectedIds: Set<Int>): List<RelatedTask> {
        if (groupId == null) return emptyList()
        val allTasks = runCatching { repository.getAllTasks() }.getOrDefault(emptyList())
        val related: MutableList<RelatedTask> = mutableListOf()
        for (task in allTasks) {
            if (task.groupId == groupId) {
                related.add(
                    RelatedTask(
                        task = task,
                        dueLabel = taskDue(task.deadline)?.text.orEmpty(),
                        selected = selectedIds.contains(task.id)
                    )
                )
            }
        }
        return related
    }

    private fun stateForTask(data: TaskEditData): EditTaskUiState {
        val members: MutableList<AssignableMember> = mutableListOf()
        val seen: MutableSet<String> = mutableSetOf()

        for (assignee in data.task.assignees) {
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
        for (group in data.groups) {
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

        val (date, time) = splitDeadline(data.task.deadline)

        return EditTaskUiState(
            title = data.task.title,
            groups = data.groups,
            selectedGroupId = data.groupId,
            projects = data.projects,
            selectedProjectId = data.task.projectId,
            taskType = data.task.taskType.takeIf { it.isNotBlank() },
            assignedMembers = members,
            relatedTasks = data.relatedTasks,
            evidenceBytes = data.photo,
            deadline = date,
            time = time,
            isPriority = data.task.isPriority,
            needsHelp = data.task.needsHelp,
            notes = data.task.description,
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

    fun onEvidenceTaken(bytes: ByteArray, mimeType: String) {
        _uiState.update {
            it.copy(evidenceBytes = bytes, evidenceMimeType = mimeType, evidenceDirty = true)
        }
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

    private suspend fun uploadEvidence(taskId: Int) {
        val state = _uiState.value
        val bytes = state.evidenceBytes
        if (!state.evidenceDirty || bytes == null) return
        runCatching {
            repository.uploadTaskPhoto(taskId, bytes, state.evidenceMimeType ?: "image/jpeg")
        }
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

        val relatedTaskIds: MutableList<Int> = mutableListOf()
        for (related in state.relatedTasks) {
            if (related.selected) {
                relatedTaskIds.add(related.task.id)
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
                        projectId = state.selectedProjectId,
                        assigneeIds = assigneeIds,
                        relatedTaskIds = relatedTaskIds
                    )
                )
            }
                .onSuccess {
                    uploadEvidence(id)
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

    private data class TaskEditData(
        val task: Task,
        val groups: List<Group>,
        val groupId: Int?,
        val projects: List<Project>,
        val relatedTasks: List<RelatedTask>,
        val photo: ByteArray?
    )
}
