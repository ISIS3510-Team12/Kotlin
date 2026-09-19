package com.team12kotlin.juggle.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupsUiState(
    val query: String = "",
    val groups: List<Group> = emptyList()
) {
    val filteredGroups: List<Group>
        get() = groups.filterFor(query)

    private fun List<Group>.filterFor(query: String): List<Group> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return this
        return filter { group ->
            group.name.lowercase().contains(q)
                    || group.description.lowercase().contains(q)
                    || group.members.any { member -> member.firstName.contains(q)}
        }
    }
}

class GroupsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            GroupsRepository.groups.collect { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(query = query) }
        // TODO: trigger repository search / navigation when data layer exists
    }

    fun onGroupClick(group: Group) {
        // TODO: navigate to group detail for group.id
    }

    fun onCreateGroup() {
        // TODO: navigate to create-group
    }
}