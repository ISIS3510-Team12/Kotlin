package com.team12kotlin.juggle.ui.groups.create

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

data class CreateGroupUiState(
    val name: String = "",
    val description: String = "",
    val query: String = "",
    val availableMembers: List<User> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val created: Boolean = false,
    val notice: String? = null
) {
    val filteredAvailableMembers: List<User>
        get() = availableMembers.filterFor(query)

    val canCreate: Boolean
        get() = name.isNotBlank() && description.isNotBlank() && !isLoading

    private fun List<User>.filterFor(query: String): List<User> {
        val q = query.trim()
        if (q.isEmpty()) return this
        return filter { user ->
            user.displayName.contains(q, ignoreCase = true) || user.email.contains(q, ignoreCase = true)
        }
    }
}

class CreateGroupViewModel(
    private val groupRepository: GroupRepository = Dependencies.groupRepository,
    private val userRepository: UserRepository = Dependencies.userRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            runCatching {
                val currentId = userRepository.getCurrentUser().userId
                userRepository.getUsers().filterNot { it.userId == currentId }
            }
                .onSuccess { users -> _uiState.update { it.copy(availableMembers = users) } }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onToggleMember(user: User) {
        _uiState.update {
            val ids = if (user.userId in it.selectedIds) it.selectedIds - user.userId else it.selectedIds + user.userId
            it.copy(selectedIds = ids)
        }
    }

    fun onCreateGroup() {
        val state = _uiState.value
        if (!state.canCreate) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val group = runCatching {
                groupRepository.createGroup(state.name.trim(), state.description.trim())
            }.getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                return@launch
            }
            // The group already exists at this point, so member failures are reported but not rolled back.
            val failed = state.availableMembers
                .filter { it.userId in state.selectedIds }
                .filter { member -> runCatching { groupRepository.addMember(group.id, member.email) }.isFailure }
            if (failed.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, created = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        notice = "Group created, but couldn't add: ${failed.joinToString { m -> m.displayName }}",
                        created = true
                    )
                }
            }
        }
    }
}
