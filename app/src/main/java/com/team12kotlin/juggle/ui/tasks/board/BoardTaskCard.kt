package com.team12kotlin.juggle.ui.tasks.board

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Lock
import com.composables.icons.materialsymbols.outlined.More_vert
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import com.team12kotlin.juggle.utils.lastUpdateText
import com.team12kotlin.juggle.utils.taskDue

@Composable
internal fun BoardTaskCard(
    task: Task,
    locked: Boolean,
    moving: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onMoveClick: () -> Unit = {}
) {
    val waitingToUpload = task.id < 0

    Card(
        onClick = onClick,
        modifier = modifier.alpha(if (moving) 0.6f else 1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, end = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (waitingToUpload) {
                    Text(
                        text = "Waiting to upload",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (task.isPriority || task.needsHelp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (task.isPriority) {
                            Badge(
                                text = "Priority",
                                container = MaterialTheme.colorScheme.tertiary,
                                content = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                        if (task.needsHelp) {
                            Badge(
                                text = "Needs help",
                                container = MaterialTheme.colorScheme.primaryContainer,
                                content = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                AssigneeLine(task)
                if (!waitingToUpload) {
                    lastUpdateText(task.lastChangedAt)?.let { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            IconButton(onClick = onMoveClick, enabled = !waitingToUpload && !moving) {
                Icon(
                    imageVector = if (locked) MaterialSymbols.Outlined.Lock else MaterialSymbols.Outlined.More_vert,
                    contentDescription = if (locked) "Reconnect to move this task" else "Move to"
                )
            }
        }
    }
}

@Composable
private fun AssigneeLine(task: Task) {
    val assignee = task.assignees.firstOrNull()
    val due = taskDue(task.deadline)
    if (assignee == null && due == null) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        assignee?.let {
            Text(
                text = it.firstName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (assignee != null && due != null) {
            Text(text = "-", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        due?.let {
            Text(
                text = it.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (it.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Badge(
    text: String,
    container: Color,
    content: Color
) {
    Surface(shape = RoundedCornerShape(4.dp), color = container, contentColor = content) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardTaskCardPreview() {
    JuggleTheme {
        BoardTaskCard(
            task = Task(
                id = 1,
                title = "Finish the figma",
                needsHelp = true,
                isPriority = true,
                deadline = "2026-12-01T12:00:00",
                assignees = listOf(User(userId = "1", firstName = "Diego"))
            ),
            locked = false,
            moving = false
        )
    }
}
