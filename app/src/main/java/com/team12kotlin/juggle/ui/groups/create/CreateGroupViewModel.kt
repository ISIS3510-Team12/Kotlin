package com.team12kotlin.juggle.ui.groups.create

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.dto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CreateGroupUiState(
    val query: String = "",
    val availableMembers: List<User> = emptyList()
) {
    val filteredAvailableMembers: List<User>
        get() = availableMembers.filterFor(query)

    private fun List<User>.filterFor(query: String): List<User> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return this
        return filter { user ->
            user.firstName.contains(q) || user.email.contains(q)
        }
    }
}

class CreateGroupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreateGroupUiState(
            availableMembers = listOf(
                User(firstName = "Diego Munévar", email="d.mun@gmail.com", major = "Comp Sci"),
                User(firstName = "Manuela Lovera", email="m.ll@gmail.com", major = "Comp Sci"),
                User(firstName = "Shaiel Jiménez", email="sm.j@gmail.com", major = "Comp Sci")
            ),
        )
    )
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(query = query) }
        // TODO: trigger repository search / navigation when data layer exists
    }

    fun onCancel() {
        // TODO: cancel group creation
    }

    fun onCreateGroup() {
        // TODO: create group - navigate to groups landing
    }
}