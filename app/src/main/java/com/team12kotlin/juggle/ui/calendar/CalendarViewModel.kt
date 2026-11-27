package com.team12kotlin.juggle.ui.calendar

import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.local.CacheResult
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.utils.parseDeadline
import com.team12kotlin.juggle.ui.utils.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

private const val MAX_CACHED_MONTHS = 6
private const val MONTH_CACHE_TTL_MS = 2 * 60 * 1000L

/** A fetched month grid. [fetchedAt] = 0 marks data that must be revalidated (e.g. offline fallback). */
private class MonthEntry(val tasks: Map<LocalDate, List<Task>>, val fetchedAt: Long) {
    fun isFresh(now: Long) = now - fetchedAt < MONTH_CACHE_TTL_MS
}

private const val FETCH_WINDOW_DAYS = 3L   // ±3 días alrededor del día seleccionado

enum class CalendarViewMode { Week, Month }

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val viewMode: CalendarViewMode = CalendarViewMode.Week,
    val visibleMonth: YearMonth = YearMonth.now(),
    val tasksByDate: Map<LocalDate, List<Task>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val dayNotLoaded: Boolean = false,
    val monthTasks: Map<LocalDate, List<Task>> = emptyMap(),
    val isRefreshing: Boolean = false
) {
    val tasksForSelectedDate: List<Task>
        get() = (if (viewMode == CalendarViewMode.Month) monthTasks else tasksByDate)[selectedDate].orEmpty()
}

class CalendarViewModel(
    private val repository: TaskRepository = Dependencies.taskRepository
) : ViewModel() {

    private val today = LocalDate.now()

    private val _uiState = MutableStateFlow(
        CalendarUiState(selectedDate = today)
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // NEW for Sprint 4: LRU of the last months visited, avoids refetching when going back and forth.
    private val monthCache = LruCache<YearMonth, MonthEntry>(MAX_CACHED_MONTHS)

    private var loadedStart: LocalDate? = null
    private var loadedEnd: LocalDate? = null

    init {
        fetchTasksAround(today)
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, dayNotLoaded = false) }
        if (_uiState.value.viewMode == CalendarViewMode.Month) return
        // Si el día cae fuera de la ventana cargada, refrescar
        val start = loadedStart
        val end = loadedEnd
        if (start == null || end == null || date < start || date > end) {
            fetchTasksAround(date)
        }
    }

    fun onViewModeChange(mode: CalendarViewMode) {
        if (mode == _uiState.value.viewMode) return
        _uiState.update { it.copy(viewMode = mode) }
        if (mode == CalendarViewMode.Month) {
            val month = YearMonth.from(_uiState.value.selectedDate)
            _uiState.update { it.copy(visibleMonth = month) }
            loadMonth(month)
        }
    }

    fun onMonthChange(month: YearMonth) {
        _uiState.update { it.copy(visibleMonth = month) }
        loadMonth(month)
    }

    fun onTodayClick() {
        val now = LocalDate.now()
        _uiState.update { it.copy(selectedDate = now, visibleMonth = YearMonth.from(now), dayNotLoaded = false) }
        if (_uiState.value.viewMode == CalendarViewMode.Month) loadMonth(YearMonth.from(now))
        else onDateSelected(now)
    }

    // Pull to refresh -> always hits the repository for the visible month. */
    fun refresh() {
        val state = _uiState.value
        if (state.viewMode == CalendarViewMode.Month) loadMonth(state.visibleMonth, force = true)
        else fetchTasksAround(state.selectedDate)
    }

    // Called when the screen resumes
    fun onResume() {
        val state = _uiState.value
        if (state.viewMode == CalendarViewMode.Month) loadMonth(state.visibleMonth)
    }

    private fun loadMonth(month: YearMonth, force: Boolean = false) {
        val cached = monthCache.get(month)
        _uiState.update { it.copy(monthTasks = cached?.tasks.orEmpty(), dayNotLoaded = false) }
        if (!force && cached != null && cached.isFresh(System.currentTimeMillis())) return

        val first = month.atDay(1)
        val gridStart = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val gridEnd = month.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = cached == null && !force, isRefreshing = force, errorMessage = null)
            }
            try {
                val result = repository.getAllTasksCached(
                    mine = true,
                    startDate = gridStart.atStartOfDay().toString(),
                    endDate = gridEnd.atTime(23, 59, 59).toString()
                )
                val grouped = result.data.groupBy { task ->
                    task.deadline?.let { parseDeadline(it)?.toLocalDate() } ?: first
                }
                val isFresh = result is CacheResult.Fresh
                val hasLocalRange = isFresh || (result is CacheResult.Stale && !result.isCacheMiss)
                if (hasLocalRange) {
                    // Offline/stale data is cached with fetchedAt = 0 so it is revalidated next time.
                    monthCache.put(month, MonthEntry(grouped, if (isFresh) System.currentTimeMillis() else 0L))
                }
                _uiState.update {
                    if (it.visibleMonth != month) it.copy(isLoading = false, isRefreshing = false)
                    else it.copy(
                        monthTasks = if (hasLocalRange) grouped else it.monthTasks,
                        isLoading = false,
                        isRefreshing = false,
                        dayNotLoaded = !hasLocalRange && result is CacheResult.Stale
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, errorMessage = e.toUserMessage())
                }
            }
        }
    }

    private fun fetchTasksAround(center: LocalDate) {
        fetchRange(center.minusDays(FETCH_WINDOW_DAYS), center.plusDays(FETCH_WINDOW_DAYS), center)
    }

    private fun fetchRange(windowStart: LocalDate, windowEnd: LocalDate, fallbackDate: LocalDate) {
        val center = fallbackDate
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {

                val result = repository.getAllTasksCached(
                    mine = true,
                    startDate = windowStart.atStartOfDay().toString(),
                    endDate   = windowEnd.atTime(23, 59, 59).toString()
                )
                val tasks = result.data

                val grouped = tasks.groupBy { task ->
                    task.deadline?.let { d ->
                        parseDeadline(d)?.toLocalDate()
                    } ?: center
                }

                val hasLocalRange =
                    result is CacheResult.Fresh ||
                        (result is CacheResult.Stale && !result.isCacheMiss)
                if (hasLocalRange) {
                    val ls = loadedStart
                    val le = loadedEnd
                    if (ls == null || le == null || windowStart > le.plusDays(1) || windowEnd < ls.minusDays(1)) {
                        loadedStart = windowStart
                        loadedEnd = windowEnd
                    } else {
                        loadedStart = minOf(ls, windowStart)
                        loadedEnd = maxOf(le, windowEnd)
                    }
                }

                val selectedDate = _uiState.value.selectedDate
                val start = loadedStart
                val end = loadedEnd
                val outsideWindow =
                    start == null || end == null || selectedDate < start || selectedDate > end

                _uiState.update { prev ->
                    val kept = prev.tasksByDate.filterKeys { it < windowStart || it > windowEnd }
                    prev.copy(
                        tasksByDate = kept + grouped,
                        isLoading = false,
                        errorMessage = null,
                        dayNotLoaded = outsideWindow && result is CacheResult.Stale
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    val selected = it.selectedDate
                    val start = loadedStart
                    val end = loadedEnd
                    val outsideWindow =
                        start == null || end == null || selected < start || selected > end
                    it.copy(
                        isLoading = false,
                        errorMessage = e.toUserMessage(),
                        dayNotLoaded = outsideWindow
                    )
                }
            }
        }
    }

    fun retry() {
        val state = _uiState.value
        if (state.viewMode == CalendarViewMode.Month) loadMonth(state.visibleMonth, force = true)
        else fetchTasksAround(state.selectedDate)
    }

    fun onTaskClick(task: Task) {
        // Handled via callback in the UI now
    }
}
