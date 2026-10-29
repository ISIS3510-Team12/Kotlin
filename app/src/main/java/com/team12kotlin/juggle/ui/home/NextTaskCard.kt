package com.team12kotlin.juggle.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Check
import com.team12kotlin.juggle.ui.components.ErrorCard
import com.team12kotlin.juggle.ui.components.IconMonogram
import com.team12kotlin.juggle.ui.components.TextMonogram
import com.team12kotlin.juggle.ui.dto.NextTask
import com.team12kotlin.juggle.ui.dto.NextTaskFlag
import com.team12kotlin.juggle.ui.dto.NextTaskReason
import com.team12kotlin.juggle.ui.dto.toFlag
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.utils.nextTaskMeta

@Composable
fun NextTaskCard(
    task: NextTask,
    flag: NextTaskFlag?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val meta = nextTaskMeta(task.groupName, task.dueAt)
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NextTaskAvatar(flag?.reason ?: task.reason)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (flag != null) {
                    Text(
                        text = flag.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = flag.reason.flagColor(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun NextTaskSection(
    state: NextTaskUiState,
    modifier: Modifier = Modifier,
    onTaskClick: (Int) -> Unit = {}
) {
    when (state) {
        NextTaskUiState.Loading -> Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        is NextTaskUiState.Content -> NextTaskCard(
            task = state.task,
            flag = state.flag,
            modifier = modifier,
            onClick = { onTaskClick(state.task.taskId) }
        )
        is NextTaskUiState.Empty -> Text(
            text = state.message,
            style = MaterialTheme.typography.headlineSmall,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 26.dp, top = 16.dp, end = 26.dp, bottom = 0.dp),
            textAlign = TextAlign.Center
        )
        NextTaskUiState.Error -> ErrorCard(
            message = "Couldn't load the next task",
            modifier = modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NextTaskAvatar(reason: NextTaskReason, modifier: Modifier = Modifier) {
    when (reason) {
        NextTaskReason.OVERDUE,
        NextTaskReason.DUE_TODAY,
        NextTaskReason.PRIORITY -> TextMonogram(
            text = "!",
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
        else -> IconMonogram(
            icon = MaterialSymbols.Outlined.Check,
            contentDescription = "",
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun NextTaskReason.flagColor(): Color = when (this) {
    NextTaskReason.OVERDUE -> MaterialTheme.colorScheme.error
    NextTaskReason.DUE_TODAY,
    NextTaskReason.PRIORITY -> MaterialTheme.colorScheme.tertiary
    NextTaskReason.DUE_SOON -> MaterialTheme.colorScheme.primary
    NextTaskReason.NEEDS_HELP -> MaterialTheme.colorScheme.secondary
    NextTaskReason.NEXT_DEADLINE -> MaterialTheme.colorScheme.onSurfaceVariant
}

private val previewTasks = listOf(
    NextTask(
        taskId = 1, title = "Submit the Sprint 2 report", groupName = "Group dev",
        dueAt = "2026-10-24T17:00:00", reasonCode = "overdue", reasonText = "Overdue by 3 days"
    ),
    NextTask(
        taskId = 2, title = "Finish the Figma prototype", groupName = "Group dev",
        dueAt = "2099-10-28T23:00:00", reasonCode = "due_today", reasonText = "Due today"
    ),
    NextTask(
        taskId = 3, title = "Review the API contract", groupName = "Backend",
        dueAt = "2099-10-29T10:00:00", reasonCode = "due_soon", reasonText = "Due tomorrow"
    ),
    NextTask(
        taskId = 4, title = "Prepare the demo script", groupName = "Group dev",
        dueAt = "2099-11-04T09:00:00", reasonCode = "priority", reasonText = "Marked as priority"
    ),
    NextTask(
        taskId = 5, title = "Fix the geofence receiver", groupName = "Mobile team",
        dueAt = "2099-11-05T15:00:00", reasonCode = "needs_help", reasonText = "Marked as needing help"
    ),
    NextTask(
        taskId = 6, title = "Write the wiki summary", groupName = "Group dev",
        dueAt = "2099-11-08T11:00:00", reasonCode = "next_deadline", reasonText = "Due in 6 days"
    ),
    NextTask(
        taskId = 7, title = "A task with a reason the app does not know about yet", groupName = "",
        dueAt = null, reasonCode = "something_new", reasonText = "Something new"
    )
)

@Preview(showBackground = true)
@Composable
private fun NextTaskCardPreview() {
    JuggleTheme {
        Column {
            previewTasks.forEach { task -> NextTaskCard(task = task, flag = task.toFlag()) }
            NextTaskCard(task = previewTasks[5], flag = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NextTaskCardPreviewDark() {
    JuggleTheme(darkTheme = true) {
        Column {
            previewTasks.forEach { task -> NextTaskCard(task = task, flag = task.toFlag()) }
            NextTaskCard(task = previewTasks[5], flag = null)
        }
    }
}

private val previewStates = listOf(
    NextTaskUiState.Loading,
    NextTaskUiState.Content(previewTasks[1], previewTasks[1].toFlag()),
    NextTaskUiState.Empty(NEXT_TASK_EMPTY_MESSAGE),
    NextTaskUiState.Error
)

@Preview(showBackground = true)
@Composable
private fun NextTaskSectionPreview() {
    JuggleTheme {
        Column {
            previewStates.forEach { state -> NextTaskSection(state = state) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NextTaskSectionPreviewDark() {
    JuggleTheme(darkTheme = true) {
        Column {
            previewStates.forEach { state -> NextTaskSection(state = state) }
        }
    }
}
