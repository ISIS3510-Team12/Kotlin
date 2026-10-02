package com.team12kotlin.juggle.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.utils.parseDeadline
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

class CalendarViewModel(
    private val repository: TaskRepository = Dependencies.taskRepository
) : ViewModel() {

    private val today = LocalDate.now()

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            selectedDate = today,
            tasksByDate = emptyMap()
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        fetchTasks()
    }

    private fun fetchTasks() {
        viewModelScope.launch {
            try {
                // Buscamos tareas de un rango de 180 días atrás y adelante
                val startDate = today.minusDays(180).atStartOfDay().toString()
                val endDate = today.plusDays(180).atTime(23, 59, 59).toString()

                val tasks = repository.getAllTasks(
                    mine = true,
                    startDate = startDate,
                    endDate = endDate
                )

                val grouped = tasks.groupBy { task ->
                    task.deadline?.let { d ->
                        parseDeadline(d)?.toLocalDate()
                    } ?: today
                }

                _uiState.update { it.copy(tasksByDate = grouped) }
            } catch (e: Exception) {
                // TODO: Manejar estado de error si es necesario
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onTaskClick(task: Task) {
        // Handled via callback in the UI now
    }
}
