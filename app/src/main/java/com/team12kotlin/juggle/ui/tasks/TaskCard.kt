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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Exclamation
import com.composables.icons.materialsymbols.outlined.Question_mark
import com.team12kotlin.juggle.ui.components.IconMonogram
import com.team12kotlin.juggle.ui.theme.JuggleTheme

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
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (task.isImportant) {
                IconMonogram(MaterialSymbols.Outlined.Exclamation, "Important task")
            } else {
                IconMonogram(MaterialSymbols.Outlined.Question_mark, "Task")
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
        TaskCard(Task(id = "preview", title = "Queso", member = "Quesoso"))
    }
}
