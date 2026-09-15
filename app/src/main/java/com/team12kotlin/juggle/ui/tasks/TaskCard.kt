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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Check
import com.composables.icons.materialsymbols.outlined.Exclamation
import com.team12kotlin.juggle.ui.components.IconMonogram
import com.team12kotlin.juggle.ui.components.TextMonogram
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.theme.JuggleTheme

@Composable
fun TaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    importantIcon: ImageVector = MaterialSymbols.Outlined.Exclamation,
    normalIcon: ImageVector = MaterialSymbols.Outlined.Check,
    importantContainerColor: Color = MaterialTheme.colorScheme.tertiary,
    importantContentColor: Color = MaterialTheme.colorScheme.onTertiary,
    ownContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    ownContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    normalContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    normalContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val (containerColor, contentColor) = when {
                task.isImportant -> importantContainerColor to importantContentColor
                task.member == null -> ownContainerColor to ownContentColor
                else -> normalContainerColor to normalContentColor
            }
            if (task.member != null) {
                TextMonogram(
                    text = task.member.take(1).uppercase(),
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            } else if (task.isImportant) {
                IconMonogram(
                    icon = importantIcon,
                    contentDescription = "Important task",
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            } else {
                IconMonogram(
                    icon = normalIcon,
                    contentDescription = "Task",
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                if (task.member != null) {
                    Text(task.member)
                }
                Text(task.dueLabel, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview
@Composable
private fun TaskCardPreview() {
    JuggleTheme {
        TaskCard(Task(id = "preview", title = "Sample task", member = "Alex"))
    }
}

@Preview
@Composable
private fun OwnTaskCardPreview(){
    JuggleTheme {
        TaskCard(Task(id = "preview", title = "Sample task"))
    }
}

@Preview
@Composable
private fun ImportantTaskCardPreview() {
    JuggleTheme {
        TaskCard(Task(id = "preview-important", title = "Sample task", isImportant = true))
    }
}
