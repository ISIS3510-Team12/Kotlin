package com.team12kotlin.juggle.ui.projects.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.ProjectRepository
import com.team12kotlin.juggle.ui.dto.ProjectCreateRequest
import com.team12kotlin.juggle.ui.utils.ProjectFieldLimits
import com.team12kotlin.juggle.utils.toIsoDateTime
import com.team12kotlin.juggle.ui.utils.toUserMessage
import com.team12kotlin.juggle.data.local.cache_structs.FormDraftCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateProjectUiState(
    val name: String = "",
    val description: String = "",
    val deadline: String = "",
    val groupId: Int? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val createdProjectId: Int? = null,
) {
    val canCreate: Boolean
        get() = name.isNotBlank() &&
                description.isNotBlank() &&
                deadline.isNotBlank() &&
                groupId != null &&
                !isSaving
}

class CreateProjectViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectRepository: ProjectRepository = Dependencies.projectRepository
    private val groupRepository: GroupRepository = Dependencies.groupRepository
    private val formDraftCache: FormDraftCache = Dependencies.formDraftCache

    private val navGroupId: Int? = savedStateHandle.get<Int>("groupId")?.takeIf { it > 0 }
    private val draftKey = "create_project_${navGroupId ?: 0}"

    private val _uiState = MutableStateFlow(
        formDraftCache.get<CreateProjectDraft>(draftKey).let { draft ->
            CreateProjectUiState(
                name = draft?.name ?: "",
                description = draft?.description ?: "",
                deadline = draft?.deadline ?: "",
                groupId = navGroupId
            )
        }
    )
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    init {
        if (navGroupId == null) loadFallbackGroup()
    }

    /**
     * Only used if the screen is opened without a groupId
     */
    private fun loadFallbackGroup() {
        viewModelScope.launch {
            runCatching {
                val groups = groupRepository.getGroups()
                groups.firstOrNull()?.id
            }
                .onSuccess { id -> _uiState.update { it.copy(groupId = id) } }
        }
    }

    private fun saveDraft() {
        val s = _uiState.value
        formDraftCache.put(
            draftKey, CreateProjectDraft(
                s.name,
                s.description,
                s.deadline,
            ))
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value.take(ProjectFieldLimits.NAME_MAX_LENGTH)) }
        saveDraft()
    }

    fun onDescriptionChange(value: String) {
        _uiState.update {
            it.copy(description = value.take(ProjectFieldLimits.DESCRIPTION_MAX_LENGTH))
        }
        saveDraft()
    }

    fun onDeadlineChange(value: String) {
        _uiState.update { it.copy(deadline = value) }
        saveDraft()
    }

    fun onResetForm() {
        if (_uiState.value.isSaving) return
        _uiState.update {
            it.copy(name = "", description = "", deadline = "", errorMessage = null)
        }
        formDraftCache.clear(draftKey)
    }

    fun onCreateProject() {
        val state = _uiState.value
        if (!state.canCreate) return

        val isoDeadline = toIsoDateTime(state.deadline, "")
        if (isoDeadline == null) {
            _uiState.update { it.copy(errorMessage = "Pick a valid date") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                projectRepository.createProject(
                    ProjectCreateRequest(
                        name = state.name.trim(),
                        description = state.description.trim(),
                        deadline = isoDeadline,
                        groupId = state.groupId ?: return@launch
                    )
                )
            }
                .onSuccess { project ->
                    formDraftCache.clear(draftKey)
                    _uiState.update { it.copy(isSaving = false, createdProjectId = project.id) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.toUserMessage()) }
                }
        }
    }
}
