package com.team12kotlin.juggle.ui.tasks.allTasks

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.dto.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AllTasksUiState(
    val selectedFilterIndex: Int = 0,
    val personalTasks: List<Task> = emptyList(),
    val groupTasks: List<Task> = emptyList()
)

class AllTasksViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        AllTasksUiState(
            personalTasks = listOf(
                Task(id = "ap1", title = "Finish Something bruh", isImportant = true),
                Task(id = "ap2", title = "Terminar"),
                Task(id = "ap3", title = "Work"),
            ),
            groupTasks = listOf(
                Task(
                    id = "ag1",
                    title = "Finish Something bruh",
                    member = "Diego",
                    isImportant = true
                ),
                Task(id = "ag2", title = "Terminar", member = "Manuela"),
                Task(id = "ag3", title = "Work", member = "Shaiel"),
                Task(id = "ag4", title = "Do the PR", member = "Diego")
            )
        )
    )
    val uiState: StateFlow<AllTasksUiState> = _uiState.asStateFlow()

    fun onFilterSelected(index: Int) {
        // TODO: Make this truly filter based on which it was selected.
        _uiState.update { it.copy(selectedFilterIndex = index) }
    }

    fun onTaskClick(task: Task) {
    }
}
