package com.team12kotlin.juggle.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.tasks.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val tasksByDate: Map<LocalDate, List<Task>> = emptyMap()
) {
    val tasksForSelectedDate: List<Task>
        get() = tasksByDate[selectedDate].orEmpty()
}

class CalendarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            TasksRepository.groupTasks.collect { sharedTasks ->
                val tasksByDate = sharedTasks
                    .filter { it.dueDate != null }
                    .groupBy { it.dueDate!! }
                _uiState.update { it.copy(tasksByDate = tasksByDate) }
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onTaskClick(task: Task) {
        // TODO: navigate to task detail for task.id
    }
}
