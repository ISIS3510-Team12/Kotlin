package com.team12kotlin.juggle.ui.groups.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.ui.dto.Group
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.groups.GroupsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val group: Group? = null,
    val relatedProjects: List<Task> = emptyList()
)

class GroupDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    private var loadedGroupId: Int? = null

    fun load(groupId: Int) {
        if (loadedGroupId == groupId) return
        loadedGroupId = groupId

        viewModelScope.launch {
            GroupsRepository.groups.collect { groups ->
                val group = groups.find { it.id == groupId }
                _uiState.update {
                    it.copy(
                        group = group,
                        relatedProjects = mockRelatedProjectsFor(group)
                    )
                }
            }
        }
    }

    fun onCreateProject() {
        // TODO: navigate to create-project once that flow exists
    }

    private fun mockRelatedProjectsFor(group: Group?): List<Task> {
        val members = group?.users.orEmpty()
        return listOf(
            Task(
                id = 1,
                title = "Marketplace prototype",
                member = members.getOrNull(0)?.firstName,
                isImportant = true,
                dueLabel = "Due in 3 days"
            ),
            Task(
                id = 2,
                title = "Interview synthesis",
                member = members.getOrNull(1)?.firstName,
                dueLabel = "Due next week"
            ),
            Task(
                id = 3,
                title = "Wiki milestone writeup",
                dueLabel = "No due date yet"
            )
        )
    }
}
