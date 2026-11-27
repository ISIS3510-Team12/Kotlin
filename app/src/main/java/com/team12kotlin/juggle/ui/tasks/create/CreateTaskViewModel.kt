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
import com.team12kotlin.juggle.ui.utils.TaskFieldLimits
import com.team12kotlin.juggle.utils.taskDue
import com.team12kotlin.juggle.utils.toIsoDateTime
import com.team12kotlin.juggle.ui.utils.toUserMessage
import com.team12kotlin.juggle.utils.FormDraftCache
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val evidenceBytes: ByteArray? = null,
    val evidenceMimeType: String? = null,
    val evidenceDirty: Boolean = false,
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
                selectedGroupId != null &&
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

    private val draftKey = "create_task_${navGroupId ?: 0}"
    private val formDraftCache: FormDraftCache = Dependencies.formDraftCache

    private val taskTypeOptions: List<String> =
        listOf("Coding", "Design", "Research", "Writing", "Meeting")

    private val initialDraft: CreateTaskDraft? = formDraftCache.get(draftKey)

    private val _uiState = MutableStateFlow(
        CreateTaskUiState(
            taskTypeOptions = taskTypeOptions,
            isLoading = true,
            title = initialDraft?.title ?: "",
            taskType = initialDraft?.taskType,
            deadline = initialDraft?.deadline ?: "",
            time = initialDraft?.time ?: "",
            isPriority = initialDraft?.isPriority ?: false,
            needsHelp = initialDraft?.needsHelp ?: false,
            notes = initialDraft?.notes ?: "",
            evidenceBytes = initialDraft?.evidenceBytes,
            evidenceMimeType = initialDraft?.evidenceMimeType,
            evidenceDirty = initialDraft?.evidenceDirty ?: false,
            relatedQuery = initialDraft?.relatedQuery ?: ""
        )
    )
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    init {
        loadForm()
    }

    fun loadForm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { groupRepository.getGroups() }
                .onSuccess { groups ->
                    val groupId = navGroupId ?: initialDraft?.selectedGroupId
                    _uiState.update {
                        it.copy(
                            groups = groups,
                            selectedGroupId = groupId,
                            isLoading = false
                        )
                    }
                    loadGroupData(groupId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
        }
    }

    fun refreshGroupData() {
        val groupId = _uiState.value.selectedGroupId ?: return
        loadGroupData(groupId)
    }

    fun onGroupSelected(groupName: String) {
        val group = _uiState.value.groups.firstOrNull { it.name == groupName }
        _uiState.update {
            it.copy(
                selectedGroupId = group?.id,
                selectedProjectId = null,
                assignedMembers = emptyList(),
                relatedTasks = emptyList()
            )
        }
        formDraftCache.clear(draftKey)
        saveDraft()
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
                coroutineScope {
                    val group = async { groupRepository.getGroup(groupId) }
                    val projects = async { projectRepository.getProjects(groupId) }
                    val allTasks = async { taskRepository.getAllTasks() }
                    Triple(group.await(), projects.await(), allTasks.await())
                }
            }
                .onSuccess { (group, projects, allTasks) ->
                    val draft = formDraftCache.get<CreateTaskDraft>(draftKey)
                        ?.takeIf { it.selectedGroupId == groupId }

                    val members = group.users.map {
                        AssignableMember(
                            name = it.firstName,
                            userId = it.userId,
                            selected = it.userId in (draft?.selectedMemberIds ?: emptySet())
                        )
                    }

                    val related = allTasks
                        .filter { it.groupId == groupId && it.id > 0 }
                        .map {
                            RelatedTask(
                                task = it,
                                dueLabel = taskDue(it.deadline)?.text.orEmpty(),
                                selected = it.id in (draft?.selectedRelatedTaskIds ?: emptySet())
                            )
                        }

                    _uiState.update {
                        it.copy(
                            projects = projects,
                            assignedMembers = members,
                            relatedTasks = related,
                            selectedProjectId = (draft?.selectedProjectId ?: it.selectedProjectId)
                                ?.takeIf { id -> projects.any { p -> p.id == id } }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.toUserMessage()) }
                }
        }
    }

    private fun saveDraft() {
        val s = _uiState.value
        val previous = formDraftCache.get<CreateTaskDraft>(draftKey)
            ?.takeIf { it.selectedGroupId == s.selectedGroupId }

        // While members/related tasks are still loading, keep the previously saved ids
        val memberIds = if (s.assignedMembers.isEmpty()) {
            previous?.selectedMemberIds ?: emptySet()
        } else {
            s.assignedMembers.filter { it.selected }.map { it.userId }.toSet()
        }
        val relatedIds = if (s.relatedTasks.isEmpty()) {
            previous?.selectedRelatedTaskIds ?: emptySet()
        } else {
            s.relatedTasks.filter { it.selected }.map { it.task.id }.toSet()
        }

        formDraftCache.put(
            draftKey,
            CreateTaskDraft(
                selectedGroupId = s.selectedGroupId,
                title = s.title,
                taskType = s.taskType,
                selectedProjectId = s.selectedProjectId,
                selectedMemberIds = memberIds,
                deadline = s.deadline,
                time = s.time,
                isPriority = s.isPriority,
                needsHelp = s.needsHelp,
                notes = s.notes,
                evidenceBytes = s.evidenceBytes,
                evidenceMimeType = s.evidenceMimeType,
                evidenceDirty = s.evidenceDirty,
                selectedRelatedTaskIds = relatedIds,
                relatedQuery = s.relatedQuery
            )
        )
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title.take(TaskFieldLimits.TITLE_MAX_LENGTH)) }
        saveDraft()
    }

    fun onTaskTypeSelected(taskType: String) {
        _uiState.update { it.copy(taskType = taskType) }
        saveDraft()
    }

    fun onProjectSelected(projectName: String) {
        val project = _uiState.value.projects.firstOrNull { it.name == projectName }
        _uiState.update { it.copy(selectedProjectId = project?.id) }
        saveDraft()
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
        saveDraft()
    }

    fun onDeadlineChange(deadline: String) {
        _uiState.update { it.copy(deadline = deadline) }
        saveDraft()
    }

    fun onTimeChange(time: String) {
        _uiState.update { it.copy(time = time) }
        saveDraft()
    }

    fun onPriorityChange(isPriority: Boolean) {
        _uiState.update { it.copy(isPriority = isPriority) }
        saveDraft()
    }

    fun onNeedsHelpChange(needsHelp: Boolean) {
        _uiState.update { it.copy(needsHelp = needsHelp) }
        saveDraft()
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes.take(TaskFieldLimits.NOTES_MAX_LENGTH)) }
        saveDraft()
    }

    fun onEvidenceTaken(bytes: ByteArray, mimeType: String) {
        _uiState.update {
            it.copy(evidenceBytes = bytes, evidenceMimeType = mimeType, evidenceDirty = true)
        }
        saveDraft()
    }

    fun onRelatedQueryChange(query: String) {
        _uiState.update { it.copy(relatedQuery = query) }
        saveDraft()
    }

    fun onRelatedTaskToggled(relatedTask: RelatedTask) {
        _uiState.update { state ->
            state.copy(
                relatedTasks = state.relatedTasks.map {
                    if (it.task.id == relatedTask.task.id) it.copy(selected = !it.selected) else it
                }
            )
        }
        saveDraft()
    }

    /** Goes back to a freshly opened form, keeping the groups already loaded. */
    fun onResetForm() {
        if (_uiState.value.isLoading) return
        _uiState.update {
            CreateTaskUiState(
                groups = it.groups,
                selectedGroupId = navGroupId,
                taskTypeOptions = taskTypeOptions
            )
        }
        loadGroupData(navGroupId)
        formDraftCache.clear(draftKey)
    }

    private suspend fun uploadEvidence(taskId: Int) {
        val state = _uiState.value
        val bytes = state.evidenceBytes
        if (!state.evidenceDirty || bytes == null) return
        runCatching {
            taskRepository.uploadTaskPhoto(taskId, bytes, state.evidenceMimeType ?: "image/jpeg")
        }
    }

    fun onCreateTask() {
        val state = _uiState.value
        if (!state.canCreate) return

        val groupId = state.selectedGroupId ?: return

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
                    evidence = state.evidenceBytes.takeIf { state.evidenceDirty },
                    request = TaskCreateRequest(
                        title = state.title,
                        taskType = state.taskType ?: "",
                        description = state.notes.takeIf { it.isNotBlank() },
                        isPriority = state.isPriority,
                        needsHelp = state.needsHelp,
                        deadline = isoDeadline,
                        projectId = state.selectedProjectId,
                        groupId = groupId,
                        assigneeIds = assigneeIds,
                        relatedTaskIds = relatedTaskIds
                    )
                )
            }
                .onSuccess { task ->
                    formDraftCache.clear(draftKey)
                    if (task.id > 0) uploadEvidence(task.id)
                    _uiState.update { it.copy(isLoading = false, createdTaskId = task.id) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
        }
    }
}