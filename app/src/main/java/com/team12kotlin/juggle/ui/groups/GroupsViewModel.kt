package com.team12kotlin.juggle.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.ui.dto.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupsUiState(
    val query: String = "",
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredGroups: List<Group>
        get() = groups.filterFor(query)

    private fun List<Group>.filterFor(query: String): List<Group> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return this
        return filter { group ->
            group.name.lowercase().contains(q)
                    || group.description.lowercase().contains(q)
                    || group.users.any { member -> member.firstName.contains(q, ignoreCase = true) }
        }
    }
}

class GroupsViewModel(
    private val groupRepository: GroupRepository = Dependencies.groupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { groupRepository.getGroups() }
                .onSuccess { groups ->
                    _uiState.update {
                        it.copy(
                            groups = groups.filterNot { group ->
                                group.isPersonal || group.name.equals("Personal", ignoreCase = true)
                            },
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onGroupClick(group: Group) {
    }

    fun onCreateGroup() {
    }
}
