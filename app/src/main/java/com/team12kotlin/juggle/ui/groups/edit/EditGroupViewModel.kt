package com.team12kotlin.juggle.ui.groups.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.UserRepository
import com.team12kotlin.juggle.ui.dto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditGroupUiState(
    val groupId: Int = 0,
    val name: String = "",
    val description: String = "",
    val query: String = "",
    val members: List<User> = emptyList(),
    val candidates: List<User> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false
) {
    val filteredDirectory: List<User>
        get() = (members + candidates).filterFor(query)

    val canSave: Boolean
        get() = name.isNotBlank() && !isLoading

    private fun List<User>.filterFor(query: String): List<User> {
        val q = query.trim()
        if (q.isEmpty()) return this
        return filter { user ->
            user.displayName.contains(q, ignoreCase = true) || user.email.contains(q, ignoreCase = true)
        }
    }
}

class EditGroupViewModel(
    private val groupRepository: GroupRepository = Dependencies.groupRepository,
    private val userRepository: UserRepository = Dependencies.userRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditGroupUiState())
    val uiState: StateFlow<EditGroupUiState> = _uiState.asStateFlow()

    private var loadedGroupId: Int? = null

    fun load(groupId: Int) {
        if (loadedGroupId == groupId) return
        loadedGroupId = groupId

        viewModelScope.launch {
            _uiState.update { it.copy(groupId = groupId, isLoading = true, errorMessage = null) }
            runCatching { groupRepository.getGroup(groupId) to userRepository.getUsers() }
                .onSuccess { (group, users) ->
                    val memberIds = group.users.map { it.userId }.toSet()
                    _uiState.update {
                        it.copy(
                            name = group.name,
                            description = group.description,
                            members = group.users,
                            candidates = users.filterNot { user -> user.userId in memberIds },
                            selectedIds = memberIds,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    loadedGroupId = null
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onMemberToggled(member: User) {
        _uiState.update {
            val ids = if (member.userId in it.selectedIds) {
                it.selectedIds - member.userId
            } else {
                it.selectedIds + member.userId
            }
            it.copy(selectedIds = ids)
        }
    }

    fun onSaveGroup() {
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                groupRepository.updateGroup(state.groupId, state.name.trim(), state.description.trim())
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                return@launch
            }

            val selectedIds = state.selectedIds.toList()
            runCatching { groupRepository.setMembers(state.groupId, selectedIds) }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, saved = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Couldn't update the members")
                    }
                }
        }
    }
}
