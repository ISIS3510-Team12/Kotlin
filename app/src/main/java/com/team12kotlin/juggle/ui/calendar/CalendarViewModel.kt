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

private const val FETCH_WINDOW_DAYS = 3L   // ±3 días alrededor del día seleccionado

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val tasksByDate: Map<LocalDate, List<Task>> = emptyMap(),
    val isLoading: Boolean = false
) {
    val tasksForSelectedDate: List<Task>
        get() = tasksByDate[selectedDate].orEmpty()
}

class CalendarViewModel(
    private val repository: TaskRepository = Dependencies.taskRepository
) : ViewModel() {

    private val today = LocalDate.now()

    private val _uiState = MutableStateFlow(
        CalendarUiState(selectedDate = today)
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // Rango actualmente en caché (null = nada cargado aún)
    private var loadedStart: LocalDate? = null
    private var loadedEnd: LocalDate? = null

    init {
        fetchTasksAround(today)
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        // Si el día cae fuera de la ventana cargada, refrescar
        val start = loadedStart
        val end = loadedEnd
        if (start == null || end == null || date < start || date > end) {
            fetchTasksAround(date)
        }
    }

    private fun fetchTasksAround(center: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val windowStart = center.minusDays(FETCH_WINDOW_DAYS)
                val windowEnd   = center.plusDays(FETCH_WINDOW_DAYS)

                val tasks = repository.getAllTasks(
                    mine = true,
                    startDate = windowStart.atStartOfDay().toString(),
                    endDate   = windowEnd.atTime(23, 59, 59).toString()
                )

                val grouped = tasks.groupBy { task ->
                    task.deadline?.let { d ->
                        parseDeadline(d)?.toLocalDate()
                    } ?: center
                }

                // Expandir el rango conocido y mergear con el mapa existente
                loadedStart = minOf(loadedStart ?: windowStart, windowStart)
                loadedEnd   = maxOf(loadedEnd   ?: windowEnd,   windowEnd)

                _uiState.update { prev ->
                    prev.copy(
                        tasksByDate = prev.tasksByDate + grouped,
                        isLoading   = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTaskClick(task: Task) {
        // Handled via callback in the UI now
    }
}
