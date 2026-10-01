package com.team12kotlin.juggle.ui.calendar

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.dto.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

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
                        member = "Diego",
                        needsHelp = true,
                        dueLabel = "Tomorrow - 12 hours left",
                        deadline = today.toString()
                    )
                )
                add(
                    Task(
                        id = 102,
                        title = "Review pull request",
                        member = "Manuela",
                        dueLabel = "Tomorrow - 12 hours left",
                        deadline = today.plusDays(1).toString()
                    )
                )
                add(
                    Task(
                        id = 103,
                        title = "Prepare sprint slides",
                        member = "Diego",
                        isImportant = true,
                        dueLabel = "In 3 days",
                        deadline = today.plusDays(3).toString()
                    )
                )
                add(
                    Task(
                        id = 104,
                        title = "Submit MS4 report",
                        member = "Shaiel",
                        dueLabel = "2 days ago",
                        deadline = today.minusDays(2).toString()
                    )
                )
            }.groupBy { it.deadline?.let(LocalDate::parse) ?: today }
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
