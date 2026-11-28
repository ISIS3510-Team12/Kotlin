package com.team12kotlin.juggle.ui.tasks.board

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Check_circle
import com.composables.icons.materialsymbols.outlined.Cloud_off
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus

@Composable
internal fun BoardColumn(
    status: TaskStatus,
    tasks: List<Task>,
    unavailable: Boolean,
    locked: Boolean,
    movingTaskIds: Set<Int>,
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {},
    onMoveClick: (Task) -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColumnHeader(status = status, count = tasks.size)

            if (tasks.isEmpty()) {
                EmptyColumn(status = status, unavailable = unavailable)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        BoardTaskCard(
                            task = task,
                            locked = locked,
                            moving = task.id in movingTaskIds,
                            modifier = Modifier.animateItem(),
                            onClick = { onTaskClick(task) },
                            onMoveClick = { onMoveClick(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnHeader(
    status: TaskStatus,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(status.boardColor(), CircleShape)
        )
        Text(
            text = status.label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp)
            )
        }
    }
}

@Composable
private fun EmptyColumn(
    status: TaskStatus,
    unavailable: Boolean,
    modifier: Modifier = Modifier
) {
    val (title, hint) = when {
        unavailable -> "Couldn't load completed tasks" to "Check your connection and open the board again."
        status == TaskStatus.NOT_STARTED -> "No tasks to start" to "Move a task back here from its menu."
        status == TaskStatus.IN_PROGRESS -> "Nothing in progress" to "Move a task here from its menu to start it."
        else -> "No completed tasks yet" to "Move a task here from its menu to mark it as done."
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (unavailable) MaterialSymbols.Outlined.Cloud_off else MaterialSymbols.Outlined.Check_circle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
