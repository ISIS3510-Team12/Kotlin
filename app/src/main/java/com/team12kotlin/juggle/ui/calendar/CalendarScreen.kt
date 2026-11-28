package com.team12kotlin.juggle.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.team12kotlin.juggle.utils.parseDeadline
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team12kotlin.juggle.ui.telemetry.ScreenName
import com.team12kotlin.juggle.ui.telemetry.TrackScreenLoad
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Chevron_left
import com.composables.icons.materialsymbols.outlined.Chevron_right
import com.team12kotlin.juggle.ui.connectivity.RefreshOnReconnect
import com.team12kotlin.juggle.ui.connectivity.rememberIsOnline
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
import com.team12kotlin.juggle.ui.tasks.TaskCard
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.ui.topbar.AppTopBar
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val DATE_STRIP_DAYS_BEFORE = 180
private const val DATE_STRIP_DAYS_AFTER = 180

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel(),
    onProfileClick: (NavigationDestination) -> Unit = {},
    onTaskClick: (Task) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenLoad(ScreenName.Calendar, uiState.isLoading)
    val isOnline = rememberIsOnline()
    RefreshOnReconnect(isOnline = isOnline, onReconnect = viewModel::retry)
    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose {}
    }

    CalendarContent(
        modifier = modifier,
        selectedDate = uiState.selectedDate,
        viewMode = uiState.viewMode,
        visibleMonth = uiState.visibleMonth,
        tasksByDate = uiState.monthTasks,
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
        onViewModeChange = viewModel::onViewModeChange,
        onMonthChange = viewModel::onMonthChange,
        onTodayClick = viewModel::onTodayClick,
        tasks = uiState.tasksForSelectedDate,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        dayNotLoaded = uiState.dayNotLoaded,
        showCachedData = isOnline == false && uiState.visibleTasks.isNotEmpty(),
        isOffline = isOnline == false,
        onProfileClick = onProfileClick,
        onDateSelected = viewModel::onDateSelected,
        onRetry = viewModel::retry,
        onTaskClick = {
            viewModel.onTaskClick(it)
            onTaskClick(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarContent(
    selectedDate: LocalDate,
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    viewMode: CalendarViewMode = CalendarViewMode.Week,
    visibleMonth: YearMonth = YearMonth.from(selectedDate),
    tasksByDate: Map<LocalDate, List<Task>> = emptyMap(),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onViewModeChange: (CalendarViewMode) -> Unit = {},
    onMonthChange: (YearMonth) -> Unit = {},
    onTodayClick: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    dayNotLoaded: Boolean = false,
    showCachedData: Boolean = false,
    isOffline: Boolean = false,
    onProfileClick: (NavigationDestination) -> Unit = {},
    onDateSelected: (LocalDate) -> Unit = {},
    onRetry: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {}
) {
    // The day sheet is only shown after the user taps a day in the month grid.
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(viewMode) { if (viewMode != CalendarViewMode.Month) sheetOpen = false }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                onProfileClick = onProfileClick,
                showCachedData = showCachedData
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ViewToggle(
                mode = viewMode,
                onModeChange = onViewModeChange,
                modifier = Modifier.padding(start = 20.dp, top = 6.dp, end = 20.dp, bottom = 2.dp)
            )

            if (viewMode == CalendarViewMode.Week) {
                Text(
                    text = monthYearLabel(selectedDate),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (isLoading) {
                androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            errorMessage?.let { message ->
                ErrorCard(
                    message = message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            if (viewMode == CalendarViewMode.Month) {
                MonthHeader(
                    month = visibleMonth,
                    onPrevious = { onMonthChange(visibleMonth.minusMonths(1)) },
                    onNext = { onMonthChange(visibleMonth.plusMonths(1)) },
                    onToday = onTodayClick
                )
                if (isOffline) {
                    // An empty day may just not have been loaded, so don't let it read as "no tasks".
                    Text(
                        text = "You're offline. Some tasks may be missing.",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                MonthGrid(
                    month = visibleMonth,
                    selectedDate = selectedDate,
                    tasksByDate = tasksByDate,
                    onDateSelected = {
                        onDateSelected(it)
                        sheetOpen = true
                    }
                )
                Spacer(modifier = Modifier.height(136.dp))
                return@Column
            }

            DateStrip(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Text(
                text = "Your schedule",
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 4.dp),
                style = MaterialTheme.typography.headlineMedium
            )

            if (tasks.isEmpty()) {
                Text(
                    text = if (dayNotLoaded) {
                        "This day wasn't loaded. Reconnect to see it."
                    } else {
                        "No tasks due this day"
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                tasks.forEach { task ->
                    TaskCard(
                        task = task,
                        onClick = { onTaskClick(task) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(136.dp))
        }
        }

        if (viewMode == CalendarViewMode.Month && sheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { sheetOpen = false },
                sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                DaySheet(
                    selectedDate = selectedDate,
                    tasks = tasks,
                    dayNotLoaded = dayNotLoaded,
                    onTaskClick = {
                        sheetOpen = false
                        onTaskClick(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun ViewToggle(
    mode: CalendarViewMode,
    onModeChange: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(3.dp)
    ) {
        CalendarViewMode.entries.forEach { entry ->
            val selected = entry == mode
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onModeChange(entry) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = monthYearLabel(month.atDay(1)),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        MonthNavButton(MaterialSymbols.Outlined.Chevron_left, "Previous month", onPrevious)
        MonthNavButton(MaterialSymbols.Outlined.Chevron_right, "Next month", onNext)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CHIP_BACKGROUND,
            onClick = onToday
        ) {
            Text(
                text = "Today",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelMedium,
                color = CHIP_CONTENT
            )
        }
    }
}

@Composable
private fun MonthNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = description, modifier = Modifier.size(20.dp))
        }
    }
}

// Chip tones from the design (primary container #E1DFFF / on-container #0C0664).
private val CHIP_BACKGROUND = Color(0xFFE1DFFF)
private val CHIP_CONTENT = Color(0xFF0C0664)

private const val MAX_CHIPS_PER_DAY = 3

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val weeks = remember(month) { monthWeeks(month) }
    val dayLabels = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 7.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp)) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        weeks.forEach { week ->
            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    MonthDayCell(
                        date = date,
                        inMonth = YearMonth.from(date) == month,
                        isToday = date == today,
                        isSelected = date == selectedDate,
                        tasks = tasksByDate[date].orEmpty(),
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    date: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    tasks: List<Task>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val numberBackground = when {
        isToday -> scheme.primary
        isSelected -> CHIP_BACKGROUND
        else -> Color.Transparent
    }
    val numberColor = when {
        isToday -> scheme.onPrimary
        isSelected -> CHIP_CONTENT
        else -> scheme.onSurfaceVariant
    }
    val extra = tasks.size - MAX_CHIPS_PER_DAY

    Column(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(12.dp)).background(numberBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = numberColor.copy(alpha = if (inMonth) 1f else 0.4f)
            )
        }
        tasks.take(MAX_CHIPS_PER_DAY).forEach { task ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CHIP_BACKGROUND.copy(alpha = if (inMonth) 1f else 0.5f))
                    .padding(horizontal = 3.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = task.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = CHIP_CONTENT
                )
            }
        }
        if (extra > 0) {
            Text(
                text = "+$extra",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = scheme.outline
            )
        }
    }
}

private val DAY_SHEET_TITLE: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)
private val DAY_SHEET_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
private const val MAX_SHEET_TASKS = 3

@Composable
private fun DaySheet(
    selectedDate: LocalDate,
    tasks: List<Task>,
    dayNotLoaded: Boolean,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAll by remember(selectedDate) { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme
    val visible = if (showAll) tasks else tasks.take(MAX_SHEET_TASKS)

    Box(modifier = modifier.fillMaxWidth().navigationBarsPadding()) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    text = selectedDate.format(DAY_SHEET_TITLE),
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = if (tasks.size == 1) "1 task" else "${tasks.size} tasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.outline
                )
            }
            if (tasks.isEmpty()) {
                Text(
                    text = if (dayNotLoaded) "This day wasn't loaded. Reconnect to see it."
                    else "No tasks due this day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
            visible.forEach { task ->
                DaySheetTaskRow(task = task, onClick = { onTaskClick(task) })
            }
            if (tasks.size > MAX_SHEET_TASKS) {
                Text(
                    text = if (showAll) "Show less" else "View all ${tasks.size} tasks",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { showAll = !showAll }
                        .padding(vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary
                )
            }
        }
    }
}

@Composable
private fun DaySheetTaskRow(task: Task, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val time = task.deadline?.let { parseDeadline(it) }?.toLocalTime()?.format(DAY_SHEET_TIME)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(scheme.primary)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                task.taskType.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            time?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun monthWeeks(month: YearMonth): List<List<LocalDate>> {
    val first = month.atDay(1)
    val start = first.minusDays((first.dayOfWeek.value % 7).toLong())
    val last = month.atEndOfMonth()
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, last).toInt() + 1
    val weekCount = (totalDays + 6) / 7
    return (0 until weekCount).map { w -> (0 until 7).map { d -> start.plusDays((w * 7 + d).toLong()) } }
}

@Composable
private fun DateStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val dates = remember(today) {
        (-DATE_STRIP_DAYS_BEFORE..DATE_STRIP_DAYS_AFTER).map { today.plusDays(it.toLong()) }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(selectedDate) {
        val index = dates.indexOf(selectedDate)
        if (index >= 0) {
            val target = (index - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDateSelected(selectedDate.minusDays(1)) }) {
            Icon(
                imageVector = MaterialSymbols.Outlined.Chevron_left,
                contentDescription = "Previous day"
            )
        }
        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
        ) {
            items(items = dates, key = { it.toEpochDay() }) { date ->
                DayCell(
                    date = date,
                    selected = date == selectedDate,
                    onClick = { onDateSelected(date) }
                )
            }
        }
        IconButton(onClick = { onDateSelected(selectedDate.plusDays(1)) }) {
            Icon(
                imageVector = MaterialSymbols.Outlined.Chevron_right,
                contentDescription = "Next day"
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .width(56.dp)
            .height(72.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = date.dayOfWeek.twoLetterLabel(), style = MaterialTheme.typography.labelMedium)
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun DayOfWeek.twoLetterLabel(): String = when (this) {
    DayOfWeek.MONDAY -> "Mo"
    DayOfWeek.TUESDAY -> "Tu"
    DayOfWeek.WEDNESDAY -> "We"
    DayOfWeek.THURSDAY -> "Th"
    DayOfWeek.FRIDAY -> "Fr"
    DayOfWeek.SATURDAY -> "Sa"
    DayOfWeek.SUNDAY -> "Su"
}

private fun monthYearLabel(date: LocalDate): String {
    val month = date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    return "$month, ${date.year}"
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    val today = LocalDate.now()
    JuggleTheme {
        CalendarContent(
            selectedDate = today,
            tasks = listOf(
                Task(
                    id = 1,
                    title = "Finish the figma",
                    assignees = listOf(User(userId = "Diego", firstName = "Diego")),
                    needsHelp = true,
                    deadline = today.plusDays(1).atTime(12, 0).toString()
                ),
                Task(
                    id = 2,
                    title = "Review pull request",
                    assignees = listOf(User(userId = "Manuela", firstName = "Manuela")),
                    deadline = today.plusDays(1).atTime(12, 0).toString()
                )
            )
        )
    }
}
