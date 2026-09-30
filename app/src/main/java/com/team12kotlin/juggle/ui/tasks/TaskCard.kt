package com.team12kotlin.juggle.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Check
import com.team12kotlin.juggle.ui.components.IconMonogram
import com.team12kotlin.juggle.ui.components.TextMonogram
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.utils.taskDue

@Composable
fun TaskCard(
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskAvatar(task)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (task.needsHelp) {
                    Text(
                        text = "Needs Help.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val assignee = task.assignees.firstOrNull()
                if (assignee != null) {
                    Text(
                        text = assignee.firstName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                taskDue(task.deadline)?.let { due ->
                    Text(
                        text = due.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (due.isOverdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskAvatar(task: Task, modifier: Modifier = Modifier) {
    val assignee = task.assignees.firstOrNull()
    when {
        task.isPriority -> TextMonogram(
            text = "!",
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
        task.status == TaskStatus.COMPLETED -> IconMonogram(
            icon = MaterialSymbols.Outlined.Check,
            contentDescription = "Completed task",
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        assignee != null -> TextMonogram(
            text = assignee.firstName.take(1).uppercase(),
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> IconMonogram(
            icon = MaterialSymbols.Outlined.Check,
            contentDescription = "Task",
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview
@Composable
private fun TaskCardPreview() {
    JuggleTheme {
        Column {
            TaskCard(
                Task(
                    id = 0,
                    title = "Do the design of the app",
                    isPriority = true,
                    deadline = "2026-09-29T23:00:00"
                )
            )
            TaskCard(
                Task(
                    id = 1,
                    title = "Get a 5/5 (hopefully)",
                    status = TaskStatus.COMPLETED,
                    deadline = "2026-09-29T23:00:00"
                )
            )
            TaskCard(
                Task(
                    id = 2,
                    title = "Finish the figma",
                    needsHelp = true,
                    deadline = "2026-09-30T12:00:00",
                    assignees = listOf(User(userId = "1", firstName = "Diego"))
                )
            )
            TaskCard(
                Task(
                    id = 3,
                    title = "Learn how to Figma.",
                    deadline = "2026-09-30T12:00:00",
                    assignees = listOf(User(userId = "1", firstName = "Diego"))
                )
            )
            TaskCard(Task(id = 4, title = "Work"))
        }
    }
}
