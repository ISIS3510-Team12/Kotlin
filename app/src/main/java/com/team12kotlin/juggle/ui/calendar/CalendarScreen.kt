package com.team12kotlin.juggle.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Chevron_left
import com.composables.icons.materialsymbols.outlined.Chevron_right
import com.composables.icons.materialsymbols.outlined.Exclamation
import com.team12kotlin.juggle.ui.components.IconMonogram
import com.team12kotlin.juggle.ui.components.TextMonogram
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.navbar.NavigationDestination
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

    CalendarContent(
        modifier = modifier,
        selectedDate = uiState.selectedDate,
        tasks = uiState.tasksForSelectedDate,
        onProfileClick = onProfileClick,
        onDateSelected = viewModel::onDateSelected,
        onTaskClick = {
            viewModel.onTaskClick(it)
            onTaskClick(it)
        }
    )
}

@Composable
private fun CalendarContent(
    selectedDate: LocalDate,
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    onProfileClick: (NavigationDestination) -> Unit = {},
    onDateSelected: (LocalDate) -> Unit = {},
    onTaskClick: (Task) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(onProfileClick = onProfileClick)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = monthYearLabel(selectedDate),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                style = MaterialTheme.typography.headlineMedium
            )

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
                    text = "No tasks due this day",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                tasks.forEach { task ->
                    ScheduleTaskCard(
                        task = task,
                        onClick = { onTaskClick(task) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(136.dp))
        }
    }
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

@Composable
private fun ScheduleTaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (task.tag != null) {
                Text(
                    text = task.tag,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (task.member != null) {
                    TextMonogram(text = task.member.take(1).uppercase(), size = 28.dp)
                    Text(text = task.member, style = MaterialTheme.typography.bodyMedium)
                } else if (task.isImportant) {
                    IconMonogram(
                        icon = MaterialSymbols.Outlined.Exclamation,
                        contentDescription = "Important task",
                        size = 28.dp,
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
            Text(
                text = task.dueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    id = "preview1",
                    title = "Finish the figma",
                    member = "Diego",
                    tag = "Needs Help.",
                    dueLabel = "Tomorrow - 12 hours left"
                ),
                Task(
                    id = "preview2",
                    title = "Finish the figma",
                    member = "Diego",
                    tag = "Needs Help.",
                    dueLabel = "Tomorrow - 12 hours left"
                )
            )
        )
    }
}
