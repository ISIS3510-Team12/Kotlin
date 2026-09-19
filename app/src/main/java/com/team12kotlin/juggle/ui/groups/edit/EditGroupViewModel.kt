package com.team12kotlin.juggle.ui.groups.edit

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.groups.GroupsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditGroupUiState(
    val groupId: String = "",
    val name: String = "",
    val description: String = "",
    val query: String = "",
    val directory: List<User> = emptyList(),
    val selectedMembers: Set<User> = emptySet()
) {
    val filteredDirectory: List<User>
        get() = directory.filterFor(query)

    private fun List<User>.filterFor(query: String): List<User> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return this
        return filter { user ->
            user.firstName.lowercase().contains(q) || user.email.lowercase().contains(q)
        }
    }
}

class EditGroupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditGroupUiState())
    val uiState: StateFlow<EditGroupUiState> = _uiState.asStateFlow()

    private var loadedGroupId: String? = null

    fun load(groupId: String) {
        if (loadedGroupId == groupId) return
        loadedGroupId = groupId

        val group = GroupsRepository.findById(groupId)
        _uiState.update {
            it.copy(
                groupId = groupId,
                name = group?.name.orEmpty(),
                description = group?.description.orEmpty(),
                directory = GroupsRepository.directory,
                selectedMembers = group?.members.orEmpty().toSet()
            )
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
        _uiState.update { state ->
            val selected = state.selectedMembers
            state.copy(
                selectedMembers = if (member in selected) selected - member else selected + member
            )
        }
    }

    fun onSaveGroup() {
        val state = _uiState.value
        GroupsRepository.updateGroup(state.groupId) { group ->
            group.copy(
                name = state.name,
                description = state.description,
                members = state.selectedMembers.toMutableList()
            )
        }
    }
}
