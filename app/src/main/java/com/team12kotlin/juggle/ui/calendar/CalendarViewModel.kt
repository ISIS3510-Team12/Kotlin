package com.team12kotlin.juggle.ui.calendar

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val tasksByDate: Map<LocalDate, List<Task>> = emptyMap()
) {
    val tasksForSelectedDate: List<Task>
        get() = tasksByDate[selectedDate].orEmpty()
}

class CalendarViewModel : ViewModel() {

    private val today = LocalDate.now()

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            selectedDate = today,
            tasksByDate = buildList {
                add(
                    Task(
                        id = 101,
                        title = "Finish the figma",
                        assignees = listOf(User(userId = "Diego", firstName = "Diego")),
                        needsHelp = true,
                        deadline = today.atTime(12, 0).toString()
                    )
                )
                add(
                    Task(
                        id = 102,
                        title = "Review pull request",
                        assignees = listOf(User(userId = "Manuela", firstName = "Manuela")),
                        deadline = today.plusDays(1).atTime(12, 0).toString()
                    )
                )
                add(
                    Task(
                        id = 103,
                        title = "Prepare sprint slides",
                        assignees = listOf(User(userId = "Diego", firstName = "Diego")),
                        isPriority = true,
                        deadline = today.plusDays(3).atTime(12, 0).toString()
                    )
                )
                add(
                    Task(
                        id = 104,
                        title = "Submit MS4 report",
                        assignees = listOf(User(userId = "Shaiel", firstName = "Shaiel")),
                        deadline = today.minusDays(2).atTime(12, 0).toString()
                    )
                )
            }.groupBy { it.deadline?.let { d -> LocalDateTime.parse(d).toLocalDate() } ?: today }
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onTaskClick(task: Task) {
        // TODO: navigate to task detail for task.id
    }
}
